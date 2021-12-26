package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.JournalCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.MajicaCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.RakutenCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.ShonanShinkinCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.UCSCardCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.YahooCardCrawler;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.StringBuilderLn;
import com.github.fujiyamakazan.zabuton.util.exec.RuntimeExc;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public abstract class RakutenQuest implements Serializable {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RakutenQuest.class);

    public static File APP_DIR = new File(EnvUtils.getUserDesktop(), "RakutenQuest3");

    /**
     * 動作確認をします。
     * TODO PasswordManagerが見切れている
     */
    public static void main(String[] args) {


        if (APP_DIR.exists() == false) {
            APP_DIR.mkdirs();
        }

        final int year = 2021;
        final List<JournalCrawler> crawlers = Generics.newArrayList();

        crawlers.add(new RakutenCrawler(year, APP_DIR));
        //crawlers.add(new RakutenBankCrawler(year, appDir));
        crawlers.add(new MajicaCrawler(year, APP_DIR));
        crawlers.add(new ShonanShinkinCrawler(year, APP_DIR));
        crawlers.add(new UCSCardCrawler(year, APP_DIR));
        crawlers.add(new YahooCardCrawler(year, APP_DIR));

        new RakutenQuest() {
            private static final long serialVersionUID = 1L;

            @Override
            protected File getWorkDir() {
                return APP_DIR;
            }

            @Override
            protected List<JournalCrawler> getCrawlers() {
                return crawlers;
            }

        }.execute();
    }

    /**
     * 処理を実行します。
     */
    public void execute() {

        StringBuilderLn sb = new StringBuilderLn();

        for (JournalCrawler crawler : getCrawlers()) {
            crawler.download();
            sb.appendLn(crawler.getText());
        }

        /* テキストへ書き出す*/
        File resultText = new File(getWorkDir(), "rakuten-quest3.txt");
        new Utf8Text(resultText).write(sb.toString());
        RuntimeExc.execute("notepad", resultText.getAbsolutePath());
    }

    protected abstract List<JournalCrawler> getCrawlers();

    protected abstract File getWorkDir();

}

