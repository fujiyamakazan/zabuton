package com.github.fujiyamakazan.zabuton.selen;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import com.github.fujiyamakazan.zabuton.app.rakutenquest.DownloadFileWorker;
import com.github.fujiyamakazan.zabuton.util.RetryWorker;

public class SelenUtils implements Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SelenUtils.class);

    /**
     * ダウンロードをします。
     */
    public static final void downloadFile(
        DownloadFileWorker downloadFileWorker,
        File dir
        ) {

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
                    String name = getLastOne(dir).getName();
                    if (name.endsWith(".tmp") || name.endsWith(".crdownload")) {
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

    /**
     * 直近のファイルを１つ返します。
     * ファイルが取得できなけければnullを返します。
     */
    public final static File getLastOne(File dir) {
        File lastFile;
        List<File> list = new ArrayList<File>(Arrays.asList(dir.listFiles()));
        if (list.isEmpty()) {
            lastFile = null;
        } else {
            Collections.sort(list, new LastModifiedFileComparator());
            Collections.reverse(list);
            lastFile = list.get(0);
        }
        return lastFile;
    }
}
