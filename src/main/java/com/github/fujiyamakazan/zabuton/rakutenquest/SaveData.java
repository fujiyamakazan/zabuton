package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.io.FilenameUtils;

public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SaveData.class);

    private final File file;

    public SaveData(File file) {
        this.file = file;
    }


    public String getName() {
        return FilenameUtils.removeExtension(this.file.getName());
    }
}
