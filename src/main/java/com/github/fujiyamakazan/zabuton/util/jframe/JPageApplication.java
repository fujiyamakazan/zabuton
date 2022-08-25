package com.github.fujiyamakazan.zabuton.util.jframe;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Serializable;
import java.util.List;

import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageButton;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageTextField;

public class JPageApplication implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Object lock = new Object();

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

        synchronized (this.lock) {
            try {
                this.lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
     */
    public static String ofSimpleInputDialog(String title) {
        final Model<String> model = Model.of("");
        JPageApplication.start(new JPage() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onInitialize() {
                super.onInitialize();
                addLine(new JPageTextField("Captcha", model));
                addLine(new JPageButton("OK", new JPageAction()));
            }
        });
        String inputText = model.getObject();
        return inputText;
    }

}
