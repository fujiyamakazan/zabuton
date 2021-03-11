//package net.nanisl.zabuton.boot.spring;
//
//import javax.annotation.PreDestroy;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//import net.nanisl.zabuton.app.ZabuApp;
//
///**
// * TODO spring-boot 検証中
// * spring-bootのコンフィギュレーションクラス
// * @author fujiyama
// */
//@SpringBootApplication
//public class ZabuSpringApplication {
//	private static final Logger log = LoggerFactory.getLogger(ZabuSpringApplication.class);
//
//	public void run(Class<? extends ZabuApp> appClass) {
//		/* サーバー起動 */
//		SpringApplication.run(ZabuSpringApplication.class, appClass.getName());
//	}
//
//	@Autowired
//	private ZabuTomcatConfig tomConf;
//
//	public void retry() {
//		log.debug("ZabuSpringApplication#retry");
//		tomConf.upPort();
//	}
//    /**
//    * Bean破棄時の処理
//    */
//    @PreDestroy
//    public void cleanupBeforeExit() {
//    	log.debug("ZabuSpringApplication#cleanupBeforeExit");
//    }
//
//}
