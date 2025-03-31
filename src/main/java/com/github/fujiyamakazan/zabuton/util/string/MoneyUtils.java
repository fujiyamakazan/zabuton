package com.github.fujiyamakazan.zabuton.util.string;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * 日本円の文字列表現に関するユーティリティです。
 *
 * @author fujiyama
 */
public class MoneyUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MoneyUtils.class);

    /**
     * 金額を示すテキストを整数に変換します。
     */
    public static int toInt(String text) {
        if (text == null) {
            text = "";
        }
        text = text.trim();
        text = text.replaceAll(Pattern.quote("\\"), "");
        text = text.replaceAll("￥", "");
        text = text.replaceAll("¥", ""); // 文字コードが特殊な記号
        text = text.replaceAll("円", "");
        text = text.replaceAll(",", "");
        text = text.replaceAll(" ", "");
        return Integer.parseInt(text);
    }

    public static String toString(String text) {
        return String.valueOf(toInt(text));
    }
}
