package com.github.fujiyamakazan.zabuton.util;

public class StringSeparator {

    /**
     * 文字列をdelimiterで分割します。
     * @param string 文字列
     */
    public static KeyValue sparate(String string, char delimiter) {
        int index = string.indexOf(delimiter);
        final KeyValue kv;
        if (index != -1) {
            kv = new KeyValue(string.substring(0, index), string.substring(index + 1));
        } else {
            kv = new KeyValue(string, "");
        }
        return kv;
    }
}
