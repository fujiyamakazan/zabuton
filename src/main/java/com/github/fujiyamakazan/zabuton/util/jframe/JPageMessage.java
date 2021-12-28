package com.github.fujiyamakazan.zabuton.util.jframe;

import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageLabel;

/**
 * JFrameのダイアログでメッセージを表示します。
 * 呼出し元のスレッドは中断しません。
 * showメソッドでメッセージを変更することができます。
 * disposeメソッドでメッセージを消します。
 *
 * @author fujiyama
 */
public class JPageMessage extends JPage {
    private static final long serialVersionUID = 1L;

    //private Model<String> message = Model.of("");
    private String message = "";

    /**
     * メッセージウィンドウを表示します。
     */
    public void show(String message) {
        //this.message.setObject(message);
        this.message = message;
        super.show();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        addLine(new JPageLabel(message));
    }





}