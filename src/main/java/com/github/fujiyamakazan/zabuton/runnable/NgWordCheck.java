package com.github.fujiyamakazan.zabuton.runnable;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fujiyamakazan.zabuton.util.KeyValue;
import com.github.fujiyamakazan.zabuton.util.ListToStringer;
import com.github.fujiyamakazan.zabuton.util.text.KeyValuesText;
import com.github.fujiyamakazan.zabuton.util.text.SeparateKeyValuesText;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

/**
 * 禁則文字をチェックします。
 * @author fujiyama
 */
public class NgWordCheck implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(NgWordCheck.class);

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {
        execute(new File("."));
    }

    /**
     * 禁則文字をチェックします。
     */
    public static void execute(File userAppDir) {
        File fileSettings = new File(userAppDir, "CheckNgWords.settings.txt");
        if (fileSettings.exists() == false) {
            LOGGER.debug("禁則文字のチェックをする場合は、" + fileSettings.getAbsolutePath() + "を設置してください。");
            return;
        }
        LOGGER.debug("禁則文字のチェックをします。");

        KeyValuesText settings = new SeparateKeyValuesText(fileSettings);

        final String[] words;
        String strWords = settings.get("words");
        if (StringUtils.isNotEmpty(strWords)) {
            words = strWords.split(",");
        } else {
            words = new String[] {};
        }

        final String[] fileOmits;
        String strFileOmits = settings.get("fileOmits");
        if (StringUtils.isNotEmpty(strFileOmits)) {
            fileOmits = strFileOmits.split(",");
        } else {
            fileOmits = new String[] {};
        }

        final String[] dirOmits;
        String strDirOmits = settings.get("dirOmits");
        if (StringUtils.isNotEmpty(strDirOmits)) {
            dirOmits = strDirOmits.split(",");
        } else {
            dirOmits = new String[] {};
        }

        IOFileFilter fileFilter = new IOFileFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return true;
            }

            @Override
            public boolean accept(File file) {
                for (String o : fileOmits) {
                    if (file.getName().matches(o)) {
                        return false;
                    }
                }
                return true;
            }
        };

        IOFileFilter dirFilter = new IOFileFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return accept(dir.getName());
            }

            @Override
            public boolean accept(File file) {
                return accept(file.getName());
            }

            protected boolean accept(String dirName) {
                for (String o : dirOmits) {
                    if (dirName.matches(o)) {
                        return false;
                    }
                }
                return true;
            }
        };

        class FileWord implements Serializable {
            private static final long serialVersionUID = 1L;
            File file;
            String word;

            public FileWord(File file, String word) {
                this.file = file;
                this.word = word;
            }

            @Override
            public String toString() {
                return String.format("禁則文字[%s]を検出：%s", word, file.getAbsoluteFile());
            }

        }

        List<FileWord> errors = Generics.newArrayList();
        for (File f : FileUtils.listFiles(new File("./"), fileFilter, dirFilter)) {
            //LOGGER.debug(f.getAbsolutePath());
            String text = Utf8Text.readData(f);
            for (String word : words) {
                if (StringUtils.containsIgnoreCase(text, word)) {
                    errors.add(new FileWord(f, word));
                }
            }
        }
        if (errors.isEmpty() == false) {
            List<KeyValue> ignores = Generics.newArrayList();
            String str = settings.get("ignores");
            if (StringUtils.isNotEmpty(str)) {
                for (String ignore : str.split(",")) {
                    ignores.add(new KeyValue(ignore.split("=")[0], ignore.split("=")[1]));
                }
            }
            for (Iterator<FileWord> ite = errors.iterator(); ite.hasNext();) {
                FileWord e = ite.next();
                boolean contains = false;
                for (KeyValue ignore : ignores) {
                    if (ignore.getKey().equals(e.file.getName())
                        && ignore.getValue().equals(e.word)) {
                        contains = true;
                        break;
                    }
                }
                if (contains) {
                    ite.remove();
                }
            }

            if (errors.isEmpty() == false) {
                throw new RuntimeException(ListToStringer.convert(errors, "\n"));
            }
        }
        LOGGER.debug("禁則文字のチェックを終了します。");
    }
}
