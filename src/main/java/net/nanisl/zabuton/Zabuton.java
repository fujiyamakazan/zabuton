package net.nanisl.zabuton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.nanisl.zabuton.app.ZabuApp;
import net.nanisl.zabuton.boot.ZabutonApplication;

/**
 * @author fujiyama
 */
public class Zabuton {

	 private static final Logger log = LoggerFactory.getLogger(Zabuton.class);

	 public static final String PARAM_TITLE = "net.nanisl.zabuton.Zabuton.PARAM_TITLE";

    /**
     * WicketApplicationをspring-bootで起動する
     * @param appClass アプリケーションクラス
     * @param appTitle アプリケーションの表示名
     */
	public static void start(Class<? extends ZabuApp> appClass) {
		ZabutonApplication.run(appClass);
	}

}
