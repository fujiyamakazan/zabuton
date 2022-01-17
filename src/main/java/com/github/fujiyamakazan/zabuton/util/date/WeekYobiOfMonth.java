package com.github.fujiyamakazan.zabuton.util.date;

import java.io.Serializable;
import java.util.Calendar;

/**
 * 第nX曜日に当たる日付を算出します。
 * 例：第３水曜日、第１月曜日
 * ※ [第nX曜日][第n週X曜日]でないことに注意してください。
 * @author fujiyama
 */
public class WeekYobiOfMonth implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WeekYobiOfMonth.class);

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {
        calcDay(2022, 1, 3, Calendar.SUNDAY); // 第3日曜日
        calcDay(2022, 1, 3, Calendar.MONDAY); // 第3月曜日
        calcDay(2022, 1, 3, Calendar.TUESDAY); // 第3火曜日
        calcDay(2022, 1, 3, Calendar.WEDNESDAY); // 第3水曜日
        calcDay(2022, 1, 3, Calendar.THURSDAY); // 第3木曜日
        calcDay(2022, 1, 3, Calendar.FRIDAY); // 第3金曜日
        calcDay(2022, 1, 3, Calendar.SATURDAY); // 第3土曜日
    }

    /**
     * 第nX曜日に当たる日付を算出します。
     * 例：第３水曜日、第１月曜日
     */
    private static int calcDay(int year, int month, int n, int dayOfWeek) {

        log.debug(year + "年" + month + "月第" + n + toYobi(dayOfWeek) + "曜日を算出します。");

        /* 月初の曜日を取得 */
        Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.set(year, month - 1, 1);
        final int firstDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        log.debug("この月は" + toYobi(firstDayOfWeek) + "曜日から始まります。");

        /* 曜日の差 */
        int offset = (7 + dayOfWeek - firstDayOfWeek) % 7; // 曜日の個数(7)を元に「あまり」を求める。
        log.debug("曜日から算出したオフセット：" + offset);

        int base = (n - 1) * 7 + 1; // 月初と同じ曜日であるn番目の日付
        log.debug("月初と同じ曜日である" + n + "番目の日付は[" + base + "]日です。");

        int day = base + offset;
        log.debug("[" + day + "]日です。");

        return day;
    }

    private static String toYobi(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return "日";
            case Calendar.MONDAY:
                return "月";
            case Calendar.TUESDAY:
                return "火";
            case Calendar.WEDNESDAY:
                return "水";
            case Calendar.THURSDAY:
                return "木";
            case Calendar.FRIDAY:
                return "金";
            case Calendar.SATURDAY:
                return "土";
            default:
                throw new RuntimeException();
        }

    }


}
