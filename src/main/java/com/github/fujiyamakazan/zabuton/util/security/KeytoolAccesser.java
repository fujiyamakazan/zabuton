package com.github.fujiyamakazan.zabuton.util.security;

import java.io.File;

import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.exec.RuntimeExc;

public class KeytoolAccesser {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(KeytoolAccesser.class);

    public static void main(String[] args) {
        execute();
    }

    public static String execute() {
        LOGGER.debug(System.getProperty("java.home"));

        File keytool = new File(EnvUtils.getJavahome(), "bin/keytool.exe");
        File cacerts = new File(EnvUtils.getJavahome(), "lib/security/cacerts");
        String pw = "changeit";

        RuntimeExc exe = new RuntimeExc();
        exe.exec(true, keytool.getAbsolutePath(), "-keystore", cacerts.getAbsolutePath(), "-list", "-storepass", pw);
        String result = exe.getOutText();
        //LOGGER.debug(result);
        return result;
    }


}
