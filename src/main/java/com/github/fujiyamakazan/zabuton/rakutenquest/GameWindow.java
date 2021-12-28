package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.List;

import javax.swing.JFrame;

import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.jframe.JPage;
import com.github.fujiyamakazan.zabuton.util.jframe.JPageChoice;
import com.github.fujiyamakazan.zabuton.util.jframe.JPageChoice.ChoiceElement;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageButton;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageDelayLabel;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageLink;

public class GameWindow<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GameWindow.class);

    private static final Font FONT = new Font("ＭＳ ゴシック", Font.PLAIN, 10);

    private String[] messages;
    private final List<ChoiceElement<T>> choices = Generics.newArrayList();
    private ChoiceElement<T> selected;

    public void setMessage(String... messages) {
        this.messages = messages;
    }

    public void addChoice(String label, T obj) {
        choices.add(new ChoiceElement<T>(label, obj));
    }

    /**
     * UIを表示します。
     */
    public void show() {

        // TODO スレッドの依存関係を整理する

        JPage msgWindow = new JPage() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void settings() {
                backgroundColor = Color.BLACK;
                foregroundColer = Color.WHITE;
                borderWidth = 0;
                baseFont = FONT;
            }

            @Override
            protected JFrame createFrame() {
                JFrame jframe = super.createFrame();
                jframe.addKeyListener(new KeyListener() {

                    @Override
                    public void keyTyped(KeyEvent e) {
                        // TODO 自動生成されたメソッド・スタブ

                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                        // TODO 自動生成されたメソッド・スタブ

                    }

                    @Override
                    public void keyPressed(KeyEvent e) {
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_UP:
                                System.out.println("up");
                                break;
                            case KeyEvent.VK_DOWN:
                                System.out.println("down");
                                break;
                            case KeyEvent.VK_LEFT:
                                System.out.println("left");
                                break;
                            case KeyEvent.VK_RIGHT:
                                System.out.println("right");
                                break;
                            case KeyEvent.VK_ENTER:
                                System.out.println("enter");
                                break;
                            default:
                                /* 処理なし*/
                                break;
                        }
                    }
                });
                return jframe;
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

                JFrame parentFrame = frame;

                JPageChoice<T> jpChoice = new JPageChoice<T>("") {
                    private static final long serialVersionUID = 1L;

                    // TODO マウスオーバーで仮選択とする
                    // TODO 初期１件目を仮選択とする

                    @Override
                    protected JPageButton createChoice(String label, Model<Boolean> model) {
                        return new JPageLink(label, model) {

                            // TODO 仮選択されているときにアンダーラインを付ける
                            // TODO 自身が選択されたら排他要素の仮選択を解除する

                            private static final long serialVersionUID = 1L;

                            @Override
                            protected String getColor() {
                                return "#ffffff";
                            }

                        };
                    }

                    @Override
                    protected JPage createPage(String message, List<JPageButton> choices) {

                        ChoicePage choicePage = new ChoicePage(message, choices) {
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
                                        parentFrame.getLocation().x,
                                        parentFrame.getLocation().y
                                                + parentFrame.getSize().height
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

                for (ChoiceElement<T> choice : choices) {
                    jpChoice.addChoice(choice);
                }

                /* 表示 */
                jpChoice.showDialog();

                /* 選択されたものを取得 */
                selected = jpChoice.getSelectedOne();

            }

        };

        msgWindow.show();
        msgWindow.dispose();

    }

    public T getSelected() {
        return this.selected.getObject();
    }

}
