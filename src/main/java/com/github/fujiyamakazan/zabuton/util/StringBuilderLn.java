package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;

public class StringBuilderLn implements Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringBuilderLn.class);

    private final StringBuilder sb = new StringBuilder();

    /**
     * 文字列を改行コードを付けて追加します。
     * @param str 追加する文字列。
     */
    public void appendLn(String str) {
        if (this.sb.length() > 0) {
            this.sb.append("\n");
        }
        this.sb.append(str);
        //log.debug(string);
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
