package com.github.fujiyamakazan.zabuton.app.rakutenquest;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.CsvUtils;
import com.github.fujiyamakazan.zabuton.util.date.Chronus;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;
import com.opencsv.CSVParser;

public abstract class JournalCsvRow implements I_JournalCsvRow {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    private final String line;
    private final String[] csv;
    private final String[] fieldNames;

    public JournalCsvRow() {
        this.line = "";
        this.csv = null;
        this.fieldNames = null;
    }

    public JournalCsvRow(
        final String[] fieldNames,
        final String line) {

        this.fieldNames = fieldNames;
        this.line = line;
        this.csv = CsvUtils.splitCsv(line);
    }

    private int getColumnIndex(final String name) {
        return Arrays.asList(this.fieldNames).indexOf(name);
    }

    /**
     * 列名を指定して値を取得します。
     */
    public String get(final String name) {
        Integer index = null;
        try {
            index = getColumnIndex(name) + 1;
            return this.csv[index].trim();
        } catch (final Exception e) {
            LOGGER.debug("name:" + name);
            LOGGER.debug("index:" + index);
            LOGGER.debug("csv:" + CsvUtils.convertString(this.csv));
            throw new RuntimeException(e);
        }
    }

    /**
     * 書式を指定して取得します。
     */
    public Date get(final String name, final DateFormat df) {
        final String str = this.csv[getColumnIndex(name) + 1].trim();
        Date date;
        try {
            date = df.parse(str);
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }

        return date;
    }

    public int getIndex() {
        return Integer.parseInt(this.csv[0]);
    }

    public String getData() {
        return this.line;
    }

    public int length() {
        return this.csv.length - 1;
    }

