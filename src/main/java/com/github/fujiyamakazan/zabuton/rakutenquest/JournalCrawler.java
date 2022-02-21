package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.wicket.util.lang.Generics;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
import com.github.fujiyamakazan.zabuton.util.RetryWorker;
import com.github.fujiyamakazan.zabuton.util.StringBuilderLn;
import com.github.fujiyamakazan.zabuton.util.date.Chronus;
import com.github.fujiyamakazan.zabuton.util.jframe.JFrameUtils;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;
import com.ibm.icu.util.Calendar;

public abstract class JournalCrawler implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JournalCrawler.class);

    public static final String STANDRD_DRIVER_NAME = "chromedriver.exe";
    protected static final int DEFAULT_TIMEOUT = 5;

    protected SelenCommonDriver cmd;

    private final String name;
    protected final File appDir;
    protected final File crawlerDir;
    protected final File crawlerDailyDir;

    /**
     * コンストラクタです。
     */
    public JournalCrawler(String name, File appDir) {
        this.name = name;
        this.appDir = appDir;
        this.crawlerDir = new File(appDir, name);
        this.crawlerDir.mkdirs();
        this.crawlerDailyDir = new File(this.crawlerDir, "daily");
        this.crawlerDailyDir.mkdirs();
    }

    /**
     * 明細をダウンロードします。
     */
    public void download() {
        if (isSkip() == false) {
            try {

                /* 前回の処理結果を削除 */
                for (File f : this.crawlerDailyDir.listFiles()) {
                    f.delete();
                }

                /* WebDriverを作成 */
                this.cmd = new SelenCommonDriver() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected WebDriver createDriver() {

                        File driverFile = new File(JournalCrawler.this.appDir, STANDRD_DRIVER_NAME);
                        if (driverFile.exists() == false) {
                            throw new RuntimeException("WebDriverが次の場所にありません。"
                                + driverFile.getAbsolutePath()
                                + " [https://chromedriver.chromium.org/]からダウロードしてください。");
                        }

                        System.setProperty("webdriver.chrome.driver", driverFile.getAbsolutePath());

                        /* ダウンロードフォルダ指定 */
                        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
                        chromePrefs.put("profile.default_content_settings.popups", 0);
                        chromePrefs.put("download.default_directory",
                            JournalCrawler.this.crawlerDailyDir.getAbsolutePath());

                        ChromeOptions options = new ChromeOptions();
                        options.setExperimentalOption("prefs", chromePrefs);
                        WebDriver driver;
                        try {
                            driver = new ChromeDriver(options);
                        } catch (Exception e) {
                            log.debug(e.getClass().getName() + "が発生。");
                            log.debug(e.getMessage());
                            if (e instanceof SessionNotCreatedException
                                && e.getMessage().contains("This version of ChromeDriver only supports Chrome version")) {
                                throw new RuntimeException(
                                "WebDriverを更新してください。"
                                    + driverFile.getAbsolutePath()
                                    + " [https://chromedriver.chromium.org/]からダウロードしてください。", e);
                            } else {
                                throw new RuntimeException(e);
                            }
                        }
                        driver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT,
                            TimeUnit.SECONDS); // 暗黙的な待機時間を設定

                        return driver;
                    }
                };

                downloadCore();
                this.cmd.quit();

            } catch (Exception e) {
                e.printStackTrace(); // 標準出力
                JFrameUtils.showErrorDialog("エラーが発生しました。終了します。詳細なエラー情報を標準出力しました。");
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isSkip() {
        /* 本日ダウンロード分があればスキップ */
        boolean skip = false;
        File fileToday = getDownloadFileOne();
        if (fileToday != null) {
            Date date = new Date(fileToday.lastModified());
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_MONTH, -1);
            skip = date.after(yesterday.getTime());
        }
        return skip;
    }

    private Map<String, JournalCsv> masters = Generics.newHashMap();
    private File summary;

    protected void setMaster(JournalCsv master) {
        this.masters.put("MAIN", master);
    }

    protected void setMaster(String key, JournalCsv master) {
        this.masters.put(key, master);
    }

    public JournalCsv getMaster() {
        return this.masters.get("MAIN");
    }

    public JournalCsv getMaster(String key) {
        return this.masters.get(key);
    }

    protected void setSummary(File summary) {
        this.summary = summary;
    }

    public File getSummary() {
        return this.summary;
    }

    protected abstract void downloadCore();

    protected File getDownloadFileOne() {
        if (this.crawlerDailyDir.listFiles().length == 0) {
            return null;
        }
        return this.crawlerDailyDir.listFiles()[0];
    }

    protected String getDownloadTextAsUtf8() {
        if (this.crawlerDailyDir.listFiles().length == 0) {
            return null;
        }
        return new Utf8Text(getDownloadFileOne()).read();
    }

    public final class DownloadWait extends RetryWorker {
        private static final long serialVersionUID = 1L;

        @Override
        protected void run() {
            File downloadFileOne = getDownloadFileOne();
            if (downloadFileOne == null) {
                throw new RuntimeException("ダウンロード未完了");
            } else {
                String name = downloadFileOne.getName();
                log.debug("[" + name + "]");
                if (name.endsWith(".tmp") || name.endsWith(".crdownload")) {
                    throw new RuntimeException("ダウンロード実行中");
                }
            }
        }

        @Override
        protected void recovery() {
            sleep(3_000);
        }
    }

    protected void sleep(int i) {
        this.cmd.sleep(i);
        //        try {
        //            Thread.sleep(i);
        //        } catch (InterruptedException e) {
        //            throw new RuntimeException(e);
        //        }
    }

    protected void saveDaily(String name, String text) {
        new Utf8Text(new File(this.crawlerDailyDir, name)).write(text);
    }

    //    protected final class StandardMerger extends TextMerger {
    //        private static final long serialVersionUID = 1L;
    //
    //        public StandardMerger(JournalCsv masterText) {
    //            super(masterText);
    //        }
    //
    //        @Override
    //        protected boolean isAvailableLine(String line) {
    //            try {
    //                return new CSVParser().parseLine(line)[0].startsWith(year + "/");
    //            } catch (IOException e) {
    //                throw new RuntimeException(e);
    //            }
    //        }
    //    }

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

    /**
     * ダウンロードしたファイルのテキスト情報を出力します。
     */
    public final String getText() {
        StringBuilderLn sb = new StringBuilderLn();

        for (Map.Entry<String, JournalCsv> master : this.masters.entrySet()) {
            sb.appendLn("-----");
            sb.appendLn("[" + this.name + "] (" + master.getKey() + ")");
            sb.appendLn("-----");
            sb.appendLn(new Utf8Text(master.getValue().getFile()).read());
        }
        if (this.summary != null) {
            sb.appendLn("-----");
            sb.appendLn("[" + this.name + "] (SUMMARY)");
            sb.appendLn("-----");
            sb.appendLn(new Utf8Text(this.summary).read());
        }

        return sb.toString();
    }

    public String getName() {
        return this.name;
    }

    protected boolean in(String value, String datePattern) {
        try {
            Chronus.parse(value, datePattern);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
