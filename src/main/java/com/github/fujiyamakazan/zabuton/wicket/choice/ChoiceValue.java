package com.github.fujiyamakazan.zabuton.wicket.choice;

/**
 * 選択肢に使うオブジェクトです。
 *
 * WicketのDropDownChoiceやRadioChoiceで使用できるよう、IChoiceを実装します。
 *
 * @author fujiyama
 */
public class ChoiceValue implements IChoice {
    private static final long serialVersionUID = 1L;
    private final String value;
    private final String display;

    public ChoiceValue(String value, String display) {
        this.value = value;
        this.display = display;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDisplay() {
        return display;
    }

    @Override
    public String toString() {
        return getValue() + "(" + getDisplay() + ")";
    }
}