package com.github.fujiyamakazan.zabuton.util.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * リソースに関わる処理の集約.
 * @author fujiyama
 */
public class ResourceUtils {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ResourceUtils.class);

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * クラスパスにあるリソースをUTF-8の文字列として取得します。
     * @param path リソースのパス
     * @return 取得した文字列
     */
    public static String getAsUtf8Text(String path) {
        return getAsUtf8Text(path, null);
    }

    /**
     * [base]と同じ名前のリソースをUTF-8の文字列として取得します。
     * 拡張子を指定します。
     * @param base リソースを検索する基点
     * @param extension 拡張子（例：".txt"）
     * @return 取得した文字列
     */
    public static String getAsUtf8Text(Class<?> base, String extension) {
        return getAsUtf8Text(base.getSimpleName() + extension, base);
    }

    /**
     * [base]と同階層にあるリソースをUTF-8の文字列として取得します。
     * @param path リソースのパス
     * @param base リソースを検索する基点
     * @return 取得した文字列
     */
    public static String getAsUtf8Text(String path, Class<?> base) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            if (base != null) {
                inputStream = base.getResourceAsStream(path);
            } else {
                inputStream = classLoader.getResourceAsStream(path);
            }
            if (inputStream == null) {
                throw new RuntimeException(path + " not found.");
            }
            reader = new BufferedReader(new InputStreamReader(inputStream, CHARSET));
//            StringBuilder sb = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                sb.append(line);
//            }
//            return sb.toString();
            return IOUtils.toString(reader);

        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * リソースからクラスを取得します。
     * Jarにパッケージされているクラスも取得します。
     * @param packageName パッケージ名
     * @return 抽出したクラス
     */
    public static List<Class<?>> findClasses(String packageName) {
        packageName = packageName.replaceAll(Pattern.quote("."), "/");
        if (StringUtils.endsWith(packageName, "/") == false) {
            packageName += "/";
        }

        ClassLoader classLoader = ResourceUtils.class.getClassLoader();
        URL url = classLoader.getResource(packageName);
        if (url == null) {
            throw new IllegalArgumentException(packageName);
        }

        if (url.getProtocol().equals("jar")) {

            String[] aryPath = url.getPath().split(":");
            String path = aryPath[aryPath.length - 1].split("!")[0];
            path = URLDecoder.decode(path, CHARSET); // パスに日本語が含まれるとURLエンコードされるため

            return findClassesJar(packageName, new File(path));

        } else {

            return findClassesDir(packageName, new File(url.getFile()));
        }
    }

    private static List<Class<?>> findClassesJar(String packageName, File jar) {
        try (JarFile jarFile = new JarFile(jar)) {
            List<Class<?>> result = Generics.newArrayList();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name != null && name.startsWith(packageName)) {
                    String fullName = name;
                    fullName = fullName.replaceAll("/", ".");
                    if (fullName.endsWith(".class")) {
                        String fullNameWithoutExtension = fullName.substring(0,
                            fullName.length() - ".class".length());
                        try {
                            result.add(Class.forName(fullNameWithoutExtension));
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            return result;
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }

    private static List<Class<?>> findClassesDir(String packageName, File dir) {
        packageName = packageName.replaceAll("/", ".");
        if (packageName.endsWith(".")) {
            packageName = packageName.substring(0, packageName.length() - 1);
        }
        ClassLoader classLoader = ResourceUtils.class.getClassLoader();
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (String path : dir.list()) {
            File entry = new File(dir, path);
            if (entry.isFile() && entry.getName().endsWith(".class")) {
                try {
                    String name = entry.getName();
                    classes.add(classLoader.loadClass(packageName + "."
                        + name.substring(0, name.length() - ".class".length())));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else if (entry.isDirectory()) {
                classes.addAll(findClassesDir(packageName + "." + entry.getName(), entry));
            }
        }
        return classes;
    }

    //    public static void main(String[] args) {
    //        /*
    //         * テキストの取得
    //         */
    //        String resourceAtRoot = getAsUtf8Text("logback.xml");
    //        log.debug(resourceAtRoot);
    //        String resourceAtJar = getAsUtf8Text("org/apache/wicket/extensions/ajax/markup/html/AjaxEditableLabel.html");
    //        log.debug(resourceAtJar);
    //        String siblings = getAsUtf8Text("ResourceUtils.txt", ResourceUtils.class);
    //        log.debug(siblings);
    //        String siblingsText = getAsUtf8Text(ResourceUtils.class, ".txt");
    //        log.debug(siblingsText);
    //
    //        /*
    //         * クラスの取得
    //         */
    //        List<Class<?>> clz = findClasses("com.github.fujiyamakazan.zabuton.util.resource");
    //        for (Class<?> c: clz) {
    //            log.debug(c.getName());
    //        }
    //        clz = findClasses("org.apache.wicket.extensions.ajax.markup.html");
    //        for (Class<?> c: clz) {
    //            log.debug(c.getName());
    //        }
    //    }

}