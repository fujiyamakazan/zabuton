package com.github.fujiyamakazan.zabuton.util;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.wicket.util.lang.Generics;

/**
 * HTTP(S)接続をするユーティリティです。
 *
 * @author fujiyama
 */
public class HttpConnector implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(HttpConnector.class);

    private List<Cookie> cookies = Generics.newArrayList();

    /**
     * GETでリクエストします。(単一リクエスト用)
     */
    public static String doGet(String url) {

        return new HttpConnector().getCore(url);

        //非同期
        //BodyHandler<String> responseBodyHandler = HttpResponse.BodyHandlers.ofString();
        //CompletableFuture<HttpResponse<String>> future = client.sendAsync(request, responseBodyHandler);
        ///* thenAccept・・・戻り値がないLambdaを処理 */
        //future.thenAccept(res -> {
        //    System.out.println(res.body());
        //});
        //Thread.sleep(3000);

    }

    /**
     * POSTでリクエストします。(単一リクエスト用)
     */
    public static String doPost(String url, List<KeyValue> postParams) {
        HttpResponse<String> res = new HttpConnector().postCore(url, postParams);
        return res.body();
    }

    /**
     * ファイルをダウンロードします。（単一リクエスト用）
     */
    public static void doDownload(String url, File to) {
        new HttpConnector().downloadCore(url, to);
    }

    /**
     * GETでリクエストします。
     */
    public String get(String url) {
        return getCore(url);
    }

    private String getCore(String url) {
        HttpClient cl = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        /* Cookieを設定します。 */
        for (Cookie cookie: cookies) {
            builder = builder.setHeader("Cookie", cookie.getName() + "=" + cookie.getValue());
        }
        HttpRequest req = builder.build();

        HttpResponse<String> res;
        try {
            res = cl.send(req, HttpResponse.BodyHandlers.ofString());
            checkStatuCode(url, res);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /* Cookieを取得します。 */
        setCookies(res.headers().allValues("set-cookie"));

        return res.body();
    }


    /**
     * POSTでリクエストします。
     */
    public String post(String url, List<KeyValue> postParams) {
        HttpResponse<String> res = postCore(url, postParams);
        String body = res.body();
        return body;
    }

    private HttpResponse<String> postCore(String url, List<KeyValue> postParams) {
        StringBuilderLn sb = new StringBuilderLn("&");
        for (KeyValue kv : postParams) {
            sb.appendLn(kv.getKey() + "=" + kv.getValue());
        }
        String strPostparam = sb.toString();
        HttpClient cl = HttpClient.newHttpClient();
        Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(strPostparam));

        /* Cookieを設定します。 */
        for (Cookie cookie: cookies) {
            builder = builder.setHeader("Cookie", cookie.getName() + "=" + cookie.getValue());
        }

        HttpRequest req = builder.build();
        HttpResponse<String> res;
        try {
            res = cl.send(req, HttpResponse.BodyHandlers.ofString());
            checkStatuCode(url, res);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /* Cookieを取得します。 */
        setCookies(res.headers().allValues("set-cookie"));

        return res;
    }

    /**
     * ファイルをダウンロードします。
     */
    public void download(String url, File to) {
        downloadCore(url, to);
    }

    private void downloadCore(String url, File to) {
        HttpGet req = new HttpGet(url);

        /* Cookieを設定します。 */
        for (Cookie cookie: cookies) {
           req.addHeader("Cookie", cookie.getName() + "=" + cookie.getValue());
        }

        try (final CloseableHttpClient cl = HttpClients.createDefault();
            final CloseableHttpResponse res = cl.execute(req)) {
            checkStatuCode(url, res.getStatusLine().getStatusCode());
            final HttpEntity entity = res.getEntity();
            Files.write(Paths.get(to.getAbsolutePath()),
                entity == null ? new byte[0] : EntityUtils.toByteArray(entity));

            ///* Cookieを取得します。 */
            //List<String> setHeaders = Generics.newArrayList();
            //for(HeaderIterator ite = res.headerIterator("set-cookie"); ite.hasNext();) {
            //    setHeaders.add(ite.nextHeader().toString());
            //}
            //setCookies(setHeaders);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkStatuCode(String url, HttpResponse<String> res) {
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
        for (Entry<String, Cookie> entry : cookies.entrySet()) {
            LOGGER.debug("Cookie:" + entry.getValue().getName() + "=" + entry.getValue().getValue());
        }
        this.cookies = new ArrayList<Cookie>(cookies.values());
    }

}

//    private static HttpURLConnection openConnection(
//        String proxyHost, String proxyPort, URL url) throws IOException {
//        HttpURLConnection connection;
//
//        if (proxyHost == null) {
//            connection = (HttpURLConnection) url.openConnection();
//            return connection;
//        } else {
//            SocketAddress addr = new InetSocketAddress(
//                proxyHost,
//                Integer.valueOf(proxyPort));
//            Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
//
//            connection = (HttpURLConnection) url.openConnection(proxy);
//            return connection;
//        }
//    }
