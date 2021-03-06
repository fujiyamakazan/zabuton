package net.nanisl.zabuton.util.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {

	public static void unzip(File jar, UnzipTask unzipTask) {

		// FIXME https://www.jpcert.or.jp/java-rules/ids04-j.html
		try (
				FileInputStream fis = new FileInputStream(jar);
				BufferedInputStream bis = new BufferedInputStream(fis);
				//ZipInputStream zis = new ZipInputStream(bis, Charset.forName("MS932"));) {
				ZipInputStream zis = new ZipInputStream(bis);) {
			ZipEntry zipentry;
			while ((zipentry = zis.getNextEntry()) != null) {

				File tmp = File.createTempFile(ZipUtils.class.getName(), "");

				try (FileOutputStream fos = new FileOutputStream(tmp);
						BufferedOutputStream bos = new BufferedOutputStream(fos);) {
					byte[] data = new byte[1024];
					int count = 0;
					while ((count = zis.read(data)) != -1) {
						bos.write(data, 0, count);
					}
				}

				unzipTask.run(zipentry.getName(), tmp);

				tmp.delete();
			}

		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	public interface UnzipTask extends Serializable {
		public void run(String entryName, File unZipFile) throws IOException;

	}

}
