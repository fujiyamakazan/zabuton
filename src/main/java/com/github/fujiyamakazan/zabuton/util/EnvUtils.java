package com.github.fujiyamakazan.zabuton.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.fujiyamakazan.zabuton.util.text.XmlText;

/**
 * 環境依存の情報に関するユーティリティです。
 * @author fujiyama
 */
public class EnvUtils {

    public static String project;

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

    //    public static File getAppData(String appId) {
    //        return new File(getUserProfile(), "AppData\\Roaming\\" + appId);
    //    }
    //
    //    public static File getAppData() {
    //        return getAppData("Zabuton");
    //    }

    /**
     * プロジェクト名を返します。
     * あらかじめstatic変数に登録された名称を使用します。(jarを想定)
     * しかし、取得できない場合は
     * .projectに記述されたプロジェクト名を取得します。(Eclipseからの起動を想定)
     */
    public static String getProjectName() {

        if (StringUtils.isNotEmpty(project)) {
            return project;
        }

        File projectFile = new File(".project");
        if (projectFile.exists() == false) {
            throw new RuntimeException(projectFile.getAbsolutePath() + "からプロジェクト名を取得できません。");
        }
        return new XmlText(projectFile).getTextOne("/projectDescription/name");
    }

    /**
     * AppData/Roaming/プロジェクト名 のファイルを取得します。
     */
    public static File getProjectDir() {
        String pjName = getProjectName();
        File dir = new File(getUserProfile(), "AppData\\Roaming\\" + pjName);
        if (dir.exists() == false) {
            try {
                FileUtils.forceMkdir(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dir;
    }

}
