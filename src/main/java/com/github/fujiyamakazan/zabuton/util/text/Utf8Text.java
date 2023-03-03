package com.github.fujiyamakazan.zabuton.util.text;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

/**
 * UTF-8のテキストファイルを読み書きします。
 * @author fujiyama
 */
public class Utf8Text extends TextFile {
    private static final long serialVersionUID = 1L;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public Utf8Text(File file) {
        super(file);
    }

    public Utf8Text(String path) {
        super(path);
    }

    @Override
    protected Charset getCharset() {
        return CHARSET;
    }

    /**
     * UTF8でテキストデータを保存します。
     */
    public static void writeData(File file, String data) {
        try {
            FileUtils.write(file, data, CHARSET);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * UTF8でテキストデータを読み出します。
     */
    public static String readData(File file) {
        try {
            return FileUtils.readFileToString(file, CHARSET);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
