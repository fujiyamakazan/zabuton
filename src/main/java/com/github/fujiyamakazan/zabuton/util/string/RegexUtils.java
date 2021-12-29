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
     * 正規表現でパターンが一致するかを検査します。
     * @param value 対象文字列
     * @param pattern パターン
     * @return 一致すればTrue
     */
    public static boolean find(String value, String pattern) {
        Pattern p = Pattern.compile(pattern);

        Matcher m = p.matcher(value);
        boolean result;
        if (m.find()) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * 対象文字列から正規表現で文字列を抽出します。（複数）
     * @param value 対象文字列
     * @param pattern 正規表現
     * @return 一致した部分の文字列
     */
    public static List<String> pickup(String value, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(value);
        List<String> list = new ArrayList<String>();
        while (m.find()) {
            list.add(m.group(1));
        }
        return list;
    }

    /**
     * 対象文字列から正規表現で文字列を抽出します。（１件のみ）
     * @param value 対象文字列
     * @param pattern 正規表現
     * @return 一致した部分の文字列
     */
    public static String pickupOne(String value, String pattern) {
        List<String> list = pickup(value, pattern);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 文字列を左から検査し、半角でない文字が見つかれば、その最初の位置を返します。
     * 全て半角文字の場合は-1を返します。
     *
     * @param value 対象文字列
     * @return int 位置
     */
    public static int indexOfNotHankaku(String value) {
        Pattern pattern = Pattern.compile(".*[^ -~｡-ﾟ].*");

        for (int i = 0; i < value.length(); i++) {
            String c = String.valueOf(value.charAt(i));
            Matcher matcher = pattern.matcher(c);
            if (matcher.matches() == true) {
                return i; // 全角
            } else {
                continue; // 半角→次の文字へ
            }
        }
        return -1;
    }

    /**
     * 半角のアルファベットだけで構築されているかを検査します。
     * @param value 対象文字列
     * @return 検査結果
     */
    public static Boolean onlyHalfAlphabet(String value) {
        Pattern pattern = Pattern.compile("^[a-zA-Z]+$");
        Matcher matcher = pattern.matcher(value);
        if (matcher.matches() == true) {
            return true;
        }
        return false;
    }



    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        System.out.println(pickup("babcd", "a(.+)c")); // b
        System.out.println(indexOfNotHankaku("0123全456")); // 4
        System.out.println(onlyHalfAlphabet("ABCＡＢＣ")); // false

    }

}
