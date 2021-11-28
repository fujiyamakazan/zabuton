package com.github.fujiyamakazan.zabuton.util.jframe;

import org.apache.wicket.model.Model;

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

    private Model<String> message = Model.of("");

    @Override
    protected void onInitialize() {
        super.onInitialize();
        addLine(new JPageLabel(message));
    }


    public void show(String message) {
        this.message.setObject(message);
        super.show();
    }


}