package net.nanisl.zabuton.tool;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.nanisl.zabuton.util.exec.RuntimeExc;

/**
 * 配布用に最小限のJREを作成する
 *
 * @author fujiyama
 */
public class JreMaker {

	private static final Logger log = LoggerFactory.getLogger(JreMaker.class);

	/**
	 * 配布用に最小限のJREを作成する
	 *
	 * @param jlink JDK の jlink.exe
	 * @param jmods JDK の jmods フォルダ
	 * @param target 作成先
	 * @param mods モジュール（カンマ区切り）
	 */
	public static void createJre(File jlink, File jmods, File target, String mods) {

		RuntimeExc runtimeExcJLink = new RuntimeExc();
		runtimeExcJLink.exec(
				jlink.getAbsolutePath(),
				"--compress=2",
				"--module-path", jmods.getAbsolutePath(),
				"--add-modules", mods,
				"--output", target.getAbsolutePath());

		log.debug(runtimeExcJLink.getOutText());
		String errText = runtimeExcJLink.getErrText();
		if (StringUtils.isNotEmpty(errText)) {
			log.warn(errText);
		}
		if (runtimeExcJLink.isSuccess() == false) {
			throw new RuntimeException(errText);
		}
	}
}
