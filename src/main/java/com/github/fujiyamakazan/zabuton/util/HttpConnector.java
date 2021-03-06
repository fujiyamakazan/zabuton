package com.github.fujiyamakazan.zabuton.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HttpConnector {

    public static void main(String[] args) {
        //String str = get("https://pgse.seesaa.net/", "xx.xx.xx.xx", "8080", StandardCharsets.UTF_8);
        String str = post("https://pgse.seesaa.net/", "abc", "xx.xx.xx.xx", "8080", StandardCharsets.UTF_8, "");
        System.out.println(str);
    }

    /**
     * URLを指定してGETで接続します。
     */
    public static String get(String url, String proxyHost, String proxyPort, Charset charset) {

        HttpURLConnection connection = null;
        InputStream in = null;
        BufferedReader reader = null;
        String result;
        try {
            URL urlObj = new URL(url);

            connection = openConnection(proxyHost, proxyPort, urlObj);

            connection.setRequestMethod("GET");
            connection.connect();

            int code = connection.getResponseCode();

            if (code == HttpURLConnection.HTTP_OK) {
                in = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in, charset));
                StringBuilder output = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    output.append(line + "\n");
                }
                result = output.toString();
            } else {
                throw new RuntimeException("接続失敗：url=" + url + " ResponseCode=" + code);
            }
        } catch (Exception e) {
            throw new RuntimeException(url + "への接続失敗", e);

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    /**
     * URLを指定してPOSTで接続します。
     * TODO 動作確認未実施
     */
    public static String post(
            String url,
            String postBody,
            String proxyHost,
            String proxyPort,
            Charset charset,
            String contextType) {

        StringBuilder sb = new StringBuilder();
        try {
            URL urlObj = new URL(url);

            HttpURLConnection connection = null;

            try {
                connection = openConnection(proxyHost, proxyPort, urlObj);

                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", contextType);

                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(connection.getOutputStream(), charset));
                writer.write(postBody);
                writer.flush();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader isr = null;
                    BufferedReader reader = null;
                    try {
                        isr = new InputStreamReader(connection.getInputStream(),
                                StandardCharsets.UTF_8);
                        reader = new BufferedReader(isr);
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        reader.close();
                        isr.close();
                    }
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    /**
     * URLを指定してファイルをダウンロードします。
     */
    public static void download(String url, File toFile, String proxyHost, String proxyPort)
            throws Exception {

        URL urlObj = new URL(url);

        DataInputStream dataInStream = null;
        DataOutputStream dataOutStream = null;

        try {
            HttpURLConnection conn = openConnection(proxyHost, proxyPort, urlObj);

            conn.setRequestMethod("GET");
            conn.connect();
            int code = conn.getResponseCode();

            if (code != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("接続失敗：url=" + url + " ResponseCode=" + code);
            }
            if (toFile != null) {
                dataInStream = new DataInputStream(conn.getInputStream());
                dataOutStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(
                        toFile)));
                byte[] b = new byte[4096]; // TODO 左記の値を検討
                int readByte = 0;
                while (-1 != (readByte = dataInStream.read(b))) {
                    dataOutStream.write(b, 0, readByte);
                }
            }

        } catch (Exception e) {
            throw e;

        } finally {
            if (dataInStream != null) {
                dataInStream.close();
            }
            if (dataOutStream != null) {
                dataOutStream.close();
            }
        }
    }

    private static HttpURLConnection openConnection(String proxyHost, String proxyPort, URL url) throws IOException {
        HttpURLConnection connection;
        SocketAddress addr = new InetSocketAddress(
                proxyHost,
                Integer.valueOf(proxyPort));
        Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);

        connection = (HttpURLConnection) url.openConnection(proxy);
        return connection;
    }

}
