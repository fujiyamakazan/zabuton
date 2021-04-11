package com.github.fujiyamakazan.zabuton.util.exec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runtime#exc のユーティリティ
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

    public void exec(String... params) {
        String enc = System.getProperty("os.name").toLowerCase().startsWith("windows") ? "MS932" : "UTF-8";

        //		for (int i = 0; i < params.length; i++) {
        //			String str = params[i];
        //			if (str.contains(" ")) {
        //				str = "\"" + str + "\"";
        //				params[i] = str;
        //			}
        //		}

        Runtime runtime = Runtime.getRuntime();
        Process process;
        try {
            process = runtime.exec(params);
        } catch (IOException e2) {
            throw new RuntimeException(e2);
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

        } catch (IOException e1) {
            throw new RuntimeException(e1);
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
        } catch (IOException e1) {
            throw new RuntimeException(e1);
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

    public String getOutText() {
        StringBuilder sb = new StringBuilder();
        for (String line : this.outs) {
            sb.append(line);
        }
        return sb.toString();
    }

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
