package com.github.fujiyamakazan.zabuton.tool;

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
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.fujiyamakazan.zabuton.util.ThrowableToString;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

/**
 * A-LINE
 *
 * <pre>
 *
 *
 * </pre>
 *
 * @author fujiyama
 */
public class ALine implements Serializable {
    private static final long serialVersionUID = 1L;

    private SimpleDateFormat dfYyyyMMdd = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat dfYyyyMMddHHmmss = new SimpleDateFormat(dfYyyyMMdd.toPattern() + " HH:mm:ss");

    private File setting = new File("a-line.setting.txt");
    private File aliveLog = new File("a-line.log.txt");

    public static void main(String[] args) {
        new ALine().run(new String[] {""});
    }

    public class SettingItems implements Serializable {
        private static final long serialVersionUID = 1L;

        private boolean use = false;
        private Integer hour = 0;
        private String token = "";
        private String message = "";
        private boolean start = false;

        public SettingItems(String params) {

            String[] settings = params.split(",");

            /* メンバ変数設定 */
            for (String setting : settings) {
                setting = setting.trim();
                if (setting.contains("=") == false) {
                    continue;
                }
                String[] keyValue = setting.split("=");
                String key = keyValue[0].trim();
                final String value;
                if (keyValue.length > 1) {
                    value = keyValue[1].trim();
                } else {
                    value = "";
                }

                if (StringUtils.equals(key, "use")) {
                    use = StringUtils.equals(value, "1");
                }
                if (StringUtils.equals(key, "hour")) {
                    hour = Integer.parseInt(value);
                }
                if (StringUtils.equals(key, "token")) {
                    token = value;
                }
                if (StringUtils.equals(key, "msg")) {
                    message = value;
                }
                if (StringUtils.equals(key, "start")) {
                    start = StringUtils.equals(value, "1");
                }
            }
        }

        public SettingItems() {

        }

        public boolean isUse() {
            return use;
        }

        public void setUse(boolean use) {
            this.use = use;
        }

        public Integer getHour() {
            return hour;
        }

        public void setHour(Integer hour) {
            this.hour = hour;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public boolean isStart() {
            return start;
        }

        public void setStart(boolean start) {
            this.start = start;
        }

        @Override
        public String toString() {
            return "A-LINE設定 [use=" + use + ", hour=" + hour + ", token=" + token + ", message=" + message + ", start="
                + start + "]";
        }
    }

    private SettingItems settingItems;

    public static void execute(String[] args) {
        new ALine().run(args);
    }

    /**
     * 設定処理です。
     */
    public void setting(SettingItems settingItem) {
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
    public void run(String[] args) {

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
            boolean isFirst = getLogLinesToday().isEmpty();

            /* ログを記録する */
            writeLog("PcAlive");

            if (args != null && args.length > 0 && StringUtils.equals(args[0], "test")) {
                writeLog("A-LINEをテストします。");
            }

            if (setting.exists() == false) {
                if (test) {
                    writeLog("まだ設定が終わっていません。");
                }
                return;
            }

            /* ファイルから設定を読込む。 */
            String text;
            try {
                text = FileUtils.readFileToString(setting, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.settingItems = new SettingItems(text);

            /* 「有効化」されていなければ送信しない */
            if (this.settingItems.use == false) {

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
                        List<String> logLines = new Utf8Text(aliveLog).readLines();
                        Collections.reverse(logLines);
                        for (String line : logLines) {
                            if (StringUtils.endsWith(line, "WindowsBackupEnd")) {
                                msg += " 最後のバックアップ情報[" + line + "]";
                                break;
                            }
                        }
                    }

                    writeLog("通知します。[" + msg + "]");

                    /* 送信 */
                    line(msg);

                } catch (Exception e) {
                    /* ログ出力 */
                    writeLog("通知に失敗しました。「有効化」をオフにします。" + e.getMessage());

                    /* 「有効化」の設定を解除する */
                    this.settingItems.use = false;
                    /* 設定保存 */
                    save();
                }
            }

        } catch (Exception e) {

            /* ログ出力 */
            writeLog("エラーが発生しました。" + e.getMessage());
            writeLog(ThrowableToString.convertToString(e));
        }
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
            FileUtils.write(setting, text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * LINE Notify を呼出します。
     */
    private void line(String message) throws Exception {

        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://notify-api.line.me/api/notify");

            boolean useProxy = (proxyHost() != null) && (proxyPort() != null);
            if (useProxy) {
                SocketAddress addr = new InetSocketAddress(
                    proxyHost(),
                    Integer.valueOf(proxyPort()));
                Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
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

                int httpStatus = conn.getResponseCode();
                if (httpStatus == 401) {
                    throw new Exception("トークンに誤りがあります。");
                }

                try (InputStream is = conn.getInputStream();
                    BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
                    String result = r.lines().collect(Collectors.joining());
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
    private void writeLog(String msg) {
        String text = getNow()
            + "[" + getHostName() + "]"
            + "" + msg;
        try {
            FileUtils.write(aliveLog, text + "\r\n", StandardCharsets.UTF_8, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 本日のログを取得します。
     */
    private List<String> getLogLinesToday() {
        List<String> logs = new ArrayList<String>();
        if (aliveLog.exists() == false) {
            return logs;
        }
        List<String> lines;
        try {
            lines = FileUtils.readLines(aliveLog, StandardCharsets.UTF_8);
        } catch (IOException e) {
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
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        return Integer.compare(currentHour, this.settingItems.hour) == 0;
    }

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private String getToday() {
        return dfYyyyMMdd.format(new Date());
    }

    private String getNow() {
        return dfYyyyMMddHHmmss.format(new Date());
    }

    protected String proxyHost() {
        return null;
    }

    protected Integer proxyPort() {
        return null;
    }

    public void setLog(File log) {
        this.aliveLog = log;
    }

    public boolean existSetting() {
        return setting.exists();
    }

}
