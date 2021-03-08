package net.nanisl.zabuton.tool;

import java.io.File;
import java.io.Serializable;

public class MavenTargetDirectory implements Serializable {
	private static final long serialVersionUID = 1L;

	final private File target;

	private File dependency;
	private File result;
	private File dependencyInfo;
	private File jre;

	public MavenTargetDirectory(File target) {
		this.target = target;
	}

	public void setLibraryDirName(String name) {
		this.dependency = new File(target, name);
	}
	public File getDependency() {
		return dependency;
	}

	public void setLibraryInfoDirName(String name) {
		this.dependencyInfo = new File(target, name);
	}
	public File getDependencyInfo() {
		return dependencyInfo;
	}

	public void setJreDirName(String name) {
		this.jre = new File(target, name);
	}

	public File getJre() {
		return jre;
	}

	public void setReultName(String name) {
		this.result = new File(target, name);
	}

	public File getResult() {
		return result;
	}


}
