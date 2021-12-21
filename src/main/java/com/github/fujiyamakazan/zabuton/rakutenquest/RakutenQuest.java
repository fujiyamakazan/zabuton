package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.StringBuilderLn;
import com.github.fujiyamakazan.zabuton.util.exec.RuntimeExc;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public abstract class RakutenQuest implements Serializable {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RakutenQuest.class);

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        File appDir = new File(EnvUtils.getUserDesktop(), "RakutenQuest3");
        if (appDir.exists() == false) {
            appDir.mkdirs();
        }

        final int year = 2021;

        final List<JournalCrawler> crawlers = Generics.newArrayList();
        crawlers.add(new RakutenCrawler(year, appDir));

        new RakutenQuest() {
            private static final long serialVersionUID = 1L;

            @Override
            protected File getWorkDir() {
                return appDir;
            }

            @Override
            protected List<Journal> getRecodied() {
                return Generics.newArrayList();
            }

            @Override
            protected List<JournalCrawler> getCrawlers() {
                return crawlers;
            }

        }.execute();
    }

    /**
     * 処理を実行します。
     * @param year 処理対象の年を4桁の西暦で示します。
     */
    public void execute() {

        StringBuilderLn sb = new StringBuilderLn();

        for (JournalCrawler crawler : getCrawlers()) {
            crawler.doDowoload();
            sb.appendLn(crawler.getText());
        }

        /* テキストへ書き出す*/
        File resultText = new File(getWorkDir(), "rakuten-quest3.txt");
        new Utf8Text(resultText).write(sb.toString());
        RuntimeExc.execute("notepad", resultText.getAbsolutePath());
    }



    protected abstract List<Journal> getRecodied();

    protected abstract List<JournalCrawler> getCrawlers();

    protected abstract File getWorkDir();

}
