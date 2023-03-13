package com.github.fujiyamakazan.zabuton.util.string;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;

import com.github.fujiyamakazan.zabuton.util.text.TextFile;

/**
 * String型のユーティリティです。
 *
 * [String]-[U]ti[l]
 *
 * ・正規表現を使用した処理は {@link RegexUtils} に集約します。
 * ・キーワードを指定し、その前後で文字列を分割する処理は {@link StringCutter} に集約します。
 * ・文字列を検査し、boolean型の判定結果を返す処理は {@link StringInspection} に集約します。
 * ・「半角カナ」などの文字列定数は {@link StringSet} に集約します。
 *
 * @author fujiyama
 */
public class Stringul implements Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Stringul.class);

    /** 文字集合名称 "UTF-8" を定数化しています。 */
    public static final String ENCODE_UTF_8 = StandardCharsets.UTF_8.name();

    /** いずれのプラットフォームにも共通する改行文字です。 */
    public static final String NEW_LINE_CODE = TextFile.LINE_SEPARATOR_LF;

    /**
     * 半角カタカナを全角カタカナにします。
     * 変換できなかった文字は変更しません。
     *
     * @param src 対象文字列
     * @return 変換後の文字列
     */
    public static String toFullKatakana(String src) {
        char[] array = src.toCharArray();
        StringBuilder sb = new StringBuilder();
        boolean nextSkip = false;
        for (int i = 0; i < array.length; i++) {
            if (nextSkip) {
                nextSkip = false;
                continue;
            }
            String srcChar = String.valueOf(array[i]);

            /*
             * 1文字の半角カタカナであり、
             * 続いて濁音記号、半濁音記号があれば、
             * まとめて変換する。
             */
            String key = null;
            if (i != array.length - 1) {
                //if (KANA_MAP_H2F.get(srcChar) != null && KANA_MAP_H2F.get(srcChar).length() == 1) {
                if (getKatakana(srcChar) != null
                    && getKatakana(srcChar).length() == 1) {
                    if (array[i + 1] == 'ﾞ' || array[i + 1] == 'ﾟ') {
                        key = srcChar + array[i + 1];
                        nextSkip = true;
                    }
                }
            }
            if (key == null) {
                key = srcChar;
            }

            //String r = KANA_MAP_H2F.get(key);
            String r = getKatakana(key);
            if (r != null) {
                sb.append(r);
            } else {
                sb.append(key);
            }
        }
        return sb.toString();
    }

    private static String getKatakana(String str) {
        for (Pair<String, String> p : StringSet.LIST_KATAKANA) {
            if (StringUtils.equals(p.getValue(), str)) {
                return p.getKey();
            }
        }
        return str;
    }

    /**
     * 数値をパディングして2桁にします。
     */
    public static String pad2(int num) {
        return pad(num, 2);
        //if (i < 0 || i > 99) {
        //    throw new IllegalArgumentException("2桁の左0埋めの値に変換できません。" + i);
        //}
        //if (i <= 9) {
        //    return "0" + i;
        //} else {
        //    return "" + i;
        //}
    }

    /**
     * 数値をパディングして4桁にします。
     */
    public static String pad4(int num) {
        return pad(num, 4);
        //if (i < 0 || i > 9999) {
        //    throw new IllegalArgumentException("4桁の左0埋めの値に変換できません。" + i);
        //}
        //String str = "000" + i;
        //str = str.substring(str.length() - 4);
        //return str;
    }

    /**
     * 数値をパディングします。
     */
    private static String pad(int num, int size) {
        return StringUtils.leftPad(String.valueOf(num), size, '0');
    }

    /**
     * XSS(Cross Site Scripting)対策です。
     * HTMLタグを実態参照へ変換します。
     * @param str 変換対象の文字列
     * @return 変換された文字列
     */
    public static String escpeHtml(String str) {

        return StringEscapeUtils.escapeHtml4(str);

        //            switch (str.charAt(i)) {
        //                case '<':
        //                    strFiltered = "&lt;";
        //                    break;
        //                case '>':
        //                    strFiltered = "&gt;";
        //                    break;
        //                case '&':
        //                    strFiltered = "&amp;";
        //                    break;
        //                case '"':
        //                    strFiltered = "&quot;";
        //                    break;
        //                case '\'':
        //                    strFiltered = "&#39;";
        //                    break;
        //                default:
        //                    break;
        //            }

    }



    /**
     * 文字列のなかのindexにあたる文字を返す。
     * @deprecated メソッドに隠ぺいするメリットがない。基本APIを直接使用すること。
     */
    @Deprecated
    public static String getWordOnIndex(String str, int index) {
        //return String.valueOf(line.toString().toCharArray()[index]);
        return String.valueOf(str.charAt(index));
    }

    /**
     * lineの中にあるsindex ～ eindex の文字を返します。
     * @deprecated StringUtils#substring とほぼ同等の処理なうえ、末尾の処理に差異が出る。呼び出し箇所が無くなったら廃止する。
     */
    @Deprecated
    public static String getWordBettweenIndex(String line, int sindex, int eindex) {
        StringBuffer ret = new StringBuffer();
        for (int i = sindex; i <= eindex; i++) {
            ret.append(getWordOnIndex(line, i));
        }
        return ret.toString();
    }

    /**
     * 文字を区切り文字の前後に分割するメソッドです。
     * @deprecated {@link String#split(String)}を利用すること。
     */
    @Deprecated
    public static List<String> stringSeparater(String str, String delimiter) {
        return Arrays.asList(str.split(delimiter));
        //ArrayList<String> arl = new ArrayList<String>();
        //StringTokenizer st = new StringTokenizer(str, delim);
        //while (st.hasMoreTokens()) {
        //    arl.add(st.nextToken());
        //}
        //return arl;
    }

    /**
     * 数値のカンマや単位などの装飾を除去します。
     */
    public static String rmDecoration4Figure(String str) {

        StringBuilder simpleFig = new StringBuilder();
        char[] num = StringSet.HANKAKU_NUM.toCharArray();

        if (StringUtils.isEmpty(str)) {
            return str;
        }

        str = str.trim(); //前後の空白を除去

        /* "-"の記録、一時除去 */
        String mark = "";
        if (str.length() != 1) {
            if (str.substring(0, 1).equals("-")) {
                mark = "-";
                str = str.substring(1);
            }
        }

        //単位の除去
        //　数字、カンマ、小数点
        boolean isNum = false;
        boolean isComma = false;
        boolean isPeriod = false;

        String piece = null;
        for (int i = 0; i < str.length(); i++) {
            piece = str.substring(i, i + 1);
            //判定
            if (piece.equals(",")) {
                //[,]と判定
                isComma = true;
            } else if (piece.equals(".")) {
                //[.]と判定
                isPeriod = true;
            } else {
                for (int j = 0; j < num.length; j++) {
                    if (piece.equals(String.valueOf(num[j]))) {
                        //数字と判定
                        isNum = true;
                        break;
                    }
                }
            }
            //判定別処理
            //　数字や[.]の場合・・・連結
            //　[,]の場合・・・連結しないで処理を続ける。
            //　それ以外の場合・・・連結しないで処理を終了する。
            if (isNum || isPeriod) {
                simpleFig.append(piece);
            } else if (isComma) {
                //連結しない
            } else if (!isComma && !isPeriod && !isNum) {
                break;
            }
            //フラグのリセット
            isNum = false;
            isComma = false;
            isPeriod = false;
        }

        //値のチェック
        //　[.]が二つ以上ある。または末尾にある場合は、最後の[.]以降を削除
        String strSimpleFig = null;
        strSimpleFig = simpleFig.toString();
        //　二つある場合
        if (strSimpleFig.indexOf(".") != strSimpleFig.lastIndexOf(".")) {
            strSimpleFig = strSimpleFig.substring(0, strSimpleFig.lastIndexOf("."));
        }

        if (strSimpleFig.lastIndexOf(".") == simpleFig.length() - 1) {
            strSimpleFig = strSimpleFig.substring(0, strSimpleFig.length() - 1);
        }

        //符号の付加
        if (mark.equals("-")) {
            return "-" + strSimpleFig;
        } else {
            return strSimpleFig;
        }
    }

    /**
     * 例外オブジェクトを文字列型に変換します。
     */
    public static String ofThrowable(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    /**
     * 例外オブジェクトを文字列型に変換します。
     * (ThrowableよりExceptionの方がなじみがあるので、このメソッド名も準備しました。)
     */
    public static String ofException(Exception e) {
        return ofThrowable(e);
    }

}
