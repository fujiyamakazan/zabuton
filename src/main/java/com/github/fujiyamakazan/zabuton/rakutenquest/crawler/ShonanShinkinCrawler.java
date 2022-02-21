package com.github.fujiyamakazan.zabuton.rakutenquest.crawler;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.rakutenquest.JournalCsv;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;
import com.github.fujiyamakazan.zabuton.util.text.ShiftJisText;
import com.github.fujiyamakazan.zabuton.util.text.TextMerger;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;
import com.opencsv.CSVParser;

public final class ShonanShinkinCrawler extends JournalCrawler {
    private static final long serialVersionUID = 1L;

    private final JournalCsv master = new JournalCsv(this.crawlerDir, "master.csv",
        new String[] { "年月日", "摘要", "お支払金額", "お預り金額", "差引残高" });
    private final File summary = new File(this.crawlerDir, "summary.txt");

    /**
     * コンストラクタです。
     */
    public ShonanShinkinCrawler(File appDir) {
        super("ShonanShinkin", appDir);
        setMaster(this.master);
        setSummary(this.summary);
    }

    @Override
    protected void downloadCore() {

        /*
         * ログイン
         */
        String url = "https://www.shinkin.co.jp/shonan/personal_bank/index.html";
        this.cmd.get(url);
        this.cmd.clickAndWait(By.partialLinkText("ログイン"));

        PasswordManager pm = new PasswordManager(this.crawlerDir);
        pm.executeByUrl(url);

        this.cmd.type(By.name("userId"), pm.getId());
        this.cmd.type(By.name("loginPwd"), pm.getPassword());
        this.cmd.clickAndWait(By.name("LoginPage"));
        sleep(5_000);

        this.cmd.clickAndWait(By.xpath("//input[@value='トップページへ']"));
        sleep(5_000);

        this.cmd.clickAndWait(By.xpath("//input[@value='この口座の入出金明細を照会']"));
        sleep(5_000);

        final TextMerger textMerger = new TextMerger(this.master, "yyyy-MM-dd");

        int roopCounter = 0;
        while (roopCounter < 1) { // 1回のみ
            roopCounter++;

            /* 前のループでダウンロードしたファイルを削除します。*/
            deletePreFile();

            /* 明細をダウンロード */
            this.cmd.clickAndWait(By.xpath("//label[contains(text(),'最新の明細から')]")); // 最新の明細から
            this.cmd.choiceByText(By.xpath("//select[@class='formSelect']"), "100");
            this.cmd.clickAndWait(By.xpath("//input[@value='明細を見る']"));
            sleep(3_000);
            this.cmd.clickAndWait(By.xpath("//input[@value='明細(CSV)をダウンロード']"));
            new DownloadWait().start(); // ファイルダウンロードを待つ

            /* CSVを整形 */
            File fileOriginal = getDownloadFileOne();
            List<String> lines = Generics.newArrayList();
            for (String line : new ShiftJisText(fileOriginal).readLines()) {
                line = line.trim();
                if (StringUtils.isEmpty(line)) {
                    continue;
                }
                String[] datas;
                try {
                    datas = new CSVParser().parseLine(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    /* 日付に変換できるかをチェック */
                    new SimpleDateFormat("yyyy-MM-dd").parse(datas[0]);
                } catch (ParseException e) {
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
        new Utf8Text(this.summary).write(getSummary(getDownloadFileOne()));

        /* ログアウト */
        this.cmd.clickAndWait(By.xpath("//a[text()='ログアウト']"));

    }

    private static String getSummary(File file) {

        String finalLine = "";
        for (String line : new ShiftJisText(file).readLines()) {
            finalLine = line;
        }
        String[] finalCsv;
        try {
            finalCsv = new CSVParser().parseLine(finalLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String finalValue = finalCsv[finalCsv.length - 1];

        return finalValue;
    }

    //    public static void main(String[] args) {
    //        ShonanShinkinCrawler me = new ShonanShinkinCrawler(2021, RakutenQuest.APP_DIR);
    //        //me.download();
    //        //me.test();
    //
    //        System.out.println(me.getSummary(me.getDownloadFileOne()));
    //
    //    }

    //    private void test() {
    //        final TextMerger textMerger = new TextMerger(master, year + "-");
    //        File fileOriginal = getDownloadFileOne();
    //        List<String> lines = Generics.newArrayList();
    //        for (String line : new ShiftJisText(fileOriginal).readLines()) {
    //            line = line.trim();
    //            if (StringUtils.isEmpty(line)) {
    //                continue;
    //            }
    //            String[] datas;
    //            try {
    //                datas = new CSVParser().parseLine(line);
    //            } catch (IOException e1) {
    //                throw new RuntimeException(e1);
    //            }
    //            try {
    //                /* 日付に変換できるかをチェック */
    //                new SimpleDateFormat("yyyy-MM-dd").parse(datas[0]);
    //            } catch (ParseException e) {
    //                continue;
    //            }
    //            lines.add(line);
    //        }
    //        textMerger.stock(lines);
    //        textMerger.flash();
    //    }

    public String getAssetShonanShinkin() {
        String num = new Utf8Text(this.summary).read();
        return "湘南信金：" + MoneyUtils.toInt(num) + "円";
    }

}