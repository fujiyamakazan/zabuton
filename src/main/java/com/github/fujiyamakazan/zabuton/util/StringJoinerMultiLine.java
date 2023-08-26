package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

public class StringJoinerMultiLine implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StringJoinerMultiLine.class);


    /**
     * 複数行の文字列を連結します。
     */
    public static String joinString(String... linesList) {
        List<String> joinedList = null;
        for (String lines : linesList) {
            List<String> listLine = Generics.newArrayList();
            for (String line : lines.split("\n")) {
                listLine.add(line);
            }
            if (joinedList == null) {
                joinedList = listLine;
            } else {
                for (int i = 0; i < listLine.size(); i++) {
                    joinedList.set(i, joinedList.get(i) + listLine.get(i));
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String str : joinedList) {
            sb.append(str + "\n");
        }
        return sb.toString();
    }
}
