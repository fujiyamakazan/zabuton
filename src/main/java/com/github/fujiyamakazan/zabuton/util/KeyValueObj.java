package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;

public class KeyValueObj<K, V> implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    private K key;
    private V value;

    public KeyValueObj() {
    }

    public KeyValueObj(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

}
