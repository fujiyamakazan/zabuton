package com.github.fujiyamakazan.zabuton.runnable;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;
import com.github.fujiyamakazan.zabuton.util.text.XmlText;

/**
 * 成果物をビルドします。
 * @author fujiyama
 */
public class ZabutonBuilder implements Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(ZabutonBuilder.class);

    /**
     * 「target」フォルダに成果物を作ります。
     */
    protected void execute() {
        File dirTarget = new File("target");

        /* 環境変数からJAVA_HOMEの場所を取得します。*/
        String strJh = System.getenv("JAVA_HOME");
        if (StringUtils.isEmpty(strJh)) {
            throw new RuntimeException("環境変数「" + strJh + "」が登録されていません。");
        }
        File javaHome = new File(strJh);

        /* ライブラリをスキャンして情報を取得します。 */
        File dirDependency = new File(dirTarget, "dependency");
        File dirDependencyInfo = new File(dirTarget, "dependency-info");

        final List<File> dependencyInfos = DependencyInspector.scanJar(
            Arrays.asList(dirDependency.listFiles()),
            dirDependencyInfo,
            javaHome);

        /* JREを作ります。 */
        final String nameJimage = "jimage"; // カスタムランタイムのフォルダ名
        final File dirJimage = new File(dirTarget, nameJimage);
        JreMaker.make(javaHome, dirJimage, dirDependencyInfo);

        /* プロジェクト名を取得します。 */
        String pjName = new XmlText(new File(".project")).getTextOne("/projectDescription/name");

        /*
         * Jarを起動するためのスクリプトを作ります。
         */
        String bat = ".\\" + nameJimage + "\\bin\\java -jar " + pjName + ".jar\n";
        String vbs = "Set ws = CreateObject(\"Wscript.Shell\")\n ws.run \"cmd /c main.bat\", vbhide\n";
        new Utf8Text(new File(dirTarget, "main.bat")).write(bat);
        new Utf8Text(new File(dirTarget, "main.vbs")).write(vbs);

        /*
         * NOTICEファイルを作成します。
         */
        String title = pjName + "が使用するライブラリ";
        final File fileNotice = new File(dirTarget, "NOTICE.html");
        NoticeMaker.make(fileNotice, title, dependencyInfos);

    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        File dirTarget = EnvUtils.getUserDesktop();
        File dirDependency = new File(dirTarget, "dependency");
        File dirDependencyInfo = new File(dirTarget, "dependency-info");
        List<File> dependencies = Arrays.asList(dirDependency.listFiles());

        /* ライブラリをスキャンして情報を取得します。 */
        List<File> dependencyInfos = DependencyInspector.scanJar(
            dependencies,
            dirDependencyInfo,
            null);

        /*
         * NOTICEファイルを作成します。
         */
        String title = "test" + "が使用するライブラリ";
        final File fileNotice = new File(dirTarget, "NOTICE.html");
        NoticeMaker.make(fileNotice, title, dependencyInfos);

    }

}
