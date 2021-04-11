package com.github.fujiyamakazan.zabuton.util.text;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.KeyValueString;
import com.github.fujiyamakazan.zabuton.util.string.SubstringUtils;

/**
 * セパレーターで分割された「キー」と「値」で構成されるテキストファイルを読み書きします。
 * デフォルトのセパレーターは[=]です。
 * ファイルのイメージ
 * ----------
 * キー1:値1～
 * キー2:値2～
 *
 * @author fujiyama
 */
public class SeparateKeyValuesText extends KeyValuesText {

    private static final long serialVersionUID = 1L;

    private static final String SEPARATOR = "=";

    public SeparateKeyValuesText(Utf8Text utf8File) {
        super(utf8File);
    }

    public SeparateKeyValuesText(String pathname) {
        super(pathname);
    }

    @Override
    public List<KeyValueString> read() {
        List<KeyValueString> results = Generics.newArrayList();

        List<String> lines = utf8File.readLines();
        for (String line : lines) {
            if (line.contains(SEPARATOR)) {
                final String key = SubstringUtils.left(line, SEPARATOR);
                final String value;
                if (StringUtils.endsWith(line, SEPARATOR)) {
                    value = "";
                } else {
                    value = SubstringUtils.rightOfFirst(line, SEPARATOR);
                }
                results.add(new KeyValueString(key, value));
            }
        }
        return results;
    }

    @Override
    public void write() {
        List<String> lines = Generics.newArrayList();
        for (KeyValueString keyValue : super.keyValueString) {
            lines.add(keyValue.getKey() + SEPARATOR + nulToBlank(keyValue.getValue()));
        }
        utf8File.writeLines(lines);
    }
}
