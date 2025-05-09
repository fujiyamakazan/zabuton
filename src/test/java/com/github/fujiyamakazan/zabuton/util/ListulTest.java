package com.github.fujiyamakazan.zabuton.util;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ListulTest {
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    @Test
    public void test_parseProperties() {
        final String text = "key1= value1\n#key2=value2\nkey3 =value3";
        final List<Map.Entry<String, String>> pairs = Listul.parseProperties(text);
        pairs.forEach(pair -> System.out.println(pair.getKey() + " = " + pair.getValue()));
    }
}
