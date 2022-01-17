package com.github.fujiyamakazan.zabuton.util.jframe.component;

import com.github.fujiyamakazan.zabuton.util.ThreadUtils;

/**
 * ラベルのコンポーネントモデルです。
 */
public class JPageDelayLabel extends JPageLabel {
    private static final long serialVersionUID = 1L;
    private String delayText;

    /**
     * 遅延文字列をセットするコンストラクタです。
     */
    public JPageDelayLabel(String text) {
        //super(text);
        super("");
        this.delayText = text;
    }

    /**
     * 遅延表示をします。
     */
    public void active() {

        for (Character c : delayText.toCharArray()) {
            String t = super.label.getText();
            t += c;
            super.label.setText(t);
            sound();
            //RakutenQuest.sound();
            ThreadUtils.sleep(100);
        }

    }

    protected void sound() {
        /* 拡張ポイント */
    }

}