package com.github.fujiyamakazan.zabuton.selen.driverfactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

/**
 * MS Edge用のWebドライバのファクトリです。
 */
public class EdgeDriverFactory extends DriverFactory {
    private static final long serialVersionUID = 1L;
    public static final String DRIVER_EXE = "msedgedriver.exe";

    public EdgeDriverFactory(File driverDir) {
        super(driverDir);
    }

    @Override
    public String getDriverFileName() {
        return DRIVER_EXE;
    }

    @Override
    public String getDriverUrl() {
        return "https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/";
        //throw new UnsupportedOperationException("この仕組みでは自動ダウンロードを目指しています");
    }

    @Override
    public void download() {
        throw new UnsupportedOperationException("未実装です");
    }

    @Override
    public WebDriver create(File downloadFilepath) {

        //WebDriverManager.edgedriver().setup();
        System.setProperty("webdriver.edge.driver", driverFile.getAbsolutePath());

        Map<String, Object> prefs = new HashMap<>();
        //prefs.put("download.default_directory", downloadFilepath); // ダウンロード先の指定
        prefs.put("download.default_directory", downloadFilepath.getAbsolutePath()); // ダウンロード先の指定
        prefs.put("download.prompt_for_download", false); // ダウンロード確認ダイアログを無効化
        prefs.put("profile.default_content_settings.popups", 0); // ポップアップを無効化

        EdgeOptions options = new EdgeOptions();
        //options.addArguments("--start-maximized");
        options.setExperimentalOption("prefs", prefs);

        return new EdgeDriver(options);

    }

    @Override
    public boolean occurredIllegalVersionDetected(Exception e) {
        return e instanceof SessionNotCreatedException
            && e.getMessage().contains("This version of ChromeDriver only supports Chrome version");
    }

}