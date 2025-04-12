package com.github.fujiyamakazan.zabuton.util.file;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.wicket.util.lang.Generics;

public class FileDirUtils implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(FileDirUtils.class);

    /**
     * 配下も含めてすべてのファイルを取得します。
     */
    public static List<File> listAll(File f) {
        List<File> results = Generics.newArrayList();
        for (File c: f.listFiles()) {
            if (c.isDirectory()) {
                results.addAll(listAll(c));
            } else {
                results.add(c);
            }
        }
        return results;
    }

    /**
     * 直近のファイルを１つ返します。
     * ファイルが取得できなけければnullを返します。
     */
    public static final File getLastOne(File dir) {
        File lastFile;
        List<File> list = new ArrayList<File>(Arrays.asList(dir.listFiles()));
        if (list.isEmpty()) {
            lastFile = null;
        } else {
            Collections.sort(list, new LastModifiedFileComparator());
            Collections.reverse(list);
            lastFile = list.get(0);
        }
        return lastFile;
    }
}
