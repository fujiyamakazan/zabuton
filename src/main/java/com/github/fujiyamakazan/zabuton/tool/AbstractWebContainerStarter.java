package com.github.fujiyamakazan.zabuton.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fujiyamakazan.zabuton.util.JFrameUtils;

/**
 * Webコンテナを起動します。
 * 既にポートが利用されていないかをチェックします。
 * ポートが利用されていても、同一アプリケーションでなければ、
 * 空きポートでリトライします。
 *
 * @author fujiyama
 */
public abstract class AbstractWebContainerStarter {
    private static final Logger log = LoggerFactory.getLogger(AbstractWebContainerStarter.class);

    /** アプリケーションクラスの名前を確認できる画面のURL。 */
    private static final String APP_INFO = "/app-info";

    private static final int DEFAULT_PORT = 8080;

    private final Class<?> appClass;

    private boolean running = false;
    public boolean isRunning() {
        return running;
    }
    private int port;

    private String subParams;

    public AbstractWebContainerStarter(Class<?> appClass) {
        this.appClass = appClass;
        this.port = getPortStart();
    }


    public AbstractWebContainerStarter(Class<?> appClass, String subParams) {
        this.appClass = appClass;
        this.subParams = subParams;
        this.port = getPortStart();
    }

    protected int getPortStart() {
        return DEFAULT_PORT;
    }


    /**
     * ポートをチェックして、Webコンテナを開始します。
     */
    public void start() {

        int retryCount = 0;
        while (this.running == false) {

            try {
                /* 起動を試みる */
                tryStartServer(retryCount);

                log.info("起動成功");

                this.running = true;
                return;

            } catch (ApplicationAlreadyException e) {

                JFrameUtils.showMessageDialog("アプリケーションは起動中です。");
                System.exit(0);

            } catch (PortAlreadyException e) {

                /* スキャン続行 */
                port = port + 1;

            }

            /* リトライ制限回数を超えていれば終了する */
            if (retryCount++ > 10) {

                JFrameUtils.showMessageDialog("利用できるポートが見つかりませんでした。");
                System.exit(0);
            }
        }
    }

    private void tryStartServer(int retryCount)
        throws ApplicationAlreadyException, PortAlreadyException {

        /*
         * ポートの使用状況を確認する
         */
        String classNameOnHtml = "";
        boolean portNoUsed = false;
        HttpURLConnection con = null;
        try {
            URL url = new URL(getUrlRoot() + APP_INFO);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();

            try (InputStream is = con.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                classNameOnHtml = sb.toString();
            }

        } catch (Exception e) {

            log.info("port=" + port + "に既存のアプリケーションは起動していません。");
            portNoUsed = true;

        } finally {
            if (con != null) {
                con.disconnect();
            }
        }

        if (portNoUsed == false) {
            if (StringUtils.equals(classNameOnHtml, appClass.getName())) {

                log.warn("port=" + port + "に同じアプリケーションが起動しています。");
                throw new ApplicationAlreadyException();

            } else {

                log.info("port=" + port + "に異なるアプリが起動しています。＞スキャン続行");
                throw new PortAlreadyException();

            }
        }

        /* アプリケーションを特定するためのサーブレット */
        AppInfoServlet appInfoServlet = new AppInfoServlet(appClass);

        /* サーバーを起動する */
        startServer(port, appInfoServlet, APP_INFO, appClass);

    }

    protected abstract void startServer(
        int port,
        AppInfoServlet appInfoServlet,
        String appInfoUrl,
        Class<?> appClass) throws PortAlreadyException;

    /**
     * ブラウザで開くURLを返す。
     * @return URL
     */
    public String getUrl() {

        if (StringUtils.isNotEmpty(subParams)) {
            return getUrlRoot() + subParams;
        }

        return getUrlRoot();
    }

    private String getUrlRoot() {
        return "http://localhost:" + port + "/";
    }

    protected static final class AppInfoServlet extends HttpServlet {
        private final Class<?> appClass;
        private static final long serialVersionUID = 1L;

        public AppInfoServlet(Class<?> appClass) {
            this.appClass = appClass;
        }

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
            Writer w = resp.getWriter();
            w.write(appClass.getName());
            w.flush();
        }
    }

    /**
     * portが使用済みの時にthrowする例外です。
     */
    public class PortAlreadyException extends Exception {
        private static final long serialVersionUID = 1L;

    }

    /**
     * アプリケーションが起動済みの時にthrowする例外です。
     */
    public class ApplicationAlreadyException extends Exception {
        private static final long serialVersionUID = 1L;

    }
}
