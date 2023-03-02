package com.github.fujiyamakazan.zabuton.util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.wicket.util.lang.Generics;
import org.jsoup.Jsoup;

/**
 * HTTP(S)接続をします。
 * @author fujiyama
 */
public class HttpAccessObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(HttpAccessObject.class);

    private InetSocketAddress proxyAddress = null;
    private List<Cookie> cookies = Generics.newArrayList();
    /** タイムアウト（ミリ秒）です。*/
    private Integer timeout = null;
    private String userAgent;

    /**
     * デフォルトコンストラクタです。
     */
    public HttpAccessObject() {
        /* 処理なし */
    }

    /**
     * プロキシを指定するコンストラクタです。
     */
    public HttpAccessObject(String addr, int port) {
        proxyAddress = new InetSocketAddress(addr, port);
    }

    /**
     * タイムアウト（ミリ秒）を設定します。
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * 簡易的にGETでリクエストします。
     * post,ファイルダウンロード,プロキシ設定,cookieを使う場合は、インスタンスメソッドを使用してください。
     */
    public static String executeGet(String url) {
        return new HttpAccessObject().get(url);
    }

    /**
     * GETでリクエストします。
     */
    public String get(String url) {
        HttpGet req = new HttpGet(url);
        common(req);
        try (final CloseableHttpClient cl = HttpClients.createDefault();
            final CloseableHttpResponse res = cl.execute(req)) {
            HttpEntity entity = common(url, res);
            return EntityUtils.toString(entity);
        } catch (RedirectException re) {
            return get(re.url); // リダイレクト
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * POSTでリクエストします。
     */
    public String post(String url, List<BasicNameValuePair> params) {
        UrlEncodedFormEntity entityIn = new UrlEncodedFormEntity(params, StandardCharsets.UTF_8);
        HttpPost req = new HttpPost(url);
        common(req);
        req.setEntity(entityIn);
        try (final CloseableHttpClient cl = HttpClients.createDefault();
            final CloseableHttpResponse res = cl.execute(req)) {
            HttpEntity entity = common(url, res);
            return EntityUtils.toString(entity);
        } catch (RedirectException re) {
            return get(re.url); // リダイレクト
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ファイルをダウンロードします。
     */
    public void download(String url, File to) {
        HttpGet req = new HttpGet(url);
        common(req);
        try (final CloseableHttpClient cl = HttpClients.createDefault();
            final CloseableHttpResponse res = cl.execute(req)) {
            HttpEntity entity = common(url, res);
            byte[] byteArray = entity == null ? new byte[0] : EntityUtils.toByteArray(entity);
            Files.write(Paths.get(to.getAbsolutePath()), byteArray);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * リクエストに対する共通処理をします。
     */
    private void common(HttpRequestBase req) {

        Builder configBuilder = RequestConfig.custom();

        /* プロキシを設定します。 */
        if (proxyAddress != null) {
            HttpHost proxy = new HttpHost(proxyAddress.getHostName(), proxyAddress.getPort());
            configBuilder = configBuilder.setProxy(proxy);
        }

        /* タイムアウトを設定します。*/
        if (this.timeout != null) {
            configBuilder = configBuilder.setConnectTimeout(timeout);
        }

        RequestConfig config = configBuilder.build();
        req.setConfig(config);

        /* Cookieを設定します。 */
        for (Cookie cookie : cookies) {
            req.addHeader("Cookie", cookie.getName() + "=" + cookie.getValue());
        }

        /* User-Agentを設定します。*/
        if (userAgent != null) {
            req.addHeader("User-Agent", userAgent);
        }
    }

    /**
     * レスポンスに対する共通処理をします。
     */
    private HttpEntity common(String url, final CloseableHttpResponse res) throws RedirectException {

        /* ステータスコードをチェックします。 */
        int code = res.getStatusLine().getStatusCode();
        LOGGER.debug("URL:" + url);
        LOGGER.debug("StatuCode:" + code);
        if (code >= 400) {
            throw new RuntimeException("StatuCode: " + code);
        }

        /* cookieを引き継ぎます。 */
        Map<String, Cookie> cookies = Generics.newHashMap();
        for (Header header : res.getHeaders("set-cookie")) {
            Cookie cookie = null;
            for (String token : header.getValue().split(";")) {
                String[] kv = token.split("=");
                String key = kv[0];
                if (StringUtils.isNotEmpty(key)) {
                    key = key.trim();
                }
                String value = "";
                if (kv.length > 1) {
                    value = kv[1];
                    if (StringUtils.isNotEmpty(value)) {
                        value = value.trim();
                    }
                }
                if (cookie == null) {
                    cookie = new Cookie(key, value);
                    cookies.put(key, cookie);
                } else {
                    if (StringUtils.equals(key, "secure")) {
                        cookie.setSecure(true);
                    }
                }
            }
        }
        for (Cookie cookie : cookies.values()) {
            LOGGER.debug("Cookie:" + cookie.getName() + "=" + cookie.getValue());

            /* 登録済みで重複するものを削除 */
            for (Iterator<Cookie> ite = this.cookies.iterator(); ite.hasNext();) {
                Cookie exist = ite.next();
                if (exist.getName().equals(cookie.getName())) {
                    ite.remove();
                    break;
                }
            }
            /* 登録 */
            this.cookies.add(cookie);
        }

        HttpEntity entity = res.getEntity();

        if (code >= 300 && code < 400) {
            String redirectUrl = res.getLastHeader("Location").getValue();
            LOGGER.debug("Location" + redirectUrl.toString());
            /* リダイレクト */
            throw new RedirectException(redirectUrl);
        }

        /* 結果を返します。 */
        return entity;
    }

    private class RedirectException extends Exception {

        private static final long serialVersionUID = 1L;
        private String url;

        public RedirectException(String url) {
            this.url = url;
        }

    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) throws IOException {

        final HttpAccessObject connectorNoProxy = new HttpAccessObject();
        connectorNoProxy.get("http://google.co.jp");

        final HttpAccessObject connector = new HttpAccessObject("proxyホスト", 8080);
        List<BasicNameValuePair> postParams = Generics.newArrayList();
        postParams.add(new BasicNameValuePair("Postパラメータ１", "Postパラメータ１の値"));
        postParams.add(new BasicNameValuePair("Postパラメータ２", "Postパラメータ２の値"));
        String html;
        html = connector.post("ログイン画面URL", postParams);
        html = connector.get("処理画面URL");
        LOGGER.debug(html);

        /* ファイルダウンロードの検査 */
        File file = File.createTempFile(HttpAccessObject.class.getSimpleName(), ".jpg");
        file.deleteOnExit();
        connector.download("https://cover.openbd.jp/9784087474398.jpg", file);

        LOGGER.debug(file.getAbsolutePath());
        ;

    }

    public static String getTitle(String html) {
        return Jsoup.parse(html).getElementsByTag("title").text();
    }

    /**
     * ポートが使用されているときにTrueを返します。
     */
    public static boolean usePort(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

}
