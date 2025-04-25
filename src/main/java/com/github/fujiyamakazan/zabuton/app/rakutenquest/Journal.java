package com.github.fujiyamakazan.zabuton.app.rakutenquest;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import com.github.fujiyamakazan.zabuton.util.date.Chronus;

/**
 * 仕訳レコードです。
 * @author fujiyama
 */
public class Journal implements Serializable {
    private static final long serialVersionUID = 1L;

    //private final DateFormat df = new SimpleDateFormat(Chronus.POPULAR_JP);

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

    ///** 記録元での生データです。 */
    //private String rawOnSource;

    ///** 記録元でのキーワードです。 */
    //private String keywordOnSource;

//    /** 記録元での残高です。 */
//    private int summaryOnSource;

    /** 記録元での行Index(ID)です。 */
    private String rowIndex;

    public Journal() {
        this.rowIndex = null;
    }

    public String getRowIndex() {
        return this.rowIndex;
    }

    public void setRowIndex(String rowIndex) {
        this.rowIndex = rowIndex;
    }

    //public String getRawOnSource() {
    //    return this.rawOnSource;
    //}

    //public void setRawOnSource(String rawOnSource) {
    //    this.rawOnSource = rawOnSource;
    //}

//    public int getSummaryOnSource() {
//        return this.summaryOnSource;
//    }
//
//    public void setSummaryOnSource(int summaryOnSource) {
//        this.summaryOnSource = summaryOnSource;
//    }

    public Date getDate() {
        return this.date;
    }

    public String getDateString() {
        DateFormat df = new SimpleDateFormat(Chronus.POPULAR_JP);
        return df.format(this.date);
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
            DateFormat df = new SimpleDateFormat(Chronus.POPULAR_JP);
            this.date = df.parse(strDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLeft() {
        return this.left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return this.right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public String getMemo() {
        return this.memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getMemo2() {
        return this.memo2;
    }

    public void setMemo2(String memo2) {
        this.memo2 = memo2;
    }

    /**
     * 「記録元」を取得します。
     * @return 記録元
     */
    public String getSource() {
        return this.source;
    }

    /**
     * 「記録元」を登録します。
     * @param source 記録元
     */
    public void setSource(String source) {
        this.source = source;
    }

    public String getActivity() {
        return this.activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return this.amount;
    }

    //public String getKeywordOnSource() {
    //    return this.keywordOnSource;
    //}

    //public void setKeywordOnSource(String keywordOnSource) {
    //   this.keywordOnSource = keywordOnSource;
    //}

    public final class JournalComparator implements Comparator<Journal> {
        @Override
        public int compare(Journal o1, Journal o2) {
            return o1.date.compareTo(o2.date);
        }
    }

    //    public static final class JournalsComparator implements Comparator<Journal> {
    //        @Override
    //        public int compare(Journal o1, Journal o2) {
    //            int compare = 0;
    //            if (compare == 0) {
    //                compare = StringUtils.compare(o1.getSource(), o2.getSource());
    //            }
    ////            if (compare == 0) {
    ////                compare = StringUtils.compare(o1.getActivity(), o2.getActivity());
    ////            }
    ////            if (compare == 0) {
    ////                compare = StringUtils.compare(o1.getMemo(), o2.getMemo());
    ////            }
    ////            if (compare == 0) {
    ////                //compare = DateUtils.truncatedCompareTo(o1.getDate(), o2.getDate(), Calendar.DAY_OF_MONTH);
    ////                compare = DateUtils.truncatedCompareTo(o1.getDate(), o2.getDate(), Calendar.HOUR_OF_DAY);
    ////            }
    //            return compare;
    //        }
    //    }

//    public static final String HEADER = "日付" + "\t"
//        + "金額" + "\t"
//        + "借方" + "\t"
//        + "貸方" + "\t"
//        + "メモ" + "\t"
//        + "メモ2" + "\t"
//        + "活動科目" + "\t"
//        + "記録元" + "\t"
//        + "#";

    /**
     * 仕訳用文字列を返します。
     */
    public String getJournalString() {
        String formatedDate = null;
        if (this.date != null) {
            DateFormat df = new SimpleDateFormat(Chronus.POPULAR_JP);
            formatedDate = df.format(this.date);
        }

        return formatedDate + "\t"
            + toMoney(this.amount) + "\t"
            + this.left + "\t"
            + this.right + "\t"
            + this.memo + "\t"
            + this.memo2 + "\t" // 突合メモ
            + this.activity + "\t"
            + this.source + "\t"
            + (this.rowIndex != null ? (this.rowIndex) : "");
    }

    private static String toMoney(int price) {
        return String.format("%,d", price);
    }

    @Override
    public String toString() {
        return "SuitoData [date="
            + (this.date != null ? new SimpleDateFormat(Chronus.POPULAR_JP).format(this.date) : "")
            + ", amount=" + this.amount + ", left=" + this.left + ", right=" + this.right + ", memo="
            + this.memo + ", source=" + this.source + ", activity=" + this.activity + "]";
    }

}