package com.github.fujiyamakazan.zabuton.util.date;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.StringBuilderLn;

public class MonthlyCalendar implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MonthlyCalendar.class);

    public static final int SIZE = 7;

    private final Calendar firstDate;
    private final int firstDayOfWeek;
    private final List<WeeklyCalendar> weeklyCalendar;

    /**
     * keyを曜日とする。valueには0～6の数値を保持する。
     * その曜日が、月初の曜日から何日後かを計算するときの基となる。
     */
    private Map<Integer, Integer> dayValues;

    /**
     * コンストラクタです。年月を指定してインスタンスを生成します。
     * 月初の曜日から「曜日の配列」を決定します。
     * @param year 年
     * @param monthValue 月(1～12)
     */
    public MonthlyCalendar(int year, int monthValue) {
        this.firstDate = Calendar.getInstance();
        this.firstDate.clear();
        this.firstDate.set(year, monthValue - 1, 1);
        this.firstDayOfWeek = this.firstDate.get(Calendar.DAY_OF_WEEK);

        //log.debug("この月は" + toYobi(firstDayOfWeek) + "曜日から始まります。");

        /* 各曜日がこの月の何番目の値となるかを設定する */
        this.dayValues = Generics.newHashMap();
        for (int i = 0; i < SIZE; i++) {
            int dayOfWeek = (((this.firstDayOfWeek - 1) + i) % SIZE) + 1; // 1～7の数列を割当てる計算式
            //log.debug("\"" + toYobi(dayOfWeek) + "\"(" + dayOfWeek + ")は月初から" + i + "日後です。");

            this.dayValues.put(dayOfWeek, i);
        }

        /* 週ごとのカレンダーを作成します。 */
        this.weeklyCalendar = Generics.newArrayList();
        Calendar c = Calendar.getInstance();
        c.setTime(this.firstDate.getTime());
        WeeklyCalendar wc = null;
        while (true) {

            if (wc == null) {
                wc = new WeeklyCalendar();
                this.weeklyCalendar.add(wc);
            }

            int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            wc.add(dayOfMonth, dayOfWeek);

            if (wc.size() >= SIZE || dayOfWeek == Calendar.SATURDAY) {
                wc = null;
            }

            if (dayOfMonth == c.getActualMaximum(Calendar.DATE)) {
                break;
            }
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

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
    private int getDay(int ordinalOfDayOfWeek, Integer dayOfWeek) {

        /* weekValue: 週の値。第1週→0, 第2週→1 ... */
        int weekValue = ordinalOfDayOfWeek - 1;

        /* dayValue: 日の値。月初の曜日を0とした連番 */
        int dayValue = this.dayValues.get(dayOfWeek);

        /* 週の値と日の値から[日付の数値]を求める。*/
        int value = weekValue * this.dayValues.size() + dayValue;

        /* [日付の数値]は0から始まる。カレンダー表記のように1から始まる数列に変更。*/
        int dayOfMonth = value + 1;

        return dayOfMonth;

    }

    /**
     * 第n週のカレンダーを取得します。
     * @param ordinalOfWeekOfMonth 週の序数。第1週なら「1」を指定
     */
    private WeeklyCalendar getWeek(int ordinalOfWeekOfMonth) {
        return this.weeklyCalendar.get(ordinalOfWeekOfMonth - 1);
    }

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

    @Override
    public String toString() {

        StringBuilderLn sb = new StringBuilderLn();
        for (WeeklyCalendar wc : this.weeklyCalendar) {
            sb.appendLn(wc.toString());
        }
        return sb.toString();
    }

    private class WeeklyCalendar implements Serializable {
        private static final long serialVersionUID = 1L;

        private List<Integer> dayOfMonths = Generics.newArrayList();
        private List<Integer> dayOfWeeks = Generics.newArrayList();

        /**
         * 指定した曜日の日付を取得します。
         * @param dayOfWeek DayOfWeek (1,2,3,4,5,6,7の何れか)
         *
         */
        public int getDay(int dayOfWeek) {
            for (int i = 0; i < this.dayOfWeeks.size(); i++) {
                if (this.dayOfWeeks.get(i).equals(dayOfWeek)) {
                    return this.dayOfMonths.get(i);
                }
            }
            return -1;
        }

        public void add(int dayOfMonth, int dayOfWeek) {
            this.dayOfMonths.add(dayOfMonth);
            this.dayOfWeeks.add(dayOfWeek);
        }

        public int size() {
            return this.dayOfMonths.size();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < this.dayOfWeeks.size(); i++) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(this.dayOfMonths.get(i) + "(" + MonthlyCalendar.toYobi(this.dayOfWeeks.get(i)) + ")");
            }
            return sb.toString();
        }

    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        MonthlyCalendar mc = new MonthlyCalendar(2022, 2);
        log.debug(mc.toString());
        log.debug("第3日曜日:" + mc.getDay(3, Calendar.SUNDAY));
        log.debug("第3月曜日:" + mc.getDay(3, Calendar.MONDAY));
        log.debug("第3火曜日:" + mc.getDay(3, Calendar.TUESDAY));
        log.debug("第3水曜日:" + mc.getDay(3, Calendar.WEDNESDAY));
        log.debug("第3木曜日:" + mc.getDay(3, Calendar.THURSDAY));
        log.debug("第3金曜日:" + mc.getDay(3, Calendar.FRIDAY));
        log.debug("第3土曜日:" + mc.getDay(3, Calendar.SATURDAY));

        log.debug("第3週の日曜日:" + mc.getWeek(3).getDay(Calendar.SUNDAY));
        log.debug("第3週の月曜日:" + mc.getWeek(3).getDay(Calendar.MONDAY));
        log.debug("第3週の火曜日:" + mc.getWeek(3).getDay(Calendar.TUESDAY));
        log.debug("第3週の水曜日:" + mc.getWeek(3).getDay(Calendar.WEDNESDAY));
        log.debug("第3週の木曜日:" + mc.getWeek(3).getDay(Calendar.THURSDAY));
        log.debug("第3週の金曜日:" + mc.getWeek(3).getDay(Calendar.FRIDAY));
        log.debug("第3週の土曜日:" + mc.getWeek(3).getDay(Calendar.SATURDAY));

    }

}
