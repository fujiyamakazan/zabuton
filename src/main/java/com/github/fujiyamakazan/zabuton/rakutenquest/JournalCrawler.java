package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
import com.github.fujiyamakazan.zabuton.util.RetryWorker;
import com.github.fujiyamakazan.zabuton.util.jframe.JFrameUtils;
import com.github.fujiyamakazan.zabuton.util.text.TextMerger;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;
import com.ibm.icu.util.Calendar;
import com.opencsv.CSVParser;

public abstract class JournalCrawler implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JournalCrawler.class);

    public static final String STANDRD_DRIVER_NAME = "chromedriver.exe";

    protected SelenCommonDriver cmd;

    protected final int year;
    //    private final String name;
    private final File appDir;
    protected final File crawlerDir;
    private final File crawlerDailyDir;

    /**
     * コンストラクタです。
     */
    public JournalCrawler(String name, int year, File appDir) {
        //        this.name = name;
        this.year = year;
        this.appDir = appDir;
        this.crawlerDir = new File(appDir, name);
        this.crawlerDir.mkdirs();
        this.crawlerDailyDir = new File(crawlerDir, "daily");
        this.crawlerDailyDir.mkdirs();
    }

    /**
     * 処理を実行します。
     */
    public List<Journal> execute() {

        /* 本日ダウンロード分があればスキップ */
        boolean skip = false;
        File fileToday = getDownloadFileOne();
        if (fileToday != null) {
            Date date = new Date(fileToday.lastModified());
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_MONTH, -1);
            skip = date.after(yesterday.getTime());
        }

        if (skip == false) {
            try {

                /* 前回の処理結果を削除 */
                for (File f : crawlerDailyDir.listFiles()) {
                    f.delete();
                }

                /* WebDriverを作成 */
                this.cmd = new SelenCommonDriver() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected WebDriver createDriver() {

                        File driverFile = new File(appDir, STANDRD_DRIVER_NAME);
                        if (driverFile.exists() == false) {
                            throw new RuntimeException("WebDriverが次の場所にありません。"
                                    + driverFile.getAbsolutePath()
                                    + " [https://chromedriver.chromium.org/]からダウロードしてください。");
                        }

                        System.setProperty("webdriver.chrome.driver", driverFile.getAbsolutePath());

                        /* ダウンロードフォルダ指定 */
                        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
                        chromePrefs.put("profile.default_content_settings.popups", 0);
                        chromePrefs.put("download.default_directory", crawlerDailyDir.getAbsolutePath());
                        ChromeOptions options = new ChromeOptions();
                        options.setExperimentalOption("prefs", chromePrefs);
                        DesiredCapabilities cap = DesiredCapabilities.chrome();
                        cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                        cap.setCapability(ChromeOptions.CAPABILITY, options);

                        @SuppressWarnings("deprecation")
                        WebDriver driver = new ChromeDriver(cap);
                        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS); // 暗黙的な待機時間を設定

                        return driver;
                    }
                };

                download();
                this.cmd.quit();

            } catch (Exception e) {
                e.printStackTrace(); // 標準出力
                JFrameUtils.showErrorDialog("エラーが発生しました。終了します。詳細なエラー情報を標準出力しました。");
                throw new RuntimeException(e);
            }
        }

        return createJournal();
    }

    protected abstract void download();

    protected abstract List<Journal> createJournal();

    protected File getDownloadFileOne() {
        if (this.crawlerDailyDir.listFiles().length == 0) {
            return null;
        }
        return this.crawlerDailyDir.listFiles()[0];
    }

    protected final class DownloadWait extends RetryWorker {
        private static final long serialVersionUID = 1L;

        @Override
        protected void run() {
            if (getDownloadFileOne() == null) {
                throw new RuntimeException("ダウンロード未完了");
            }
        }

        @Override
        protected void recovery() {
            sleep(3_000);
        }
    }

    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void saveDaily(String name, String text) {
        new Utf8Text(new File(this.crawlerDailyDir, name)).write(text);
    }

    protected final class StandardMerger extends TextMerger {
        private static final long serialVersionUID = 1L;

        protected StandardMerger(File masterText) {
            super(masterText);
        }

        @Override
        protected boolean isAvailableLine(String line) {
            try {
                return new CSVParser().parseLine(line)[0].startsWith(year + "/");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * ダウンロードしたファイルを削除します。
     */
    protected void deletePreFile() {

        File f = getDownloadFileOne();
        if (getDownloadFileOne() != null) {
            f.delete();
        }
        if (getDownloadFileOne() != null) {
            throw new RuntimeException();
        }
    }

}
