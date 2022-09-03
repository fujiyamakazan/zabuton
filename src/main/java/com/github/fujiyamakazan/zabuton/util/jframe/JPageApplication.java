package com.github.fujiyamakazan.zabuton.util.jframe;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Serializable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageButton;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageTextField;

public class JPageApplication implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Object lock = new Object();
    private boolean closed = false;

    private final List<JPage> pages = Generics.newArrayList();

    public static void start(JPage page) {
        new JPageApplication().invokePage(page);
    }

    /**
     * ページを起動します。
     * この時点で呼び出し元のスレッドを待機状態にします。
     */
    public void invokePage(JPage page) {

        bind(page);
        page.show();

        if (this.closed == false) {
            synchronized (this.lock) {
                try {
                    this.lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    /**
     * 全てのページを破棄します。
     * 呼出し元のスレッドを再開させます。
     */
    public void close() {

        for (JPage p : this.pages) {
            p.dispose();
        }

        synchronized (this.lock) {
            this.lock.notifyAll();
        }

        this.closed = true;
    }

    /**
     * ページ遷移します。
     */
    public void changePage(JPage from, JPage to) {
        from.hide();

        bind(to);
        to.show();
    }

    /**
     * アプリケーションとページを紐づけます。
     */
    public void bind(JPage page) {
        if (this.pages.contains(page) == false) {
            this.pages.add(page);
            page.setApplication(this);
        }
    }

    /**
     * 閉じるボタンの振る舞いです。
     */
    public WindowListener getWindowListener() {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        };
    }

    /**
     * シンプルな入力ダイアログを表示します。
     * @param title 項目名
     * @param timeSec タイムアウト[秒]
     */
    public static String ofSimpleInputDialog(String title, int timeSec) {
        final Model<String> model = Model.of("");

        JPageApplication.start(new JPage() {
            private static final long serialVersionUID = 1L;

            private JPageButton okButton = null;

            @Override
            protected void onInitialize() {
                super.onInitialize();
                addLine(new JPageTextField(title, model));
                addLine(this.okButton = new JPageButton("OK", new JPageAction()));
            }

            @Override
            protected void onAfterShow() {
                super.onAfterShow();

                // タイムアウト
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getOkButton().doClick();
                    }
                }, timeSec * 1000);

            }

            public JPageButton getOkButton() {
                return this.okButton;
            }


        });

        String inputText = model.getObject();
        return inputText;
    }

    public static void main(String[] args) {
        String str = JPageApplication.ofSimpleInputDialog("test", 3);
        System.out.println(str);
    }

}
