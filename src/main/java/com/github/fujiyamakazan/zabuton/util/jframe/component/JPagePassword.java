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

        this.pw = new JPasswordField(model.getObject());
        this.pw.setPreferredSize(new Dimension(300, 30));
        this.pw.setVisible(!this.showText);

        this.text = new JTextField(model.getObject());
        this.text.setVisible(this.showText); // 初期非表示
        this.text.setPreferredSize(new Dimension(300, 20));

        JButton showPw = new JButton();
        showPw.setText("表示");
        showPw.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JPagePassword.this.showText) {
                    String input = JPagePassword.this.text.getText();
                    JPagePassword.this.text.setVisible(false);
                    JPagePassword.this.pw.setText(input);
                    JPagePassword.this.pw.setVisible(true);
                } else {
                    String input = String.valueOf(JPagePassword.this.pw.getPassword());
                    final Point location = JPagePassword.this.pw.getLocation();
                    final Dimension size = JPagePassword.this.pw.getSize();
                    JPagePassword.this.pw.setVisible(false);
                    JPagePassword.this.text.setText(input);
                    JPagePassword.this.text.setVisible(true);
                    JPagePassword.this.text.setLocation(location);
                    JPagePassword.this.text.setSize(size);
                }
                JPagePassword.this.showText = !JPagePassword.this.showText; // トグル
            }
        });

        addJFrameComponent(new JLabel(label));
        addJFrameComponent(this.pw);
        addJFrameComponent(this.text);
        addJFrameComponent(showPw);
    }

    @Override
    public void updateModel() {
        if (this.showText) {
            this.model.setObject(this.text.getText());
        } else {
            this.model.setObject(String.valueOf(this.pw.getPassword()));
        }
    }
}