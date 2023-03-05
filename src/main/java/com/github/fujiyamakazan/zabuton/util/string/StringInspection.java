package com.github.fujiyamakazan.zabuton.util.string;

import java.io.UnsupportedEncodingException;

/**
 * <pre>
 * 文字列検査のクラス
 * 原則として以下の条件を満たすメソッドを定義します。
 * ・検査対象の文字列をString型で受け取る
 * ・検査対象の文字列に対し何らかの評価を行う。
 * ・判定結果をboolean,int,charのいずれかで返却する。
 * </pre>
 * @author fujiyama
 */
public class StringInspection {
    /**
     * シングルバイトかを判定します。
     */
    public static boolean isSinglebyte(String str, String encoding) {
        if (isEmpty(str)) {
            return false;
        }
        int length = str.length();
        int lengthB;
        try {
            lengthB = str.getBytes(encoding).length;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("文字コード指定が不正です。encoding=[" + encoding + "]");
        }
        return length == lengthB;
    }

    /**
     * マルチバイトかを判定します。
     */
    public static boolean isMultibyte(String str, String encoding) {
        if (isEmpty(str)) {
            return false;
        }
        boolean onlySingle = true;
        for (int i = 0; i < str.length(); i++) {
            System.out.println(str.substring(i, i + 1));
            if (isSinglebyte(str.substring(i, i + 1), encoding)) {
                onlySingle = false;
                break;
            }
        }
        return onlySingle;
    }


    /**
     * <pre>
     * str1にstr2が含まれるかを評価します。
     * [実装例] contains("ABC9A", "ABC") ⇒ TRUE
     * ・不正な文字の検出など
     * </pre>
     * @param str1 検査対象
     * @param str2 文字列
     * @return strにinclusionsが含まれればTrue
     */
    public static boolean contains(String str1, char[] str2) {
        if (isEmpty(str1)) {
            return false;
        }
        for (int i = 0; i < str1.length(); i++) {
            char c = str1.charAt(i);
            boolean isHankaku = false;
            for (char h : str2) {
                if (c == h) {
                    isHankaku = true;
                    break;
                }
            }
            if (!isHankaku) {
                return true;
            }
        }
        return false;
    }

    /**
     * <pre>
     * str1がstr2だけで構成されているかを評価します。
     * [実装例] madeonly("ABCBA", "ABC") ⇒ TRUE
     * [実装例] madeonly("AB9BA", "ABC") ⇒ FALSE
     * ・不正な文字の検出など
     * </pre>
     * @param str1 検査対象
     * @param str2 文字列
     * @return strにinclusionsが含まれればTrue
     */
    public static boolean match(String str1, char[] str2) {
        return !contains(str1, str2);
    }

//    /**
//     * <pre>
//     * 「全角が含まれるか」を評価します。
//     * 「半角のみか」を評価する場合は以下のように否定(NOT)を使用してください。
//     *  ⇒!containsZenkaku(str)
//     *
//     * 【補足】
//     * 全角とはほぼ正方形の字形をした文字を示します。
//     * 文字コードにより2Byteであるとは限りません。
//     * </pre>
//     * @param str 検査対象
//     * @return 全角の文字が含まれればTrue
//     */
//    public static boolean containsZenkakuSample(String str) {
//        return contains(str, StringSet.HANKAKU.toCharArray());
//    }

    /*
     * 空文字、nullなどの判定メソッド集
     * isEmpty(String)以外は積極的に利用する必要はありません。
     * 他のメソッドはBlank、Space、nullの違いを明確に定義する為に
     * 作成しました。
     */
    /**
     * 空文字判定。Trimの後、評価されます。
     * @param str 検査対象
     * @return null、またはBlankであればTrue
     */
    public static boolean isEmpty(String str) {
        return isEmpty(str, true);
    }

    /**
     * 空文字判定をします。
     * @param str 検査対象
     * @param trim 前後のスペースを無視(Trim)するか？
     * @return null、またはBlankであればTrue
     */
    public static boolean isEmpty(String str, boolean trim) {
        if (str == null) {
            return true;
        }

        if (trim) {
            str.trim();
        }
        if (isBlank(str)) {
            return true;
        }

        return false;
    }

    /**
     * ブランク判定をします。
     * @param str 検査対象
     * @return 0Byteの文字列
     */
    public static boolean isBlank(String str) {
        if ("".equals(str)) {
            return true;
        }
        return false;
    }

    /**
     * スペース判定をします。
     * @param c 検査対象
     * @return スペース
     */
    public static boolean isSpace(char c) {
        if (c == ' ') {
            return true;
        }
        return false;
    }
}
