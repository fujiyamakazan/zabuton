package com.github.fujiyamakazan.zabuton.runnable;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.github.fujiyamakazan.zabuton.util.StringBuilderLn;
import com.github.fujiyamakazan.zabuton.util.string.StringCutter;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

/**
 * Jarから取得した情報をもとにNOTICEファイルを生成します。
 * @author fujiyama
 */
public class NoticeMaker implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(NoticeMaker.class);

    /**
     * ファイルを生成します。
     */
    public static void make(File out, String title, List<File> jarInfos) {

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
                if (DependencyInspector.isLicenseFilename(file.getName())) {
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
                //                if (StringUtils.equals(infoName, "zabuton") || StringUtils.equals(infoName, "kinchaku")) {
                //                    licenseText = "https://www.apache.org/licenses/LICENSE-2.0.html";
                //                }
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
            String licenseType = getType(licenseText);

            if (StringUtils.isEmpty(licenseType)) {
                LOGGER.error(infoName + "のライセンス区分が特定できません。");
            } else {
                licenseType = "(" + licenseType + ")";
            }

            //            if (licenseFile != null) {
            //                /* ファイルがあればリンクで表示 */
            //                String href = infoName + "/" + licenseFile.getName();
            //
            //                html.append("<dd><a href='" + href + "' target='license' >" + licenseFile.getName() + "</a>"
            //                    + licenseType + "</dd>");
            //
            //            } else {
            if (StringUtils.isNotEmpty(licenseText)) {
                /* テキスト折りたたんで表示 */
                html.append("<dd>"
                    + "<details><summary>ライセンス記述" + licenseType + "</summary>"
                    + "<pre>" + licenseText + "</pre>"
                    + "</details>"
                    + "</dd>");
            }

            /*
             * NOTICEテキスト、READMEテキストがあれば表示
             */
            for (File file : jarInfo.listFiles()) {
                if (DependencyInspector.isNotice(file.getName()) == false
                    && DependencyInspector.isReadMe(file.getName()) == false) {
                    continue;
                }
                //String href = infoName + "/" + file.getName();
                //html.append("<dd><a href='" + href + "' target='license' >" + file.getName() + "</a></dd>");
                /* 折りたたんで表示 */
                html.append("<dd>"
                    + "<details><summary>" + file.getName() + "</summary>"
                    + "<pre>" + new Utf8Text(file).read() + "</pre>"
                    + "</details>"
                    + "</dd>");
            }

            html.append("</dl>");
            html.append("</li>\n");
        }
        html.append("</ul>");
        html.append("</body></html>");

        String strHtml = html.toString();
        //System.out.println(strHtml);
        LOGGER.debug(out.getAbsolutePath() + " 出力");
        new Utf8Text(out).write(strHtml);
    }

    /**
     * ライセンスの種類を判定します。
     */
    public static String getType(String licenseText) {

        StringBuilderLn sb = new StringBuilderLn(" および ");
        if (isApache20(licenseText)) {
            sb.appendLn("Apache 2.0");
        }

        if (isEpl1(licenseText)) {
            sb.appendLn("EPL 1.0");
        }

        if (isCddl1(licenseText)) {
            sb.appendLn("CDDL 1.0");
        }

        if (isMit(licenseText)) {
            sb.appendLn("MIT License");
        }

        if (isBsd3(licenseText)) {
            sb.appendLn("BSD-3");
        }

        if (isLgpl21(licenseText)) {
            sb.appendLn("LGPL 2.1");
        }

        return sb.toString();

    }

    private static boolean isApache20(String str) {

        return (StringUtils.contains(str, "http://www.apache.org/licenses/LICENSE-2.0.txt")
            || StringUtils.contains(str, "https://www.apache.org/licenses/LICENSE-2.0.txt")
            || StringUtils.contains(str, "http://www.apache.org/licenses/LICENSE-2.0")
            || StringUtils.contains(str, "https://www.apache.org/licenses/LICENSE-2.0"));

    }

    private static boolean isEpl1(String str) {
        return StringUtils.contains(str, "Eclipse Public License - v 1.0")
            || StringUtils.contains(str, "epl-v10");
    }

    private static boolean isCddl1(String str) {
        return StringUtils.contains(str, "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0");
    }

    private static boolean isMit(String str) {
        return StringUtils.contains(str, "The MIT License") || StringUtils.contains(str, "mit-license");
    }

    private static boolean isBsd3(String str) {
        return StringUtils.contains(str, "BSD-3-Clause");
    }

    private static boolean isLgpl21(String str) {
        return StringUtils.contains(str, "lgpl-2.1");
    }
}
