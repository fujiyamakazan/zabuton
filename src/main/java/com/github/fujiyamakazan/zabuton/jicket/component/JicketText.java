package com.github.fujiyamakazan.zabuton.jicket.component;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.wicket.model.Model;

public class JicketText extends JfPageComponent<String> {
    private static final long serialVersionUID = 1L;
    private final JTextField textField;

    public JicketText(String label, Model<String> model) {
        super(model);
        textField = new JTextField(model.getObject());
        textField.setPreferredSize(new Dimension(300, 30));

        super.comps.add(new JLabel(label));
        super.comps.add(textField);
    }

    @Override
    protected void setObject() {
        super.model.setObject(textField.getText());
    }
}