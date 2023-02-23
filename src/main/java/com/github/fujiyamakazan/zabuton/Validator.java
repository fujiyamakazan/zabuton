package com.github.fujiyamakazan.zabuton;

import com.github.fujiyamakazan.zabuton.runnable.NgWordCheck;

public class Validator {

    public static void main(String[] args) {
        NgWordCheck.execute(Zabuton.getDir());
    }
}
