package com.github.fujiyamakazan.zabuton.util;

import java.io.File;

public class EnvUtils {

    public static String getUserName() {
        return System.getenv("USERNAME");
    }

    public static File getUserProfile() {
        return new File(System.getenv("USERPROFILE"));
    }


    public static File getUserDesktop() {
        return new File(getUserProfile(), "Desktop");
    }

    public static File getUserDesktop(String path) {
        return new File(getUserDesktop(), path);
    }

    public static File getUserDocuments() {
        return new File(getUserProfile(), "Documents");
    }

    public static File getUserDownload() {
        return new File(getUserProfile(),"Downloads");
    }

    public static File getUserMusic() {
        return new File(getUserProfile(), "Music");
    }

    public static File getUserPicture() {
        return new File(getUserProfile(), "Pictures");
    }

    public static File getUserLocalTemp() {
        return new File(getUserProfile(), "AppData\\Local\\Temp");
    }

    public static File getAppData(String appId) {
        return new File(getUserProfile(), "AppData\\Roaming\\" + appId);
    }


}
