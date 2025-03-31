package com.github.fujiyamakazan.zabuton.util.string;

import org.apache.commons.lang3.StringUtils;

/**
 * キーワードを指定し、その前後で文字列を分割します。
 *
 * @author fuijyama
 */
public class StringCutter {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    /**
     * startWordとendWordに挟まれた文字列を返します。
     *
     * StringUtilsのメソッドで代替できるため、実装を差し替え。
     *
     * @return startWordとendWordに挟まれた文字列。
     */
    public static String between(String str, String startWord, String endWord) {
        return StringUtils.substringBetween(str, startWord, endWord);
    }

    /**
     * startMarkとtailMarkに挟まれた文字を置換します。
     * @param src 変換前文字列
     * @param startMark 置換開始箇所を示すマーク
     * @param tailMark 置換終了箇所を示すマーク
     * @param repStr 挿入する文字列
     * @return 置換後の文字列
     */
    public static String replaceBetween(String src, String startMark, String tailMark, String repStr) {
        int idxStart = src.indexOf(startMark);
        if (idxStart == -1) {
            return src;
        }
        int idxTail = -1;
        //if (idxStart == -1) {
        //    return src;
        //}
        idxTail = src.indexOf(tailMark, idxStart) + tailMark.length();
        if (idxStart >= idxTail) {
            return src;
        }

        String str = "";
        str += src.substring(0, idxStart);
        str += repStr;
        str += src.substring(idxTail);

        return str;
    }

//    public static void main(String[] args) {
//        LOGGER.debug(StringUtils.substringBefore("abdceabcde", "d"));
//        LOGGER.debug(StringUtils.substringBeforeLast("abdceabcde", "d"));
//        LOGGER.debug(left("abdceabcde", "d"));
//        LOGGER.debug(leftOfLast("abdceabcde", "d"));
//    }

    /**
     * delimiterより左側の部分を返します。
     * StringUtilsのメソッドで代替できるため、実装を差し替え。
     *
     * @return delimiterより左側の部分
     */
    public static String left(String src, String delimiter) {
//        return leftCore(src, src.indexOf(delimiter));
        return StringUtils.substringBefore(src, delimiter);
    }

    /**
     * delimiterより左側の部分を返します。
     *
     * StringUtilsのメソッドで代替できるため、実装を差し替え。
     *
     * @return delimiterより左側の部分 (区切り文字は後方一致とする)
     */
    public static String leftOfLast(String src, String delimiter) {
//        return leftCore(src, src.lastIndexOf(delimiter));
        return StringUtils.substringBeforeLast(src, delimiter);
    }

//    private static String leftCore(String src, final int index) {
//        try {
//            return src.substring(0, index);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }


public static void main(String[] args) {
    //LOGGER.debug(StringUtils.substringBefore("abdceabcde", "d"));
    //LOGGER.debug(StringUtils.substringBeforeLast("abdceabcde", "d"));
    LOGGER.debug(right("abdceabcde", "d"));
    LOGGER.debug(rightOfFirst("abdceabcde", "d"));
}

    /**
     * delimiterより右側の部分を返します。
     *
     * StringUtilsのメソッドで代替できるため、実装を差し替え。
     *
     * @return delimiterより右側の部分
     */
    public static String right(String src, String delimiter) {
        //final int index = src.lastIndexOf(delimiter);
        //return rightCore(src, delimiter, index);
        return StringUtils.substringAfterLast(src, delimiter);
    }

    /**
     * delimiterより右側の部分を返します。
     *
     * StringUtilsのメソッドで代替できるため、実装を差し替え。
     *
     * @return delimiterより右側の部分(区切り文字は前方一致とする)
     */
    public static String rightOfFirst(String src, String delimiter) {
        //final int index = src.indexOf(delimiter);
        //return rightCore(src, delimiter, index);
        return StringUtils.substringAfter(src, delimiter);
    }

//    private static String rightCore(String src, String delimiter, final int index) {
//        try {
//            return src.substring(index + delimiter.length());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

}
