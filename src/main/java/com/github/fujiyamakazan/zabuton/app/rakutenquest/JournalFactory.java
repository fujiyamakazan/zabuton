package com.github.fujiyamakazan.zabuton.app.rakutenquest;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
import com.github.fujiyamakazan.zabuton.selen.SelenDeck;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;
import com.github.fujiyamakazan.zabuton.util.text.TextFile;

public abstract class JournalFactory implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    //protected final JournalCsv master;
    //protected final PasswordManager pm;
    //protected final JournalsTerm term;
    //protected final String sourceName;
    //private final String assetName;
    //protected final File crawlerDir;
    //protected File cache;
    //protected final File appDir;
    private Exception downloadException;
    //private Exception createJurnalsException;

    /**
     * コンストラクタです。
     */
    public JournalFactory(
        //final String sourceName,
        //final JournalsTerm term,
        //final File appDir
        ) {

        //this.sourceName = sourceName;
        //this.term = term;
        //this.appDir = appDir;

        //this.crawlerDir = new File(appDir, getCrawlerName());
        //this.crawlerDir.mkdirs();

        //createCashe();

        //        String[] cols = getHeaders();
        //        if (cols != null) {
        //            this.master = new JournalCsv(this.crawlerDir, getMasterName(), cols);
        //        } else {
        //            this.master = null;
        //        }

        //this.pm = new PasswordManager(appDir);
        //onInitialize();
    }

    /**
     * 明細の元となる情報をダウンロードします。
     */
    public abstract void download();
//
//    /**
//     * 明細の元となる情報をダウンロードします。
//     */
//    protected final void doDownloadOnce(File appDir, File cacheDir, Consumer<SelenCommonDriver> downloader) {
//
//        final File fileToday = FileDirUtils.getLastOne(cacheDir);
//        /* 本日分のダウンロードファイルがあれば、ダウンロード処理を中断するように判定します。 */
//        boolean isSkip = false;
//        if (fileToday != null) {
//            Date date = new Date(fileToday.lastModified());
//            Calendar yesterday = Calendar.getInstance();
//            yesterday.add(Calendar.DAY_OF_MONTH, -1);
//            isSkip = date.after(yesterday.getTime());
//        }
//        boolean skipDownload = isSkip;
//        if (skipDownload) {
//            return;
//        }
//
//        try {
//            /* 前回の処理結果を削除 */
//            for (File f : cacheDir.listFiles()) {
//                f.delete();
//            }
//
//            /* WebDriverを作成 */
//            //SelenCommonDriver cmd = new ChoromeDriverFactory(appDir)
//            SelenCommonDriver cmd = new EdgeDriverFactory(appDir)
//                .downloadDir(cacheDir)
//                .build();
//
//            //doDownloadByChrome(cmd);
//            downloader.accept(cmd);
//
//            cmd.quit();
//
//        } catch (Exception e) {
//
//            /* 完了しなかった処理結果を削除 */
//            for (File f : cacheDir.listFiles()) {
//                f.delete();
//            }
//
//            //e.printStackTrace(); // 標準出力
//            LOGGER.error("ダウンロード失敗", e);
//            JFrameUtils.showErrorDialog("[" + getClass().getSimpleName() + "]"
//                + "エラーが発生しました。終了します。詳細なエラー情報を標準出力しました。");
//            throw new RuntimeException(e);
//        }
//    }

    /**
     * Dailyフォルダに書き出します。
     * @param name ファイル名
     * @param text 内容
     */
    protected static final void saveDaily(File cache, String name, String text, Charset charset) {
        File file = new File(cache, name);
        TextFile textObj = new TextFile(file) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Charset getCharset() {
                //return getSaveDilyEnc();
                return charset;
            }
        };
        textObj.write(text);
    }
    //    /**
    //     * Dailyフォルダに書き出すときの文字セットを決定します。
    //     */
    //    protected Charset getSaveDilyEnc() {
    //        return StandardCharsets.UTF_8;
    //    }
    //    /**
    //     * ファイルを呼出すときの文字セットを決定します。
    //     */
    //    protected Charset getReadCharset() {
    //        return StandardCharsets.UTF_8;
    //    }

    //    /**
    //     * 明細の元となる情報をGoogleChoromeでダウンロードするように
    //     * サブクラスで実装します。
    //     */
    //    protected void doDownloadByChrome(SelenCommonDriver cmd) {
    //        // 規定は処理なし。
    //    }

    //    /**
    //     * 仕訳データを作成する処理の準備としてマスターをアップデートします。
    //     * 既定の処理：
    //     * ・ダウンロードした１つのファイルから「createCsvRow」でデータを取得します。
    //     */
    //    protected void doUpdateMaster() {
    //        File file = doChoiceFileForUpdateMaster(cache);
    //
    //        Element body = getHtmlBody(file);
    //        List<String> rows = createCsvRow(body);
    //        final JournalMerger textMerger = createMerger();
    //        textMerger.stock(rows);
    //        textMerger.flash();
    //    }

