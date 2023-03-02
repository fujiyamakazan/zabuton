package com.github.fujiyamakazan.zabuton;

import java.io.File;

import com.github.fujiyamakazan.zabuton.app.MyApp;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.starter.WebContainerInvokeFrame;
import com.github.fujiyamakazan.zabuton.util.starter.WicketBootByTomcat;

public class Zabuton {

    public static final String NAME = "zabuton";
    public static final String NAME_DISP = "ZABUTON";

    public static File getDir() {
        return new File(EnvUtils.getUserAppData(), NAME);
    }

    public static void callStarter() {
        WebContainerInvokeFrame.show(NAME_DISP, new WicketBootByTomcat(MyApp.class).dispName(NAME_DISP));
    }

    public static void main(String[] args) {
        Zabuton.callStarter();
    }

}
