package net.nanisl.zabuton.jframe;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class SimpleControlFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String FONT = "ＭＳ　ゴシック"; // 物理フォント
    //private static final String FONT = "Serif"; // 論理フォント

    private JButton startButton;

    public JButton getStartButton() {
		return startButton;
	}

	public SimpleControlFrame(String title) {
        super(title);
    }

    public void start(Runnable action) {
        SimpleControlFrame controlFrame = this;
      controlFrame.setVisible(true);
      controlFrame.setSize(650, 100);
      controlFrame.setLocationRelativeTo(null); // 画面中央へ
      controlFrame.setResizable(false); // 最大化ボタン不要

      /*
       * [×]ボタンの処理
       */
      //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // フレームが閉じるとメインスレッドも終わる
      controlFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      controlFrame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
              /* 終了処理 */
              onLauncherClose(controlFrame);
          }
      });

      /* パネルを配置 */
      Panel panel = new Panel();
      controlFrame.add(panel);
      panel.setLayout(new BorderLayout());

      /* 実行ボタンを配置 */
      this.startButton = new JButton();
      panel.add(startButton, BorderLayout.CENTER); // パネルに配置
      startButton.setSize(150, 80);
      startButton.setFont(new Font(FONT, Font.PLAIN, 20));
      startButton.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              action.run();
          }
      });

      /* 終了ボタンを配置 */
      JButton jButtonStop = new JButton("終了");
      panel.add(jButtonStop, BorderLayout.EAST); // パネルに配置
      jButtonStop.setSize(20, 10);
      jButtonStop.setFont(new Font(FONT, Font.PLAIN, 15));
      jButtonStop.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              /* 終了処理 */
              onLauncherClose(controlFrame);
          }
      });
    }

    /** ランチャー終了処理 */
    private void onLauncherClose(JFrame frame) {
        int ans = JOptionPane.showConfirmDialog(
                frame, "本当に終了しますか?", "確認", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (ans == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }


    public void showMessag(String message) {
        JOptionPane.showMessageDialog(
                this, "準備中...", "メッセージ", JOptionPane.INFORMATION_MESSAGE);
    };



}
