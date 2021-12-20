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
    /** serialVersionUID */
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RakutenQuest.class);

    public static void main(String[] args) {
        new RakutenQuest() {
            private static final long serialVersionUID = 1L;

            @Override
            protected File getWorkDir() {
                File f = new File(EnvUtils.getUserDesktop(),"RakutenQuest3");
                if (f.exists() == false) {
                    f.mkdir();
                }
                return f;
            }

            @Override
            protected List<JournalCrawler> getCrawlers() {
                List<JournalCrawler> list = Generics.newArrayList();
                list.add(new RakutenJournalCrawler());
                return list;
            }

            @Override
            protected List<Journal> getRecodied() {
                return Generics.newArrayList();
            }

        }.execute(2021, "chromedriver96.exe");
    }

    public void execute(int year, String driverName) {
        File resultText = new File(getWorkDir(), "rakuten-quest3.txt");
        File driverFile = new File(getWorkDir(), driverName);

        StringBuilderLn sb = new StringBuilderLn();
        for (JournalCrawler crawler: getCrawlers()) {

            crawler.setYear(year);
            crawler.setRecordedData(getRecodied());
            crawler.setupDir(getWorkDir());
            crawler.setDriverFile(driverFile);
            sb.appendLn(crawler.execute());

        }

        new Utf8Text(resultText).write(sb.toString());
        RuntimeExc.execute("notepad", resultText.getAbsolutePath());
    }

    protected abstract List<Journal> getRecodied();

    protected abstract List<JournalCrawler> getCrawlers();

    protected abstract File getWorkDir();


}
