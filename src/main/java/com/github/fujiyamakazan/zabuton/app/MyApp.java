package com.github.fujiyamakazan.zabuton.app;

import org.apache.wicket.Page;

public class MyApp extends ZabuApp {

    @Override
    public Class<? extends Page> getHomePage() {
        return MyPage.class;
    }
}
