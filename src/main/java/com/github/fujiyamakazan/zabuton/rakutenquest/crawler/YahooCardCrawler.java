package com.github.fujiyamakazan.zabuton.rakutenquest.crawler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.rakutenquest.JournalCsv;
import com.github.fujiyamakazan.zabuton.rakutenquest.RakutenQuest;
import com.github.fujiyamakazan.zabuton.util.StringBuilderLn;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.text.ShiftJisText;
import com.github.fujiyamakazan.zabuton.util.text.TextMerger;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public final class YahooCardCrawler extends JournalCrawler {
    private static final long serialVersionUID = 1L;

    private final JournalCsv masterFile = new JournalCsv(crawlerDir, year + ".csv");
    private final File summary = new File(crawlerDir, "summary_" + year + ".txt");

    /**
     * コンストラクタです。
     */
    public YahooCardCrawler(int year, File appDir) {
        super("YahooCard", year, appDir);
        setMaster(masterFile);
        setSummary(summary);
    }

    //    public static void main(String[] args) {
    //
    //        /* 翌月から過去１年間 */
    //        SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
    //        List<String> days = Generics.newArrayList();
    //        Calendar cal = Calendar.getInstance();
    //        cal.add(Calendar.MONTH, 1);
    //        days.add(df.format(cal.getTime()));
    //        for (int i = 0; i < 12; i++) {
    //            cal.add(Calendar.MONTH, -1);
    //            days.add(df.format(cal.getTime()));
    //        }
    //    }

    @Override
    protected void downloadCore() {

        /*
         * ログイン
         */
        final String url = "https://member1.card.yahoo.co.jp/usage/detail";
        cmd.get(url);

        PasswordManager pm = new PasswordManager(crawlerDir);
        pm.executeByUrl(url);

        cmd.type(By.name("login"), pm.getId());
        cmd.clickAndWait(By.name("btnNext"));
        cmd.type(By.name("passwd"), pm.getPassword());
        cmd.clickAndWait(By.name("btnSubmit"));

        String summaryText = "";
        final TextMerger textMerger = new TextMerger(masterFile, year + "/");

        /* 翌月から過去１年間 */
        SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
        List<String> days = Generics.newArrayList();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        days.add(df.format(cal.getTime()));
        for (int i = 0; i < 12; i++) {
            cal.add(Calendar.MONTH, -1);
            days.add(df.format(cal.getTime()));
        }
        Iterator<String> iterator = days.iterator();

        while (iterator.hasNext()) {

            /* 前のループでダウンロードしたファイルを削除します。*/
            deletePreFile();

            /* CSVダウンロードボタンのある画面へ移動 */
            String day = iterator.next();
            cmd.get("https://member1.card.yahoo.co.jp/usage/detail/" + day);

            if (cmd.containsText("ご利用明細はありません")) {
                continue;
            }

            /* 支払情報収取 */
            String html = this.cmd.getPageSource();
            new Utf8Text(new File(appDir, "test1.html")).write(html); // TODO
            summaryText += Jsoup.parse(html).select("div.mainStatus").text() + ",";

            /* 明細をダウンロード */
            if (cmd.isPresent(By.partialLinkText("CSVダウンロード")) == false) {
                new Utf8Text(new File(appDir, "test2.html")).write(cmd.getPageSource()); // TODO
                continue;
            }
            cmd.clickAndWait(By.partialLinkText("CSVダウンロード"));
            new DownloadWait().start(); // ファイルダウンロードを待つ

            /* CSVを整形 */
            File org = getDownloadFileOne();
            List<String> lines = Generics.newArrayList();
            for (String line : new ShiftJisText(org).readLines()) {
                line = line.trim();
                /* 空行スキップ */
                if (StringUtils.isEmpty(line)) {
                    continue;
                }
                /* 見出し行スキップ */
                if (line.equals("\"利用日\",\"利用店名・商品名\",\"利用者\",\"支払区分\","
                        + "\"利用金額\",\"手数料\",\"支払総額\",\"当月支払金額\",\"翌月以降繰越金額\"")) {
                    continue;
                }
                lines.add(line);
            }
            if (textMerger.stock(lines) == false) {
                break;
            }
        }
        textMerger.flash();

        /* Summary取得 */
        new Utf8Text(summary).write(summaryText);

        /*
         * ログアウト
         */
        cmd.get("https://accounts.yahoo.co.jp/profile");
        cmd.clickAndWait(By.partialLinkText("ログアウト"));

    }

    public static void main(String[] args) {
        String year = "2021";
        //        YahooCardCrawler me = new YahooCardCrawler(2021, RakutenQuest.APP_DIR);
        //        //me.test();
        //        me.download();

        String html = new Utf8Text(new File(RakutenQuest.APP_DIR, "test2.html")).read();
        //        //System.out.println(html);
        //

        StringBuilderLn sb = new StringBuilderLn();

        Elements trs = Jsoup.parse(html).select("tr");
        for (Element tr : trs) {
            String str = tr.text() + ",";
            if (str.startsWith(year + "/") == false) {
                continue;
            }
            StringBuilder sbTd = new StringBuilder();
            for (Element td : tr.select("td")) {
                if (sbTd.length() > 0) {
                    sbTd.append(",");
                }
                sbTd.append("\"" + td.text().trim() + "\"");
            }
            sb.appendLn(sbTd.toString());
        }

        System.out.println(sb.toString());

    }

}