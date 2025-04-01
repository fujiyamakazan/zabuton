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
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());
    public static final Charset CHARSET = StandardCharsets.UTF_8;

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
     * UTF8でテキストデータを読み出します。Apache Commonsを利用
     * @deprecated readStringの利用を検討してください。（Charsetの検査が厳密になります。）
     */
    @Deprecated
    public static String readData(File file) {
        try {
            return FileUtils.readFileToString(file, CHARSET);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * UTF8でテキストデータを読み出します。Java標準APIを使用。
     */
    public static String readString(File file) {
        return readString(file, CHARSET);
    }

}
