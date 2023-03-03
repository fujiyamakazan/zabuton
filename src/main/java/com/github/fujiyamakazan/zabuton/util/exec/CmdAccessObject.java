//package com.github.fujiyamakazan.zabuton.util.exec;
//
//import java.io.Serializable;
//
///**
// * Windowsのcmd.exeにアクセスします。
// * @author fujiyama
// */
//public class CmdAccessObject implements Serializable {
//    private static final long serialVersionUID = 1L;
//    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CmdAccessObject.class);
//
//    private void execute(String command, String[] params) {
//        // TODO 自動生成されたメソッド・スタブ
//
//    }
//
//    /**
//     * 動作確認をします。
//     */
//    public static void main(String[] args) {
//
//        CmdAccessObject cmd = new CmdAccessObject();
//        //cmd.exec("cmd", "/c", "date", "/t");
//        cmd.execute("date", "/t");
//
//        LOGGER.debug("---");
//        LOGGER.debug(cmd.getOut());
//        LOGGER.debug("---");
//    }
//
//
//
//
//
//}
