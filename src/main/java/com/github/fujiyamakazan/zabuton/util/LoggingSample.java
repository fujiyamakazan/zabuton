package com.github.fujiyamakazan.zabuton.util;

public class LoggingSample {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        LOGGER.debug("test");
        LOGGER.info("test");
        LOGGER.error("test");
    }
}
