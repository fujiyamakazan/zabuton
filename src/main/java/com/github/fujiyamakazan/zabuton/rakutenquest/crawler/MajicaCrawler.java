package com.github.fujiyamakazan.zabuton.rakutenquest.crawler;

import java.io.File;
import java.util.List;

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

public final class MajicaCrawler extends JournalCrawler {
    private static final long serialVersionUID = 1L;
    private static final String[] FIELD_NAMES = new String[] { "利用日時", "利用店舗", "入金", "利用" };

    private final JournalCsv master = new JournalCsv(crawlerDir, "master.csv", FIELD_NAMES);
    private final File summary = new File(crawlerDir, "summary.txt");


    public static class MajikaJournalCsv extends JournalCsv {
        private static final long serialVersionUID = 1L;

        public MajikaJournalCsv(File crawlerDir, String name) {
            super(crawlerDir, name, FIELD_NAMES);
        }
    }

    /**
     * コンストラクタです。
     */
    public MajicaCrawler(File appDir) {
        super("Majica",  appDir);
        setMaster(master);
        setSummary(summary);
    }

    @Override
    protected void downloadCore() {

        /*
         * ログイン
         */
        String url = "https://www.giftcard.ne.jp/majica/carduser/";
        cmd.get(url);
        cmd.assertTitleContains("ログイン");

        PasswordManager pm = new PasswordManager(crawlerDir);
        pm.executeByUrl(url);

        cmd.type(By.name("cardNo"), pm.getId());
        cmd.type(By.name("pinOrPassword"), pm.getPassword());
        cmd.clickButtonAndWait("ログイン");

        final TextMerger textMerger = new TextMerger(master, Chronus.POPULAR_JP);

        int roopCounter = 0;
        while (roopCounter < 1) { // 1回のみ
            roopCounter++;

            /* 前のループでダウンロードしたファイルを削除します。*/
            deletePreFile();

            /* 明細を表示 */
            cmd.get("https://www.giftcard.ne.jp/majica/carduser/ReferPage/open.do");
            cmd.assertTitleContains("履歴照会");

            /* HTMLを保存 */
            String html = this.cmd.getPageSource();
            saveDaily("html.txt", html);

            /* CSV形式に変換 */
            List<String> lines = Generics.newArrayList();
            Document doc = Jsoup.parse(html);
            Element table = doc.getElementsByClass("tline").first().getElementsByTag("table").first();
            for (Element tr : table.getElementsByTag("tr")) {
                String date = tr.getElementsByTag("td").get(master.getColumnIndex("利用日時")).text();
                String body = tr.getElementsByTag("td").get(master.getColumnIndex("利用店舗")).text();
                String in = tr.getElementsByTag("td").get(master.getColumnIndex("入金")).text();
                String out = tr.getElementsByTag("td").get(master.getColumnIndex("利用")).text();

                /* 日付の降順となるように前に追加 */
                lines.add(0, CsvUtils.convertString(new String[] { date, body, in, out }));
            }

            if (textMerger.stock(lines) == false) {
                break;
            }
        }
        textMerger.flash();

        /* Summary取得 */
        int total = 0;
        String html = getDownloadTextAsUtf8();
        Document doc = Jsoup.parse(html);
        Elements trs = doc.select("tr");
        for (Element tr : trs) {

            String line = tr.text();
            if (line.startsWith("● 利用可能残高 ：")) {
                line = line.substring("● 利用可能残高 ：".length());
                total = MoneyUtils.toInt(line);
                break;
            }
        }
        new Utf8Text(summary).write(String.valueOf(total));

    }

    public String getAssetMajicaMoney() {
        String num = new Utf8Text(summary).read();
        return "マジカマネー：" + MoneyUtils.toInt(num) + "円";
    }
}