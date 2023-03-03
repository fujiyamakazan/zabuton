package com.github.fujiyamakazan.zabuton.util;

import com.github.fujiyamakazan.zabuton.util.string.Stringul;

public class ThrowableToString {

    /**
     * 例外オブジェクトを文字列型に変換します。
     * @deprecated {@link Stringul#ofThrowable(Throwable)}
     */
    public static String convertToString(Throwable t) {
        //StringWriter sw = new StringWriter();
        //PrintWriter pw = new PrintWriter(sw);
        //t.printStackTrace(pw);
        //pw.flush();
        //return sw.toString();
        return Stringul.ofThrowable(t);
    }

}
