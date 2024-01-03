package com.github.fujiyamakazan.zabuton.util.jframe;

import java.awt.Component;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JFrame操作のユーティリティです。
 *
 * @author fujiyama
 */
public class JFrameUtils {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(JFrameUtils.class);

    /**
     * 実装サンプルです。
     */
    public static void main(String[] args) {

        //        log.debug("ダイアログでメッセージを表示します。");
        //        showDialog("テストメッセージ９０１２３４５６７８９０");
        //
        //        log.debug("ダイアログでメッセージを表示します。エラーメッセージのアイコンを表示します。");
        //        showErrorDialog("テストメッセージ９０１２３４５６７８９０");
        //
        //        log.debug("ダイアログで確認ダイアログを表示します。");
        //        for (int i = 0; i < 3; i++) {
        //            boolean result = showConfirmDialog("テストメッセージ９０１２３４５６７８９０");
        //            System.out.println(result);
        //        }
        //        log.debug("SimpleMessageのサンプル");
        //        SimpleMessage simpleMessage = new SimpleMessage();
        //        for (int i = 0; i < 5; i++) {
        //            simpleMessage.show(i + " テストメッセージ９０１２３４５６７８９０");
        //            ThreadSleep.threadSleep(1000);
        //        }
        //        simpleMessage.dispose();

        //        for (int i = 0; i < 4; i++) {
        //            System.out.println("ChoiceMessageDialogのサンプル");
        //            Model<Boolean> cancel = Model.of(false);
        //            Model<Boolean> choice1 = Model.of(false);
        //            Model<Boolean> choice2 = Model.of(false);
        //            JPageChoice<String> pageChoice = new JPageChoice<String>(
        //                    "〇〇を選択してください。", cancel);
        //            pageChoice.addChoice("キャンセル", cancel);
        //            pageChoice.addChoice("選択１", choice1);
        //            pageChoice.addChoice("選択２", choice2);
        //            pageChoice.showDialog();
        //            System.out.println("cancel:" + cancel.getObject());
        //            System.out.println("choice1:" + choice1.getObject());
        //            System.out.println("choice2:" + choice2.getObject());
        //            System.out.println("selected:" + pageChoice.getSelectedOne().getLabel());
        //
        //        }

        System.out.println("End");

    }

    /**
     * JFrameでメッセージをダイアログ表示します。
     * ダイアログなので、呼出し元のスレッドを中断します。
     * @param message メッセージ
     */
    public static void showDialog(String message) {
        //showDialogCore(null, message, JOptionPane.INFORMATION_MESSAGE);
        showDialogCore(null, new JFrameDialogParams().message(message));
    }

    /**
     * JFrameでメッセージをダイアログ表示します。
     * ダイアログなので、呼出し元のスレッドを中断します。
     * @param message メッセージ
     */
    public static void showDialog(JFrameDialogParams params) {
        //showDialogCore(null, message, JOptionPane.INFORMATION_MESSAGE);
        showDialogCore(null, params);

    }

    /**
     * JFrameのダイアログでメッセージを表示します。
     * ダイアログなので、呼出し元のスレッドを中断します。
     * @param  parent 親コンポーネント
     * @param message メッセージ
    */
    public static void showDialog(Component parent, String message) {
        //showDialogCore(parent, message, JOptionPane.INFORMATION_MESSAGE);
        showDialogCore(parent, new JFrameDialogParams().message(message));
    }

    /**
     * JFrameでエラーメッセージをダイアログ表示します。
     * ダイアログなので、呼出し元のスレッドを中断します。
     * @param message メッセージ
     */
    public static void showErrorDialog(String message) {
        //showDialogCore(null, message, JOptionPane.ERROR_MESSAGE);
        showDialogCore(null, new JFrameDialogParams().message(message).iconType(JOptionPane.ERROR_MESSAGE));
    }

    /**
     * JFrameで確認メッセージをダイアログ表示します。
     * ダイアログなので、呼出し元のスレッドを中断します。
     * @param message メッセージ
     * @return OKを選択したらTrue
     */
    public static boolean showConfirmDialog(String message) {
        return showConfirmDialogCore(null, message, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * JFrameで確認メッセージをダイアログ表示します。
     * ダイアログなので、呼出し元のスレッドを中断します。
     * @param  parent 親コンポーネント
     * @param message メッセージ
     * @return OKを選択したらTrue
     */
    public static boolean showConfirmDialog(Component parent, String message) {
        return showConfirmDialogCore(parent, message, JOptionPane.INFORMATION_MESSAGE);
    }

    public static class JFrameDialogParams implements Serializable {
        private static final long serialVersionUID = 1L;
        private String message;
        private String title = "";
        private int iconType = JOptionPane.INFORMATION_MESSAGE;

        public JFrameDialogParams message(String message) {
            this.message = message;
            return this;
        }

        public JFrameDialogParams title(String title) {
            this.title = title;
            return this;
        }

        public JFrameDialogParams iconType(int iconType) {
            this.iconType = iconType;
            return this;
        }
    }

    private static void showDialogCore(Component parent, JFrameDialogParams params) {
        JFrame tmpForm = null;
        if (parent == null) {
            tmpForm = new JFrame();
            tmpForm.setAlwaysOnTop(true); // 最前面
            parent = tmpForm;
        }

        JOptionPane.showMessageDialog(
            parent,
            params.message,
            params.title,
            params.iconType);

        if (tmpForm != null) {
            tmpForm.dispose();
        }
    }

//    private static void showDialogCore(Component parent, String message, int iconType) {
//        JFrame tmpForm = null;
//        if (parent == null) {
//            tmpForm = new JFrame();
//            tmpForm.setAlwaysOnTop(true); // 最前面
//            parent = tmpForm;
//        }
//
//        JOptionPane.showMessageDialog(
//            parent,
//            message,
//            "",
//            iconType);
//
//        if (tmpForm != null) {
//            tmpForm.dispose();
//        }
//    }

    private static boolean showConfirmDialogCore(Component parent, String message, int iconType) {
        JFrame tmpForm = null;
        if (parent == null) {
            tmpForm = new JFrame();
            tmpForm.setAlwaysOnTop(true); // 最前面
            parent = tmpForm;
        }

        int ans = JOptionPane.showConfirmDialog(
            parent,
            message,
            "",
            JOptionPane.OK_CANCEL_OPTION, // [OK][取消] //JOptionPane.OK_OPTION, // [はい][いいえ]
            iconType);

        boolean result = ans == JOptionPane.YES_OPTION;

        if (tmpForm != null) {
            tmpForm.dispose();
        }
        return result;
    }

}
