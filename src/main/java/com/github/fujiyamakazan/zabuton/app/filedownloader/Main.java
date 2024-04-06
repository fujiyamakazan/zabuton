package com.github.fujiyamakazan.zabuton.app.filedownloader;

import java.io.File;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.HttpAccessObject;
import com.github.fujiyamakazan.zabuton.util.jframe.JFrameUtils;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.string.StringCutter;

public class Main {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("TEST");

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

        try {

         // TODO seesaaのファイルマネージャーに特化
            cmd.get("https://ssl.seesaa.jp/auth");

            File appDir = EnvUtils.getUserDesktop("test");
            PasswordManager pm = new PasswordManager(appDir);
            pm.executeByUrl("https://ssl.seesaa.jp");
            cmd.type(By.name("email"), pm.getId());
            cmd.type(By.name("password"), pm.getPassword());
            cmd.clickButtonAndWait("サインイン");

            JFrameUtils.showConfirmDialog("ダウンロードしたいページを開いてください。");



            //List<WebElement> list = cmd.findElements(By.cssSelector(".list-table td:first-child a"));
            Document d = Jsoup.parse(cmd.getPageSource());
            Elements elements = d.select(".list-table td:first-child a");

            for (Element e: elements) {
                String href = "https://blog.seesaa.jp/cms/upload/regist/"  + e.attr("href");
                System.out.println(href);
                cmd.get(href);

                Document d2 = Jsoup.parse(cmd.getPageSource());
                Elements elements2 = d2.select(".upload-file .upload-name a");

                for (Element e2: elements2) {
                    String url = e2.attr("href");
                    File to = new File(appDir, StringCutter.right(url, "/").replaceAll(":", "_"));
                    new HttpAccessObject("10.2.0.4", 8080).download(url, to);
                }

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            cmd.quit();
        }



    }
}
