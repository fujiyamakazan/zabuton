package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.util.CsvUtils;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;
import com.github.fujiyamakazan.zabuton.util.text.TextMerger;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public class RakutenCrawler extends JournalCrawler {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RakutenCrawler.class);

    public static final String CREDIT = "CREDIT";
    public static final String POINT = "POINT";

    private final JournalCsv masterCredit = new JournalCsv(crawlerDir, "credit_" + year + ".csv");
    private final JournalCsv masterPoint = new JournalCsv(crawlerDir, "point_" + year + ".csv");
    private final File summary = new File(crawlerDir, "summary_" + year + ".txt");

    public static void main(String[] args) {

        RakutenCrawler c = new RakutenCrawler(2021, EnvUtils.getUserDesktop("RakutenQuest3"));

        //        Utf8Text text = new Utf8Text(c.getMaster(CREDIT));
        //        List<String> lines = text.readLines();
        //        int rowIndex = 1;
        //        List<String> newLines = Generics.newArrayList();
        //        for (String line: lines) {
        //            line = "\""+ (rowIndex++)+"\"," + line;
        //            newLines.add(line);
        //        }
        //        text.writeLines(newLines);

//        Utf8Text text = new Utf8Text(c.getMaster(POINT));
//        List<String> lines = text.readLines();
//        int rowIndex = 1;
//        List<String> newLines = Generics.newArrayList();
//        for (String line : lines) {
//            line = "\"" + (rowIndex++) + "\"," + line;
//            newLines.add(line);
//        }
//        text.writeLines(newLines);

    }

    /**
     * コンストラクタです。
     */
    public RakutenCrawler(int year, File appDir) {
        super("Rakuten", year, appDir);
        setMaster(CREDIT, masterCredit);
        setMaster(POINT, masterPoint);
        setSummary(summary);
    }

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

        /* 残高 */
        downloadSummary();
    }

    private void downloadSummary() {

        /* 前の処理でダウンロードしたファイルを削除します。*/
        deletePreFile();

        cmd.get("https://point.rakuten.co.jp/history/?l-id=point_top_history_pc");

        /* HTMLを保存 */
        String html = this.cmd.getPageSource();
        saveDaily("html.txt", html);

        Document doc = Jsoup.parse(html);
        Elements points = doc.select("h2.box_point-total span.point_total");
        int point1 = MoneyUtils.toInt(points.get(0).text());
        int point2 = MoneyUtils.toInt(points.get(1).text());
        int point3 = MoneyUtils.toInt(doc.select("h2.box_cash-total span.point_total").text());
        int total = point1 + point2 + point3;

        //        log.debug("nomal:" + point1);
        //        log.debug("予定:" + point2);
        //        log.debug("cash:" + point3);
        //        log.debug("total:" + total);

        new Utf8Text(summary).write(String.valueOf(total));
    }

    /**
     * クレカ明細をダウンロードします。
     */
    private void downloadCredit() {

        final TextMerger textMerger = new StandardMerger(masterCredit);

        int roopCounter = -1;
        while (roopCounter < 12) { // 1年分取得
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
                String note = tds.get(5).text();
                /* 「獲得予定ポイント 」は取得のタイミングで消えるため、除去 */
                if (note.startsWith("獲得予定ポイント ")) {
                    note = note.substring("獲得予定ポイント ".length());
                }

                /* 日付の降順となるように前に追加 */
                lines.add(0, CsvUtils.convertString(new String[] { strDate, service, naiyo, kubun, value, note }));
            }

            if (textMerger.stock(lines) == false) {
                break;
            }
        }
        textMerger.flash();
    }

}
