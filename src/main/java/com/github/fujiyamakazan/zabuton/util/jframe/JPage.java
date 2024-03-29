package com.github.fujiyamakazan.zabuton.util.jframe;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageButton;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageComponent;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageDelayLabel;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageInputComponent;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageLabel;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageLink;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageTextField;

/**
 * JFrameのコンポーネントで構成するページです。
 * @author fujiyama
 */
public class JPage implements Serializable {
    private static final long serialVersionUID = 1L;

    private JPageApplication app;
    protected JFrame frame;
    protected JScrollPane scrollpane;
    protected JPanel panelMain;
    private boolean initialized;
    private boolean initializedSuper;

    protected List<JPageComponent<?>> components = Generics.newArrayList();

    protected Color backgroundColor = null;
    protected Color foregroundColer = null;
    protected int borderWidth;
    protected Font baseFont = null;

    /**
     * コンストラクタです。
     */
    public JPage() {

    }

    /**
     * 初期化プロセスを実行します。
     * 子クラスで拡張する場合は、必ずこの親クラスのメソッドも呼び出さなければいけません。
     */
    protected void onInitialize() {
        this.initializedSuper = true;

        settings();

        this.frame = createFrame();
        this.frame.setLocation(20, 20);
        this.frame.setSize(650, 300); // 高さは仮の値
        this.frame.setResizable(false); // 最大化ボタン不要
        this.frame.setAlwaysOnTop(true); // 最前面
        this.frame.setLocationRelativeTo(null); // 画面中央へ
        this.frame.setResizable(true); // リサイズ許可

        //frame.setForeground(Color.RED);

        this.scrollpane = new JScrollPane();
        this.frame.add(this.scrollpane);
        //scrollpane.setBackground(Color.GREEN);

        this.panelMain = new JPanel();
        this.scrollpane.setViewportView(this.panelMain);
        /* 余白 */
        this.panelMain.setBorder(BorderFactory.createEmptyBorder(this.borderWidth,
            this.borderWidth, this.borderWidth, this.borderWidth));
        //panelMain.setBorder(BorderFactory.createLineBorder(Color.red));
        this.panelMain.setLayout(new BoxLayout(this.panelMain, BoxLayout.Y_AXIS));// 縦方向に子パネルを追加する。

        for (JPageComponent<?> pc : this.components) {
            pc.onInitialize();
        }

        /* 閉じるボタンの処理を設定 */
        if (this.app != null) {
            this.frame.addWindowListener(this.app.getWindowListener());
        } else {
            /* デフォルトでは閉じるときに自身を破棄する。 */
            this.frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    JPage.this.frame.dispose();
                }
            });
        }
    }

    protected JFrame createFrame() {
        return new JFrame();
    }

    protected void settings() {
        //this.backgroundColor = Color.WHITE;
        this.foregroundColer = Color.BLACK;
        this.borderWidth = 5;
    }

    public final void setApplication(JPageApplication app) {
        this.app = app;
    }

    public JPageApplication getApplication() {
        return this.app;
    }

    /**
     * コンポーネントを追加します。追加したコンポーネントは横一列となります。
     * @param componets コンポーネント
     */
    protected final void addLine(JPageComponent<?>... componets) {
        addLine(Arrays.asList(componets));
    }

    private int lineSize = 0;

    /**
     * コンポーネントを追加します。追加したコンポーネントは横一列となります。
     * @param componets コンポーネント
     */
    protected final void addLine(List<? extends JPageComponent<?>> componets) {

        this.lineSize++;

        JPanel linePanel = new JPanel();
        this.panelMain.add(linePanel);
        linePanel.setLayout(new BoxLayout(linePanel, BoxLayout.X_AXIS)); // 横方向にコンポーネントを追加する

        for (JPageComponent<?> componet : componets) {

            /* ページとコンポーネントを紐づけます。 */
            this.components.add(componet);
            componet.setPage(this);

            /* コンポーネント一つ一つを個別のパネルに配置して、横一列のパネルに乗せます。*/
            JPanel p = new JPanel();
            linePanel.add(p);
            p.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1)); // 余白
            //p.setBorder(BorderFactory.createLineBorder(Color.red)); // デバッグ用に赤い線を表示

            if (this.backgroundColor == null) {
                p.setBackground(Color.WHITE);
            } else {
                p.setBackground(this.backgroundColor);
            }

            /*
             * コンポーネントに含まれる複数のJFrameコンポーネントを配置します。
             */
            final List<JComponent> comps = componet.getJComponents();
            if (componet instanceof JPageButton) {
                if (componet instanceof JPageLink) {
                    /* リンク形式のボタンは左寄せで配置します。*/
                    p.setLayout(new FlowLayout(FlowLayout.LEFT));
                } else {
                    /* ボタンはセンタリングで配置します。 */
                    p.setLayout(new FlowLayout(FlowLayout.CENTER));
                }

            } else {
                /* 左寄せで配置します。*/
                p.setLayout(new FlowLayout(FlowLayout.LEFT));
            }

            for (JComponent jc : comps) {
                p.add(jc);
            }

            /* 配色など */
            for (JComponent jc : comps) {

                if (this.backgroundColor != null) {
                    jc.setBackground(this.backgroundColor);
                    if (this.backgroundColor.equals(Color.BLACK)) {
                        if (jc instanceof JTextField) {
                            ((JTextField) jc).setCaretColor(Color.WHITE);
                        }
                    }
                }
                jc.setForeground(this.foregroundColer);

                if (this.baseFont != null) {
                    jc.setFont(this.baseFont);
                }
            }
        }
    }

    /**
     * 表示します。
     * まだ初期化が行われていなければ、行います。
     */
    public final void show() {

        if (this.initialized == false) {

            /* 初期化 */
            onInitialize();

            if (this.initializedSuper == false) {
                throw new RuntimeException("onInitializeを実装する場合は、親クラスのメソッドも呼び出してください。");
            }

            this.initialized = true;
        }

        /* 画面表示直前処理 */
        for (JPageComponent<?> pc : this.components) {
            pc.onBeforeShow();
        }

        /* 行数による高さ調整 */
        int width = this.frame.getSize().width;
        int height = 40; // 40 = フレームのヘッダーのおおよその高さ
        height += (this.lineSize * 50); // 1行当たり50とする。
        this.frame.setSize(width, height);

        /* 画面を表示する */
        this.frame.setVisible(true);

        /* 表示後の拡張処理 */
        onAfterShow();
    }

    /** 表示後の拡張処理です。 */
    protected void onAfterShow() {
        for (JPageComponent<?> pc : this.components) {

            /* メッセージの遅延表示 */
            if (pc instanceof JPageDelayLabel) {
                ((JPageDelayLabel) pc).active();

            }
        }
    }

    /**
     * ボタンが押されたときの処理です。
     */
    public void submit(JPageButton button) {

        /* コンポーネントの入力値でモデルを更新します。 */
        for (JPageComponent<?> pc : this.components) {
            if (pc instanceof JPageButton) {
                if (pc.equals(button)) {
                    ((JPageButton) pc).getModel().setObject(true);
                } else {
                    ((JPageButton) pc).getModel().setObject(false);
                }
            } else {
                pc.updateModel();
            }
        }
    }

    /**
     * モデルを登録します。
     */
    public void setTextFromModel() {
        for (JPageComponent<?> pc : this.components) {
            if (pc instanceof JPageInputComponent) {
                ((JPageInputComponent) pc).setTextFromModel();
            }
        }
    }

    public final void hide() {
        this.frame.setVisible(false);
    }

    public final void dispose() {
        this.frame.dispose();
    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {
        JPage page1 = new JPage();
        page1.addLine(new JPageLabel("テスト・・・・・・・・・・・・・・・・・・・・・・・・・・・・・・1"));
        page1.addLine(new JPageButton("ボタン1", Model.of(false)));
        page1.addLine(new JPageTextField("項目1", Model.of("")));
        page1.addLine(new JPageTextField("項目2", Model.of("")));
        page1.addLine(new JPageTextField("項目3", Model.of("")));
        page1.addLine(new JPageTextField("項目4", Model.of("")));
        page1.addLine(new JPageButton("ボタンA", Model.of(false)), new JPageButton("ボタンB", Model.of(false)));
        page1.show();
    }

    public JFrame getFrame() {
        return this.frame;
    }

    public JComponent getRootPane() {
        return this.scrollpane;
    }
}
