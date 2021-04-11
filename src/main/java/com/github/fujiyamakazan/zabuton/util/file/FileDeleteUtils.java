package com.github.fujiyamakazan.zabuton.util.file;

import java.io.File;
import java.io.Serializable;

public class FileDeleteUtils implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 再帰的にファイルを削除します。
     * @param target 削除対象
     */
    public static void delete(File target) {
        if (target.isDirectory()) {
            for (File sub : target.listFiles()) {
                delete(sub);
            }
        }
        if (target.delete() == false) {
            throw new RuntimeException("Delete Failure " + target.getAbsolutePath());
        }
    }
}
