package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.jframe.JChoice;
import com.github.fujiyamakazan.zabuton.util.jframe.JChoiceElement;
import com.github.fujiyamakazan.zabuton.util.jframe.JChoicePage;
import com.github.fujiyamakazan.zabuton.util.jframe.JPage;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageButton;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageDelayLabel;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageLink;

public class GameWindow<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GameWindow.class);

    private static final Font FONT = new Font("ＭＳ ゴシック", Font.PLAIN, 10);

    private String[] messages;
    private final List<JChoiceElement<T>> choices = Generics.newArrayList();
    private JChoiceElement<T> selected;

    public void setMessage(String... messages) {
        this.messages = messages;
    }

    public void addChoice(String label, T obj) {
        choices.add(new JChoiceElement<T>(label, obj));
    }

    /**
     * UIを表示します。
     */
    public void show() {

        JPage msgWindow = new JPage() {

            private static final long serialVersionUID = 1L;

            private JFrame msgWindowFrame;

            @Override
            protected void settings() {
                backgroundColor = Color.BLACK;
                foregroundColer = Color.WHITE;
                borderWidth = 0;
                baseFont = FONT;
            }

            @Override
            protected JFrame createFrame() {
                msgWindowFrame = super.createFrame();
                return msgWindowFrame;
            }

            @Override
            protected void onInitialize() {
                super.onInitialize();
                for (String msg : messages) {
                    addLine(new JPageDelayLabel(msg));
                }
            }

            @Override
            protected void onAfterShow() {
                super.onAfterShow();

                JChoice<T> choice = new JChoice<T>("") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected JPageButton createChoice(String label, Model<Boolean> model) {
                        return new JPageLink(label, Model.of(false)) {
                            private static final long serialVersionUID = 1L;

                            @Override
                            protected String getColor() {
                                return "#ffffff";
                            }
                        };
                    }

                    @Override
                    protected JPage createPage(String message, List<JPageButton> choices) {
                        JChoicePage choicePage = new JChoicePage("", choices) {
                            private static final long serialVersionUID = 1L;

                            // TODO カーソルキーが押されたら仮選択を移動する
                            // TODO Enterキーが押されたら仮選択をクリックとみなす

                            @Override
                            protected void settings() {
                                backgroundColor = Color.BLACK;
                                foregroundColer = Color.WHITE;
                                borderWidth = 0;
                                baseFont = FONT;
                            }

                            @Override
                            protected void onAfterShow() {
                                super.onAfterShow();
                                /* メッセージウィンドウ下部に表示します。 */
                                frame.setLocation(
                                    msgWindowFrame.getLocation().x,
                                    msgWindowFrame.getLocation().y
                                        + msgWindowFrame.getSize().height
                                        + 20);

                                // TODO 初期一番上を選択し、アンダーラインを入れる。
                                // TODO カーソルで選択可能とする
                                // TODO カーソル移動時にエフェクトを入れる

                            }

                        };
                        choicePage.setHorizonal(false);
                        return choicePage;
                    }

                };
                choice.addAllChoice(choices);

                /* 表示 */
                choice.showDialog();

                /* 選択されたものを取得 */
                selected = choice.getSelectedOne();

            }
        };

        AbstractAction myActionUpOrLeft = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("UPが押されました");
            }
        };

        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

//        choicePage.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "VK_UP");
//        choicePage.getRootPane().getActionMap().put("VK_UP", myActionUpOrLeft);
//
//        msgWindow.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "VK_UP");
//        msgWindow.getRootPane().getActionMap().put("VK_UP", myActionUpOrLeft);



        msgWindow.show();
        msgWindow.dispose();


        //            // TODO マウスオーバーで仮選択とする
        //            // TODO 初期１件目を仮選択とする
        //                    // TODO 仮選択されているときにアンダーラインを付ける
        //                    // TODO 自身が選択されたら排他要素の仮選択を解除する

    }

    public T getSelected() {
        if (this.selected != null) {
            return this.selected.getObject();
        }
        return null;
    }

}
