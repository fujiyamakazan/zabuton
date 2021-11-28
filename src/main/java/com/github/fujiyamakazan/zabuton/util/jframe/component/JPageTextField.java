package com.github.fujiyamakazan.zabuton.util.jframe.component;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.wicket.model.Model;

/**
 * テキストフィールドのコンポーネントモデルです。
 */
public class JPageTextField extends JPageComponent<String> {
    private static final long serialVersionUID = 1L;
    private final JTextField textField;

    /**
     * コンストラクタです。
     */
    public JPageTextField(String label, Model<String> model) {
        super(model);
        textField = new JTextField(model.getObject());
        textField.setPreferredSize(new Dimension(300, 30));

        addJFrameComponent(new JLabel(label));
        addJFrameComponent(textField);
    }

    @Override
    public void updateModel() {
        model.setObject(textField.getText());
    }
}