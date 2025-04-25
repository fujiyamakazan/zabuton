package com.github.fujiyamakazan.zabuton.app.aline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.date.Chronus;
import com.github.fujiyamakazan.zabuton.util.string.Stringul;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

/**
 * A-LINEアプリです。
 * @author fujiyama
 */
public class ALine implements Serializable {
    private static final long serialVersionUID = 1L;

    // staticなDateFormatのメソッド呼び出しはバグの可能性がある
    //private static SimpleDateFormat dfYyyyMMdd = new SimpleDateFormat(Chronus.POPULAR_JP);
    private static SimpleDateFormat getDfYyyyMMdd() {
        return new SimpleDateFormat(Chronus.POPULAR_JP);
    }

    // staticなDateFormatのメソッド呼び出しはバグの可能性がある
    //private static SimpleDateFormat dfYyyyMMddHHmmss = new SimpleDateFormat(dfYyyyMMdd.toPattern() + " HH:mm:ss");
    private static SimpleDateFormat getDfYyyyMMddHHmmss() {
        return new SimpleDateFormat(getDfYyyyMMdd().toPattern() + " HH:mm:ss");
    }

    private final File setting;
    private File aliveLog;
    private final File logDir;

    /**
     * コンストラクタです。
     * @param appDir 設定ファイルを保存するディレクトリ
     */
    public ALine(final File appDir) {
        setting = new File(appDir, "a-line.setting.txt");
        aliveLog = new File(appDir, "a-line.log.txt");
        logDir = new File(appDir, "a-line.send.logs");
        if (logDir.exists() == false) {
            logDir.mkdirs();
        }
    }

    public static void main(final String[] args) {
        new ALine(new File(EnvUtils.getUserAppData(), "ALine")).run(new String[] { "test" });
    }

    public class SettingItems implements Serializable {
        private static final long serialVersionUID = 1L;

        private boolean use = false;
        private Integer hour = 0;
        private String token = "";
        private String message = "";
        private boolean start = false;

        /**
         * コンストラクタです。
         */
        public SettingItems(final String params) {

            final String[] settings = params.split(",");

            /* メンバ変数設定 */
            for (String setting : settings) {
                setting = setting.trim();
                if (setting.contains("=") == false) {
                    continue;
                }
                final String[] keyValue = setting.split("=");
                final String key = keyValue[0].trim();
                final String value;
                if (keyValue.length > 1) {
                    value = keyValue[1].trim();
                } else {
                    value = "";
                }

                if (StringUtils.equals(key, "use")) {
                    this.use = StringUtils.equals(value, "1");
                }
                if (StringUtils.equals(key, "hour")) {
                    this.hour = Integer.parseInt(value);
                }
                if (StringUtils.equals(key, "token")) {
                    this.token = value;
                }
                if (StringUtils.equals(key, "msg")) {
                    this.message = value;
                }
                if (StringUtils.equals(key, "start")) {
                    this.start = StringUtils.equals(value, "1");
                }
            }
        }

        public SettingItems() {

        }

        public boolean isUse() {
            return this.use;
        }

        public void setUse(final boolean use) {
            this.use = use;
        }

        public Integer getHour() {
            return this.hour;
        }

        public void setHour(final Integer hour) {
            this.hour = hour;
        }

        public String getToken() {
            return this.token;
        }

        public void setToken(final String token) {
            this.token = token;
        }

        public String getMessage() {
            return this.message;
        }

        public void setMessage(final String message) {
            this.message = message;
        }

        public boolean isStart() {
            return this.start;
        }

        public void setStart(final boolean start) {
            this.start = start;
        }

        @Override
        public String toString() {
            return "A-LINE設定 [use=" + this.use + ", hour=" + this.hour + ", token=" + this.token + ", message="
                + this.message + ", start="
                + this.start + "]";
        }
    }

    private SettingItems settingItems;

    public static void execute(final String[] args, final File appDir) {
        new ALine(appDir).run(args);
    }

