package com.github.fujiyamakazan.zabuton.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

public abstract class ListDfMucher<T> {

    /** Aだけにしかないオブジェクトです。 */
    private final List<T> restLeft;

    /** Bだけにしかないオブジェクトです。 */
    private final List<T> restRight;

    /** [eq]メソッドをもとにマッチしたオブジェクトです。 */
    private List<KeyValueObj<T, T>> pairs = Generics.newArrayList();

    public ListDfMucher(List<T> listA, List<T> listB) {
        this.restLeft = new ArrayList<T>(listA); // 参照を切る
        this.restRight = new ArrayList<T>(listB); // 参照を切る
    }

    /**
     * 主処理です。
     */
    public void execute() {
        for (Iterator<T> iteA = restLeft.iterator(); iteA.hasNext();) {
            T a = iteA.next();
            for (Iterator<T> iteB = restRight.iterator(); iteB.hasNext();) {
                T b = iteB.next();
                if (eq(a, b)) {
                    /* ペアとして登録する */
                    pairs.add(new KeyValueObj<T, T>(a, b));

                    /* 処理が終わったので消し込む */
                    iteA.remove();
                    iteB.remove();
                    continue;
                }
            }
        }
    }

    protected abstract boolean eq(T a, T b);

    public List<T> getRestLeft() {
        return restLeft;
    }

    public List<T> getRestRight() {
        return restRight;
    }

    public List<KeyValueObj<T,T>> getPairs() {
        return pairs;
    }

}
