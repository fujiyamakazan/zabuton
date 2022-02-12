package com.github.fujiyamakazan.zabuton.util.jframe;

import java.awt.event.WindowListener;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageButton;

/**
 * JFrameでメッセージを表示します。
 * ダイアログなので、呼出し元のスレッドを中断させます。
 * 選択肢をボタンで表示し、ユーザが選択することで終了します。
 *
 * @author fujiyama
 */
public class JChoice<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
    private Model<Boolean> cancel;
    private List<JPageButton> choices = Generics.newArrayList();
    private JPageApplication app;

    private Map<JChoiceElement<T>, Model<Boolean>> map = Generics.newHashMap();

    /**
     * インスタンスを生成します。
     * @param message メッセージ
     * @param cancel 閉じるボタンのモデル
     */
    public JChoice(String message, Model<Boolean> cancel) {
        this.message = message;
        this.cancel = cancel;
    }

    /**
     * インスタンスを生成します。
     * @param message メッセージ
     */
    public JChoice(String message) {
        this(message, Model.of(false));
    }

    /**
     * 選択肢を追加します。
     * @param label ラベル
     * @param model 選択されたことを検知するモデル
     */
    public void addChoice(String label, Model<Boolean> model) {
        map.put(new JChoiceElement<T>(label, null), model);
        choices.add(createChoice(label, model));
    }

    /**
     * 選択肢を追加します。
     * @param choice 選択肢
     */
    public void addChoice(JChoiceElement<T> choice) {
        Model<Boolean> model = Model.of(false);
        map.put(choice, model);
        choices.add(createChoice(choice.getLabel(), model));
    }

    /**
     * 選択肢を追加します。
     */
    public void addAllChoice(List<JChoiceElement<T>> choices) {
        for (JChoiceElement<T> choice : choices) {
            addChoice(choice);
        }
    }

    protected JPageButton createChoice(String label, Model<Boolean> model) {
        return new JPageButton(label, model);
    }

    /**
     * 選択されたラベルに対するモデルを返します。
     * @return
     */
    public List<JChoiceElement<T>> getSelected() {
        List<JChoiceElement<T>> results = Generics.newArrayList();
        for (Map.Entry<JChoiceElement<T>, Model<Boolean>> entry : map.entrySet()) {
            if (entry.getValue().getObject()) {
                results.add(entry.getKey());
            }
        }
        return results;
    }

    /**
     * 選択されたラベルに対するモデルを返します。
     * @return
     */
    public JChoiceElement<T> getSelectedOne() {
        for (Map.Entry<JChoiceElement<T>, Model<Boolean>> entry : map.entrySet()) {
            if (entry.getValue().getObject()) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * JFrameでメッセージと選択肢を表示します。
     * いずれかの選択肢、または閉じるボタンが押されると終了します。
     */
    public void showDialog() {
        app = new JPageApplication() {
            private static final long serialVersionUID = 1L;

            @Override
            public WindowListener getWindowListener() {
                cancel.setObject(true);
                return super.getWindowListener();
            }
        };
        JPage choicePage = createPage(message, choices);
        app.invokePage(choicePage);
    }

    protected JPage createPage(String message, List<JPageButton> choices) {
        return new JChoicePage(message, choices);
    }

    /**
     * ウィンドウを閉じます。
     */
    public void close() {
        if (app == null) {
            throw new RuntimeException("まだshowDialogが呼出されていません。");
        }
        app.close();
    }

}
