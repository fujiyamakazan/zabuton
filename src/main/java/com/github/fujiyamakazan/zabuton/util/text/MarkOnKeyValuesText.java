package com.github.fujiyamakazan.zabuton.util.text;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.KeyValueString;

/**
 * 「■」から始まる「キー」とその次の行から始まる「値」で構成されるテキストファイルを読み書きします。
 * ファイルのイメージ
 * ----------
 * ■キー1
 * 値1～
 * ■キー2
 * 値2～
 *
 * @author fujiyama
 */
public class MarkOnKeyValuesText extends KeyValuesText {

    private static final long serialVersionUID = 1L;

    private static final String MARK = "■";

    public MarkOnKeyValuesText(Utf8Text utf8File) {
        super(utf8File);
    }

    public MarkOnKeyValuesText(String pathname) {
        super(pathname);
    }

    @Override
    public List<KeyValueString> read() {
        List<KeyValueString> results = Generics.newArrayList();

        String head = null;
        StringBuilder sb = new StringBuilder();
        List<String> lines = utf8File.readLines();
        for (String line : lines) {

            if (StringUtils.startsWith(line, MARK)) {
                /* 見出し行を検知 */
                String buff = sb.toString();
                if (buff.isEmpty() == false) {
                    /* バッファ処理 */
                    results.add(new KeyValueString(head, buff.trim()));
                    head = "";
                    sb = new StringBuilder();
                }
                /* 新たなバッファの準備 */
                head = line.substring(MARK.length()).trim();
                sb = new StringBuilder();
                continue;
            } else {
                sb.append(line + Utf8Text.LINE_SEPARATOR_LF);
            }
        }
        /* バッファ処理 */
        String buff = sb.toString();
        if (buff.isEmpty() == false) {
            results.add(new KeyValueString(head, buff.trim()));
        }
        return results;
    }

    @Override
    public void write() {
        List<String> lines = Generics.newArrayList();
        for (KeyValueString keyValue : super.keyValueString) {
            lines.add(MARK + keyValue.getKey());
            lines.add(nulToBlank(keyValue.getValue()));
        }
        utf8File.writeLines(lines);
    }
}
