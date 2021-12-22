package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.Serializable;

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



}
