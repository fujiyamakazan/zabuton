package com.github.fujiyamakazan.zabuton.selen.driverfactory;

import java.io.File;
import java.io.Serializable;

import org.openqa.selenium.WebDriver;

/**
 * Webドライバのファクトリです。
 */
public abstract class DriverFactory implements Serializable {
    private static final long serialVersionUID = 1L;

    public File driverFile;

    public DriverFactory(File driverDir) {
        this.driverFile = new File(driverDir, getDriverFileName());
    }

    public File getDriverFile() {
        return driverFile;
    }

    /**
     * ドライバの実行ファイルの名称を返します。
     */
    public abstract String getDriverFileName();

    /**
     * ドライバの実行ファイルを取得できるURLを返します。
     */
    public abstract String getDriverUrl();

    /**
     * ドライバの実行ファイルを取得します。
     */
    public abstract void download();

    /**
     * ドライバの実行ファイルからWebドライバオブジェクトを生成します。
     */
    public abstract WebDriver create(File downloadDefaultDir);

    /**
     * 例外情報から不正なバージョンの発生を検知します。
     */
    public abstract boolean occurredIllegalVersionDetected(Exception e);

}