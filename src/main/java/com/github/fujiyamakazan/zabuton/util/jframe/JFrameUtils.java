package com.github.fujiyamakazan.zabuton.util.jframe;

import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fujiyamakazan.zabuton.util.jframe.component.JicketLabel;

public class JFrameUtils {
    private static final Logger log = LoggerFactory.getLogger(JFrameUtils.class);

    /**
     * JFrameでメッセージをダイアログ表示します。
     * @param message メッセージ
     */
    public static void showDialog(String message) {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true); // 最前面
        JOptionPane.showMessageDialog(
                frame, message, "", JOptionPane.INFORMATION_MESSAGE);
        frame.dispose();
    }

    /**
     * JFrameでエラーメッセージをダイアログ表示します。
     * @param message メッセージ
     */
    public static void showErrorDialog(String message) {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true); // 最前面
        JOptionPane.showMessageDialog(
                frame, message, "", JOptionPane.ERROR_MESSAGE);
        frame.dispose();
    }

    /**
     * JFrameで確認メッセージをダイアログ表示します。
     * @param message メッセージ
     * @return OKを選択したらTrue
     */
    public static boolean showConfirmDialog(String message) {
        return showConfirmDialog(null, message);
    }

    /**
     * JFrameで確認メッセージをダイアログ表示します。
     * @param message メッセージ
     * @return OKを選択したらTrue
     */
    public static boolean showConfirmDialog(Component parent, String message) {

        JFrame tmpFrame = null;
        if (parent == null) {
            tmpFrame = new JFrame();
            parent = tmpFrame;
        }

        int ans = JOptionPane.showConfirmDialog(
                parent,
                message,
                "",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
        boolean result = ans == JOptionPane.YES_OPTION;

        if (tmpFrame != null) {
            tmpFrame.dispose();
        }

        return result;
    }

    public static void main(String[] args) {

        log.debug("showDialog");
        showDialog("テストメッセージ９０１２３４５６７８９０");

        log.debug("showErrorDialog");
        showErrorDialog("テストメッセージ９０１２３４５６７８９０");

        log.debug("showConfirmDialog");
        System.out.println(showConfirmDialog("テストメッセージ９０１２３４５６７８９０"));

        log.debug("JfPage");
        new JfApplication().invokePage(new JfPage() {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                add(new JicketLabel("テストメッセージ９０１２３４５６７８９０"));
            }
        });

    }

}
