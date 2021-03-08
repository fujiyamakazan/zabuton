package net.nanisl.zabuton.app;

import org.apache.wicket.Page;

public class MyApp extends ZabuApp {

	public Class<? extends Page> getHomePage() {
		return MyPage.class;
	}
}