    /**
     * 設定処理です。
     */
    public void setting(final SettingItems settingItem) {
        this.settingItems = settingItem;

        /* 設定保存 */
        save();

        /* 稼働ログを記録する */
        writeLog("A-LINEの設定を変更しました。");
        writeLog(settingItem.toString());
        writeLog("「テスト」をしてください。");

        /*
         * 設定変更の場合はここで終了
         */
        return;

    }

    public void run() {
        run(null);
    }

    /**
     * PcAliveの処理です。
     * 　「PcAlive」をログに書き出します。
     * 　さらに、指定した時間帯ならLineに通知します。
     * 　一日の最初の起動の時には、最後に成功したバックアップの日付をメッセージ。
     */
    public void run(final String[] args) {

        boolean test = false;
        /*
         * パラメータ[set]があれば、設定処理へ。
         */
        if (args != null && args.length > 0) {
            if (StringUtils.equals(args[0], "set")) {
                /* パラメータから設定 */
                setting(new SettingItems(args[1]));
                return;

            } else if (StringUtils.equals(args[0], "test")) {
                test = true;

            } else {
                writeLog("パラメータが不正です。[" + args[0] + "]");
                return;
            }
        }

        try {
            /* 処理の前に本日分のログがあるかを判定 */
            final boolean isFirst = getLogLinesToday().isEmpty();

            /* ログを記録する */
            writeLog("PcAlive");

            if (args != null && args.length > 0 && StringUtils.equals(args[0], "test")) {
                writeLog("A-LINEをテストします。");
            }

            if (this.setting.exists() == false) {
                if (test) {
                    writeLog("まだ設定が終わっていません。");
                }
                return;
            }

            /* ファイルから設定を読込む。 */
            setupSetting();

            /* 「有効化」されていなければ送信しない */
            if (isUse()) {

                if (test) {
                    writeLog("「有効化」がオフなので、通知できません。");
                }
                return;
            }

            /* 通知条件の判定 */
            final boolean send;

            if (test) {
                /* テストモードの場合は条件に関係なく通知 */
                send = true;

            } else {
                if (isOnTime()) {
                    /* 時間帯が該当する。*/
                    send = true;

                } else if (this.settingItems.start && isFirst) {
                    /* 本日最初のアクセス */
                    send = true;

                } else {
                    send = false;
                }
            }

            /* 通知 */
            if (send) {
                try {

                    String msg = this.settingItems.message;

                    if (isFirst) {

                        msg += "本日最初の確認です。";

                        /* 最後のバックアップの情報も追加 */
                        final List<String> logLines = new Utf8Text(this.aliveLog).readLines();
                        Collections.reverse(logLines);
                        for (final String line : logLines) {
                            if (StringUtils.endsWith(line, "WindowsBackupEnd")) {
                                msg += " 最後のバックアップ情報[" + line + "]";
                                break;
                            }
                        }
                    }

                    writeLog("通知します。[" + msg + "]");

                    /* 送信 */
                    line(msg);

                } catch (final Exception e) {
                    /* ログ出力 */
                    writeLog("通知に失敗しました。「有効化」をオフにします。" + e.getMessage());

                    /* 「有効化」の設定を解除する */
                    this.settingItems.use = false;
                    /* 設定保存 */
                    save();
                }
            }

        } catch (final Exception e) {

            /* ログ出力 */
            writeLog("エラーが発生しました。" + e.getMessage());
            writeLog(Stringul.ofException(e));
        }
    }

    public boolean isUse() {
        return this.settingItems.use;
    }

