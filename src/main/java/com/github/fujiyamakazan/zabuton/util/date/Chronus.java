package com.github.fujiyamakazan.zabuton.util.date;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

/**
 * 「時」を司ります。
 * @author fujiyama
 */
public class Chronus implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Chronus.class);

    public static final String POPULAR_JP = "yyyy/MM/dd";
    public static final int MAX_HOUR_OF_DAY = 23;

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

    public static int getNowYyyy() {
        LocalDate now = LocalDate.now();
        return now.getYear();
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

    /**
     * 年月日を指定してDateを生成します。
     */
    public static Date dateOf(int year, int month, int dayOfMonth) {
        LocalDate ld = LocalDate.of(year, month, dayOfMonth);
        LocalDateTime ldt = ld.atStartOfDay();
        return Date.from(ZonedDateTime.of(ldt, ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date から LocaleDate へ変換します。
     */
    public static LocalDate localDateOf(Date date) {
        return LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Date から LocaleDateTime へ変換します。
     */
    public static LocalDateTime localDateTimeOf(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 指定日付が、現在を過ぎているか。
     * @param dateTime 指定日付
     * @return 過ぎている時にTrue
     */
    public static boolean isAfter(LocalDateTime dateTime) {
        return isPast(dateTime, null);
    }

    /**
     * 指定日付に追加日付を加算した日付が、現在を過ぎているか。
     * @param dateTime 指定日付
     * @param additionalTime 追加日付
     * @return 過ぎている時にTrue
     */
    public static boolean isPast(LocalDateTime dateTime, Duration additionalTime) {
        if (additionalTime != null) {
            dateTime = dateTime.plusSeconds(additionalTime.getSeconds());
        }
        return LocalDateTime.now().isAfter(dateTime);
    }

    public static void main(String[] args) {


        LocalDateTime dateTime = LocalDateTime.of(2023, 2, 19, 17, 51, 0);
        Duration add = Duration.ofDays(7);

        LOGGER.debug("現在       ：" + LocalDateTime.now().toString());
        LOGGER.debug("7日後の日付：" + dateTime.plusDays(7).toString());

        boolean after = isPast(dateTime, add);
        if (after) {
            System.out.println("現在が指定期限を過ぎている");
        } else {
            System.out.println("現在が指定期限を過ぎていない");
        }
    }



}
