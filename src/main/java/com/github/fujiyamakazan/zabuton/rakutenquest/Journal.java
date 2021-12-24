package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.ibm.icu.util.Calendar;

/**
 * 仕訳レコードです。
 */
public class Journal implements Serializable {
    private static final long serialVersionUID = 1L;

    private final DateFormat DF = new SimpleDateFormat("yyyy/MM/dd");

    /** 日付 */
    private Date date;

    /** 金額 */
    private int amount;

    /** 借方 */
    private String left;

    /** 貸方 */
    private String right;

    /** メモ */
    private String memo;

    /** 記録元 */
    private String source;

    /** 活動科目 */
    private String activity;

    /** 記録元での生データ */
    private String rawOnSource;

    /** 記録元でのキーワード */
    private String keywordOnSource;

    /** 記録元での残高 */
    private int summaryOnSource;

    /** 記録元での行Index(ID) */
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
        return DF.format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(String strDate) {
        try {
            this.date = DF.parse(strDate);
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
            formatedDate = DF.format(date);
        }

        return formatedDate + "\t"
            + toMoney(amount) + "\t"
            + left + "\t"
            + right + "\t"
            + memo + "\t"
            + "\t" // 突合メモ
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
            + (date != null ? new SimpleDateFormat("yyyy/MM/dd").format(date) : "")
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