package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

public class DuplicateChecker implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DuplicateChecker.class);

    private static class Journal {
        private String memo;

        public Journal(String string, int i, String string2, String string3, String string4) {
            this.date = string;
            this.amount = i;
            this.left = string2;
            this.right = string3;
            this.memo = string4;
        }

        @SuppressWarnings("unused")
        private String date;
        @SuppressWarnings("unused")
        private int amount;
        @SuppressWarnings("unused")
        private String left;
        @SuppressWarnings("unused")
        private String right;

        @Override
        public String toString() {
            return "Journal [memo=" + memo + "]";
        }

    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {
        List<Journal> journals = Generics.newArrayList();
        journals.add(new Journal("2021/01/01", 100, "TEST", "ABC", "memo1"));
        journals.add(new Journal("2021/01/01", 100, "TEST", "ABC", "memo2"));
        journals.add(new Journal("2021/01/01", 100, "TEST", "ABC", "memo3"));
        journals.add(new Journal("2021/01/01", 100, "TEST", "ABC", "memo4"));

        System.out.println(ListToStringer.convert(journals));
    }
}
