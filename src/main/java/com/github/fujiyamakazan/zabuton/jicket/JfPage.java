package com.github.fujiyamakazan.zabuton.jicket;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Generics;

public abstract class JfPage {

    private final JFrame frame;
    private final JPanel panel;

    private final Object lock = new Object();

    private List<PageComponent<?>> components = Generics.newArrayList();

    public JfPage() {

        frame = new JFrame();
        frame.setLocation(20, 20);
        frame.setSize(600, 200);
        frame.setAlwaysOnTop(true); // 最前面

        panel = new JPanel();
        frame.add(panel);
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);

        onInitialize();

    }

    public void add(PageComponent<?>... componets) {

        JPanel p = new JPanel();
        panel.add(p);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        for (PageComponent<?> c : componets) {
            components.add(c);
            c.apendTo(p);
        }

    }

    public void show() {

        for (PageComponent<?> pc : components) {
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

    protected abstract class PageComponent<T extends Serializable> implements Serializable {
        private static final long serialVersionUID = 1L;

        protected final List<JComponent> comps = Generics.newArrayList();
        private final Model<T> model;

        public PageComponent(Model<T> model) {
            this.model = model;
        }

        public void apendTo(JPanel panel) {
            for (JComponent jc : getJComplenets()) {
                panel.add(jc);
            }
        }

        private final List<JComponent> getJComplenets() {
            return comps;
        }

        protected abstract void setObject();

    }

    protected class Label extends PageComponent<String> {
        private static final long serialVersionUID = 1L;

        public Label(String text) {
            super(Model.of(text));
            super.comps.add(new JLabel(text));
        }

        @Override
        protected void setObject() {
            /* 処理なし */
        }

    }

    protected class Text extends PageComponent<String> {
        private static final long serialVersionUID = 1L;
        private final JTextField textField;

        public Text(String label, Model<String> model) {
            super(model);
            textField = new JTextField(model.getObject());

            super.comps.add(new JLabel(label));
            super.comps.add(textField);
        }

        @Override
        protected void setObject() {
            super.model.setObject(textField.getText());
        }
    }

    protected class Password extends PageComponent<String> {
        private static final long serialVersionUID = 1L;

        boolean showText = false;
        private final JTextField text;
        private final JPasswordField pw;

        public Password(String label, Model<String> model) {
            super(model);

            super.comps.add(new JLabel(label));
            pw = new JPasswordField(model.getObject());
            pw.setVisible(!showText);
            super.comps.add(pw);
            text = new JTextField(model.getObject());
            text.setVisible(showText); // 初期非表示
            JButton showPw = new JButton();
            showPw.setText("表示");
            super.comps.add(pw);
            super.comps.add(text);
            super.comps.add(showPw);

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
        }

        @Override
        protected void setObject() {
            if (showText) {
                super.model.setObject(text.getText());
            } else {
                super.model.setObject(String.valueOf(pw.getPassword()));
            }
        }
    }

    protected class CheckBox extends PageComponent<Boolean> {
        private static final long serialVersionUID = 1L;
        private final JCheckBox jc;

        public CheckBox(String label, Model<Boolean> model) {
            super(model);
            jc = new JCheckBox(label, model.getObject());
            super.comps.add(jc);
        }

        @Override
        protected void setObject() {
            super.model.setObject(jc.isSelected());
        }
    }

    protected class Button extends PageComponent<String> {
        private static final long serialVersionUID = 1L;

        public Button(String label, Runnable work) {
            super(Model.of(label));
            JButton button = new JButton();
            button.setText(label);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (PageComponent<?> pc : components) {
                        pc.setObject();
                    }
                    work.run();

                    synchronized (lock) {
                        lock.notifyAll();
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

}
