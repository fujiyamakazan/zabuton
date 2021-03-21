package net.nanisl.zabuton.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.nanisl.zabuton.util.ListToStringer;
import net.nanisl.zabuton.util.exec.RuntimeExc;
import net.nanisl.zabuton.util.file.FileDeleteUtils;
import net.nanisl.zabuton.util.file.Utf8FileObj;

/**
 * 配布用に最小限のJREを作成する
 *
 * @author fujiyama
 */
public class JreMaker {

    private static final Logger log = LoggerFactory.getLogger(JreMaker.class);

    /**
     * 配布用に最小限のJREを作成する
     *
     * @param target 作成先
     * @param mods モジュール（カンマ区切り）
     */
    public static void createJre(File jdk, File out, Collection<String> mods) {

        List<String> listMods = new ArrayList<String>(mods);
        Collections.sort(listMods);
        String strMods = ListToStringer.convert(listMods, ",");

        File modulesTxt = new File(out, "modules.txt");

        Utf8FileObj fileMods = Utf8FileObj.of(modulesTxt);
        if (out.exists() && StringUtils.equals(strMods, fileMods.readFileToString()) == false) {
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
                //					"--launcher", // TODO ランチャー？
                //					"sample=com.example.musiccopy/com.example.musiccopy.MusicCopy",
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
            fileMods.writeString(strMods);
        }
    }
}
