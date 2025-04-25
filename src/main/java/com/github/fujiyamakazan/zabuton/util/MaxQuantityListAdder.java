package com.github.fujiyamakazan.zabuton.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 質量をもつオブジェクトを指定量までリストに追加する処理です。
 */
public class MaxQuantityListAdder {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    /**
     * 質量をもつオブジェクトに付与するインターフェースです。
     */
    public interface IMassObject {
        /**
         * 質量を返します。
         */
        int getMass();
    }

    public static <T extends IMassObject> List<T> extecute(final List<T> source, final int limit) {
        final List<T> list = new ArrayList<T>();
        double totalMass = 0;

        for (final T obj : source) {
            if (totalMass + obj.getMass() > limit) {
                // 質量オーバーなので追加しない。
                // breakはしない。この後に、質量が小さいオブジェクトで満たされる可能性があるため。
            } else {
                list.add(obj);
                totalMass += obj.getMass();
            }

        }
        return list;
    }

    public static void main(final String[] args) {

        class MassObj implements IMassObject {
            private final String name;
            private final int mass;

            public MassObj(final String name, final int mass) {
                this.name = name;
                this.mass = mass;
            }

            @Override
            public int getMass() {
                return mass;
            }

            @Override
            public String toString() {
                return name + "(" + mass + "kg)";
            }
        }

        final List<MassObj> list = new ArrayList<>();
        list.add(new MassObj("①", 2));
        list.add(new MassObj("②", 1));
        list.add(new MassObj("③", 1));
        list.add(new MassObj("④", 1));

        for (int i = 0; i < 10; i++) {
            Collections.shuffle(list);
            final List<MassObj> results = MaxQuantityListAdder.extecute(list, 3);
            LOGGER.debug(results.toString() + " 合計："+ results.stream().mapToInt(MassObj::getMass).sum());

        }



    }

}
