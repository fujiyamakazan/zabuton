package com.github.fujiyamakazan.zabuton.selen.zo;

import java.io.File;
import java.io.Serializable;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;

public class ZoSelen implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ZoSelen.class);

    public static void main(String[] args) {
        SelenCommonDriver cmd = new SelenCommonDriver() {

            private static final long serialVersionUID = 1L;

            @Override
            protected File getDriverDir() {
                //return null;
                return new File("C:\\tmp");
            }

            @Override
            protected File getDownloadDir() {
                return null;
            }
        };

        cmd.get("http://google.co.jp");

    }
}
