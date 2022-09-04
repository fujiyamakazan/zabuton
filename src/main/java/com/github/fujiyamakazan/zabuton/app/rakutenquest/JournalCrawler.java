package com.github.fujiyamakazan.zabuton.app.rakutenquest;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
import com.github.fujiyamakazan.zabuton.util.RetryWorker;
import com.github.fujiyamakazan.zabuton.util.date.Chronus;
import com.github.fujiyamakazan.zabuton.util.jframe.JFrameUtils;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;
import com.ibm.icu.util.Calendar;

public abstract class JournalCrawler implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JournalCrawler.class);

    /**
     * 名称です。明細の「記録元」として使われます。
     */
    private final String name;

    private final File crawlerDailyDir;

    protected final File crawlerDir;

    private final File driver;

    //private Map<String, JournalCsv> masters = Generics.newHashMap();
    private JournalCsv master;
    //private File summary;

    /**
     * コンストラクタです。
     */
    public JournalCrawler(File appDir) {
        this(null, appDir);
    }

    /**
     * コンストラクタです。
     */
    public JournalCrawler(String name, File appDir) {
        if (StringUtils.isEmpty(name)) {
            this.name = getClass().getSimpleName();
        } else {
            this.name = name;
        }
        this.driver = new File(appDir, "chromedriver.exe");
        this.crawlerDir = new File(appDir, this.name);
        this.crawlerDir.mkdirs();
        this.crawlerDailyDir = new File(this.crawlerDir, "daily");
        this.crawlerDailyDir.mkdirs();

        String[] culmuns = getCulmuns();
        if (culmuns != null) {
            this.master = new JournalCsv(this.crawlerDir, "master.csv", culmuns);
            //this.masters.put("MAIN", master);
        }

    }

    protected abstract String[] getCulmuns();

    public JournalCsv getMaster() {
        //return this.getMaster("MAIN");
        return this.master;
    }

    //public JournalCsv getMaster(String key) {
    //    return this.masters.get(key);
    //}

    //  protected void setMaster(JournalCsv master) {
    //  this.masters.put("MAIN", master);
    //}

    //protected void setMaster(String key, JournalCsv master) {
    //    this.masters.put(key, master);
    //}

    //    protected void setSummary(File summary) {
    //        this.summary = summary;
    //    }
    //
    //    public File getSummary() {
    //        return this.summary;
    //    }

    /**
     * 明細をダウンロードします。
     * 本日ダウンロード分があればスキップします。
     */
    public final void download() {

        File fileToday = getDownloadFileLastOne();

        if (fileToday != null) {
            Date date = new Date(fileToday.lastModified());
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_MONTH, -1);
            if (date.after(yesterday.getTime())) {
                /* 本日ダウンロード分があれば中断 */
                return;
            }
        }

        try {
            /* 前回の処理結果を削除 */
            for (File f : this.crawlerDailyDir.listFiles()) {
                f.delete();
            }

            /* WebDriverを作成 */
            SelenCommonDriver cmd = new SelenCommonDriver() {
                private static final long serialVersionUID = 1L;

                @Override
                protected File getDriverFile() {
                    return JournalCrawler.this.driver;
                }

                @Override
                protected File getDownloadDir() {
                    return JournalCrawler.this.crawlerDailyDir;
                }

            };

            downloadCore(cmd);

            cmd.quit();

        } catch (Exception e) {
            e.printStackTrace(); // 標準出力
            JFrameUtils.showErrorDialog("エラーが発生しました。終了します。詳細なエラー情報を標準出力しました。");
            throw new RuntimeException(e);
        }
    }

    /**
     * ダウンロードの主処理を実装します。
     */
    protected abstract void downloadCore(SelenCommonDriver cmd);

    /**
     * 仕訳データを作成する処理の準備をします。
     */
    protected void prepareForCreateJurnals() {
        /* 処理なし */
    }

    /**
     * ダウンロードされたファイルを返します。ファイル名順です。
     */
    protected List<File> getDownloadFiles() {
        List<File> list = new ArrayList<File>(Arrays.asList(this.crawlerDailyDir.listFiles()));
        Collections.sort(list, new NameFileComparator());
        return list;
    }

    /**
     * ダウンロードされたファイルを返します。更新日付の新しい順です。
     */
    protected List<File> getDownloadFilesNew() {
        List<File> list = new ArrayList<File>(Arrays.asList(this.crawlerDailyDir.listFiles()));
        Collections.sort(list, new LastModifiedFileComparator());
        Collections.reverse(list);
        return list;
    }

    /**
     * ダウンロードされたファイルの数を返します。
     */
    protected int getDownloadFileSize() {
        return getDownloadFiles().size();
    }

    /**
     * 直近にダウンロードされたファイルを１つ返します。
     * ダウンロードされていなければnullを返します。
     */
    protected File getDownloadFileLastOne() {
        File lastFile;
        List<File> list = new ArrayList<File>(Arrays.asList(this.crawlerDailyDir.listFiles()));
        if (list.isEmpty()) {
            lastFile = null;
        } else {
            Collections.sort(list, new LastModifiedFileComparator());
            Collections.reverse(list);
            lastFile = list.get(0);
        }
        return lastFile;
    }

    protected File getDownloadFile(String name) {
        return new File(this.crawlerDailyDir, name);
    }

    protected String getDownloadTextAsUtf8LastOne() {
        File file = getDownloadFileLastOne();
        if (file != null) {
            return new Utf8Text(file).read();
        }
        return null;
    }

    protected String getDownloadTextAsUtf8(String name) {
        //        if (this.crawlerDailyDir.listFiles().length == 0) {
        //            return null;
        //        }
        //        return new Utf8Text(getDownloadFileLastOne()).read();
        File file = getDownloadFile(name);
        if (file != null) {
            return new Utf8Text(file).read();
        }
        return null;
    }

    protected void downloadFile(DownloadFileWorker downloadFileWorker) {
        int iniSize = getDownloadFileSize();
        downloadFileWorker.action();
        waitForDownload(iniSize); // ダウンロードが終わるのを待ちます。
    }

    protected abstract class DownloadFileWorker {
        protected abstract void action();
    }

    /**
     * ダウンロードが終わるのを待ちます。
     */
    private void waitForDownload(int iniSize) {
        new RetryWorker() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void run() {
                //File downloadFileOne = getDownloadFileLastOne();
                int count = getDownloadFileSize();
                //if (downloadFileOne == null) {
                if (count <= iniSize) {
                    throw new RuntimeException("ダウンロード未完了");
                } else {
                    //String name = downloadFileOne.getName();
                    String name = getDownloadFileLastOne().getName();
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
        }.start(); // ファイルダウンロードを待つ
    }

    protected void saveDaily(String name, String text) {
        new Utf8Text(new File(this.crawlerDailyDir, name)).write(text);
    }

    protected List<String> readDialies() {
        List<String> list = new ArrayList<String>();
        //for (File f : this.crawlerDailyDir.listFiles()) {
        for (File f : getDownloadFiles()) {
            list.add(new Utf8Text(f).read());
        }
        return list;
    }

    /**
     * ダウンロードしたファイルを削除します。
     */
    protected void deletePreFile() {

        File f = getDownloadFileLastOne();
        if (getDownloadFileLastOne() != null) {
            f.delete();
        }
        if (getDownloadFileLastOne() != null) {
            throw new RuntimeException();
        }
    }

    //    /**
    //     * ダウンロードしたファイルのテキスト情報を出力します。
    //     */
    //    public final String getText() {
    //        StringBuilderLn sb = new StringBuilderLn();
    //
    //        for (Map.Entry<String, JournalCsv> master : this.masters.entrySet()) {
    //            sb.appendLn("-----");
    //            sb.appendLn("[" + this.name + "] (" + master.getKey() + ")");
    //            sb.appendLn("-----");
    //            sb.appendLn(new Utf8Text(master.getValue().getFile()).read());
    //        }
    //        if (this.summary != null) {
    //            sb.appendLn("-----");
    //            sb.appendLn("[" + this.name + "] (SUMMARY)");
    //            sb.appendLn("-----");
    //            sb.appendLn(new Utf8Text(this.summary).read());
    //        }
    //
    //        return sb.toString();
    //    }

    public String getName() {
        return this.name;
    }

    protected boolean in(String value, String datePattern) {
        try {
            Chronus.parse(value, datePattern);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected Element getHtmlBody(File file) {
        Element body = Jsoup.parse(new Utf8Text(file).read()).getElementsByTag("body").first();
        return body;
    }

    protected Element getHtmlBody(String name) {
        return getHtmlBody(getDownloadFile(name));
    }

}
