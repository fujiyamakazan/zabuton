package net.nanisl.zabuton.tool;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.nanisl.zabuton.file.Utf8File;
import net.nanisl.zabuton.util.file.ZipUtils;
import net.nanisl.zabuton.util.file.ZipUtils.UnzipTask;
import net.nanisl.zabuton.util.string.StringCutter;

public class LicenseChecker {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(LicenseChecker.class);

	private static final List<String> linesceNames = Arrays.asList(new String[] {
			"LICENSE.txt",
			"license.txt",
			"LICENSE",
	});

	private static final List<String> noticeNames = Arrays.asList(new String[] {
			"NOTICE.txt",
			"notice.txt",
			"NOTICE"
	});

	private File dirLib;
	private File dirLicense;

	public static void main(String[] args) {
		new LicenseChecker().execute();
	}

	private void execute() {
		if (StringUtils.containsIgnoreCase(System.getProperty("os.name"), "win") == false) {
			throw new RuntimeException("Windows only.");
		}
		File desktopDir = new File(System.getProperty("user.home"), "Desktop");
		this.dirLib = new File(desktopDir, "lib");

		/* Jarファイルをスキャンしてライセンス情報を抽出する */
		this.dirLicense = new File(dirLib.getParent(), dirLib.getName() + "_license");
		List<String> messagesScan = scanJar(dirLib);

		/* ライセンスの判定 */
		List<String> messagesCheckType = checkType();

		/* ファイル出力 */
		File fileLicenseAll = new File(dirLicense, "ZABUTON_LICENSE.txt");
		List<String> messages = Generics.newArrayList();
		messages.addAll(messagesScan);
		messages.addAll(messagesCheckType);
		try {
			FileUtils.writeLines(fileLicenseAll, messages);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<String> scanJar(File dirLib) {

		List<String> messages = Generics.newArrayList();

		for (File jar : dirLib.listFiles()) {

			final String jarFullName = jar.getName(); // ex. commons-collections4-4.4.jar
			if (StringUtils.endsWith(jarFullName, ".jar") == false) {
				continue;
			}

			String jarName = StringCutter.left(jarFullName, ".jar"); // ex. commons-collections4-4.4

			File dirLicenseJar = new File(dirLicense, jarName);
			dirLicenseJar.mkdirs();

			final Model<File> fileLicense =new Model<File>();
			final Model<File> fileNotice =new Model<File>();

			ZipUtils.unzip(jar, new UnzipTask() {
				private static final long serialVersionUID = 1L;

				@Override
				public void run(String entryName, File unZipFile) throws IOException {

					if (StringUtils.startsWith(entryName, "META-INF/") == false) {
						return;
					}

					if (StringUtils.contains(entryName, "/")) {
						entryName = StringCutter.right(entryName, "/");
					}

//					if (StringUtils.endsWith(entryName, ".class")
//							|| StringUtils.endsWith(entryName, ".properites")
//							|| StringUtils.endsWith(entryName, ".xml")) {
//						return;
//					}

					if (linesceNames.contains(entryName)) {
						File file = new File(dirLicenseJar, entryName);
						fileLicense.setObject(file);
						FileUtils.copyFile(unZipFile, file);
					} else {
						if (StringUtils.containsIgnoreCase(entryName, "license")) {
							throw new RuntimeException(jarName + "#" + entryName + "はLICENSEファイルの可能性があります。");
						}
					}

					if (noticeNames.contains(entryName)) {
						File file = new File(dirLicenseJar, entryName);
						fileNotice.setObject(file);
						FileUtils.copyFile(unZipFile, file);
					} else {
						if (StringUtils.containsIgnoreCase(entryName, "notice")) {
							throw new RuntimeException(jarName + "#" + entryName + "はNOTICEファイルの可能性があります。");
						}
					}
				}
			});

			String message = jarName;
			if (fileLicense.getObject() == null) {
				message += " LICENSEファイルが検出できません。";
			} else {
				message += " LICENSEファイル:" + fileLicense.getObject().getName();
			}
			if (fileNotice.getObject() == null) {
				message += " NOTICEファイルが検出できません。";
			} else {
				message += " NOTICEファイル:" + fileNotice.getObject().getName();
			}
			messages.add(message);
		}
		return messages;
	}

	private List<String> checkType() {
		List<String> messages = Generics.newArrayList();

		for (File fileFlisence: FileUtils.listFiles(dirLicense, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)) {

			String name = fileFlisence.getName();

			String jarName = fileFlisence.getAbsolutePath();
			jarName = jarName.replaceAll(Pattern.quote(dirLicense.getAbsolutePath()), "");
			for (String lisenceName: linesceNames) {
				jarName = jarName.replaceAll(Pattern.quote(lisenceName), "");
			}
			jarName = jarName.replaceAll(Pattern.quote("\\"), "");

			if (linesceNames.contains(name)) {

				Utf8File file = Utf8File.of(fileFlisence);

				boolean isApache20 = isApache2(file);
				boolean isEpl1 = isEpl1(file);
				boolean isCddl = isCddl1(file);

				if (isApache20 && isEpl1) {
					messages.add(jarName + "は" + "Apache License Version 2.0 と Eclipse Public License v1.0 のデュアルライセンスです。");
				} else if (isApache20) {
					messages.add(jarName + "は" + "Apache License Version 2.0 です。");
				} else if (isCddl) {
					messages.add(jarName + "は" + "CDDL Version 1.0 です。");
				} else {
					throw new RuntimeException("未知のパターン:" + name);
				}

			}
		}
		return messages;
	}

	private boolean isApache2(Utf8File file) {
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
	private boolean isEpl1(Utf8File file) {
		for (String line: file.readLines()) {
			if (StringUtils.equals(line, "Eclipse Public License - v 1.0")) {
				return true;
			}
		}
		return false;
	}
	private boolean isCddl1(Utf8File file) {
		for (String line: file.readLines()) {
			if (StringUtils.equals(line, "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (CDDL) Version 1.0")) {
				return true;
			}
		}
		return false;
	}
}
