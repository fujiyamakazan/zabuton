package com.github.fujiyamakazan.zabuton.jicket.component;

import javax.swing.JCheckBox;

import org.apache.wicket.model.Model;

public class JicketCheckBox extends JfPageComponent<Boolean> {
    private static final long serialVersionUID = 1L;
    private final JCheckBox jc;

    public JicketCheckBox(String label, Model<Boolean> model) {
        super(model);
        jc = new JCheckBox(label, model.getObject());
        super.comps.add(jc);
    }

    @Override
    public void setObject() {
        super.model.setObject(jc.isSelected());
    }
}