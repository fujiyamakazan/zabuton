package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;
import java.util.Map;

import org.apache.wicket.util.lang.Generics;

public class Counter implements Serializable {

    private static final long serialVersionUID = 1L;

    public Map<Object, Integer> map = Generics.newHashMap();

    /**
     * 集計対象を登録します。
     */
    public void put(Object obj) {
        Integer i = this.map.get(obj);
        if (i == null) {
            this.map.put(obj, 1);
        } else {
            this.map.put(obj, ++i);
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<Object, Integer> entry : this.map.entrySet()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(entry.getKey() + "(" + entry.getValue() + ")");
        }
        return sb.toString();
    }
}
