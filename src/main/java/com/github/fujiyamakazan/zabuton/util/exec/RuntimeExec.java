package com.github.fujiyamakazan.zabuton.util.exec;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fujiyamakazan.zabuton.util.ListToStringer;
import com.github.fujiyamakazan.zabuton.util.StringBuilderLn;

/**
 * Runtime#exec のユーティリティです。
 * @author fujiyama
 */
public class RuntimeExec implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeExec.class);

    public static class RuntimeExecResult implements Serializable {
        private static final long serialVersionUID = 1L;
        private boolean success;
        private String out;
        private String err;

        public boolean isSuccess() {
            return success;
        }

        public String getOut() {
            return out;
        }

        public String getErr() {
            return err;
        }

        @Override
        public String toString() {
            String o = "";
            if (StringUtils.isNotBlank(out)) {
                o = "\n" + out;
            }
            String e = "";
            if (StringUtils.isNotBlank(err)) {
                e = "\n[標準エラー]" + err;
            }
            return (success ? "[success]" : "[error]") + o + e;
        }
    }

    /**
     * cmd.exeに関わる処理は、公開メソッドを別のクラスへ集約しました。
     * @deprecated {@link CmdAccessObject#executeCmdToUtf8Text(File, String)}
     */
    public static void executeCmdToUtf8Text(File target, String command) {
        CmdAccessObject.executeCmdToUtf8Text(target, command);
    }

    /**
     * cmd.exeに関わる処理は、公開メソッドを別のクラスへ集約しました。
     * @deprecated  {@link CmdAccessObject#executeCmd(String...)}
     */
    public static RuntimeExecResult executeCmd(String... cmdarray) {
        return CmdAccessObject.executeCmd(cmdarray);
    }

    /**
     * コマンドを実行します。
     * @param cmdarray コマンドとパラメータ
     * @deprecated {@link #exec(String...)} へ集約
     */
    public static RuntimeExecResult execute(String... cmdarray) {
        return exec(cmdarray);
        //if (me.success) {
        //    LOGGER.debug(me.getOutText());
        //} else {
        //    throw new RuntimeException(me.getErrText());
        //}
        //return me.success;
    }

    /**
     * コマンドを実行します。
     * @param cmdarray コマンドとパラメータ
     */
    public static RuntimeExecResult exec(String... cmdarray) {
        return exec(true, cmdarray);
    }

    /**
     * Runtime#excを実行します。
     * @param sync 非同期の場合はfalseを指定
     * @param cmdarray コマンドとパラメータ
     */
    public static RuntimeExecResult exec(boolean sync, String... cmdarray) {

        LOGGER.debug(ListToStringer.convert(cmdarray));

        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            process = runtime.exec(cmdarray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (sync == false) {
            return null;
        }

        final String enc = System.getProperty("os.name").toLowerCase().startsWith("windows")
            ? "MS932"
            : "UTF-8";

        RuntimeExecResult result = new RuntimeExecResult();
        StringBuilderLn sb = new StringBuilderLn();
        try (InputStream in = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName(enc)));) {
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    //this.outs.add(line);
                    sb.appendLn(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        result.out = sb.toString();

        StringBuilderLn sbError = new StringBuilderLn();
        try (InputStream in = process.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName(enc)));) {
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    //this.errs.add(line);
                    sbError.appendLn(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        result.err = sbError.toString();

        try {
            //int result = process.waitFor();
            //this.success = (result == 0);
            result.success = (process.waitFor() == 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {
        execute("C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe", "http://google.co.jp?test");
    }

}
