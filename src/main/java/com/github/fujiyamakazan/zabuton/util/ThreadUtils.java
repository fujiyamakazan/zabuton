package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;

public class ThreadUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ThreadUtils.class);

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
