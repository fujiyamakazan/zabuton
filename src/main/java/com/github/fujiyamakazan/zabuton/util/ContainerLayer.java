package com.github.fujiyamakazan.zabuton.util;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.date.Chronus;

public abstract class ContainerLayer<K, D> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ContainerLayer.class);

    private abstract static class ContainerMap<K, D> implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Map<K, List<D>> map = Generics.newHashMap();

        /**
         * データを登録します。
         */
        public void put(D data) {
            K key = dataToKey(data);
            List<D> datas = map.get(key);
            if (datas == null) {
                datas = Generics.newArrayList();
                map.put(key, datas);
            }
            datas.add(data);
        }

        /**
         * 登録済みのキーを取得します。
         */
        public List<K> getKeys() {
            List<K> keys = Generics.newArrayList();
            for (Entry<K, List<D>> entry : map.entrySet()) {
                keys.add(entry.getKey());
            }
            return keys;
        }

        /**
         * キーに紐づく登録済みのデータを取得します。
         */
        public List<D> getDatas(K key) {
            return map.get(key);
        }

        /**
         * グループ化の基準となるキーを取得します。
         */
        protected abstract K dataToKey(D body);

    }

    private class Item implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int num;
        private final List<Item> children = Generics.newArrayList();
        private final List<D> datas = Generics.newArrayList();

        public Item(int num) {
            this.num = num;
        }
    }

    private final Item root;

    /**
     * コンストラクタです。ContainerMapを階層化します。
     */
    public ContainerLayer(ContainerMap<K, D> cm) {
        root = new Item(0);

        for (K key : sortKeys(cm)) {

            LocalDate date = (LocalDate) key;

            int year = date.getYear();
            Item y = root.children.stream().filter(item -> item.num == year).findFirst().orElse(null);
            if (y == null) {
                root.children.add(y = new Item(year));
            }

            int monthValue = date.getMonthValue();
            Item m = y.children.stream().filter(item -> item.num == monthValue).findFirst().orElse(null);
            if (m == null) {
                y.children.add(m = new Item(monthValue));
            }

            int dayOfMonth = date.getDayOfMonth();
            Item d = m.children.stream().filter(item -> item.num == dayOfMonth).findFirst().orElse(null);
            if (d == null) {
                m.children.add(d = new Item(dayOfMonth));
            }

            d.datas.addAll(cm.getDatas(key));
        }
    }

    private List<K> sortKeys(ContainerMap<K, D> cm) {
        List<K> list = new ArrayList<K>(cm.getKeys());
        list.sort(new Comparator<K>() {
            @Override
            public int compare(K o1, K o2) {
                return ContainerLayer.this.compare(o1, o2);
            }
        });
        return list;
    }

    protected abstract int compare(K o1, K o2);

    private Item getRoot() {
        return root;
    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        File docs = EnvUtils.getUserDownload();
        Collection<File> files = FileUtils.listFiles(docs, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

        ContainerMap<LocalDate, File> cm = new ContainerMap<LocalDate, File>() {
            private static final long serialVersionUID = 1L;

            @Override
            protected LocalDate dataToKey(File file) {
                return Chronus.localDateOf(file.lastModified());
            }
        };
        for (File f : files) {
            cm.put(f);
        }

        LOGGER.debug("------------------------------------------------------------");

        ContainerLayer<LocalDate, File> cl = new ContainerLayer<LocalDate, File>(cm) {

            private static final long serialVersionUID = 1L;

            @Override
            protected int compare(LocalDate o1, LocalDate o2) {
                return o1.compareTo(o2);
            }

        };
        ContainerLayer<LocalDate, File>.Item root = cl.getRoot();
        debug(0, root);
    }

    private static void debug(int indent, ContainerLayer<LocalDate, File>.Item item) {
        String pad = StringUtils.repeat(' ', indent);
        LOGGER.debug(pad + item.num);
        for (File file : item.datas) {
            LOGGER.debug(pad + "-" + file.getName());
        }
        for (ContainerLayer<LocalDate, File>.Item child : item.children) {
            debug(indent + 1, child);
        }

    }

}
