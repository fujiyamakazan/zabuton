package com.github.fujiyamakazan.zabuton.util.jframe.component;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import com.github.fujiyamakazan.zabuton.util.jframe.JPageAction;

/**
 * リンクのコンポーネントモデルです。
 */
public class JPageLink extends JPageButton {
    private static final long serialVersionUID = 1L;

    /**
     * コンストラクタです。
     */
    public JPageLink(String label, JPageAction action) {
        super(label, action);
    }

    @Override
    protected void addDecoration(JButton button) {
        button.setText("<HTML><FONT color=\\\"#000099\\\"><U>" + button.getText() + "</U></FONT></HTML>\"");
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder());
    }


}