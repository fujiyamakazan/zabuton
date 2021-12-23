package com.github.fujiyamakazan.zabuton.rakutenquest.crawler;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.rakutenquest.JournalCsv;
import com.github.fujiyamakazan.zabuton.util.jframe.JFrameUtils;
import com.github.fujiyamakazan.zabuton.util.text.ShiftJisText;
import com.github.fujiyamakazan.zabuton.util.text.TextMerger;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;
import com.opencsv.CSVParser;

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
    protected void download() {
        /*
         * ダウンロード処理
         */
        String url = "https://fes.rakuten-bank.co.jp/MS/main/RbS?CurrentPageID=START&&COMMAND=LOGIN";
        //        cmd.get(url);
        //        cmd.assertTitleContains("ようこそ");
        //
        //        PasswordManager pm = new PasswordManager(crawlerDir);
        //        pm.executeBySightKey("rakuten-bank-01");
        //
        //        cmd.type(By.name("LOGIN:USER_ID"), pm.getId());
        //        cmd.type(By.name("LOGIN:LOGIN_PASSWORD"), pm.getPassword());
        //        cmd.clickAndWait(By.partialLinkText("ログイン"));

        final TextMerger textMerger = new TextMerger(master, null) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean isAvailableLine(String line) {
                try {
                    return new CSVParser().parseLine(line)[0].startsWith(String.valueOf(year));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        /* ブラウザを開いてアプリケーションを表示する */
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        /* 保存場所フォルダを開く*/
        //RuntimeExc.execute("explorer.exe", crawlerDailyDir.getAbsolutePath());
        try {
            Desktop.getDesktop().open(crawlerDailyDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JFrameUtils.showDialog("ご自身でCSVをダウンロードして[" + crawlerDailyDir.getAbsolutePath() + "]に保存してしてください。");

        /* CSVを整形 */
        File fileOriginal = getDownloadFileOne();
        List<String> orignalLine = new ShiftJisText(fileOriginal).readLines();
        List<String> tmp = Generics.newArrayList();
        for (String original : orignalLine) {
            if (StringUtils.startsWith(original, "取引日,入出金(円),取引後残高(円),入出金内容")) { // 見出し行除外
                continue;
            }
            tmp.add(original);
        }
        List<String> lines = tmp;

        textMerger.stock(lines);
        textMerger.flash();

        String finalLine = lines.get(lines.size() - 1);
        String num;
        try {
            num = new CSVParser().parseLine(finalLine)[2];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new Utf8Text(summary).write(num);
    }

}
