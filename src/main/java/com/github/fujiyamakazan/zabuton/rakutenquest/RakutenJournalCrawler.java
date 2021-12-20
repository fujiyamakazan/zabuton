package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.model.Model;
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
import com.opencsv.CSVParser;

public class RakutenJournalCrawler extends JournalCrawler {
    /** serialVersionUID */
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RakutenJournalCrawler.class);

    @Override
    protected String getName() {
        return "Rakuten";
    }

    @Override
    protected void doDownload() {
        /*
         * ダウンロード処理
         */
        String url = "https://www.rakuten-card.co.jp/e-navi/members/";
        cmd.get(url);
        cmd.assertTitleContains("ログイン画面");

        // TODO アカリスの定期的なダウンロードを促す仕組みとして、ダウンロードしたテキストからチェック処理をいれたい

        PasswordManager pm = new PasswordManager(this.rootDir);
        pm.executeByUrl(url);

        cmd.type(By.name("u"), pm.getId());
        cmd.type(By.name("p"), pm.getPassword());
        cmd.clickAndWait(By.xpath("//input[@value='ログイン']"));

        /* クレジットカード */
        downloadCredit();

        /* ポイント実績 */
        downloadPoint();

    }

    private void downloadCredit() {
        File maasterFileCredit = getMaasterFileCredit();

        log.debug(maasterFileCredit.getAbsolutePath());

        TextMerger textMergerCredit = new TextMerger(maasterFileCredit) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean isAvailableLine(String line) {
                try {
                    return new CSVParser().parseLine(line)[0].startsWith(getYear() + "/");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        for (int i = 0; i < 12; i++) { // 1年分取得

            /* 前のループのファイルを削除する */
            File f = getDownloadFileOne();
            if (getDownloadFileOne() != null) {
                f.delete();
            }
            if (getDownloadFileOne() != null) {
                throw new RuntimeException();
            }

            /*
             * ダウンロード実行
             */
            cmd.get("https://www.rakuten-card.co.jp/e-navi/members/statement/index.xhtml?tabNo=" + i);
            cmd.assertTitleContains("ご利用明細");

            cmd.clickAndWait(By.xpath("//a[contains(@class,'stmt-csv-btn')]")); // ダウンロードボタン
            new DownloadWait().start(); // ファイルダウンロードを待つ

            /*
             * ダウンロードしたファイルを永続化
             */
            f = getDownloadFileOne();
            List<String> lines = new Utf8Text(f).readLines();
            List<String> tmp = Generics.newArrayList();
            for (String line : lines) {
                if (StringUtils.startsWith(line, "\"利用日\"")) { // 見出し行除外
                    continue;
                }
                if (StringUtils.startsWith(line, "\"\",")) { // 追加行
                    tmp.set(tmp.size() - 1, tmp.get(tmp.size() - 1) + "," + line);
                    continue;
                }
                tmp.add(line);
            }
            lines = tmp;
            /* マージ */
            textMergerCredit.stock(lines);
            if (textMergerCredit.hasNext() == false) {
                break;
            }
        }
        if (textMergerCredit.isFinish() == false) {
            /* マスターがあるにもかかわらず、最後に処理したテキストにもマスター追加済みレコードと一致するものが無ければ、
             * 遡及回数の不足と考えられる。処理を中断し、警告をする。
             */
            throw new RuntimeException("遡及処理の上限回数が不足しています。");
        }
        textMergerCredit.flash();
    }

    private void downloadPoint() {
        TextMerger textMergerPoint = new TextMerger(getMaasterFilePoint()) {

            private static final long serialVersionUID = 1L;

            @Override
            protected boolean isAvailableLine(String line) {
                try {
                    return new CSVParser().parseLine(line)[0].startsWith(getYear() + "/");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        for (int i = 1; i <= 15; i++) { // 約一年分
            cmd.get("https://point.rakuten.co.jp/history/?page=" + i + "#point_history");

            /*
             * ファイルを永続化
             */

            /* CSV形式に変換 */
            String html = this.cmd.getPageSource();
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
                lines.add(CsvUtils.convertString(new String[] { strDate, service, naiyo, kubun, value, note }));
            }

            /* マージ */
            textMergerPoint.stock(lines);
            if (textMergerPoint.hasNext() == false) {
                break;
            }
        }
        if (textMergerPoint.isFinish() == false) {
            /* マスターがあるにもかかわらず、最後に処理したテキストにもマスター追加済みレコードと一致するものが無ければ、
             * 遡及回数の不足と考えられる。処理を中断し、警告をする。
             */
            throw new RuntimeException("遡及処理の上限回数が不足しています。");
        }
        textMergerPoint.flash();
    }

    @Override
    protected String doRead() {

        log.debug(this.getMaasterFileCredit().getAbsolutePath());

        StringBuilder sb = new StringBuilder();
        sb.append("--------------------------------------------------\n");
        sb.append(readPoint(recodied));
        sb.append("--------------------------------------------------\n");
        sb.append(readCredit(recodied));
        log.debug("----------------------------------------------------------------------------------");
        return sb.toString();
    }

    /**
     * ポイント実績の処理
     */
    private String readPoint(List<Journal> existDatas) {

        /* 最終行のデータ */
        Model<Journal> lastData = new Model<Journal>();

        /*
         * 成形したデータを出力する
         */
        List<Journal> masterDatas = getPointDatas(lastData, existDatas);

        StringBuilder sb = new StringBuilder();
        if (masterDatas.isEmpty()) {
            sb.append("すべて仕訳済み。最終データ[" + lastData.getObject().getRawOnSource() + "]\n");
        } else {
            for (Journal data : masterDatas) {
                sb.append(data.getJournalString() + "\n");
            }
        }
        return sb.toString();
    }

    /**
     * クレカ明細の処理
     */
    private String readCredit(List<Journal> existDatas) {

        /* 最終行のデータ */
        Model<Journal> lastData = new Model<Journal>();

        /*
         * 成形したデータを出力する
         */
        List<Journal> masterDatas = getCreditDatas(lastData, existDatas);

        StringBuilder sb = new StringBuilder();
        if (masterDatas.isEmpty()) {
            sb.append("すべて仕訳済み。最終データ[" + lastData.getObject().getRawOnSource() + "]\n");
        } else {
            for (Journal data : masterDatas) {
                sb.append(data.getJournalString() + "\n");
            }
        }
        return sb.toString();
    }

    private List<Journal> getPointDatas(Model<Journal> lastData, List<Journal> existDatas) {
        List<Journal> masterDatas = Generics.newArrayList();
        int rowIndex = 0;
        for (String masterLine : new Utf8Text(getMaasterFilePoint()).readLines()) {
            rowIndex++;

            Journal downloadData = new Journal(String.valueOf(rowIndex));
            if (lastData != null) {
                lastData.setObject(downloadData); // 最終データ確保
            }

            String[] datas = CsvUtils.splitCsv(masterLine);
            for (String data : datas) {
                data = data.trim();
            }

            downloadData.setRawOnSource(masterLine);

            String date = datas[0];
            String service = datas[1];
            String naiyo = datas[2];
            Pattern pattern = Pattern.compile("\\[(.+)\\]");
            Matcher matcher = pattern.matcher(naiyo);
            if (matcher.find()) {
                if (date.equals(matcher.group(1)) == false) {
                    /* 発生日と一致しないので情報落ちになってしまう。*/
                } else {
                    naiyo = matcher.replaceAll("");
                }
            }
            final String kubun = datas[3];
            String value = datas[4].replaceAll(",", "");
            String note = "";
            if (datas.length > 5) {
                note = datas[5];
            }

            downloadData.setDate(date);
            downloadData.setAmount(Integer.parseInt(value));
            downloadData.setSource("楽天ポイント実績");

            if (StringUtils.equals(kubun, "獲得") || StringUtils.equals(kubun, "獲得 期間限定")) {
                /*
                 * 区分「獲得」
                 */
                downloadData.setLeft("楽天ペイ");
                downloadData.setRight("ポイント還元");
                downloadData.setActivity("ポイ活");

                /*
                 * 例：ランクアップ対象(2021/01/01より利用可能)
                 */
                naiyo = naiyo.replaceAll("ランクアップ対象", "")
                    .replaceAll(getYear() + "/", "")
                    .replaceAll("でポイントを獲得", "で獲得")
                    .trim();
                note = "";

                downloadData.setMemo(naiyo + note);

            } else if (StringUtils.equals(kubun, "利用")) {
                /*
                 * 区分「利用」
                 */
                downloadData.setLeft("★費用★");
                downloadData.setRight("楽天ペイ");
                downloadData.setActivity("★消費活動★");

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

                downloadData.setMemo(naiyo.trim() + "(" + note + ")");

            } else if (StringUtils.equals(kubun, "利用完了")
                || StringUtils.equals(kubun, "利用 手続き中(申請中)")) {
                /*
                 * 区分「利用完了」「利用 手続き中(申請中)」
                 */

                if (StringUtils.startsWith(naiyo, "投信積立でのポイント利用")) {
                    downloadData.setLeft("楽天証券");
                    downloadData.setRight("楽天ペイ");
                    downloadData.setActivity("投資");
                    downloadData.setMemo("[" + kubun + "]" + service + " " + naiyo);

                } else {
                    throw new RuntimeException("処理区分想定外[" + kubun + "]");
                }

            } else if (StringUtils.equals(kubun, "獲得 キャッシュ")) {
                if (StringUtils.startsWith(naiyo, "楽天キャッシュチャージ(楽天カード)")) {
                    continue; // クレカ明細と重複するため処理しない

                } else {
                    throw new RuntimeException("処理区分想定外[" + kubun + "]");
                }
            }

            /* すでに記録済みのデータなら除外する */
            boolean exist = false;
            if (existDatas != null) {
                for (Journal existData : existDatas) {
                    if (existData.getDate() != null
                        && StringUtils.equals(existData.getSource(), downloadData.getSource())
                        && StringUtils.equals(existData.getRowIndex(), downloadData.getRowIndex())) {
                        exist = true;
                        log.debug("仕訳済み：" + existData);
                        break;
                    }
                }
            }

            if (exist == false) {
                //log.debug("未仕訳：" + downloadData);
                masterDatas.add(downloadData);
            }
        }

        /* パターンごとにソート */
        sort(masterDatas);
        return masterDatas;
    }

    private List<Journal> getCreditDatas(Model<Journal> lastData, List<Journal> existDatas) {
        List<Journal> masterDatas = Generics.newArrayList();
        int rowIndex = 0;
        for (String masterLine : new Utf8Text(getMaasterFileCredit()).readLines()) {
            rowIndex++;
            Journal downloadData = new Journal(String.valueOf(rowIndex));
            if (lastData != null) {
                lastData.setObject(downloadData); // 最終データ確保
            }

            String[] tokens = CsvUtils.splitCsv(masterLine);
            String strDate = tokens[0];

            downloadData.setRawOnSource(masterLine);
            downloadData.setDate(strDate);
            downloadData.setAmount(Integer.parseInt(tokens[4]));
            downloadData.setSource("楽天クレカ明細");
            downloadData.setRight("クレジット"); // 借方に入れて消込む

            String key = tokens[1];

            extracted(downloadData, key);

            if (tokens.length > 11) {
                /* 形式が特殊なレコード */
                downloadData.setMemo(downloadData.getMemo()  + "★割引などの追加情報を処理してください。[" + masterLine + "]★");
            }

            /* すでに記録済みのデータなら除外する */
            boolean exist = false;
            if (existDatas != null) {
                for (Journal existData : existDatas) {
                    if (existData.getDate() != null
                        && StringUtils.equals(existData.getSource(), downloadData.getSource())
                        && StringUtils.equals(existData.getRowIndex(), downloadData.getRowIndex())) {
                        exist = true;
                        log.debug("仕訳済み：" + existData);
                        break;
                    }
                }
            }
            if (exist == false) {
                //log.debug("未仕訳：" + downloadData);
                masterDatas.add(downloadData);
            }
        }
        /* 日付順 にソート
         * してはいけない。
         * 支払月が分からなくなってしまうため。
         */
        //Collections.sort(datas, this.new Data().new MyComparator());

        /* パターンごとにソート */
        sort(masterDatas);
        return masterDatas;
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

    private File getMaasterFileCredit() {
        return new File(rootDir, "credit_" + getYear() + ".csv");
    }

    private File getMaasterFilePoint() {
        return new File(rootDir, "point_" + getYear() + ".csv");
    }

    private static void sort(List<Journal> datas) {
        Collections.sort(datas, new Comparator<Journal>() {

            @Override
            public int compare(Journal o1, Journal o2) {
                int compare = 0;
                if (compare == 0) {
                    compare = o1.getActivity().compareTo(o2.getActivity());
                }
                if (compare == 0) {
                    compare = o1.getMemo().compareTo(o2.getMemo());
                }
                if (compare == 0) {
                    compare = o1.getDate().compareTo(o2.getDate());
                }
                return compare;
            }
        });
    }

}
