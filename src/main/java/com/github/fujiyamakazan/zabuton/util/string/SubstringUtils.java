package com.github.fujiyamakazan.zabuton.util.string;

/**
 * 部分文字列に関する操作をします。
 * @author fuijyama
 */
public class SubstringUtils {
    /**
     * @return startWordとendWordに挟まれた文字列。引数が不正であればnullを返す。
     */
    public static String between(String str, String startWord, String endWord) {
        if (str == null) {
            return null;
        }
        int start = str.indexOf(startWord);
        int end;
        if (endWord != null) {
            end = str.indexOf(endWord, start);
        } else {
            end = str.length();
        }
        if (start >= end) {
            return null;
        }
        if (start == -1 || end == -1) {
            return null;
        }
        return str.substring(start + startWord.length(), end);
    }

    /**
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
        if (idxStart == -1) {
            return src;
        }
        idxTail = src.indexOf(tailMark, idxStart) + tailMark.length();
        if (idxStart >= idxTail) {
            return src;
        }

        String str = "";
        try {
            str += src.substring(0, idxStart);
            str += repStr;
            str += src.substring(idxTail);
        } catch (Exception e) {
            e.printStackTrace();
            return src;
        }
        return str;
    }

    /**
     * @return delimiterより左側の部分
     */
    public static String left(String src, String delimiter) {
        return leftCore(src, src.indexOf(delimiter));
    }

    /**
     * @return delimiterより左側の部分 (区切り文字は後方一致とする)
     */
    public static String leftOfLast(String src, String delimiter) {
        return leftCore(src, src.lastIndexOf(delimiter));
    }

    private static String leftCore(String src, final int index) {
        try {
            return src.substring(0, index);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return delimiterより右側の部分
     */
    public static String right(String src, String delimiter) {
        final int index = src.lastIndexOf(delimiter);
        return rightCore(src, delimiter, index);
    }

    /**
     * @return delimiterより右側の部分(区切り文字は前方一致とする)
     */
    public static String rightOfFirst(String src, String delimiter) {
        final int index = src.indexOf(delimiter);
        return rightCore(src, delimiter, index);
    }

    private static String rightCore(String src, String delimiter, final int index) {
        try {
            return src.substring(index + delimiter.length());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
