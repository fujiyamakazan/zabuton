package com.github.fujiyamakazan.zabuton.util.string;

import java.util.List;

import org.junit.Test;

import junit.framework.TestCase;

public class RegexUtilsTest extends TestCase {

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RegexUtilsTest.class);

    @Test
    public void testFind() {
        assertEquals(true, RegexUtils.find("〒 220-0000神奈川県～", "〒\\s?[0-9]{3}-?[0-9]{4}"));
        assertEquals(false, RegexUtils.find("090-0000-0000", "〒\\s?[0-9]{3}-?[0-9]{4}"));
        assertEquals(false, RegexUtils.find("", "〒\\s?[0-9]{3}-?[0-9]{4}"));
    }

    @Test
    public void testPickup() {
        List<String> pickup1 = RegexUtils.pickup("〒 110-0000東京都～ 〒 220-0000神奈川県～", "〒\\s?([0-9]{3})-?([0-9]{4})");
        assertEquals(2, pickup1.size());

        List<String> pickup2 = RegexUtils.pickup("090-0000-0000", "〒\\s?([0-9]{3})-?([0-9]{4})");
        assertEquals(0, pickup2.size());

        List<String> pickup3 = RegexUtils.pickup("", "〒\\s?([0-9]{3})-?([0-9]{4})");
        assertEquals(0, pickup3.size());
    }

    @Test
    public void testPickupOne() {
        String pickup1 = RegexUtils.pickupOne("〒 110-0000東京都～ 〒 220-0000神奈川県～", "〒\\s?([0-9]{3})-?([0-9]{4})");
        assertEquals("110", pickup1);

        String pickup2 = RegexUtils.pickupOne("090-0000-0000", "〒\\s?([0-9]{3})-?([0-9]{4})");
        assertEquals(null, pickup2);

        String pickup3 = RegexUtils.pickupOne("", "〒\\s?([0-9]{3})-?([0-9]{4})");
        assertEquals(null, pickup3);
    }
}
