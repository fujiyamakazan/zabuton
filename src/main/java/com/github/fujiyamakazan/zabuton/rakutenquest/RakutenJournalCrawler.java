package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.util.CsvUtils;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.text.TextMerger;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public class RakutenJournalCrawler extends JournalCrawler {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RakutenJournalCrawler.class);

    public RakutenJournalCrawler(int year, File appDir) {
        super("Rakuten", year, appDir);
    }

    private final File masterCredit = new File(crawlerDir, "credit_" + year + ".csv");
    private final File masterPoint = new File(crawlerDir, "point_" + year + ".csv");

    @Override
    protected void download() {
        /*
         * ダウンロード処理
         */
        String url = "https://www.rakuten-card.co.jp/e-navi/members/";
        cmd.get(url);
        cmd.assertTitleContains("ログイン画面");

        PasswordManager pm = new PasswordManager(crawlerDir);
        pm.executeByUrl(url);

        cmd.type(By.name("u"), pm.getId());
        cmd.type(By.name("p"), pm.getPassword());
        cmd.clickAndWait(By.xpath("//input[@value='ログイン']"));

        /* クレジットカード */
        downloadCredit();

        /* ポイント実績 */
        downloadPoint();

    }

    /**
     * クレカ明細をダウンロードします。
     */
    private void downloadCredit() {

        final TextMerger textMerger = new StandardMerger(masterCredit);

        int roopCounter = -1;
        while (roopCounter < 12) {  // 1年分取得
            roopCounter++;

            /* 前のループでダウンロードしたファイルを削除します。*/
            deletePreFile();

            /* 明細をダウンロード */
            cmd.get("https://www.rakuten-card.co.jp/e-navi/members/statement/index.xhtml?tabNo=" + roopCounter);
            cmd.assertTitleContains("ご利用明細");
            cmd.clickAndWait(By.xpath("//a[contains(@class,'stmt-csv-btn')]")); // ダウンロードボタン
            new DownloadWait().start(); // ファイルダウンロードを待つ

            /* CSVを整形 */
            File fileOriginal = getDownloadFileOne();
            List<String> orignalLine = new Utf8Text(fileOriginal).readLines();
            List<String> tmp = Generics.newArrayList();
            for (String original : orignalLine) {
                if (StringUtils.startsWith(original, "\"利用日\"")) { // 見出し行除外
                    continue;
                }
                if (StringUtils.startsWith(original, "\"\",")) { // 追加行
                    tmp.set(tmp.size() - 1, tmp.get(tmp.size() - 1) + "," + original);
                    continue;
                }
                tmp.add(original);
            }

            List<String> lines = tmp;

            if (textMerger.stock(lines) == false) {
                break;
            }
        }
        textMerger.flash();
    }

    /**
     * ポイント実績をダウンロードします。
     */
    private void downloadPoint() {

        final TextMerger textMerger = new StandardMerger(masterPoint);

        int roopCounter = 0;
        while (roopCounter <= 15) { // 約１年分
            roopCounter++;

            /* 前のループでダウンロードしたファイルを削除します。*/
            deletePreFile();

            cmd.get("https://point.rakuten.co.jp/history/?page=" + roopCounter + "#point_history");

            /* HTMLを保存 */
            String html = this.cmd.getPageSource();
            saveDaily("html.txt", html);

            /* CSV形式に変換 */
            Document doc = Jsoup.parse(html);
            Element table = doc.getElementsByClass("history-table").get(0);
            Elements trs = table.getElementsByTag("tr");
            List<String> lines = Generics.newArrayList();
            for (Element tr : trs) {
                if (tr.attr("class").equals("insert-table")) {
                    continue;
                }
                Elements tds = tr.getElementsByTag("td");
                if (tds.isEmpty()) {
                    continue;
                }
                String strDate = tds.get(0).html();
                strDate = strDate.replaceAll("<br>", "/");
                final String service = tds.get(1).text();
                final String naiyo = tds.get(2).text();
                final String kubun = tds.get(3).text();
                final String value = tds.get(4).text();
                final String note = tds.get(5).text();

                /* 日付の降順となるように前に追加 */
                lines.add(0, CsvUtils.convertString(new String[] { strDate, service, naiyo, kubun, value, note }));
            }

            if (textMerger.stock(lines) == false) {
                break;
            }
        }
        textMerger.flash();
    }

    @Override
    protected List<Journal> createJournal() {
        List<Journal> results = Generics.newArrayList();
        results.addAll(readCredit());
        results.addAll(readPoint());
        return results;
    }

    /**
     * クレカ明細の処理をします。
     */
    private List<Journal> readCredit() {

        /*
         * 成形したデータを出力する
         */
        List<Journal> masterDatas1 = Generics.newArrayList();
        int rowIndex = 0;
        for (String line : new Utf8Text(masterCredit).readLines()) {
            rowIndex++;

            Journal journal = new Journal();
            journal.setSource("楽天クレカ明細");
            journal.setRowIndex(String.valueOf(rowIndex));
            journal.setRawOnSource(line);

            String[] csv = CsvUtils.splitCsv(line);
            journal.setDate(csv[0].trim());
            journal.setAmount(Integer.parseInt(csv[4]));
            journal.setRight("クレジット"); // 借方に入れて消込む
            extracted(journal, csv[1]);
            if (csv.length > 11) {
                /* 形式が特殊なレコード */
                journal.setMemo(journal.getMemo() + "★割引などの追加情報を処理してください。[" + line + "]★");
            }

        }
        List<Journal> masterDatas = masterDatas1;
        return masterDatas;
    }

    /**
     * ポイント実績の処理をします。
     */
    private List<Journal> readPoint() {

        /*
         * 成形したデータを出力する
         */
        List<Journal> results = Generics.newArrayList();
        int rowIndex = 0;
        for (String line : new Utf8Text(masterPoint).readLines()) {
            rowIndex++;

            String[] csv = CsvUtils.splitCsv(line);
            String date = csv[0].trim();
            String service = csv[1].trim();
            String naiyo = csv[2].trim();
            String kubun = csv[3].trim();
            String value = csv[4].trim();
            String note = "";
            if (csv.length > 5) {
                note = csv[5].trim();
            }

            value = value.replaceAll(",", ""); // 金額のカンマを除去

            Pattern pattern = Pattern.compile("\\[(.+)\\]");
            Matcher matcher = pattern.matcher(naiyo);
            if (matcher.find()) {
                if (date.equals(matcher.group(1)) == false) {
                    /* 発生日と一致しないので情報落ちになってしまう。*/
                } else {
                    naiyo = matcher.replaceAll("");
                }
            }

            Journal journal = new Journal();
            journal.setSource("楽天ポイント実績");
            journal.setRowIndex(String.valueOf(rowIndex));
            journal.setRawOnSource(line);

            journal.setDate(date);
            journal.setAmount(Integer.parseInt(value));

            final String left;
            final String right;
            final String activity;
            final String memo;

            if (StringUtils.equals(kubun, "獲得") || StringUtils.equals(kubun, "獲得 期間限定")) {
                /*
                 * 区分「獲得」
                 */
                left = "楽天ペイ";
                right = "ポイント還元";
                activity = "ポイ活";

                memo = naiyo.replaceAll("ランクアップ対象", "")
                        .replaceAll(year + "/", "")
                        .replaceAll("でポイントを獲得", "で獲得")
                        .trim();

            } else if (StringUtils.equals(kubun, "利用")) {
                /*
                 * 区分「利用」
                 */
                left = "★費用★";
                right = "楽天ペイ";
                activity = "★消費活動★";

                /*
                 * [利用]楽天ペイ/ファミリーマート楽天ペイでポイントを利用 [2021/01/08](内訳(ポイント優先利用) 5,000円 0ポイント)
                 * → 日付は発生日と一致するので不要
                 * → その他不要な文字をカット
                 */
                naiyo = naiyo.replaceAll(Pattern.quote("楽天ペイでポイントを利用"), "");
                note = note.replaceAll(Pattern.quote("内訳(ポイント優先利用) "), "");

                Pattern p = Pattern.compile("(.+)円 (.+)ポイント");
                Matcher m = p.matcher(note);
                if (m.find()) {
                    note = "含む" + m.group(2) + "pt";
                }

                memo = naiyo.trim() + "(" + note + ")";

            } else if (StringUtils.equals(kubun, "利用完了")
                    || StringUtils.equals(kubun, "利用 手続き中(申請中)")) {
                /*
                 * 区分「利用完了」「利用 手続き中(申請中)」
                 */

                if (StringUtils.startsWith(naiyo, "投信積立でのポイント利用")) {
                    left = "楽天証券";
                    right = "楽天ペイ";
                    activity = "投資";
                    memo = "[" + kubun + "]" + service + " " + naiyo;

                } else {
                    throw new RuntimeException("処理区分想定外[" + kubun + "]");
                }

            } else if (StringUtils.equals(kubun, "獲得 キャッシュ")) {
                if (StringUtils.startsWith(naiyo, "楽天キャッシュチャージ(楽天カード)")) {
                    continue; // クレカ明細と重複するため処理しない

                } else {
                    throw new RuntimeException("処理区分想定外[" + kubun + "]");
                }
            } else {
                throw new RuntimeException("処理区分想定外[" + kubun + "]");
            }

            journal.setLeft(left);
            journal.setRight(right);
            journal.setActivity(activity);
            journal.setMemo(memo);
            results.add(journal);

        }

        return results;
    }

    protected void extracted(Journal downloadData, String key) {
        final String left;
        final String activity;
        final String memo;
        if (key.startsWith("楽天キャッシュ・チャージ")) {
            left = "楽天ペイ";
            activity = "チャージ";
            memo = key;

        } else if (key.equals("Ｅｄｙチャージ※楽天ｅ－ＮＡＶ")
                || key.equals("Ｅｄｙオートチャージ")) {
            left = "Edy";
            activity = "チャージ";
            memo = key;

        } else if (key.equals("AMAZON.CO.JP")) {
            left = "★費用★";
            activity = "★消費活動★";
            memo = "★アマゾンの注文履歴をチェックして記入★" + key;

        } else {
            left = "★費用★";
            activity = "★消費活動★";
            memo = "★レシートをチェックして記入★" + key;
        }
        downloadData.setLeft(left);
        downloadData.setActivity(activity);
        downloadData.setMemo(memo);
    }

}
