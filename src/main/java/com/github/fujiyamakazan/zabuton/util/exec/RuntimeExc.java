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

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(RuntimeExc.class);

    /* 標準出力 */
    private List<String> outs = new ArrayList<String>();
    /* 標準エラー */
    private List<String> errs = new ArrayList<String>();

    /* リターンコード = 0 */
    private boolean success = false;

    /**
     * コマンドを実行します。
     * @param params コマンド
     * @return リターンコードが0のときにTrueを返します。
     */
    public static boolean execute(String... params) {
        RuntimeExc me = new RuntimeExc();
        me.exec(params);

        if (me.isSuccess() == false) {
            throw new RuntimeException(me.getErrText());
        }

        return me.success;
    }

    /**
     * コマンドプロンプトを実行します。
     * @param command コマンド
     * @return リターンコードが0のときにTrueを返します。
     */
    public static boolean executeCmd(String... params) {

        List<String> list = Generics.newArrayList();
        list.add("cmd");
        list.add("/c");
        for (String p: params) {
            list.add(p);
        }

        //return execute(new String[] {"cmd", "/c", command});
        return execute(list.toArray(new String[list.size()]));
    }

    public static boolean executeCmdToUtf8Text(String command, File out) {
        String arg2 = "chcp 65001|" + command + " > " + out.getAbsolutePath();
        return execute(new String[] {"cmd", "/c", arg2});
    }

    /**
     * コマンドを実行します。
     * @param params コマンド
     */
    public void exec(String... params) {
        String enc = System.getProperty("os.name").toLowerCase().startsWith("windows") ? "MS932" : "UTF-8";

        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            process = runtime.exec(params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream in = process.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName(enc)));
                ) {
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
