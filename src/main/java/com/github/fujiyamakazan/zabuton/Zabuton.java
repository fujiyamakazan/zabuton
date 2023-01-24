package com.github.fujiyamakazan.zabuton;

import java.io.File;

import com.github.fujiyamakazan.zabuton.util.EnvUtils;

public class Zabuton {

    public static File getDir() {
        return new File(EnvUtils.getUserAppData(), "zabuton");
    }
}
