package com.github.fujiyamakazan.zabuton.selen;

import java.io.File;
import java.io.Serializable;

import com.github.fujiyamakazan.zabuton.app.rakutenquest.DownloadFileWorker;
import com.github.fujiyamakazan.zabuton.util.RetryWorker;
import com.github.fujiyamakazan.zabuton.util.file.FileDirUtils;

public class SelenUtils implements Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SelenUtils.class);

    /**
     * ダウンロードをします。
     */
    public static final void downloadFile(
        DownloadFileWorker downloadFileWorker,
        File dir) {

        int iniSize = dir.listFiles().length;

        downloadFileWorker.action();

        /*
         * ダウンロードが終わるのを待ちます。
         * 3秒に一度最新のファイルをチェックし、ファイルが増えていること、
         * そのファイルが一時ファイルでないことをもって、終了判定をします。
         */
        new RetryWorker() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void run() {
                int count = dir.listFiles().length;
                if (count <= iniSize) {
                    throw new RuntimeException("ダウンロード未完了");
                } else {
                    File lastOne = FileDirUtils.getLastOne(dir);
                    if (isDownloadTemp(lastOne)) {
                        throw new RuntimeException("ダウンロード実行中");
                    }
                }
            }

            @Override
            protected void recovery() {
                try {
                    Thread.sleep(3_000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
    }

    public static boolean isDownloadTemp(File file) {
        return file.getName().endsWith(".tmp") || file.getName().endsWith(".crdownload");
    }


}
