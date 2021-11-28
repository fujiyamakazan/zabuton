package com.github.fujiyamakazan.zabuton.util.jframe;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

public class JPageApplication {

    private final Object lock = new Object();

    private final List<JPage> pages = Generics.newArrayList();

    /**
     * ページを起動します。
     * この時点で呼び出し元のスレッドを待機状態にします。
     */
    public void invokePage(JPage page) {

        bind(page);
        page.show();

        synchronized (lock) {
            try {
                lock.wait();
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

        for (JPage p : pages) {
            p.dispose();
        }

        synchronized (lock) {
            lock.notifyAll();
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
        if (pages.contains(page) == false) {
            pages.add(page);
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

}
