package com.github.fujiyamakazan.zabuton;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * ログ出力テスト。
     * @param args ignore
     */
    public static void main(String[] args) {
        LOGGER.trace("log.trace");
        LOGGER.debug("log.debug");
        LOGGER.info("log.info");
        LOGGER.warn("log.warn");
        LOGGER.error("log.error");

        System.out.println("------------------------------------------------------------");
        System.err.println("------------------------------------------------------------");

        Logger logLib = LoggerFactory.getLogger("com.example.xxx");
        logLib.trace("logLib.trace");
        logLib.debug("ogLib.debug");
        logLib.info("logLib.info");
        logLib.warn("logLib.warn");
        logLib.error("logLib.error");
    }
}
