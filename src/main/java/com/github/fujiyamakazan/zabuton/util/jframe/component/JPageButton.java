package com.github.fujiyamakazan.zabuton.util.jframe.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.apache.wicket.model.Model;

import com.github.fujiyamakazan.zabuton.util.jframe.JPageAction;
import com.github.fujiyamakazan.zabuton.util.jframe.JPageChangeAction;

/**
 * ボタンのコンポーネントモデルです。
 */
public class JPageButton extends JPageComponent<Boolean> {
    private static final long serialVersionUID = 1L;

    private JButton button;

    /**
     * コンストラクタです。
     * @param label ボタンに表示するラベルです。
     * @param action ボタンを押したときの処理です。
     */
    public JPageButton(final String label, final JPageAction action) {
        this(Model.of(false), label, action);
    }

    /**
     * コンストラクタです。
     * ボタンがクリックされたら引数のモデルにTrue設定します。
     *
     * @param label ボタンに表示するラベルです。
     * @param model データを保持するモデル。
     */
    public JPageButton(String label, Model<Boolean> model) {
        this(model, label, new JPageAction());
    }

    private JPageButton(final Model<Boolean> model, final String label, final JPageAction action) {
        super(model);
        this.button = new JButton();
        this.button.setText(label);
        this.button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                /* このボタンが押されたことを親オブジェクトに通知します。 */
                getPage().submit(JPageButton.this);

                /* 実装された主処理を実行 */
                action.run();



                /* アプリケーションを終了します。 */
                if (action.isFinal()) {
                    if (action instanceof JPageChangeAction == false) {
                        getPage().getApplication().close();
                    }
                }

            }

        });

        addDecoration(this.button);

        addJFrameComponent(this.button);
    }

    protected void addDecoration(JButton button) {
        /* 拡張ポイント */
    }

    @Override
    public void updateModel() {
        /* 処理なし */
    }

    public void doClick() {
        this.button.doClick();
    }

    @Override
    public String toString() {
        return super.toString() + " " + this.button.getText();
    }



}