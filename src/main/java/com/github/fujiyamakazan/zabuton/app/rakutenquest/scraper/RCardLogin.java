package com.github.fujiyamakazan.zabuton.app.rakutenquest.scraper;

import java.lang.invoke.MethodHandles;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;

public class RCardLogin {
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(MethodHandles.lookup().lookupClass());

    public void login(PasswordManager pm, SelenCommonDriver cmd) {
        String url = "https://www.rakuten-card.co.jp/e-navi/members/";
        cmd.get(url);
        //            cmd.get(
        //                "https://login.account.rakuten.com/sso/authorize"
        //                    + "?client_id=rakuten_card_enavi_web"
        //                    + "&redirect_uri=https://www.rakuten-card.co.jp/e-navi/auth/login.xhtml"
        //                    + "&scope=openid%20profile&response_type=code&prompt=login#/sign_in");

        if (StringUtils.contains(cmd.getTitle(), "ログイン")) {
            /* ログイン */
            //pm.executeBySightKey("rakuten");
            pm.executeByUrl(url);
            cmd.type(By.name("username"), pm.getId());
            cmd.clickAndWait(By.xpath("(//body//div[text() = '次へ'])[1]"));
            cmd.type(By.name("password"), pm.getPassword());
            cmd.clickAndWait(By.xpath("(//body//div[text() = '次へ'])[2]"));

//            // 更にもう一回
//            cmd.sleep(3000);
//            cmd.type(By.name("password"), pm.getPassword());
//            cmd.sleep(3000);
//            cmd.clickAndWait(By.xpath("//body//div[text() = '次へ']"));
        }

        // カード切替え
        String type = selectCardType();
        if (StringUtils.isNotEmpty(type)) {
            cmd.choiceByText(By.xpath("//select[@id='cardChangeForm:cardtype']"), "楽天カード（ＪＣＢ）");
        }
    }

    protected String selectCardType() {
        return null;
    }

}
