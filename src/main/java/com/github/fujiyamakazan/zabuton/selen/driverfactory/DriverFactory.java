package com.github.fujiyamakazan.zabuton.selen.driverfactory;

import java.io.File;
import java.io.Serializable;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;

/**
 * Webドライバのファクトリです。
 */
public abstract class DriverFactory implements Serializable {
    private static final long serialVersionUID = 1L;

    protected File driverFile;
    protected File downloadDir;

    public DriverFactory(File driverFile) {
        this.driverFile = driverFile;
    }

    public final DriverFactory downloadDir(File downloadDir) {
        this.downloadDir = downloadDir;
        return this;
    }

    public abstract SelenCommonDriver build();

//    public DriverFactory(File driverDir) {
//        this.driverFile = new File(driverDir, getDriverFileName());
//    }
//
//    public File getDriverFile() {
//        return driverFile;
//    }

//    /**
//     * ドライバの実行ファイルの名称を返します。
//     */
//    public abstract String getDriverFileName();
//
//    /**
//     * ドライバの実行ファイルからWebドライバオブジェクトを生成します。
//     */
//    public abstract WebDriver create(File downloadDefaultDir);
//
//    /**
//     * 例外情報から不正なバージョンの発生を検知します。
//     */
//    public abstract boolean occurredIllegalVersionDetected(Exception e);

}