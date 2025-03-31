package com.github.fujiyamakazan.zabuton.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 質量をもつオブジェクトを指定量までリストに追加する処理です。
 */
public class MaxQuantityListAdder {
    @SuppressWarnings("unused")
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

    public static <T extends IMassObject> List<T> extecute(List<T> source, int limit) {
        List<T> list = new ArrayList<T>();
        double totalMass = 0;

        for (T obj : source) {
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

    public static void main(String[] args) {
        List<MassObjectSample> sourceList = new ArrayList<>();
        sourceList.add(new MassObjectSample("Object1", 2));
        sourceList.add(new MassObjectSample("Object2", 1));
        sourceList.add(new MassObjectSample("Object3", 1));
        sourceList.add(new MassObjectSample("Object4", 1));

        sub(sourceList);
        sub(sourceList);
        sub(sourceList);
        sub(sourceList);
        sub(sourceList);
        sub(sourceList);
        sub(sourceList);
        sub(sourceList);
        sub(sourceList);
        sub(sourceList);
    }

    protected static void sub(List<MassObjectSample> sourceList) {
        Collections.shuffle(sourceList);
        int maxMass = 3;
        List<MassObjectSample> transferredList = MaxQuantityListAdder.extecute(sourceList, maxMass);
        int sum = transferredList.stream().mapToInt(MassObjectSample::getMass).sum();
        System.out.println(sum + " : " + transferredList);
    }

    private static class MassObjectSample implements IMassObject {
        private final String name;
        private final int mass;

        public MassObjectSample(String name, int mass) {
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
}
