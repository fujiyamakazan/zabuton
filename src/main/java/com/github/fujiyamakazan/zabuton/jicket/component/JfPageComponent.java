package com.github.fujiyamakazan.zabuton.jicket.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.jicket.JfPage;

public abstract class JfPageComponent<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final List<JComponent> comps = Generics.newArrayList();
    protected final Model<T> model;

    protected JfPage page;

    public JfPageComponent(Model<T> model) {
        this.model = model;
    }

    public void apendTo(JPanel pLine) {
        JPanel p = new JPanel();
        pLine.add(p);
        p.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1)); // 余白
        //p.setBorder(BorderFactory.createLineBorder(Color.red));

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

    protected abstract void setObject();

    public void setPage(JfPage page) {
        this.page = page;
    }

    protected Object getLock() {
        return page.getLock();
    }


}