package com.github.fujiyamakazan.zabuton.util.jframe.component;

import java.awt.Color;

import javax.swing.JCheckBox;

import org.apache.wicket.model.Model;

/**
 * チェックボックスのコンポーネントモデルです。
 */
public class JPageCheckBox extends JPageComponent<Boolean> {
    private static final long serialVersionUID = 1L;
    private final JCheckBox jc;

    /**
     * コンストラクタです。
     */
    public JPageCheckBox(String label, Model<Boolean> model) {
        super(model);
        this.jc = new JCheckBox(label, model.getObject());
        this.jc.setBackground(Color.WHITE);
        addJFrameComponent(this.jc);
    }

    @Override
    public void updateModel() {
        getModel().setObject(this.jc.isSelected());
    }
}