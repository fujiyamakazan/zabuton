package com.github.fujiyamakazan.zabuton.runnable;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.github.fujiyamakazan.zabuton.util.string.SubstringUtils;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public class BuildXml implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Utf8Text buildXmlFile;

    public BuildXml(File file) {
        this.buildXmlFile = new Utf8Text(file);
    }

    public void rewriteDependency(File[] jars) {
        rewriteDependency(Arrays.asList(jars));
    }

    /**
     * Dependencyを記述します。
     */
    public void rewriteDependency(List<File> jars) {

        String text = this.buildXmlFile.read();

        String startTag = "<!-- dependency-start -->";
        String endTag = "<!-- dependency-end -->";

        String head = SubstringUtils.left(text, startTag);
        final String tail = SubstringUtils.right(text, endTag);

        StringBuilder sb = new StringBuilder();
        sb.append(head.trim() + "\n");
        sb.append(startTag + "\n");

        for (File jar : jars) {
            sb.append(
                "<zipfileset excludes=\"META-INF/*.SF\" src=\"${dir.dependency}/"
                    + jar.getName() + "\"/>\n");
        }

        sb.append(endTag + "\n");
        sb.append(tail.trim() + "\n");

        this.buildXmlFile.write(sb.toString());

    }

    /**
     * build.xmlを実行します。
     */
    public void exeBuildXml() {
        //      try {
        //
        //          Project project = new Project();
        //          project.init();
        //
        //          File buildFile = new File("build.xml");
        //          ProjectHelper.getProjectHelper().parse(project, buildFile);
        //          BuildLogger buildLogger = new DefaultLogger();
        //          buildLogger.setMessageOutputLevel(Project.MSG_INFO);
        //          buildLogger.setOutputPrintStream(new PrintStream(System.out));
        //          buildLogger.setErrorPrintStream(new PrintStream(System.err));
        //          buildLogger.setEmacsMode(false);
        //          project.addBuildListener(buildLogger); // loggerの設定
        //
        //          project.executeTarget(project.getDefaultTarget());
        //
        //      } catch (BuildException e) {
        //          throw new RuntimeException(e);
        //      }
    }

}
