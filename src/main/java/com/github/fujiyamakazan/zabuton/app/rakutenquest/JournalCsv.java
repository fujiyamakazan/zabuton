package com.github.fujiyamakazan.zabuton.app.rakutenquest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.CsvUtils;
import com.github.fujiyamakazan.zabuton.util.date.Chronus;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;
import com.opencsv.CSVParser;

public abstract class JournalCsv implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JournalCsv.class);

    private final File file;
    private final String[] fieldNames;

    public JournalCsv(File crawlerDir, String name, String[] fieldNames) {
        this.file = new File(crawlerDir, name);
        this.fieldNames = fieldNames;
    }

    public File getFile() {
        return this.file;
    }

    /**
     * ヘッダーのチェックをします。
     */
    public boolean validHeader(String line) {
        if (this.fieldNames == null) {
            //return line.startsWith("\"#\"");
            throw new RuntimeException("列名が未定義");
        }

        //return line.equals(getHeader() + "," + CsvUtils.convertString(this.fieldNames));
        return StringUtils.equals(line, getHeader());
    }

    public String getHeader() {
        return "\"#\"," + CsvUtils.convertString(this.fieldNames);
    }

    //    public static String getHeader() {
    //        return "\"#\"";
    //    }

    /**
     * 行を取得します。
     */
    public List<Row> getRrows() {
        List<String> lines = new Utf8Text(this.file).readLines();
        List<Row> rows = Generics.newArrayList();
        for (String line : lines) {
            line = line.trim();
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            if (line.startsWith("\"#\"")) {
                continue;
            }
            rows.add(new Row(line));
            //rows.add(rowFactory.apply(line));
        }
        return rows;
    }

    public class Row implements Serializable {
        private static final long serialVersionUID = 1L;
        private String line;
        private String[] csv;

        public Row(String line) {
            this.line = line;
            this.csv = CsvUtils.splitCsv(line);
        }

        /**
         * 列名を指定して値を取得します。
         */
        public String get(String name) {
            Integer index = null;
            try {
                index = getColumnIndex(name) + 1;
                return this.csv[index].trim();
            } catch (Exception e) {
                log.debug("name:" + name);
                log.debug("index:" + index);
                log.debug("csv:" + CsvUtils.convertString(this.csv));
                throw new RuntimeException(e);
            }
        }

        /**
         * 書式を指定して取得します。
         */
        public Date get(String name, DateFormat df) {
            String str = this.csv[getColumnIndex(name) + 1].trim();
            Date date;
            try {
                date = df.parse(str);
            } catch (ParseException e) {
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
        public boolean contains(String string) {
            for (String str : this.csv) {
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

        public String pickupDate() {
            return JournalCsv.this.pickupDate(this);
        }

        public String pickupMemo() {
            return JournalCsv.this.pickupMemo(this);
        }

        public String pickupKeywordOnsource() {
            return JournalCsv.this.pickupKeywordOnsource(this);
        }

        public String pickupAmount() {
            return JournalCsv.this.pickupAmount(this);
        }

        public Date getDateFromCsv(JournalsTerm term) {
            String strDate = pickupDate();
            if (StringUtils.length(strDate) == 5) {
                throw new RuntimeException("不正日付:" + this);
            }
            return getDateFromCsv(strDate, term);
        }

        protected Date getDateFromCsv(String strDate, JournalsTerm term) {
            final Date date;
            String pattern = JournalCsv.this.getDateFormat();
            if (term.in(strDate, pattern) == false) {
                date = null;
            } else {
                date = Chronus.parse(strDate, pattern);
            }
            return date;
        }

    }

    public static int getRowIndex(String line) throws IOException {
        return Integer.parseInt(new CSVParser().parseLine(line)[0]);
    }

    public int getColumnIndex(String name) {
        return Arrays.asList(this.fieldNames).indexOf(name);
    }

    /**
     * 行データから日付情報を取り出します。
     */
    public abstract String pickupDate(Row row);

    /**
     * 行データから金額情報を取り出します。
     */
    public abstract String pickupAmount(Row row);

    /**
     * 行データからメモ情報を取り出します。
     */
    public abstract String pickupMemo(Row row);
    /**
     * 行データからキーワード情報を抽出します。
     * 既定では、メモ情報をそのまま使用します。
     */
    public String pickupKeywordOnsource(Row row) {
        return pickupMemo(row);
    }

    /**
     * 日付情報のフォーマットを返します。
     */
    public abstract String getDateFormat();

}
