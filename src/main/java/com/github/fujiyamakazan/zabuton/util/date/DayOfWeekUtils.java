package com.github.fujiyamakazan.zabuton.util.date;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateFormatUtils;


/**
 * 曜日に関わるユーティリティです。
 * @author fujiyama
 */
public class DayOfWeekUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DayOfWeekUtils.class);

    /**
     * DayOfWeekを曜日の名前に変換します。
     * @param dayOfWeek DayOfWeek (1,2,3,4,5,6,7の何れか)
     * @return 曜日の名前 日,月,火,水,木,金,土
     */
    public static String toYobi(int dayOfWeek) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        return DateFormatUtils.format(c, "E");

    }

    /**
     * 第nX曜日に当たる日付を算出します。
     * 例：第３水曜日、第１月曜日
     * 「月における何回目のその曜日」であり、
     * 「第n週のその曜日]でないことに注意してください。
     * @param year 年
     * @param month 月（1～）
     * @param ordinalOfDayOfWeek  曜日の序数。第1X曜日なら「1」を指定
     * @param dayOfWeek DayOfWeek (1,2,3,4,5,6,7の何れか)
     *
     */
    private static int calcDay(int year, int month, int ordinalOfDayOfWeek , int dayOfWeek) {

        //log.debug(year + "年" + month + "月第" + n + toYobi(dayOfWeek) + "曜日を算出します。");

        /* 月初の曜日を取得 */
        Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.set(year, month - 1, 1);
        final int firstDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        //log.debug("この月は" + toYobi(firstDayOfWeek) + "曜日から始まります。");

        /* 曜日の差 */
        int len = DayOfWeek.values().length;
        int offset = (len + dayOfWeek - firstDayOfWeek) % len; // 曜日の個数(7)を元に「あまり」を求める。
        //log.debug("曜日から算出したオフセット：" + offset);

        int base = (ordinalOfDayOfWeek - 1) * len + 1; // 月初と同じ曜日であるn番目の日付
        //log.debug("月初と同じ曜日である" + n + "番目の日付は[" + base + "]日です。");

        int day = base + offset;
        //log.debug("[" + day + "]日です。");

        return day;
    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        log.debug("第3日曜日:" + calcDay(2022, 2, 3, Calendar.SUNDAY));
        log.debug("第3月曜日:" + calcDay(2022, 2, 3, Calendar.MONDAY));
        log.debug("第3火曜日:" + calcDay(2022, 2, 3, Calendar.TUESDAY));
        log.debug("第3水曜日:" + calcDay(2022, 2, 3, Calendar.WEDNESDAY));
        log.debug("第3木曜日:" + calcDay(2022, 2, 3, Calendar.THURSDAY));
        log.debug("第3金曜日:" + calcDay(2022, 2, 3, Calendar.FRIDAY));
        log.debug("第3土曜日:" + calcDay(2022, 2, 3, Calendar.SATURDAY));

        log.debug("第1日曜日:" + calcDay(2022, 2, 1, Calendar.SUNDAY));

        Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.set(2022, 2 - 1, 1);




    }

}
