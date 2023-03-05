package com.github.fujiyamakazan.zabuton.util.string;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.util.lang.Generics;

/**
 * 文字列の集合を定義します。
 *
 * TODO 定数の整理
 *
 *
 * ここで示す「半角」「全角」は一般的な認識に基づくものです。
 * 必ずしも半角がシングルバイトとは限りません。「半角」「全角」は使用フォントの字体(glyph)の影響を受けます。
 *
 * ここで示す「数字」とはアラビア数字のことです。
 * ここで示す「英字」「アルファベット」 とはラテン文字のことです。
 *
 */
public class StringSet implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StringSet.class);

    /** 半角数字（intでは48～57）*/
    public static final String HANKAKU_NUM = "0123456789";

    /** 半角英小字 */
    private static final String HANKAKU_ALPHA_SMALL = "abcdefghijklmnopqrstuvwxyz";

    /** 半角英大字 */
    private static final String HANKAKU_ALPHA_BIG = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /** ASCIIに含まれる半角記号の一部（intでは33～47）*/
    private static final String ASCII_KIGO_1 = "!\"#$%&'()*+,-./";

    /** ASCIIに含まれる半角記号の一部（intでは58～64）*/
    private static final String ASCII_KIGO_2 = ":;<=>?@";

    /** ASCIIに含まれる半角記号の一部（intでは91～96）*/
    private static final String ASCII_KIGO_3 = "[\\]^_`";

    /** ASCIIに含まれる半角記号の一部（intでは123～126）*/
    private static final String ASCII_KIGO_4 = "{|}~";

    /**
     * ASCII文字集合です。（intでは33～126）
     * スペース以外の制御文字は除きます。
     */
    public static final String ASCII = ASCII_KIGO_1
        + HANKAKU_NUM
        + ASCII_KIGO_2
        + HANKAKU_ALPHA_BIG
        + ASCII_KIGO_3
        + HANKAKU_ALPHA_SMALL
        + ASCII_KIGO_4;

    /** JIS_X_0201に含まれる半角カタカナの一部 */
    private static final String HANKAKU_KATAKANA1 = "ｦｧｨｩｪｫｬｭｮｯ";

    /** JIS_X_0201に含まれる半角カタカナの一部 */
    private static final String HANKAKU_KATAKANA2 = "ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ";

    /**
     * 半角カタカナです。
     * ASCII文字集合のうち、JIS X 0208(JIS基本漢字)の５区（カタカナ）に対応する文字があるもの。
     */
    public static final String HANKAKU_KATAKANA = HANKAKU_KATAKANA2 + HANKAKU_KATAKANA1;

    /** JIS_X_0201に含まれる半角記号の一部 */
    private static final String HANKAKU_KIGO_1 = "｡｢｣､･";

    /**
     * JIS_X_0201に含まれる半角記号の一部です。
     * 「ｰ」は、対応する文字がJIS X 0208の５区にないため、記号と定義します。
     */
    private static final String HANKAKU_KIGO_2 = "ｰ";

    /**
     * JIS_X_0201に含まれる半角記号の一部です。
     * 「ﾞ」「ﾟ」は、対応する文字がJIS X 0208の５区にないため、記号と定義します。
     */
    private static final String HANKAKU_KIGO_3 = "ﾞﾟ";

    /**
     * JIS_X_0201文字集合です。
     * ASCIIに片仮名図形文字集合（intでは65377～65439）を加えたものです。
     * スペース以外の制御文字は除きます。
     */
    public static final String JIS_X_0201 = ASCII
        + HANKAKU_KIGO_1
        + HANKAKU_KATAKANA1
        + HANKAKU_KIGO_2
        + HANKAKU_KATAKANA2
        + HANKAKU_KIGO_3;

    /** 半角英字 */
    public static final String HANKAKU_ALPHA = HANKAKU_ALPHA_SMALL + HANKAKU_ALPHA_BIG;

    /** ASCIIに含まれる半角記号 */
    private static final String ASCII_KIGO = ASCII_KIGO_1 + ASCII_KIGO_2 + ASCII_KIGO_3 + ASCII_KIGO_4;

    /**
     * JIS_X_0201に含まれる記号です。
     */
    public static String HANKAKU_KIGO = ASCII_KIGO + HANKAKU_KIGO_1 + HANKAKU_KIGO_2 + HANKAKU_KIGO_3;

    /**
     * 半角英数カナとJIS_X_0201に含まれる記号です。
     */
    public static String HANKAKU = HANKAKU_NUM + HANKAKU_ALPHA + HANKAKU_KATAKANA + HANKAKU_KIGO;

    /**
     * 全角カタカナと半角カタカナの対応です。
     * 濁点、半濁点を含むカタカナは、半角では記号付き２文字です。
     */
    public static final List<Pair<String, String>> LIST_KATAKANA = initKatakana();

    private static List<Pair<String, String>> initKatakana() {
        ArrayList<Pair<String, String>> list = Generics.newArrayList();
        list.add(Pair.of("ァ", "ｧ"));
        list.add(Pair.of("ア", "ｱ"));
        list.add(Pair.of("ィ", "ｨ"));
        list.add(Pair.of("イ", "ｲ"));
        list.add(Pair.of("ゥ", "ｩ"));
        list.add(Pair.of("ウ", "ｳ"));
        list.add(Pair.of("ェ", "ｪ"));
        list.add(Pair.of("エ", "ｴ"));
        list.add(Pair.of("ォ", "ｫ"));
        list.add(Pair.of("オ", "ｵ"));
        list.add(Pair.of("カ", "ｶ"));
        list.add(Pair.of("ガ", "ｶﾞ"));
        list.add(Pair.of("キ", "ｷ"));
        list.add(Pair.of("ギ", "ｷﾞ"));
        list.add(Pair.of("ク", "ｸ"));
        list.add(Pair.of("グ", "ｸﾞ"));
        list.add(Pair.of("ケ", "ｹ"));
        list.add(Pair.of("ゲ", "ｹﾞ"));
        list.add(Pair.of("コ", "ｺ"));
        list.add(Pair.of("ゴ", "ｺﾞ"));
        list.add(Pair.of("サ", "ｻ"));
        list.add(Pair.of("ザ", "ｻﾞ"));
        list.add(Pair.of("シ", "ｼ"));
        list.add(Pair.of("ジ", "ｼﾞ"));
        list.add(Pair.of("ス", "ｽ"));
        list.add(Pair.of("ズ", "ｽﾞ"));
        list.add(Pair.of("セ", "ｾ"));
        list.add(Pair.of("ゼ", "ｾﾞ"));
        list.add(Pair.of("ソ", "ｿ"));
        list.add(Pair.of("ゾ", "ｿﾞ"));
        list.add(Pair.of("タ", "ﾀ"));
        list.add(Pair.of("ダ", "ﾀﾞ"));
        list.add(Pair.of("チ", "ﾁ"));
        list.add(Pair.of("ヂ", "ﾁﾞ"));
        list.add(Pair.of("ッ", "ｯ"));
        list.add(Pair.of("ツ", "ﾂ"));
        list.add(Pair.of("ヅ", "ﾂﾞ"));
        list.add(Pair.of("テ", "ﾃ"));
        list.add(Pair.of("デ", "ﾃﾞ"));
        list.add(Pair.of("ト", "ﾄ"));
        list.add(Pair.of("ド", "ﾄﾞ"));
        list.add(Pair.of("ナ", "ﾅ"));
        list.add(Pair.of("ニ", "ﾆ"));
        list.add(Pair.of("ヌ", "ﾇ"));
        list.add(Pair.of("ネ", "ﾈ"));
        list.add(Pair.of("ノ", "ﾉ"));
        list.add(Pair.of("ハ", "ﾊ"));
        list.add(Pair.of("バ", "ﾊﾞ"));
        list.add(Pair.of("パ", "ﾊﾟ"));
        list.add(Pair.of("ヒ", "ﾋ"));
        list.add(Pair.of("ビ", "ﾋﾞ"));
        list.add(Pair.of("ピ", "ﾋﾟ"));
        list.add(Pair.of("フ", "ﾌ"));
        list.add(Pair.of("ブ", "ﾌﾞ"));
        list.add(Pair.of("プ", "ﾌﾟ"));
        list.add(Pair.of("ヘ", "ﾍ"));
        list.add(Pair.of("ベ", "ﾍﾞ"));
        list.add(Pair.of("ペ", "ﾍﾟ"));
        list.add(Pair.of("ホ", "ﾎ"));
        list.add(Pair.of("ボ", "ﾎﾞ"));
        list.add(Pair.of("ポ", "ﾎﾟ"));
        list.add(Pair.of("マ", "ﾏ"));
        list.add(Pair.of("ミ", "ﾐ"));
        list.add(Pair.of("ム", "ﾑ"));
        list.add(Pair.of("メ", "ﾒ"));
        list.add(Pair.of("モ", "ﾓ"));
        list.add(Pair.of("ャ", "ｬ"));
        list.add(Pair.of("ヤ", "ﾔ"));
        list.add(Pair.of("ュ", "ｭ"));
        list.add(Pair.of("ユ", "ﾕ"));
        list.add(Pair.of("ョ", "ｮ"));
        list.add(Pair.of("ヨ", "ﾖ"));
        list.add(Pair.of("ラ", "ﾗ"));
        list.add(Pair.of("リ", "ﾘ"));
        list.add(Pair.of("ル", "ﾙ"));
        list.add(Pair.of("レ", "ﾚ"));
        list.add(Pair.of("ロ", "ﾛ"));
        list.add(Pair.of("ヮ", "ﾜ"));
        list.add(Pair.of("ワ", "ﾜ"));
        list.add(Pair.of("ヲ", "ｦ"));
        list.add(Pair.of("ン", "ﾝ"));
        list.add(Pair.of("ヴ", "ｳﾞ"));
        //list.add(Pair.of("ヰ","ｲ"));
        //list.add(Pair.of("ヱ","ｴ"));
        //list.add(Pair.of("ヵ","ｶ"));
        //list.add(Pair.of("ヶ","ｹ"));
        return list;
    }

    ///**
    // * テストに用いる文字のサンプルです。
    // * 代表となる文字をピックアップしています。(同値クラスを省略)
    // * ※ JIS X 0201、及び対応する全角文字は全て含めます。
    // */
    //public static final String CHARS_OF_SAMPLE;

    // [-]はU+002D(ハイフンマイナス)

    static {

        /* A.半角文字(全角に変換可能) */
        StringBuilder hankakuConvertibl = new StringBuilder();

        // JIS X 0201 (ASCII) [-はU+002D(ハイフンマイナス)][\と"はJavaの言語仕様により\でエスケープしている。]
        hankakuConvertibl.append(ASCII);

        // JIS X 0201 (カタカナ用図形)
        //hankakuConvertibl.append("｡｢｣､･" + HANKAKU_KANA);

        // その他
        //hankakuConvertibl.append("‾"); //  U+203E 全角オーバーライン

        /* B.全角文字(半角に変換可能) */
        //StringBuilder zenkakuConvertibl = new StringBuilder();
        //zenkakuConvertibl
        //    .append("　！”＃＄％＆’（）＊＋，－．／０１２３４５６７８９：；＜＝＞？＠
        //ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ［￥］＾＿｀
        //ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ｛｜｝～");
        //zenkakuConvertibl.append("。「」、・ヲァィゥェォャュョッー"
        //    + "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロ"
        //    + "ワン゛゜");
        //zenkakuConvertibl.append("￣"); // U+FFE3 全角オーバーライン

        ///* 半角文字(Aを包含) */
        //StringBuilder hankaku = new StringBuilder();
        //hankaku.append(hankakuConvertibl);
        //hankaku.append("‑"); // U+2011 ノンブレーキングハイフン
        //hankaku.append("£"); // U+00A3 ポンド記号

        /* 全角文字(Bを包含)  */
        StringBuilder zenkaku = new StringBuilder();
        //zenkaku.append(zenkakuConvertibl);
        zenkaku.append("亜"); // JIS X 0208 第１水準
        zenkaku.append("弌"); // JIS X 0208 第２水準
        zenkaku.append("♪"); // JIS X 0208 2区
        zenkaku.append("①"); // JIS X 0208 13区(NEC特殊文字)
        zenkaku.append("髙彅﨑"); // JIS X 0208 89区～92区(NEC選定IBM拡張文字)
        zenkaku.append("∩"); // JIS X 0208 重複定義(2区と13区)
        zenkaku.append("∵"); // JIS X 0208 重複定義(2区と13区)
        zenkaku.append("￢"); // JIS X 0208 重複定義(2区と92区と115区)
        zenkaku.append("㈱"); // JIS X 0208 重複定義(13区と115区)
        zenkaku.append("ヰヱヵヶ"); // 対応する半角文字が無い全角カタカナ
        zenkaku.append("你"); // 中国語 (Windows-31J)に含まれない
        zenkaku.append("〜"); // 波ダッシュ (Cp943CとMS932で解釈が変わる文字の代表)
        zenkaku.append("‐"); // U+2010 ハイフン
        zenkaku.append("￡"); // U+FFE1 全角ポンド記号

        /*
         * 代表文字列(同値クラスの省略)
         * JIS X 0201、及び対応する全角文字は全て列挙
         */
        StringBuilder words = new StringBuilder();
        //words.append(hankaku);
        words.append(zenkaku);
        //System.out.println(words.toString());
        //CHARS_OF_SAMPLE = words.toString();

        /* 代表文字列(同値クラスの省略) */
        //      String sample = "!:[~｡ﾞ0ｱｧAa０アァＡａ亜ヰ";
    }

    /*
     * JIS X 0208コード表
     * https://www.asahi-net.or.jp/~ax2s-kmtn/ref/jisx0208.html
     *
     * ５区のうち、対応するハンカクカナがJIS_X_0201にないもの
     * 　ガギグゲゴ
     * 　ザジズゼゾ
     * 　ダヂヅデド
     * 　バパビピブプベペボポ
     * 　ヮヰヱヴヵヶ
     */

    private static HashMap<String, String> getAll() {
        HashMap<String, String> all = Generics.newHashMap();
        all.put("半角数字", HANKAKU_NUM);
        all.put("半角英字", HANKAKU_ALPHA);
        all.put("半角カタカナ", HANKAKU_KATAKANA);
        all.put("半角記号", HANKAKU_KIGO);
        all.put("半角文字", HANKAKU);
        return all;
    }

    public static void main(String[] args) {

        LOGGER.debug("種別ごとに出力");
        Map<String, String> map = getAll();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : map.entrySet()) {
            LOGGER.debug(e.getKey() + ":" + e.getValue());
            sb.append(e.getValue());
        }
        //String all = sb.toString();
        String all = HANKAKU;

        LOGGER.debug("intと6進表記で出力");
        for (char c : all.toCharArray()) {

            byte[] utf8Bytes = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
            String utf8 = "";
            for (int j = 0; j < utf8Bytes.length; j++) {
                utf8 += Integer.toHexString(utf8Bytes[j] & 0xff) + " ";
            }
            byte[] sjisBytes = String.valueOf(c).getBytes(Charset.forName("Shift_JIS"));
            String sjis = "";
            for (int j = 0; j < sjisBytes.length; j++) {
                sjis += Integer.toHexString(sjisBytes[j] & 0xff) + " ";
            }

            LOGGER.debug(String.format("%s: %s(int) %s(Unicode) %s(Shift_JIS)", c, (int) c, utf8, sjis));
        }

        //        LOGGER.debug("Unicodeにおける16進表記で出力");
        //        for (char c: all.toCharArray()) {
        //            System.out.print(c + " ");
        //
        //        }
        //
        //        LOGGER.debug("Shift_JISにおける16進表記で出力");
        //        for (char c: all.toCharArray()) {
        //            System.out.print(c + " ");
        //            byte[] sjisBytes = String.valueOf(c).getBytes(Charset.forName("Shift_JIS"));
        //            for (int j = 0; j < sjisBytes.length; j++) {
        //                LOGGER.debug(Integer.toHexString(sjisBytes[j] & 0xff) + " ");
        //            }
        //        }

    }

    /**
     * テストメソッド
     */
    private static void mainBack() {
        //        StringBuilder sb = new StringBuilder();
        //        for (int i = 32; i <= 126; i++) {
        //            sb.append((char) i);
        //        }
        //        String 半角_文字コードに基づく = sb.toString();
        //        String 半角 = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
        //        sb = new StringBuilder();
        //        for (int i = 65377; i <= 65439; i++) {
        //            sb.append((char) i);
        //        }
        //        String 半角2_文字コードに基づく = sb.toString();
        //        String 半角2 = "｡｢｣､･" + HANKAKU_KANA;
        //
        //        String 全角 = "　！”＃＄％＆’（）＊＋，－．／０１２３４５６７８９：；＜＝＞？＠ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ［￥］＾＿‘ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ｛｜｝￣";
        //        System.out.println(半角_文字コードに基づく);
        //        System.out.println(半角);
        //        System.out.println(半角.equals(半角_文字コードに基づく));
        //        System.out.println(全角);
        //
        //        System.out.println(半角2_文字コードに基づく);
        //        System.out.println(半角2);
        //        System.out.println(半角2.equals(半角2_文字コードに基づく));
        //
        //        System.out.println((int) '￣');
        //        System.out.println((int) '～');
        //        System.out.println((int) '~');
    }

}

