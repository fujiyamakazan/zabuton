package com.github.fujiyamakazan.zabuton.selen;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.RetryWorker;

public abstract class SelenCommonDriver implements Serializable {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SelenCommonDriver.class);

    private static final By BODY = By.cssSelector("body");

    private static final long serialVersionUID = 1L;

    private final transient WebDriver originalDriver;

    public SelenCommonDriver() {
        this.originalDriver = createDriver();
    }

    protected abstract WebDriver createDriver();

    public void get(String url) {
        originalDriver.get(url);
        /* ページの表示を待ちます */
        //new WebDriverWait(this.originalDriver, 1).until(ExpectedConditions.);
    }

    public void clickAndWait(By by) {

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
                element.click();
            }

            @Override
            protected void recovery() {
                down(1);
            }

        }.start();

        /* 待機処理。次のHTMLが表示されるのを待ちます。 */
        new WebDriverWait(this.originalDriver, 1)
            .until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
    }

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

    public void down(int i) {
        for (int j = 0; j < i; j++) {
            findElement(BODY).sendKeys(Keys.DOWN);
        }
    }

    public void choiceByText(By by, String value) {
        WebElement element = findElement(by);
        if (element.getTagName().equals("select") == false) {
            throw new RuntimeException("セレクトボックスではありません。" + by.toString());
        }
        new Select(element).selectByVisibleText(value);
    }

    /**
     * @param recoveryTimeout 処理後に戻すときのタイムアウト時間
     */
    public boolean containsText(String text, int recoveryTimeoutSec) {

        /* タイムアウトを短くする */
        this.originalDriver.manage().timeouts().implicitlyWait(200, TimeUnit.MILLISECONDS);

        try {
            return findElements(By.xpath("//*[contains(., '" + text + "')]")).isEmpty() == false;
        } finally {
            this.originalDriver.manage().timeouts().implicitlyWait(recoveryTimeoutSec * 1000, TimeUnit.MILLISECONDS);
        }

    }

    public boolean isPresent(By by) {
        return findElements(by).isEmpty() == false;
    }

    public String getPageSource() {
        return this.originalDriver.getPageSource();
    }

    public WebElement findElement(By by) {

        /*
         * 新しいウィンドウに遷移しているかもしれないので、
         * 最新のウィンドウハンドルに切り替えます。
         */
        updateWindowHandle();

        /* TODO 表示されるまで待つ */

        return originalDriver.findElement(by);
    }

    public List<WebElement> findElements(By by) {

        /*
         * 新しいウィンドウに遷移しているかもしれないので、
         * 最新のウィンドウハンドルに切り替えます。
         */
        updateWindowHandle();

        /* TODO 表示されるまで待つ */

        return originalDriver.findElements(by);
    }

    /**
     * 新しいウィンドウに遷移しているかもしれないので、
     * 最新のウィンドウハンドルに切り替えます。
     */
    private void updateWindowHandle() {

        Set<String> set = this.originalDriver.getWindowHandles();
        this.originalDriver.switchTo().window(set.toArray(new String[] {})[set.size() - 1]);
    }

    public void quit() {
        this.originalDriver.quit();
        deleteTempFile();
    }

    private static void deleteTempFile() {
        /* Windowsに溜まるゴミファイルの削除 */
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

    public void assertTitleContains(String partialTitle) {
        String title = this.originalDriver.getTitle();
        if (title.contains(partialTitle) == false) {
            throw new RuntimeException("タイトル不正：title=" + title + " expect=" + partialTitle);
        }
    }

    public void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
