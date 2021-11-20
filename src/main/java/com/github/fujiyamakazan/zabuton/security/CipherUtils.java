package com.github.fujiyamakazan.zabuton.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;

/**
 * 暗号化処理をします。
 * @author fujiyama
 */
public class CipherUtils {

    /** このクラスを使用して作成された暗号の接頭文字です。*/
    private static final String ENCRYPT_HEADER = "#encrypt#";

    /** 暗号化に使用するアルゴリズムです。*/
    private static final String ALGORITHM_ENCRYPT = "Blowfish";

    /** ハッシュ化に使用するアルゴリズムです。*/
    private static final String ALGORITHM_HASH = "SHA-256";

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        final String key = "SampleKey";
        final String clearText = "p@ss-word.123_Test+亜";
        System.out.println("平文：" + clearText);

        String cipherText = encrypt(key, clearText);
        System.out.println("暗号文：" + cipherText);

        String decryptText = decrypt(key, cipherText);
        System.out.println("復号した文：" + decryptText);

        System.out.println("ハッシュ化した値：" + toHashValue(decryptText));
    }

    /**
     * 暗号化します。
     * @param clearText 平文
     * @return 暗号文
     */
    public static String encrypt(String key, String clearText) {

        /* 平文 ⇒ 暗号化されたバイト配列 */
        SecretKeySpec sksSpec = new SecretKeySpec(key.getBytes(), ALGORITHM_ENCRYPT);

        Cipher cipher;
        byte[] cipherBytes;
        try {
            cipher = Cipher.getInstance(ALGORITHM_ENCRYPT);
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, sksSpec);
            cipherBytes = cipher.doFinal(clearText.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        byte[] base64CipherBytes = Base64.getEncoder().encode(cipherBytes);

        /* 暗号化されたバイト配列 → 暗号文 */
        String base64CipherString = new String(base64CipherBytes, StandardCharsets.UTF_8);
        base64CipherString = ENCRYPT_HEADER + base64CipherString; // 接頭文字を付与

        return base64CipherString;
    }

    /**
     * 復号します。
     * @param cipherText 暗号化された文字列（接頭文字を含む）
     * @return 復号済みの文
     */
    public static String decrypt(String key, String cipherText) {
        if (StringUtils.startsWith(cipherText, ENCRYPT_HEADER) == false) {
            //throw new RuntimeException("このプログラムでは復号できません。");
            return "";
        }
        cipherText = cipherText.replace(ENCRYPT_HEADER, ""); // 窃盗文字を除去

        /* 暗号化された文字列 ⇒ 暗号化されたバイト配列 */
        byte[] base64CipherBytes = Base64.getDecoder().decode(cipherText);

        /* 暗号化されたバイト配列 ⇒ 平文 */
        SecretKeySpec sksSpec = new SecretKeySpec(key.getBytes(), ALGORITHM_ENCRYPT);
        String clearText;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_ENCRYPT);
            cipher.init(Cipher.DECRYPT_MODE, sksSpec);
            clearText = new String(cipher.doFinal(base64CipherBytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return clearText;
    }

    /**
     * テキストをハッシュ化します。
     * @param text ハッシュ化するテキスト。
     * @return ハッシュ値。
     */
    public static String toHashValue(String text) {
        MessageDigest md = null;
        StringBuilder sb = null;
        try {
            md = MessageDigest.getInstance(ALGORITHM_HASH);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(text.getBytes());
        sb = new StringBuilder();
        for (byte b : md.digest()) {
            String hex = String.format("%02x", b);
            sb.append(hex);
        }
        return sb.toString();
    }
}
