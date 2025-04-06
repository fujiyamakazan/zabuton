package com.github.fujiyamakazan.zabuton.selen.scraper;

import java.lang.invoke.MethodHandles;

public abstract class JcomScraper extends Scraper {
    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(MethodHandles.lookup().lookupClass());


//
//    /**
//     * 処理をします。
//     */
//    public static void main(String[] args) {
//        new JcomScraper() {
//            private static final long serialVersionUID = 1L;
//            @Override
//            protected File getAppDir() {
//                return EnvUtils.getUserDesktop("test");
//            }
//        }.execute();
//    }
}
