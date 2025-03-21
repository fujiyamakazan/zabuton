package com.github.fujiyamakazan.zabuton.selen;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.HttpAccessObject;
import com.github.fujiyamakazan.zabuton.util.RetryWorker;
import com.github.fujiyamakazan.zabuton.util.exec.CmdAccessObject;
import com.github.fujiyamakazan.zabuton.util.file.ZipUtils;
import com.github.fujiyamakazan.zabuton.util.security.CookieManager;
import com.github.fujiyamakazan.zabuton.util.string.Stringul;

public abstract class SelenCommonDriver implements Serializable {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SelenCommonDriver.class);
    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_TIMEOUT = 5;

    private static final By BODY = By.cssSelector("body");

    private final transient WebDriver originalDriver;

    /** Seleniumの一時ファイルの自動削除を中断する場合はfalseを指定します。 */
    public static boolean deleteTemp = true;

    /**
     * Webドライバのファクトリです。
     */
    private abstract class DriverFactory implements Serializable {
        private static final long serialVersionUID = 1L;

        public File driverFile;

        public DriverFactory(File driverDir) {
            this.driverFile = new File(driverDir, getDriverFileName());
        }

        public File getDriverFile() {
            return driverFile;
        }

        /**
         * ドライバの実行ファイルの名称を返します。
         */
        public abstract String getDriverFileName();

        /**
         * ドライバの実行ファイルを取得できるURLを返します。
         */
        public abstract String getDriverUrl();

        /**
         * ドライバの実行ファイルを取得します。
         */
        public abstract void download();

        /**
         * ドライバの実行ファイルからWebドライバオブジェクトを生成します。
         */
        public abstract WebDriver create(File downloadDefaultDir);

        /**
         * 例外情報から不正なバージョンの発生を検知します。
         */
        public abstract boolean occurredIllegalVersionDetected(Exception e);

    }

    /**
     * GoogleChrome用のWebドライバのファクトリです。
     */
    private class ChoromeDriverFactory extends DriverFactory {
        private static final long serialVersionUID = 1L;
        private static final String DRIVER_EXE = "chromedriver.exe";

        public ChoromeDriverFactory(File driverDir) {
            super(driverDir);
        }

        @Override
        public String getDriverFileName() {
            return "" + DRIVER_EXE;
        }

        @Override
        public String getDriverUrl() {
            //return "https://chromedriver.chromium.org/downloads";
            return "https://googlechromelabs.github.io/chrome-for-testing/";
        }

        @Override
        public void download() {
            HttpAccessObject hao = createHao();
            String html = hao.get(getDriverUrl());

            Document doc = Jsoup.parse(html);
            Elements trs = doc.getElementsByTag("tr");

            String url = "";
            for (Element tr : trs) {
                Elements ths = tr.getElementsByTag("th");
                if (ths.size() >= 2) {
                    if (ths.get(0).text().equals("chromedriver")
                        && ths.get(1).text().equals("win64")) {
                        url = tr.getElementsByTag("td").get(0).getElementsByTag("code").text();
                        break;
                    }
                }
            }

            File zip = new File(driverFile.getAbsolutePath() + ".zip");
            hao.download(url, zip);
            LOGGER.debug(url);
            new ZipUtils.UnzipTask(zip) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void runByEntry(String name, File file) {
                    try {
                        if (StringUtils.contains(name, DRIVER_EXE)) {
                            if (StringUtils.contains(name, "/")) {
                                name = name.substring(name.indexOf('/') + 1);
                            }
                            File unpackfile = new File(driverFile.getParentFile(), name);
                            FileUtils.copyFile(file, unpackfile);
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }.start();

        }

        @Override
        public WebDriver create(File downloadDefaultDir) {

            /* Choromeの環境変数を設定します。 */
            System.setProperty("webdriver.chrome.driver", driverFile.getAbsolutePath());

            /* ダウンロードフォルダを指定します。 */
            HashMap<String, Object> prefes = new HashMap<String, Object>();
            prefes.put("profile.default_content_settings.popups", 0);
            if (downloadDefaultDir != null) {
                prefes.put("download.default_directory", downloadDefaultDir.getAbsolutePath());
            }

            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("prefs", prefes);

            /* Webドライバのインスタンスを返します。*/
            return new ChromeDriver(options);

            /*
             * Edgeの場合、「自動テストソフトウェアによって制御されています」を非表示にする方法は下記の通り。
             * TODO Choromeにも適用することを検討
             */
            //            EdgeOptions options = new EdgeOptions();
            //            options.setExperimentalOption("useAutomationExtension", false);
            //            options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        }

        @Override
        public boolean occurredIllegalVersionDetected(Exception e) {
            return e instanceof SessionNotCreatedException
                && e.getMessage().contains("This version of ChromeDriver only supports Chrome version");
        }

    }

    /**
     * コンストラクタです。Webドライバを生成ます。
     */
    public SelenCommonDriver() {

        /* デフォルトではGoogleChromeのWebドライバを生成します。 */
        DriverFactory factory = new ChoromeDriverFactory(getDriverDir());

        final File driverFile = factory.getDriverFile();

        if (driverFile.exists() == false) {

            /* ドライバのファイルをダウンロードします。 */
            factory.download();

            /* ダウンロードが失敗したら例外情報として返します。 */
            if (driverFile.exists() == false) {
                throw new RuntimeException(
                    "ドライバのファイルが" + driverFile.getAbsolutePath() + "にありません。"
                        + " [" + factory.getDriverUrl() + "]からダウンロードしてください。");
            }

        }

        WebDriver driver;
        try {
            driver = factory.create(getDownloadDir());

        } catch (Exception e) {
            LOGGER.error(Stringul.ofException(e));

            if (factory.occurredIllegalVersionDetected(e)) {
                /* ドライバファイルのバージョン不正を検知したときの処理 */
                try {
                    killTask();
                    Thread.sleep(1000);

                    // TODO 何度も失敗するのは解凍したときにサブフォルダがあるのが問題？

                    LOGGER.info("削除対象：" + driverFile.getAbsolutePath());

                    Files.deleteIfExists(Path.of(driverFile.getAbsolutePath())); // ファイル削除




                } catch (Exception deleteException) {
                    throw new RuntimeException("削除失敗", deleteException);
                }
                throw new RuntimeException(
                    "ドライバのファイルがバージョンが不正のため削除しました。"
                        + "再度実行すると、新しいファイルをダウンロードします。",
                    e);

            } else {
                throw new RuntimeException(e);
            }
        }

        onInitialize(driver);

        this.originalDriver = driver;
    }

    protected HttpAccessObject createHao() {
        return new HttpAccessObject();
    }

    /**
     * 拡張ポイントです。
     * 作成されたドライバに設定を追加します。
     */
    protected void onInitialize(WebDriver driver) {
        /* 暗黙的な待機時間を設定します。 */
        driver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * ドライバのファイルを返すように実装します。
     * 例えばクロームの場合は「chromedriver.exe」です。
     */
    protected abstract File getDriverDir();

    /**
     * ダウンロードを指示したときに保存するディレクトリを返すように実装します。
     */
    protected abstract File getDownloadDir();

    //    /**
    //     * Cookieを保存するフォルダを実装します。
    //     * デフォルトの実装ではダウンロードフォルダの中に作成します。
    //     */
    //    protected File getSCookieDir() {
    //        File file = new File(getDownloadDir(), "cookie");
    //        if (file.exists() == false) {
    //            file.mkdirs();
    //        }
    //        return file;
    //    }

    private CookieManager cm;

    /**
     * URLを指定して表示します。
     */
    public void get(String url) {

        this.originalDriver.get(url);

        if (useCookieManager() && this.cm != null) {
            this.cm.save();
        }
    }

    public void click(By by) {
        clickAndWait(by, false);
    }

    /**
     * 画面要素を指定してクリックします。
     */
    public void click(By by, boolean withAlert) {
        WebDriverWait wait = newWebDriverWait(this.originalDriver, DEFAULT_TIMEOUT);

        /* 要素が、ページのDOMに存在して可視となるまで待つ */
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));

        WebElement element = findElement(by);

        click(element);
    }

    private WebDriverWait newWebDriverWait(WebDriver driver, int timeout) {
        return new WebDriverWait(this.originalDriver, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    /**
     * 画面要素を指定してクリックします。
     */
    public void click(WebElement element) {
        WebDriverWait wait = newWebDriverWait(this.originalDriver, DEFAULT_TIMEOUT);

        /* 要素の位置までスクロールします。 */
        new Actions(this.originalDriver).moveToElement(element).perform();

        /*
         * クリックします。
         * 別の要素が重なっている場合は、少しずつ位置をずらしながらリトライします。
         */
        new RetryWorker() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void run() {
                wait.until(ExpectedConditions.elementToBeClickable(element)); // クリック可能になるまで待ちます。
                element.click();
            }
            @Override
            protected void recovery() {
                down(1);
            }
        }.start();
    }

    /**
     * 画面要素を指定してクリックします。
     */
    public void clickAndWait(By by) {
        clickAndWait(by, false);
    }

    /**
     * 画面要素を指定してクリックします。
     */
    public void clickAndWait(WebElement element) {
        clickAndWait(element, false);

    }

    /**
     * 画面要素を指定してクリックします。
     */
    public void clickAndWait(By by, boolean withAlert) {
        WebDriverWait wait = newWebDriverWait(this.originalDriver, DEFAULT_TIMEOUT);

        /* 要素が、ページのDOMに存在して可視となるまで待つ */
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));

        WebElement element = findElement(by);

        clickAndWait(element, withAlert);
    }

    /**
     * 画面要素を指定してクリックします。
     */
    public void clickAndWait(WebElement element, boolean withAlert) {
        WebDriverWait wait = newWebDriverWait(this.originalDriver, DEFAULT_TIMEOUT);

        /* 要素の位置までスクロールします。 */
        new Actions(this.originalDriver).moveToElement(element).perform();

        /*
         * クリックします。
         * 別の要素が重なっている場合は、少しずつ位置をずらしながらリトライします。
         */
        new RetryWorker() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void run() {

                /* クリック可能になるまで待ちます。*/
                //wait.until(ExpectedConditions.elementToBeClickable(by));
                wait.until(ExpectedConditions.elementToBeClickable(element));

                element.click();
            }

            @Override
            protected void recovery() {
                down(1);
            }

        }.start();

        if (withAlert) {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.accept();
        }

        /* 待機処理。次のHTMLが表示されるのを待ちます。 */
        newWebDriverWait(this.originalDriver, DEFAULT_TIMEOUT)
            .until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
    }

    /**
     * 属性valueを指定し、サブミットボタンをクリックします。
     */
    public void clickButtonAndWait(String value) {
        By by = By.xpath("//input[@type='submit' and @value='" + value + "']");
        clickAndWait(by);

    }

    public void clickLinkPartialAndWait(String text) {
        By by = By.partialLinkText(text);
        clickAndWait(by);
    }

    /**
     * 入力コンポーネントに対してタイピングをします。すでに何か入力されていればクリアします。
     */
    public void type(By by, String value) {
        typeCore(by, value);
    }

    /**
     * 入力コンポーネントに対してタイピングをします。すでに何か入力されていればクリアします。
     */
    public void type(By by, Keys key) {
        typeCore(by, key.toString());
    }

    private void typeCore(By by, String value) {
        WebElement element = findElement(by);

        element.clear(); // いったんクリア
        element.sendKeys(value);

        /* エラー検知を避けるためにタイムラグを入れる。*/
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * downキーをシミュレートします。
     */
    public void down(int i) {
        for (int j = 0; j < i; j++) {
            findElement(BODY).sendKeys(Keys.DOWN);
        }
    }

    /**
     * カーソルキーの右をシミュレートします。
     */
    public void right(int i) {
        for (int j = 0; j < i; j++) {
            findElement(BODY).sendKeys(Keys.ARROW_RIGHT);
        }
    }

    /**
     * セレクトボックスを選択します。
     */
    public void choiceByText(By by, String value) {
        WebElement element = getSelectElement(by);
        new Select(element).selectByVisibleText(value);
    }

    /**
     * セレクトボックスを選択します。
     */
    public void choiceByIndex(By by, int index) {
        WebElement element = getSelectElement(by);
        new Select(element).selectByIndex(index);
    }

    private WebElement getSelectElement(By by) {
        WebElement element = findElement(by);
        if (element.getTagName().equals("select") == false) {
            throw new RuntimeException("セレクトボックスではありません。" + by.toString());
        }
        return element;
    }

    public String getText(By by) {
        WebElement element = findElement(by);
        return element.getText();
    }

    /**
     * テキストが含まれるかを検査します。
     */
    public boolean containsText(String text) {

        /* タイムアウトを短くする */
        this.originalDriver.manage().timeouts().implicitlyWait(200, TimeUnit.MILLISECONDS);

        try {
            return findElements(By.xpath("//*[contains(., '" + text + "')]")).isEmpty() == false;
        } finally {
            //this.originalDriver.manage().timeouts().implicitlyWait(recoveryTimeoutSec * 1000,
            //    TimeUnit.MILLISECONDS);
            this.originalDriver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT,
                TimeUnit.SECONDS);
        }

    }

    public boolean isPresent(By by) {
        return findElements(by).isEmpty() == false;
    }

    public String getPageSource() {
        return this.originalDriver.getPageSource();
    }

    /**
     * 画面要素を取得します。
     */
    public WebElement findElement(By by) {

        /*
         * 新しいウィンドウに遷移しているかもしれないので、
         * 最新のウィンドウハンドルに切り替えます。
         */
        updateWindowHandle();

        /* TODO 表示されるまで待つ */

        return this.originalDriver.findElement(by);
    }

    /**
     * 画面要素を取得します。
     */
    public List<WebElement> findElements(By by) {

        /*
         * 新しいウィンドウに遷移しているかもしれないので、
         * 最新のウィンドウハンドルに切り替えます。
         */
        updateWindowHandle();

        /* TODO 表示されるまで待つ */

        return this.originalDriver.findElements(by);
    }

    /**
     * 新しいウィンドウに遷移しているかもしれないので、
     * 最新のウィンドウハンドルに切り替えます。
     */
    private void updateWindowHandle() {

        Set<String> set = this.originalDriver.getWindowHandles();
        this.originalDriver.switchTo().window(set.toArray(new String[] {})[set.size() - 1]);
    }

    /**
     * 終了処理をします。
     */
    public void quit() {
        this.originalDriver.quit();
        if (deleteTemp) {
            deleteTempFile();
        }
    }

    /**
     * Windowsに溜まるゴミファイルの削除をします。
     */
    public static void deleteTempFile() {
        File dir = EnvUtils.getUserLocalTemp();
        for (File f : dir.listFiles()) {
            if (f.isDirectory() && f.getName().startsWith("scoped_dir")) {
                try {
                    FileUtils.deleteDirectory(f);
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                    LOGGER.warn("scoped_dirの削除に失敗", e);
                }
            }
            if (f.getName().equals("screenshot")) {
                f.delete();
            }
        }
    }

    /**
     * タイトルの検査をします。
     */
    public void assertTitleContains(String partialTitle) {
        String title = getTitle();
        if (title.contains(partialTitle) == false) {
            throw new RuntimeException("タイトル不正：title=" + title + " expect=" + partialTitle);
        }
    }

    public String getTitle() {
        return this.originalDriver.getTitle();
    }

    /**
     * 中断処理を入れます。
     * @param ms ミリ秒
     */
    public void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void back() {
        this.originalDriver.navigate().back();
    }

    public void until(ExpectedCondition<WebElement> condition) {
        newWebDriverWait(this.originalDriver, DEFAULT_TIMEOUT).until(condition);
    }

    public WebDriver getDriver() {
        return this.originalDriver;
    }

    public void addCookie(Cookie cookie) {
        this.originalDriver.manage().addCookie(cookie);
    }

    /**
     * 要素が存在するかを判定します。
     * TODO 待ち時間の短縮
     */
    public boolean presentElemet(By by) {
        return originalDriver.findElements(by).size() > 0;
    }

    /**
     * ドライバのタスクを強制終了します。
     */
    public static void killTask() {
        /* 既存の処理を列挙 */
        LOGGER.trace("終了処理として残留しているドライバのタスクを終了します。");
        try {
            CmdAccessObject.executeCmd("taskkill /im " + ChoromeDriverFactory.DRIVER_EXE + " /f");
        } catch (Exception e) {
            /* プロセスがなくて失敗することもあるが問題としない。 */
            LOGGER.debug(e.getMessage());
        }
    }

    protected boolean useCookieManager() {
        return false;
    }

    /**
     * URLを指定して表示します。
     * cookie操作もします。
     */
    public void getWithCookie(String url) {
        if (useCookieManager() == false) {
            throw new RuntimeException();
        }
        this.get(url);
        this.cm = createCookieManager();
        this.cm.executeByUrl(url);
    }

    protected CookieManager createCookieManager() {
        throw new RuntimeException("未実装");
    }

    /**
     * テストをします。
     */

    public static void main(String[] args) {
        SelenCommonDriver cmd = new SelenCommonDriver() {

            private static final long serialVersionUID = 1L;

            @Override
            protected File getDriverDir() {
                //return null;
                return new File("C:\\tmp");
            }

            @Override
            protected File getDownloadDir() {
                return null;
            }
        };

        cmd.get("http://google.co.jp");

    }

}
