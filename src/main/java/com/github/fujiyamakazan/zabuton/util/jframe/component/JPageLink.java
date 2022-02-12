package com.github.fujiyamakazan.zabuton.util.jframe.component;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import org.apache.wicket.model.Model;

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

    /**
     * コンストラクタです。
     */
    public JPageLink(String label, Model<Boolean> model) {
        super(label, model);
    }

    @Override
    protected void addDecoration(JButton button) {
        button.setText("<HTML><FONT color=\"" + getColor() + "\"><U>"
            + button.getText() + "</U></FONT></HTML>\"");
        //button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder());
    }

    protected String getColor() {
        return "#000099";
    }

}