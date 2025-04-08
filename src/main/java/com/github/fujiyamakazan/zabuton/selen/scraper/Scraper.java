package com.github.fujiyamakazan.zabuton.selen.scraper;

import java.io.File;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;

/**
 * スクレイピングをします。
 * @author fujiyama
 */
public abstract class Scraper implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {
        String opt = "edge";
        SelenCommonDriver cmd = new SelenCommonDriver(opt) {
            private static final long serialVersionUID = 1L;

            @Override
            protected File getDriverDir() {
                return EnvUtils.getUserDesktop("test");
            }

            @Override
            protected File getDownloadDir() {
                //return Scraper.this.dir;
                //throw new RuntimeException("未実装");
                return new File(EnvUtils.getUserDesktop("test"),"dl");
            }

        };

        cmd.get("https://haritora.net/look.cgi?script=45");
        cmd.clickButtonAndWait("全文ダウンロード");
        cmd.quit();
    }





//    protected SelenCommonDriver cmd;

//    private File driverDir;
//
//    private File dir;

    /**
     * コンストラクタです。
     */
    public Scraper() {
        //String name = getName();
        //this.driverDir = new File(getAppDir(), "selen");
        //this.dir = Path.of(getAppDir().getAbsolutePath(), "selen", "Scraper-sights", name).toFile();
        //if (this.dir.exists() == false) {
        //    Files.mkdirs(this.dir);
        //}
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
