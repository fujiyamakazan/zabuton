package com.github.fujiyamakazan.zabuton.rakutenquest.crawler;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.rakutenquest.JournalCsv;
import com.github.fujiyamakazan.zabuton.rakutenquest.RakutenQuest;
import com.github.fujiyamakazan.zabuton.util.date.Chronus;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;
import com.github.fujiyamakazan.zabuton.util.string.RegexUtils;
import com.github.fujiyamakazan.zabuton.util.text.ShiftJisText;
import com.github.fujiyamakazan.zabuton.util.text.TextMerger;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;
import com.opencsv.CSVParser;

public final class UCSCardCrawler extends JournalCrawler {
    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UCSCardCrawler.class);

    private final JournalCsv master = new JournalCsv(crawlerDir, "master.csv",
        new String[] { "支払方法コード", "支払方法", "利用日", "加盟店名称", "利用金額", "利用者", "海外利用有無サイン", "現地通貨額", "通貨名称", "換算レート" });
    private final File summary = new File(crawlerDir, "summary.txt");

    /**
     * コンストラクタです。
     */
    public UCSCardCrawler(File appDir) {
        super("UCSCard", appDir);
        setMaster(master);
        setSummary(summary);
    }

    @Override
    protected void downloadCore() {

        /*
         * ログイン
         */
        final String url = "https://www.ucscard.co.jp/NetServe/login/";
        cmd.get(url);

        PasswordManager pm = new PasswordManager(crawlerDir);
        pm.executeByUrl(url);

        cmd.type(By.id("webId"), pm.getId());
        cmd.type(By.id("password"), pm.getPassword());
        cmd.clickAndWait(By.xpath("//input[@value='ログイン']"));
        cmd.clickAndWait(By.partialLinkText("ご利用明細照会"));

        /* 明細のダウンロード */
        //String summaryText = "";
        //summaryText = downloadJournal(summaryText);
        downloadJournal();

        /*
         * 残高情報の収集
         */
        downloadSummary();

        //        /* Summary取得 */
        //        new Utf8Text(summary).write(summaryText);

    }

    private void downloadJournal() {
        final TextMerger textMerger = new TextMerger(master, "yyyyMMdd") {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean isAvailableLine(String line) {
                try {
                    //return new CSVParser().parseLine(line)[2].startsWith(String.valueOf(year));
                    return in(new CSVParser().parseLine(line)[2]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        List<By> entryBys = Generics.newArrayList();
        entryBys.add(null); // １件目は移動無し
        entryBys.add(By.xpath("//nav[@class='usage-details_monthly-archive']/ul[@class='list']/li[2]/a")); // 先月
        entryBys.add(By.xpath("//nav[@class='usage-details_monthly-archive']/ul[@class='list']/li[3]/a")); // 先々月
        Iterator<By> iterator = entryBys.iterator();

        while (iterator.hasNext()) {

            /* 前のループでダウンロードしたファイルを削除します。*/
            deletePreFile();

            /* CSVダウンロードボタンのある画面へ移動 */
            By entryBy = iterator.next();
            if (entryBy != null) {
                cmd.clickAndWait(entryBy);
            }

            /* 明細をダウンロード */
            if (cmd.isPresent(By.xpath("//a[img[@alt='ご利用明細ダウンロード(CSV)']]")) == false) {
                continue;
            }
            cmd.clickAndWait(By.xpath("//a[img[@alt='ご利用明細ダウンロード(CSV)']]"));
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
                if (line.startsWith("支払方法コード")) {
                    continue;
                }
                lines.add(line);
            }
            if (textMerger.stock(lines) == false) {
                break;
            }
        }
        textMerger.flash();
        //        return summaryText;
    }

    private void downloadSummary() {

        /* 前の処理でダウンロードしたファイルを削除します。*/
        deletePreFile();

        /* 各月のHTMLをダウンロードします。 */
        By by1 = By.xpath(
            "//section[contains(@class,'usage-details_defined-price')]/section[1]//img[@alt='詳細を見る']"); // 最新
        By by2 = By.xpath(
            "//section[contains(@class,'usage-details_defined-price')]/section[2]//img[@alt='詳細を見る']"); // その前
        By by3 = By.xpath(
            "//section[contains(@class,'usage-details_defined-price')]/section[3]//img[@alt='詳細を見る']"); // 前々月
        List<By> bys = Generics.newArrayList();
        bys.add(by1);
        bys.add(by2);
        bys.add(by3);
        StringBuilder sb = new StringBuilder();
        for (By by : bys) {
            cmd.get("https://www.ucscard.co.jp/NetServe/NavigationAction.do");
            cmd.clickAndWait(by);
            sb.append(cmd.getPageSource());
        }

        saveDaily("summarys.html", sb.toString()); // 当日の作業記録として保存

        /* Summary保存 */
        new Utf8Text(summary).write(sb.toString());

    }

    /**
     * 未払いの金額を返します。
     */
    public String getAssetUCSCredit() {

        String htmlAll = new Utf8Text(summary).read();

        int html1Index = 0;
        int html2Index = htmlAll.indexOf("<html", html1Index + 1);
        int html3Index = htmlAll.indexOf("<html", html2Index + 1);

        List<String> htmls = Generics.newArrayList();
        htmls.add(htmlAll.substring(html1Index, html2Index));
        htmls.add(htmlAll.substring(html2Index, html3Index));
        htmls.add(htmlAll.substring(html3Index));

        int amount = 0;
        String lastPaymentDate = "";
        for (String html : htmls) {
            Document doc = Jsoup.parse(html);

            String amountOfMonth = doc.select("div.usage-details_container dl:nth-child(1)").text();
            if (StringUtils.isEmpty(amountOfMonth)) {
                amountOfMonth = "0";
            } else {
                amountOfMonth = RegexUtils.pickupOne(amountOfMonth, "([0-9,]+円)");
            }
            log.debug(amountOfMonth);

            String strPaymentDate = doc.select("div.usage-details_container dl:nth-child(2)").text();
            strPaymentDate = RegexUtils.pickupOne(strPaymentDate, "([0-9]+年[0-9]+月[0-9]+日)");
            Date paymentDate;
            if (StringUtils.isEmpty(strPaymentDate)) {
                paymentDate = null;
            } else {
                paymentDate = Chronus.parse(strPaymentDate, "yyyy年MM月dd日");
                log.debug(paymentDate.toString());
            }

            if (paymentDate == null) {
                /* 支払日の記載が無い */
                amount += MoneyUtils.toInt(amountOfMonth); // 計上
            } else if (paymentDate.after(new Date())) {
                /* 支払日が未来日付 */
                amount += MoneyUtils.toInt(amountOfMonth); // 計上
                break; // 繰返し処理終了
            } else {
                /* 支払日が過去日付 */
                // 清算済みの為、計上不要
                lastPaymentDate = strPaymentDate;
                break; // 繰返し処理終了
            }

        }

        String msg = String.valueOf(amount) + "円"
            + "(最後の支払日[" + lastPaymentDate + "]以降に発生した金額)";

        return "UCSマジカカード（クレジット）：" + msg;
    }

    public static void main(String[] args) {
        UCSCardCrawler me = new UCSCardCrawler(RakutenQuest.APP_DIR);
        System.out.println(me.getAssetUCSCredit());
    }

}