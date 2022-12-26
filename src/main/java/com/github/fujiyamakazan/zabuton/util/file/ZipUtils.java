package com.github.fujiyamakazan.zabuton.util.file;

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

    public abstract static class UnzipTask implements Serializable {

        private static final long serialVersionUID = 1L;

        private File zip;

        public UnzipTask(File zip) {
            this.zip = zip;
        }

        /**
         * Zipファイルを展開し、エントリーごとに処理します。
         */
        public void start() {
            // FIXME https://www.jpcert.or.jp/java-rules/ids04-j.html
            try (
                FileInputStream fis = new FileInputStream(zip);
                BufferedInputStream bis = new BufferedInputStream(fis);
                //ZipInputStream zis = new ZipInputStream(bis, Charset.forName("MS932"));) {
                ZipInputStream zis = new ZipInputStream(bis);) {
                ZipEntry zipentry;
                while ((zipentry = zis.getNextEntry()) != null) {
                    if (accept(zipentry) == false) {
                        continue;
                    }

                    File tmp = File.createTempFile(ZipUtils.class.getName(), "");

                    try (FileOutputStream fos = new FileOutputStream(tmp);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);) {
                        byte[] data = new byte[1024];
                        int count = 0;
                        while ((count = zis.read(data)) != -1) {
                            bos.write(data, 0, count);
                        }
                    }

                    runByEntry(zipentry.getName(), tmp);

                    tmp.delete();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 処理対象かどうかを判定します。
         */
        protected boolean accept(ZipEntry zipentry) {
            return true;
        }

        /**
         * Zipファイルに含まれるエントリーに対して処理する実装をします。
         */
        protected abstract void runByEntry(String name, File file);

    }

}
