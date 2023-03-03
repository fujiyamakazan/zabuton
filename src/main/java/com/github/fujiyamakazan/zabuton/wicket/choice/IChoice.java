package com.github.fujiyamakazan.zabuton.wicket.choice;

import java.io.Serializable;

/**
 * 選択肢に使うインターフェースです。
 *
 * 列挙型にも使えるよう、インターフェースとしました。
 * WicketのDropDownChoiceやRadioChoiceで使用できるよう、Serializableを継承します。
 *
 * @author fujiyama
 */
public interface IChoice extends Serializable {

    /**
     * 値を返します。
     */
    String getValue();

    /**
     * 表示用の文字列を返します。
     */
    String getDisplay();

}
