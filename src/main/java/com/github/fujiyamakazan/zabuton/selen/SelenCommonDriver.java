package com.github.fujiyamakazan.zabuton.selen;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.danekja.java.misc.serializable.SerializableRunnable;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.github.fujiyamakazan.zabuton.selen.driverfactory.ChoromeDriverFactory;
import com.github.fujiyamakazan.zabuton.selen.driverfactory.DriverFactory;
import com.github.fujiyamakazan.zabuton.selen.driverfactory.EdgeDriverFactory;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.RetryWorker;
import com.github.fujiyamakazan.zabuton.util.exec.CmdAccessObject;
import com.github.fujiyamakazan.zabuton.util.security.CookieManager;
import com.github.fujiyamakazan.zabuton.util.string.Stringul;

public abstract class SelenCommonDriver implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    public static final int DEFAULT_TIMEOUT = 5;

    private static final By BODY = By.cssSelector("body");

    private final transient WebDriver originalDriver;

    /** Seleniumの一時ファイルの自動削除を中断する場合はfalseを指定します。 */
    public static boolean deleteTemp = true;

    private SerializableRunnable onQuit;

    /**
     * コンストラクタです。Webドライバを生成ます。
     */
    public SelenCommonDriver() {
        this(null);
    }

    public SelenCommonDriver(String opt) {

        ///* デフォルトではGoogleChromeのWebドライバを生成します。 */
        //DriverFactory factory = new ChoromeDriverFactory(getDriverDir());

        WebDriver driver;
        if (opt == null) {

            /* デフォルトではGoogleChromeのWebドライバを生成します。 */
            final DriverFactory factory;
            factory = new ChoromeDriverFactory(getDriverDir());

            final File driverFile = factory.getDriverFile();
            if (driverFile.exists() == false) {
                factory.download(); // ドライバのファイルをダウンロード
                /* ダウンロードが失敗したら例外情報として返します。 */
                if (driverFile.exists() == false) {
                    throw new RuntimeException(
                        "ドライバのファイルが" + driverFile.getAbsolutePath() + "にありません。"
                            + " [" + factory.getDriverUrl() + "]からダウンロードしてください。");
                }
            }

            try {
                driver = factory.create(getDownloadDir());

            } catch (Exception e) {
                LOGGER.error(Stringul.ofException(e));
                if (factory.occurredIllegalVersionDetected(e)) {
                    /* ドライバファイルのバージョン不正を検知したときの処理 */
                    killTask();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        throw new RuntimeException(e1);
                    }
                    throw new RuntimeException("ドライバのバージョンが不正です。", e);

                } else {
                    throw new RuntimeException(e);
                }
            }

        } else if (StringUtils.equals(opt, "edge")) {
            final DriverFactory factory;
            factory = new EdgeDriverFactory(getDriverDir());
            driver = factory.create(getDownloadDir());

            /* ファイルダウンロードが中断しないようにする処理 */
            onQuit = () -> {

                File dir = getDownloadDir();
                int waited = 0;
                while (waited < 5) {
                    boolean downloading = false;

                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.getName().endsWith(".crdownload") || file.getName().endsWith(".tmp")) {
                                LOGGER.debug("ダウンロード実行中");
                                downloading = true;
                                break;
                            }
                        }
                    }

                    if (!downloading) {
                        LOGGER.debug("進行中のダウンロードは検出されませんでした。終了します。");
                        return;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } // 1秒待機
                    waited++;
                }

                throw new RuntimeException("進行中のダウンロードの待機がタイムアウトしました。");
            };

        } else {
            throw new IllegalArgumentException(String.format("opt:%s", opt));
        }

        onInitialize(driver);

        this.originalDriver = driver;
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
    protected File getDownloadDir() {
        return null;
    }

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
        WebDriverWait wait = createWebDriverWait(this.originalDriver, DEFAULT_TIMEOUT);

        /* 要素が、ページのDOMに存在して可視となるまで待つ */
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));

        WebElement element = findElement(by);

        click(element);
    }

    private WebDriverWait createWebDriverWait(WebDriver originalDriver, int timeout) {
        //return new WebDriverWait(this.originalDriver, timeout);
        return new WebDriverWait(this.originalDriver, Duration.ofSeconds(timeout));
    }

    /**
     * 画面要素を指定してクリックします。
     */
    public void click(WebElement element) {
        WebDriverWait wait = createWebDriverWait(this.originalDriver, DEFAULT_TIMEOUT);

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
        WebDriverWait wait = createWebDriverWait(this.originalDriver, DEFAULT_TIMEOUT);

        /* 要素が、ページのDOMに存在して可視となるまで待つ */
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));

        WebElement element = findElement(by);

        clickAndWait(element, withAlert);
    }

    /**
     * 画面要素を指定してクリックします。
     */
    public void clickAndWait(WebElement element, boolean withAlert) {
        WebDriverWait wait = createWebDriverWait(this.originalDriver, DEFAULT_TIMEOUT);

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
        createWebDriverWait(this.originalDriver, DEFAULT_TIMEOUT)
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
        if (onQuit != null) {
            onQuit.run();
        }
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
        createWebDriverWait(this.originalDriver, DEFAULT_TIMEOUT).until(condition);
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
        LOGGER.trace("残留しているドライバのタスクを終了します。");
        try {
            CmdAccessObject.executeCmd("taskkill /im " + ChoromeDriverFactory.DRIVER_EXE + " /f");
            CmdAccessObject.executeCmd("taskkill /im " + EdgeDriverFactory.DRIVER_EXE + " /f");
        } catch (Exception e) {
            /* プロセスがなくて失敗することもある */
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
     * Edgeでドライバを作成します。
     */
    public static SelenCommonDriver createEdgeDriver(File work) {
        return new SelenCommonDriver("edge") {

            private static final long serialVersionUID = 1L;

            @Override
            protected File getDriverDir() {
                return work;
            }

            @Override
            protected File getDownloadDir() {
                return work;
            }

        };
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