//    /**
//     * 明細データを作成します。
//     * @param existDatas すでに登録済みのデータです。これにもとづき、新しい明細に絞り込みます。
//     * @param templates テンプレートデータ。これにもとづき、内容を修正します。
//     */
//    //public abstract List<Journal> createJournals(List<Journal> existDatas, List<Journal> templates);
//
//    public final List<Journal> createJournals(List<Journal> existDatas, List<Journal> templates) {
//
//        /* マスターを更新します */
//        doUpdateMaster();
//
//        /* マスターからからジャーナル形式のデータを作成し、以下の項目を設定します。
//         * 仕訳.記録元 ← JournalFactory.記録元名(インスタンス化時に指定)
//         * 仕訳.生データ ← 一行のテキスト
//         * 仕訳.Index(ID) ← CSVの行番号
//         * 仕訳.日付 ← CSVの日付
//         * 仕訳.キーワード ← キーワード情報（規定はpickupMemo()の実装による）
//         * 仕訳.メモ ← キーワード情報（JpickupMemo()の実装による）
//         * 仕訳.金額 ← CSVの金額
//         */
//        List<Journal> journals = createJournalsStep2();
//
//        /* テンプレート適用し、
//         * 借方、貸方、活動科目、メモを登録します。
//         */
//        createJornalStep3(templates, journals);
//
//        /* 仕訳済みを除外する */
//        return createJornalStep4(existDatas, journals);
//    }

    public abstract void doUpdateMaster();

    public abstract List<Journal> createJournalsStep2(); // TODO 誤字修正

    /**
     * CSVから仕訳レコードを作成します。
     *
     * 仕訳.記録元 ← JournalFactory.記録元名(インスタンス化時に指定)
     * 仕訳.生データ ← 一行のテキスト
     * 仕訳.Index(ID) ← CSVの行番号
     * 仕訳.日付 ← CSVの日付
     * 仕訳.キーワード ← キーワード情報（規定はJournalFactory#pickupMemo()の実装による）
     * 仕訳.メモ ← キーワード情報（JournalFactory#pickupMemo()の実装による）
     * 仕訳.金額 ← CSVの金額
     *
     * 日付が対象期間のデータのみ作成します。
     *
     */
    protected static List<Journal> masterToJournals(
        String sourceName, JournalsTerm term, JournalCsv masterCsv) {

        //JournalCsv masterCsv = getJournalCsvMaster();

        List<Journal> journals = Generics.newArrayList();
        for (JournalCsv.Row row : masterCsv.getRrows()) {

            Journal journal = new Journal();
            journal.setSource(sourceName);
            journal.setRawOnSource(row.getData());
            journal.setRowIndex(String.valueOf(row.getIndex()));

            //String strDate = pickupDate(row);
            /*
            String strDate = row.pickupDate();
            if (StringUtils.length(strDate) == 5) {
                throw new RuntimeException("不正日付:" + row);
            }
            final Date date = getDateFromCsv(strDate);
            */
            final Date date = row.getDateFromCsv(term);

            if (date == null) {
                continue;
            }
            journal.setDate(date);
            //journal.setKeywordOnSource(pickupKeywordOnsource(row));
            journal.setKeywordOnSource(row.pickupKeywordOnsource());
            //journal.setMemo(pickupMemo(row));
            journal.setMemo(row.pickupMemo());
            //journal.setAmount(MoneyUtils.toInt(pickupAmount(row)));
            journal.setAmount(MoneyUtils.toInt(row.pickupAmount()));

            journals.add(journal);

        }
        return journals;
    }

    //protected abstract Function<String, Row> createRowFactory();

    //protected abstract JournalCsv getJournalCsvMaster();

    /**
     * テンプレート適用し、
     * 借方、貸方、活動科目、メモを登録します。
     */
    public static void createJornalStep3(List<Journal> templates, List<Journal> journals) {
        if (templates != null) {
            for (Journal journal : journals) {
                for (Journal template : templates) {
                    if (journal.getSource().equals(template.getSource()) == false) {
                        continue;
                    }
                    String source = journal.getKeywordOnSource();
                    String templateSource = template.getKeywordOnSource();
                    if (templateSource == null) {
                        templateSource = "";
                    }

                    final boolean hit;
                    if (templateSource.equals("*")) {
                        hit = true;
                    } else if (templateSource.startsWith("*") && templateSource.endsWith("*")) {
                        hit = source.contains(templateSource.substring(1, templateSource.length() - 1));
                    } else if (templateSource.startsWith("*")) {
                        hit = source.endsWith(templateSource.substring(1));
                    } else if (templateSource.endsWith("*")) {
                        hit = source.startsWith(templateSource.substring(0, templateSource.length() - 1));
                    } else {
                        hit = source.equals(templateSource);
                    }

                    if (hit) {
                        journal.setLeft(template.getLeft());
                        journal.setRight((template.getRight()));
                        journal.setActivity(template.getActivity());
                        if (StringUtils.isNotEmpty(template.getMemo())) {
                            if (StringUtils.isNotEmpty(journal.getMemo())) {
                                journal.setMemo(journal.getMemo() + " " + template.getMemo());
                            } else {
                                journal.setMemo(template.getMemo());
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /* 仕訳済みを除外する */
    public static List<Journal> createJornalStep4(List<Journal> existDatas, List<Journal> journals) {
        for (Iterator<Journal> iterator = journals.iterator(); iterator.hasNext();) {
            Journal journal = iterator.next();
            if (existDatas != null) {
                for (Journal exist : existDatas) {
                    String sourceOfExixt = exist.getSource();
                    String sourceOfJournal = journal.getSource();
                    if (StringUtils.equals(sourceOfExixt, sourceOfJournal)
                        && StringUtils.equals(exist.getRowIndex(), journal.getRowIndex())) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }

        return journals;
    }

    //    /**
    //     * 初期化時の処理を実装します。
    //     * コンストラクタの最後に呼び出されます。
    //     */
    //    protected void onInitialize() {
    //        // 処理なし
    //    }

//    protected String getCrawlerName() {
//        return getClass().getSimpleName();
//    }

    protected String getMasterName() {
        return "master.csv";
    }





    //    protected JournalMerger createMerger(JournalCsv master) {
    //        final JournalMerger textMerger = new JournalMerger(
    //            getClass().getSimpleName(),
    //            master,
    //            getDateFormat());
    //        return textMerger;
    //    }

    //    protected Date getDateFromCsv(String strDate) {
    //        final Date date;
    //        String pattern = getDateFormat();
    //        if (this.term.in(strDate, pattern) == false) {
    //            date = null;
    //        } else {
    //            date = Chronus.parse(strDate, pattern);
    //        }
    //        return date;
    //    }

    /**
     * ファイルからHTMLを取得します。
     */
    public static final Element getHtmlBody(File file, Charset charset) {
        TextFile textObj = new TextFile(file) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Charset getCharset() {
                //return getReadCharset();
                return charset;
            }
        };
        String read = textObj.read();
        if (StringUtils.isEmpty(read)) {
            return null;
        }
        return Jsoup.parse(read).getElementsByTag("body").first();
    }

//    /**
//     * 行データを生成します。
//     * 既定の処理：
//     * ・tr要素、td要素からデータを取得します。
//     */
//    protected List<String> createCsvRow(Element body) {
//        List<String> rows = Generics.newArrayList();
//        for (Element tr : getTrElement(body)) {
//            List<String> row = Generics.newArrayList();
//            for (Element td : tr.select("td")) {
//                row.add(td.text());
//            }
//            String strCols = CsvUtils.convertString(row);
//            rows.add(strCols);
//        }
//        return rows;
//    }
//
//    /**
//     * tr要素を取り出します。
//     */
//    protected Elements getTrElement(Element body) {
//        return body.select("tr");
//    }

    public void setDownloadException(Exception e) {
        this.downloadException = e;
    }

    public Exception getDownloadException() {
        return downloadException;
    }

    public SelenDeck deck;

    public void set(SelenDeck deck) {
        this.deck = deck;
    }
    public void remove(SelenCommonDriver cmd) {
        this.deck.unregister(cmd);
    }

    //    /**
    //     * 列名を定義します。
    //     * JournalCsvを使用するときにサブクラスで上書き実装します。
    //     */
    //    protected String[] getHeaders() {
    //        return null;
    //    }

    //    /**
    //     * 行データから日付情報を取り出します。
    //     * JournalCsvを使用するときにサブクラスで上書き実装します。
    //     */
    //    protected String pickupDate(JournalCsv.Row row) {
    //        return null;
    //    }

    //    /**
    //     * 日付情報のフォーマットを返します。
    //     * JournalCsvを使用するときにサブクラスで上書き実装します。
    //     */
    //    protected String getDateFormat() {
    //        return null;
    //    }

    //    /**
    //     * 行データから金額情報を取り出します。
    //     * JournalCsvを使用するときにサブクラスで上書き実装します。
    //     */
    //    protected String pickupAmount(JournalCsv.Row row) {
    //        return null;
    //    }

    //    /**
    //     * 行データからメモ情報を取り出します。
    //     * JournalCsvを使用するときにサブクラスで上書き実装します。
    //     */
    //    protected String pickupMemo(JournalCsv.Row row) {
    //        return null;
    //    }

    //    /**
    //     * 行データからキーワード情報を抽出します。
    //     * 既定では、メモ情報をそのまま使用します。
    //     */
    //    protected String pickupKeywordOnsource(JournalCsv.Row row) {
    //        return pickupMemo(row);
    //    }

    //    protected static Journal createTemplate(
    //        String source, String activity, String left, String right, String memo, String key) {
    //        Journal data = new Journal();
    //        data.setSource(source);
    //        data.setLeft(left);
    //        data.setRight(right);
    //        data.setActivity(activity);
    //        data.setMemo(memo);
    //        data.setKeywordOnSource(key);
    //        return data;
    //    }

    //    /**
    //     * 名前を指定して、クロールディレクトリのファイルを返します。
    //     */
    //    protected final File getCrawlerFile(String fileName) {
    //        return new File(crawlerDir, fileName);
    //    }

    //    /**
    //     * 名前を指定して、ダウンロードされたファイルを返します。
    //     */
    //    public final File getDownloadFile(File cache, String name) {
    //        return new File(cache, name);
    //    }

    //    /**
    //     * ダウンロードされたファイルを返します。ファイル名順です。
    //     */
    //    private final List<File> getDownloadFiles(File cache) {
    //        List<File> list = new ArrayList<File>(Arrays.asList(cache.listFiles()));
    //        Collections.sort(list, new NameFileComparator());
    //        return list;
    //    }

    //    /**
    //     * ダウンロードされたファイルを返します。更新日付の新しい順です。
    //     */
    //    protected final List<File> getDownloadFilesNew(File cache) {
    //        List<File> list = new ArrayList<File>(Arrays.asList(cache.listFiles()));
    //        Collections.sort(list, new LastModifiedFileComparator());
    //        Collections.reverse(list);
    //        return list;
    //    }

    //    /**
    //     * 直近にダウンロードされたファイルを１つ返します。
    //     * ダウンロードされていなければnullを返します。
    //     */
    //    public final File getDownloadFileLastOne(File cache) {
    //        return FileDirUtils.getLastOne(cache);
    //
    //    }

    //    /**
    //     * 直近にダウンロードされたファイルを１つ、テキストとして返します。
    //     * ファイルが取得できなければnullを返します。
    //     */
    //    protected String getDownloadUtf8TextLastOne(File cache) {
    //        File file = FileDirUtils.getLastOne(cache);
    //        if (file != null) {
    //            return new Utf8Text(file).read();
    //        }
    //        return null;
    //    }

    //    /**
    //     * 複数件のダウンロードしたファイルをテキストとして返します。
    //     */
    //    protected List<String> getDownloadUtf8Texs(File cache) {
    //        List<String> list = new ArrayList<String>();
    //        for (File f : getDownloadFiles(cache)) {
    //            list.add(new Utf8Text(f).read());
    //        }
    //        return list;
    //    }
    //    /**
    //     * 名前を指定したファイルをテキストとして返します。
    //     * ダウンロードされていなければnullを返します。
    //     */
    //    protected String getDownloadUtf8Text(File cache, String name) {
    //        //File file = getDownloadFile(cache, name);
    //        //File file = new File(cache, name);
    //        //if (file != null) {
    //        //return new Utf8Text(file).read();
    //        return new Utf8Text(new File(cache, name)).read();
    //
    //        //}
    //        //return null;
    //    }

    //    protected final JournalMerger createTextManager() {
    //        return new JournalMerger(
    //            getClass().getSimpleName(),
    //            this.master,
    //            getDateFormat());
    //    }

    //    protected boolean useCookieManager() {
    //        return true;
    //    }

    //    public void setCreateJurnalsException(Exception e) {
    //        this.createJurnalsException = e;
    //    }

    //    public Exception getCreateJurnalsException() {
    //        return createJurnalsException;
    //    }


}
