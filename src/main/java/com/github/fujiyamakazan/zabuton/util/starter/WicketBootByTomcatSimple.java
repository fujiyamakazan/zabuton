package com.github.fujiyamakazan.zabuton.util.starter;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.wicket.protocol.http.ContextParamWebApplicationFactory;
import org.apache.wicket.protocol.http.WicketFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * アプリケーションを組込みTomcatで起動します。
 * TODO 実装調整中
 */
public class WicketBootByTomcatSimple {
    private static final Logger LOGGER = LoggerFactory.getLogger(WicketBootByTomcatSimple.class);

    private final Class<?> appClass;
    private final int port;
    private boolean running = false;
    private Tomcat tomcat;

    public WicketBootByTomcatSimple(Class<?> appClass, int port) {
        this.appClass = appClass;
        this.port = port;
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * Tomcatを開始します。
     */
    public void start() {

        try (ServerSocket ss = new ServerSocket(port)) {
            // 処理なし
        } catch (IOException e) {
            throw new RuntimeException("ポート[" + port + "]は使用されています。");
        }

        /* サーバーを起動する */
        tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        File base = new File("webapp");
        if (base.exists() == false) {
            base.mkdirs();
            LOGGER.info("makedirs:" + base.getAbsolutePath());
        }
        Context context = tomcat.addContext("", base.getAbsolutePath());

        Tomcat.addServlet(context, "default", new DefaultServlet()).addMapping("/"); // 静的ファイルを扱う

        /*
         * web.xmlの代わりにFilterなどの設定をします。
         */
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

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }

        this.running = true;
        return;

    }

    /**
    * ブラウザで開くURLを返す。
    */
    public String getUrl() {
        return "http://localhost:" + port + "/";
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
