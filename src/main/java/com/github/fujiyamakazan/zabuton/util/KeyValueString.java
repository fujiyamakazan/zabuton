package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;

public class KeyValueString implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String key;

    private String value;

    public KeyValueString(String key) {
        this.key = key;
    }

    public KeyValueString(String key, String value) {
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
