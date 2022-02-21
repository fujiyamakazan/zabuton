package com.github.fujiyamakazan.zabuton.util.jframe.component;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.wicket.model.Model;

/**
 * テキストフィールドのコンポーネントモデルです。
 */
public class JPageTextField extends JPageComponent<String> implements JPageInputComponent {
    private static final long serialVersionUID = 1L;
    private final JTextField textField;

    public JPageTextField(String label, Model<String> model) {
        this(label, model, null);
    }

    /**
     * コンストラクタです。
     */
    public JPageTextField(String label, Model<String> model, Integer length) {
        super(model);
        if (length != null) {
            this.textField = new JTextField(model.getObject(), length);
        } else  {
            this.textField = new JTextField(model.getObject());
        }

        this.textField.setPreferredSize(new Dimension(300, 30));

        addJFrameComponent(new JLabel(label));
        addJFrameComponent(this.textField);

    }

    @Override
    public void updateModel() {
        this.model.setObject(this.textField.getText());
    }

    @Override
    public void setTextFromModel() {
        this.textField.setText(getModel().getObject());
    }
}