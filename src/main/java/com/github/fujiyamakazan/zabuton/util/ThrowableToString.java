package com.github.fujiyamakazan.zabuton.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ThrowableToString {

    /**
     * 例外オブジェクトを文字列型に変換します。
     */
    public static String convertToString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();

    }

}
