package net.nanisl.zabuton.myapp;

import org.apache.wicket.Page;

import net.nanisl.zabuton.app.ZabuApp;

public class MyApp extends ZabuApp {



	public Class<? extends Page> getHomePage() {
		return MyPage.class;
	}
}
