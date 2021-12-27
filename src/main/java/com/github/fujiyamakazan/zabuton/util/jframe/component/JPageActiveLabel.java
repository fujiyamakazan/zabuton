package com.github.fujiyamakazan.zabuton.util.jframe.component;

import com.github.fujiyamakazan.zabuton.rakutenquest.RakutenQuest;
import com.github.fujiyamakazan.zabuton.util.ThreadUtils;

/**
 * ラベルのコンポーネントモデルです。
 */
public class JPageActiveLabel extends JPageLabel {
    private static final long serialVersionUID = 1L;

    public JPageActiveLabel(String text) {
        super(text);
    }


    public void atvie() {
        String text = super.label.getText();
        super.label.setText("");

        for (Character c: text.toCharArray()) {
            String t = super.label.getText();
            t += c;
            super.label.setText(t);
            RakutenQuest.sound();
            ThreadUtils.sleep(100);
        }


    }

}