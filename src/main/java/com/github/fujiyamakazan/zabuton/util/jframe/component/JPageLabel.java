package com.github.fujiyamakazan.zabuton.util.jframe.component;

import javax.swing.JLabel;

import org.apache.wicket.model.Model;

/**
 * ラベルのコンポーネントモデルです。
 */
public class JPageLabel extends JPageComponent<String> {
    private static final long serialVersionUID = 1L;
    private JLabel label;
    //private JTextArea label;

    /**
     * コンストラクタです。
     */
    public JPageLabel(String text) {
        this(Model.of(text));
    }

    /**
     * コンストラクタです。
     */
    public JPageLabel(Model<String> model) {
        super(model);

        label = new JLabel();
        label.setVerticalAlignment(JLabel.TOP); // 上寄せ
      //label = new JTextArea();
        //label.setLineWrap(true);


        addJFrameComponent(label);
    }

    @Override
    public void onBeforeShow() {
        super.onBeforeShow();
        /*
         * 最新のモデルの情報でラベルの内容を更新します。
         */
        label.setText(edit(model.getObject()));
    }

    private static String edit(String text) {
        //StringBuilder sb = new StringBuilder("<html> " + text + "</html>");
        StringBuilder sb = new StringBuilder(text);
        text = sb.toString();
        return text;
    }

    @Override
    public void updateModel() {
        /* 処理なし */
    }

}