package com.github.fujiyamakazan.zabuton;

import java.io.File;

import org.apache.wicket.Page;

import com.github.fujiyamakazan.zabuton.app.page.HomePage;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.starter.WebContainerInvokeFrame;
import com.github.fujiyamakazan.zabuton.util.starter.WicketBootByTomcat;
import com.github.fujiyamakazan.zabuton.wicket.ZabuApp;

public class Zabuton extends ZabuApp {

    public static final String NAME = "zabuton";
    public static final String NAME_DISP = "ZABUTON";

    public static File getDir() {
        return new File(EnvUtils.getUserAppData(), NAME);
    }

    public static void callStarter() {
        WebContainerInvokeFrame.show(NAME_DISP, new WicketBootByTomcat(Zabuton.class).dispName(NAME_DISP));
    }

    public static void main(String[] args) {
        Zabuton.callStarter();
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

}
