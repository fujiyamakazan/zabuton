package com.github.fujiyamakazan.zabuton.util.date;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatConverter implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DateFormatConverter.class);

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

    private static SimpleDateFormat createDateFormat(String pattern) {
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        df.setLenient(false); // 厳密
        return df;
    }
}
