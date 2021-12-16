package com.github.fujiyamakazan.zabuton.util;

import java.io.IOException;
import java.io.Serializable;

import com.opencsv.CSVParser;

public class CsvUtils implements Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CsvUtils.class);

    public static void main(String[] args) {
        String str = "\"2021/10/25\",\"ﾎｼﾉｺ-ﾋ-\"\"ﾃﾝ\"\"XXXﾃﾝ\",\"3200\",\"0\",\"3,200\",\"3200\",\"0\",\"*\"";
        String[] ary = splitCsv(str);
        for (int i = 0; i < ary.length; i++) {
            String string = ary[i];
            System.out.println(string);
        }
        System.out.println(convertString(new String[] { "a", "\"b\"", "1,234" }));
    }

    public static String convertString(String[] ary) {
        return new CSVParser().parseToLine(ary, true);
    }

    public static String[] splitCsv(String str) {
        try {
            return new CSVParser().parseLine(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
