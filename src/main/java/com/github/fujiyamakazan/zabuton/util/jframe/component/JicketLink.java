package com.github.fujiyamakazan.zabuton.util.jframe.component;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import org.apache.wicket.model.Model;

public class JicketLink extends JfPageComponent<String> {
    private static final long serialVersionUID = 1L;

    /**
     * コンストラクタです。
     */
    public JicketLink(String label, Runnable work) {
        super(Model.of(label));
        JButton button = new JButton();
        button.setText("<HTML><FONT color=\\\"#000099\\\"><U>" + label + "</U></FONT></HTML>\"");
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                work.run();
            }
        });
        super.comps.add(button);
    }

    @Override
    public void setObject() {
        /* 処理なし */
    }

}