package com.github.fujiyamakazan.zabuton.runnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.fujiyamakazan.zabuton.util.exec.RuntimeExc;
import com.github.fujiyamakazan.zabuton.util.file.FileDeleteUtils;
import com.github.fujiyamakazan.zabuton.util.file.ZipUtils;
import com.github.fujiyamakazan.zabuton.util.file.ZipUtils.UnzipTask;
import com.github.fujiyamakazan.zabuton.util.string.SubstringUtils;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

/**
 * 依存するライブラリ（Jar）の解析
 *
 * @author fujiyama
 */
public class DependencyInspector {
    private static final Logger log = LoggerFactory.getLogger(DependencyInspector.class);

    /**
     * jarをスキャンし以下を行う
     *
     * ・ライセンス関連ファイルの取得
     * ・バージョン情報の取得
     * ・依存するJREのモジュールの情報を取得
     * @param jdepsTxt
     * @param jdk
     *
     */
    public static void scanJar(List<File> jars, File dirOut, String licenseListTitle, String jdepsTxt, File jdk) {

        List<File> jarInfos = Generics.newArrayList();

        for (File jar : jars) {

            final String jarFileName = jar.getName(); // ex. commons-collections4-4.4.jar
            if (StringUtils.endsWith(jarFileName, ".jar") == false) {
                continue;
            }

            final String jarName = SubstringUtils.left(jarFileName, ".jar"); // ex.) commons-collections4-4.4

            //log.debug("■" + jarName);

            String artifactIdAndVersion = jarName;
            if (artifactIdAndVersion.endsWith("-SNAPSHOT")) {
                artifactIdAndVersion = SubstringUtils.left(artifactIdAndVersion, "-SNAPSHOT");
            }
            final String artifactId = SubstringUtils.leftOfLast(artifactIdAndVersion, "-"); // ex.) commons-collections4
            final String version = SubstringUtils.right(artifactIdAndVersion, "-"); // ex.) 4.4

            //			log.debug("jarName:" + jarName);
            //			log.debug("artifactId:" + artifactId);
            //			log.debug("version:" + version);

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

                /* ライセンス関連ファイルを取得する */
                scanTextInJar(jar, jarName, jarInfo);

                /* 依存するJREのモジュールを検査する */
                Set<String> modules = invokeJdeps(jdk, jar);
                Utf8Text jreModulesFile = new Utf8Text(new File(jarInfo, jdepsTxt));
                jreModulesFile.writeLines(new ArrayList<String>(modules));

            }
        }

        /* index.htmlを作成する */
        final File indexHtml = new File(dirOut, "index.html");
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset=\"UTF-8\" /></head><body>");
        html.append("<h1>" + licenseListTitle + "</h1>");
        html.append("<ul>\n");
        for (File jarInfo : jarInfos) {
            html.append("<li>");
            html.append("<dl>");
            html.append("<dt>" + jarInfo.getName() + "</dt>");

            for (File file : jarInfo.listFiles()) {

                String fileName = file.getName();

                if (LicenseFileUtils.isLicenseFilename(fileName)) {

                    Utf8Text f = new Utf8Text(file);
                    boolean isApache20 = LicenseFileUtils.isApache2(f);
                    boolean isEpl1 = LicenseFileUtils.isEpl1(f);
                    boolean isCddl = LicenseFileUtils.isCddl1(f);
                    String msg;
                    if (isApache20 && isEpl1) {
                        msg = "Apache License Version 2.0 と Eclipse Public License v1.0 のデュアルライセンス";
                    } else if (isApache20) {
                        msg = "Apache License Version 2.0";
                    } else if (isCddl) {
                        msg = "CDDL Version 1.0";
                    } else {
                        //throw new RuntimeException("未知のパターン:" + name);
                        msg = "";
                    }

                    String href = jarInfo.getName() + "/" + fileName;
                    html.append("<dd><a href='" + href + "'>" + fileName + "</a>(" + msg + ")</dd>");
                }

                if (LicenseFileUtils.isNoteFileName(fileName)) {
                    String href = jarInfo.getName() + "/" + fileName;
                    html.append("<dd><a href='" + href + "'>" + fileName + "</a></dd>");
                }
            }

            html.append("</dt>");
            html.append("</dl>");
            html.append("</li>\n");
        }
        html.append("</ul>");
        html.append("</body></html>");
        new Utf8Text(indexHtml).write(html.toString());
    }

    /**
     * ライセンス関連ファイルを取得する
     */
    private static void scanTextInJar(File jar, final String jarName, final File jarInfo) {
        ZipUtils.unzip(jar, new UnzipTask() {
            private static final long serialVersionUID = 1L;

            @Override
            public void run(String entryName, File unZipFile) throws IOException {

                if (StringUtils.startsWith(entryName, "META-INF/") == false) {
                    return;
                }

                if (StringUtils.contains(entryName, "/")) {
                    entryName = SubstringUtils.right(entryName, "/");
                }
                if (StringUtils.isEmpty(entryName)) {
                    return;
                }
                if (StringUtils.equals(entryName, "MANIFEST.MF")
                    || StringUtils.equals(entryName, "DEPENDENCIES")
                    || StringUtils.equals(entryName, "INDEX.LIST")
                    || StringUtils.endsWith(entryName, ".class")
                    || StringUtils.endsWith(entryName, ".xml")
                    || StringUtils.endsWith(entryName, ".properties")
                    || StringUtils.endsWith(entryName, ".xsd")
                    || StringUtils.endsWith(entryName, ".json")) {
                    return;
                }

                if (LicenseFileUtils.isLicenseFilename(entryName)
                    || LicenseFileUtils.isNoteFileName(entryName)) {

                    /* 保存 */
                    File f = new File(jarInfo, entryName);
                    FileUtils.copyFile(unZipFile, f);

                } else {
                    log.warn("skip " + entryName + " in " + jarName);
                }
            }
        });
    }

    public static Set<String> invokeJdeps(File jdk, File jar) {

        File jdeps = new File(jdk, "bin/jdeps.exe"); // ライブラリが使用するモジュールを解析するプログラム

        Set<String> modules = new HashSet<String>();

        String[] params = new String[] { jdeps.getAbsolutePath(), "--list-deps" };

        RuntimeExc runtimeExc = new RuntimeExc();
        String[] params1 = ArrayUtils.addAll(params, new String[] { jar.getAbsolutePath() });
        runtimeExc.exec(params1);

        String outText = runtimeExc.getOutText();
        outText = outText.trim();
        log.debug(jar.getName() + "-> " + outText);

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
        log.debug(jar.getName() + "--> " + modules);
        return modules;
    }
}
