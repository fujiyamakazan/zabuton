package com.github.fujiyamakazan.zabuton.util.jframe;

import java.io.Serializable;

public class JChoiceElement<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String label;
    private final T obj;

    public JChoiceElement(String label, T obj) {
        this.label = label;
        this.obj = obj;
    }

    public String getLabel() {
        return this.label;
    }

    public T getObject() {
        return this.obj;
    }
}