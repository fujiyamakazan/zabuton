package com.github.fujiyamakazan.zabuton.sample.sample2023sort;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ソートのテストをするための検証用Beanです。
 * 1st,2nd,3rdはソートキーです。
 * @author fujiyama
 */
public class SampleSortItme implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final Integer data1st;
    private final Date data2nd;
    private final String data3rd;

    /**
     * コンストラクタです。
     */
    public SampleSortItme(int id, Integer data1st, Date data2nd, String data3rd) {
        this.id = id;
        this.data1st = data1st;
        this.data2nd = data2nd;
        this.data3rd = data3rd;
    }

    public int getId() {
        return this.id;
    }

    public Integer getData1st() {
        return this.data1st;
    }

    public Date getData2nd() {
        return this.data2nd;
    }

    public String getData3rd() {
        return this.data3rd;
    }

    @Override
    public String toString() {
        final String str2nd;
        if (this.data2nd == null) {
            str2nd = "null";
        } else {
            str2nd = new SimpleDateFormat("yyyy/MM/dd").format(this.data2nd);
        }
        return String.format("%2d %d %10s %s", this.id, this.data1st, str2nd, this.data3rd);
    }

}
