package net.nanisl.zabuton;

import net.nanisl.zabuton.app.MyApp;
import net.nanisl.zabuton.boot.ZabuBootByTomcat;

/**
 * zbuton自身をサーブレットコンテナで起動する
 * @author fujiyama
 */
public class Main {
    public static void main(String[] args) {
    	new ZabuBootByTomcat().invoke(MyApp.class, "zabuton");
    }
}
