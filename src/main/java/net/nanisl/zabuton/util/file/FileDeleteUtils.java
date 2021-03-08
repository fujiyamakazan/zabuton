package net.nanisl.zabuton.util.file;

import java.io.File;
import java.io.Serializable;

public class FileDeleteUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	public static void delete(File file) {
		if (file.isDirectory()) {
			for (File sub: file.listFiles()) {
				delete(sub);
			}
		}
		if (file.delete() == false) {
			throw new RuntimeException("Delete Failure " + file.getAbsolutePath());
		}
	}
}
