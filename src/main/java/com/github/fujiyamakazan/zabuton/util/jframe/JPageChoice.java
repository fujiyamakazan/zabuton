package com.github.fujiyamakazan.zabuton.util.jframe;

import java.awt.event.WindowListener;
import java.io.Serializable;
import java.util.List;

import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageButton;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageLabel;

/**
 * JFrameでメッセージを表示します。
 * ダイアログなので、呼出し元のスレッドを中断させます。
 * 選択肢をボタンで表示し、ユーザが選択することで終了します。
 *
 * @author fujiyama
 */
public class JPageChoice implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
    private Model<Boolean> cancel;
    private List<JPageButton> choices = Generics.newArrayList();
    private JPageApplication app;

    /**
     * インスタンスを生成します。
     * @param message メッセージ
     * @param cancel 閉じるボタンのモデル
     */
    public JPageChoice(String message, Model<Boolean> cancel) {
        this.message = message;
        this.cancel = cancel;
    }

    public void addChoice(String label, Model<Boolean> model) {
        choices.add(new JPageButton(label, model));
    }

    /**
     * JFrameでメッセージと選択肢を表示します。
     * いずれかの選択肢、または閉じるボタンが押されると終了します。
     */
    public void showDialog() {
        app = new JPageApplication() {
            @Override
            public WindowListener getWindowListener() {
                cancel.setObject(true);
                return super.getWindowListener();
            }
        };
        app.invokePage(new JPage() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onInitialize() {
                super.onInitialize();
                addLine(new JPageLabel(message));
                addLine(choices);
            }
        });
    }

    public void close() {
        if (app == null) {
            throw new RuntimeException("まだshowDialogが呼出されていません。");
        }
        app.close();
    }
}
