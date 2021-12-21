package com.github.fujiyamakazan.zabuton.util.string;

import java.io.Serializable;

public class MoneyUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MoneyUtils.class);

    public static int toInt(String text) {
        text = text.replaceAll("円", "");
        text = text.replaceAll(",", "");
        text = text.replaceAll(" ", "");
        return Integer.parseInt(text);
    }
}
