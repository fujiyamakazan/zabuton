package com.github.fujiyamakazan.zabuton.util.exec;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.exec.RuntimeExec.RuntimeExecResult;

/**
 * Windowsのcmd.exeにアクセスします。
 * @author fujiyama
 */
public class CmdAccessObject implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CmdAccessObject.class);

    /**
     * コマンドプロンプトを使用する文字コードをUTF-8へ変更した後、
     * 処理結果をファイルに出力します。(パラメータ付きのコマンドに未対応)
     */
    public static void executeCmdToUtf8Text(File target, String command) {
        command = "chcp 65001|" + command + " > " + target.getAbsolutePath();
        executeCmd(command);
    }

    /**
     * コマンドプロンプトで処理を実行します。
     * @return リターンコードが0のときにTrueを返します。
     */
    public static RuntimeExecResult executeCmd(String... cmdarray) {
        List<String> list = Generics.newArrayList();
        list.add("cmd");
        list.add("/c");
        for (String p : cmdarray) {
            list.add(p);
        }
        return RuntimeExec.exec(list.toArray(new String[list.size()]));
    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {
        LOGGER.debug(executeCmd("date", "/t").toString());
        LOGGER.debug(executeCmd("dir").toString());
        LOGGER.debug(executeCmd("abc").toString());

    }

}
