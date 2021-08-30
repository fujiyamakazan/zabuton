package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;

public class KeyValue<K, V> implements Serializable {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    private K key;
    private V value;

    public KeyValue() {
    }

    public KeyValue(K key, V value) {
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
