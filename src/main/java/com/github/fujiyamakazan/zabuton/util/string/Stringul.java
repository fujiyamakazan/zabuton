package com.github.fujiyamakazan.zabuton.util.string;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.github.fujiyamakazan.zabuton.util.text.TextFile;

/**
 * String型のユーティリティです。
 *
 * TODO 定数の整理
 *
 * [String]-[U]ti[l]
 *
 * @author fujiyama
 */
public class Stringul implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Stringul.class);

    /** 文字集合名称 "UTF-8" を定数化しています。 */
    public static final String ENCODE_UTF_8 = StandardCharsets.UTF_8.name();

    /** いずれのプラットフォームにも共通する改行文字 */
    public static final String NEW_LINE_CODE = TextFile.LINE_SEPARATOR_LF;


    /**
     * 全角カタカナへの変換処理。
     * 変換できなかった文字はそのまま出力される。
     * @param str 対象文字列
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
                if (get(StringSet.LIST_KATAKANA, srcChar) != null
                    && get(StringSet.LIST_KATAKANA, srcChar).length() == 1) {
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
            String r = get(StringSet.LIST_KATAKANA, key);
            if (r != null) {
                sb.append(r);
            } else {
                sb.append(key);
            }
        }
        return sb.toString();
    }

    private static String get(List<Pair<String, String>> katakanas, String str) {
        for (Pair<String, String> p: katakanas) {
            if (StringUtils.equals(p.getValue(), str)) {
                return p.getKey();
            }
        }
        return str;
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
    public static List<String> find(String str, String pattern) {
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
     * 正規表現による文字列検査・抽出。
     * パターンに一致しなければnullを返却する。
     * グループとして複数件取得していればその１件目を返却する。
     * @param str 対象文字列
     * @param pattern 正規表現のパターン
     * @return 取得結果
     */
    public static String findFirst(String str, String pattern) {
        List<String> list = find(str, pattern);
        if (list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * 数値を2桁の文字へ変換
     */
    public static String pad2(int i) {
        if (i < 0 || i > 99) {
            throw new IllegalArgumentException("2桁の左0埋めの値に変換できません。" + i);
        }

        if (i <= 9) {
            return "0" + i;
        } else {
            return "" + i;
        }
    }

    /**
     * 数値を4桁の文字へ変換
     */
    public static String pad4(int i) {
        if (i < 0 || i > 9999) {
            throw new IllegalArgumentException("4桁の左0埋めの値に変換できません。" + i);
        }
        String str = "000" + i;
        str = str.substring(str.length() - 4);
        return str;
    }

    /**
     * <pre>
     * XSS(Cross Site Scripting)対策のMethodです。
     * 引数の文字列に含まれるHTMLタグを実態参照へ変換します。
     * </pre>
     * @param str 変換対象の文字列
     * @return 変換された文字列
     */
    public static String escpeHtml(String str) {

        if (str == null || str.length() == 0) {
            return str;
        }

        StringBuffer strResult = null;
        String strFiltered = null;
        for (int i = 0; i < str.length(); i++) {
            strFiltered = null;
            switch (str.charAt(i)) {
                case '<':
                    strFiltered = "&lt;";
                    break;
                case '>':
                    strFiltered = "&gt;";
                    break;
                case '&':
                    strFiltered = "&amp;";
                    break;
                case '"':
                    strFiltered = "&quot;";
                    break;
                case '\'':
                    strFiltered = "&#39;";
                    break;
                default:
                    break;
            }

            if (strResult == null) {
                if (strFiltered != null) {
                    strResult = new StringBuffer(str.length() + 50);
                    if (i > 0) {
                        strResult.append(str.substring(0, i));
                    }
                    strResult.append(strFiltered);
                }
            } else {
                if (strFiltered == null) {
                    strResult.append(str.charAt(i));
                } else {
                    strResult.append(strFiltered);
                }
            }
        }
        if (strResult == null) {
            return str;
        } else {
            return strResult.toString();
        }
    }

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

    public static boolean isMultibyte(String str, String encoding) {
        if (isEmpty(str)) {
            return false;
        }
        boolean onlySingle = true;
        for (int i = 0; i < str.length(); i++) {
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
     * @param c 検査対象
     * @return スペース
     */
    public static boolean isSpace(char c) {
        if (c == ' ') {
            return true;
        }
        return false;
    }

    /**
     * lineのなかのindexにあたる文字を返す。
     */
    public static String getWordOnIndex(String line, int index) {
        return String.valueOf(line.toString().toCharArray()[index]);
    }

    /**
     * lineの中にあるsindex 〜 eindex の文字を返す。
     */
    public static String getWordBettweenIndex(String line, int sindex, int eindex) {
        StringBuffer ret = new StringBuffer();
        for (int i = sindex; i <= eindex; i++) {
            ret.append(getWordOnIndex(line, i));
        }
        return ret.toString();
    }

    /**
     * 文字を区切り文字の前後に分割するメソッド
     */
    public static ArrayList<String> stringSeparater(String str, String delim) {

        ArrayList<String> arl = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(str, delim);

        while (st.hasMoreTokens()) {
            arl.add(st.nextToken());
        }

        return arl;

    }

    /**
     * 数値のカンマや単位などの装飾を除去するメソッド
     */
    public static String rmDecoration4Figure(String decFig) {

        StringBuffer simpleFig = new StringBuffer();
        String[] num = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };

        //前後の空白を除去
        decFig = decFig.trim();

        //decFigがNullかblankならばそのまま返す。
        if (decFig == null || decFig.equals("")) {
            return decFig;
        }

        //"-"の記録、一時除去
        String mark = "";
        if (decFig.length() != 1) {
            if (decFig.substring(0, 1).equals("-")) {
                mark = "-";
                decFig = decFig.substring(1);
            }
        }

        //単位の除去
        //　数字、カンマ、小数点
        boolean isNum = false;
        boolean isComma = false;
        boolean isPeriod = false;

        String piece = null;
        for (int i = 0; i < decFig.length(); i++) {
            piece = decFig.substring(i, i + 1);
            //判定
            if (piece.equals(",")) {
                //[,]と判定
                isComma = true;
            } else if (piece.equals(".")) {
                //[.]と判定
                isPeriod = true;
            } else {
                for (int j = 0; j < num.length; j++) {
                    if (piece.equals(num[j])) {
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

    // http://www7a.biglobe.ne.jp/~java-master/samples/string/ZenkakuHiraganaToZenkakuKatakana.html

    /**
     * 制限桁数で文字を切り出す処理。
     * @param src 元の文字列(処理実施後に残りの文字列に変化します。)
     * @param limit 制限桁数
     * @return 制限桁数で切り捨てたられた文字列
     */
    @SuppressWarnings("unused")
    private static StringBuffer split(StringBuffer src, int limit) {
        StringBuffer dest = new StringBuffer("");
        int c = 0; // 現在の桁数
        /*
         * destの桁数がLIMITを超える直前か、srcが0桁になるまで繰り返し処理
         */
        while (src.length() > 0) {
            String tmp = src.substring(0, 1);
            boolean isDublebyte = StringInspection.isMultibyte(tmp, ENCODE_UTF_8);
            if (isDublebyte) {
                if ((c + 2) > limit) {
                    break;
                } else {
                    c = c + 2;
                }
            } else {
                if ((c + 1) > limit) {
                    break;
                } else {
                    c = c + 1;
                }
            }
            dest.append(tmp);
            src.delete(0, 1);
        }
        return dest;
    }

    //    private static final char ZENKAKU_KATAKANA_FIRST_CHAR = ZENKAKU_KATAKANA[0];
    //
    //    private static final char ZENKAKU_KATAKANA_LAST_CHAR = ZENKAKU_KATAKANA[ZENKAKU_KATAKANA.length - 1];
    //
    //
    //    public static String zenkakuKatakanaToHankakuKatakana(char c) {
    //          if (c >= ZENKAKU_KATAKANA_FIRST_CHAR && c <= ZENKAKU_KATAKANA_LAST_CHAR) {
    //            return HANKAKU_KATAKANA[c - ZENKAKU_KATAKANA_FIRST_CHAR];
    //          } else {
    //            return String.valueOf(c);
    //          }
    //        }
    //
    //        public static String zenkakuKatakanaToHankakuKatakana(String s) {
    //          StringBuffer sb = new StringBuffer();
    //          for (int i = 0; i < s.length(); i++) {
    //            char originalChar = s.charAt(i);
    //            String convertedChar = zenkakuKatakanaToHankakuKatakana(originalChar);
    //            sb.append(convertedChar);
    //          }
    //          return sb.toString();
    //
    //        }

    /**
     * @return 半角自整数(不の値は含まない)であればTrue。それ以外やnullの時はFalse。
     */
    public static boolean isHalfNum(String str) {
        if (str == null) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            boolean match = false;
            for (char c : StringSet.HANKAKU_NUM.toCharArray()) {
                if (c == str.charAt(i)) {
                    match = true;
                    break;
                }
            }
            if (match == false) {
                return false;
            }
        }
        return true;
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
