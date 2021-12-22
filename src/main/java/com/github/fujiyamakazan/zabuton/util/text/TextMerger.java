package com.github.fujiyamakazan.zabuton.util.text;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.rakutenquest.JournalCsv;
import com.opencsv.CSVParser;

/**
 * テキストファイルの追記処理をします。
 * ・マスータテキストには処理済みレコードが累積して記録されているものとする。
 * ・マスターテキストに、追加テキストのレコードを追記する。ただし、マスターが無ければ新規作成する。
 * ・マスターに同一のレコードがあれば、そのレコードは追記しない。
 * ・処理は最新の追加テキストから順に行う。テキスト内の全てのレコードがマスターに存在すれば、それ以上遡及しない。
 * ・遡及回数は一定の上限値を決める。（無限ループの防止。）
 * ・マスターがあるにもかかわらず、最後に処理したテキストにもマスター追加済みレコードと一致するものが無ければ、
 * 遡及回数の不足と考えられる。処理を中断し、警告をする。
 */
public abstract class TextMerger implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        int year = 2021;
        JournalCsv fileMaster = new JournalCsv("C:\\tmp\\textMaster" + year + ".txt");
        List<File> additionlFiles = Generics.newArrayList();
        additionlFiles.add(new File("C:\\tmp\\text追加2.txt")); // 処理は最新の追加テキストから順に行う。
        additionlFiles.add(new File("C:\\tmp\\text追加1.txt"));

        TextMerger textMerger = new TextMerger(fileMaster) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean isAvailableLine(String line) {
                try {
                    return new CSVParser().parseLine(line)[0].equals(String.valueOf(year));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        for (File additionalFile : additionlFiles) { // 遡及回数は一定の上限値を決める。（無限ループの防止。）

            Utf8Text utf8Text = new Utf8Text(additionalFile);
            String additionalText = utf8Text.read();

            textMerger.stock(Arrays.asList(additionalText.split("\n")));
            if (textMerger.hasNext() == false) {
                break;
            }
        }
        if (textMerger.isFinish() == false) {
            /* マスターがあるにもかかわらず、最後に処理したテキストにもマスター追加済みレコードと一致するものが無ければ、
             * 遡及回数の不足と考えられる。処理を中断し、警告をする。
             */
            throw new RuntimeException("遡及処理の上限回数が不足しています。");
        }
        textMerger.flash();

    }

    protected abstract boolean isAvailableLine(String line);

    private final JournalCsv masterText;
    private final List<String> masterLines = Generics.newArrayList();
    private final List<String> buffer = Generics.newArrayList();

    private boolean hasNext = true;
    private boolean existMaster = false;
    private int maxRowIndex = 0;

    public boolean hasNext() {
        return hasNext;
    }

    /**
     * コンストラクタです。マスターテキストを登録します。
     */
    public TextMerger(JournalCsv masterText) {
        this.masterText = masterText;

        boolean first = true;

        for (String line : new Utf8Text(masterText.getFile()).readLines()) {
            line = line.trim();

            if (first) {
                if (JournalCsv.validHeader(line) == false) {
                    throw new RuntimeException("見出し行を持たない不正なマスターです。" + line);
                }
                first = false;
                continue;
            }

            if (masterLines.contains(line)) {
                throw new RuntimeException("重複するレコードを持つ不正なマスターです。" + line);
            }

            /* 行番号取得 */
            try {
                int rowIndex = JournalCsv.getRowIndex(line);
                maxRowIndex = Math.max(maxRowIndex, rowIndex);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            int headIndex = line.indexOf(',') + 1; // 行番号除去
            line = line.substring(headIndex);

            if (isSkipLine(line)) {
                continue;
            }

            masterLines.add(line);

        }
    }





    /** マスターがあるにもかかわらず、
     * 最後に処理したテキストにもマスター追加済みレコードと一致するものが無ければ、
     * 遡及回数の不足と考えられる。処理を中断し、警告をする。
     */
    public boolean isFinish() {
        return masterLines.isEmpty() || existMaster;
    }

    /**
     * マスターにある同一のレコード以外をbufferに仮保存する。
     * @return 続きがあればTrueを返す。
     */
    public boolean stock(List<String> lines) {

        List<String> joins = Generics.newArrayList();
        for (String additionalLine : lines) {
            additionalLine = additionalLine.trim();
            if (StringUtils.isEmpty(additionalLine)) {
                continue;
            }
            if (isSkipLine(additionalLine)) {
                continue;
            }
            if (masterLines.contains(additionalLine)) {
                /*  マスターに同一のレコードがあれば、そのレコードは追記しない。 */
                existMaster = true; // 重複する行があったことを記録

            } else {
                String line = additionalLine;
                joins.add(line);
            }
        }
        if (joins.isEmpty()) {
            /* テキスト内の全てのレコードがマスターに存在すれば、それ以上遡及しない。*/
            hasNext = false;

        } else {
            /* 最新のファイルから処理しているので、後から処理したものを前方に追加する。*/
            buffer.addAll(0, joins);
        }

        return hasNext;

    }

    /**
     * 仮保存していたレコードをマスターに保存します。
     */
    public void flash() {

        if (isFinish() == false) {
            /* マスターがあるにもかかわらず、最後に処理したテキストにもマスター追加済みレコードと一致するものが無ければ、
             * 遡及回数の不足と考えられる。処理を中断し、警告をする。
             */
            throw new RuntimeException("遡及処理の上限回数が不足しています。");
        }

        Utf8Text master = new Utf8Text(masterText.getFile());
        if (masterText.getFile().exists()) {
            if (master.read().endsWith("\n") == false) {
                buffer.add(0, "\n");
            }
        }

        /* 行番号付与 */
        List<String> tmp = Generics.newArrayList();
        for (String line : buffer) {
            maxRowIndex = maxRowIndex + 1;
            line = "\"" + maxRowIndex + "\"," + line;
            tmp.add(line);
        }
        buffer.clear();
        buffer.addAll(tmp);

        /* マスターテキストに、追加テキストのレコードを追記する。ただし、マスターが無ければ新規作成する。 */
        if (masterText.getFile().exists()) {
            master.writeLines(buffer, true);
        } else {
            buffer.add(0, JournalCsv.getHeader()); // 見出し行
            master.writeLines(buffer);
        }

        buffer.clear(); // 使用済みの為削除
    }


    protected boolean isSkipLine(String line) {
        if (StringUtils.isEmpty(line)) {
            return false;
        }
        return isAvailableLine(line) == false;
    }

}
