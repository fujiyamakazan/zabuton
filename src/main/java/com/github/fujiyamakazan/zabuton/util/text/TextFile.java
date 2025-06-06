package com.github.fujiyamakazan.zabuton.util.text;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

/**
 * テキストファイルを読み書きします。
 * @author fujiyama
 */
public abstract class TextFile implements Serializable {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    private static final long serialVersionUID = 1L;

    /**
     * 改行コードはプラットフォームにより異なります。
     * 現在、いずれのプラットフォームにも共通する「\n」を定数として宣言します。
     */
    public static final String LINE_SEPARATOR_LF = "\n";

    protected final File file;

    public TextFile(final File file) {
        this.file = file;
    }

    public File getFile() {
        return this.file;
    }

    public TextFile(final String pathname) {
        this(new File(pathname));
    }

    protected abstract Charset getCharset();

    /**
     * 文字セット「UTF-8」を指定してファイルから複数行のテキストを取得します。
     * 先頭にBOMがあれば除外します。
     * 各行はトリムされ、改行コードを含みません。
     * 空行は除外します。
     * @return ファイルから取得したテキスト。ファイルが無ければ空のリストを返します。
     */
    public List<String> readLines() {
        return readLines(null);
    }

    /**
     * 文字セット「UTF-8」を指定してファイルから複数行のテキストを取得します。
     * 先頭にBOMがあれば除外します。
     * 各行はトリムされ、改行コードを含みません。
     * 空行は除外します。
     * ignoreHead からはじまる行（コメント行）は除外します。
     * @return ファイルから取得したテキスト。ファイルが無ければ空のリストを返します。
     */
    public List<String> readLines(final String ignoreHead) {
        final String string = read();
        if (string == null) {
            return Generics.newArrayList();
        }
        final List<String> lines = Generics.newArrayList();
        for (String line : string.split(LINE_SEPARATOR_LF)) {
            if (doTrim()) {
                line = line.trim();
            }
            if (StringUtils.isEmpty(line)) {
                continue; // 空行スキップ
            }
            if (StringUtils.isNotEmpty(ignoreHead)) {
                if (StringUtils.startsWith(line, ignoreHead)) {
                    continue; // コメント行スキップ
                }
            }
            lines.add(line);
        }
        return lines;
    }

    protected boolean doTrim() {
        return true;
    }

    /**
     * 文字セット「UTF-8」を指定してファイルからテキストを取得します。
     * 先頭にBOMがあれば除外します。
     * @return ファイルから取得したテキスト。ファイルが無ければnullを返します。
     */
    public String read() {
        if (this.file.exists() == false) {
            return null;
        }

        String text;
        try {
            text = FileUtils.readFileToString(this.file, getCharset());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        /* 先頭にBOMがあれば除去する */
        if (text == null || text.length() == 0) {
            /* 処理なし */
        } else {
            final String first = Integer.toHexString(text.charAt(0));
            if (StringUtils.equals(first, "feff")) {
                text = text.substring(1); // 先頭を除去
            }
        }
        return text;

    }

    /**
     * 文字セットを指定してテキストを保存します。
     * @param data テキスト
     */
    public void write(final String data) {
        write(data, false);
    }

    /**
     * 文字セット「UTF-8」を指定してテキストを保存します。
     * @param data テキスト
     * @param append 追記モードを指定する場合はTrue
     */
    public void write(final String data, final boolean append) {
        try {
            FileUtils.write(this.file, data, getWriteCharset(), append);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 保存するときの文字セットを指定します。取得するときと変更するときにはオーバーライドします。
     */
    protected Charset getWriteCharset() {
        return getCharset();
    }

    /**
     * 文字セット「UTF-8」を指定して複数行のテキストを保存します。
     * @param lines 複数行のテキスト
     */
    public void writeLines(final List<String> lines) {
        writeLines(lines, false);
    }

    /**
     * 文字セット「UTF-8」を指定して複数行のテキストを保存します。
     * @param lines 複数行のテキスト
     */
    public void writeLines(final List<String> lines, final boolean append) {
        try {
            FileUtils.writeLines(this.file, getWriteCharset().toString(), lines, append);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 最終行を返します。
     */
    public String getFinalLine() {
        String text = read().trim();
        final int lastLn = text.lastIndexOf("\n");
        if (lastLn != -1) {
            text = text.substring(lastLn + 1);
        }
        return text;
    }

    /**
     * 標準APIを使用して読み取ります。
     * @param lenient trueのとき、org.apache.commons.io.FileUtilsを使用して、文字コードの不整合を許容します。
     */
    public static String readString(
        final File file,
        final Charset charset,
        final boolean lenient) {

        String str;
        try {
            str = Files.readString(file.toPath(), charset);
        } catch (final IOException e) {
            if (lenient) {
                try {
                    return FileUtils.readFileToString(file, charset);
                } catch (final IOException e1) {
                    throw new RuntimeException(e1);
                }
            }
            LOGGER.error(file.getAbsolutePath() + "の読取りに失敗しました。");
            throw new RuntimeException(e);
        }
        return str;
    }
}
