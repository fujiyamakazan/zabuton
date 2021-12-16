package com.github.fujiyamakazan.zabuton.util.text;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

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

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        File fileMaster = new File("C:\\tmp\\textMaster.txt");
        List<File> additionlFiles = Generics.newArrayList();
        additionlFiles.add(new File("C:\\tmp\\text追加2.txt")); // 処理は最新の追加テキストから順に行う。
        additionlFiles.add(new File("C:\\tmp\\text追加1.txt"));

        TextMerger textMerger = new TextMerger(fileMaster);
        for (File additionalFile : additionlFiles) { // 遡及回数は一定の上限値を決める。（無限ループの防止。）

            Utf8Text utf8Text = new Utf8Text(additionalFile);
            String additionalText = utf8Text.read();

            textMerger.stock(additionalText);
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

    private final File masterText;
    private final List<String> masterLines = Generics.newArrayList();
    private final List<String> buffer = Generics.newArrayList();

    private boolean hasNext = true;
    private boolean existMaster = false;

    private boolean hasNext() {
        return hasNext;
    }

    /**
     * コンストラクタです。マスターテキストを登録します。
     */
    public TextMerger(File masterText) {
        this.masterText = masterText;
        for (String line : new Utf8Text(masterText).readLines()) {
            line = line.trim();
            if (StringUtils.isEmpty(line) == false) {
                masterLines.add(line);
            }

        }
    }

    /** マスターがあるにもかかわらず、
     * 最後に処理したテキストにもマスター追加済みレコードと一致するものが無ければ、
     * 遡及回数の不足と考えられる。処理を中断し、警告をする。
     */
    private boolean isFinish() {
        return masterLines.isEmpty() == false && existMaster;
    }


    /**
     * マスターにある同一のレコード以外をbufferに仮保存する。
     */
    public void stock(String additionalText) {

        List<String> joins = Generics.newArrayList();
        for (String additionalLine : additionalText.split("\n")) {
            additionalLine = additionalLine.trim();
            if (StringUtils.isEmpty(additionalLine)) {
                continue;
            }
            if (masterLines.contains(additionalLine)) {
                /*  マスターに同一のレコードがあれば、そのレコードは追記しない。 */
                existMaster = true; // 重複する行があったことを記録

            } else {
                joins.add(additionalLine);
            }
        }
        if (joins.isEmpty()) {
            /* テキスト内の全てのレコードがマスターに存在すれば、それ以上遡及しない。*/
            hasNext = false;

        } else {
            /* 最新のファイルから処理しているので、後から処理したものを前方に追加する。*/
            buffer.addAll(0, joins);
        }

    }

    /**
     * 仮保存していたレコードをマスターに保存します。
     */
    public void flash() {

        Utf8Text master = new Utf8Text(masterText);
        if (masterText.exists()) {
            if (master.read().endsWith("\n") == false) {
                buffer.add(0, "\n");
            }
        }

        /* マスターテキストに、追加テキストのレコードを追記する。ただし、マスターが無ければ新規作成する。 */
        master.writeLines(buffer, true);

        buffer.clear(); // 使用済みの為削除
    }

}
