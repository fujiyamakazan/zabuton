package com.github.fujiyamakazan.zabuton.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Listul {
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    /*
     * 【Sream API 実装例】
     * ■ リストから集計する実装
     * int totalCount = list.stream().mapToInt(Object::getCount).sum();
     *
     *
     * ■ その他
     * SerializableRunnable hr = () -> System.out.println("---");

        record User(String name, int age) implements Serializable {
        }
        ;

        List<User> users = new ArrayList<User>();
        users.add(new User("テスト0", 0));
        users.add(new User("テスト1", 1));

        users.forEach(System.out::println);
        hr.run();

        List<Integer> ages = users.stream().map(User::age).collect(Collectors.toList());
        ages.forEach(System.out::println);
        hr.run();

        System.out.println(ages.stream().mapToDouble(Double::valueOf).average().getAsDouble());

        System.out.println(users.stream().map(User::toString).collect(Collectors.joining("/")));
     *
     */

    public static void main(final String[] args) throws Exception {

        final Integer[] values = {1, 2, 3, 4, 5};


        final Stream<Integer> of = Stream.of(values);
        final IntSummaryStatistics stats = of
            .collect(Collectors.summarizingInt(Integer::intValue));
        System.out.println(stats);
    }

    /**
     * プロパティ形式のテキストをパースします。
     * (例)
     * key1=value1\n#key2=value2\nkey3=value3
     * → [key1 = value1],[key3 = value3]
     */
    public static List<Map.Entry<String, String>> parseProperties(final String text)  {
        final Properties properties = new Properties();
        try {
            properties.load(new StringReader(text));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        final List<Map.Entry<String, String>> pairs = properties.entrySet().stream()
        .map(entry -> Map.entry((String) entry.getKey(), (String) entry.getValue()))
        .collect(Collectors.toList());
        return pairs;
    }
}
