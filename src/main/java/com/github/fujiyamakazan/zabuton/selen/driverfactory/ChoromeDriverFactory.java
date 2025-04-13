package com.github.fujiyamakazan.zabuton.selen.driverfactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.github.fujiyamakazan.zabuton.Zabuton;
import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.HttpAccessObject;
import com.github.fujiyamakazan.zabuton.util.file.ZipUtils;
import com.github.fujiyamakazan.zabuton.util.string.Stringul;

/**
 * GoogleChrome用のWebドライバのファクトリです。
 */
public class ChoromeDriverFactory extends DriverFactory {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    private static final String URL_DRIVER = "https://googlechromelabs.github.io/chrome-for-testing/";
    public static final String DRIVER_EXE = "chromedriver.exe";

    public ChoromeDriverFactory() {
        super(new File(new File(Zabuton.getDir(), "selen_driver"), DRIVER_EXE));
    }

    public ChoromeDriverFactory(File driverDir) {
        super(new File(driverDir, DRIVER_EXE));
    }

    @Override
    public SelenCommonDriver build() {

        if (driverFile.exists() == false) {
            download(); // ドライバのファイルをダウンロード
            /* ダウンロードが失敗したら例外情報として返します。 */
            if (driverFile.exists() == false) {
                throw new RuntimeException(
                    "ドライバのファイルが" + driverFile.getAbsolutePath() + "にありません。"
                        + " [" + ChoromeDriverFactory.URL_DRIVER + "]からダウンロードしてください。");
            }
        }

        try {
            /* Choromeの環境変数を設定します。 */
            System.setProperty("webdriver.chrome.driver", driverFile.getAbsolutePath());

            /* ダウンロードフォルダを指定します。 */
            HashMap<String, Object> prefes = new HashMap<String, Object>();
            prefes.put("profile.default_content_settings.popups", 0);
            if (this.downloadDir != null) {
                prefes.put("download.default_directory", this.downloadDir.getAbsolutePath());
            }

            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("prefs", prefes);
            WebDriver driver = new ChromeDriver(options);

            return new SelenCommonDriver(driver);

        } catch (Exception e) {

            LOGGER.error(Stringul.ofException(e));
            if (e instanceof SessionNotCreatedException
                && e.getMessage().contains("This version of ChromeDriver only supports Chrome version")) {
                /* ドライバファイルのバージョン不正を検知したときの処理 */
                SelenCommonDriver.killTask();
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
    }

    private void download() {
        HttpAccessObject hao = new HttpAccessObject();
        String html = hao.get(URL_DRIVER);

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

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {
        File driverDir = EnvUtils.getUserDesktop("test");
        File downloadDir = EnvUtils.getUserDesktop("test/dl");
        SelenCommonDriver cmd = new ChoromeDriverFactory(driverDir)
            .downloadDir(downloadDir)
            .build();

        cmd.get("https://haritora.net/look.cgi?script=45");
        cmd.clickButtonAndWait("全文ダウンロード");
        cmd.quit();
    }

}