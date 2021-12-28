package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

public class SaveDataManager implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SaveDataManager.class);

    private final List<SaveData> datas = Generics.newArrayList();

    /**
     * コンストラクタです。
     * メンバにセーブデータを登録します。
     */
    public SaveDataManager(File appDir) {
        File dirData = new File(appDir, "datas");
        if (dirData.exists() == false) {
            dirData.mkdirs();
        }
        for (File file: dirData.listFiles()) {
            datas.add(new SaveData(file));
        }

    }

    public List<SaveData> getDatas() {
        return datas;
    }
}
