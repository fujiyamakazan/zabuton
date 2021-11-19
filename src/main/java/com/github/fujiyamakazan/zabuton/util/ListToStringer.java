package com.github.fujiyamakazan.zabuton.util;

import java.util.Arrays;
import java.util.List;


/**
 * ListをStringに変換する
 * @author fujiyama
 */
public class ListToStringer {

    public static String convert(List<?> list) {
        //return convert(list, "\n");
        return convert(list, ","); // 2021.9.12
    }

    /**
     * リストをdelimiterを区切り文字として文字列に変換します。
     */
    public static String convert(List<?> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (Object e :list) {
            if (sb.length() > 0) {
                sb.append(delimiter);
            }
            if (e == null) {
                sb.append("[null]");
            } else {
                sb.append(e.toString());
            }
        }
        return sb.toString();
    }

    public static String convert(String[] strings) {
        return convert(Arrays.asList(strings));
    }

}


