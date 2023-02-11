package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;

public class ThreadSleep implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * スレッドを指定したミリ秒で停止します。
     * @param millis ミリ秒
     */
    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
