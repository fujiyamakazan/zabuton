package com.github.fujiyamakazan.zabuton.util.string;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.TestCase;

/**
 * @author fujiyama
 */
public class StringulTest extends TestCase {

    //    /**
    //     * 3点リーダーによる省略
    //     */
    //    public void testAbbreviate() {
    //        assertEquals(StringUtil.abbreviate("test", 3), "t...");
    //    }

    //    /**
    //     *
    //     */
    //    public void testContentZenkaku() {
    //        assertFalse(StringUtil.isHankakuAlphaNum("あ"));
    //        assertFalse(StringUtil.isHankakuAlphaNum("ＡA"));
    //        assertFalse(StringUtil.isHankakuAlphaNum("あ "));
    //        assertFalse(StringUtil.isHankakuAlphaNum(" あ"));
    //        assertFalse(StringUtil.isHankakuAlphaNum("abc  あ"));
    //        assertFalse(StringUtil.isHankakuAlphaNum("あabc   efg"));
    //        assertFalse(StringUtil.isHankakuAlphaNum("!\"#$%&'(あ)=~|'\\"));
    //
    //        assertTrue(StringUtil.isHankakuAlphaNum(null));
    //        assertTrue(StringUtil.isHankakuAlphaNum(""));
    //        assertTrue(StringUtil.isHankakuAlphaNum(" "));
    //        assertTrue(StringUtil.isHankakuAlphaNum(" abc"));
    //        assertTrue(StringUtil.isHankakuAlphaNum("abc  "));
    //        assertTrue(StringUtil.isHankakuAlphaNum("abc   efg"));
    //        assertTrue(StringUtil.isHankakuAlphaNum("!\"#$%&'()=~|'\\"));
    //    }

    /**
     * <pre>
     * 一般的な入力値チェックを想定したテストケース
     * </pre>
     */
    public void testValidateSample() {
        Map<String, String> errs;

        errs = validateSample(
            "John Fitzgerald Kennedy", // 任意入力。シンングルバイトのみ。
            "ジョン・F・ケネディ", // 必須入力。マルチバイト混在OK。
            "ジョン・フィッツジェラルド・ケネディ" // 任意入力。マルチバイトのみ。
        );
        printerr(errs);
        assertTrue("正しい入力値", errs.isEmpty());

        errs = validateSample(
            "あ", // 任意入力。シンングルバイトのみ。
            "", // 必須入力。マルチバイト混在OK。
            "A" // 任意入力。マルチバイトのみ。
        );
        printerr(errs);
        assertTrue("不正な入力値", errs.size() == 3);

    }

    private static Map<String, String> validateSample(String nameSingle, String name, String nameMulti) {
        Map<String, String> errs = new TreeMap<String, String>();
        /* 任意入力。シンングルバイトのみ。*/
        if (Stringul.isEmpty(nameSingle)) {
            /* 任意入力なので、処理なし */
        } else {
            if (!Stringul.isSinglebyte(nameSingle, Stringul.ENCODE_UTF_8)) {
                errs.put("nameSingle", "半角文字のみで入力して下さい。全角文字は利用できません。");
                // 厳密にいえばSingleByte≠半角である。ユーザの利便性を考慮したメッセージ。
            }

        }
        /* 必須入力。マルチバイト混在OK。 */
        if (Stringul.isEmpty(name)) {
            errs.put("name", "入力必須です。");
        } else {
            /* 処理なし */
        }
        /* 任意入力。マルチバイトのみ。*/
        if (Stringul.isEmpty(nameMulti)) {
            /* 任意入力なので、処理なし */
        } else {
            if (!Stringul.isMultibyte(nameMulti, Stringul.ENCODE_UTF_8)) {
                errs.put("nameMulti", "全角文字のみで入力して下さい。半角文字は利用できません。");
                // 厳密にいえばSingleByte≠半角である。ユーザの利便性を考慮したメッセージ。
            }
        }
        return errs;
    }

    private static void printerr(Map<String, String> errs) {
        if (!errs.isEmpty()) {
            Set<Map.Entry<String, String>> entrys = errs.entrySet();
            for (Map.Entry<String, String> entry : entrys) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
        }
    }
}
