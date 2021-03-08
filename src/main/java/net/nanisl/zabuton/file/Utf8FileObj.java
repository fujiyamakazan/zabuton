package net.nanisl.zabuton.file;

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
 * UTF-8のテキストファイルを操作するオブジェクト
 *
 * @author fujiyama
 */
public class Utf8FileObj implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    final private File file;

    /**
     * ofメソッドからインスタンス化されることを想定し、コンストラクタはprivateとする
     * @param file
     */
    private Utf8FileObj(File file) {
        this.file = file;
    }

    public static Utf8FileObj of(File file) {
        return new Utf8FileObj(file);
    }

    public String readFileToString() {
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
            // 空は対象外
        } else {
            String first = Integer.toHexString(text.charAt(0));
            if (StringUtils.equals(first, "feff")){
                text = text.substring(1); // 先頭を除去
            }
        }
        return text;

    }

    public void writeString(String text) {
        try {
            FileUtils.write(file, text, CHARSET);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void writeString(String text, boolean append) {
        try {
            FileUtils.write(file, text, CHARSET, append);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeListString(List<String> lines) {
        try {
            FileUtils.writeLines(file, CHARSET.toString(), lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> readLines() {
        return readLines(null);
    }
    public List<String> readLines(String commentHeader) {
        List<String> lines = Generics.newArrayList();
        for (String line: readFileToString().split("\n")) {
            line = line.trim();
            if (StringUtils.isEmpty(line)) continue;
            if (StringUtils.isNotEmpty(commentHeader)) {
                if (StringUtils.startsWith(line, commentHeader)) continue;
            }

            lines.add(line);
        }
        return lines;
    }
	/**
	 * @return 行ごとのリスト。要素はトリムし、空文字は除外したもの。
	 */
	public List<String>  readTrimeLinesIgnoreEmpty() {
		List<String> list = Generics.newArrayList();
		for (String line: readLines()) {
			line = line.trim();
			if (StringUtils.isNotEmpty(line)) {
				list.add(line);
			}
		}
		return list;
	}

	@Override
	public String toString() {
		return readFileToString();
	}




}
