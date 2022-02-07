package com.github.fujiyamakazan.zabuton.rakutenquest.crawler;

import java.io.File;
import java.util.List;

import org.apache.wicket.util.lang.Generics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.rakutenquest.JournalCsv;
import com.github.fujiyamakazan.zabuton.rakutenquest.RakutenQuest;
import com.github.fujiyamakazan.zabuton.util.CsvUtils;
import com.github.fujiyamakazan.zabuton.util.date.Chronus;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;
import com.github.fujiyamakazan.zabuton.util.text.TextMerger;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public class RakutenBankCrawler extends JournalCrawler {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RakutenBankCrawler.class);

    private static final String[] FIELD_NAMES = new String[] { "取引日", "入出金内容", "入出金", "取引後残高" };

    private final JournalCsv master = new JournalCsv(crawlerDir, "master.csv", FIELD_NAMES);
    private final File summary = new File(crawlerDir, "summary.txt");

    public static class RakutenBankJournalCsv extends JournalCsv {

        private static final long serialVersionUID = 1L;

        public RakutenBankJournalCsv(File crawlerDir, String name) {
            super(crawlerDir, name, FIELD_NAMES);
        }
    }

    /**
     * コンストラクタです。
     */
    public RakutenBankCrawler(File appDir) {
        super("RakutenBank", appDir);
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
        cmd.sleep(500);

        // TODO ここで支店番号や合言葉を求められる可能性がある。

        cmd.get(
            "https://fes.rakuten-bank.co.jp/MS/main/gns?COMMAND=BALANCE_INQUIRY_START&&CurrentPageID=HEADER_FOOTER_LINK");

        String html = "";
        html += cmd.getPageSource();
        cmd.clickAndWait(By.partialLinkText("入出金明細"));
        cmd.sleep(500);
        html += cmd.getPageSource();
        saveDaily("all.html", html);

        cmd.clickAndWait(By.partialLinkText("ログアウト"));
        cmd.sleep(500);
        cmd.sleep(500);
        cmd.clickAndWait(By.xpath("//img[@alt='はい']"));

        downloadCoreWork();

    }

    private void downloadCoreWork() {
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
            try {
                if (in(line, Chronus.POPULAR_JP) == false) {
                    continue;
                }
            } catch (Exception e) {
                continue;
            }
            List<String> row = Generics.newArrayList();
            for (Element td : tr.getElementsByTag("td")) {
                row.add(td.text());
            }
            rows.add(CsvUtils.convertString(row));
        }

        final TextMerger textMerger = new TextMerger(master, Chronus.POPULAR_JP);
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
        return "楽天銀行残高：" + MoneyUtils.toInt(amount) + "円";
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
        return "楽天証券資産残高：" + MoneyUtils.toInt(amount) + "円";
    }

    public static void main(String[] args) {
        RakutenBankCrawler crawler = new RakutenBankCrawler(RakutenQuest.APP_DIR);
        crawler.downloadCoreWork();
    }
}
