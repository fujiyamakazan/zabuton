package com.github.fujiyamakazan.zabuton.util.string;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

import junit.framework.TestCase;

public class StringInspectionTest extends TestCase {

    //    @Test
    //    public void testContains() {
    //        assertEquals(false, StringInspection.contains("abc", new char[] { 'z', 'd' }));
    //        assertEquals(true, StringInspection.contains("abc", new char[] { 'z', 'a' }));
    //    }

    @Test
    public void testOnly() {
        assertEquals(true, StringInspection.isOnly("ABCBA", new char[] { 'A', 'B', 'C' }));
        assertEquals(false, StringInspection.isOnly("AB9BA", new char[] { 'A', 'B', 'C' }));
    }


    @Test
    public void testIsSinglebyte() {
        assertEquals(true, StringInspection.isSinglebyte("a", StandardCharsets.UTF_8));
        assertEquals(false, StringInspection.isSinglebyte("あ", StandardCharsets.UTF_8));
        assertEquals(false, StringInspection.isSinglebyte("", StandardCharsets.UTF_8));
    }

    @Test
    public void testIssMultibyte() {
        assertEquals(true, StringInspection.isMultibyte("ｱ", StandardCharsets.UTF_8));
        assertEquals(false, StringInspection.isMultibyte("a", StandardCharsets.UTF_8));
        assertEquals(false, StringInspection.isMultibyte("", StandardCharsets.UTF_8));
    }










}
