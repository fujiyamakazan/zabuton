package com.github.fujiyamakazan.zabuton.util.jframe.component;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.wicket.model.Model;

/**
 * パスワード入力のコンポーネントモデルです。
 */
public class JPagePassword extends JPageComponent<String> {
    private static final long serialVersionUID = 1L;

    boolean showText = false;
    private final JTextField text;
    private final JPasswordField pw;

    /**
     * コンストラクタです。
     */
    public JPagePassword(String label, Model<String> model) {
        super(model);

        pw = new JPasswordField(model.getObject());
        pw.setPreferredSize(new Dimension(300, 30));
        pw.setVisible(!showText);

        text = new JTextField(model.getObject());
        text.setVisible(showText); // 初期非表示
        text.setPreferredSize(new Dimension(300, 20));

        JButton showPw = new JButton();
        showPw.setText("表示");
        showPw.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showText) {
                    String input = text.getText();
                    text.setVisible(false);
                    pw.setText(input);
                    pw.setVisible(true);
                } else {
                    String input = String.valueOf(pw.getPassword());
                    final Point location = pw.getLocation();
                    final Dimension size = pw.getSize();
                    pw.setVisible(false);
                    text.setText(input);
                    text.setVisible(true);
                    text.setLocation(location);
                    text.setSize(size);
                }
                showText = !showText; // トグル
            }
        });

        addJFrameComponent(new JLabel(label));
        addJFrameComponent(pw);
        addJFrameComponent(text);
        addJFrameComponent(showPw);
    }

    @Override
    public void updateModel() {
        if (showText) {
            model.setObject(text.getText());
        } else {
            model.setObject(String.valueOf(pw.getPassword()));
        }
    }
}