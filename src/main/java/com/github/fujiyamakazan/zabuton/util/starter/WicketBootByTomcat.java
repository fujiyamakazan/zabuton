package com.github.fujiyamakazan.zabuton.util.starter;

import java.io.File;
import java.net.BindException;

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
 * アプリケーションをTomcatで起動します。
 * @author fujiyama
 */
public class WicketBootByTomcat extends AbstractWebContainerStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WicketBootByTomcat.class);

    public WicketBootByTomcat(Class<?> appClass) {
        super(appClass);
    }

    public WicketBootByTomcat(Class<?> appClass, String subParams) {
        super(appClass, subParams);
    }

    @Override
    public void startServer(
        int port,
        AppInfoServlet appInfoServlet,
        String appInfoUrl,
        Class<?> appClass) throws PortAlreadyException {

        Tomcat tomcat = new Tomcat();
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
        Tomcat.addServlet(context, "app-info", appInfoServlet).addMapping(appInfoUrl);

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

            if (e.getCause() != null && e.getCause() instanceof BindException) {
                if (e.getCause().getMessage().equals("Address already in use: bind")) {
                    throw new PortAlreadyException();
                }
            }
            throw new RuntimeException(e);
        }

    }
}
