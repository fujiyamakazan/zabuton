package com.github.fujiyamakazan.zabuton.selen;

import java.io.File;
import java.io.Serializable;

import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.selen.driverfactory.EdgeDriverFactory;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.jframe.JFrameUtils;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;

public class SelenSample implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SelenSample.class);

    /**
     * 処理を実行します。
     */
    public static void main(String[] args) {

//        new Thread() {
//            @Override
//            public void run() {
//                main();
//            };
//        }.start();
//
//        new Thread() {
//            @Override
//            public void run() {
//                main();
//            };
//        }.start();

        SelenCommonDriver cmd = new EdgeDriverFactory(EnvUtils.getUserDesktop("test"))
            .downloadDir(new File(EnvUtils.getUserDesktop("test"), "dl"))
            .build();
        cmd.get("https://haritora.net/look.cgi?script=1245");
        cmd.clickButtonAndWait("全文ダウンロード");
        cmd.sleep(300);

        cmd.quit();

    }

    protected static void main() {
        SelenCommonDriver driver = new EdgeDriverFactory(new File("C:\\tmp"))
            .downloadDir(new File("C:\\tmp"))
            .build();


        driver.get("https://www.amazon.co.jp/gp/css/order-history?ie=UTF8&ref_=nav_orders_first&");

        PasswordManager pm = new PasswordManager();
        pm.executeBySightKey("amazon");

        driver.type(By.name("email"), pm.getId());
        driver.clickAndWait(By.id("continue"));
        driver.type(By.name("password"), pm.getPassword());
        driver.clickAndWait(By.id("signInSubmit"));

        //driver.clickLinkPartialAndWait("過去3か月");

        if(JFrameUtils.showConfirmDialog("終了しますか?")) {
            driver.quit();
        }

    }

}
