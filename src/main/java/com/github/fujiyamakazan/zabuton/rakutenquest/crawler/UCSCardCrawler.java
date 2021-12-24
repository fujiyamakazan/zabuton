package com.github.fujiyamakazan.zabuton.rakutenquest.crawler;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.rakutenquest.JournalCsv;
import com.github.fujiyamakazan.zabuton.rakutenquest.RakutenQuest;
import com.github.fujiyamakazan.zabuton.util.CsvUtils;
import com.github.fujiyamakazan.zabuton.util.date.DateFormatConverter;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.text.ShiftJisText;
import com.github.fujiyamakazan.zabuton.util.text.TextMerger;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;
import com.opencsv.CSVParser;

public final class UCSCardCrawler extends JournalCrawler {
    private static final long serialVersionUID = 1L;

    private final JournalCsv master = new JournalCsv(crawlerDir, year + ".csv");
    private final File summary = new File(crawlerDir, "summary_" + year + ".txt");

    /**
     * コンストラクタです。
     */
    public UCSCardCrawler(int year, File appDir) {
        super("UCSCard", year, appDir);
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

        String summaryText = "";
        final TextMerger textMerger = new TextMerger(master, null) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean isAvailableLine(String line) {
                try {
                    return new CSVParser().parseLine(line)[2].startsWith(String.valueOf(year));
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

            /* 支払情報収取 */
            String html = this.cmd.getPageSource();
            summaryText += "\"" + Jsoup.parse(html).select("dl.usage-details_summary").text() + "\",";

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
                if (line.equals("支払方法コード,支払方法,利用日,加盟店名称,利用金額,利用者,海外利用有無サイン,現地通貨額,通貨名称,換算レート")) {
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

    }

    public String getAssetUCSCredit() {
        String str = new Utf8Text(summary).read();
        StringBuilder sb = new StringBuilder();
        for (String data : CsvUtils.splitCsv(str)) {
            Pattern p = Pattern.compile("今回のお支払総額 (.+)円 お支払日 (.+)");
            Matcher m = p.matcher(data);
            if (m.find()) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                String dateString = m.group(2);
                dateString = DateFormatConverter.convert(dateString, "yyyy年MM月dd日（E）", "MM/dd");
                sb.append(m.group(1) + "円(" + dateString + "精算)");
            }

        }

        return "UCSマジカカード（クレジット）：" + sb.toString();
    }

    public static void main(String[] args) {
        UCSCardCrawler me = new UCSCardCrawler(2021, RakutenQuest.APP_DIR);
        System.out.println(me.getAssetUCSCredit());
    }

}