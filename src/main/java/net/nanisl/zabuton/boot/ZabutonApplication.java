package net.nanisl.zabuton.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.nanisl.zabuton.app.ZabuApp;

/**
 * spring-bootのコンフィギュレーションクラス
 *
 * TODO コントロールパネルの設置
 * TODO 可変ポート対応 https://dawaan.com/spring-boot-multiple-ports/
 *
 * @author fujiyama
 */
@SpringBootApplication
public class ZabutonApplication {
	public static void run(Class<? extends ZabuApp> appClass) {
		SpringApplication.run(ZabutonApplication.class, appClass.getName());
	}

}
