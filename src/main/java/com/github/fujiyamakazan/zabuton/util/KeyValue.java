package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;

/**
 * KeyとValueをペアで保持するオブジェクト。保持する値はStringに限定します。
 *
 * Pair&lt;String, String&gt;へ移行します。
 */
public class KeyValue extends KeyValueObj<String, String> implements Serializable {
    private static final long serialVersionUID = 1L;

    //private String key;

    //private String value;

    //public KeyValue() {
    //}

    //private final Pair<String, String> keyValue;

    /**
     * コンストラクタです。valueを指定しません。
     */
    public KeyValue(String key) {
        //this.key = key;
        //this.keyValue = MutablePair.of(key, null);

        super(key);
    }

    /**
     * コンストラクタです。
     */
    public KeyValue(String key, String value) {
        //this.key = key;
        //this.value = value;
        //this.keyValue = MutablePair.of(key, value);

        super(key, value);
    }

    //    public String getKey() {
    //        //return this.key;
    //        return this.keyValue.getKey();
    //    }
    //
    //    public String getValue() {
    //        //return this.value;
    //        return this.keyValue.getValue();
    //    }
    //
    //    public void setValue(String value) {
    //        //this.value = value;
    //        this.keyValue.setValue(value);
    //    }

    /**
     * 単体テストをします。
     */
    public static void main(String[] args) {

        KeyValue kv = new KeyValue("key", "val");
        kv.setValue("val2");

        System.out.println(kv.getKey());
        System.out.println(kv.getValue());

    }

}
