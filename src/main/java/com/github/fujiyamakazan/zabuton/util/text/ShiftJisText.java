package com.github.fujiyamakazan.zabuton.util.text;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

/**
 * Shift_JISのテキストファイルを読み書きします。
 * @author fujiyama
 */
public class ShiftJisText extends TextFile {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    private static final Charset CHARSET = Charset.forName("Shift_JIS");

    public ShiftJisText(File file) {
        super(file);
    }

    @Override
    protected Charset getCharset() {
        return CHARSET;
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

    /**
     * UTF8でテキストデータを読み出します。Java標準APIを使用。
     */
    public static String readString(File file) {
        return readString(file, CHARSET);
    }

}
