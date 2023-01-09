package com.github.fujiyamakazan.zabuton.selen;

import java.io.File;
import java.io.Serializable;

import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;

public class SelenSample implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SelenSample.class);

    /**
     * 処理を実行します。
     */
    public static void main(String[] args) {

        SelenCommonDriver driver = new SelenCommonDriver() {

            private static final long serialVersionUID = 1L;

            //            @Override
            //            protected WebDriver createDriver() {
            //
            //                System.setProperty("webdriver.edge.driver", getDriverFile().getAbsolutePath());
            //
            //                WebDriver driver = new EdgeDriver();
            //
            //                driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS); // 暗黙的な待機時間を設定
            //
            //                return driver;
            //            }

            @Override
            protected File getDownloadDir() {
                return new File("C:\\tmp\\");
            }

            @Override
            protected File getDriverDir() {
                //return new File("C:\\tmp\\msedgedriver.exe");
                return new File("C:\\tmp");
            }

        };

        driver.get("https://www.amazon.co.jp/gp/css/order-history?ie=UTF8&ref_=nav_orders_first&");

        PasswordManager pm = new PasswordManager(EnvUtils.getProjectDir());
        pm.executeBySightKey("amazon");

        driver.type(By.name("email"), pm.getId());
        driver.clickAndWait(By.id("continue"));
        driver.type(By.name("password"), pm.getPassword());
        driver.clickAndWait(By.id("signInSubmit"));

        driver.clickLinkPartialAndWait("過去3か月");

    }

}
