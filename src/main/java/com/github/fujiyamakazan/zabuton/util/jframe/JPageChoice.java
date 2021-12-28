package com.github.fujiyamakazan.zabuton.util.jframe;

import java.awt.event.WindowListener;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
public class JPageChoice<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
    private Model<Boolean> cancel;
    private List<JPageButton> choices = Generics.newArrayList();
    private JPageApplication app;

    private Map<ChoiceElement<T>, Model<Boolean>> map = Generics.newHashMap();

    public static class ChoiceElement<T extends Serializable> implements Serializable {

        private static final long serialVersionUID = 1L;
        private final String label;
        private final T obj;

        public ChoiceElement(String label, T obj) {
            this.label = label;
            this.obj = obj;
        }

        public String getLabel() {
            return label;
        }

        public T getObject() {
            return obj;
        }
    }

    /**
     * インスタンスを生成します。
     * @param message メッセージ
     * @param cancel 閉じるボタンのモデル
     */
    public JPageChoice(String message, Model<Boolean> cancel) {
        this.message = message;
        this.cancel = cancel;
    }

    /**
     * インスタンスを生成します。
     * @param message メッセージ
     */
    public JPageChoice(String message) {
        this(message, Model.of(false));
    }

    /**
     * 選択肢を追加します。
     * @param label ラベル
     * @param model 選択されたことを検知するモデル
     */
    public void addChoice(String label, Model<Boolean> model) {
        map.put(new ChoiceElement<T>(label, null), model);
        choices.add(createChoice(label, model));
    }

    /**
     * 選択肢を追加します。
     * @param choice 選択肢
     */
    public void addChoice(ChoiceElement<T> choice) {
        Model<Boolean> model = Model.of(false);
        map.put(choice, model);
        choices.add(createChoice(choice.getLabel(), model));
    }

    protected JPageButton createChoice(String label, Model<Boolean> model) {
        return new JPageButton(label, model);
    }

    /**
     * 選択されたラベルに対するモデルを返します。
     * @return
     */
    public List<ChoiceElement<T>> getSelected() {
        List<ChoiceElement<T>> results = Generics.newArrayList();
        for (Map.Entry<ChoiceElement<T>, Model<Boolean>> entry : map.entrySet()) {
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
    public ChoiceElement<T> getSelectedOne() {
        for (Map.Entry<ChoiceElement<T>, Model<Boolean>> entry : map.entrySet()) {
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
        return new ChoicePage(message, choices);
    }

    protected static class ChoicePage extends JPage {
        private static final long serialVersionUID = 1L;
        private final List<JPageButton> choices;
        private final String message;
        private boolean horizontal = true;

        protected ChoicePage(String message, List<JPageButton> choices) {
            this.choices = choices;
            this.message = message;
        }

        @Override
        protected void onInitialize() {
            super.onInitialize();
            addLine(new JPageLabel(message));
            if (horizontal) {
                addLine(choices);
            } else {
                for (JPageButton button: choices) {
                    addLine(button);
                }
            }

        }

        public void setHorizonal(boolean horizontal) {
            this.horizontal = horizontal;
        }

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
