//package com.github.fujiyamakazan.zabuton.app.rakutenquest;
//
//import java.io.File;
//import java.io.Serializable;
//
//public final class JournalCrawler implements Serializable {
//    private static final long serialVersionUID = 1L;
//
//    //    protected final File crawlerDir;
//    //    final File crawlerDailyDir;
//    //    final File driver;
//    //    //private Map<String, JournalCsv> masters = Generics.newHashMap();
//    //    private JournalCsv master;
//    //    //private File summary;
//    private JournalFactory journalFactory;
//
//    /**
//     * コンストラクタです。
//     */
//    public JournalCrawler(File appDir, JournalFactory journalFactory) {
//
//        this.journalFactory = journalFactory;
//
//        //        if (StringUtils.isEmpty(name)) {
//        //            journalFactory.nameOnCrawler = getClass().getSimpleName();
//        //        } else {
//        //            journalFactory.nameOnCrawler = name;
//        //        }
//        //        this.driver = new File(appDir, "chromedriver.exe");
//        //        this.crawlerDir = new File(appDir, journalFactory.nameOnCrawler);
//        //        this.crawlerDir.mkdirs();
//        //        this.crawlerDailyDir = new File(this.crawlerDir, "daily");
//        //        this.crawlerDailyDir.mkdirs();
//        //
//        //        //String[] cols = journalFactory.getCulmuns();
//        //
//        //        String[] cols = journalFactory.getHeaders();
//        //
//        //        if (cols != null) {
//        //            this.master = new JournalCsv(this.crawlerDir, journalFactory.getMasterName(), cols);
//        //        }
//
//        //        onInitialize();
//
//    }
//
//    //    protected void onInitialize() {
//    //        //
//    //    }
//
//    //    protected String getMasterName() {
//    //        return "master.csv";
//    //    }
//
//    //protected abstract String[] getCulmuns();
//
//    //    protected JournalCsv getMaster() {
//    //        return journalFactory.master;
//    //    }
//
//    //    /**
//    //     * 明細をダウンロードします。
//    //     * 本日ダウンロード分があればスキップします。
//    //     */
//    //    protected final void download() {
//    //
//    //    }
//
//    //    /**
//    //     * ダウンロードされたファイルを返します。ファイル名順です。
//    //     */
//    //    public List<File> getDownloadFiles() {
//    //        List<File> list = new ArrayList<File>(Arrays.asList(journalFactory.crawlerDailyDir.listFiles()));
//    //        Collections.sort(list, new NameFileComparator());
//    //        return list;
//    //    }
//    //
//    //    /**
//    //     * ダウンロードされたファイルを返します。更新日付の新しい順です。
//    //     */
//    //    public List<File> getDownloadFilesNew() {
//    //        List<File> list = new ArrayList<File>(Arrays.asList(journalFactory.crawlerDailyDir.listFiles()));
//    //        Collections.sort(list, new LastModifiedFileComparator());
//    //        Collections.reverse(list);
//    //        return list;
//    //    }
//    //
//    //    /**
//    //     * ダウンロードされたファイルの数を返します。
//    //     */
//    //    protected int getDownloadFileSize() {
//    //        return getDownloadFiles().size();
//    //    }
//    //
//    //    /**
//    //     * 直近にダウンロードされたファイルを１つ返します。
//    //     * ダウンロードされていなければnullを返します。
//    //     */
//    //    protected File getDownloadFileLastOne() {
//    //        File lastFile;
//    //        List<File> list = new ArrayList<File>(Arrays.asList(journalFactory.crawlerDailyDir.listFiles()));
//    //        if (list.isEmpty()) {
//    //            lastFile = null;
//    //        } else {
//    //            Collections.sort(list, new LastModifiedFileComparator());
//    //            Collections.reverse(list);
//    //            lastFile = list.get(0);
//    //        }
//    //        return lastFile;
//    //    }
//    //
//    //    public File getDownloadFile(String name) {
//    //        return new File(journalFactory.crawlerDailyDir, name);
//    //    }
//    //
//    //    protected String getDownloadTextAsUtf8LastOne() {
//    //        File file = getDownloadFileLastOne();
//    //        if (file != null) {
//    //            return new Utf8Text(file).read();
//    //        }
//    //        return null;
//    //    }
//    //
//    //    public String getDownloadTextAsUtf8(String name) {
//    //        //        if (this.crawlerDailyDir.listFiles().length == 0) {
//    //        //            return null;
//    //        //        }
//    //        //        return new Utf8Text(getDownloadFileLastOne()).read();
//    //        File file = getDownloadFile(name);
//    //        if (file != null) {
//    //            return new Utf8Text(file).read();
//    //        }
//    //        return null;
//    //    }
//    //
//    //    protected void downloadFile(DownloadFileWorker downloadFileWorker) {
//    //        int iniSize = getDownloadFileSize();
//    //        downloadFileWorker.action();
//    //        waitForDownload(iniSize); // ダウンロードが終わるのを待ちます。
//    //    }
//
////    public abstract class DownloadFileWorker {
////        protected abstract void action();
////    }
////
////    /**
////     * ダウンロードが終わるのを待ちます。
////     */
////    void waitForDownload(int iniSize) {
////        new RetryWorker() {
////            private static final long serialVersionUID = 1L;
////
////            @Override
////            protected void run() {
////                //File downloadFileOne = getDownloadFileLastOne();
////                int count = journalFactory.getDownloadFileSize();
////                //if (downloadFileOne == null) {
////                if (count <= iniSize) {
////                    throw new RuntimeException("ダウンロード未完了");
////                } else {
////                    //String name = downloadFileOne.getName();
////                    String name = journalFactory.getDownloadFileLastOne().getName();
////                    if (name.endsWith(".tmp") || name.endsWith(".crdownload")) {
////                        throw new RuntimeException("ダウンロード実行中");
////                    }
////                }
////            }
////
////            @Override
////            protected void recovery() {
////                try {
////                    Thread.sleep(3_000);
////                } catch (InterruptedException e) {
////                    throw new RuntimeException(e);
////                }
////            }
////        }.start(); // ファイルダウンロードを待つ
////    }
////
////    protected void saveDaily(String name, String text) {
////        File file = new File(journalFactory.crawlerDailyDir, name);
////        TextFile textObj = new TextFile(file) {
////            private static final long serialVersionUID = 1L;
////
////            @Override
////            protected Charset getCharset() {
////                return journalFactory.getSaveDilyEnc();
////            }
////
////            //            @Override
////            //            protected Charset getWriteCharset() {
////            //                return StandardCharsets.UTF_8;
////            //            }
////
////        };
////        textObj.write(text);
////    }
////
////    public List<String> readDialies() {
////        List<String> list = new ArrayList<String>();
////        //for (File f : this.crawlerDailyDir.listFiles()) {
////        for (File f : journalFactory.getDownloadFiles()) {
////            list.add(new Utf8Text(f).read());
////        }
////        return list;
////    }
////
////    /**
////     * ダウンロードしたファイルを削除します。
////     */
////    protected void deletePreFile() {
////
////        File f = journalFactory.getDownloadFileLastOne();
////        if (journalFactory.getDownloadFileLastOne() != null) {
////            f.delete();
////        }
////        if (journalFactory.getDownloadFileLastOne() != null) {
////            throw new RuntimeException();
////        }
////    }
//
//    //    /**
//    //     * ダウンロードしたファイルのテキスト情報を出力します。
//    //     */
//    //    public final String getText() {
//    //        StringBuilderLn sb = new StringBuilderLn();
//    //
//    //        for (Map.Entry<String, JournalCsv> master : this.masters.entrySet()) {
//    //            sb.appendLn("-----");
//    //            sb.appendLn("[" + this.name + "] (" + master.getKey() + ")");
//    //            sb.appendLn("-----");
//    //            sb.appendLn(new Utf8Text(master.getValue().getFile()).read());
//    //        }
//    //        if (this.summary != null) {
//    //            sb.appendLn("-----");
//    //            sb.appendLn("[" + this.name + "] (SUMMARY)");
//    //            sb.appendLn("-----");
//    //            sb.appendLn(new Utf8Text(this.summary).read());
//    //        }
//    //
//    //        return sb.toString();
//    //    }
//
//}
