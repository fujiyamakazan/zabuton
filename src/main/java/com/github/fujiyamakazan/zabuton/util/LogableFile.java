package com.github.fujiyamakazan.zabuton.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.github.fujiyamakazan.zabuton.util.date.Chronus;

public class LogableFile extends File {
    private static final long serialVersionUID = 1L;

    private SimpleDateFormat dfYyyyMMdd = new SimpleDateFormat(Chronus.POPULAR_JP);
    private SimpleDateFormat dfYyyyMMddHHmmss = new SimpleDateFormat(dfYyyyMMdd.toPattern() + " HH:mm:ss");

    public LogableFile(String pathname) {
        super(pathname);
    }

    public void writeLog(String msg) {
        String text = getNow() + " " + msg;
        try {
            System.out.println(text);
            FileUtils.write(this, text + System.lineSeparator(), StandardCharsets.UTF_8, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getNow() {
        return dfYyyyMMddHHmmss.format(new Date());
    }
}
