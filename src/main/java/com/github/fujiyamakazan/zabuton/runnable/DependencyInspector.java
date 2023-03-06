package com.github.fujiyamakazan.zabuton.runnable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
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
@SuppressWarnings("deprecation")
public class DependencyInspector {
    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyInspector.class);

    /** ライブラリが使用するJREのモジュールを記録するファイルの名前。 */
    public static final String JDEPS_TXT = "jdeps.txt";

    private static final String FILENAME_LICENSE = "LICENSE";
    private static final String FILENAME_NOTICE = "NOTICE";
    private static final String FILENAME_README = "README";

    /**
     * jarをスキャンし以下を行います。
     * ・ライセンス関連ファイルの取得
     * ・バージョン情報の取得
     * ・依存するJREのモジュールの情報を取得
     */
    public static List<File> scanJar(
        List<File> jars,
        File dirOut,
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
                    Utf8Text jreModulesFile = new Utf8Text(new File(jarInfo, JDEPS_TXT));
                    jreModulesFile.writeLines(new ArrayList<String>(modules));
                }

                /* index.htmlの作成に必要なファイルを収集する */
                getInfoFiles(jar, jarInfo);

            }
        }
        return jarInfos;
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



    private static void save(String text, String fileName, File jarInfo) {
        File f = new File(jarInfo, fileName);
        try {
            FileUtils.write(f, text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isNotice(String fileName) {
        return StringUtils.startsWithIgnoreCase(fileName, FILENAME_NOTICE);
    }

    public static boolean isReadMe(String fileName) {
        return StringUtils.startsWithIgnoreCase(fileName, FILENAME_README)
            || StringUtils.startsWithIgnoreCase(fileName, "READ_ME");
    }

    public static boolean isLicenseFilename(String fileName) {
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


}
