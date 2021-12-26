package com.github.fujiyamakazan.zabuton.util.date;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatConverter implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DateFormatConverter.class);

    public static String convert(String string, String pattern) {

        /*
         * from,to が同一のときに期待されること
         * "2022年01月27日 (木)"を "yyyy年MM月dd日"で変換するとパターンに一致しない部分＜(木)＞をトリムできる。
         */

        return convert(string, pattern, pattern);
    }

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

    public static Date parse(String string, String pattern) {
        try {
            return createDateFormat(pattern).parse(string);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private static SimpleDateFormat createDateFormat(String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        df.setLenient(false); // 厳密
        return df;
    }


}