    /**
     * 含まれるかを検査します。
     */
    public boolean contains(final String string) {
        for (final String str : this.csv) {
            if (StringUtils.equals(str, string)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Row [line=" + this.line + ", csv=" + Arrays.toString(this.csv) + "]";
    }

    //        public String pickupDate() {
    //            return JournalCsv.this.pickupDate(this);
    //        }

    //        public String pickupMemo() {
    //            return JournalCsv.this.pickupMemo(this);
    //        }

    //        public String pickupKeywordOnsource() {
    //            return JournalCsv.this.pickupKeywordOnsource(this);
    //        }

    //        public String pickupAmount() {
    //            return JournalCsv.this.pickupAmount(this);
    //        }

    public Date getDateFromCsv(final JournalsTerm term) {
        final String strDate = pickupDate();
        if (StringUtils.length(strDate) == 5) {
            throw new RuntimeException("不正日付:" + this);
        }
        return getDateFromCsv(strDate, term);
    }

    protected Date getDateFromCsv(final String strDate, final JournalsTerm term) {
        final Date date;
        final String pattern = getDateFormat();
        if (term.in(strDate, pattern) == false) {
            date = null;
        } else {
            date = Chronus.parse(strDate, pattern);
        }
        return date;
    }

    /**
     * 日付情報を取り出します。
     */
    protected abstract String pickupDate();

    /**
     * 日付情報のフォーマットを返します。
     */
    protected abstract String getDateFormat();

    /**
     * メモ情報を取り出します。
     */
    protected abstract String pickupMemo();

    /**
     * 金額情報を取り出します。
     */
    protected abstract String pickupAmount();

    public static int getRowIndex(final String line) throws IOException {
        return Integer.parseInt(new CSVParser().parseLine(line)[0]);
    }

    public static List<Journal> createJournals(
        final String sourceName,
        final JournalsTerm term,
        final List<JournalCsvRow> rows) {

        final List<Journal> journals = Generics.newArrayList();
        for (final JournalCsvRow row : rows) {

            final Journal journal = new Journal();
            journal.setSource(sourceName);
            //journal.setRawOnSource(row.getData()); 一旦取下げ
            journal.setRowIndex(String.valueOf(row.getIndex()));


            final Date date = row.getDateFromCsv(term);

            if (date == null) {
                continue;
            }
            journal.setDate(date);
            //journal.setKeywordOnSource(pickupKeywordOnsource(row));
            //journal.setKeywordOnSource(row.pickupKeywordOnsource()); //一旦取下げ
            //journal.setMemo(pickupMemo(row));
            journal.setMemo(row.pickupMemo());
            //journal.setAmount(MoneyUtils.toInt(pickupAmount(row)));
            journal.setAmount(MoneyUtils.toInt(row.pickupAmount()));

            journals.add(journal);

        }
        return journals;
    }

}

//package com.github.fujiyamakazan.zabuton.app.rakutenquest;
//
//import java.io.Serializable;
//
//public abstract class JournalCsv implements Serializable {
//  private static final long serialVersionUID = 1L;
//  static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
//      .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());
//
//  //private final File file;
//  //private final String[] fieldNames;
//
//  //public JournalCsv(final String[] fieldNames) {
//
//      //this.file = new File(crawlerDir, name);
//      //this.fieldNames = fieldNames;
//  //}
//
//  //public File getFile() {
//  //    return this.file;
//  //}
//
////  /**
////   * ヘッダーのチェックをします。
////   */
////  public boolean validHeader(final String line) {
////      if (this.fieldNames == null) {
////          //return line.startsWith("\"#\"");
////          throw new RuntimeException("列名が未定義");
////      }
////      //return line.equals(getHeader() + "," + CsvUtils.convertString(this.fieldNames));
////      return StringUtils.equals(line, getHeader());
////  }
//
//  //public String getHeader() {
//  //    return "\"#\"," + CsvUtils.convertString(this.fieldNames);
//  //}
//
//  //public int getColumnIndex(final String name) {
//  //    return Arrays.asList(this.fieldNames).indexOf(name);
//  //}
//
////  /**
////   * 行データから日付情報を取り出します。
////   */
////  public abstract String pickupDate(JournalCsvRow row);
//
////  /**
////   * 行データから金額情報を取り出します。
////   */
////  public abstract String pickupAmount(JournalCsvRow row);
//
////  /**
////   * 行データからメモ情報を取り出します。
////   */
////  public abstract String pickupMemo(JournalCsvRow row);
//
////  /**
////   * 行データからキーワード情報を抽出します。
////   * 既定では、メモ情報をそのまま使用します。
////   */
////  public String pickupKeywordOnsource(Row row) {
////      return pickupMemo(row);
////  }
//
////  /**
////   * 日付情報のフォーマットを返します。
////   */
////  public abstract String getDateFormat();
//
////  /**
////   * CSVから仕訳レコードを作成します。
////   *
////   * 仕訳.記録元 ← JournalFactory.記録元名(インスタンス化時に指定)
////   * 仕訳.生データ ← 一行のテキスト
////   * 仕訳.Index(ID) ← CSVの行番号
////   * 仕訳.日付 ← CSVの日付
////   * 仕訳.キーワード ← キーワード情報（規定はJournalFactory#pickupMemo()の実装による）
////   * 仕訳.メモ ← キーワード情報（JournalFactory#pickupMemo()の実装による）
////   * 仕訳.金額 ← CSVの金額
////   *
////   * 日付が対象期間のデータのみ作成します。
////   *
////   */
////  public List<Journal> createJournals(
////      final String sourceName,
////      final JournalsTerm term,
////      final File fileMaster,
////      final String[] fieldNames) {
////
////      final List<JournalCsvRow> rows = this.getRrows(fieldNames, fileMaster);
////      return createJournals(sourceName, term, rows);
////  }
////
////  /**
////   * 行を取得します。
////   */
////  private List<JournalCsvRow> getRrows(final String[] fieldNames, final File fileMaster) {
////      final List<String> lines = new Utf8Text(fileMaster).readLines();
////      final List<JournalCsvRow> rows = Generics.newArrayList();
////      for (String line : lines) {
////          line = line.trim();
////          if (StringUtils.isEmpty(line)) {
////              continue;
////          }
////          if (line.startsWith("\"#\"")) {
////              continue;
////          }
////          rows.add(new JournalCsvRow(fieldNames, line));
////          //rows.add(rowFactory.apply(line));
////      }
////      return rows;
////  }
//
//}
