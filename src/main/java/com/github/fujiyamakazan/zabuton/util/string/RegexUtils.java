package com.github.fujiyamakazan.zabuton.util.string;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * 正規表現による処理のユーティリティです。
 * @author fujiyama
 */
public class RegexUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RegexUtils.class);

    /**
     * valueを正規表現（regex）で調べ、ヒットしたらリストにして返します。
     * @param groupIndex グループ指定。0は全体。
     * @param flags オプション
     */
    private static List<String> matcher(String value, String regex, int groupIndex, int flags) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        if (StringUtils.isEmpty(regex)) {
            throw new IllegalArgumentException("正規表現が指定されていません。");
        }
        Matcher m = Pattern.compile(regex, flags).matcher(value);

        List<String> list = new ArrayList<String>();
        while (m.find()) {
            list.add(m.group(groupIndex));
        }
        return list;
    }

    /**
     * valueを正規表現（regex）で調べ、ヒットするかを判定します。
     */
    public static boolean find(String value, String regex) {
        List<String> list = matcher(value, regex, 0, 0); // グループは全体(0)を指定
        return list != null && list.isEmpty() == false;
    }

    /**
     * valueから正規表現で示されたグループを取得します。
     * 正規表現が複数個所でヒットすれば、その全てを返します。
     * １つもヒットしなければ空のリストを返します。
     *
     * ※ 郵便番号上位3桁をグループとした例 [〒\\s?([0-9]{3})-?[0-9]{4}]
     * ※ このメソッドでは第１グループのみを対象とします。
     */
    public static List<String> pickup(String value, String regex) {
        return pickup(value, regex, 0);
    }

    /**
     * valueから正規表現で示されたグループを取得します。
     * 正規表現が複数個所でヒットすれば、その全てを返します。
     * １つもヒットしなければ空のリストを返します。
     *
     * ※ 郵便番号上位3桁をグループとした例 [〒\\s?([0-9]{3})-?[0-9]{4}]
     * ※ このメソッドでは第１グループのみを対象とします。
     *
     * @param flags オプション
     */
    public static List<String> pickup(String value, String regex, int flags) {
        List<String> list = matcher(value, regex, 1, flags); // 第1グループを指定
        if (list == null) {
            list = new ArrayList<String>();
        }
        return list;
    }

    /**
     * valueから正規表現で示されたグループを取得します。
     * 正規表現が複数個所でヒットすれば、最初だけを返します。
     * １つもヒットしなければnullを返します。
     *
     * ※ 郵便番号上位3桁をグループとした例 [〒\\s?([0-9]{3})-?[0-9]{4}]
     * ※ このメソッドでは第１グループのみを対象とします。
     */
    public static String pickupOne(String value, String regex) {
        return pickupOne(value, regex, 0);
    }

    /**
     * valueから正規表現で示されたグループを取得します。
     * 正規表現が複数個所でヒットすれば、最初だけを返します。
     * １つもヒットしなければnullを返します。
     *
     * ※ 郵便番号上位3桁をグループとした例 [〒\\s?([0-9]{3})-?[0-9]{4}]
     * ※ このメソッドでは第１グループのみを対象とします。
     *
     * @param flags オプション
     */
    public static String pickupOne(String value, String regex, int flags) {
        List<String> list = pickup(value, regex, flags);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 正規表現に対応した置換をします。改行も含みます。
     * @param str 元の文字列
     * @param start 開始する文字列
     * @param end 終了する文字列
     * @param replacement 差し替える文字列
     * @param flags オプション
     * @return 差し替え後の文字列。
     */
    public static String replaceAll(String str, String start, String end, String replacement, int flags) {
        Pattern p = Pattern.compile("(" + start + ")[\\s\\S]*(" + end + ")", flags);
        Matcher m = p.matcher(str);
        return m.replaceAll(replacement);
    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {
        //LOGGER.debug("郵便番号かどうかを判定１：" + find("〒 220-0000神奈川県～", "〒\\s?[0-9]{3}-?[0-9]{4}"));
        //LOGGER.debug("郵便番号を取得１：" + pickup("〒 110-0000東京都～ 〒 220-0000神奈川県～", "〒\\s?[0-9]{3}-?[0-9]{4}"));
        //LOGGER.debug("郵便番号を取得２：" + pickup("090-0000-0000", "〒\\s?[0-9]{3}-?[0-9]{4}"));
        //LOGGER.debug("郵便番号を取得１：" + pickupOne("〒 110-0000東京都～ 〒 220-0000神奈川県～", "〒\\s?([0-9]{3})-?([0-9]{4})"));
        //LOGGER.debug("郵便番号を取得２：" + findPattern("〒 238-0000神奈川県～", "〒\\s?[0-9]{3}-?[0-9]{4}"));

        //LOGGER.debug(ListToStringer.convert(pickup("babcd", "a(.+)c"))); // b
        //LOGGER.debug("" + indexOfNotHankaku("0123全456")); // 4
        //LOGGER.debug("" + onlyHalfAlphabet("ABCＡＢＣ")); // false

        //LOGGER.debug(indexOfNotHankaku("aaaｱあああabc") + "");

    }



    //    /**
    //     * 正規表現による文字列検査・抽出。
    //     * パターンに一致すれば要素が1件以上のリストを返却し、
    //     * パターンに一致しなけれnullを返却する。
    //     * グループとして複数件取得していれば個別のリスト用として構成される。
    //     * @param value 対象文字列
    //     * @param regex 正規表現のパターン
    //     * @return 取得結果
    //     */
    //    public static List<String> findPattern(String value, String regex) {
    //        if (StringUtils.isEmpty(regex)) {
    //            throw new IllegalArgumentException("正規表現が指定されていません。");
    //        }
    //
    //        if (StringUtils.isEmpty(value)) {
    //            return null;
    //        }
    //
    //        Pattern p = Pattern.compile(regex);
    //        Matcher m = p.matcher(value);
    //        List<String> list = new ArrayList<String>();
    //        if (m.find()) {
    //            if (m.groupCount() == 0) {
    //                list.add(m.group());
    //            } else {
    //                for (int i = 0; i < m.groupCount(); i++) {
    //                    list.add(m.group(i));
    //                }
    //            }
    //        }
    //        return list;
    //    }
    //
    //    /**
    //     * 正規表現による文字列検査・抽出。
    //     * パターンに一致しなければnullを返却する。
    //     * グループとして複数件取得していればその１件目を返却する。
    //     * @param str 対象文字列
    //     * @param pattern 正規表現のパターン
    //     * @return 取得結果
    //     */
    //    public static String findPatternFirst(String str, String pattern) {
    //        List<String> list = findPattern(str, pattern);
    //        if (list.size() == 0) {
    //            return null;
    //        } else {
    //            return list.get(0);
    //        }
    //    }

    // 以下はStringSetの定数を使う方が良い。必要に応じてStringInspectionで実装する。

    //    /**
    //     * 文字列を左から検査し、半角でない文字が見つかれば、その最初の位置を返します。
    //     * 全て半角文字の場合は-1を返します。
    //     * @param value 対象文字列
    //     * @return int 位置
    //     */
    //    public static int indexOfNotHankaku(String value) {
    //        Pattern pattern = Pattern.compile(".*[^ -~｡-ﾟ].*"); // [ ]はASCIIの始端、[~]はASCIIの終端
    //
    //        for (int i = 0; i < value.length(); i++) {
    //            String c = String.valueOf(value.charAt(i));
    //            Matcher matcher = pattern.matcher(c);
    //            if (matcher.matches() == true) {
    //                return i; // 全角
    //            } else {
    //                continue; // 半角→次の文字へ
    //            }
    //        }
    //        return -1;
    //    }
    ///**
    // * 半角のアルファベットだけで構築されているかを検査します。
    // * @param value 対象文字列
    // * @return 検査結果
    // */
    //public static Boolean onlyHalfAlphabet(String value) {
    //    Pattern pattern = Pattern.compile("^[a-zA-Z]+$");
    //    Matcher matcher = pattern.matcher(value);
    //    if (matcher.matches() == true) {
    //        return true;
    //    }
    //    return false;
    //}

}
