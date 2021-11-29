package com.github.fujiyamakazan.zabuton.util.jframe;

import java.awt.Color;
import java.awt.FlowLayout;
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

import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageButton;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageComponent;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageLabel;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageTextField;

/**
 * JFrameのコンポーネントで構成するページです。
 * @author fujiyama
 */
public class JPage implements Serializable {
    private static final long serialVersionUID = 1L;

    private JPageApplication app;
    private final JFrame frame;
    private final JPanel panelMain;
    private boolean initialized;
    private boolean initializedSuper;

    private List<JPageComponent<?>> components = Generics.newArrayList();

    /* 閉じるボタンの処理 */
    private WindowAdapter windowListener;

    public static void main(String[] args) {
        JPage page1 = new JPage();
        page1.addLine(new JPageLabel("テスト1"));
        page1.addLine(new JPageButton("ボタン1", Model.of(false)));
        page1.addLine(new JPageTextField("項目1", Model.of("")));
        page1.addLine(new JPageTextField("項目2", Model.of("")));
        page1.addLine(new JPageTextField("項目3", Model.of("")));
        page1.addLine(new JPageTextField("項目4", Model.of("")));
        page1.addLine(new JPageButton("ボタンA", Model.of(false)), new JPageButton("ボタンB", Model.of(false)));
        page1.show();

        //        new JPageApplication().invokePage(new JPage() {
        //            private static final long serialVersionUID = 1L;
        //            @Override
        //            protected void onInitialize() {
        //                super.onInitialize();
        //                addLine(new JPageLabel("テスト2"));
        //                addLine(new JPageLabel("テスト3"));
        //            }
        //        });

    }

    /**
     * コンストラクタです。
     */
    public JPage() {
        frame = new JFrame();
        frame.setLocation(20, 20);
        frame.setSize(600, 300); // 高さは仮の値
        frame.setResizable(false); // 最大化ボタン不要
        frame.setAlwaysOnTop(true); // 最前面
        frame.setLocationRelativeTo(null); // 画面中央へ
        frame.setResizable(true); // リサイズ許可
        frame.setForeground(Color.RED);

        JScrollPane scrollpane = new JScrollPane();
        frame.add(scrollpane);
        scrollpane.setBackground(Color.GREEN);

        panelMain = new JPanel();
        scrollpane.setViewportView(panelMain);
        panelMain.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 余白
        //panelMain.setBorder(BorderFactory.createLineBorder(Color.red));
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));// 縦方向に子パネルを追加する。

    }

    /**
     * 初期化プロセスを実行します。
     * 子クラスで拡張する場合は、必ずこの親クラスのメソッドも呼び出さなければいけません。
     */
    protected void onInitialize() {
        /* 処理なし */
        initializedSuper = true;

        for (JPageComponent<?> pc : components) {
            pc.onInitialize();
        }

        /* 閉じるボタンの処理を設定 */
        if (app != null) {
            frame.addWindowListener(app.getWindowListener());
        } else {
            /* デフォルトでは閉じるときに自身を破棄する。 */
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    frame.dispose();
                }
            });
        }
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

        lineSize++;

        JPanel linePanel = new JPanel();
        panelMain.add(linePanel);
        linePanel.setLayout(new BoxLayout(linePanel, BoxLayout.X_AXIS)); // 横方向にコンポーネントを追加する

        for (JPageComponent<?> componet : componets) {

            /* ページとコンポーネントを紐づけます。 */
            components.add(componet);
            componet.setPage(this);

            /* コンポーネント一つ一つを個別のパネルに配置して、横一列のパネルに乗せます。*/
            JPanel p = new JPanel();
            linePanel.add(p);
            p.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1)); // 余白
            //p.setBorder(BorderFactory.createLineBorder(Color.red)); // デバッグ用に赤い線を表示
            p.setBackground(Color.WHITE);

            /*
             * コンポーネントに含まれる複数のJFrameコンポーネントを配置します。
             */
            final List<JComponent> comps = componet.getJComponents();
            //            if (comps.size() == 1) {
            //                /* 要素が1つだけのときは BorderLayout でパネルいっぱいに表示します。 */
            //                p.setLayout(new BorderLayout());
            //            } else {
            if (componet instanceof JPageButton) {
                p.setLayout(new FlowLayout(FlowLayout.CENTER));
            } else {
                /* 左寄せで配置します。*/
                p.setLayout(new FlowLayout(FlowLayout.LEFT));
            }

            //            }
            for (JComponent jc : comps) {
                p.add(jc);
            }
        }
    }

    /**
     * 表示します。
     * まだ初期化が行われていなければ、行います。
     */
    public final void show() {

        if (initialized == false) {

            /* 初期化 */
            onInitialize();

            if (initializedSuper == false) {
                throw new RuntimeException("onInitializeを実装する場合は、親クラスのメソッドも呼び出してください。");
            }

            initialized = true;
        }

        /* 画面表示直前処理 */
        for (JPageComponent<?> pc : components) {
            pc.onBeforeShow();
        }

        /* 行数による高さ調整 */
        int width = this.frame.getSize().width;
        int height = 40; // 40 = フレームのヘッダーのおおよその高さ
        height += (lineSize * 50); // 1行当たり50とする。
        this.frame.setSize(width, height);

        /* 画面を表示する */
        this.frame.setVisible(true);
    }

    /**
     * ボタンが押されたときの処理です。
     */
    public void submit(JPageButton button) {

        /* コンポーネントの入力値でモデルを更新します。 */
        for (JPageComponent<?> pc : components) {
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

    public final void hide() {
        this.frame.setVisible(false);
    }

    public final void dispose() {
        this.frame.dispose();
    }

}
