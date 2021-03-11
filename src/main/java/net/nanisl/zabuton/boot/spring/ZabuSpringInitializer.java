//package net.nanisl.zabuton.boot.spring;
//
//import javax.servlet.FilterRegistration;
//import javax.servlet.ServletContext;
//import javax.servlet.ServletException;
//
//import org.apache.wicket.protocol.http.ContextParamWebApplicationFactory;
//import org.apache.wicket.protocol.http.WebApplication;
//import org.apache.wicket.protocol.http.WicketFilter;
//import org.apache.wicket.spring.SpringWebApplicationFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.web.servlet.ServletContextInitializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * TODO spring-boot 検証中
// * spring-bootで起動したときにTomcatにWicketAppliicationを登録する
// * @author fujiyama
// */
//@Configuration // Beanの設定をするクラス
//public class ZabuSpringInitializer implements ServletContextInitializer {
//
//	private String appClassName;
//
//	@Autowired
//	/** @param applicationArguments SpringApplication.run の第２引数 */
//	public ZabuSpringInitializer(ApplicationArguments applicationArguments) {
//		this.appClassName = applicationArguments.getSourceArgs()[0];
//	}
//
//    /**
//	 * SpringBeanにアプリケーションを登録する
//	 */
//	@Bean
//    public WebApplication webApplicationBean() {
//        try {
//			return (WebApplication)Class.forName(this.appClassName).getDeclaredConstructor().newInstance();
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//    }
//
//	@Override
//	public void onStartup(ServletContext context) throws ServletException {
//		FilterRegistration filter = context.addFilter("wicket-filter", WicketFilter.class);
//		filter.setInitParameter(WicketFilter.APP_FACT_PARAM, SpringWebApplicationFactory.class.getName());
//		filter.setInitParameter(ContextParamWebApplicationFactory.APP_CLASS_PARAM, this.appClassName);
//		filter.setInitParameter(WicketFilter.FILTER_MAPPING_PARAM, "/*");
//		filter.addMappingForUrlPatterns(null, false, "/*");
//	}
//
//}
