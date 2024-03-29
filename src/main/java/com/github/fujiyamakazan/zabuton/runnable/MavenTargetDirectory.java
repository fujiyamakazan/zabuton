package com.github.fujiyamakazan.zabuton.runnable;

import java.io.File;
import java.io.Serializable;

public class MavenTargetDirectory implements Serializable {
    private static final long serialVersionUID = 1L;

    private final File target;

    private File dependency;
    private File result;
    private File dependencyInfo;
    private File jre;

    public MavenTargetDirectory(File target) {
        this.target = target;
    }

    public void setLibraryDirName(String name) {
        this.dependency = new File(this.target, name);
    }

    public File getDependency() {
        return this.dependency;
    }

    public void setLibraryInfoDirName(String name) {
        this.dependencyInfo = new File(this.target, name);
    }

    public File getDependencyInfo() {
        return this.dependencyInfo;
    }

    public void setJreDirName(String name) {
        this.jre = new File(this.target, name);
    }

    public File getJre() {
        return this.jre;
    }

    public void setReultName(String name) {
        this.result = new File(this.target, name);
    }

    public File getResult() {
        return this.result;
    }

}
