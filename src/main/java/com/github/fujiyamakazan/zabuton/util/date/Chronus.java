package com.github.fujiyamakazan.zabuton.util.date;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 「時」を司ります。
 * @author fujiyama
 */
public class Chronus implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Chronus.class);

    public static final String POPULAR_JP = "yyyy/MM/dd";

    /**
     * 書式変更をします。
     */
    public static String convert(String string, String from, String to) {
        Date d;
        try {
            SimpleDateFormat dfFrom = createDateFormat(from);
            d = dfFrom.parse(string);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        SimpleDateFormat dfTo = createDateFormat(to);
        return dfTo.format(d);
    }

    /**
     * Date型に変換します。
     */
    public static Date parse(String string, String pattern) {
        try {
            return createDateFormat(pattern).parse(string);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * yyyy/MM/dd形式の文字列を日付にします。
     * @param yyyyMMdd yyyy/MM/dd形式の文字列
     * @return 日付
     */
    public static Date parse(String yyyyMMdd) {
        try {
            return createDateFormat(POPULAR_JP).parse(yyyyMMdd);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String formant(Date date, String pattern) {
        return createDateFormat(pattern).format(date);
    }

    public static String formant(Date date) {
        return createDateFormat(POPULAR_JP).format(date);
    }

    private static SimpleDateFormat createDateFormat(String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        df.setLenient(false); // 厳密
        return df;
    }

    public static String getNowYyyy() {
        return new SimpleDateFormat("yyyy").format(new Date());
    }

    /**
     * 月を加算した日付を返します。
     */
    public static Date addMonth(Date date, int amount) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, amount);
        return c.getTime();
    }

}
