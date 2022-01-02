package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.awt.Color;
import java.awt.Font;

import com.github.fujiyamakazan.zabuton.util.ThreadUtils;
import com.github.fujiyamakazan.zabuton.util.jframe.JPage;


public class RPage extends JPage {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RPage.class);

    public static final Font FONT = new Font("ＭＳ ゴシック", Font.PLAIN, 15);

    @Override
    protected final void settings() {
        backgroundColor = Color.BLACK;
        foregroundColer = Color.WHITE;
        borderWidth = 0;
        baseFont = FONT;
    }

    public void toast(int i) {
        show();
        ThreadUtils.sleep(i * 1000);
        dispose();
    }

}
