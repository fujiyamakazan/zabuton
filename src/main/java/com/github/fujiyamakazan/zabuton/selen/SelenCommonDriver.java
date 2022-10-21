package com.github.fujiyamakazan.zabuton.selen;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
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
import com.github.fujiyamakazan.zabuton.util.RetryWorker;

public abstract class SelenCommonDriver implements Serializable {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SelenCommonDriver.class);

    public static final int DEFAULT_TIMEOUT = 5;

    private static final By BODY = By.cssSelector("body");

    private static final long serialVersionUID = 1L;

    private final transient WebDriver originalDriver;

    /**
     * Seleniumの一時ファイルを自動削除する場合はtrueを指定します。
     */
    public static boolean deleteTemp = true;

    public SelenCommonDriver() {
        this.originalDriver = createDriver();
    }

    protected WebDriver createDriver() {

        final File driverFile = getDriverFile();
        final File dir = getDownloadDir();

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
            dir.getAbsolutePath());

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);
        WebDriver driver;
        try {
            driver = new ChromeDriver(options);
        } catch (Exception e) {
            LOGGER.debug(e.getClass().getName() + "が発生。");
            LOGGER.debug(e.getMessage());
            if (e instanceof SessionNotCreatedException
                && e.getMessage()
                    .contains("This version of ChromeDriver only supports Chrome version")) {
                throw new RuntimeException(
                    "WebDriverを更新してください。"
                        + driverFile.getAbsolutePath()
                        + " [https://chromedriver.chromium.org/]からダウロードしてください。",
                    e);
            } else {
                throw new RuntimeException(e);
            }
        }
        driver.manage().timeouts().implicitlyWait(DEFAULT_TIMEOUT,
            TimeUnit.SECONDS); // 暗黙的な待機時間を設定

        return driver;
    }

    protected abstract File getDownloadDir();

    protected abstract File getDriverFile();

    /**
     * URLを指定して表示します。
     */
    public void get(String url) {
        this.originalDriver.get(url);
    }

    /**
     * 画面要素を指定してクリックします。
     */
    public void clickAndWait(By by) {
        WebDriverWait wait = new WebDriverWait(this.originalDriver, DEFAULT_TIMEOUT);

        /* 要素が、ページのDOMに存在して可視となるまで待つ */
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));

        WebElement element = findElement(by);

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
                wait.until(ExpectedConditions.elementToBeClickable(by));

                element.click();
            }

            @Override
            protected void recovery() {
                down(1);
            }

        }.start();

        /* 待機処理。次のHTMLが表示されるのを待ちます。 */
        new WebDriverWait(this.originalDriver, DEFAULT_TIMEOUT)
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

    public void type(By by, String value) {
        typeCore(by, value);
    }

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
     * キーダウンをします。
     */
    public void down(int i) {
        for (int j = 0; j < i; j++) {
            findElement(BODY).sendKeys(Keys.DOWN);
        }
    }

    /**
     * セレクトボックスを選択します。
     */
    public void choiceByText(By by, String value) {
        WebElement element = findElement(by);
        if (element.getTagName().equals("select") == false) {
            throw new RuntimeException("セレクトボックスではありません。" + by.toString());
        }
        new Select(element).selectByVisibleText(value);
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
                    LOGGER.error("scoped_dirの削除に失敗", e);

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
        String title = this.originalDriver.getTitle();
        if (title.contains(partialTitle) == false) {
            throw new RuntimeException("タイトル不正：title=" + title + " expect=" + partialTitle);
        }
    }

    /**
     * 中断処理を入れます。
     * @param i ミリ秒
     */
    public void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void back() {
        this.originalDriver.navigate().back();
    }

    public void until(ExpectedCondition<WebElement> condition) {
        new WebDriverWait(this.originalDriver, DEFAULT_TIMEOUT).until(condition);
    }

    public WebDriver getDriver() {
        return this.originalDriver;
    }

}
