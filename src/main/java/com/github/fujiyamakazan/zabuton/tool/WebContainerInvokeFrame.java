package com.github.fujiyamakazan.zabuton.tool;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * ランチャーを表示してWebコンテナを起動します。
 * TODO port固定化のオプションを追加する
 *
 * @author fujiyama
 *
 */
public class WebContainerInvokeFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String FONT = "ＭＳ　ゴシック"; // 物理フォント
    //private static final String FONT = "Serif"; // 論理フォント

    private JButton startButton;

    private AbstractWebContainerStarter starter;

    /**
     * ランチャーを表示してWebコンテナを起動します。
     * @param title タイトル
     * @param starter  Webコンテナを起動するオブジェクト。
     */
    public static void show(String title, AbstractWebContainerStarter starter) {

        starter.start();

        WebContainerInvokeFrame invoker = new WebContainerInvokeFrame(title, starter);
        invoker.execute();
    }

    public WebContainerInvokeFrame(String title, AbstractWebContainerStarter starter) {
        super(title);
        this.starter = starter;
    }

    /**
     * ランチャーを表示します。
     */
    public void execute() {

        /* ブラウザを起動しWebアプリケーションを表示する処理 */
        Runnable action = new Runnable() {
            @Override
            public void run() {
                try {

                    /* ブラウザを開いてアプリケーションを表示する */
                    Desktop.getDesktop().browse(new URI(starter.getUrl()));

                } catch (Exception t) {
                    throw new RuntimeException(t);
                }
            }
        };

        /* フレームを初期化 */
        initializeFrame(action);

        /*
         * Webコンテナが起動するまではブラウザを起動できないようにする。
         */
        Runnable statusWorker = new Runnable() {
            @Override
            public void run() {
                if (starter.isRunning()) {
                    startButton.setEnabled(true);
                    startButton.setText("[" + getTitle() + "]へ");
                } else {
                    startButton.setEnabled(false);
                }
            }
        };
        ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();
        schedule.scheduleWithFixedDelay(statusWorker, 1, 1, TimeUnit.SECONDS);

    }

    /**
     * @param action ブラウザを起動しWebアプリケーションを表示する処理
     * @deprecated 廃止予定
     */
    public void start(Runnable action) {
        /* フレームを初期化 */
        initializeFrame(action);
    }

    /**
     * フレームを初期化します。
     *
     * @param actionOfOpenBrowser ブラウザを起動しWebアプリケーションを表示するオブジェクト。
     */
    private void initializeFrame(Runnable actionOfOpenBrowser) {

        this.setVisible(true);
        this.setSize(650, 100);
        this.setLocationRelativeTo(null); // 画面中央へ
        this.setResizable(false); // 最大化ボタン不要

        /*
         * [×]ボタン
         */
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 終了方法：フレームが閉じるとメインスレッドも終わる
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 終了方法：処理なし
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onLauncherClose(); // 終了処理を設定
            }
        });

        /* 実行ボタンを配置 */
        Panel panel = new Panel();
        this.add(panel);
        panel.setLayout(new BorderLayout());
        this.startButton = new JButton();
        panel.add(startButton, BorderLayout.CENTER); // パネルに配置
        startButton.setSize(150, 80);
        startButton.setFont(new Font(FONT, Font.PLAIN, 20));
        startButton.setText("[" + getTitle() + "]は準備中です...");
        startButton.setEnabled(false);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /* ブラウザを起動しWebアプリケーションを表示する */
                actionOfOpenBrowser.run();
            }
        });

        /* 終了ボタンを配置 */
        JButton stopButton = new JButton("終了");
        panel.add(stopButton, BorderLayout.EAST); // パネルに配置
        stopButton.setSize(20, 10);
        stopButton.setFont(new Font(FONT, Font.PLAIN, 15));
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /* 終了処理 */
                onLauncherClose();
            }
        });
    }

    /**
     * 終了処理をします。
     */
    private void onLauncherClose() {
        int ans = JOptionPane.showConfirmDialog(
            this, getTitle() + "を終了しますか?", "確認",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (ans == JOptionPane.YES_OPTION) {
            /* JVMを終了 */
            System.exit(0);
        }
    }



}
