package com.github.fujiyamakazan.zabuton.util.string;

import java.io.Serializable;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;

/**
 * 文字列の検査をするユーティリティです。
 *
 * 引数には検査対象のStringをとり、
 * なんらかの判定結果をbooleanで返却します。
 *
 * @author fujiyama
 */
public class StringInspection implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StringInspection.class);

    /**
     * 引数strにarrayの文字が含まれるかを検査します。
     * @deprecated {@link StringUtils#contains(CharSequence, CharSequence)} を直接利用してください。
     */
    public static boolean contains(String str, char[] searchArray) {
        return StringUtils.contains(str, String.valueOf(searchArray));
        //        for (char c: str.toCharArray()) {
        //            boolean flg = false;
        //            for (char h : array) {
        //                if (c == h) {
        //                    flg = true;
        //                    break;
        //                }
        //            }
        //            if (!flg) {
        //                return true;
        //            }
        //        }
        //        return false;
    }

    /**
     * strがarrayの要素だけで構成されているかを評価します。
     * "ABCBA", "ABC" ⇒ TRUE
     * "AB9BA", "ABC" ⇒ FALSE
     */
    public static boolean isOnly(String str, String searchStr) {
        //return contains(str, array) == false;
        if (StringUtils.isEmpty(str)) {
            return true;
        }
        for (char s : str.toCharArray()) {
            if (searchStr.contains(String.valueOf(s)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * strがarrayの要素だけで構成されているかを評価します。
     * "ABCBA", "ABC" ⇒ TRUE
     * "AB9BA", "ABC" ⇒ FALSE
     */
    public static boolean isOnly(String str, char[] searchArray) {
        return isOnly(str, String.valueOf(searchArray));
    }

    /**
     * strがarrayの要素だけで構成されているかを評価します。
     * @deprecated {@link #isOnly(String, char[])} 名称を見直したメソッドをご利用ください。
     */
    public static boolean match(String str, char[] array) {
        return isOnly(str, array);
    }

    /**
     * 半角数字だけで構成される文字かを判定します。
     */
    public static boolean isHalfNum(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        return isOnly(str, StringSet.HANKAKU_NUM);
    }

    ///**
    // * 「Empty」の解釈が一般的でないため、廃止。
    // * 空文字判定。Trimの後、評価されます。
    // * @param str 検査対象
    // * @return null、またはBlankであればTrue
    // */
    //public static boolean isEmpty(String str) {
    //    return isEmpty(str, true);
    //}
    ///**
    // * 「Blank」「Empty」の解釈が一般的でないため、廃止。
    // * @param str 検査対象
    // * @param trim 前後のスペースを無視(Trim)するか？
    // * @return null、またはBlankであればTrue
    // */
    //public static boolean isEmpty(String str, boolean trim) {
    //    if (str == null) {
    //        return true;
    //    }
    //    if (trim) {
    //        str.trim();
    //    }
    //    if (isBlank(str)) {
    //        return true;
    //    }
    //    return false;
    //}
    ///**
    // * Blankかどうか
    // * 0バイトのStringをBlankとするのは一般的でないため、廃止。
    // */
    //public static boolean isBlank(String str) {
    //    if ("".equals(str)) {
    //        return true;
    //    }
    //    return false;
    //}
    ///**
    // * @param c 検査対象
    // * @return スペース
    // */
    //public static boolean isSpace(char c) {
    //    if (c == ' ') {
    //        return true;
    //    }
    //    return false;
    //}

    /**
     * シングルバイトかどうかを判定します。空文字はfalseとします。
     * 「シングルバイト＝半角」ではありません。（UTF-8の半角カタカナなど）
     */
    public static boolean isSinglebyte(String str, Charset charset) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        int length = str.length();
        int lengthB = str.getBytes(charset).length;
        //LOGGER.debug("文字数:" + length + " バイト数:" + lengthB);
        return length == lengthB;
    }

    /**
     * シングルバイトかどうかを判定します。
     * @deprecated {@link #isSinglebyte(String, Charset)}
     */
    public static boolean isSinglebyte(String str, String charsetName) {
        return isSinglebyte(str, Charset.forName(charsetName));
    }

    /**
     * マルチバイトかどうかを判定します。空文字はfalseとします。
     */
    public static boolean isMultibyte(String str, Charset charset) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        boolean onlySingle = true;
        for (int i = 0; i < str.length(); i++) {
            if (isSinglebyte(str.substring(i, i + 1), charset)) {
                onlySingle = false;
                break;
            }
        }
        return onlySingle;
    }

    /**
     * マルチバイトかどうかを判定します。
     * @deprecated {@link #isSinglebyte(String, Charset)}
     */
    public static boolean isMultibyte(String str, String charsetName) {
        return isMultibyte(str, Charset.forName(charsetName));
    }

}
