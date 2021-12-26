package com.github.fujiyamakazan.zabuton.util.string;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegexUtils.class);

    /**
     * 対象文字列から正規表現で文字列を抽出します。（複数）
     * @param target 対象文字列
     * @param pattern 正規表現
     * @return 一致した部分の文字列
     */
    public static List<String> pickup(String target, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(target);
        List<String> list = new ArrayList<String>();
        while (m.find()) {
            list.add(m.group(1));
        }
        return list;
    }

    /**
     * 対象文字列から正規表現で文字列を抽出します。（１件のみ）
     * @param target 対象文字列
     * @param pattern 正規表現
     * @return 一致した部分の文字列
     */
    public static String pickupOne(String target, String pattern) {
        List<String> list = pickup(target, pattern);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

}
