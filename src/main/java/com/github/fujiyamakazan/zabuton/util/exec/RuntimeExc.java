package com.github.fujiyamakazan.zabuton.util.exec;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.lang.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runtime#exc のユーティリティです。
 * @author fujiyama
 */
public class RuntimeExc implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeExc.class);

    /* 標準出力 */
    private List<String> outs = new ArrayList<String>();
    /* 標準エラー */
    private List<String> errs = new ArrayList<String>();

    /* リターンコード = 0 */
    private boolean success = false;

    /**
     * コマンドプロンプトを使用する文字コードをUTF-8へ変更した後、
     * 処理結果をファイルに出力します。(パラメータ付きのコマンドに未対応)
     *
     * TODO CmdAccessObjectへの移行
     *
     */
    public static void executeCmdToUtf8Text(File target, String command) {
        command = "chcp 65001|" + command + " > " + target.getAbsolutePath();
        //execute(new String[] { "cmd", "/c", arg2 });
        executeCmd(command);
    }

    /**
     * コマンドプロンプトで処理を実行します。
     * @return リターンコードが0のときにTrueを返します。
     *
     * TODO CmdAccessObjectへの移行
     *
     */
    public static boolean executeCmd(String... cmdarray) {
        List<String> list = Generics.newArrayList();
        list.add("cmd");
        list.add("/c");
        for (String p : cmdarray) {
            list.add(p);
        }
        return execute(list.toArray(new String[list.size()]));
    }

    /**
     * コマンドを実行します。
     * @param cmdarray コマンドとパラメータ
     * @return リターンコードが0のときにTrueを返します。
     */
    public static boolean execute(String... cmdarray) {
        RuntimeExc me = new RuntimeExc();
        me.exec(true, cmdarray);

        if (me.success) {
            LOGGER.debug(me.getOutText());
        } else {
            throw new RuntimeException(me.getErrText());
        }

        return me.success;
    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {
        executeCmd("date", "/t");
        execute("C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe", "http://google.co.jp?test");
    }


    /**
     * コマンドを実行します。
     * @param cmdarray コマンドとパラメータ
     */
    public void exec(String... cmdarray) {
        exec(true, cmdarray);
    }

    /**
     * コマンドを実行します。
     * @param sync 非同期の場合はfalseを指定
     * @param cmdarray コマンドとパラメータ
     */
    public void exec(boolean sync, String... cmdarray) {
        String enc = System.getProperty("os.name").toLowerCase().startsWith("windows")
            ? "MS932"
            : "UTF-8";

        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            process = runtime.exec(cmdarray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (sync == false) {
            return;
        }

        try (InputStream in = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName(enc)));) {
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    this.outs.add(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream in = process.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName(enc)));) {
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    this.errs.add(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            int result = process.waitFor();
            this.success = (result == 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getOuts() {
        return this.outs;
    }

    public List<String> getErrs() {
        return this.errs;
    }

    /**
     * 標準出力を返します。
     */
    public String getOutText() {
        StringBuilder sb = new StringBuilder();
        for (String line : this.outs) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * 標準エラーを返します。
     */
    public String getErrText() {
        StringBuilder sb = new StringBuilder();
        for (String line : this.errs) {
            sb.append(line);
        }
        return sb.toString();
    }

    public boolean isSuccess() {
        return this.success;
    }

}
