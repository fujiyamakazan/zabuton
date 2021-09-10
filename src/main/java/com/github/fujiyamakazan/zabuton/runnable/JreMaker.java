package com.github.fujiyamakazan.zabuton.runnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fujiyamakazan.zabuton.util.ListToStringer;
import com.github.fujiyamakazan.zabuton.util.exec.RuntimeExc;
import com.github.fujiyamakazan.zabuton.util.file.FileDeleteUtils;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

/**
 * 配布用に最小限のJREを作成する
 *
 * @author fujiyama
 */
public class JreMaker {

    private static final Logger log = LoggerFactory.getLogger(JreMaker.class);

    /**
     * 配布用に最小限のJREを作成します。
     *
     * @param out 作成先
     * @param mods モジュール（カンマ区切り）
     */
    public static void createJre(File jdk, File out, Collection<String> mods) {

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

            log.debug(runtimeExcJLink.getOutText());
            String errText = runtimeExcJLink.getErrText();
            if (StringUtils.isNotEmpty(errText)) {
                log.warn(errText);
            }
            if (runtimeExcJLink.isSuccess() == false) {
                throw new RuntimeException(errText);
            }

            /* モジュール情報を書出す*/
            fileMods.write(strMods);
        }
    }
}
