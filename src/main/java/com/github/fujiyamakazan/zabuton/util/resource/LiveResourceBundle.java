package com.github.fujiyamakazan.zabuton.util.resource;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * ResourceBundleから値を取得するユーティリティです。
 * キャッシュ有効期限を 60 秒とします。
 *
 * @author fujiyama
 */
public class LiveResourceBundle implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final ResourceBundle.Control RESOURCE_BUNDLE_CONTROL = new ResourceBundle.Control() {
        @Override
        public long getTimeToLive(String baseName, Locale locale) {
            /* デフォルトでは無制限（-2）が指定されているので、「60秒」に上書きする。 */
            return 60000L;
        }
    };

    private static ResourceBundle getBundle(String baseName) {
        return ResourceBundle.getBundle(baseName, RESOURCE_BUNDLE_CONTROL);
    }

    /**
     * プロパティファイル名を指定して値を取得します。
     * まず、{baseName}-overwrite.propertiesからの取得を試みます。
     * そこで値が取得できなかったら、{baseName}.propertiesから取得します。
     * 最終的に値が取得できない場合もエラーとしません。
     *
     * @param key キー
     * @return 値（取得できなければfalse）
     */
    public static boolean getLiveBoolean(String baseName, String key) {
        String value = getLiveString(baseName, key);
        return toBoolean(value);
    }

    /**
     * プロパティファイル名を指定して値を取得します。
     * まず、{baseName}-overwrite.propertiesからの取得を試みます。
     * そこで値が取得できなかったら、{baseName}.propertiesから取得します。
     * 最終的に値が取得できない場合もエラーとしません。
     *
     * @param baseName プロパティファイル名
     * @param key キー
     * @return 値（取得できなければnull）
     */
    public static String getLiveString(String baseName, String key) {
        String value = null;
        try {
            ResourceBundle bundle = getBundle(baseName + "-overwrite");
            value = bundle.getString(key);

        } catch (Exception e1) {
            try {
                ResourceBundle bundle = getBundle(baseName);
                value = bundle.getString(key);
            } catch (Exception e2) {
                /* エラーとしない */
            }
        }
        return value;
    }

    /**
     * Stringで示された論理値をtrueかfalseに変換します。
     * [true][t][yes][y][1]で示されたときにtrueを返します。大文字小文字は問いません。
     * @param str 文字列
     * @return 変換後の値
     */
    private static boolean toBoolean(String str) {
        if (str == null) {
            return false;
        }
        str = str.toLowerCase();
        return str.equals("true")
            || str.equals("t")
            || str.equals("yes")
            || str.equals("y")
            || str.equals("1");
    }
}
