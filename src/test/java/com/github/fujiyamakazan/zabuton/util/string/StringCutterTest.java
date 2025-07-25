package com.github.fujiyamakazan.zabuton.util.string;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringCutterTest {
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());


    @Test
    public void testBetween() {
        // StringUtilsを使用するため割愛
    }

    @Test
    public void testReplaceBetween() {
        assertEquals("-LOVE-", StringCutter.replaceBetween("-clover-", "c", "r", "LOVE"));
        assertEquals("-clover-", StringCutter.replaceBetween("-clover-", "r", "c", "LOVE"));
        assertEquals("-clover-", StringCutter.replaceBetween("-clover-", "z", "z", "LOVE"));
    }

    @Test
    public void testLeft() {
        // StringUtilsを使用するため割愛
    }

    @Test
    public void testLeftOfLast() {
        // StringUtilsを使用するため割愛
    }

    @Test
    public void testRight() {
        // StringUtilsを使用するため割愛
    }

    @Test
    public void testRightOfFirst() {
        // StringUtilsを使用するため割愛
    }
}
