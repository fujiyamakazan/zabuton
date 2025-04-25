package com.github.fujiyamakazan.zabuton.selen.scraper;

import java.io.File;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
import com.github.fujiyamakazan.zabuton.selen.SelenDeck;
import com.github.fujiyamakazan.zabuton.util.file.FileDirUtils;
import com.github.fujiyamakazan.zabuton.util.text.TextFile;

/**
 * スクレイピングをします。
 *
 * TODO ダウンロードだけではなくスクレイピングをするところまで定義する
 *
 * @author fujiyama
 */
public abstract class Scraper implements Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(MethodHandles.lookup().lookupClass());

    //    /**
    //     * 作業フォルダです
    //     * このフォルダにキャッシュファイルが作成されます。
    //     */
    //    protected final File work;
    //
    //    /**
    //     * Seleniumの制御情報を保存するディレクトリです。
    //     * このフォルダにWebDriverを配備して使用します。
    //     * このフォルダにPasswordManagerの情報を保存します。
    //     */
    //    protected final File selen;

    //    /**
    //     * コンストラクタです。
    //     */
    //    public Scraper(
    //        final File work,
    //        final File selen) {
    //        this.work = work;
    //        this.selen = selen;
    //    }

    private final SelenDeck deck;

    public Scraper(SelenDeck deck) {
        this.deck = deck;
    }

    /**
     * 初期化処理を実装します。
     */
    public void onInitialize() {
        // 既定の処理なし
    }

    /**
     * ダウンロードします。
     * 前提条件を満たしていなければダウンロードはキャンセルします。
     */
    public final void download() {

        if (isNeeded()) {
            SelenCommonDriver cmd = null;
            try {
                cmd = createCmd();
                if (deck != null) {
                    deck.register(cmd);
                }
                preDownload();
                doDownload(cmd);

            } catch (Exception e) {
                onFailureDownload();
                throw new RuntimeException(e);

            } finally {
                if (cmd != null) {
                    cmd.close();
                }
                if (deck != null) {
                    deck.unregister(cmd);
                }
            }
        }
    }

    /**
     * 前提条件を実装します。
     * すでにダウンロード済みのキャッシュが取得済みであれば、
     * ダウンロードをキャンセルするために条件を設定します。
     */
    protected abstract boolean isNeeded();

    /**
     * SelenCommonDriverを作成する処理を実装します。
     */
    protected abstract SelenCommonDriver createCmd();

    /**
     * ダウンロード前の事前処理を実行します。
     */
    protected void preDownload() {
        // 必要に応じて上書き実装のこと。
    }

    /**
     * ダウンロードの処理が失敗したときの処理を実装します。
     */
    protected void onFailureDownload() {
        // 必要に応じて上書き実装のこと。
    }

    /**
     * ダウンロードを実行する処理を実装します。
     */
    protected abstract void doDownload(SelenCommonDriver cmd);

    /**
     * ダウンロード済みのキャッシュの有効期限が切れていないか？
     * などの判定に使用できるユーティリティメソッドです。
     * 引数のディレクトリに含まれるファイルを検査します。
     * 最新のファイルが一日以上経過して入ればTrueを返します。
     * ディレクトリが空のときもTrueを返します。
     */
    public static boolean isCacheExpired(File dir) {
        final File fileToday = FileDirUtils.getLastOne(dir);
        if (fileToday == null) {
            return true; // キャッシュが無い
        } else {
            Date date = new Date(fileToday.lastModified());
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_MONTH, -1);
            return !date.after(yesterday.getTime()); // 一日以上経過
        }
    }

    /**
     * HTMLファイルからbodyタグを取得します。
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



    //protected abstract File getAppDir();

    //    protected CookieManager createCookieManager() {
    //        return new CookieManager(this.driverDir, this.cmd);
    //    }
    //
    //    public PasswordManager createPasswordManager() {
    //        return new PasswordManager(this.driverDir);
    //    }
    //
    //    protected boolean existFile(String path) {
    //        File f = new File(this.dir, path);
    //        return f.exists();
    //    }
    //
    //    protected final void save(String name, String path) {
    //        Utf8Text.writeData(new File(dir, name), path);
    //    }

    //    protected final void downloadFile(DownloadFileWorker downloadFileWorker) {
    //        SelenUtils.downloadFile(downloadFileWorker, dir);
    //    }
    //
    //    protected final File getFileLastOne() {
    //        return SelenUtils.getLastOne(dir);
    //    }

    //    public String getName() {
    //        return this.getClass().getSimpleName();
    //    }

    //    /**
    //     * 事前準備をします。
    //     * SelenCommonDriverを生成します。
    //     */
    //    protected void prepare() {
    //
    //        LOGGER.debug(getName() + ".execute...");
    //
    //        this.cmd = new SelenCommonDriver() {
    //
    //            private static final long serialVersionUID = 1L;
    //
    //            @Override
    //            protected File getDriverDir() {
    //                return Scraper.this.driverDir;
    //            }
    //
    //            @Override
    //            protected File getDownloadDir() {
    //                return Scraper.this.dir;
    //            }
    //
    //        };
    //
    //        //this.pm = new PasswordManager(dirSelen);
    //        //this.cm = new CookieManager(this.dirSelen, this.cmd);
    //
    //    }

    //    protected File getFile(String name) {
    //        return new File(this.dir, name);
    //    }

    //    protected File getDir() {
    //        return this.dir;
    //    }

    //    public abstract class Action implements Serializable {
    //        private static final long serialVersionUID = 1L;
    //        private final String name;
    //
    //        public Action(String name) {
    //            this.name = name;
    //        }
    //
    //
    //        public String getName() {
    //            return this.name;
    //        }
    //
    //        public abstract Action execute();
    //
    //    }
    //
    //    public abstract void execute();

}
