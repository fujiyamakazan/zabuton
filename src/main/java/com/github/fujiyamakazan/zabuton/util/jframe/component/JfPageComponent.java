package com.github.fujiyamakazan.zabuton.util.jframe.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.jframe.JfApplication;
import com.github.fujiyamakazan.zabuton.util.jframe.JfPage;

public abstract class JfPageComponent<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final List<JComponent> comps = Generics.newArrayList();
    protected final Model<T> model;

    protected JfPage page;

    public JfPageComponent(Model<T> model) {
        this.model = model;
    }

    /**
     * 引数のパネルに自身を登録します。
     */
    public void apendTo(JPanel pLine) {
        JPanel p = new JPanel();
        pLine.add(p);
        p.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1)); // 余白
        //p.setBorder(BorderFactory.createLineBorder(Color.red));
        p.setBackground(Color.WHITE);

        if (getJComplenets().size() == 1) {
            p.setLayout(new BorderLayout());
        } else {
            p.setLayout(new FlowLayout(FlowLayout.LEFT));
        }

        for (JComponent jc : getJComplenets()) {
            p.add(jc);
        }
    }

    public final List<JComponent> getJComplenets() {
        return comps;
    }

    public abstract void setObject();

    public void setPage(JfPage page) {
        this.page = page;
    }

    protected JfApplication getApplication() {
        return this.page.getApplication();
    }


}