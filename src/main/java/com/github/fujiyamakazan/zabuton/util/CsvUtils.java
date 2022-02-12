package com.github.fujiyamakazan.zabuton.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.opencsv.CSVParser;

public class CsvUtils implements Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CsvUtils.class);

    public static String convertString(String[] ary) {
        return new CSVParser().parseToLine(ary, true);
    }

    public static String convertString(List<String> csv) {
        return convertString(csv.toArray(new String[csv.size()]));
    }

    /**
     * 文字列を要素に分解します。
     */
    public static String[] splitCsv(String str) {
        try {
            return new CSVParser().parseLine(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
