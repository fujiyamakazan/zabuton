package com.github.fujiyamakazan.zabuton.app.rakutenquest;

import java.awt.Color;
import java.awt.Font;

import com.github.fujiyamakazan.zabuton.util.ThreadSleep;
import com.github.fujiyamakazan.zabuton.util.jframe.JPage;


public class RPage extends JPage {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RPage.class);

    public static final Font FONT = new Font("ＭＳ ゴシック", Font.PLAIN, 15);

    @Override
    protected final void settings() {
        this.backgroundColor = Color.BLACK;
        this.foregroundColer = Color.WHITE;
        this.borderWidth = 0;
        this.baseFont = FONT;
    }

    /**
     * 一定時間の後、非表示にします。
     */
    public void toast(int i) {
        show();
        ThreadSleep.sleep(i * 1000);
        dispose();
    }

}
