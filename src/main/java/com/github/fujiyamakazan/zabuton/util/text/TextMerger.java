package com.github.fujiyamakazan.zabuton.util.text;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.rakutenquest.JournalCsv;
import com.github.fujiyamakazan.zabuton.util.date.Chronus;
import com.github.fujiyamakazan.zabuton.util.jframe.JFrameUtils;
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
public class TextMerger implements Serializable {

    private static final long serialVersionUID = 1L;

    private final JournalCsv masterText;
    private final List<String> masterLines = Generics.newArrayList();
    private final List<String> buffer = Generics.newArrayList();

    private boolean hasNext = true;
    private boolean existMaster = false;
    private int maxRowIndex = 0;

    //private final TermAction termAction;
    private final String datePattern;

    public boolean hasNext() {
        return this.hasNext;
    }

    /**
     * コンストラクタです。マスターテキストを登録します。
     */
    //public TextMerger(JournalCsv masterText, String availableKeyWord) {
    public TextMerger(JournalCsv masterText, String datePattern) {
        this.masterText = masterText;
        this.datePattern = datePattern;
        boolean first = true;

        for (String line : new Utf8Text(masterText.getFile()).readLines()) {
            line = line.trim();

            if (first) {
                if (masterText.validHeader(line) == false) {
                    throw new RuntimeException("見出し行を持たない不正なマスターです。" + line);
                }
                first = false;
                continue;
            }

            if (this.masterLines.contains(line)) {
                throw new RuntimeException("重複するレコードを持つ不正なマスターです。" + line);
            }

            /* 行番号取得 */
            try {
                int rowIndex = JournalCsv.getRowIndex(line);
                this.maxRowIndex = Math.max(this.maxRowIndex, rowIndex);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            int headIndex = line.indexOf(',') + 1; // 行番号除去
            line = line.substring(headIndex);

            if (isSkipLine(line)) {
                continue;
            }

            this.masterLines.add(line);

        }
    }

    /** マスターがあるにもかかわらず、
     * 最後に処理したテキストにもマスター追加済みレコードと一致するものが無ければ、
     * 遡及回数の不足と考えられる。処理を中断し、警告をする。
     */
    public boolean isFinish() {
        return this.masterLines.isEmpty() || this.existMaster;
    }

    /**
     * マスターにある同一のレコード以外をbufferに仮保存する。
     * @return 続きがあればTrueを返す。
     */
    public boolean stock(List<String> dailyLines) {

        List<String> joins = Generics.newArrayList();
        List<String> masterLines = standardize(this.masterLines);

        for (String dailyLine : dailyLines) {
            dailyLine = dailyLine.trim();
            if (StringUtils.isEmpty(dailyLine)) {
                continue;
            }
            if (isSkipLine(dailyLine)) {
                continue;
            }

            //            if (dailyLine.contains("2022/05/02") && dailyLine.contains("SUICA MOBILE PAYMENT")) {
            //                System.out.println(dailyLine);
            //
            //                for (String msterLine: masterLines) {
            //                    if (msterLine.contains("2022/05/02") && msterLine.contains("SUICA MOBILE PAYMENT")) {
            //                        System.out.println(msterLine);
            //                    }
            //                }
            //
            //            }

            String al = standardize(dailyLine); // 標準化

            if (masterLines.contains(al)) {
                /*  マスターに同一のレコードがあれば、そのレコードは追記しない。 */
                this.existMaster = true; // 重複する行があったことを記録

            } else {
                joins.add(dailyLine);
            }
        }
        if (joins.isEmpty()) {
            /* テキスト内の全てのレコードがマスターに存在すれば、それ以上遡及しない。*/
            this.hasNext = false;

        } else {
            /* 最新のファイルから処理しているので、後から処理したものを前方に追加する。*/
            this.buffer.addAll(0, joins);

        }

        return this.hasNext;

    }

    /**
     * 標準化するための実装で上書きすることができます。
     */
    protected List<String> standardize(List<String> lines) {
        List<String> result = Generics.newArrayList();
        result.addAll(lines);
        return result;
    }

    /**
     * 標準化するための実装で上書きすることができます。
     */
    protected String standardize(String line) {
        List<String> list = Generics.newArrayList();
        list.add(line);
        return standardize(list).get(0);
    }

    /**
     * 仮保存していたレコードをマスターに保存します。
     */
    public void flash() {

        if (isFinish() == false) {
            /* マスターがあるにもかかわらず、最後に処理したテキストにもマスター追加済みレコードと一致するものが無ければ、
             * 遡及回数の不足と考えられる。処理を中断し、警告をする。
             */
            if (JFrameUtils.showConfirmDialog("遡及処理の上限回数が不足しています。続行しますか？") == false) {
                throw new RuntimeException("遡及処理の上限回数が不足しています。");
            }
        }

        Utf8Text master = new Utf8Text(this.masterText.getFile());
        if (this.masterText.getFile().exists()) {
            if (master.read().endsWith("\n") == false) {
                this.buffer.add(0, "\n");
            }
        }

        /* 行番号付与 */
        List<String> tmp = Generics.newArrayList();
        for (String line : this.buffer) {
            this.maxRowIndex = this.maxRowIndex + 1;
            line = "\"" + this.maxRowIndex + "\"," + line;
            tmp.add(line);
        }
        this.buffer.clear();
        this.buffer.addAll(tmp);

        /* マスターテキストに、追加テキストのレコードを追記する。ただし、マスターが無ければ新規作成する。 */
        if (this.masterText.getFile().exists()) {
            master.writeLines(this.buffer, true);
        } else {
            this.buffer.add(0, JournalCsv.getHeader()); // 見出し行
            master.writeLines(this.buffer);
        }

        this.buffer.clear(); // 使用済みの為削除
    }

    protected boolean isSkipLine(String line) {
        if (StringUtils.isEmpty(line)) {
            return false;
        }
        return isAvailableLine(line) == false;
    }

    protected boolean isAvailableLine(String line) {
        try {

            //return new CSVParser().parseLine(line)[0].startsWith(availableKeyWord);

            return in(new CSVParser().parseLine(line)[0]);

        } catch (IOException e) {
            return false;
        }
    }

    protected boolean in(String value) {
        try {
            Chronus.parse(value, this.datePattern);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
