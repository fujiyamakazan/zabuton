package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RetryWorker implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(RetryWorker.class);

    /**
     * 処理を実行します。
     */
    public void start() {
        int count = 0;
        while (true) {
            if (count > 0) {
                //log.debug("RetryWorker:" + count);
            }
            try {
                run();
                break;
            } catch (Exception e) {
                if (count++ > 10) {
                    throw new RuntimeException(e);
                }
                recovery();
            }
        }
    }

    protected abstract void run();

    protected abstract void recovery();

}