package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.github.fujiyamakazan.zabuton.util.date.Chronus;
import com.ibm.icu.util.Calendar;

/**
 * 仕訳レコードです。
 * @author fujiyama
 */
public class Journal implements Serializable {
    private static final long serialVersionUID = 1L;

    private final DateFormat df = new SimpleDateFormat(Chronus.POPULAR_JP);

    /** 日付です。 */
    private Date date;

    /** 金額です。 */
    private int amount;

    /** 借方です。 */
    private String left;

    /** 貸方です。 */
    private String right;

    /** メモです。 */
    private String memo;

    /** メモ(追加)です。 */
    private String memo2 = "";

    /** 記録元です。 */
    private String source;

    /** 活動科目です。 */
    private String activity;

    /** 記録元での生データです。 */
    private String rawOnSource;

    /** 記録元でのキーワードです。 */
    private String keywordOnSource;

    /** 記録元での残高です。 */
    private int summaryOnSource;

    /** 記録元での行Index(ID)です。 */
    private String rowIndex;

    public Journal() {
        this.rowIndex = null;
    }

    public String getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(String rowIndex) {
        this.rowIndex = rowIndex;
    }

    public String getRawOnSource() {
        return rawOnSource;
    }

    public void setRawOnSource(String rawOnSource) {
        this.rawOnSource = rawOnSource;
    }

    public int getSummaryOnSource() {
        return summaryOnSource;
    }

    public void setSummaryOnSource(int summaryOnSource) {
        this.summaryOnSource = summaryOnSource;
    }

    public Date getDate() {
        return date;
    }

    public String getDateString() {
        return df.format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * 文字列から日付を登録します。
     * @param strDate 登録する文字列
     */
    public void setDate(String strDate) {
        try {
            this.date = df.parse(strDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getMemo2() {
        return memo2;
    }

    public void setMemo2(String memo2) {
        this.memo2 = memo2;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public String getKeywordOnSource() {
        return keywordOnSource;
    }

    public void setKeywordOnSource(String keywordOnSource) {
        this.keywordOnSource = keywordOnSource;
    }

    public String getJournalString() {
        String formatedDate = null;
        if (date != null) {
            formatedDate = df.format(date);
        }

        return formatedDate + "\t"
            + toMoney(amount) + "\t"
            + left + "\t"
            + right + "\t"
            + memo + "\t"
            + memo2 + "\t" // 突合メモ
            + activity + "\t"
            + source + "\t"
            + (rowIndex != null ? (rowIndex) : "");
    }

    private String toMoney(int price) {
        return String.format("%,d", price);
    }

    @Override
    public String toString() {
        return "SuitoData [date="
            + (date != null ? new SimpleDateFormat(Chronus.POPULAR_JP).format(date) : "")
            + ", amount=" + amount + ", left=" + left + ", right=" + right + ", memo="
            + memo + ", source=" + source + ", activity=" + activity + "]";
    }





    public final class JournalComparator implements Comparator<Journal> {
        @Override
        public int compare(Journal o1, Journal o2) {
            return o1.date.compareTo(o2.date);
        }
    }

    public static final class JournalsComparator implements Comparator<Journal> {
        @Override
        public int compare(Journal o1, Journal o2) {
            int compare = 0;
            if (compare == 0) {
                compare = StringUtils.compare(o1.getSource(), o2.getSource());
            }
            if (compare == 0) {
                compare = StringUtils.compare(o1.getActivity(), o2.getActivity());
            }
            if (compare == 0) {
                compare = StringUtils.compare(o1.getMemo(), o2.getMemo());
            }
            if (compare == 0) {
                compare = DateUtils.truncatedCompareTo(o1.getDate(), o2.getDate(), Calendar.DAY_OF_MONTH);
            }
            return compare;
        }
    }
}