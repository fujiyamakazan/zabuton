package com.github.fujiyamakazan.zabuton.sample.sample2023sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 自作Comparatorで「IllegalArgumentException: Comparison method violates its general contract!」が発生することについてのドリル。
 *
 * メッセージをヒントに問題解決をしようとしても、なかなか原因が突き止められません。
 * さらに厄介なことに、データの状態に左右されるため、
 * 本番環境で発生しても、テスト環境で再現しないケースがほとんどです。
 *
 * ここでは、問題のあるソースコードを示して、その原因を解説します。
 *
 * なお、Java SE 7以上が前提条件です。
 *
 * ★設問１～設問４★
 * 各メソッドのJavaDocをご覧ください。
 *
 * ★設問５★
 * これらのエラーは、Javaのバージョンを下げたり、設定を変更すると回避できます。
 * ・・・それでいいんですか？
 *
 * ☆回答５☆
 * アルゴリズムが誤っていることには変わりないです。
 * また、「安定ソート」にもならない可能性が高いです。
 *
 * ～まとめ～
 * 日付も数値もそのクラス自身が「compareTo」メソッドを持っています。
 * 不必要に実装する事を避け、なるべく事故の起きない実装については、別に解説します。
 *
 * @author fujiyama
 */
public class SampleBadSort {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SampleBadSort.class);

    private static final int SIZE = 32;

    /**
     * 処理を実行します。
     */
    public static void main(String[] args) {

        List<LocalDate> days = sample1();
        for (LocalDate date : days) {
            LOGGER.debug(date.toString());
        }

        List<Integer> numbers = sample2();
        for (Integer num : numbers) {
            LOGGER.debug(String.valueOf(num));
        }

        for (int i = 0; i < 1000; i++) {
            System.out.println(i + "回目");
            List<Date> dates = sample3();
            for (Date date : dates) {
                LOGGER.debug(date.toString());
            }
        }

    }

    /**
     * ★設問１★
     * 10個のランダムな日付をソートするプログラムです。
     * 要素数「SIZE」をある値数以上にするとエラーになります。その値はなんでしょう？
     *
     * ★設問２★
     * 「Comparison method violates its general contract!」とは、「比較方法が違反!」という意味です。
     * どのような違反がありますか？
     *
     * ☆回答１☆
     * 32。(java.util.TimSort.MIN_MERGEによる。)
     *
     * ☆回答２☆
     * day1.equals(day2) のとき0を返していない。
     */
    private static List<LocalDate> sample1() {
        Random rand = new Random();

        List<LocalDate> days = new ArrayList<LocalDate>();
        while (days.size() < SIZE) {
            /* 3種類の要素を持つランダムな日付 */
            days.add(LocalDate.now().plusDays(rand.nextInt(3)));
        }
        /* Comparatorを実装して並び替えます。 */
        days.sort(new Comparator<LocalDate>() {
            @Override
            public int compare(LocalDate o1, LocalDate o2) {
                if (o1.isAfter(o2)) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return days;
    }

    /**
     * ★設問３★
     * 3種類(1000, 1001, 1002)の要素を持つランダムなIntegerをソートするプログラムです。
     * やはり「Comparison method violates its general contract!」が発生します。
     * ここでの違反は何ですか？
     *
     * ☆回答３☆
     * Integerの比較に「==」を使用している。「equals」メソッドを使用するべきです。
     * ※ Integerは「127」までは「==」が有効なので、気づきづらい。
     *
     */
    private static List<Integer> sample2() {
        Random rand = new Random();

        /* Integerのソート */
        List<Integer> numbers = new ArrayList<Integer>();
        while (numbers.size() < SIZE) {
            /* 3種類(1000, 1001, 1002)の要素を持つランダムなInteger */
            Integer num = Integer.valueOf(rand.nextInt(3) + 1000);
            numbers.add(num);
            LOGGER.debug(num + "");
        }

        /* Comparatorを実装して並び替えます。 */
        numbers.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if (o1.equals(o2)) {
                    return 0;
                } else if (o1 < o2) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return numbers;
    }

    /**
     * ★設問４★
     * 3種類(今日, 明日, 25日)の要素を持つランダムな日付をソートするプログラムです。
     * 何度も試すと「Comparison method violates its general contract!」が発生します。
     * ここでの違反は何ですか？
     *
     * ☆回答４☆
     * 日付の差分を取得した結果はlong型。それを無理やり、intにキャストしています。
     * もし日付が25日以上離れていると、intの上限を超えるため、プラスとマイナスが逆転する。
     *
     */
    private static List<Date> sample3() {

        Random r = new Random();

        /* 日付のソート２ */
        List<Date> dates = new ArrayList<Date>();

        while (dates.size() < SIZE) {
            Calendar c = Calendar.getInstance();
            int nextInt = r.nextInt(3);
            if (nextInt == 0) {
                c.add(Calendar.DAY_OF_MONTH, 1);
            } else if (nextInt == 1) {
                c.add(Calendar.DAY_OF_MONTH, 25);
            }
            dates.add(c.getTime());
        }

        /* Comparatorを実装して並び替えます。 */
        dates.sort(new Comparator<Date>() {
            @Override
            public int compare(Date o1, Date o2) {
                /* 日付の差分を取得 */
                long span = o1.getTime() - o2.getTime();
                return (int) span;
            }
        });
        return dates;
    }

}
