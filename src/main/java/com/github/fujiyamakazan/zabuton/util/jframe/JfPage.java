package com.github.fujiyamakazan.zabuton.util.jframe;

import java.awt.Font;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.jframe.component.JfPageComponent;

public class JfPage {

    boolean initialized;
    private JfApplication app;
    private final JFrame frame;
    private final JPanel panelMain;

    private List<JfPageComponent<?>> components = Generics.newArrayList();

    /**
     * コンストラクタです。
     */
    public JfPage() {
        frame = new JFrame();
        frame.setLocation(20, 20);
        frame.setSize(600, 300);
        frame.setAlwaysOnTop(true); // 最前面

        panelMain = new JPanel();
        frame.add(panelMain);
        BoxLayout layout = new BoxLayout(panelMain, BoxLayout.Y_AXIS);
        panelMain.setLayout(layout);

    }

    public void setApplication(JfApplication app) {
        this.app = app;
        frame.addWindowListener(app.getWindowListener()); //  閉じるボタンの振舞い
    }

    public JfApplication getApplication() {
        return this.app;
    }

    protected void onInitialize() {
        /* 処理なし */
    }

    protected void add(JfPageComponent<?>... componets) {
        JPanel pLine = new JPanel();
        panelMain.add(pLine);
        pLine.setLayout(new BoxLayout(pLine, BoxLayout.X_AXIS));
        for (JfPageComponent<?> c : componets) {
            components.add(c);
            c.setPage(this);
            c.apendTo(pLine);
        }
    }

    public void show() {

        if (initialized == false) {
            onInitialize();

            for (JfPageComponent<?> pc : components) {
                for (JComponent jc : pc.getJComplenets()) {
                    jc.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 20));
                }
            }
            initialized = true;
        }


        this.frame.setVisible(true);
    }

    public void hide() {
        this.frame.setVisible(false);
    }

    public void dispose() {
        this.frame.dispose();
    }

    /**
     * 画面項目の入力値をモデルに登録します。
     */
    public void setObjectAll() {
        for (JfPageComponent<?> c : components) {
            c.setObject();
        }
    }

}
