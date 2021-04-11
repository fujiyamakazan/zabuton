package com.github.fujiyamakazan.zabuton.util.text;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fujiyamakazan.zabuton.util.KeyValueString;

/**
 * キーと値のペアを定義したテキストファイルの読み書きをします。
 *
 * @author fujiyama
 */
public abstract class KeyValuesText implements Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger log =  LoggerFactory.getLogger(KeyValuesText.class);

    protected Utf8Text utf8File;

    protected List<KeyValueString> keyValueString = null;

    public KeyValuesText(Utf8Text utf8File) {
        this.utf8File = utf8File;
    }

    public KeyValuesText(String pathname) {
        this(new Utf8Text(pathname));
    }

    /**
     * テキストファイルから呼出します。
     * 内部的（このクラス、およびサブクラス）による遅延処理を目的にしているため、
     * publicにはしません。
     */
    protected abstract List<KeyValueString> read();

    /**
     * テキストファイルに保存する処理を実装します。
     */
    public abstract void write();

    /**
     * キーを指定して値を返します。
     * @param key キー
     * @return 値。キーが無ければnull。
     */
    public String get(String key) {
        if (keyValueString == null) {
            this.keyValueString = read();
        }
        for (KeyValueString kv: this.keyValueString) {
            if (StringUtils.equals(kv.getKey(), key)) {
                return kv.getValue();
            }
        }
        return null;
    }

    /**
     * キーを指定して値を登録します。
     * @param key キー
     * @param value 値
     */
    public void set(String key, String value) {
        if (keyValueString == null) {
            this.keyValueString = read();
        }
        boolean exist = false;
        for (KeyValueString kv: this.keyValueString) {
            if (StringUtils.equals(kv.getKey(), key)) {
                kv.setValue(value);
                exist = true;
            }
        }
        if (exist == false) {
            this.keyValueString.add(new KeyValueString(key, value));
        }
    }

    protected static String nulToBlank(String value) {
        return value == null ? "" : value;
    }
}
