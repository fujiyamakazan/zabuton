package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import com.github.fujiyamakazan.zabuton.util.date.Chronus;

public class JournalsTerm implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JournalsTerm.class);

    /* 期首の日付 */
    private final Date start;
    /* 期末が終了した直後の日付 */
    private final Date next;

    /**
     * 「yyyy/MM/dd～yyyy/MM/dd」で示されたパターンからインスタンスを生成します。
     * @param pattern パターン
     */
    public JournalsTerm(String pattern) {
        String strStart = pattern.substring(0, pattern.indexOf('～'));
        this.start = Chronus.parse(strStart);
        String strEnd = pattern.substring(pattern.indexOf('～') + 1);
        Date end = Chronus.parse(strEnd);
        Calendar c = Calendar.getInstance();
        c.clear();
        c.setTime(end);
        c.add(Calendar.DAY_OF_MONTH, 1);
        this.next = c.getTime();
    }

    /**
     * 開始年の元旦を期首。その年の大晦日を期末とするインスタンスを生成します。
     * @param year 開始年
     */
    public JournalsTerm(int year) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, 0, 1);
        this.start = c.getTime();
        c.add(Calendar.YEAR, 1);
        this.next = c.getTime();
    }

    public Boolean in(String value, String pattern) {
        return inCore(value, pattern, start, next);
    }

    public TermAction createTermAction(String pattern) {
        TermAction action = new TermAction(pattern, start, next);
        return action;
    }

    public static class TermAction implements Serializable {
        private static final long serialVersionUID = 1L;
        private String pattern;
        /* 期首の日付 */
        private final Date start;
        /* 期末が終了した直後の日付 */
        private final Date next;

        public TermAction(String pattern, Date start, Date next) {
            this.pattern = pattern;
            this.start = start;
            this.next = next;
        }

        public boolean in(String value) {
            return inCore(value, pattern, start, next);
        }

    }

    /**
     * ファイル名に使う名前。期首日付。ただし、1/1は省略可能としyyyyとする。
     * 1日は省略可能としyyyyMMとする。
     * @return
     */
    public String getName() {
        Calendar c = Calendar.getInstance();
        c.setTime(start);
        if (c.get(Calendar.MONTH) == 0 && c.get(Calendar.DAY_OF_MONTH) == 1) {
            return String.valueOf(c.get(Calendar.YEAR));
        }
        String name = Chronus.formant(start, "yyyyMMdd");
        return name;
    }

    @Override
    public String toString() {
        return "JournalsTerm [" + start + "=>" + next + "]";
    }


    private static Boolean inCore(String value, String pattern, Date start, Date next) {

        log.debug("inCore:" + value + " pattern:" + pattern);
        boolean result;
        Date date;
        try {
            date = Chronus.parse(value, pattern);
            if (date.before(start)) {
                result =  false;
            } else if (date.equals(next) || date.after(next)) {
                result =  false;
            } else {
                result = true;
            }
        } catch (Exception e) {
            result = false;
        }

        log.debug(">>" + result);
        return result;
    }

    public static void main(String[] args) {

        JournalsTerm term1 = new JournalsTerm(2021);
        log.debug(term1.toString());

        JournalsTerm term2 = new JournalsTerm("2021/01/01～2021/12/31");
        log.debug(term2.toString());

        log.debug(term1.in("2020/12/31", Chronus.POPULAR_JP).toString());
        log.debug(term1.in("2021/01/01", Chronus.POPULAR_JP).toString()); // true
        log.debug(term1.in("2021/12/31", Chronus.POPULAR_JP).toString()); // true
        log.debug(term1.in("2022/01/01", Chronus.POPULAR_JP).toString());
        log.debug(term1.in("2022/01/02", Chronus.POPULAR_JP).toString());

    }

}
