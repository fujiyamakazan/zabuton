package com.github.fujiyamakazan.zabuton.util.starter;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.wicket.protocol.http.ContextParamWebApplicationFactory;
import org.apache.wicket.protocol.http.WicketFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fujiyamakazan.zabuton.util.HttpAccessObject;
import com.github.fujiyamakazan.zabuton.util.jframe.JFrameUtils;

/**
 * アプリケーションをTomcatで起動します。
 * @author fujiyama
 */
//public class WicketBootByTomcat extends AbstractWebContainerStarter {
public class WicketBootByTomcat {

    private static final Logger LOGGER = LoggerFactory.getLogger(WicketBootByTomcat.class);

    //public WicketBootByTomcat(Class<?> appClass) {
    //    super(appClass);
    //}

    //public WicketBootByTomcat(Class<?> appClass, String subParams) {
    //    super(appClass, subParams);
    //}

    /* ------------------------------------------------------------ */

    /** アプリケーションを確認できる画面のURですL。 */
    private static final String APP_INFO = "/app-info";

    /** アプリケーションを終了できる画面のURLです。 */
    private static final String APP_EXIT = "/app-exit";

    private static final int DEFAULT_PORT = 8080;

    private final Class<?> appClass;
    private String dispName;

    private boolean running = false;

    public boolean isRunning() {
        return this.running;
    }

    private Tomcat tomcat;

    private int port;

    private String subParams;

    public WicketBootByTomcat(Class<?> appClass) {
        this.appClass = appClass;
        this.port = DEFAULT_PORT;
    }

    /**
     * コンストラクタです。
     */
    public WicketBootByTomcat(Class<?> appClass, String subParams) {
        this(appClass);
        this.subParams = subParams;
    }

    public WicketBootByTomcat dispName(String dispName) {
        this.dispName = dispName;
        return this;
    }

    /**
     * ポートをチェックして、Webコンテナを開始します。
     */
    public void start() {

        //int retryCount = 0;
        //while (this.running == false) {

        //try {

        /*
         * ポートの使用状況を確認します。
         */
        //boolean portNoUsed = false;
        //HttpURLConnection con = null;
        //try {
        //    URL url = new URL(getUrlRoot() + APP_INFO);
        //    con = (HttpURLConnection) url.openConnection();
        //    con.setRequestMethod("GET");
        //    con.connect();
        //    try (InputStream is = con.getInputStream();
        //        BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
        //        StringBuilder sb = new StringBuilder();
        //        String line;
        //        while ((line = br.readLine()) != null) {
        //            sb.append(line);
        //        }
        //        classNameOnHtml = sb.toString();
        //    }
        //} catch (Exception e) {
        //    //log.info("port=" + this.port + "に既存のアプリケーションは起動していません。");
        //    portNoUsed = true;
        //} finally {
        //    if (con != null) {
        //        con.disconnect();
        //    }
        //}
        boolean usePort = HttpAccessObject.usePort(port);
        if (usePort) {
            try {
                String existInfo = HttpAccessObject.executeGet(getUrlRoot() + APP_INFO);
                if (StringUtils.equals(existInfo, getAppInfo())) {
                    //JFrameUtils.showErrorDialog("アプリケーションは起動中です。");

                    /*
                     * 再起動対応
                     */
                    if (JFrameUtils.showConfirmDialog("アプリケーションは起動中です。再起動しますか？")) {
                        try {
                            HttpAccessObject.executeGet(getUrlRoot() + APP_EXIT);
                        } catch (Exception e) {
                            /* 処理なし */
                        }
                        //System.exit(0);
                    } else {
                        System.exit(0);
                    }


                } else {
                    JFrameUtils.showErrorDialog("ポート[" + port + "]は" + existInfo + "が使用中です。");
                    System.exit(0);

                }
            } catch (Exception e) {
                /*
                 * TODO 具体的なアプリ名の表示
                 * 　ポート→PID cmd [netstat -nao | find ":8080"]
                 * 　PID→アプリ cmd [tasklist /fi "PID eq {PID}"]
                 */
                JFrameUtils.showErrorDialog("ポート[" + port + "]は使用中です。");
                System.exit(0);

            }
        }

        /* サーバーを起動する */
        startServer(this.port, this.appClass);
        LOGGER.info("起動成功");
        this.running = true;

        //return;

        //} catch (ApplicationAlreadyException e) {
        //    JFrameUtils.showErrorDialog("アプリケーションは起動中です。");
        //    System.exit(0);
        //} catch (PortAlreadyException e) {
        ///* スキャン続行 */
        //this.port = this.port + 1;
        //}
        ///* リトライ制限回数を超えていれば終了する */
        //if (retryCount++ > 10) {
        //    JFrameUtils.showErrorDialog("利用できるポートが見つかりませんでした。");
        //    System.exit(0);
        //}
    }
    //}