///**
// * 全角カタカナから半角カタカナを検索するためのMap
// * keyに全角カタカナ、valueに半角カタカナを登録します。
// */
//public static final Map<String, String> KANA_MAP_F2H;
///**
// * 半角カタカナから全角カタカナを検索するためのMap
// * keyに半角カタカナ、valueに全角カタカナを登録します。
// */
//public static final Map<String, String> KANA_MAP_H2F;
///* 全角カタカナ(半角ｶﾀｶﾅに変換可能) */
//char[] zenkakuKatakanaConvertiblHankakuKatakana = {
//  'ァ', 'ア', 'ィ', 'イ', 'ゥ',
//  'ウ', 'ェ', 'エ', 'ォ', 'オ', 'カ', 'ガ', 'キ', 'ギ', 'ク', 'グ', 'ケ', 'ゲ',
//  'コ', 'ゴ', 'サ', 'ザ', 'シ', 'ジ', 'ス', 'ズ', 'セ', 'ゼ', 'ソ', 'ゾ', 'タ',
//  'ダ', 'チ', 'ヂ', 'ッ', 'ツ', 'ヅ', 'テ', 'デ', 'ト', 'ド', 'ナ', 'ニ', 'ヌ',
//  'ネ', 'ノ', 'ハ', 'バ', 'パ', 'ヒ', 'ビ', 'ピ', 'フ', 'ブ', 'プ', 'ヘ', 'ベ',
//  'ペ', 'ホ', 'ボ', 'ポ', 'マ', 'ミ', 'ム', 'メ', 'モ', 'ャ', 'ヤ', 'ュ', 'ユ',
//  'ョ', 'ヨ', 'ラ', 'リ', 'ル', 'レ', 'ロ', 'ヮ', 'ワ', 'ヲ', 'ン', 'ヴ' };
///* 半角カタカナ(全角カタカナに変換可能) */
//String[] hankakuKatakanaConvertiblZenkakuKatakana = {
//  "ｧ", "ｱ", "ｨ", "ｲ", "ｩ",
//  "ｳ", "ｪ", "ｴ", "ｫ", "ｵ", "ｶ", "ｶﾞ", "ｷ", "ｷﾞ", "ｸ", "ｸﾞ", "ｹ",
//  "ｹﾞ", "ｺ", "ｺﾞ", "ｻ", "ｻﾞ", "ｼ", "ｼﾞ", "ｽ", "ｽﾞ", "ｾ", "ｾﾞ", "ｿ",
//  "ｿﾞ", "ﾀ", "ﾀﾞ", "ﾁ", "ﾁﾞ", "ｯ", "ﾂ", "ﾂﾞ", "ﾃ", "ﾃﾞ", "ﾄ", "ﾄﾞ",
//  "ﾅ", "ﾆ", "ﾇ", "ﾈ", "ﾉ", "ﾊ", "ﾊﾞ", "ﾊﾟ", "ﾋ", "ﾋﾞ", "ﾋﾟ", "ﾌ",
//  "ﾌﾞ", "ﾌﾟ", "ﾍ", "ﾍﾞ", "ﾍﾟ", "ﾎ", "ﾎﾞ", "ﾎﾟ", "ﾏ", "ﾐ", "ﾑ", "ﾒ",
//  "ﾓ", "ｬ", "ﾔ", "ｭ", "ﾕ", "ｮ", "ﾖ", "ﾗ", "ﾘ", "ﾙ", "ﾚ", "ﾛ", "ﾜ",
//  "ﾜ", "ｦ", "ﾝ", "ｳﾞ" };
//KANA_MAP_F2H = new TreeMap<String, String>();
//KANA_MAP_H2F = new TreeMap<String, String>();
//for (int i = 0; i < zenkakuKatakanaConvertiblHankakuKatakana.length; i++) {
//  KANA_MAP_F2H.put(
//      String.valueOf(zenkakuKatakanaConvertiblHankakuKatakana[i]),
//      hankakuKatakanaConvertiblZenkakuKatakana[i]);
//  KANA_MAP_H2F.put(
//      hankakuKatakanaConvertiblZenkakuKatakana[i],
//      String.valueOf(zenkakuKatakanaConvertiblHankakuKatakana[i]));
//}
///*
//* 「ヰ」、「ヱ」、「ヵ」、「ヶ」などの文字は半角が存在しないため、
//* 「ｲ」「ｴ」「ｶ」「ｹ」に割り当てる。→ 独自ルールなのでいったん取下げ
//*/
//KANA_MAP_F2H.put("ヰ", "ｲ");
//KANA_MAP_F2H.put("ヱ", "ｴ");
//KANA_MAP_F2H.put("ヵ", "ｶ");
//KANA_MAP_F2H.put("ヶ", "ｹ");
//    public static final String ZEN_KATAKANA_SEION = "アイウエオカキクケコサシスセソタチツテトナ二ヌネノハヒフヘホマミムメモヤユヨラリルレロワン";
//    /** 全角カタカナの濁音 */
//    private static final String ZEN_KATAKANA_DAKUON = "ガギグゲゴザジズゼゾダヂヅデドバパピブプベペボポヴ";
//    /** 全角カタカナ */
//    public static final String ZEN_KATAKANA = ZEN_KATAKANA_SEION
//        + ZEN_KATAKANA_DAKUON
//        + "ヲァィゥェォャュョッー";
//    /**
//     * カタカナの集合です。
//     */
//    public static final String KATA_KANA = "["
//        + ZEN_KATAKANA
//        + "ﾞﾟｦｧｨｩｪｫｬｭｮｯｰ"
//        + "ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ 　]";
//    public static final String KATA_KANA2 = "[０１２３４５６７８９"
//        + "、。，．・：；？！゛゜´｀¨^￣_々ー―‐／＼~∥|“”（）［］｛｝〈〉「」＋＊－÷＝≠≦≧※＠"
//        + "ヲァィゥェォャュョッー"
//        + ZEN_KATAKANA_SEION
//        + "ガギグゲゴザジズゼゾダヂヅデドバパピブプベペボポヴﾞﾟｦｧｨｩｪｫｬｭｮｯｰ"
//        + "ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ 　]";
//    /**
//     * カタカナと数字の集合です。
//     */
//    public static final String KATA_KANA_NUM = "[ヲァィゥェォャュョッー"
//        + ZEN_KATAKANA_SEION
//        + ZEN_KATAKANA_DAKUON
//        + "ﾞﾟｦｧｨｩｪｫｬｭｮｯｰｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ"
//        + StringSet.HANKAKU_NUM
//        + "]";
//  /**
//  * カタカナの集合
//  */
// public static final String KATA_KANA = "[ヲァィゥェォャュョッー"
//     + "アイウエオカキクケコサシスセソタチツテトナ二ヌネノハヒフヘホマミムメモヤユヨラリルレロワン"
//     + "ガギグゲゴザジズゼゾダヂヅデドバパピブプベペボポヴﾞﾟｦｧｨｩｪｫｬｭｮｯｰｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ 　]";
// public static final String KATA_KANA2 = "[０１２３４５６７８９"
//     + "、。，．・：；？！゛゜´｀¨^￣_々ー―‐／＼~∥|“”（）［］｛｝〈〉「」＋＊－÷＝≠≦≧※＠"
//     + "ヲァィゥェォャュョッーアイウエオカキクケコサシスセソタチツテトナ二ヌネノハヒフヘホマミムメモヤユヨラリルレロワン"
//     + "ガギグゲゴザジズゼゾダヂヅデドバパピブプベペボポヴﾞﾟｦｧｨｩｪｫｬｭｮｯｰｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ 　]";
//    public static final String[][] rule = {
//        // 0:半濁点
//        { "ﾊﾋﾌﾍﾎ", "パピプペポ" },
//        // 1:濁点
//        { "ｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾊﾋﾌﾍﾎｳ", "ガギグゲゴザジズゼゾダヂヅデドバビブベボヴ" },
//        // 2:カナ
//        {
//            "ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜｦﾝｰｧｨｩｪｫｬｭｮｯﾟﾞ･",
//            "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲンーァィゥェォャュョッ゜゛・", },
//        // 3:数字
//        { "0123456789", "０１２３４５６７８９" },
//        // 4:英大文字
//        { "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ" },
//        // 5:英小文字
//        { "abcdefghijklmnopqrstuvwxyz", "ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ" },
//        // 6:ASCII
//        { " !\"#$%&'()*+,/:;<=>?@[\\]^_{|}~",
//            "　！”＃＄％＆’（）＊＋，／：；＜＝＞？＠［￥］＾＿｛｜｝～" } };

//    /**
//     * 半角、全角変換用ルールです。
//     */
//    public static final String[][] RULE_HAN_ZEN = {
//        // 0:半濁点
//        { "ﾊﾋﾌﾍﾎ", "パピプペポ" },
//        // 1:濁点
//        { "ｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾊﾋﾌﾍﾎｳ", "ガギグゲゴザジズゼゾダヂヅデドバビブベボヴ" },
//        // 2:カナ
//        {
//            "ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜｦﾝｰｧｨｩｪｫｬｭｮｯﾟﾞ･",
//            "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲンーァィゥェォャュョッ゜゛・", },
//        // 3:数字
//        { "0123456789", "０１２３４５６７８９" },
//        // 4:英大文字
//        { "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ" },
//        // 5:英小文字
//        { "abcdefghijklmnopqrstuvwxyz", "ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ" },
//        // 6:ASCII
//        { " !\"#$%&'()*+,/:;<=>?@[\\]^_{|}~",
//            "　！”＃＄％＆’（）＊＋，／：；＜＝＞？＠［￥］＾＿｛｜｝～" } };
