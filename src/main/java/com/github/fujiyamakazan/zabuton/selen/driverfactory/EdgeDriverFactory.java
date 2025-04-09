package com.github.fujiyamakazan.zabuton.selen.driverfactory;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.danekja.java.misc.serializable.SerializableRunnable;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;

/**
 * MS Edge用のWebドライバのファクトリです。
 */
public class EdgeDriverFactory extends DriverFactory {
    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    //private static final String URL_DRIVER = "https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/";
    public static final String DRIVER_EXE = "msedgedriver.exe";


    public EdgeDriverFactory(File driverDir) {
        super(new File(driverDir, DRIVER_EXE));
    }

    @Override
    public SelenCommonDriver build() {

        //WebDriverManager.edgedriver().setup();
        System.setProperty("webdriver.edge.driver", driverFile.getAbsolutePath());

        Map<String, Object> prefs = new HashMap<>();
        //prefs.put("download.default_directory", downloadFilepath); // ダウンロード先の指定
        prefs.put("download.default_directory", downloadDir.getAbsolutePath()); // ダウンロード先の指定
        prefs.put("download.prompt_for_download", false); // ダウンロード確認ダイアログを無効化
        prefs.put("profile.default_content_settings.popups", 0); // ポップアップを無効化

        EdgeOptions options = new EdgeOptions();
        //options.addArguments("--start-maximized");
        options.setExperimentalOption("prefs", prefs);

        //「自動テストソフトウェアによって制御されています」を非表示にする
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        WebDriver driver = new EdgeDriver(options);

        /* ファイルダウンロードが中断しないようにする処理 */
        SerializableRunnable onQuit = () -> {

            int waited = 0;
            while (waited < 5) {
                boolean downloading = false;

                File[] files = downloadDir.listFiles();
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

        return new SelenCommonDriver(driver, onQuit);
    }

//    @Override
//    public boolean occurredIllegalVersionDetected(Exception e) {
//        return e instanceof SessionNotCreatedException
//            && e.getMessage().contains("This version of ChromeDriver only supports Chrome version");
//    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {
        File driverDir = EnvUtils.getUserDesktop("test");
        File downloadDir = EnvUtils.getUserDesktop("test/dl");
        SelenCommonDriver cmd = new EdgeDriverFactory(driverDir)
            .downloadDir(downloadDir)
            .build();

        cmd.get("https://haritora.net/look.cgi?script=45");
        cmd.clickButtonAndWait("全文ダウンロード");
        cmd.quit();
    }








}