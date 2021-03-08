package net.nanisl.zabuton.boot.spring;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * TODO spring-boot ポートを切替えて起動する方法を検証中
 * @author fujiyama
 */
@Configuration // Beanの設定をするクラス
public class ZabuTomcatConfig implements WebMvcConfigurer {
	private static final Logger log = LoggerFactory.getLogger(ZabuTomcatConfig.class);

	private int port = 8178;
    public void upPort() {
		this.port = port + 1;
	}
	/**
     * Bean登録
     * @return Tomcatのportを変更するBean
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainer() {
//    	return factory -> {factory.setPort(8178);};
    	WebServerFactoryCustomizer<TomcatServletWebServerFactory> factoryCustomizer
    		= new WebServerFactoryCustomizer<TomcatServletWebServerFactory>() {
			@Override
			public void customize(TomcatServletWebServerFactory factory) {
				factory.setPort(port);
			}
    	};
    	return factoryCustomizer;
    }
    /**
    * Bean破棄時の処理
    */
    @PreDestroy
    public void cleanupBeforeExit() {
    	log.debug("TomcatConfig#cleanupBeforeExit");
    }
}
