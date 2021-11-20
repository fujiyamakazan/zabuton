package com.github.fujiyamakazan.zabuton.jicket.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.apache.wicket.model.Model;

import com.github.fujiyamakazan.zabuton.jicket.JfPage;

/** JicketButton */
public class JicketButton extends JfPageComponent<String> {
    private static final long serialVersionUID = 1L;

    public JicketButton(final JfPage page, final String label, final Runnable work) {
        super(Model.of(label));
        JButton button = new JButton();
        button.setText(label);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (JfPageComponent<?> pc : page.getComponents()) {
                    pc.setObject();
                }
                work.run();

                synchronized (getLock()) {
                    getLock().notifyAll();
                }
            }
        });
        super.comps.add(button);
    }

    @Override
    protected void setObject() {
        /* 処理なし */
    }

}