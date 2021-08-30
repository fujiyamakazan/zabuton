package com.github.fujiyamakazan.zabuton.util.text;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * UTF-8のテキストファイルを読み書きします。
 * @author fujiyama
 */
public class Utf8Text extends TextFile {

    private static final long serialVersionUID = 1L;

    public Utf8Text(File file) {
        super(file);
    }

    public Utf8Text(String path) {
        super(path);
    }

    @Override
    protected Charset getCharset() {
        return StandardCharsets.UTF_8;
    }

}
