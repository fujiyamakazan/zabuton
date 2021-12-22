package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.CsvUtils;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;
import com.opencsv.CSVParser;

public class JournalCsv implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JournalCsv.class);

    private final File file;

    public JournalCsv(File crawlerDir, String name) {
        this.file = new File(crawlerDir, name);
    }

    public JournalCsv(String path) {
        this.file = new File(path);
    }

    public File getFile() {
        return file;
    }

    public static boolean validHeader(String line) {
        return line.startsWith("\"#\"");
    }

    public static String getHeader() {
        return "\"#\"";
    }

    public List<Row> getRrows() {
        List<String> lines = new Utf8Text(file).readLines();
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

        public String get(int i) {
            return csv[i + 1].trim();
        }

        public int getIndex() {
            return Integer.parseInt(csv[0]);
        }

        public String getData() {
            return line;
        }

        public int length() {
            return csv.length - 1;
        }
    }

    public static int getRowIndex(String line) throws IOException {
        return Integer.parseInt(new CSVParser().parseLine(line)[0]);
    }

}
