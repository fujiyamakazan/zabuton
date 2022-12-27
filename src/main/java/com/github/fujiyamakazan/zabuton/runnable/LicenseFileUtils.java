package com.github.fujiyamakazan.zabuton.runnable;

import org.apache.commons.lang3.StringUtils;

import com.github.fujiyamakazan.zabuton.util.StringBuilderLn;

/**
 * ライセンスファイルのユーティリティです。
 */
public class LicenseFileUtils {

    /**
     * ライセンスの種類を判定します。
     */
    public static String getType(String licenseText) {

        StringBuilderLn sb = new StringBuilderLn(" および ");
        if (isApache20(licenseText)) {
            sb.appendLn("Apache 2.0");
        }

        if (isEpl1(licenseText)) {
            sb.appendLn("EPL 1.0");
        }

        if (isCddl1(licenseText)) {
            sb.appendLn("CDDL 1.0");
        }

        if (isMit(licenseText)) {
            sb.appendLn("MIT License");
        }

        if (isBsd3(licenseText)) {
            sb.appendLn("BSD-3");
        }

        if (isLgpl21(licenseText)) {
            sb.appendLn("LGPL 2.1");
        }

        return sb.toString();

    }

    private static boolean isApache20(String str) {

        return (StringUtils.contains(str, "http://www.apache.org/licenses/LICENSE-2.0.txt")
            || StringUtils.contains(str, "https://www.apache.org/licenses/LICENSE-2.0.txt")
            || StringUtils.contains(str, "http://www.apache.org/licenses/LICENSE-2.0")
            || StringUtils.contains(str, "https://www.apache.org/licenses/LICENSE-2.0"));

    }

    private static boolean isEpl1(String str) {
        return StringUtils.contains(str, "Eclipse Public License - v 1.0")
            || StringUtils.contains(str, "epl-v10");
    }

    private static boolean isCddl1(String str) {
        return StringUtils.contains(str, "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0");
    }

    private static boolean isMit(String str) {
        return StringUtils.contains(str, "The MIT License") || StringUtils.contains(str, "mit-license");
    }

    private static boolean isBsd3(String str) {
        return StringUtils.contains(str, "BSD-3-Clause");
    }

    private static boolean isLgpl21(String str) {
        return StringUtils.contains(str, "lgpl-2.1");
    }

}
