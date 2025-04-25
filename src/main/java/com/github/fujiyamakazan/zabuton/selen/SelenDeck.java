package com.github.fujiyamakazan.zabuton.selen;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.util.lang.Generics;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

import com.github.fujiyamakazan.zabuton.util.jframe.JFrameUtils;

public class SelenDeck {
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    private final Map<Integer, BrowserEntry> browserMap = new HashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    private final int columns;
    //private final List<Integer> launchOrder = new ArrayList<>();

    private final int rows;

    // 内部的に保持するブラウザインスタンスの情報
    private static class BrowserEntry {
        final SelenCommonDriver driver;
        @SuppressWarnings("unused")
        final int id;

        BrowserEntry(final SelenCommonDriver driver, final int id) {
            this.driver = driver;
            this.id = id;
        }
    }

    public SelenDeck(final int columns, final int rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public synchronized int register(final SelenCommonDriver selenCommonDriver) {
        final int id = idCounter.getAndIncrement();
        final BrowserEntry entry = new BrowserEntry(selenCommonDriver, id);
        browserMap.put(id, entry);
        //launchOrder.add(id);
        layoutGrid(columns, rows);
        return id;
    }

    public synchronized void unregister(final SelenCommonDriver cmd) {
        //browserMap.remove(id);
        browserMap.entrySet().removeIf(e->e.getValue().driver.equals(cmd));
        layoutGrid(columns, rows); // 残ったブラウザで再レイアウト
    }

    public synchronized void minimizeAll() {
        for (final BrowserEntry info : browserMap.values()) {
            info.driver.getDriver().manage().window().minimize();
        }
    }


    public void layoutGrid(final int columns, final int rows) {
        // ディスプレイサイズ取得
        final Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds(); // タスクバーを除いた表示領域

        final int totalWidth = screenBounds.width;
        final int totalHeight = screenBounds.height;

        final int cellWidth = totalWidth / columns;
        final int cellHeight = totalHeight / rows;

        int i = 0;
        for (final BrowserEntry entry : browserMap.values()) {
            final int col = i % columns;
            final int row = i / columns;

            final int x = col * cellWidth + screenBounds.x;
            final int y = row * cellHeight + screenBounds.y;

            final WebDriver driver = entry.driver.getDriver();
            driver.manage().window().setSize(new Dimension(cellWidth, cellHeight));
            driver.manage().window().setPosition(new Point(x, y));

            i++;
            if (i >= columns * rows) break; // それ以上置かない
        }
    }

    public static void main(final String[] args) {


        final SelenDeck deck = new SelenDeck(2,2);

        final List<SelenCommonDriver> cmds = Generics.newArrayList();
        for (int i = 0; i < 4; i++) {
            final int index = i;
            new Thread() {
                @Override
                public void run() {
                    try (SelenCommonDriver cmd = SelenCommonDriver.ofEdge()) {
                        cmds.add(cmd);
                        deck.register(cmd);
                        cmd.get("http://google.co.jp");
                        JFrameUtils.showDialog("メッセージ表示" + index);
                        cmd.quit();
                    }
                };
            }.start();

        }



//        // 操作例
//        deck.bringToFront(id1);
//        deck.minimizeAll();
//        deck.restoreAll();
    }


}
