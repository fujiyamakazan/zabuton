package com.github.fujiyamakazan.zabuton.util.string;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * Stringulをテストします。
 * @author fujiyama
 */
public class StringulTest extends TestCase {
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StringulTest.class);

    @Test
    public void testToFullKatakana() {
        assertEquals("ア", Stringul.toFullKatakana("ｱ"));
        assertEquals("パ", Stringul.toFullKatakana("ﾊﾟ"));
    }

    @Test
    public void testPad4() {
        assertEquals("0123", Stringul.pad4(123));
    }

    @Test
    public void testEscpeHtml() {
        assertEquals("&lt;test&gt;?a=b&amp;c=dあ", Stringul.escpeHtml("<test>?a=b&c=dあ"));
    }


    @Test
    public void testRmDecoration4Figure() {
        assertEquals("3.1415", Stringul.rmDecoration4Figure("3.1415"));
        assertEquals("-1000", Stringul.rmDecoration4Figure("-1,000"));
    }

}
