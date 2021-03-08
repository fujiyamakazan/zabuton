package net.nanisl.zabuton.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;

import net.nanisl.zabuton.file.Utf8FileObj;
import net.nanisl.zabuton.util.ListToStringer;
import net.nanisl.zabuton.util.file.FileDeleteUtils;

public class RunnableJarBuilder {

	/** ライブラリが使用するJREのモジュールを記録するファイルの名前 */
	private static final String JDEPS_TXT = "jdeps.txt";

	final private String appName;

	public RunnableJarBuilder(String appName) {
		this.appName = appName;
	}

	protected void build(String jdkPath) {
		File jdk = new File(jdkPath);

		MavenTargetDirectory dirTarget = new MavenTargetDirectory(new File("target"));
		dirTarget.setLibraryDirName("dependency"); // ライブラリ(Jar)
		dirTarget.setReultName(appName); // 最終成果物の保存先
		dirTarget.setLibraryInfoDirName(appName + "/License"); // ライブラリ情報(ライセンス情報など)
		dirTarget.setJreDirName(appName + "/jre-min"); // ランタイム

		/* ライブラリをスキャンして情報を取得する */
		inspectLibrary(dirTarget, jdk);

		/* JREを生成する */
		createJre(dirTarget, jdk);

		/* build.xmlのzipfilesetを書換えて実行する */
		BuildXml buildXml = new BuildXml(new File("build.xml"));
		buildXml.rewriteDependency(dirTarget.getDependency().listFiles());
		buildXml.exeBuildXml();

		/* Jarを起動するためのスクリプトを作成する */
		createLaunchers(dirTarget);
	}


	/**
	 * ライブラリをスキャンし以下を行う
	 *
	 * ・ライセンス関連ファイルの取得
	 * ・バージョン情報の取得
	 * ・依存するJREのモジュールの情報を取得
	 * @param dirTarget
	 * @param jdk2
	 */
	protected void inspectLibrary(MavenTargetDirectory dirTarget, File jdk) {

		File jdeps = new File(jdk, "bin/jdeps.exe"); // ライブラリが使用するJREのモジュールを解析するプログラム

		List<File> jars = Arrays.asList(dirTarget.getDependency().listFiles());
		String licenseListTitle = appName + "が使用するライブラリ";
		DependencyInspector.scanJar(jars, dirTarget.getDependencyInfo(), licenseListTitle, jdeps, JDEPS_TXT);

	}

	/**
	 * JREを生成する
	 */
	protected void createJre(MavenTargetDirectory dirTarget, File jdk) {

		/* 必須モジュールの一覧を作成する */
		List<String> mods = new ArrayList<String>();
		for (File jeps: FileUtils.listFiles(dirTarget.getDependencyInfo(),
				FileFilterUtils.nameFileFilter(JDEPS_TXT), // ファイル名のフィルタ
				TrueFileFilter.INSTANCE) // ディレクトリ名は限定しない
				) {

			/* jdeps.txt */
			Utf8FileObj f = Utf8FileObj.of(jeps);
			for (String line: f.readTrimeLinesIgnoreEmpty()) {
				if (mods.contains(line) == false) {
					mods.add(line);
				}
			}
		}
		mods.add("java.security.jgss"); // TODO 検知できないため強制的に追加
		Collections.sort(mods);
		String strMods = ListToStringer.convert(mods, ",");
		File jreDir = dirTarget.getJre();
		File existMods = new File(jreDir, "modules.txt");

		Utf8FileObj fileMods = Utf8FileObj.of(existMods);
		if (jreDir.exists() && StringUtils.equals(strMods, fileMods.toString()) == false) {
			/* モジュール情報が変更されていれば、一旦削除 */
			FileDeleteUtils.delete(jreDir);
		}
		if (jreDir.exists() == false) {

			/* JREを作成する */
			File jlink = new File(jdk, "bin/jlink.exe");
			File jmods = new File(jdk, "jmods");
			JreMaker.createJre(jlink, jmods, jreDir, strMods);

			/* モジュール情報を書出す*/
			fileMods.writeString(strMods);
		}
	}

	/**
	 * Jarを起動するためのスクリプトを作成する
	 */
	public void createLaunchers(MavenTargetDirectory dirTarget) {

		File dirResult = dirTarget.getResult();

		String bat = ".\\jre-min\\bin\\java -jar "+ appName +".jar\n";
		String vbs = "Set ws = CreateObject(\"Wscript.Shell\")\n ws.run \"cmd /c main.bat\", vbhide\n";
		Utf8FileObj.of(new File(dirResult, "main.bat")).writeString(bat);
		Utf8FileObj.of(new File(dirResult, "main.vbs")).writeString(vbs);

	}

}
