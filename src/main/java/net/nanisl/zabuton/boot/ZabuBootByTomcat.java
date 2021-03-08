package net.nanisl.zabuton.boot;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.wicket.protocol.http.ContextParamWebApplicationFactory;
import org.apache.wicket.protocol.http.WicketFilter;

import net.nanisl.zabuton.Zabuton;

public class ZabuBootByTomcat extends AbstractZabuBoot {

//	public static void main(String[] args) throws Exception {
//		new ZabuBootByTomcat().startServletContainer(8180, "com.example.music_copy.MusicCopy", "TEST");
//	}


	public void startServletContainer(int port, String appClassName, String appTitle) throws Exception {

	    Tomcat tomcat = new Tomcat();
	    tomcat.setPort(port);
	    tomcat.getConnector(); // portを設定するため

	    //File base = new File("src/main/webapp/");
	    File base = new File(".");
        Context context = tomcat.addContext("/", base.getAbsolutePath());

        Tomcat.addServlet(context, "default", new DefaultServlet()).addMapping("/"); // 静的ファイルを扱う
//        Tomcat.addServlet(context, "hello", new HttpServlet() {
//			private static final long serialVersionUID = 1L;
//			protected void service(HttpServletRequest req, HttpServletResponse resp)
//                    throws ServletException, IOException {
//                Writer w = resp.getWriter();
//                w.write("Hello, World!");
//                w.flush();
//            }
//        }).addMapping("/hello");

	    /*
		  <filter>
		    <filter-name>wickletFilter</filter-name>
		    <filter-class>org.apache.wicket.protocol.http.WicketFilter</filter-class>
		    <init-param>
		      <param-name>applicationClassName</param-name>
		      <param-value>com.example.SampleApp</param-value>
		      <param-name>net.nanisl.zabuton.Zabuton.PARAM_TITLE</param-name>
		      <param-value>APP_TILTE</param-value>
		    </init-param>
		  </filter>

		  <filter-mapping>
		    <filter-name>wickletFilter</filter-name>
		    <url-pattern>/*</url-pattern>
		  </filter-mapping>
	     */

	    FilterDef filterDef = new FilterDef();
	    final String filterName = "wickletFilter";
		filterDef.setFilterName(filterName);
	    filterDef.setFilterClass(WicketFilter.class.getName());
	    filterDef.addInitParameter(ContextParamWebApplicationFactory.APP_CLASS_PARAM, appClassName); // "applicationClassName"
	    filterDef.addInitParameter(Zabuton.PARAM_TITLE, appTitle);
	    filterDef.addInitParameter(WicketFilter.FILTER_MAPPING_PARAM, "/*"); // filterMappingUrlPattern
		context.addFilterDef(filterDef);

		FilterMap filterMap = new FilterMap();
		filterMap.setFilterName(filterName);
		filterMap.addURLPattern("/*");
		context.addFilterMap(filterMap);

	    tomcat.start();
//	    tomcat.getServer().await(); 非同期で処理を続行させるため、この処理はコメントアウト

	}

}
