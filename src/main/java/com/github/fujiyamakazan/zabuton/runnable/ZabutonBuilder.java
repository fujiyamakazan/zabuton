package com.github.fujiyamakazan.zabuton.runnable;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;
import com.github.fujiyamakazan.zabuton.util.text.XmlText;

/**
 * 成果物をビルドします。
 * @author fujiyama
 */
public class ZabutonBuilder implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 「target」フォルダに成果物を作ります。
     */
    protected void execute(File appDir) {

        /* 禁則文字のチェックをします */
        NgWordCheck.execute(appDir);

        File dirTarget = new File("target");

        /* 環境変数からJAVA_HOMEの場所を取得します。*/
        String strJh = System.getenv("JAVA_HOME");
        if (StringUtils.isEmpty(strJh)) {
            throw new RuntimeException("環境変数「" + strJh + "」が登録されていません。");
        }
        File javaHome = new File(strJh);

        /* ライブラリをスキャンして情報を取得します。 */
        final List<File> dependencyInfos;
        final File dirDependency = new File(dirTarget, "dependency");
        final File dirDependencyInfo = new File(dirTarget, "dependency-info");
        if (skipDependensyScan() == false) {
            dependencyInfos = DependencyInspector.scanJar(
                Arrays.asList(dirDependency.listFiles()),
                dirDependencyInfo,
                javaHome);

        } else {
            dependencyInfos = null;
        }

        /* JREを作ります  */
        final String jrePath = createJre(dirTarget, javaHome, dirDependencyInfo);

        /* プロジェクト名を取得します。 */
        //String pjName = new XmlText(new File(".project")).getTextOne("/projectDescription/name");
        String pjName = getProjectName();

        /*
         * Jarを起動するためのスクリプトを作ります。
         */
        String bat = "cd /d %~dp0\n"
            + jrePath + " -jar " + pjName + ".jar\n";
        String vbs = "Set ws = CreateObject(\"Wscript.Shell\")\n ws.run \"cmd /c main.bat\", vbhide\n";
        new Utf8Text(new File(dirTarget, "main.bat")).write(bat);
        new Utf8Text(new File(dirTarget, "main.vbs")).write(vbs);

        /*
         * NOTICEファイルを作成します。
         */
        if (skipDependensyScan() == false) {
            String title = pjName + "が使用するライブラリ";
            final File fileNotice = new File(dirTarget, "NOTICE.html");
            NoticeMaker.make(fileNotice, title, dependencyInfos);
        }

    }

    protected boolean skipDependensyScan() {
        return false;
    }

    /** JREを作ります。 */
    protected String createJre(File dirTarget, File javaHome, File dirDependencyInfo) {

        //final String nameJimage = "jimage"; // カスタムランタイムのフォルダ名
        final File dirJimage = new File(dirTarget, "jimage");
        JreMaker.make(javaHome, dirJimage, dirDependencyInfo);
        final String nameJimage = dirJimage.getName();
        final String jrePath = ".\\" + nameJimage + "\\bin\\java";
        return jrePath;
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

    /**
     * .projectに記述されたプロジェクト名を取得します。(Eclipseからの起動を想定)
     */
    protected static String getProjectName() {

        File projectFile = new File(".project");
        if (projectFile.exists() == false) {
            throw new RuntimeException(projectFile.getAbsolutePath() + "からプロジェクト名を取得できません。");
        }
        return new XmlText(projectFile).getTextOne("/projectDescription/name");
    }

}
