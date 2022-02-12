package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

/**
 * staticメソッドだけにできるのでは？SaveDataへの統合を検討する。
 * @author fujiyama
 */
public class SaveDataManager implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SaveDataManager.class);

    private final List<SaveData> datas = Generics.newArrayList();
    private final File dirDatas;

    /**
     * コンストラクタです。
     * メンバにセーブデータを登録します。
     */
    public SaveDataManager(File appDir) {
        if (appDir.exists() == false) {
            appDir.mkdirs();
        }

        this.dirDatas = new File(appDir, "datas");
        if (dirDatas.exists() == false) {
            dirDatas.mkdirs();
        }
        for (File file : dirDatas.listFiles()) {
            datas.add(new SaveData(file));
        }

    }

    public List<SaveData> getDatas() {
        return datas;
    }

    public SaveData get(File appDir, String name) {
        File dirData = new File(appDir, "datas");
        return new SaveData(new File(dirData, name));
    }

    /**
     * セーブデータを作成します。
     */
    public SaveData createSaveData(String name) {
        File newDir = new File(dirDatas, name);
        newDir.mkdirs();
        SaveData newData = new SaveData(newDir);
        this.datas.add(newData);
        return newData;

    }
}
