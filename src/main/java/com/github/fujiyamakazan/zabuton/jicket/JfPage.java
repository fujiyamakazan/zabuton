package com.github.fujiyamakazan.zabuton.jicket;

import java.awt.Font;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.jicket.component.JfPageComponent;

public abstract class JfPage {

    private final JFrame frame;
    private final JPanel panelMain;

    private final Object lock = new Object();

    private List<JfPageComponent<?>> components = Generics.newArrayList();

    public JfPage() {

        frame = new JFrame();
        frame.setLocation(20, 20);
        frame.setSize(600, 300);
        frame.setAlwaysOnTop(true); // 最前面

        panelMain = new JPanel();
        frame.add(panelMain);
        BoxLayout layout = new BoxLayout(panelMain, BoxLayout.Y_AXIS);
        panelMain.setLayout(layout);

        onInitialize();

    }

    public void add(JfPageComponent<?>... componets) {

        JPanel pLine = new JPanel();
        panelMain.add(pLine);
        pLine.setLayout(new BoxLayout(pLine, BoxLayout.X_AXIS));
        for (JfPageComponent<?> c : componets) {
            components.add(c);
            c.setPage(this);
            c.apendTo(pLine);
        }

    }

    public void show() {

        for (JfPageComponent<?> pc : components) {
            for (JComponent jc : pc.getJComplenets()) {
                jc.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 20));
            }
        }
        frame.setVisible(true);
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        frame.setVisible(false);
        frame.dispose();

    }

    protected abstract void onInitialize();

    public Object getLock() {
        return lock;
    }

    public List<JfPageComponent<?>> getComponents() {
        return components;
    }



}
