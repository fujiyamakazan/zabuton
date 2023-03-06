package com.github.fujiyamakazan.zabuton.util.string;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.fujiyamakazan.zabuton.util.ListToStringer;

/**
 * 正規表現による処理のユーティリティです。
 * @author fujiyama
 */
public class RegexUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RegexUtils.class);

    /**
     * 正規表現でパターンが一致するかを検査します。
     * @param value 対象文字列
     * @param pattern パターン
     * @return 一致すればTrue
     */
    public static boolean find(String value, String pattern) {
        Matcher m = Pattern.compile(pattern).matcher(value);
        return m.find();
    }


    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        LOGGER.debug("郵便番号かどうかを判定：" + find("〒 238-0000神奈川県～", "〒\\s?[0-9]{3}-?[0-9]{4}"));

        LOGGER.debug(ListToStringer.convert(pickup("babcd", "a(.+)c"))); // b
        LOGGER.debug("" + indexOfNotHankaku("0123全456")); // 4
        LOGGER.debug("" + onlyHalfAlphabet("ABCＡＢＣ")); // false

    }

    /**
     * 正規表現による文字列検査・抽出。
     * パターンに一致すれば要素が1件以上のリストを返却し、
     * パターンに一致しなけれnullを返却する。
     * グループとして複数件取得していれば個別のリスト用として構成される。
     * @param str 対象文字列
     * @param pattern 正規表現のパターン
     * @return 取得結果
     */
    public static List<String> findPattern(String str, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("パターンが指定されていません。");
        }

        if (str == null || str.isEmpty()) {
            return null;
        }

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);
        List<String> list = new ArrayList<String>();
        if (m.find()) {
            if (m.groupCount() == 0) {
                list.add(m.group());
            } else {
                for (int i = 0; i < m.groupCount(); i++) {
                    list.add(m.group(i));
                }
            }
        }
        return list;
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
     * 正規表現による文字列検査・抽出。
     * パターンに一致しなければnullを返却する。
     * グループとして複数件取得していればその１件目を返却する。
     * @param str 対象文字列
     * @param pattern 正規表現のパターン
     * @return 取得結果
     */
    public static String findPatternFirst(String str, String pattern) {
        List<String> list = findPattern(str, pattern);
        if (list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }




}
