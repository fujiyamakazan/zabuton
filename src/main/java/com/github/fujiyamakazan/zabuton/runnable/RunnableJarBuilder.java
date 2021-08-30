package com.github.fujiyamakazan.zabuton.runnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;
import com.github.fujiyamakazan.zabuton.util.text.XmlText;

public class RunnableJarBuilder {

    private static final Logger log = LoggerFactory.getLogger(RunnableJarBuilder.class);

    /** ライブラリが使用するJREのモジュールを記録するファイルの名前。 */
    private static final String JDEPS_TXT = "jdeps.txt";

    private String projectName;
    private File javaHome;

    protected void execute() {
        log.info("Create JVM...");

        String javaHomeStr = System.getenv("JAVA_HOME");
        log.debug("javaHome:" + javaHomeStr);

        if (StringUtils.isEmpty(javaHomeStr)) {
            throw new RuntimeException("環境変数「JAVA_HOME」が登録されていません。");
        }

        this.javaHome = new File(javaHomeStr);

        XmlText projectXml = new XmlText(new File(".project"));
        this.projectName = projectXml.getTextOne("/projectDescription/name");
        log.debug("projectName:" + projectName);

        File dirTarget = new File("target");
        //File jar = new File(dirTarget, projectName + ".jar");

        /*
         * ライブラリをスキャンして情報を取得する
         */
        File dirDependency = new File(dirTarget, "dependency");
        File dirDependencyInfo = new File(dirTarget, "dependency-info");

        List<File> dependencies = Arrays.asList(dirDependency.listFiles());
        String licenseListTitle = projectName + "が使用するライブラリ";
        DependencyInspector.scanJar(dependencies, dirDependencyInfo, licenseListTitle, JDEPS_TXT, javaHome);// TODO メソッドの場所が不自然

        /*
         * JREを生成する
         */
        final String nameJimage = "jimage"; // customランタイムのフォルダ名
        final File dirJimage = new File(dirTarget, nameJimage);
        /* 必須モジュールの一覧を作成する */
        List<String> mods = new ArrayList<String>();
        for (File jeps : FileUtils.listFiles(dirDependencyInfo,
            FileFilterUtils.nameFileFilter(JDEPS_TXT), // ファイル名のフィルタ
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
        log.debug("mods:" + mods);

        /* JREを作成する */
        JreMaker.createJre(javaHome, dirJimage, mods);

        /*
         * Jarを起動するためのスクリプトを作成する
         */
        RunnableJarBuilder.createInvokeJarScripts(dirTarget, projectName, nameJimage);

    }

    /**
     * 実行可能Jarを起動するためのスクリプトを生成します。
     * @param dirResult 生成場所のディレクトリ
     * @param appName アプリケーション名
     * @param nameJimage JVMのファイル名
     */
    public static void createInvokeJarScripts(File dirResult, String appName, String nameJimage) {
        String bat = ".\\" + nameJimage + "\\bin\\java -jar " + appName + ".jar\n";
        String vbs = "Set ws = CreateObject(\"Wscript.Shell\")\n ws.run \"cmd /c main.bat\", vbhide\n";
        new Utf8Text(new File(dirResult, "main.bat")).write(bat);
        new Utf8Text(new File(dirResult, "main.vbs")).write(vbs);
    }

}