    //protected abstract void startServer(
    //    int port,
    //    AppInfoServlet appInfoServlet,
    //    String appInfoUrl,
    //    Class<?> appClass) throws PortAlreadyException;

    /**
     * ブラウザで開くURLを返す。
     * @return URL
     */
    public String getUrl() {

        if (StringUtils.isNotEmpty(this.subParams)) {
            return getUrlRoot() + this.subParams;
        }

        return getUrlRoot();
    }

    private String getUrlRoot() {
        return "http://localhost:" + this.port + "/";
    }

    ///**
    // * portが使用済みの時にthrowする例外です。
    // */
    //public class PortAlreadyException extends Exception {
    //    private static final long serialVersionUID = 1L;
    //    public PortAlreadyException(String messsage) {
    //        super(messsage);
    //    }
    //}

    ///**
    // * アプリケーションが起動済みの時にthrowする例外です。
    // */
    //public class ApplicationAlreadyException extends Exception {
    //    private static final long serialVersionUID = 1L;
    //}

    //@Override
    /**
     * サーバーを起動します。
     * Tomcat以外のサーバーを扱うことになったとき、このメソッドが拡張ポイントになります。
     */
    protected void startServer(
        int port,
        Class<?> appClass) {

        tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // portを設定するため

        //File base = new File("src/main/webapp/");
        File base = new File("webapp");
        if (base.exists() == false) {
            base.mkdirs();
            LOGGER.info("makedirs:" + base.getAbsolutePath());
        }
        Context context = tomcat.addContext("", base.getAbsolutePath());
        Tomcat.addServlet(context, "default", new DefaultServlet()).addMapping("/"); // 静的ファイルを扱う

        /* アプリケーションを特定するためのサーブレットを追加 */
        Tomcat.addServlet(context, APP_INFO, new AppInfoServlet()).addMapping(APP_INFO);

        /* アプリケーションを終了させるためのサーブレットを追加 */
        Tomcat.addServlet(context, APP_EXIT, new AppExitServlet()).addMapping(APP_EXIT);

        FilterDef filterDef = new FilterDef();
        final String filterName = "wickletFilter";
        filterDef.setFilterName(filterName);
        filterDef.setFilterClass(WicketFilter.class.getName());
        filterDef.addInitParameter(
            ContextParamWebApplicationFactory.APP_CLASS_PARAM,
            appClass.getName());

        filterDef.addInitParameter(
            WicketFilter.FILTER_MAPPING_PARAM,
            "/*"); // filterMappingUrlPattern

        context.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(filterName);
        filterMap.addURLPattern("/*");
        context.addFilterMap(filterMap);

        // TODO ホットデプロイ
        // tomcat.getHost().setAutoDeploy(true);
        // tomcat.getHost().setDeployOnStartup(true);

        try {
            tomcat.start();
            // tomcat.getServer().await(); 非同期で処理を続行させるため、この処理はコメントアウト
        } catch (LifecycleException e) {
            //if (e.getCause() != null && e.getCause() instanceof BindException) {
            //    if (e.getCause().getMessage().equals("Address already in use: bind")) {
            //        throw new PortAlreadyException(e.getCause().getMessage());
            //    }
            //}
            throw new RuntimeException(e);
        }

    }

    private final class AppInfoServlet extends HttpServlet {
        //private final Class<?> appClass;
        private static final long serialVersionUID = 1L;

        //public AppInfoServlet(Class<?> appClass) {
        //    this.appClass = appClass;
        //}

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

            try (Writer w = resp.getWriter();) {
                //w.write(this.appClass.getName());
                w.write(getAppInfo());
                w.flush();
            }
        }
    }

    private String getAppInfo() {
        String dispName = WicketBootByTomcat.this.dispName;
        if (StringUtils.isEmpty(dispName)) {
            dispName = WicketBootByTomcat.this.appClass.toString();
        }
        return dispName;
    }

    private final class AppExitServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

            // TODO 終了処理
            stopServer();
            System.exit(0);

        }
    }

    /**
     * サーバーを停止します。
     */
    public void stopServer() {
        try {
            tomcat.stop();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }

}
