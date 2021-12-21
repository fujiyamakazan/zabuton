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
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.text.TextMerger;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public class RakutenCrawler extends JournalCrawler {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RakutenCrawler.class);

    public static final String CREDIT = "CREDIT";
    public static final String POINT = "POINT";


    private final File masterCredit = new File(crawlerDir, "credit_" + year + ".csv");
    private final File masterPoint = new File(crawlerDir, "point_" + year + ".csv");

    public RakutenCrawler(int year, File appDir) {
        super("Rakuten", year, appDir);
        setMaster(CREDIT, masterCredit);
        setMaster(POINT, masterPoint);
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
    public String getText() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }





}
