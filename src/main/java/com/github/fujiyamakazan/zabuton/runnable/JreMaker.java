package com.github.fujiyamakazan.zabuton.runnable;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fujiyamakazan.zabuton.util.ListToStringer;
import com.github.fujiyamakazan.zabuton.util.exec.RuntimeExc;
import com.github.fujiyamakazan.zabuton.util.file.FileDeleteUtils;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

/**
 * 配布用に最小限のJREを作成します。
 *
 * @author fujiyama
 */
public class JreMaker implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ZabutonBuilder.class);

    /**
     * 配布用に最小限のJREを作成します。
     *
     * @param out 作成先
     */
    public static void make(File jdk, File out, File dirDependencyInfo) {

        /* 必須モジュールの一覧を作成する */
        List<String> mods = new ArrayList<String>();
        for (File jeps : FileUtils.listFiles(dirDependencyInfo,
            FileFilterUtils.nameFileFilter(DependencyInspector.JDEPS_TXT), // ファイル名のフィルタ
            TrueFileFilter.INSTANCE) // ディレクトリ名は限定しない
        ) {
            /* jdeps.txt */
            Utf8Text f = new Utf8Text(jeps);
            for (String line : f.readLines()) {
                if (mods.contains(line) == false) {
                    mods.add(line);
                }
            }
        }
        mods.add("java.security.jgss"); // TODO 検知できないため強制的に追加
        LOGGER.debug("mods:" + mods);

        List<String> listMods = new ArrayList<String>(mods);
        Collections.sort(listMods);
        String strMods = ListToStringer.convert(listMods, ",");

        File modulesTxt = new File(out, "modules.txt");

        Utf8Text fileMods = new Utf8Text(modulesTxt);
        if (out.exists() && StringUtils.equals(strMods, fileMods.read()) == false) {
            /* モジュール情報が変更されていれば、一旦削除 */
            FileDeleteUtils.delete(out);
        }

        if (out.exists() == false) {

            /* JREを作成する */
            File jlink = new File(jdk, "bin/jlink.exe"); // JREを作成するプログラム
            File jmods = new File(jdk, "jmods"); // モジュールディレクトリ

            RuntimeExc runtimeExcJLink = new RuntimeExc();
            runtimeExcJLink.exec(
                jlink.getAbsolutePath(),
                "--compress=2",
                "--module-path", jmods.getAbsolutePath(),
                "--add-modules", strMods,
                "--output", out.getAbsolutePath());

            LOGGER.debug(runtimeExcJLink.getOutText());
            String errText = runtimeExcJLink.getErrText();
            if (StringUtils.isNotEmpty(errText)) {
                LOGGER.warn(errText);
            }
            if (runtimeExcJLink.isSuccess() == false) {
                throw new RuntimeException(errText);
            }

            /* モジュール情報を書出す*/
            fileMods.write(strMods);
        }
    }
}
