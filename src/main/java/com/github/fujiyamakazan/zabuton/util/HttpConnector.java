package com.github.fujiyamakazan.zabuton.util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.wicket.util.lang.Generics;

/**
 * HTTP(S)接続をします。
 *
 * TODO リファクタリング
 *
 * @author fujiyama
 */
public class HttpConnector implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(HttpConnector.class);

    private InetSocketAddress proxyAddress = null;
    private List<Cookie> cookies = Generics.newArrayList();
    /** タイムアウト（ミリ秒）です。*/
    private Integer timeout = null;
    private String userAgent;

    /**
     * デフォルトコンストラクタです。
     */
    public HttpConnector() {
        /* 処理なし */
    }

    /**
     * プロキシを指定するコンストラクタです。
     */
    public HttpConnector(String addr, int port) {
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
        return new HttpConnector().get(url);
    }

    /**
     * GETでリクエストします。
     */
    public String get(String url) {
//        HttpClient cl = buildClient();
//
//        Builder reqBuilder = HttpRequest.newBuilder().uri(URI.create(url));
//        /* Cookieを設定します。 */
//        for (Cookie cookie : cookies) {
//            reqBuilder = reqBuilder.setHeader("Cookie", cookie.getName() + "=" + cookie.getValue());
//        }
//        /* User-Agentを設定します。*/
//        if (userAgent != null) {
//            reqBuilder = reqBuilder.setHeader("User-Agent", userAgent);
//        }
//        HttpRequest req = reqBuilder.build();
//
//        HttpResponse<String> res;
//        try {
//            res = cl.send(req, HttpResponse.BodyHandlers.ofString());
//            checkStatuCode(url, res);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        /* Cookieを取得します。 */
//        setCookies(res.headers().allValues("set-cookie"));
//
//        return res.body();


        HttpGet req = getRequest(url);
        try (final CloseableHttpClient cl = HttpClients.createDefault();
            final CloseableHttpResponse res = cl.execute(req)) {

            /* ステータスコードをチェックします。 */
            checkStatuCode(url, res.getStatusLine().getStatusCode());

            /* cookieを引き継ぎます。 */
            setCookies(res.getHeaders("set-cookie"));

            /* 結果を返します。 */
            return EntityUtils.toString(res.getEntity());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * POSTでリクエストします。
     */
    public String post(String url, List<KeyValue> params) {

        HttpPost  req = getRequestPost(url);

        List<BasicNameValuePair> formparams = Generics.newArrayList();
        for (KeyValue kv : params) {
            formparams.add(new BasicNameValuePair(kv.getKey(), kv.getValue()));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, StandardCharsets.UTF_8);
        req.setEntity(entity);

        try (final CloseableHttpClient cl = HttpClients.createDefault();
            final CloseableHttpResponse res = cl.execute(req)) {

            /* ステータスコードをチェックします。 */
            checkStatuCode(url, res.getStatusLine().getStatusCode());

            /* cookieを引き継ぎます。 */
            setCookies(res.getHeaders("set-cookie"));

            /* 結果を返します。 */
            return EntityUtils.toString(res.getEntity());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        HttpClient cl = buildClient();
//
//        StringBuilderLn sb = new StringBuilderLn("&");
//        for (KeyValue kv : params) {
//            sb.appendLn(kv.getKey() + "=" + kv.getValue());
//        }
//        String strPostparam = sb.toString();
//
//        java.net.http.HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
//            .uri(URI.create(url))
//            .header("Content-Type", "application/x-www-form-urlencoded")
//            .POST(HttpRequest.BodyPublishers.ofString(strPostparam));
//
//        /* Cookieを設定します。 */
//        for (Cookie cookie : cookies) {
//            reqBuilder = reqBuilder.setHeader("Cookie", cookie.getName() + "=" + cookie.getValue());
//        }
//        /* User-Agentを設定します。*/
//        if (userAgent != null) {
//            reqBuilder = reqBuilder.setHeader("User-Agent", userAgent);
//        }
//
//        HttpRequest req = reqBuilder.build();
//        HttpResponse<String> res;
//        try {
//            res = cl.send(req, HttpResponse.BodyHandlers.ofString());
//            checkStatuCode(url, res);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        /* Cookieを取得します。 */
//        setCookies(res.headers().allValues("set-cookie"));
//        String body = res.body();
//        return body;
    }

    /**
     * ファイルをダウンロードします。
     */
    public void download(String url, File to) {
        HttpGet req = getRequest(url);
        try (final CloseableHttpClient cl = HttpClients.createDefault();
            final CloseableHttpResponse res = cl.execute(req)) {

            checkStatuCode(url, res.getStatusLine().getStatusCode());
            final HttpEntity entity = res.getEntity();
            Files.write(Paths.get(to.getAbsolutePath()),
                entity == null ? new byte[0] : EntityUtils.toByteArray(entity));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpGet getRequest(String url) {
        HttpGet req = new HttpGet(url);
        org.apache.http.client.config.RequestConfig.Builder configBuilder = RequestConfig.custom();

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
        return req;
    }

    private HttpPost getRequestPost(String url) {
        HttpPost req = new HttpPost(url);
        org.apache.http.client.config.RequestConfig.Builder configBuilder = RequestConfig.custom();

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
        return req;
    }

    private static void checkStatuCode(String url, HttpResponse<String> res) {
        int code = res.statusCode();
        checkStatuCode(url, code);
        if (code >= 300 && code < 400) {
            LOGGER.debug("Location" + res.headers().map().get("Location").toString());
        }
    }

    private static void checkStatuCode(String url, int code) {
        LOGGER.debug("URL:" + url);
        LOGGER.debug("StatuCode:" + code);
        if (code >= 400) {
            throw new RuntimeException("StatuCode: " + code);
        }
    }

    private void setCookies(Header[] headers) {
        List<String> setCookies = Generics.newArrayList();
        for (Header header: headers) {
            setCookies.add(header.getValue());
        }
        setCookies(setCookies);
    }

    private void setCookies(List<String> setCookies) {
        Map<String, Cookie> cookies = Generics.newHashMap();
        for (String line : setCookies) {
            Cookie cookie = null;
            for (String token : line.split(";")) {
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
    }

    private HttpClient buildClient() {
        java.net.http.HttpClient.Builder clBuilder = HttpClient.newBuilder();
        if (proxyAddress != null) {
            /* プロキシを設定します。 */
            clBuilder = clBuilder.proxy(ProxySelector.of(proxyAddress));
        }
        if (this.timeout != null) {
            /* タイムアウトを設定します。*/
            clBuilder = clBuilder.connectTimeout(Duration.ofMillis(timeout));
        }

        HttpClient cl = clBuilder.build();
        //.version(HttpClient.Version.HTTP_2)
        return cl;
    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) throws IOException {

        final HttpConnector connectorNoProxy = new HttpConnector();
        connectorNoProxy.get("http://プロキシ未設定で接続するサイト");

        final HttpConnector connector = new HttpConnector("proxyホスト", 8080);
        List<KeyValue> postParams = Generics.newArrayList();
        postParams.add(new KeyValue("Postパラメータ１", "Postパラメータ１の値"));
        postParams.add(new KeyValue("Postパラメータ２", "Postパラメータ２の値"));
        String html;
        html = connector.post("ログイン画面URL", postParams);
        html = connector.get("処理画面URL");
        LOGGER.debug(html);

        /* ファイルダウンロードの検査 */
        File file = File.createTempFile(HttpConnector.class.getSimpleName(), ".jpg");
        file.deleteOnExit();
        connector.download("https://cover.openbd.jp/9784087474398.jpg", file);

        LOGGER.debug(file.getAbsolutePath());;

    }


}
