package com.github.fujiyamakazan.zabuton.util.security;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Set;

import org.apache.commons.codec.binary.StringUtils;
import org.openqa.selenium.Cookie;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
import com.github.fujiyamakazan.zabuton.util.CsvUtils;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public class CookieManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CookieManager.class);

    private static final String NONE = "NONE";
    private final SelenCommonDriver cmd;
    private final File saveDir;
    private String sightKey;
    private File cookieDir;

    /**
     * コンストラクタです。
     */
    public CookieManager(File appDir, SelenCommonDriver cmd) {
        this.saveDir = new File(appDir, "CookieManager");
        if (this.saveDir.exists() == false) {
            this.saveDir.mkdirs();
        }
        this.cmd = cmd;
    }

    /**
     * Cookieを読み込みます。
     * URLをキーとします。
     */
    public void executeByUrl(String url) {
        try {
            this.sightKey = new URI(url).getRawAuthority();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        ///* invalid cookie domainが発生しないようにする。 */
        //this.cmd.get(url);

        cookieDir = new File(this.saveDir, sightKey);

        try {
            if (cookieDir.exists()) {
                for (File f : cookieDir.listFiles()) {

                    String data = Utf8Text.readData(f);
                    String[] datas = CsvUtils.splitCsv(data);
                    String name = datas[0];
                    String value = datas[1];
                    String domain = datas[2];
                    String path = datas[3];
                    String strExpiry = datas[4];
                    final Date expiry;
                    if (StringUtils.equals(strExpiry, NONE)) {
                        expiry = null;
                    } else {
                        expiry = new Date(Long.parseLong(strExpiry));
                    }
                    LOGGER.debug("domain:" + domain);
                    Cookie cookie = new Cookie(name, value, domain, path, expiry);

                    cmd.addCookie(cookie);
                }
            }
        } catch (org.openqa.selenium.InvalidCookieDomainException e) {
            throw new RuntimeException("InvalidCookieDomainExceptionが発生しました。"
                + "Cooikeを操作する前にドメインへ移動してください。", e);
        }

        ///* 再表示 */
        //this.cmd.get(url);

    }

    /**
     * Cookieを書き出します。
     */
    public void save() {
        Set<Cookie> cookies = cmd.getDriver().manage().getCookies();
        for (Cookie cookie : cookies) {
            String name = cookie.getName();
            String value = cookie.getValue();
            String domain = cookie.getDomain();
            String path = cookie.getPath();
            Date dateExpiy = cookie.getExpiry();
            final String expiry;
            if (dateExpiy == null) {
                expiry = NONE;
            } else {
                expiry = String.valueOf(dateExpiy.getTime());
            }

            String datas = CsvUtils.convertString(new String[] { name, value, domain, path, expiry });

            File f = new File(cookieDir, name);
            Utf8Text.writeData(f, datas);
        }
    }
}
