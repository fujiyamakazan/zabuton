package net.nanisl.zabuton.util;

import java.util.List;


/**
 * ListをStringに変換する
 * @author fujiyama
 */
public class ListToStringer {

    public static String convert(List<?> list) {
        return convert(list, "\n");
    }
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

}
