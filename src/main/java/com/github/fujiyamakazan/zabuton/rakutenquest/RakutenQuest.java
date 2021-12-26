package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.Serializable;

import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.MajicaCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.RakutenCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.ShonanShinkinCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.UCSCardCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.YahooCardCrawler;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;

public abstract class RakutenQuest implements Serializable {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RakutenQuest.class);

    public static File APP_DIR = new File(EnvUtils.getUserDesktop(), "RakutenQuest3");
    private static final int YEAR = 2021;

    /**
     * 動作確認をします。
     * TODO Summuryの動作確認
     */
    public static void main(String[] args) {




        if (APP_DIR.exists() == false) {
            APP_DIR.mkdirs();
        }

        RakutenCrawler rakuten = new RakutenCrawler(YEAR, APP_DIR);
        rakuten.download();
        System.out.println("Rakuten-Card:" + rakuten.getAssetRakutenCredit());
        System.out.println("Rakuten-Point:" + rakuten.getAssetRakutenPoint());

        UCSCardCrawler ucs = new UCSCardCrawler(YEAR, APP_DIR);
        ucs.download();
        System.out.println("UCS-Card:" + ucs.getAssetUCSCredit());

        YahooCardCrawler yahoo = new YahooCardCrawler(YEAR, APP_DIR);
        yahoo.download();
        System.out.println("Yahoo-Card:" + yahoo.getAssetYahooCredit());

        ShonanShinkinCrawler shonan = new ShonanShinkinCrawler(YEAR, APP_DIR);
        shonan.download();
        System.out.println("Shonan:" + shonan.getAssetShonanShinkin());

        MajicaCrawler majica = new MajicaCrawler(YEAR, APP_DIR);
        majica.download();
        System.out.println("Majica:" + majica.getAssetMajicaMoney());



//        final List<JournalCrawler> crawlers = Generics.newArrayList();
//        crawlers.add(new RakutenCrawler(year, APP_DIR));
//        //crawlers.add(new RakutenBankCrawler(year, appDir));
//        crawlers.add(new MajicaCrawler(year, APP_DIR));
//        crawlers.add(new ShonanShinkinCrawler(year, APP_DIR));
//        crawlers.add(new UCSCardCrawler(year, APP_DIR));
//        crawlers.add(new YahooCardCrawler(year, APP_DIR));

//        new RakutenQuest() {
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            protected File getWorkDir() {
//                return APP_DIR;
//            }
//
//            @Override
//            protected List<JournalCrawler> getCrawlers() {
//                return crawlers;
//            }
//
//        }.execute();
    }

//    /**
//     * 処理を実行します。
//     */
//    public void execute() {
//
//        StringBuilderLn sb = new StringBuilderLn();
//
//        for (JournalCrawler crawler : getCrawlers()) {
//            crawler.download();
//            //sb.appendLn(crawler.getText());
//        }
//
//        /* テキストへ書き出す*/
//        File resultText = new File(getWorkDir(), "rakuten-quest3.txt");
//        new Utf8Text(resultText).write(sb.toString());
//        RuntimeExc.execute("notepad", resultText.getAbsolutePath());
//    }
//
//    protected abstract List<JournalCrawler> getCrawlers();
//
//    protected abstract File getWorkDir();

}

