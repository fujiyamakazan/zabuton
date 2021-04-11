package com.github.fujiyamakazan.zabuton.util.text;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

/**
 * UTF-8のテキストファイルを読み書きします。
 * @author fujiyama
 */
public class Utf8Text implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * 改行コードはプラットフォームにより異なります。
     * 現在、いずれのプラットフォームにも共通する「\n」を定数として宣言します。
     */
    public static final String LINE_SEPARATOR_LF = "\n";

    protected final File file;

    public Utf8Text(File file) {
        this.file = file;
    }

    public Utf8Text(String pathname) {
        this(new File(pathname));
    }

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
    public List<String> readLines(String ignoreHead) {
        String string = read();
        if (string == null) {
            return Generics.newArrayList();
        }
        List<String> lines = Generics.newArrayList();
        for (String line : string.split(LINE_SEPARATOR_LF)) {
            line = line.trim();
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

    /**
     * 文字セット「UTF-8」を指定してファイルからテキストを取得します。
     * 先頭にBOMがあれば除外します。
     * @return ファイルから取得したテキスト。ファイルが無ければnullを返します。
     */
    public String read() {
        if (file.exists() == false) {
            return null;
        }

        String text;
        try {
            text = FileUtils.readFileToString(file, CHARSET);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /* 先頭にBOMがあれば除去する */
        if (text == null || text.length() == 0) {
            /* 処理なし */
        } else {
            String first = Integer.toHexString(text.charAt(0));
            if (StringUtils.equals(first, "feff")) {
                text = text.substring(1); // 先頭を除去
            }
        }
        return text;

    }

    /**
     * 文字セット「UTF-8」を指定してテキストを保存します。
     * @param data テキスト
     */
    public void write(String data) {
        write(data, false);
    }

    /**
     * 文字セット「UTF-8」を指定してテキストを保存します。
     * @param data テキスト
     * @param append 追記モードを指定する場合はTrue
     */
    public void write(String data, boolean append) {
        try {
            FileUtils.write(file, data, CHARSET, append);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 文字セット「UTF-8」を指定して複数行のテキストを保存します。
     * @param lines 複数行のテキスト
     */
    public void writeLines(List<String> lines) {
        try {
            FileUtils.writeLines(file, CHARSET.toString(), lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File getFile() {
        return file;
    }

}
