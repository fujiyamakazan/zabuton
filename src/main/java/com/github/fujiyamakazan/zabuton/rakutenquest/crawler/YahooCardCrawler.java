package com.github.fujiyamakazan.zabuton.rakutenquest.crawler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import com.github.fujiyamakazan.zabuton.rakutenquest.RakutenQuest;
import com.github.fujiyamakazan.zabuton.util.CsvUtils;
import com.github.fujiyamakazan.zabuton.util.ListToStringer;
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

            if (cmd.containsText("ご利用明細はありません")) { // TODO タイムアウトマジ時間がかかることを改善したい
                //if (cmd.containsText("利用店名・商品名") == false) {
                continue;
            }

            /* 支払情報収取 */
            String html = this.cmd.getPageSource();
            new Utf8Text(new File(appDir, "test1.html")).write(html); // TODO
            summaryText += "\"" + Jsoup.parse(html).select("div.mainStatus").text() + "\",";

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

    public String getAssetYahooCredit() {
//        String str = new Utf8Text(summary).read();
//        StringBuilder sb = new StringBuilder();
//        for (String data : CsvUtils.splitCsv(str)) {
//            Pattern p = Pattern.compile("お支払い日 (.+)ご請求金額(.+)円");
//            Matcher m = p.matcher(data);
//            if (m.find()) {
//                if (sb.length() > 0) {
//                    sb.append(",");
//                }
//                String dateString = m.group(1);
//                dateString = DateFormatConverter.convert(dateString, "MM月 dd日", "MM/dd");
//                sb.append(m.group(2) + "円(" + dateString + "精算)");
//            }
//
//        }
        return "ヤフーカード（クレジット）：" + "★実装中★" ;
    }

    public static void main(String[] args) {
        YahooCardCrawler me = new YahooCardCrawler(2021, RakutenQuest.APP_DIR);
        me.download();

        System.out.println(me.getAssetYahooCredit());

        Document doc202111 = Jsoup.parse(new Utf8Text(new File(me.crawlerDir, "202111.html")).read());
        //System.out.println(doc202111.select("h2.mainStatus__title span").text());
        System.out.println(doc202111.select("div.mainStatus__right--payment em.money").text());
        System.out.println(doc202111.select("p.calender__month").text());
        System.out.println(doc202111.select("p.calender__date").text());
        System.out.println(doc202111.select("p.calender__date").text());

        /* 残高集計 */
        Elements trs20211 = doc202111.select("div.mainDetail__table--big table tr");
        String amount = "";
        for (Element tr : trs20211) {
            String text = tr.text();
            if (StringUtils.startsWith(text, "回数指定払い小計")) {
                amount = tr.getElementsByTag("td").get(1).text();
                break;
            }

        }
        System.out.println(amount);

        /* CSV修正（確定月） */
        List<String> tmpKakutei = Generics.newArrayList();
        for (String orgString: getJournalCsv(me, trs20211)) {
            String[] org = CsvUtils.splitCsv(orgString);
            String[] csv = new String[3]; // 日付、内容、金額
            csv[0] = org[0].replace(" ", "/"); // 日付を [2021 10/18] から [2021/10/18] へ変更
            csv[1] = org[1];
            csv[2] = org[7]; // 当月支払金額

            tmpKakutei.add(CsvUtils.convertString(csv));
        }
        System.out.println(ListToStringer.convert(tmpKakutei));


        System.out.println("-----");

        Document doc202201 = Jsoup.parse(new Utf8Text(new File(me.crawlerDir, "202201.html")).read());
        System.out.println(doc202201.select("h2.mainStatus__title span").text());
        System.out.println(doc202201.select("div.mainStatus__right--payment em.money").text());
        System.out.println(doc202201.select("p.calender__month").text());
        System.out.println(doc202201.select("p.calender__date").text());


        Elements trs202201 = doc202201.select("div.mainDetail__table--big table tr");

        /* CSV修正（未確定月） */
        List<String> tmpMikaku = Generics.newArrayList();
        for (String orgString: getJournalCsv(me, trs202201)) {
            String[] org = CsvUtils.splitCsv(orgString);
            String[] csv = new String[3]; // 日付、内容、金額
            csv[0] = org[0];
            csv[1] = org[1];
            csv[2] = org[3]; // 利用金額

            tmpMikaku.add(CsvUtils.convertString(csv));
        }

        System.out.println(ListToStringer.convert(tmpMikaku, "\n"));


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

}