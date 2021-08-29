package com.github.fujiyamakazan.zabuton.util.text;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Shift_JISのテキストファイルを読み書きします。
 * @author fujiyama
 */
public class ShiftJisText extends TextFile {

    private static final long serialVersionUID = 1L;

    public ShiftJisText(File file) {
        super(file);
    }

    @Override
    protected Charset getCharset() {
        return Charset.forName("Shift_JIS");
    }

}
