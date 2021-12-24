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
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;
import com.github.fujiyamakazan.zabuton.util.text.TextMerger;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public final class MajicaCrawler extends JournalCrawler {
    private static final long serialVersionUID = 1L;

    private final JournalCsv masterFile = new JournalCsv(crawlerDir, year + ".csv");
    private final File summary = new File(crawlerDir, "summary_" + year + ".txt");

    /**
     * コンストラクタです。
     */
    public MajicaCrawler(int year, File appDir) {
        super("Majica", year, appDir);
        setMaster(masterFile);
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

        final TextMerger textMerger = new TextMerger(masterFile, year + "/");

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
                String date = tr.getElementsByTag("td").get(0).text();
                String body = tr.getElementsByTag("td").get(1).text();
                String in = tr.getElementsByTag("td").get(2).text();
                String out = tr.getElementsByTag("td").get(3).text();

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