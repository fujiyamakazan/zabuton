package com.github.fujiyamakazan.zabuton.jicket.component;

import java.awt.Font;

import javax.swing.JLabel;

import org.apache.wicket.model.Model;

public class JicketLabel extends JfPageComponent<String> {
    private static final long serialVersionUID = 1L;

    /**
     * コンストラクタです。
     */
    public JicketLabel(String text) {
        super(Model.of(text));
        JLabel jc = new JLabel(text);
        super.comps.add(jc);

        jc.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 20));
    }

    @Override
    public void setObject() {
        /* 処理なし */
    }

}