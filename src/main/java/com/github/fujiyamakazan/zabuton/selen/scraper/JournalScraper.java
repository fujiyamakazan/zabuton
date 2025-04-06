package com.github.fujiyamakazan.zabuton.selen.scraper;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;

public abstract class JournalScraper<DTO> extends Scraper {
    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(MethodHandles.lookup().lookupClass());

    /**
     * 作業フォルダです
     * このフォルダにキャッシュファイルが作成されます。
     */
    private final File work;

    /**
     * Seleniumの制御情報を保存するディレクトリです。
     * このフォルダにWebDriverを配備して使用します。
     * このフォルダにPasswordManagerの情報を保存します。
     */
    private final File selen;

    /** キャッシュファイル */
    private final List<File> caches = Generics.newArrayList();

    public JournalScraper(File work, File selen) {
        this.work = work;
        this.selen = selen;
    }

    protected void addcache(String name) {
        caches.add(new File(work, name));
    }

    /**
     * ダウンロードしキャッシュを作成します。
     * すでに有効なキャッシュがあれば、スキップします。
     *
     * @param selenDir WebDriverが保存されているディレクトリ
     */
    public final JournalScraper<DTO> download() {
        if (!isCacheExpired()) {
            return this;
        }
        SelenCommonDriver cmd = null;
        try {
            cmd = SelenCommonDriver.createEdgeDriver(selen);
            //cmd.getDriver().manage().window().setSize(new Dimension(150, 400));
            doDownload(cmd);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (cmd != null) {
                cmd.quit();
            }
        }
        return this;
    }

    protected abstract void doDownload(final SelenCommonDriver cmd);

    protected abstract JournalScraper<DTO> updateMaster(File masterCsv);

    protected abstract int getAsset();

    public PasswordManager createPasswordManager() {
        return new PasswordManager(selen);
    }

    protected void saveCache(SelenCommonDriver cmd, String name) {
        if (!caches.stream().anyMatch(c -> StringUtils.equals(c.getName(), name))) {
            throw new IllegalArgumentException(name);
        }
        for (File cache : caches) {
            if (StringUtils.equals(cache.getName(), name)) {
                try {
                    FileUtils.write(cache, cmd.getPageSource(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    protected List<File> getCaches() {
        return caches;
    }

    protected File getCache(String name) {
        Optional<File> first = caches.stream().filter(
            c -> StringUtils.equals(c.getName(), name)).findFirst();
        if (first.isPresent()) {
            return first.get();
        } else {
            throw new IllegalArgumentException(name);
        }
    }

    /**
     * ダウンロード済みのキャッシュの有効期限が切れていないか？
     */
    private boolean isCacheExpired() {
        boolean hasRecentFile = false;
        final long twentyFourHoursInMillis = TimeUnit.HOURS.toMillis(24);
        final long currentTime = System.currentTimeMillis();
        for(File cache: caches) {
            long creationTime = this.caches.get(0).lastModified();
            if (currentTime - creationTime > twentyFourHoursInMillis) {
                LOGGER.debug("無効なキャッシュがあります。Webから取得する必要があります。");
                return true;
            }
        }
        return false;
    }


}
