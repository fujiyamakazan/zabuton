package com.github.fujiyamakazan.zabuton.util.string;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * String型のユーティリティです。
 *
 * [String]-[U]ti[l]
 *
 * @author fujiyama
 */
public class Stringul implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Stringul.class);

    /**
     * 例外オブジェクトを文字列型に変換します。
     */
    public static String ofThrowable(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    /**
     * 例外オブジェクトを文字列型に変換します。
     * (ThrowableよりExceptionの方がなじみがあるので、このメソッド名も準備しました。)
     */
    public static String ofException(Exception e) {
        return ofThrowable(e);
    }

    public static void main(String[] args) {
        LOGGER.debug(ofException(new RuntimeException("テスト")));
    }
}
