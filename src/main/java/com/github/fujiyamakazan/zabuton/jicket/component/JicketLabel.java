package com.github.fujiyamakazan.zabuton.jicket.component;

import javax.swing.JLabel;

import org.apache.wicket.model.Model;

public class JicketLabel extends JfPageComponent<String> {
    private static final long serialVersionUID = 1L;

    public JicketLabel(String text) {
        super(Model.of(text));
        super.comps.add(new JLabel(text));
    }

    @Override
    protected void setObject() {
        /* 処理なし */
    }

}