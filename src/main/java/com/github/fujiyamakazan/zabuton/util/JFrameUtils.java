package com.github.fujiyamakazan.zabuton.util;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JFrameUtils {
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(JFrameUtils.class);

    /**
     * JFrameでメッセージを表示します。
     * @param message メッセージ
     */
    public static void showMessageDialog(String message) {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true); // 最前面
        JOptionPane.showMessageDialog(
            frame, message, "メッセージ", JOptionPane.YES_OPTION);
    }

}
