package com.github.fujiyamakazan.zabuton.util.random;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * 「無作為」のユーティリティです。
 * @author fujiyama
 */
public class Roulette implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Roulette.class);

    /**
     * 無作為に１つの要素を抽出します。
     * @return 引数の中から無作為に抽出された一つの要素
     */
    public static <T> T randomOne(Collection<T> collection) {
        List<T> list = new ArrayList<T>(collection);
        T one = list.get(getRandomNumber(list.size()));
        return one;
    }

    /**
     * 無作為に１つの要素を抽出します。
     * @return 引数の中から無作為に抽出された一つの要素
     */
    public static <T> T randomOne(T[] array) {
        return randomOne(new ArrayList<T>(Arrays.asList(array)));
    }

    /**
     * 乱数を発生させます。
     * 例：引数8 → 0～7
     * @return 乱数(0～指定数未満の整数)
     */
    public static int getRandomNumber(int max) {
        return Double.valueOf(Math.random() * max).intValue();
    }

    public static boolean getRandomTrueOrFalse() {
        Random random = new Random();
        return random.nextBoolean();  // trueかfalseをランダムに生成
    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {
        List<String> c = Arrays.asList(new String[] {"a", "b", "c"});
        String one = randomOne(c);
        LOGGER.debug(one);
    }





}
