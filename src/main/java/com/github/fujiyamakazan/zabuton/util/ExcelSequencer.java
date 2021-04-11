package com.github.fujiyamakazan.zabuton.util;

/**
 * Excelの列名に準拠した処理
 * @author fujiyama
 */
public class ExcelSequencer {
    /**
     * 数字をExcelの列名のようにアルファベットへ変換する。
     *
     * @param num 1以上の整数
     * @return 例）A,B,C...Z,AA,AB...AZ,AAA,AAB...
     */
    public static String num2alphabet(int num) {

        int firstIndexAlpha = (int) 'A'; // アルファベットの最初の文字
        int sizeAlpha = 26; // アルファベットの個数

        if (num <= 0) {
            /* 0以下はブランクで返す */
            return "";

        } else if (num <= sizeAlpha) {
            /* 1～26までの処理 */
            return String.valueOf((char) (firstIndexAlpha + num - 1));

        } else {
            /* 27以上の処理 */

            int offset = num - 1; // 0からの連番に補正した値
            int tmp = offset;
            String str = "";
            while (true) {
                int div = tmp / sizeAlpha; // 商
                int mod = tmp % sizeAlpha; // あまり

                str = num2alphabet(mod + 1) + str;

                if (div <= 0) {
                    break;
                }

                tmp = (div - 1);
            }
            ;
            return str;
        }
    }
}
