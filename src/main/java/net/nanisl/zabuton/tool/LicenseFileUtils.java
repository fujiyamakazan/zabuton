package net.nanisl.zabuton.tool;

import org.apache.commons.lang3.StringUtils;

import net.nanisl.zabuton.file.Utf8FileObj;

/**
 * TODO 不要になった処理の除去
 *
 * @author fujiyama
 */
public class LicenseFileUtils {

	public static boolean isNoteFileName(String fileName) {
		return StringUtils.startsWithIgnoreCase(fileName, "NOTICE")
								|| StringUtils.startsWithIgnoreCase(fileName, "README")
								|| StringUtils.startsWithIgnoreCase(fileName, "READ_ME");
	}

	public static boolean isLicenseFilename(String fileName) {
		return StringUtils.startsWithIgnoreCase(fileName, "LICENSE");
	}

	public static boolean isApache2(Utf8FileObj file) {
		boolean nextVerCheck = false;
		for (String line: file.readLines()) {
			line = line.trim();
			if (StringUtils.equals(line, "Apache License")) {
				nextVerCheck = true;
				continue;
			}
			if (nextVerCheck) {
				if (StringUtils.startsWith(line, "Version 2.0,")) {
					return true;
				}
				nextVerCheck = false;
			}
		}
		return false;
	}
	public static  boolean isEpl1(Utf8FileObj file) {
		for (String line: file.readLines()) {
			if (StringUtils.equals(line, "Eclipse Public License - v 1.0")) {
				return true;
			}
		}
		return false;
	}
	public static  boolean isCddl1(Utf8FileObj file) {
		for (String line: file.readLines()) {
			if (StringUtils.equals(line, "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0")) {
				return true;
			}
		}
		return false;
	}
}
