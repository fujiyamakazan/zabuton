package com.github.fujiyamakazan.zabuton.util;

import java.util.Arrays;
import java.util.List;


/**
 * ListをStringに変換します。
 *
 * @author fujiyama
 */
public class ListToStringer {

    /**
     * リストをカンマ区切りで文字列にします。
     * // TODO Stringulへの集約
     */
    public static String convert(List<?> list) {
        //return convert(list, "\n");
        return convert(list, ","); // 2021.9.12
    }

    /**
     * 配列をカンマ区切りで文字列にします。
     * // TODO Stringulへの集約
     */
    public static String convert(String[] strings) {
        return convert(Arrays.asList(strings));
    }

    /**
     * リストをdelimiterを区切り文字として文字列に変換します。
     */
    public static String convert(List<?> list, String delimiter) {
        if (list == null) {
            return null;
        }
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



}


