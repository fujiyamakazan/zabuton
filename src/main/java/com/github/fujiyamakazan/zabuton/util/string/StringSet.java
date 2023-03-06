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
 * ここで示す「半角」「全角」は一般的な認識に基づくものです。
 * 必ずしも半角がシングルバイトとは限りません。「半角」「全角」は使用フォントの字体(glyph)の影響を受けます。
 *
 * 「数字」とはアラビア数字のことです。
 * 「英字」「アルファベット」 とはラテン文字のことです。
 * 「小文字」とは英字のlower case(ローワーケース)のことです。
 * 「大文字」とは英字のupper case(アッパーケース)のことです。
 *
 */
public class StringSet implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StringSet.class);

    /** 半角数字です。（intでは48～57）*/
    public static final String HANKAKU_NUM = "0123456789";

    /** 半角英小字です。 */
    private static final String HANKAKU_ALPHA_SMALL = "abcdefghijklmnopqrstuvwxyz";

    /** 半角英大字です。 */
    private static final String HANKAKU_ALPHA_BIG = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /** ASCIIに含まれる半角記号の一部です。（intでは33～47）*/
    private static final String ASCII_KIGO_1 = "!\"#$%&'()*+,-./";

    /** ASCIIに含まれる半角記号の一部です。（intでは58～64）*/
    private static final String ASCII_KIGO_2 = ":;<=>?@";

    /** ASCIIに含まれる半角記号の一部です。（intでは91～96）*/
    private static final String ASCII_KIGO_3 = "[\\]^_`";

    /** ASCIIに含まれる半角記号の一部です。（intでは123～126）*/
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

    /** JIS_X_0201に含まれる半角カタカナの一部です。 */
    private static final String HANKAKU_KATAKANA1 = "ｦｧｨｩｪｫｬｭｮｯ";

    /** JIS_X_0201に含まれる半角カタカナの一部です。 */
    private static final String HANKAKU_KATAKANA2 = "ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜﾝ";

    /**
     * 半角カタカナです。
     * ASCII文字集合のうち、JIS X 0208(JIS基本漢字)の５区（カタカナ）に対応する文字があるもの。
     */
    public static final String HANKAKU_KATAKANA = HANKAKU_KATAKANA2 + HANKAKU_KATAKANA1;

    /** JIS_X_0201に含まれる半角記号の一部です。 */
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

    /** 半角英字です。 */
    public static final String HANKAKU_ALPHA = HANKAKU_ALPHA_SMALL + HANKAKU_ALPHA_BIG;

    /** ASCIIに含まれる半角記号です。 */
    private static final String ASCII_KIGO = ASCII_KIGO_1 + ASCII_KIGO_2 + ASCII_KIGO_3 + ASCII_KIGO_4;

    /**
     * JIS X 0201に含まれる記号です。
     */
    public static String HANKAKU_KIGO = ASCII_KIGO + HANKAKU_KIGO_1 + HANKAKU_KIGO_2 + HANKAKU_KIGO_3;

    /**
     * JIS X 0201に含まれる半角英数カナと記号です。
     */
    public static String HANKAKU = HANKAKU_NUM + HANKAKU_ALPHA + HANKAKU_KATAKANA + HANKAKU_KIGO;

    /** ひらがなです。（JIS X 0208 4区）*/
    public static String HIRAGANA = "ぁあぃいぅうぇえぉおかがきぎく"
        + "ぐけげこごさざしじすずせぜそぞた"
        + "だちぢっつづてでとどなにぬねのは"
        + "ばぱひびぴふぶぷへべぺほぼぽまみ"
        + "むめもゃやゅゆょよらりるれろゎわ"
        + "ゐゑをん";

    /** カタカナです。（JIS X 0208 5区）*/
    public static String KATAKANA = "ァアィイゥウェエォオカガキギク"
        + "グケゲコゴサザシジスズセゼソゾタ"
        + "ダチヂッツヅテデトドナニヌネノハ"
        + "バパヒビピフブプヘベペホボポマミ"
        + "ムメモャヤュユョヨラリルレロヮワ"
        + "ヰヱヲンヴヵヶ";

    /**
     * JIS X 0208 からサンプリングした文字列です。
     * 単体テストで使用します。
     */
    private static final String SAMPLE_JIS_X_0208 = ""
        + "亜" // 第1水準漢字(16区～47区)
        + "弌" // 第2水準漢字(48区～84区)
        + "♪" // 記号、英数字、かな(01区～08区)
        + "①" // NEC特殊文字(13区)
        + "髙彅﨑" // NEC選定IBM拡張文字(89区～92区)
        + "∩" // 重複定義(2区と13区)
        + "∵" // 重複定義(2区と13区)
        + "￢" // 重複定義(2区と92区と115区)
        + "㈱" // 重複定義(13区と115区)
        + "〜" // 波ダッシュ (Cp943CとMS932で解釈が変わる文字)
    ;

    /**
     * JIS X 0208に含まれない（Windows-31Jに含まれない）文字をサンプリングしたものです。
     */
    private static final String SAMPLE_UNICODE = "你";

    /**
     * 字形がハイフンに近い文字です。
     * -˗ᅳ‐‑‒–—―⁃⁻−▬─━➖ーㅡ﹘﹣－ｰ 
     * ハイフンの名寄せに使用します。
     */
    public static final String LIKE_HYPHEN = initLikeHyphen();

    private static String initLikeHyphen() {
        int[] intLineHyphen = new int[] {
            45, // 半角マイナス
            727,
            4467,
            8208,
            8209,
            8210,
            8211,
            8212,
            8213,
            8259,
            8315,
            8722,
            9644,
            9472,
            9473,
            10134,
            12540,
            12641,
            65112,
            65123,
            65293,
            65392,
            5760,
        };
        StringBuilder sb = new StringBuilder();
        for (int i : intLineHyphen) {
            sb.append((char) i);
        }
        return sb.toString();
    }

    /**
     * 全角カタカナと半角カタカナの対応表です。
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
        //list.add(Pair.of("ヰ","ｲ")); 客観性に欠ける
        //list.add(Pair.of("ヱ","ｴ")); 客観性に欠ける
        //list.add(Pair.of("ヵ","ｶ")); 客観性に欠ける
        //list.add(Pair.of("ヶ","ｹ")); 客観性に欠ける
        return list;
    }

    /*
     * JIS X 0208コード表
     * https://www.asahi-net.or.jp/~ax2s-kmtn/ref/jisx0208.html
     * ５区のうち、対応するハンカクカナがJIS_X_0201にないもの
     * 　ガギグゲゴ
     * 　ザジズゼゾ
     * 　ダヂヅデド
     * 　バパビピブプベペボポ
     * 　ヮヰヱヴヵヶ
     */

    /*
     * その他の文字に関する覚え書き
     */
    // -    U+002D  ハイフンマイナス
    // ‑    U+2011 ノンブレーキングハイフン
    // £    U+00A3 ポンド記号
    // ￡   U+FFE1 全角ポンド記号
    // ‾    U+203E 全角オーバーライン
    // ‐   U+2010 ハイフン
    // ヰヱヵヶ 対応する半角文字が無い全角カタカナ

    private static HashMap<String, String> getAll() {
        HashMap<String, String> all = Generics.newHashMap();
        all.put("半角数字", HANKAKU_NUM);
        all.put("半角英字", HANKAKU_ALPHA);
        all.put("半角カタカナ", HANKAKU_KATAKANA);
        all.put("半角文字", HANKAKU);
        all.put("ひらがな", HIRAGANA);
        all.put("カタカナ", KATAKANA);
        all.put("JIS X 0208 のサンプル", SAMPLE_JIS_X_0208);
        all.put("JIS X 0208 以外のサンプル", SAMPLE_UNICODE);
        all.put("字形がハイフンに近い文字", LIKE_HYPHEN);
        return all;
    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        LOGGER.debug("種別ごとに出力");
        Map<String, String> map = getAll();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : map.entrySet()) {
            LOGGER.debug(e.getKey() + ":" + e.getValue());
            sb.append(e.getValue());
        }
        String all = sb.toString();
        //String all = HANKAKU;

        LOGGER.debug("intと16進表記で出力");
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
    }
}
