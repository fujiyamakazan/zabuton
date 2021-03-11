package net.nanisl.zabuton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.nanisl.zabuton.app.ZabuApp;
import net.nanisl.zabuton.boot.ZabuBootByTomcat;

/**
 * zabutonを利用してアプリケーションを起動する
 * @author fujiyama
 */
public class Zabuton {

	 @SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Zabuton.class);

	 public static final String PARAM_TITLE = "net.nanisl.zabuton.Zabuton.PARAM_TITLE";

    /**
     * 起動
     * @param appClass アプリケーションクラス
     * @param subParams
     * @param appTitle アプリケーションの表示名
     */
	public static void start(Class<? extends ZabuApp> appClass, String appTtitle, String subParams) {
		new ZabuBootByTomcat().invoke(appClass, appTtitle, subParams);
	}

}
