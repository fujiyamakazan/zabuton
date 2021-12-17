package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;

public class StringBuilderLn implements Serializable {
    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringBuilderLn.class);

    private final StringBuilder sb = new StringBuilder();

    public void appendLn(String string) {
        this.sb.append(string + "\n");
        log.debug(string);
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
