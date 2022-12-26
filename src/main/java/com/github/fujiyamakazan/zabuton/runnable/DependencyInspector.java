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

    public static boolean debug = false;

    /**
     * jarをスキャンし以下を行います。
     * ・ライセンス関連ファイルの取得
     * ・バージョン情報の取得
     * ・依存するJREのモジュールの情報を取得
     *
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

            //log.debug("■" + jarName);

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
            boolean searchlicense = false;
            if (jarInfo.exists() == false) {
                searchlicense = true;
                jarInfo.mkdir();

                /* バージョンを記録 */
                verFile.write(version);

                /* 依存するJREのモジュールを検査する */
                Set<String> modules = invokeJdeps(jdk, jar);
                Utf8Text jreModulesFile = new Utf8Text(new File(jarInfo, jdepsTxt));
                jreModulesFile.writeLines(new ArrayList<String>(modules));
            }

            if (searchlicense || debug) {
                if (hasLicenseFile(jarInfo) == false) {
                    LOGGER.debug("■" + jarInfo.getName() + "(実体からライセンス取得)");

                    new UnzipTask(jar) {
                        private static final long serialVersionUID = 1L;

                        boolean exist = false;

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

                            if (exist) {
                                return;
                            }

                            String fileName = name;
                            if (StringUtils.contains(name, "/")) {
                                fileName = name.substring(name.indexOf('/') + 1);
                            }

                            /*
                             * 「LICENSE」テキストが存在するケース
                             */
                            if (LicenseFileUtils.isLicenseFilename(fileName)) {
                                final String text = new Utf8Text(file).read();
                                save(text, fileName, jarInfo);
                                exist = true;
                                return;
                            }

                            /*
                             * MANIFEST.MFに宣言されているケース
                             * 例）asm
                             */
                            if (StringUtils.endsWith(name, "MANIFEST.MF")) {
                                for (String line : new Utf8Text(file).readLines()) {
                                    if (line.startsWith("Bundle-License")) {
                                        save(line, "LICENSE_IN_MANIFEST", jarInfo);
                                        exist = true;
                                        return;
                                    }
                                }
                            }
                        }
                    }.start();
                }

                if (hasLicenseFile(jarInfo) == false) {
                    LOGGER.debug("■" + jarInfo.getName() + "(m2からライセンス情報取得)");
                    /*
                     * jarファイル名から.m2を探索して jarファイル名.pomを引き当てる。
                     * その中に <licenses> で書かれたライセンス記述を引き当てる。
                     */
                    File m2 = new File(EnvUtils.getUserProfile(), ".m2");

                    for (File pom : FileUtils.listFiles(m2, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
                        if (pom.getName().endsWith(".pom") == false) {
                            continue;
                        }
                        if (pom.getName().equals(jar.getName().replaceAll(".jar", ".pom"))) {
                            LOGGER.debug(pom.getAbsolutePath());

                            try {
                                String text = new Utf8Text(pom).read();
                                String licenses = StringCutter.between(text, "<licenses>", "</licenses>");
                                if (StringUtils.isNotEmpty(licenses)) {
                                    /*
                                     * licensesタグで書き込まれているケース
                                     * 例）curvesapi
                                     */
                                    File f = new File(jarInfo, "LICENSE_IN_MAVEN_LICENSES");
                                    FileUtils.write(f, licenses, StandardCharsets.UTF_8);

                                } else {
                                    String comment = StringCutter.between(text, "<!--", ">");
                                    if (comment != null) {
                                        comment = comment.trim();
                                        if (comment.startsWith("The MIT License")
                                            || comment.startsWith(
                                                "Licensed to the Apache Software Foundation (ASF) under one or more")) {
                                            /*
                                             * コメントとして書き込まれているケース
                                             * 例）animal-sniffer-annotations
                                             */
                                            save(comment, "LICENSE_IN_MAVEN_COMMENT", jarInfo);
                                        }
                                    }
                                }

                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        }
                    }
                }

                if (hasNoticeFile(jarInfo) == false) {
                    LOGGER.debug("■" + jarInfo.getName() + "(実体からNotice関連ファイルを取得)");

                    new UnzipTask(jar) {
                        private static final long serialVersionUID = 1L;

                        boolean exist = false;

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
                            if (exist) {
                                return;
                            }
                            String fileName = name;
                            if (StringUtils.contains(name, "/")) {
                                fileName = name.substring(name.indexOf('/') + 1);
                            }

                            /*
                             * 「LICENSE」テキストが存在するケース
                             */
                            if (isNotice(fileName)) {
                                final String text = new Utf8Text(file).read();
                                save(text, fileName, jarInfo);
                                exist = true;
                                return;
                            }
                        }
                    }.start();
                }

                if (hasCopyright(jarInfo) == false) {
                    LOGGER.debug("■" + jarInfo.getName() + "m2からコピーライトを取得)");

                    /*
                     * jarファイル名から.m2を探索して jarファイル名.pomを引き当てる。
                     * その中に <licenses> で書かれたライセンス記述を引き当てる。
                     */
                    File m2 = new File(EnvUtils.getUserProfile(), ".m2");

                    for (File pom : FileUtils.listFiles(m2, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {
                        if (pom.getName().endsWith(".pom") == false) {
                            continue;
                        }
                        if (pom.getName().equals(jar.getName().replaceAll(".jar", ".pom"))) {
                            LOGGER.debug(pom.getAbsolutePath());

                            String xml = new Utf8Text(pom).read();
                            Document doc = Jsoup.parse(xml);
                            String url = doc.select("project>url").html();
                            String str = "";
                            str += doc.select("project>name").html();
                            str += "(";
                            if (StringUtils.isNotEmpty(url)) {
                                str += String.format("<a href='%s' target='_blank'>", url);
                            }
                            str += doc.select("project>groupId").html() + " " + doc.select("project>artifactId").html();
                            if (StringUtils.isNotEmpty(url)) {
                                str += "</a>";
                            }
                            str += ")";
                            save(str, "COPYRIGHT", jarInfo);

                            break;
                        }
                    }
                }

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
                String href = jarInfo.getName() + "/" + fileName;

                if (LicenseFileUtils.isLicenseFilename(fileName)) {

                    // TODO デュアル のチェックの確実性が低い

                    Utf8Text f = new Utf8Text(file);
                    String license;
                    if (LicenseFileUtils.isApache2(f) && LicenseFileUtils.isEpl1(f)) {
                        license = "Apache License Version 2.0 と Eclipse Public License v1.0 のデュアルライセンス";

                    } else if (LicenseFileUtils.isApache2(f)) {
                        license = "Apache License Version 2.0";

                    } else if (LicenseFileUtils.isCddl1(f)) {
                        license = "CDDL Version 1.0";

                    } else if (LicenseFileUtils.isMit(f)) {
                        license = "MIT License";

                    } else if (LicenseFileUtils.isBsd3(f)) {
                        license = "3 clause BSD license";

                    } else {
                        license = "";
                    }

                    if (StringUtils.isNotEmpty(license)) {
                        license = "(" + license + ")";
                    }
                    html.append(
                        "<dd><a href='" + href + "' target='license' >" + fileName + "</a>" + license + "</dd>");

                } else if (isNotice(fileName)) {
                    html.append(
                        "<dd><a href='" + href + "' target='notice' >" + fileName + "</a></dd>");

                } else if (isCopyright(fileName)) {
                    Utf8Text f = new Utf8Text(file);
                    html.append("<dd>" + f.read() + "</dd>");
                }
            }

            html.append("</dt>");
            html.append("</dl>");
            html.append("</li>\n");
        }
        html.append("</ul>");
        html.append("</body></html>");

        String strHtml = html.toString();
        //System.out.println(strHtml);
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

    private static boolean hasLicenseFile(final File jarInfo) {
        boolean hasLicence = false;
        for (File file : jarInfo.listFiles()) {
            String fileName = file.getName();
            if (LicenseFileUtils.isLicenseFilename(fileName)) {
                hasLicence = true;
            }
        }
        return hasLicence;
    }

    private static boolean hasNoticeFile(final File jarInfo) {
        boolean has = false;
        for (File file : jarInfo.listFiles()) {
            String fileName = file.getName();
            if (isNotice(fileName)) {
                has = true;
            }
        }
        return has;
    }

    private static boolean hasCopyright(final File jarInfo) {
        boolean has = false;
        for (File file : jarInfo.listFiles()) {
            String fileName = file.getName();
            if (isCopyright(fileName)) {
                has = true;
            }
        }
        return has;
    }

    private static boolean isNotice(String fileName) {
        return StringUtils.startsWithIgnoreCase(fileName, "NOTICE")
            || StringUtils.startsWithIgnoreCase(fileName, "README")
            || StringUtils.startsWithIgnoreCase(fileName, "READ_ME");
    }

    private static boolean isCopyright(String fileName) {
        return StringUtils.startsWithIgnoreCase(fileName, "COPYRIGHT");
    }

    //    /**
    //     * ライセンス関連ファイルを取得します。
    //     */
    //    private static void scanTextInJar(File jar, final String jarName, final File jarInfo) {
    //        ZipUtils.unzip(jar, new UnzipTask() {
    //            private static final long serialVersionUID = 1L;
    //
    //            @Override
    //            public void run(String entryName, File unZipFile) throws IOException {
    //
    //                if (StringUtils.startsWith(entryName, "META-INF/") == false) {
    //                    return;
    //                }
    //
    //                if (StringUtils.contains(entryName, "/")) {
    //                    entryName = StringCutter.right(entryName, "/");
    //                }
    //                if (StringUtils.isEmpty(entryName)) {
    //                    return;
    //                }
    //                if (StringUtils.equals(entryName, "MANIFEST.MF")
    //                    || StringUtils.equals(entryName, "DEPENDENCIES")
    //                    || StringUtils.equals(entryName, "INDEX.LIST")
    //                    || StringUtils.endsWith(entryName, ".class")
    //                    || StringUtils.endsWith(entryName, ".xml")
    //                    || StringUtils.endsWith(entryName, ".properties")
    //                    || StringUtils.endsWith(entryName, ".xsd")
    //                    || StringUtils.endsWith(entryName, ".json")) {
    //                    return;
    //                }
    //
    //                if (LicenseFileUtils.isLicenseFilename(entryName)
    //                    || LicenseFileUtils.isNoteFileName(entryName)) {
    //
    //                    /* 保存 */
    //                    File f = new File(jarInfo, entryName);
    //                    FileUtils.copyFile(unZipFile, f);
    //
    //                } else {
    //                    log.warn("skip " + entryName + " in " + jarName);
    //                }
    //            }
    //        });
    //    }

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

        File dirTarget = new File("target");
        String absolutePath = dirTarget.getAbsolutePath();
        absolutePath = absolutePath.replaceAll("zabuton", "chabudai");
        dirTarget = new File(absolutePath);
        File dirDependency = new File(dirTarget, "dependency");
        File dirDependencyInfo = new File(dirTarget, "dependency-info");
        List<File> dependencies = Arrays.asList(dirDependency.listFiles());
        String licenseListTitle = "chabudai" + "が使用するライブラリ";

        debug = true;
        DependencyInspector.scanJar(
            dependencies,
            dirDependencyInfo,
            licenseListTitle,
            null, null);

    }
}