    /**
     * 設定をします。
     */
    public void setupSetting() {
        String text;
        try {
            text = FileUtils.readFileToString(this.setting, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        this.settingItems = new SettingItems(text);
    }

    public void writeWindowsBackupStart() {
        /* ログを記録する */
        writeLog("WindowsBackupStart");
    }

    public void writeWindowsBackupEnd() {
        /* ログを記録する */
        writeLog("WindowsBackupEnd");
    }

    /**
     * 設定をファイルに保存します。
     */
    private void save() {

        String text = "";
        text += "use=" + (this.settingItems.use ? "1" : "0") + ",";
        text += "hour=" + this.settingItems.hour + ",";
        text += "token=" + this.settingItems.token + ",";
        text += "msg=" + this.settingItems.message + ",";
        text += "start=" + (this.settingItems.start ? "1" : "0") + ",";

        try {
            FileUtils.write(this.setting, text, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * LINE Notify を呼出します。
     */
    public void line(final String message) throws Exception {

        HttpURLConnection conn = null;
        try {
            final URL url = new URI("https://notify-api.line.me/api/notify").toURL();

            final boolean useProxy = (proxyHost() != null) && (proxyPort() != null);
            if (useProxy) {
                final SocketAddress addr = new InetSocketAddress(
                    proxyHost(),
                    Integer.valueOf(proxyPort()));
                final Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
                conn = (HttpURLConnection) url.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Authorization", "Bearer " + this.settingItems.token);

            try (OutputStream os = conn.getOutputStream();
                PrintWriter writer = new PrintWriter(os)) {

                writer.append("message=").append(URLEncoder.encode(
                    "[" + getHostName() + "]" + message, "UTF-8")).flush();

                final int httpStatus = conn.getResponseCode();
                if (httpStatus == 401) {
                    throw new Exception("トークンに誤りがあります。");
                }

                try (InputStream is = conn.getInputStream();
                    BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
                    final String result = r.lines().collect(Collectors.joining());
                    if (result.contains("\"message\":\"ok\"") == false) {
                        throw new Exception(result);
                    }
                }
            }

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * ログファイルへ書込みます。
     */
    public void writeLog(final String msg) {
        final String text = getNow()
            + "[" + getHostName() + "]"
            + "" + msg;
        try {
            FileUtils.write(this.aliveLog, text + "\r\n", StandardCharsets.UTF_8, true);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 本日のログを取得します。
     */
    private List<String> getLogLinesToday() {
        final List<String> logs = new ArrayList<String>();
        if (this.aliveLog.exists() == false) {
            return logs;
        }
        List<String> lines;
        try {
            lines = FileUtils.readLines(this.aliveLog, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        for (String line : lines) {
            line = line.trim();
            if (StringUtils.startsWith(line, getToday())) {
                logs.add(line);
            }
        }
        return logs;
    }

    /**
     * 通知時間帯のときにTrueを返します。
     */
    private boolean isOnTime() {
        final Calendar calendar = Calendar.getInstance();
        final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        return Integer.compare(currentHour, this.settingItems.hour) == 0;
    }

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getToday() {
        return getDfYyyyMMdd().format(new Date());
    }

    private static String getNow() {
        return getDfYyyyMMddHHmmss().format(new Date());
    }

    protected String proxyHost() {
        return null;
    }

    protected Integer proxyPort() {
        return null;
    }

    public void setLog(final File log) {
        this.aliveLog = log;
    }

    public boolean existSetting() {
        return this.setting.exists();
    }

    public String readToken() {
        return this.settingItems.token;
    }

    /**
     * 送れる状態かどうかをチェックします。
     * @param title 区分名
     * @param distance 前回同じ区分のメッセージを送信してから、次に送ってもよい期間
     */
    public boolean canSend(final String title, final Duration distance) {
        final File subDir = new File(this.logDir, title);
        if (subDir.exists()) {
            for (final File log : subDir.listFiles()) {
                /* 指定期間を過ぎていないログがあればfalseを返す。*/
                final LocalDateTime mod = Chronus.localDateTimeOf(new Date(log.lastModified()));
                if (Chronus.isPast(mod, distance) == false) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * ラインとともにログに記録します。
     */
    public void lineWithLog(final String msg, final String title) throws Exception {
        this.line(msg);
        final File subDir = new File(this.logDir, title);
        final LocalDateTime now = LocalDateTime.now();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");
        final File log = new File(subDir, now.format(formatter) + ".log");
        Utf8Text.writeData(log, msg);
    }

}
