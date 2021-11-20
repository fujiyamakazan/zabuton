package com.github.fujiyamakazan.zabuton.util;

import java.io.File;

public class EnvUtils {
    public static File getUserDocuments() {
        return new File(System.getenv("USERPROFILE") + "\\Documents");
    }

    public static File getUserDownload() {
        return new File(System.getenv("USERPROFILE") + "\\Downloads");
    }

    public static File getUserMusic() {
        return new File(System.getenv("USERPROFILE") + "\\Music");
    }

    public static File getUserPicture() {
        return new File(System.getenv("USERPROFILE") + "\\Pictures");
    }

    public static File getUserLocalTemp() {
        return new File(System.getenv("USERPROFILE") + "\\AppData\\Local\\Temp");
    }

    public static File getAppData(String appId) {
        return new File(System.getenv("USERPROFILE") + "\\AppData\\Roaming\\" + appId);
    }
}
