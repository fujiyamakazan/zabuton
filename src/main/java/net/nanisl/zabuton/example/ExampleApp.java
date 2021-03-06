package net.nanisl.zabuton.example;

import org.apache.wicket.Page;

import net.nanisl.zabuton.Zabuton;
import net.nanisl.zabuton.app.ZabuApp;

/**
 * zabutonの実装サンプル
 *
 * Javaアプリケーションとして（mainメソッドを）実行すると
 * spring-bootでWebアプリケーションが起動します。
 *
 * @author fujiyama
 */
public class ExampleApp extends ZabuApp {

	public static void main(String[] args) {
		Zabuton.start(ExampleApp.class);
	}

	/**
	 * アプリケーションの表示名
	 */
	@Override
	public String getTitle() {
		return "さぶとんサンプル";
	}

	/**
	 * 初めに表示するページ
	 */
	public Class<? extends Page> getHomePage() {
		return ExamplePage.class;
	}

}
