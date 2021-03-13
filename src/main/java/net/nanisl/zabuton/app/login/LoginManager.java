package net.nanisl.zabuton.app.login;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import net.nanisl.zabuton.app.ZabuSession;
import net.nanisl.zabuton.file.TextDataAccess;

@Component
public class LoginManager implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 認証情報のファイル名 */
    private static final String DIGESTS = "digests";

    /** データアクセスオブジェクト */
    private TextDataAccess textDao = new TextDataAccess();

    /**
     * @return ログイン済みのときにTrue
     */
    public boolean isLogin() {
        return getLoginUser() != null;
    }

    /**
     * @return ログイン情報
     */
    public LoginUser getLoginUser() {
        return ZabuSession.get().getLoginUser();
    }

    /**
     * セッションからログイン情報を削除する
     */
    public void logout() {
        ZabuSession.get().setLoginUser(null);
    }

    /**
     * @return 正しいパスワードならTrue
     */
    public boolean inspectPassword(String id, String password) {

        String digest = digest(password);
        Map<String, String> map = textDao.getKeyValues(DIGESTS);
        String value = map.get(id);
        if (value == null) {
            return false; // パスワード未登録
        }
        return StringUtils.equals(digest, value);
    }

    /** セッションにログイン情報を登録する */
    public void login(LoginUser user) {
        ZabuSession.get().setLoginUser(user);
    }

    /** パスワードを変更する */
    public void changePassword(String userId, String password) {
        /* 削除 */
        textDao.removeKeyValue(DIGESTS, userId);
        /* 登録 */
        issuePassword(userId, password);
    }

    /** パスワードを登録する */
    private void issuePassword(String userId, String password) {
        Map<String, String> map = textDao.getKeyValues(DIGESTS);
        if (map.get(password) != null) {
            throw new RuntimeException("パスワード登録済み");
        }
        String digest = digest(password); // パスワードのハッシュ
        textDao.addKeyValue(DIGESTS, userId, digest);
    }

    /**
     * @return ハッシュ化されたパスワード
     */
    private static String digest(String str) {
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] sha256_result = sha256.digest(str.getBytes());
        String result = String.format("%040x", new BigInteger(1, sha256_result));
        return result;
    }

}
