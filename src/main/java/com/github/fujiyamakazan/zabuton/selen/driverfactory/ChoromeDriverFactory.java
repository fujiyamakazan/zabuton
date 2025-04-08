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

import com.github.fujiyamakazan.zabuton.util.HttpAccessObject;
import com.github.fujiyamakazan.zabuton.util.file.ZipUtils;

/**
 * GoogleChrome用のWebドライバのファクトリです。
 */
public class ChoromeDriverFactory extends DriverFactory {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    public static final String DRIVER_EXE = "chromedriver.exe";

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
        HttpAccessObject hao = new HttpAccessObject();
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