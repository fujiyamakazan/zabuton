package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
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
import com.ibm.icu.util.Calendar;

public abstract class JournalCrawler implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JournalCrawler.class);

    protected SelenCommonDriver cmd;

    protected File rootDir;
    protected List<Journal> recodied;

    private File dailyDir;
    private File driverFile;
    private int year;

    public void setDriverFile(File driverFile) {
        this.driverFile = driverFile;
    }

    public void setupDir(File appRoot) {
        this.rootDir = new File(appRoot, getName());
        this.rootDir.mkdirs();
        this.dailyDir = new File(rootDir, "daily");
        this.dailyDir.mkdirs();
    }

    public void setRecordedData(List<Journal> recodied) {
        this.recodied = recodied;
    }

    public String execute() {

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
                for (File f : dailyDir.listFiles()) {
                    f.delete();
                }

                /* WebDriverを作成 */
                this.cmd = new SelenCommonDriver() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected WebDriver createDriver() {

                        System.setProperty("webdriver.chrome.driver", driverFile.getAbsolutePath());

                        /* ダウンロードフォルダ指定 */
                        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
                        chromePrefs.put("profile.default_content_settings.popups", 0);
                        chromePrefs.put("download.default_directory", dailyDir.getAbsolutePath());
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

                doDownload();
                this.cmd.quit();

            } catch (Exception e) {
                e.printStackTrace(); // 標準出力
                JFrameUtils.showErrorDialog("エラーが発生しました。終了します。詳細なエラー情報を標準出力しました。");
                throw new RuntimeException(e);
            }
        }

        return doRead();
    }

    protected File getDownloadFileOne() {
        if (this.dailyDir.listFiles().length == 0) {
            return null;
        }
        return this.dailyDir.listFiles()[0];
    }

    protected abstract String getName();

    protected abstract void doDownload();

    protected abstract String doRead();

    protected String getYear() {
        return String.valueOf(year);
    }

    public void setYear(int year) {
        this.year = year;
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

}
