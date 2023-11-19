package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * KeyとValueをペアで保持するオブジェクト。
 *
 * Pair&lt;Object, Object&gt;へ移行します。
 */
public class KeyValueObj<K, V> implements Serializable {
    private static final long serialVersionUID = 1L;

    //private K key;
    //private V value;

    //public KeyValueObj() {
    //}

    private final Pair<K, V> keyValue;

    /**
     * コンストラクタです。
     */
    public KeyValueObj(K key, V value) {
        //this.key = key;
        //this.value = value;
        this.keyValue = MutablePair.of(key, value);
    }

    /**
     * コンストラクタです。valueを指定しません。
     */
    public KeyValueObj(K key) {
        this.keyValue = MutablePair.of(key, null);
    }

    public K getKey() {
        //return this.key;
        return this.keyValue.getKey();
    }

    //public void setKey(K key) {
    //    this.key = key;
    //}

    public V getValue() {
        //return this.value;
        return this.keyValue.getValue();
    }

    public void setValue(V value) {
        //this.value = value;
        this.keyValue.setValue(value);
    }

}
