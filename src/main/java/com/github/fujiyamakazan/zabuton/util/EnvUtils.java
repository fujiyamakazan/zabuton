package com.github.fujiyamakazan.zabuton.util;

import java.io.File;

public class EnvUtils {
    public static File getUserDocuments() {
        return new File(System.getenv("USERPROFILE") + "\\Documents");
    }
}
