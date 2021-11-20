package com.github.fujiyamakazan.zabuton.jicket.component;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.apache.wicket.model.Model;

public class JicketLink extends JfPageComponent<String> {
    private static final long serialVersionUID = 1L;

    public JicketLink(String label, Runnable work) {
        super(Model.of(label));
        JButton button = new JButton();
        button.setText("<HTML><FONT color=\\\"#000099\\\"><U>" + label + "</U></FONT></HTML>\"");
        button.setBackground(Color.WHITE);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                work.run();
            }
        });
        super.comps.add(button);
    }

    @Override
    protected void setObject() {
        /* 処理なし */
    }

}