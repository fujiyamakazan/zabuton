package com.github.fujiyamakazan.zabuton.util.string;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

public class HyphenUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HyphenUtils.class);

    /*
     * 区切り文字としてのハイフンに統一しても差し支えない文字の一覧。（マイルール）
     * 長音「ー」「ｰ」は除外
     */
    public static final String LIST = "-˗ᅳ‐‑‒–—―⁃⁻−▬─━➖ㅡ﹘﹣－";

    /*
     * 参考
     * 　ハイフンに似ている横棒を全て統一する
     * 　https://qiita.com/non-caffeine/items/77360dda05c8ce510084
     */

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        /*
         * 定数の検査
         */
        List<Character> buffer = Generics.newArrayList();
        for (Character c : LIST.toCharArray()) {
            log.debug(c + "[" + ((int) c) + "]");
            if (buffer.contains(c)) {
                throw new RuntimeException("登録済みです。[" + c + "]");
            }
            buffer.add(c);
        }

        /*
         * 名寄せ
         */
        String standard = standardization("-˗ᅳ‐‑‒–—―⁃⁻−▬─━➖ㅡ﹘﹣－");
        System.out.println(standard);

    }

    /**
     * ハイフンを標準化します。
     */
    public static String standardization(String string) {
        for (Character c : LIST.toCharArray()) {
            log.debug(c + "[" + ((int) c) + "]");
            string = string.replaceAll(String.valueOf(c), String.valueOf(LIST.charAt(0)));
        }
        return string;
    }

}
