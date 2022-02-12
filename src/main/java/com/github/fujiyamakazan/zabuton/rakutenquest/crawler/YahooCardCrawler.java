package com.github.fujiyamakazan.zabuton.rakutenquest.crawler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.rakutenquest.JournalCsv;
import com.github.fujiyamakazan.zabuton.util.CsvUtils;
import com.github.fujiyamakazan.zabuton.util.date.Chronus;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;
import com.github.fujiyamakazan.zabuton.util.text.TextMerger;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public final class YahooCardCrawler extends JournalCrawler {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(YahooCardCrawler.class);

    private static final String[] FIELD_NAMES = new String[] { "利用日", "利用店名・商品名", "利用金額" };

    private final JournalCsv master = new JournalCsv(crawlerDir, "master.csv",
        FIELD_NAMES);
    private final File summary = new File(crawlerDir, "summary.txt");

    public static class YahooCardJournalCsv extends JournalCsv {
        private static final long serialVersionUID = 1L;

        public YahooCardJournalCsv(File crawlerDir, String name) {
            super(crawlerDir, name, FIELD_NAMES);
        }
    }

    /**
     * コンストラクタです。
     */
    public YahooCardCrawler(File appDir) {
        super("YahooCard", appDir);
        setMaster(master);
        setSummary(summary);
    }

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

        /*
         * 明細情報の収集
         */
        downloadJournal();

        /*
         * 残高情報の収集
         */
        downloadSummary();

        /*
         * ログアウト
         */
        cmd.get("https://accounts.yahoo.co.jp/profile");
        cmd.clickAndWait(By.partialLinkText("ログアウト"));

    }

    /**
     * 明細情報の収集をします。
     */
    private void downloadJournal() {
        final TextMerger textMerger = new TextMerger(master, Chronus.POPULAR_JP);

        /* 翌月から過去１年間 */
        Iterator<String> iteratorJournal = createIte();

        while (iteratorJournal.hasNext()) {

            /* 前のループでダウンロードしたファイルを削除します。*/
            deletePreFile();

            /* CSVダウンロードボタンのある画面へ移動 */
            String day = iteratorJournal.next();
            cmd.get("https://member1.card.yahoo.co.jp/usage/detail/" + day);

            if (cmd.containsText("ご利用明細はありません", DEFAULT_TIMEOUT)) {
                continue;
            }

            /* 明細情報を取得 */
            final List<String> lines = Generics.newArrayList();
            String html = cmd.getPageSource();
            saveDaily(day + ".html", html);
            Document doc = Jsoup.parse(html);

            boolean presentCsv = cmd.isPresent(By.partialLinkText("CSVダウンロード"));

            Elements trs = doc.select("div.mainDetail__table--big table tr");
            if (presentCsv) {
                /* 「CSVダウンロード」が表示されている画面のレイアウトに対応する */
                for (String orgString : getJournalCsv(this, trs)) {
                    String[] org = CsvUtils.splitCsv(orgString);
                    String[] csv = new String[3]; // 日付、内容、金額
                    csv[0] = org[0].replace(" ", "/"); // 日付を [2021 10/18] から [2021/10/18] へ変更
                    csv[1] = org[1];
                    csv[2] = org[7]; // 当月支払金額
                    lines.add(CsvUtils.convertString(csv));
                }
            } else {
                /* 「CSVダウンロード」が表示されていない画面のレイアウトに対応する */
                for (String orgString : getJournalCsv(this, trs)) {
                    String[] org = CsvUtils.splitCsv(orgString);
                    String[] csv = new String[3]; // 日付、内容、金額
                    csv[0] = org[0];
                    csv[1] = org[1];
                    csv[2] = org[3]; // 利用金額
                    lines.add(CsvUtils.convertString(csv));
                }
            }

            /* 明細情報をマージ用に保存する */
            if (textMerger.stock(lines) == false) {
                break;
            }
        }

        /* 明細情報をマージする */
        textMerger.flash();
    }

    /**
     * 残高情報の収集をします。
     */
    private void downloadSummary() {

        /* 翌月から過去１年間 */
        Iterator<String> iteratorSummary = createIte();

        int amount = 0;
        String lastPaymentDate = "";
        while (iteratorSummary.hasNext()) {

            /* 前のループでダウンロードしたファイルを削除します。*/
            deletePreFile();

            String day = iteratorSummary.next();
            cmd.get("https://member1.card.yahoo.co.jp/usage/detail/" + day);

            if (cmd.containsText("ご利用明細はありません", DEFAULT_TIMEOUT)) {
                continue;
            }

            String html = cmd.getPageSource();
            saveDaily(day + ".html", html);

            /* 残高集計 */
            Document doc = Jsoup.parse(html);
            String amountOfMonth = "";
            Elements trs = doc.select("div.mainDetail__table--big table tr");
            for (Element tr : trs) {
                String text = tr.text();
                if (StringUtils.startsWith(text, "回数指定払い小計")) {
                    amountOfMonth = tr.getElementsByTag("td").get(1).text();
                    break;
                }
            }

            String paymentMonth = doc.select("p.calender__month").text();
            String paymentDay = doc.select("p.calender__date").text();
            if (StringUtils.isEmpty(paymentMonth)) {
                amount += MoneyUtils.toInt(amountOfMonth); // 計上
            } else {
                String strPaymentDate = day.substring(0, 4) + "年" + paymentMonth + paymentDay;
                Date paymentDate = Chronus.parse(strPaymentDate, "yyyy年MM月dd日");
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
        }
        new Utf8Text(summary).write(String.valueOf(amount) + "円"
            + "(最後の支払日[" + lastPaymentDate + "]以降に発生した金額)");
    }

    private static Iterator<String> createIte() {
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
        return iterator;
    }

    private List<String> getJournalCsv(YahooCardCrawler me, Elements trs) {
        List<String> csvs = Generics.newArrayList();
        Tr: for (Element tr : trs) {
            List<String> csv = Generics.newArrayList();
            for (Element td : tr.getElementsByTag("td")) {
                String strTd = td.text();
                if (StringUtils.isBlank(strTd)) {
                    continue;
                }
                strTd = strTd.trim();

                if (csv.isEmpty()) {

                    try {
                        if (in(strTd, Chronus.POPULAR_JP) == false
                            && in(strTd, "yyyy MM/dd") == false) {
                            continue Tr;
                        }
                    } catch (Exception e) {
                        continue Tr;
                    }
                }
                csv.add(strTd);
            }
            if (csv.isEmpty() == false) {
                csvs.add(CsvUtils.convertString(csv));
            }
        }
        return csvs;
    }

    public String getAssetYahooCredit() {
        return "ヤフーカード（クレジット）：" + new Utf8Text(summary).read();
    }

    //    public static void main(String[] args) {
    //        YahooCardCrawler yahoo = new YahooCardCrawler(2021, RakutenQuest.APP_DIR);
    //        String html = new Utf8Text(new File(yahoo.crawlerDir, "202201.html")).read();
    //        Document doc = Jsoup.parse(html);
    //
    //    }

}