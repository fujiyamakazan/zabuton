package net.nanisl.zabuton.container;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.protocol.http.ContextParamWebApplicationFactory;
import org.apache.wicket.protocol.http.WicketFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.nanisl.zabuton.Zabuton;
import net.nanisl.zabuton.app.ZabuApp;
import net.nanisl.zabuton.app.ZabuInfoPage;
import net.nanisl.zabuton.jframe.SimpleControlFrame;

/**
 * アプリケーションコンテナ
 * @author k.fujiyama
 */
public class ZabuContainer {

	private static final Logger log = LoggerFactory.getLogger(ZabuContainer.class);

	private static final int DEFAULT_PORT = 8178;

	private int port = DEFAULT_PORT;

	private Status status;

	private enum Status {
		Try, Running, Error_Exist, Error;
	}

	/**
	 * WicketApplicationをjettyで起動する
	 * @param appClass アプリケーションクラス
	 * @param appTitle アプリケーション名
	 */
	public static void invoke(Class<? extends ZabuApp> appClass, String appTitle) {

		ZabuContainer container = new ZabuContainer();

		/* ランチャーを表示する */
		//container.showLauncher(appTitle);
		SimpleControlFrame controlPanel = new SimpleControlFrame(appTitle);
		Runnable action = new Runnable() {

			@Override
			public void run() {
                if (container.status != Status.Running) {
                	controlPanel.showMessag("準備中...");

                } else {
                    try {
                        Desktop.getDesktop().browse(new URI(container.getUrl()));
                    } catch (Exception t) {
                        throw new RuntimeException(t);
                    }
                }
			}
		};
		controlPanel.start(action);

		/* URL（port）を決定する */
		int retryCount = 0;
		container.status = Status.Try;
		while (container.status == Status.Try) {

			/* 現在のportで起動を試みる */
			container.startServer(appClass.getName(), appTitle);

			/* リトライ制限回数を超えていれば終了する */
			if (retryCount++ > 10) {

				/* 起動不可 */
				container.status = Status.Error;
				log.error("利用できるポートが見つかりませんでした。");
				break;
			}

			/* 起動していなければインクリメントする */
			if (container.status == Status.Try) {
				container.port = container.port + 1;
			}
		}
		if (container.status == Status.Running) {
			log.info("起動成功");

		} else {

			JFrame frame = new JFrame(appTitle);
			final String message;
			if (container.status == Status.Error_Exist) {
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
				log.info("既に[" + clz + "]が起動しています。");
				return;

			} else {

				/* スキャン継続 */
				log.info("他のZabuApp[" + clz + "]が起動しています。＞スキャン続行");
				status = Status.Try;
				return;

			}

		} catch (Exception e) {

			log.info("他のZabuAppは起動していません。");

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
			Server server = new Server(port);

			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");

			// FIXME Jettyを11にすると失敗する

			ServletHandler handler = new ServletHandler();

			FilterHolder holder = handler.addFilterWithMapping(WicketFilter.class, "/*",
					EnumSet.of(DispatcherType.REQUEST));
			holder.setInitParameter(WicketFilter.FILTER_MAPPING_PARAM, "/*");
			holder.setInitParameter(ContextParamWebApplicationFactory.APP_CLASS_PARAM, appClassName);
			holder.setInitParameter(Zabuton.PARAM_TITLE, appTitle);

			context.addFilter(holder, "/*", EnumSet.of(DispatcherType.REQUEST));

			context.setHandler(handler);
			server.setHandler(context);

			server.start();
			//server.join();

			/* 起動状態へ移行 */
			status = Status.Running;

		} catch (IOException e) {
			if (StringUtils.contains(e.getMessage(), "Failed to bind to")) {

				/* スキャン継続 */
				log.info("既にポートが使用されています。＞スキャン続行");
				status = Status.Try;
				return;

			} else {
				/* 起動不可 */
				status = Status.Error;
				log.error("起動失敗", e);
				return;
			}
		} catch (Exception e) {
			/* 起動不可 */
			status = Status.Error;
			log.error("起動失敗", e);
		}
	}

	private String getUrl() {
		return "http://localhost:" + port;
	}

}
