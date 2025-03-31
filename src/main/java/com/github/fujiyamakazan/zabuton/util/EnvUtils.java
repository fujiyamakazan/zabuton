package com.github.fujiyamakazan.zabuton.util;

import java.io.File;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 環境依存の情報に関するユーティリティです。
 * @author fujiyama
 */
public class EnvUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvUtils.class);

    public static String getUserName() {
        return System.getenv("USERNAME");
    }

    public static File getUserProfile() {
        return new File(System.getenv("USERPROFILE"));
    }

    public static File getUserAppData() {
        return new File(System.getenv("APPDATA")); // C:\Users\xxxxx\AppData\Roaming
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
        return new File(getUserProfile(), "Downloads");
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

    public static File getJavahome() {
        return new File(System.getProperty("java.home"));
    }

    /**
     * カレントディレクトリ（実行時のパス）を取得します。
     * 例：workspace\zabuton
     */
    public static File getCurrentDir() {
        return new File(System.getProperty("user.dir"));
    }

    public static void main(String[] args) {
        LOGGER.debug(getCurrentDir().getAbsolutePath());
    }
}

