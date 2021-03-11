package net.nanisl.zabuton.boot;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.nanisl.zabuton.app.ZabuApp;
import net.nanisl.zabuton.app.ZabuInfoPage;
import net.nanisl.zabuton.jframe.SimpleControlFrame;

/**
 * アプリケーションをサーブレットコンテナで起動する
 *
 * TODO port固定化のオプションを追加する
 *
 * @author k.fujiyama
 */
public abstract class AbstractZabuBoot {

	private static final Logger log = LoggerFactory.getLogger(AbstractZabuBoot.class);

	private static final int DEFAULT_PORT = 8080;

	private int port = DEFAULT_PORT;

	private Status status;

    private String subParams;

	private enum Status {
		Try, Running, Error_Exist, Error;
	}

	/**
	 * WicketApplicationをjettyで起動する
	 * @param appClass アプリケーションクラス
	 * @param appTitle アプリケーション名
	 * @param subParams
	 */
	public void invoke(Class<? extends ZabuApp> appClass, String appTitle, String subParams) {

	    this.subParams = subParams;

		/*
		 * コントロールパネルの構築
		 */
		SimpleControlFrame controlPanel = new SimpleControlFrame(appTitle);

		/* 実行ボタンの処理 */
		Runnable action = new Runnable() {
			@Override
			public void run() {
                try {

                	/* ブラウザを開いてアプリケーションを表示する */
                    Desktop.getDesktop().browse(new URI(getUrl()));

                } catch (Exception t) {
                    throw new RuntimeException(t);
                }
			}
		};
		/*
		 * コントロールパネルを表示
		 */
		controlPanel.start(action);

		/* 実行ボタンの表示制御 */
        Runnable statusWorker = new Runnable(){
            @Override
            public void run() {
            	JButton startButton = controlPanel.getStartButton();
                if (status == Status.Running) {
                	startButton.setEnabled(true);
					startButton.setText("[" + appTitle + "]へ");
                } else {
                	startButton.setEnabled(false);
                	startButton.setText("[" + appTitle + "]は準備中です...");
                }
            }
        };
        ScheduledExecutorService schedule= Executors.newSingleThreadScheduledExecutor();
        schedule.scheduleWithFixedDelay(statusWorker, 1, 1, TimeUnit.SECONDS);



		/* URL（port）を決定する */
		int retryCount = 0;
		status = Status.Try;
		while (status == Status.Try) {

			/* 現在のportで起動を試みる */
			startServer(appClass.getName(), appTitle);

			/* リトライ制限回数を超えていれば終了する */
			if (retryCount++ > 10) {

				/* 起動不可 */
				status = Status.Error;
				log.error("利用できるポートが見つかりませんでした。");
				break;
			}

			/* 起動していなければインクリメントする */
			if (status == Status.Try) {
				port = port + 1;
			}
		}
		if (status == Status.Running) {
			log.info("起動成功");

		} else {

			JFrame frame = new JFrame(appTitle);
			final String message;
			if (status == Status.Error_Exist) {
				message = appTitle + " は起動中です。";
			} else {
				message = "起動できませんでした。";
			}
			JOptionPane.showMessageDialog(
					frame, message, "メッセージ", JOptionPane.YES_OPTION);

			/* アプリ終了 */
			System.exit(0);
		}
	}

	/**
	 * サーバー起動処理
	 */
	private void startServer(String appClassName, String appTitle) {

		/*
		 * ポートの使用状況を確認する
		 */
		HttpURLConnection con = null;
		BufferedReader reader = null;
		try {
			String url = getUrl();
			con = (HttpURLConnection) new URL(url + "/" + ZabuApp.URL_INFOPATH).openConnection();
			con.setRequestMethod("GET");
			con.connect();
			InputStream is = con.getInputStream();

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			Element root = doc.getDocumentElement();
			Element el = (Element) root.getElementsByTagName(ZabuInfoPage.CLASS).item(0);
			String clz = "";
			if (el != null) {
				if (el.getFirstChild() != null) {
					clz = el.getFirstChild().getNodeValue();
				}
			}

			if (StringUtils.equals(clz, appClassName)) {

				/* 起動不可 */
				status = Status.Error_Exist;
				log.info("port=" + port + "は既に[" + clz + "]が起動しています。");
				return;

			} else {

				/* スキャン継続 */
				log.info("port=" + port + "は他のZabuApp[" + clz + "]が起動しています。＞スキャン続行");
				status = Status.Try;
				return;

			}

		} catch (Exception e) {

			log.info("port=" + port + "に他のZabuAppは起動していません。");

		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			if (con != null) {
				con.disconnect();
			}
		}

		/*
		 * サーバーを起動する
		 */
		try {

			/*
			 * サーバー起動
			 */
			startServletContainer(port, appClassName, appTitle);

			/* 起動状態へ移行 */
			status = Status.Running;

		} catch (PortAlreadyException e) {

			status = Status.Try;
			log.info("既にポートが使用されています。＞スキャン続行");

		} catch (Exception e) {
			/* 起動不可 */
			status = Status.Error;
			log.error("起動失敗", e);
		}
	}

	protected abstract void startServletContainer(int port, String appClassName, String appTitle) throws Exception;

	private String getUrl() {

	    if (StringUtils.isNotEmpty(subParams)) {
	        return "http://localhost:" + port + "/" + subParams;
	    }

		return "http://localhost:" + port;
	}

	/**
	 * 起動しようとしたサーバーのportが使用済みの時にthrowする例外
	 */
	protected class PortAlreadyException extends Exception {

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;

	}

}
