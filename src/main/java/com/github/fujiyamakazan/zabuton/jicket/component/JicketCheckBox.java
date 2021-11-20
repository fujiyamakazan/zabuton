package com.github.fujiyamakazan.zabuton.jicket.component;

import java.awt.Color;

import javax.swing.JCheckBox;

import org.apache.wicket.model.Model;

public class JicketCheckBox extends JfPageComponent<Boolean> {
    private static final long serialVersionUID = 1L;
    private final JCheckBox jc;

    /**
     * コンストラクタです。
     */
    public JicketCheckBox(String label, Model<Boolean> model) {
        super(model);
        jc = new JCheckBox(label, model.getObject());
        jc.setBackground(Color.WHITE);
        super.comps.add(jc);
    }

    @Override
    public void setObject() {
        super.model.setObject(jc.isSelected());
    }
}