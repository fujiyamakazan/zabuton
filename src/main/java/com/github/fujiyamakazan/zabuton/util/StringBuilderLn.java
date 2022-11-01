package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;

public class StringBuilderLn implements Serializable {
    private static final long serialVersionUID = 1L;

    private final StringBuilder sb = new StringBuilder();

    private final String delimiter;

    public StringBuilderLn() {
        this.delimiter = "\n";
    }

    public StringBuilderLn(String delimiter) {
        this.delimiter = delimiter;
    }


    /**
     * 文字列を区切り文字を付けて追加します。
     * @param str 追加する文字列。
     */
    public void appendLn(String str) {
        if (this.sb.length() > 0) {
            this.sb.append(this.delimiter);
        }
        this.sb.append(str);
        //log.debug(string);
    }

    @Override
    public String toString() {
        return this.sb.toString();
    }
}
