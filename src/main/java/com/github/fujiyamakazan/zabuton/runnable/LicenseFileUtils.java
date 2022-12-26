package com.github.fujiyamakazan.zabuton.runnable;

import org.apache.commons.lang3.StringUtils;

import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

/**
 * ライセンスファイルのユーティリティです。
 * TODO 不要になった処理の除去
 * @author fujiyama
 */
public class LicenseFileUtils {

    public static boolean isLicenseFilename(String fileName) {
        return StringUtils.startsWithIgnoreCase(fileName, "LICENSE");
    }

    /**
     * licenseの判定をします。
     */
    public static boolean isApache2(Utf8Text file) {

        String text = file.read();
        if (text.contains("http://www.apache.org/licenses/LICENSE-2.0.txt")
            || text.contains("https://www.apache.org/licenses/LICENSE-2.0.txt")
            || text.contains("http://www.apache.org/licenses/LICENSE-2.0")
            || text.contains("https://www.apache.org/licenses/LICENSE-2.0")) {
            return true;
        }

        boolean nextVerCheck = false;
        for (String line : file.readLines()) {
            line = line.trim();
            if (StringUtils.equals(line, "Apache License")) {
                nextVerCheck = true;
                continue;
            }
            if (nextVerCheck) {
                if (StringUtils.startsWith(line, "Version 2.0,")) {
                    return true;
                }
                nextVerCheck = false;
            }
        }
        return false;
    }

    /**
     * licenseの判定をします。
     */
    public static boolean isEpl1(Utf8Text file) {
        for (String line : file.readLines()) {
            if (StringUtils.equals(line, "Eclipse Public License - v 1.0")) {
                return true;
            }
        }
        return false;
    }

    /**
     * licenseの判定をします。
     */
    public static boolean isCddl1(Utf8Text file) {
        for (String line : file.readLines()) {
            if (StringUtils.equals(line, "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0")) {
                return true;
            }
        }
        return false;
    }

    /**
     * licenseの判定をします。
     */
    public static boolean isMit(Utf8Text file) {
        return file.read().contains("The MIT License");
    }

    /**
     * licenseの判定をします。
     */
    public static boolean isBsd3(Utf8Text file) {
        return file.read().contains("BSD-3-Clause");
    }
}
