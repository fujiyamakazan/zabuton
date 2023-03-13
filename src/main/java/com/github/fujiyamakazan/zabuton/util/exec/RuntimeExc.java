package com.github.fujiyamakazan.zabuton.util.exec;

import java.util.Arrays;
import java.util.List;

import com.github.fujiyamakazan.zabuton.util.exec.RuntimeExec.RuntimeExecResult;

/**
 * Runtime#exec のユーティリティです。
 * @deprecated 名前の修正、およびstatic化をした {@link RuntimeExec} へ集約してください。
 */
@Deprecated
public class RuntimeExc {

    private RuntimeExecResult result;

    public void exec(String... cmdarray) {
        this.result = RuntimeExec.exec(cmdarray);
    }

    public List<String> getOuts() {
        //return this.outs;
        return Arrays.asList(this.result.getOut().split("\n"));
    }

    //    public List<String> getErrs() {
    //        return this.errs;
    //    }

    /**
    * 標準出力を返します。
    */
    public String getOutText() {
        return this.result.getOut();
        //StringBuilder sb = new StringBuilder();
        //for (String line : this.outs) {
        //    sb.append(line);
        //}
        //return sb.toString();
    }

    /**
    * 標準エラーを返します。
    */
    public String getErrText() {
        return this.result.getErr();
        //StringBuilder sb = new StringBuilder();
        //for (String line : this.errs) {
        //    sb.append(line);
        //}
        //return sb.toString();
    }

    public boolean isSuccess() {
        return this.result.isSuccess();
    }
}
