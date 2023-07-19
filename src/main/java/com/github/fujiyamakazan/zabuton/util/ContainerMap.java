package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.util.lang.Generics;

public abstract class ContainerMap<K, D> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<K, List<D>> map = Generics.newHashMap();

    /**
     * データを登録します。
     */
    public void put(D data) {
        K key = dataToKey(data);
        List<D> datas = map.get(key);
        if (datas == null) {
            datas = Generics.newArrayList();
            map.put(key, datas);
        }
        datas.add(data);
    }

    /**
     * 登録済みのキーを取得します。
     */
    public List<K> getKeys() {
        List<K> keys = Generics.newArrayList();
        for (Entry<K, List<D>> entry: map.entrySet()) {
            keys.add(entry.getKey());
        }
        return keys;
    }

    /**
     * キーに紐づく登録済みのデータを取得します。
     */
    public List<D> getDatas(K key) {
        return map.get(key);
    }

    /**
     * グループ化の基準となるキーを取得します。
     */
    protected abstract K dataToKey(D body);

}
