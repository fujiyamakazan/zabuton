package com.github.fujiyamakazan.zabuton.runnable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.exec.RuntimeExc;
import com.github.fujiyamakazan.zabuton.util.file.FileDeleteUtils;
import com.github.fujiyamakazan.zabuton.util.file.ZipUtils.UnzipTask;
import com.github.fujiyamakazan.zabuton.util.string.StringCutter;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

/**
 * 依存するライブラリ（Jar）の解析をします。
 * @author fujiyama
 */
public class DependencyInspector {
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyInspector.class);

    private static final String FILENAME_LICENSE = "LICENSE";
    private static final String FILENAME_NOTICE = "NOTICE";
    private static final String FILENAME_README = "README";

    /**
     * jarをスキャンし以下を行います。
     * ・ライセンス関連ファイルの取得
     * ・バージョン情報の取得
     * ・依存するJREのモジュールの情報を取得
     *
     * また、情報を集約したindex.htmlを作成します。
     */
    public static void scanJar(
        List<File> jars,
        File dirOut,
        String licenseListTitle,
        String jdepsTxt,
        File jdk) {

        List<File> jarInfos = Generics.newArrayList();

        for (File jar : jars) {

            final String jarFileName = jar.getName(); // ex. commons-collections4-4.4.jar
            if (StringUtils.endsWith(jarFileName, ".jar") == false) {
                continue;
            }

            final String jarName = StringCutter.left(jarFileName, ".jar"); // ex.) commons-collections4-4.4

            String artifactIdAndVersion = jarName;
            if (artifactIdAndVersion.endsWith("-SNAPSHOT")) {
                artifactIdAndVersion = StringCutter.left(artifactIdAndVersion, "-SNAPSHOT");
            }
            final String artifactId = StringCutter.leftOfLast(artifactIdAndVersion, "-");
            final String version = StringCutter.right(artifactIdAndVersion, "-"); // ex.) 4.4

            final File jarInfo = new File(dirOut, artifactId);
            jarInfos.add(jarInfo);
            if (jarInfo.exists() == false) {
                jarInfo.mkdirs();
            }
            Utf8Text verFile = new Utf8Text(new File(jarInfo, "VERSION.txt"));

            if (jarInfo.exists()) {
                /* バージョンが異なったら一旦削除 */
                String verText = verFile.read();
                if (StringUtils.equals(verText, version) == false) {
                    FileDeleteUtils.delete(jarInfo);
                }

            }

            if (jarInfo.exists() == false) {
                jarInfo.mkdir();

                /* バージョンを記録 */
                verFile.write(version);

                /* 依存するJREのモジュールを検査する */
                if (jdk != null) {
                    Set<String> modules = invokeJdeps(jdk, jar);
                    Utf8Text jreModulesFile = new Utf8Text(new File(jarInfo, jdepsTxt));
                    jreModulesFile.writeLines(new ArrayList<String>(modules));
                }

                /* index.htmlの作成に必要なファイルを収集する */
                getInfoFiles(jar, jarInfo);

            }
        }

        /* index.htmlを作成する */
        createIndexHtml(dirOut, licenseListTitle, jarInfos);
    }

    /**
     * jarに関連する情報を収集します。
     */
    private static void getInfoFiles(File jar, final File jarInfo) {

        LOGGER.debug("■" + jarInfo.getName() + "の情報収集");

        new UnzipTask(jar) {
            private static final long serialVersionUID = 1L;

            //boolean exist = false;

            @Override
            protected boolean accept(ZipEntry zipentry) {
                /*
                 * 処理対象をルート直下か、META-INFフォルダ配下に限定する。
                 */
                return zipentry.getName().contains("/") == false
                    || zipentry.getName().contains("META-INF/");
            }

            @Override
            protected void runByEntry(String name, File file) {

                if (StringUtils.contains(name, "/")) {
                    name = name.substring(name.indexOf('/') + 1);
                }

                /* 「LICENSE」テキスト */
                if (isLicenseFilename(name)) {
                    final String text = new Utf8Text(file).read();
                    save(text, name, jarInfo);
                    return;
                }

                /* 「NOTICE」テキスト */
                if (isNotice(name)) {
                    final String text = new Utf8Text(file).read();
                    save(text, name, jarInfo);
                    return;
                }

                /* 「README」テキスト */
                if (isReadMe(name)) {
                    final String text = new Utf8Text(file).read();
                    save(text, name, jarInfo);
                    return;
                }

                /* MANIFEST.MF */
                if (StringUtils.equals(name, "MANIFEST.MF")) {
                    final String text = new Utf8Text(file).read();
                    save(text, name, jarInfo);
                }
            }
        }.start();

        /*
         * jarファイル名から.m2を探索して jarファイル名.pomを引き当てる。
         */
        File m2 = new File(EnvUtils.getUserProfile(), ".m2");

        for (File pom : FileUtils.listFiles(m2, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
            if (pom.getName().endsWith(".pom") == false) {
                continue;
            }
            if (pom.getName().equals(jar.getName().replaceAll(".jar", ".pom"))) {
                final String text = new Utf8Text(pom).read();
                save(text, pom.getName(), jarInfo);
            }
        }
    }

    /**
     * index.htmlを作成します。
     * @param title H1タグの出力するタイトルです。
     */
    private static void createIndexHtml(File dir, String title, List<File> jarInfos) {
        final File indexHtml = new File(dir, "index.html");

        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset=\"UTF-8\" /></head><body>");
        html.append("<h1>" + title + "</h1>");
        html.append("<ul>\n");
        for (File jarInfo : jarInfos) {
            html.append("<li>");
            html.append("<dl>");

            String pomText = "";
            File pomFile = null;
            for (File f : jarInfo.listFiles()) {
                if (f.getName().endsWith(".pom")) {
                    pomText = new Utf8Text(f).read();
                    pomFile = f;
                    break;
                }
            }
            Document pomDoc = Jsoup.parse(pomText);

            /* 名称 */
            String infoName = jarInfo.getName();
            {
                String name = pomDoc.select("project>name").text();
                if (StringUtils.contains(name, "${")) {
                    name = ""; // 変数が使用されていれば無効
                }
                if (StringUtils.isEmpty(name)) {
                    name = infoName;
                }

                html.append("<dt>" + name + "</dt>");
            }

            /* jarファイル名(pom.xmlへのリンク) */
            {
                String hrefPom = infoName + "/" + pomFile.getName();
                String jarName = pomFile.getName().replaceAll(".pom", ".jar");
                html.append(String.format("<dd>%s(<a href='%s' target='pom'>pom</a>)</dd>", jarName, hrefPom));
            }

            /* プロジェクトサイトへのリンク */
            {
                String url = pomDoc.select("project>url").html();

                // TODO 既存のファイルから解析できず。Googleで検索した結果を利用。
                if (StringUtils.equals(infoName, "zabuton")) {
                    url = "https://github.com/fujiyamakazan/zabuton";
                }
                if (StringUtils.equals(infoName, "kinchaku")) {
                    url = "https://github.com/fujiyamakazan/kinchaku";
                }
                if (StringUtils.equals(infoName, "animal-sniffer-annotations")) {
                    url = "https://www.mojohaus.org/animal-sniffer/animal-sniffer-annotations/";
                }
                if (StringUtils.equals(infoName, "byte-buddy")) {
                    url = "http://bytebuddy.net";
                }
                if (StringUtils.startsWith(infoName, "wicket-")) {
                    url = "https://wicket.apache.org";
                }
                if (StringUtils.startsWith(infoName, "wicketstuff-")) {
                    url = "http://wicketstuff.org";
                }
                if (StringUtils.startsWith(infoName, "guava-")) {
                    url = "https://github.com/google/guava";
                }
                if (StringUtils.startsWith(infoName, "kuromoji-")) {
                    url = "https://www.atilika.com/ja/kuromoji/";
                }
                if (StringUtils.startsWith(infoName, "logback-")) {
                    url = "https://logback.qos.ch";
                }
                if (StringUtils.startsWith(infoName, "log4j-")) {
                    url = "https://logging.apache.org/log4j/";
                }
                if (StringUtils.startsWith(infoName, "slf4j-")) {
                    url = "https://www.slf4j.org/";
                }
                if (StringUtils.equals(infoName, "okhttp")) {
                    url = "https://square.github.io/okhttp/";
                }
                if (StringUtils.equals(infoName, "okio")) {
                    url = "https://square.github.io/okio/";
                }
                if (StringUtils.equals(infoName, "error_prone_annotations")) {
                    url = "http://errorprone.info/";
                }
                if (StringUtils.equals(infoName, "cglib")) {
                    url = "https://github.com/cglib";
                }


                if (StringUtils.isEmpty(url)) {
                    LOGGER.error(infoName + "のURLが特定できません。");
                } else {
                    html.append(String.format("<dd><a href='%s' target='project'>%s</a></dd>", url, url));
                }
            }

            /* ライセンス判定 */
            File licenseFile = null;
            for (File file : jarInfo.listFiles()) {
                if (isLicenseFilename(file.getName())) {
                    if (licenseFile != null) {
                        throw new RuntimeException("ライセンスファイルが複数あります。");
                    }
                    licenseFile = file;
                }
            }

            String licenseText = "";
            if (licenseFile != null) {
                licenseText = new Utf8Text(licenseFile).read();

            } else {

                /* pom.xmlの <licenses> タグに入力されているライセンス記述を取得 */
                String tag = StringCutter.between(pomText, "<licenses>", "</licenses>");
                if (StringUtils.isNotEmpty(tag)) {
                    licenseText = tag;
                }

                if (StringUtils.isEmpty(licenseText)) {
                    /* pom.xmlのコメントとして書き込まれているライセンス記述を取得 */
                    String comment = StringCutter.between(pomText, "<!--", ">");
                    if (StringUtils.containsIgnoreCase(comment, "license")) {
                        licenseText = comment;
                    }
                }

                // TODO 既存のファイルから解析できず。別途調査した結果を利用。
                if (StringUtils.equals(infoName, "zabuton") || StringUtils.equals(infoName, "kinchaku")) {
                    licenseText = "https://www.apache.org/licenses/LICENSE-2.0.html";
                }
                if (StringUtils.equals(infoName, "byte-buddy")) {
                    licenseText = "https://www.apache.org/licenses/LICENSE-2.0.html";
                }
                if (StringUtils.equals(infoName, "wicketstuff-annotation")) {
                    licenseText = "http://www.apache.org/licenses/LICENSE-2.0.txt";
                }
                if (StringUtils.startsWith(infoName, "kuromoji-")) {
                    licenseText = "http://www.apache.org/licenses/LICENSE-2.0.txt";
                }
                if (StringUtils.startsWith(infoName, "logback-")) {
                    licenseText = "http://www.eclipse.org/legal/epl-v10.html, http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html ";
                }
                if (StringUtils.startsWith(infoName, "guava-")) {
                    licenseText = "http://www.apache.org/licenses/LICENSE-2.0.txt";
                }
                if (StringUtils.equals(infoName, "slf4j-api")) {
                    licenseText = "http://www.opensource.org/licenses/mit-license.php";
                }
                if (StringUtils.equals(infoName, "okhttp")
                    || StringUtils.equals(infoName, "okio")) {
                    licenseText = "http://www.apache.org/licenses/LICENSE-2.0.txt";
                }


            }

            /* ライセンスタイプの取得 */
            String licenseType = LicenseFileUtils.getType(licenseText);

            if (StringUtils.isEmpty(licenseType)) {
                LOGGER.error(infoName + "のライセンス区分が特定できません。");
            } else {
                licenseType = "(" + licenseType + ")";
            }

            if (licenseFile != null) {
                /* ファイルがあればリンクで表示 */
                String href = infoName + "/" + licenseFile.getName();

                html.append("<dd><a href='" + href + "' target='license' >" + licenseFile.getName() + "</a>"
                    + licenseType + "</dd>");

            } else {
                if (StringUtils.isNotEmpty(licenseText)) {
                    /* テキストしかなければ折りたたんで表示 */
                    html.append("<dd>"
                        + "<details><summary>ライセンス記述" + licenseType + "</summary>"
                        + licenseText
                        + "</details>"
                        + "</dd>");
                }
            }

            /*
             * NOTICEテキスト、READMEテキストがあれば表示
             */
            for (File file : jarInfo.listFiles()) {
                if (isNotice(file.getName()) == false && isReadMe(file.getName()) == false) {
                    continue;
                }
                String href = infoName + "/" + file.getName();
                html.append("<dd><a href='" + href + "' target='license' >" + file.getName() + "</a></dd>");
            }

            html.append("</dl>");
            html.append("</li>\n");
        }
        html.append("</ul>");
        html.append("</body></html>");

        String strHtml = html.toString();
        //System.out.println(strHtml);
        LOGGER.debug(indexHtml.getAbsolutePath() + " 出力");
        new Utf8Text(indexHtml).write(strHtml);
    }

    private static void save(String text, String fileName, File jarInfo) {
        File f = new File(jarInfo, fileName);
        try {
            FileUtils.write(f, text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isNotice(String fileName) {
        return StringUtils.startsWithIgnoreCase(fileName, FILENAME_NOTICE);
    }

    private static boolean isReadMe(String fileName) {
        return StringUtils.startsWithIgnoreCase(fileName, FILENAME_README)
            || StringUtils.startsWithIgnoreCase(fileName, "READ_ME");
    }

    private static boolean isLicenseFilename(String fileName) {
        return StringUtils.startsWithIgnoreCase(fileName, FILENAME_LICENSE);
    }

    /**
     * jdeps.exeを実行します。
     */
    public static Set<String> invokeJdeps(File jdk, File jar) {

        File jdeps = new File(jdk, "bin/jdeps.exe"); // ライブラリが使用するモジュールを解析するプログラム

        Set<String> modules = new HashSet<String>();

        String[] params = new String[] { jdeps.getAbsolutePath(), "--list-deps" };

        RuntimeExc runtimeExc = new RuntimeExc();
        String[] params1 = ArrayUtils.addAll(params, new String[] { jar.getAbsolutePath() });
        runtimeExc.exec(params1);

        String outText = runtimeExc.getOutText();
        outText = outText.trim();
        LOGGER.debug(jar.getName() + "-> " + outText);

        String err = runtimeExc.getErrText();
        err = err.trim();

        if (StringUtils.contains(outText, "エラー:")
            && StringUtils.contains(outText, "はマルチリリースjarファイルですが--multi-releaseオプションが設定されていません")) {

            runtimeExc = new RuntimeExc();
            String[] params2 = ArrayUtils.addAll(params,
                new String[] { "--multi-release", "9", jar.getAbsolutePath() });
            runtimeExc.exec(params2);
        }

        List<String> outs = runtimeExc.getOuts();
        for (String out : outs) {
            out = out.trim();
            if (StringUtils.isEmpty(out)) {
                continue;
            }
            if (StringUtils.startsWith(out, "JDK removed internal API/")) {
                continue;
            }
            modules.add(out);
        }
        LOGGER.debug(jar.getName() + "--> " + modules);
        return modules;
    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        File dirTarget = EnvUtils.getUserDesktop();
        File dirDependency = new File(dirTarget, "dependency");
        File dirDependencyInfo = new File(dirTarget, "dependency-info");
        List<File> dependencies = Arrays.asList(dirDependency.listFiles());
        String licenseListTitle = "test" + "が使用するライブラリ";

        DependencyInspector.scanJar(
            dependencies,
            dirDependencyInfo,
            licenseListTitle,
            null, null);

    }
}
