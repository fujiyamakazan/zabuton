package com.github.fujiyamakazan.zabuton.util.string;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.TestCase;

public class StringInspectionTest extends TestCase {

    /**
     * <pre>
     * 一般的な入力値チェックを想定したテストケースです。
     * </pre>
     */
    public void testValidateSample() {
        //      String nameSingle = "あ";//"John Fitzgerald Kennedy";
        //      String name = "";//"ジョン・F・ケネディ"; // 必須入力。マルチバイト混在OK。
        //      String nameMulti = "A";//ジョン・フィッツジェラルド・ケネディ"; // 任意入力。マルチバイトのみ。

        Map<String, String> errs = validateSample(
            "John Fitzgerald Kennedy", // 任意入力。シンングルバイトのみ。
            "ジョン・F・ケネディ", // 必須入力。マルチバイト混在OK。
            "ジョン・フィッツジェラルド・ケネディ" // 任意入力。マルチバイトのみ。
        );
        printerr(errs);
        assertTrue("正しい入力値", errs.isEmpty());

    }

    /**
     * <pre>
     * 一般的な入力値チェックを想定したテストケース
     * </pre>
     */
    public void testValidateSample2() {
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


    private static Map<String, String> validateSample2(String nameSingle, String name, String nameMulti) {
        Map<String, String> errs = new TreeMap<String, String>();
        /* 任意入力。シンングルバイトのみ。*/
        if (StringInspection.isEmpty(nameSingle)) {
            /* 任意入力なので、処理なし */
        } else {
            if (!StringInspection.isSinglebyte(nameSingle, Stringul.ENCODE_UTF_8)) {
                errs.put("nameSingle", "半角文字のみで入力して下さい。全角文字は利用できません。");
                // 厳密にいえばSingleByte≠半角である。ユーザの利便性を考慮したメッセージ。
            }

        }
        /* 必須入力。マルチバイト混在OK。 */
        if (StringInspection.isEmpty(name)) {
            errs.put("name", "入力必須です。");
        } else {
            /* 処理なし */
        }
        /* 任意入力。マルチバイトのみ。*/
        if (StringInspection.isEmpty(nameMulti)) {
            /* 任意入力なので、処理なし */
        } else {
            if (!StringInspection.isMultibyte(nameMulti, Stringul.ENCODE_UTF_8)) {
                errs.put("nameMulti", "全角文字のみで入力して下さい。半角文字は利用できません。");
                // 厳密にいえばSingleByte≠半角である。ユーザの利便性を考慮したメッセージ。
            }
        }
        return errs;
    }

    private static Map<String, String> validateSample(String nameSingle, String name, String nameMulti) {
        Map<String, String> errs = new TreeMap<String, String>();
        /* 任意入力。シンングルバイトのみ。*/
        if (StringInspection.isEmpty(nameSingle)) {
            /* 任意入力なので、処理なし */
        } else {
            if (!StringInspection.isSinglebyte(nameSingle, Stringul.ENCODE_UTF_8)) {
                errs.put("nameSingle", "半角文字のみで入力して下さい。全角文字は利用できません。");
                // 厳密にいえばSingleByte≠半角である。ユーザの利便性を考慮したメッセージ。
            }

        }
        /* 必須入力。マルチバイト混在OK。 */
        if (StringInspection.isEmpty(name)) {
            errs.put("name", "入力必須です。");
        } else {
            /* 処理なし */
        }
        /* 任意入力。マルチバイトのみ。*/
        if (StringInspection.isEmpty(nameMulti)) {
            /* 任意入力なので、処理なし */
        } else {
            if (!StringInspection.isMultibyte(nameMulti, Stringul.ENCODE_UTF_8)) {
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

    //    public void testContentZenkaku() {
    //        assertTrue(StringInspection.containsZenkakuSample("あ"));
    //        assertTrue(StringInspection.containsZenkakuSample("ＡA"));
    //        assertTrue(StringInspection.containsZenkakuSample("あ "));
    //        assertTrue(StringInspection.containsZenkakuSample(" あ"));
    //        assertTrue(StringInspection.containsZenkakuSample("abc  あ"));
    //        assertTrue(StringInspection.containsZenkakuSample("あabc   efg"));
    //        assertTrue(StringInspection.containsZenkakuSample("!\"#$%&'(あ)=~|'\\"));
    //
    //        assertFalse(StringInspection.containsZenkakuSample(null));
    //        assertFalse(StringInspection.containsZenkakuSample(""));
    //        assertFalse(StringInspection.containsZenkakuSample(" "));
    //        assertFalse(StringInspection.containsZenkakuSample(" abc"));
    //        assertFalse(StringInspection.containsZenkakuSample("abc  "));
    //        assertFalse(StringInspection.containsZenkakuSample("abc   efg"));
    //        assertFalse(StringInspection.containsZenkakuSample("!\"#$%&'()=~|'\\"));
    //    }
//    /**
//    *
//    */
//   public void testContentZenkaku() {
//       assertTrue(StringInspection.containsZenkakuSample("あ"));
//       assertTrue(StringInspection.containsZenkakuSample("ＡA"));
//       assertTrue(StringInspection.containsZenkakuSample("あ "));
//       assertTrue(StringInspection.containsZenkakuSample(" あ"));
//       assertTrue(StringInspection.containsZenkakuSample("abc  あ"));
//       assertTrue(StringInspection.containsZenkakuSample("あabc   efg"));
//       assertTrue(StringInspection.containsZenkakuSample("!\"#$%&'(あ)=~|'\\"));
//
//       assertFalse(StringInspection.containsZenkakuSample(null));
//       assertFalse(StringInspection.containsZenkakuSample(""));
//       assertFalse(StringInspection.containsZenkakuSample(" "));
//       assertFalse(StringInspection.containsZenkakuSample(" abc"));
//       assertFalse(StringInspection.containsZenkakuSample("abc  "));
//       assertFalse(StringInspection.containsZenkakuSample("abc   efg"));
//       assertFalse(StringInspection.containsZenkakuSample("!\"#$%&'()=~|'\\"));
//   }




}
