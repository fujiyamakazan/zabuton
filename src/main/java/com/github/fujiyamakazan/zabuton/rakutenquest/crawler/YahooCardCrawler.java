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
import com.github.fujiyamakazan.zabuton.util.date.DateFormatConverter;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;
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

        final TextMerger textMerger = new TextMerger(masterFile, year + "/");

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

        /*
         * 残高情報の収集
         */

        /* 翌月から過去１年間 */
        Iterator<String> iteratorSummary = createIte();

        int amount = 0;
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
            Document doc = Jsoup.parse(html);

            /* 残高集計 */
            String amountOfMonth = "";
            Elements trs = doc.select("div.mainDetail__table--big table tr");
            for (Element tr : trs) {
                String text = tr.text();
                if (StringUtils.startsWith(text, "回数指定払い小計")) {
                    amountOfMonth = tr.getElementsByTag("td").get(1).text();
                    break;
                }

            }

            String paymonth = doc.select("p.calender__month").text();
            String payday = doc.select("p.calender__date").text();
            if (StringUtils.isEmpty(paymonth)) {
                amount += MoneyUtils.toInt(amountOfMonth); // 計上
            } else {
                String strPaydate = year + "年" + paymonth + payday;
                Date padate = DateFormatConverter.parse(strPaydate, "yyyy年MM月dd日");
                if (padate == null) {
                    /* 支払日の記載が無い */
                    amount += MoneyUtils.toInt(amountOfMonth); // 計上
                } else if (padate.after(new Date())) {
                    /* 支払日が未来日付 */
                    amount += MoneyUtils.toInt(amountOfMonth); // 計上
                    break; // 繰返し処理終了
                } else {
                    /* 支払日が過去日付 */
                    // 清算済みの為、計上不要
                    break; // 繰返し処理終了
                }
            }
        }
        new Utf8Text(summary).write(String.valueOf(amount));

        /*
         * ログアウト
         */
        cmd.get("https://accounts.yahoo.co.jp/profile");
        cmd.clickAndWait(By.partialLinkText("ログアウト"));

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

    private static List<String> getJournalCsv(YahooCardCrawler me, Elements trs) {
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
                    /* 1列目が該当年度でなけれえば行をスキップ */
                    if (strTd.startsWith(me.year + "/") == false
                            && strTd.startsWith(me.year + " ") == false) {
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


}