package com.github.fujiyamakazan.zabuton.sample.sample2023sort;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.comparators.NullComparator;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.date.Chronus;

/**
 * Comparatorをimplementsするときのサンプルです。
 *
 * 要素にnullが入る可能性を考慮し、NullComparatorでラップします。
 * ここでは第１ソートキーから第３ソートキーまで、判定した結果を返しています。
 * それぞれのチェックでも比較演算子は使わず、あくまでもComparatorを使います。
 *
 *
 * ■ なぜとことんComparatorを使うのか？
 * [自作Comparatorで「IllegalArgumentException: Comparison method violates its general contract!」が発生することについてのドリル。]
 * で示した通り、判定処理には予期せぬワナが山ほど潜んでいます。いずれのパターンも、Comparator（つまりはcompareTo）を
 * 使えば、発生しませんでした。
 * 加えて、「1」「0」「-1」と言ったマジックナンバーに悩まされることもありません。
 *
 * ■ ComparatorChainとBeanComparatorについて
 * この実装にもマジックナンバー「0」は残留しています。
 * このあたり、ComparatorChainで解決できるかと思いましたが、null対応を組み合わせた場合、
 * どうしてもBeanComparatorが必要になりました。
 * BeanComparatorは変数名をリテラルで指定する必要があります。
 * リファクタリングで事故が起きることを考え、保留しました。
 *
 * ■ Stream API や ラムダ式の利用。
 * これは、今後の学習課題。Java8移行、とてもスマートな記述方法が多数紹介されています。
 * が、まだNull対応なども含めた完全版が見つかりません。
 * それに、過去の遺産にはComparatorが山ほど。
 * しばらくはこのスタイルで・・・
 *
 * ■ テストパターンのサンプル
 * mainメソッドに書きました。
 * ポイントはキーごとに「小」「大」「同じ」「null」でバリエーションを組むこと。
 * もし独自の判定文が含まれるようなら、要素数を32個以上にし、十分な試行回数を試すことも必要です。
 *
 * @author fujiyama
 */
public class SampleComparator implements Comparator<SampleSortItme> {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SampleComparator.class);

    @Override
    public int compare(SampleSortItme o1, SampleSortItme o2) {

        return new NullComparator<SampleSortItme>(new Comparator<SampleSortItme>() {

            @Override
            public int compare(SampleSortItme o1, SampleSortItme o2) {
                int r;
                r = new NullComparator<Integer>().compare(o1.getData1st(), o2.getData1st());
                if (r != 0) {
                    return r;
                }
                r = new NullComparator<Date>().compare(o1.getData2nd(), o2.getData2nd());
                if (r != 0) {
                    return r;
                }
                r = new NullComparator<String>().compare(o1.getData3rd(), o2.getData3rd());
                return r;
            }
        }).compare(o1, o2);

    }

    /**
     * 処理を実行します。
     */
    public static void main(String[] args) {


        List<SampleSortItme> list1 = Generics.newArrayList();
        int id = 1;

        list1.add(new SampleSortItme(id++, 1000, Chronus.dateOf(2023, 1, 1), "text2")); // 第３キー 小さい値
        list1.add(new SampleSortItme(id++, 1000, Chronus.dateOf(2023, 1, 1), "text3")); // 第３キー 基準値
        list1.add(new SampleSortItme(id++, 1000, Chronus.dateOf(2023, 1, 1), "text3")); // 第３キー 同じ値
        list1.add(new SampleSortItme(id++, 1000, Chronus.dateOf(2023, 1, 1), "text4")); // 第３キー 大きい値
        list1.add(new SampleSortItme(id++, 1000, Chronus.dateOf(2023, 1, 1), null)); // 第３キー null
        list1.add(new SampleSortItme(id++, 1000, Chronus.dateOf(2023, 1, 1), null)); // 第３キー null

        list1.add(new SampleSortItme(id++, 1000, Chronus.dateOf(2023, 1, 2), "text1")); // 第２キー 小さい値
        list1.add(new SampleSortItme(id++, 1000, Chronus.dateOf(2023, 1, 3), "text1")); // 第２キー 基準値
        list1.add(new SampleSortItme(id++, 1000, Chronus.dateOf(2023, 1, 3), "text1")); // 第２キー 同じ値
        list1.add(new SampleSortItme(id++, 1000, Chronus.dateOf(2023, 1, 4), "text1")); // 第２キー 大きい値
        list1.add(new SampleSortItme(id++, 1000, null, "test1")); // 第２キー null
        list1.add(new SampleSortItme(id++, 1000, null, "test1")); // 第２キー null

        list1.add(new SampleSortItme(id++, 2000, Chronus.dateOf(2023, 1, 1), "text1")); // 第１キー 小さい値
        list1.add(new SampleSortItme(id++, 3000, Chronus.dateOf(2023, 1, 1), "text1")); // 第１キー 基準値
        list1.add(new SampleSortItme(id++, 3000, Chronus.dateOf(2023, 1, 1), "text1")); // 第１キー 同じ値
        list1.add(new SampleSortItme(id++, 4000, Chronus.dateOf(2023, 1, 1), "text1")); // 第１キー 大きい値
        list1.add(new SampleSortItme(id++, null, Chronus.dateOf(2023, 1, 1), "text1")); // 第１キー null
        list1.add(new SampleSortItme(id++, null, Chronus.dateOf(2023, 1, 1), "text1")); // 第１キー null

        list1.add(null); // null
        list1.add(null); // null
        List<SampleSortItme> list = list1;

        Collections.shuffle(list);

        LOGGER.debug("ソート前---");
        for (SampleSortItme b : list) {
            LOGGER.debug(b == null ? "null" : b.toString());
        }

        list.sort(new SampleComparator());

        LOGGER.debug("ソート後---");
        for (SampleSortItme b : list) {
            LOGGER.debug(b == null ? "null" : b.toString());
        }
    }
}
