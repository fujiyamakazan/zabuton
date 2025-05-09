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

    public Utf8Text(final File file) {
        super(file);
    }

    public Utf8Text(final String path) {
        super(path);
    }

    @Override
    protected Charset getCharset() {
        return CHARSET;
    }

    /**
     * UTF8でテキストデータを保存します。
     */
    public static void writeData(final File file, final String data) {
        try {
            FileUtils.write(file, data, CHARSET);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * UTF8でテキストデータを読み出します。Apache Commonsを利用
     * @deprecated readStringの利用を検討してください。（Charsetの検査が厳密になります。）
     */
    @Deprecated
    public static String readData(final File file) {
        try {
            return FileUtils.readFileToString(file, CHARSET);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * UTF8でテキストデータを読み出します。Java標準APIを使用。
     */
    public static String readString(final File file) {
        return readString(file, false);
    }

    /**
     * UTF8でテキストデータを読み出します。Java標準APIを使用。
     * @param lenient trueのとき、org.apache.commons.io.FileUtilsを使用して、文字コードの不整合を許容します。
     */
    public static String readString(
        final File file,
        final boolean lenient) {
        return readString(file, CHARSET, lenient);
    }

}
