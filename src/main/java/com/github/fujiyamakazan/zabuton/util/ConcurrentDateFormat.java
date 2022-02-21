package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * マルチスレッドに対応したDateFormatです。
 * @author fujiyama
 */
public class ConcurrentDateFormat implements Serializable {
    private static final long serialVersionUID = 1L;

    private final DateFormat df;

    public ConcurrentDateFormat(final String pattern) {

        this.df = new SimpleDateFormat(pattern);
    }

    private DateFormat getDf() {
        return this.df;
    }

    /**
     * マルチスレッドに対応したparseメソッドです。
     *   * @link {@link SimpleDateFormat#parse(String)}
     */
    public Date parse(String source) throws ParseException {
        synchronized (this.df) {
            return getDf().parse(source);
        }
    }

    /**
     * マルチスレッドに対応したformatメソッドです。
     * @link {@link SimpleDateFormat#format(Date)}
     */
    public String format(Date date) {
        synchronized (this.df) {
            return getDf().format(date);
        }
    }

}
