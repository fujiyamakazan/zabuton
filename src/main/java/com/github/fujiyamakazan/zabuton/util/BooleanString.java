package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;

import org.apache.commons.lang3.BooleanUtils;

public class BooleanString implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文字列をTrueかFalseに判定します。
     * nullはFalseとします。
     * 大/小文字は評価しません。
     */
    public static boolean toBoolean(String value) {
        if (value == null) {
            return false;
        }
        //        String str = value.toLowerCase();
        //        return str.equals("1")
        //                || str.equals("yes")
        //                || str.equals("y")
        //                || str.equals("true")
        //                || str.equals("t");
        return BooleanUtils.toBooleanObject(value);
    }

    //    public static void main(String[] args) {
    //        System.out.println(BooleanUtils.toBooleanObject("1"));
    //        System.out.println(BooleanUtils.toBooleanObject("yes"));
    //        System.out.println(BooleanUtils.toBooleanObject("y"));
    //        System.out.println(BooleanUtils.toBooleanObject("true"));
    //        System.out.println(BooleanUtils.toBooleanObject("t"));
    //    }
}
