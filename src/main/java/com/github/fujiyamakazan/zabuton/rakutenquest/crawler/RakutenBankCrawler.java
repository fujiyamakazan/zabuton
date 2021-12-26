package com.github.fujiyamakazan.zabuton.rakutenquest.crawler;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.rakutenquest.JournalCsv;
import com.github.fujiyamakazan.zabuton.util.CsvUtils;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.text.TextMerger;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public class RakutenBankCrawler extends JournalCrawler {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RakutenBankCrawler.class);

    private final JournalCsv master = new JournalCsv(crawlerDir, year + ".csv");
    private final File summary = new File(crawlerDir, "summary_" + year + ".txt");

    /**
     * コンストラクタです。
     */
    public RakutenBankCrawler(int year, File appDir) {
        super("RakutenBank", year, appDir);
        setMaster(master);
        setSummary(summary);
    }

    @Override
    protected void downloadCore() {

        String url = "https://fes.rakuten-bank.co.jp/MS/main/RbS?CurrentPageID=START&&COMMAND=LOGIN";
        cmd.get(url);
        cmd.assertTitleContains("ようこそ");

        PasswordManager pm = new PasswordManager(crawlerDir);
        pm.executeBySightKey("rakuten-bank-01");

        cmd.type(By.name("LOGIN:USER_ID"), pm.getId());
        cmd.type(By.name("LOGIN:LOGIN_PASSWORD"), pm.getPassword());
        cmd.clickAndWait(By.partialLinkText("ログイン"));

        String html = "";
        html += cmd.getPageSource();
        cmd.clickAndWait(By.partialLinkText("入出金明細"));
        html += cmd.getPageSource();
        cmd.clickAndWait(By.partialLinkText("ログアウト"));
        cmd.clickAndWait(By.xpath("//img[@alt='はい']"));


        /*
         * 明細解析
         */
        File file = getDownloadFileOne();
        String htmlAll = new Utf8Text(file).read();
        int html2Index = htmlAll.indexOf("<html", 1);
        String htmlList = htmlAll.substring(html2Index);

        List<String> rows = Generics.newArrayList();
        Document docList = Jsoup.parse(htmlList);
        for (Element tr : docList.select("table tr")) {
            String line = tr.text();
            if (StringUtils.startsWith(line, year + "/") == false) {
                continue;
            }
            List<String> row = Generics.newArrayList();
            for (Element td : tr.getElementsByTag("td")) {
                row.add(td.text());
            }
            rows.add(CsvUtils.convertString(row));
        }

        final TextMerger textMerger = new TextMerger(master, year + "/");
        textMerger.stock(rows);
        textMerger.flash();

        /* HTMLを保存 */
        new Utf8Text(summary).write(htmlAll);

    }

    /**
     * 楽天銀行の残高を返します。
     */
    public String getAssetBank() {
        String htmlAll = new Utf8Text(summary).read();
        int html2Index = htmlAll.indexOf("<html", 1);
        String htmlTop = htmlAll.substring(0, html2Index);
        Document docTop = Jsoup.parse(htmlTop);
        String amount = docTop.select("div#lyt-deposit table span.amount").text();
        return "楽天銀行残高：" + amount + "円";
    }

    /**
     * 楽天証券の資産残高を返します。
     */
    public String getAssetSecurities() {
        String htmlAll = new Utf8Text(summary).read();
        int html2Index = htmlAll.indexOf("<html", 1);
        String htmlTop = htmlAll.substring(0, html2Index);
        Document docTop = Jsoup.parse(htmlTop);
        String amount = docTop.select("div#lyt-rakuten-sec span.amount").text();
        return "楽天証券資産残高：" + amount + "円";
    }

//    public static void main(String[] args) {

//        RakutenBankCrawler crawler = new RakutenBankCrawler(2021, RakutenQuest.APP_DIR);
//        crawler.download();
//
//        File file = crawler.getDownloadFileOne();
//        String htmlAll = new Utf8Text(file).read();
//        int html2Index = htmlAll.indexOf("<html", 1);
//        String htmlTop = htmlAll.substring(0, html2Index);
//        String htmlList = htmlAll.substring(html2Index);
//
//        /*
//         * 明細保存
//         */
//        List<String> rows = Generics.newArrayList();
//        Document docList = Jsoup.parse(htmlList);
//        for (Element tr : docList.select("table tr")) {
//            String line = tr.text();
//            if (StringUtils.startsWith(line, crawler.year + "/") == false) {
//                continue;
//            }
//            List<String> row = Generics.newArrayList();
//            for (Element td : tr.getElementsByTag("td")) {
//                row.add(td.text());
//            }
//            rows.add(CsvUtils.convertString(row));
//        }
//        //System.out.println(ListToStringer.convert(rows, "\n"));
//
//        final TextMerger textMerger = new TextMerger(crawler.master, crawler.year + "/");
//        textMerger.stock(rows);
//        textMerger.flash();
//
//        /*
//         * サマリ保存
//         */
//        Document docTop = Jsoup.parse(htmlTop);
//        System.out.println(docTop.select("div#lyt-rakuten-sec span.amount").text());
//        System.out.println(docTop.select("div#lyt-deposit table span.amount").text());

//    }

}
