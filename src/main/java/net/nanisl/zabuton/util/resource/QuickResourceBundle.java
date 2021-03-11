package net.nanisl.zabuton.util.resource;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 値の変更でサーバーの再起動を必要としない、プロパティファイルの読み取り機能
 * ・読み取り対象は settings.properties とする。
 * ・キャッシュの持続時間は 60 秒とする。
 *
 * @author fujiyama
 */
public class QuickResourceBundle implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final ResourceBundle.Control RESOURCE_BUNDLE_CONTROL = new ResourceBundle.Control() {
        public long getTimeToLive(String baseName, Locale locale) {
            return 60000L;
        }
    };

    private static ResourceBundle getBundle(String fileName) {
        return ResourceBundle.getBundle(fileName, RESOURCE_BUNDLE_CONTROL);
    }

    public static final String getString(String key) {
        return getPropString("settings", key);
    }

    /**
     * 指定されたプロパティファイルより値を取得する。
     * まず、xxx-overwrite.properties からの取得を試みる。
     * そこで値が取得できなかったら、xxx.propertiesからの取得を実施する。
     * 最終的に値が取得できない場合もエラーとしない。
     *
     * @param prop プロパティファイル
     * @param key キー
     * @return 取得値（取得できない場合はnull）
     */
    private static String getPropString(String prop, String key) {
        String value = null;
        try {
            ResourceBundle bundle = getBundle(prop + "-overwrite");
            value = bundle.getString(key);

        } catch (Exception e) {

            try {
                ResourceBundle bundle = getBundle(prop);
                value = bundle.getString(key);
            } catch (Exception e1) {
                // 処理なし
            }
        }
        return value;
    }

    public static final String getString(String key, String def) {
        String value = getString(key);
        if(value == null) {
            return def;
        } else {
            return value;
        }
    }

    public static boolean getBoolean(String key) {
        String value = getString(key);
        return toBoolean(value);
    }

    public static boolean toBoolean(String value) {
        if (value == null) return false;
        String str = value.toLowerCase();
        return str.equals("1")
                || str.equals("yes")
                || str.equals("y")
                || str.equals("true")
                || str.equals("t")
                ;
    }
    /**
     * @param key プロパティファイルのキー
     * @param def プロパティが参照できなかったときのデフォルト値
     * @return プロパティファイルの値
     */
    public static boolean getBoolean(String key, boolean def) {
        String value = getString(key);
        if(value == null) {
            return def;
        } else {
            String str = getString(key).toLowerCase();
            return str.equals("1")
                    || str.equals("y")
                    || str.equals("t")
                    || str.equals("yes")
                    || str.equals("true")
                    ;
        }
    }
}
