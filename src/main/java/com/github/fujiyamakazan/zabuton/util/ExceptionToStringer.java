package com.github.fujiyamakazan.zabuton.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionToStringer {

    public static String convetToString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();

    }

}
