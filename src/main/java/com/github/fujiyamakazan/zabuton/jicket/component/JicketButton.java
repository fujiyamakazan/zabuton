package com.github.fujiyamakazan.zabuton.jicket.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.apache.wicket.model.Model;

/**
 * ボタンを定義します。
 */
public class JicketButton extends JfPageComponent<String> {
    private static final long serialVersionUID = 1L;

    /**
     * コンストラクタです。
     */
    public JicketButton(final String label, final Runnable work) {
        super(Model.of(label));
        JButton button = new JButton();
        button.setText(label);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                /* コンポーネントの入力値をモデルに登録 */
                page.setObjectAll();

                /* 実装された主処理を実行 */
                work.run();

                /* アプリケーションを終了する */
                getApplication().close();

            }


        });
        super.comps.add(button);
    }

    @Override
    public void setObject() {
        /* 処理なし */
    }

}