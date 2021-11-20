package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;

public class KeyValue implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String key;

    private String value;

    public KeyValue(String key) {
        this.key = key;
    }

    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
