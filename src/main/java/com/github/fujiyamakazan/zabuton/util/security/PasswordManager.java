package com.github.fujiyamakazan.zabuton.util.security;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.wicket.model.Model;

import com.github.fujiyamakazan.zabuton.security.CipherUtils;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.KeyValue;
import com.github.fujiyamakazan.zabuton.util.StringSeparator;
import com.github.fujiyamakazan.zabuton.util.jframe.JfApplication;
import com.github.fujiyamakazan.zabuton.util.jframe.JfPage;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JicketButton;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JicketCheckBox;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JicketLabel;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JicketLink;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JicketPassword;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JicketText;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public class PasswordManager extends JfApplication {

    /**
     * 開発中の動作確認をします。
     */
    public static void main(String[] args) {

        String url = "http://www.example.com/test";

        PasswordManager pm = new PasswordManager("SampleApp");
        pm.execute(url);

        String id = pm.getId();
        String pw = pm.getPassword();

        System.out.println("id=" + id);
        System.out.println("pw=" + pw);

    }

//    private String url;
    private String sightKey;

    private final Model<String> modelId = Model.of("");
    private final Model<String> modelPw = Model.of("");
    private final File saveDir;
    private MainPage mainPage;

    /**
     * コンストラクタです。
     */
    public PasswordManager(String appId) {
        this.saveDir = new File(EnvUtils.getAppData(appId), "PasswordManager");
        if (saveDir.exists() == false) {
            saveDir.mkdirs();
        }
    }

    public String getId() {
        return modelId.getObject();
    }

    public String getPassword() {
        return modelPw.getObject();
    }

    /**
     * 主処理を実行します。
     */
    public void execute(String sightKey, String url) {
        try {
            this.sightKey = new URI(url).getRawAuthority();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
//        this.url = url;

        this.mainPage = new MainPage();
        invokePage(this.mainPage);

    }

    public void execute(String url) {
        this.sightKey = url;
//        this.url = url;

        this.mainPage = new MainPage();
        invokePage(this.mainPage);

    }

    private final class MainPage extends JfPage {

        @Override
        protected void onInitialize() {
            super.onInitialize();

            /* 保存されているIDとPWを取得 */
            File setting = new File(saveDir, sightKey);
            Utf8Text utf8Text = new Utf8Text(setting);
            if (setting.exists()) {
                String savedText = CipherUtils.decrypt(PasswordManager.class.getSimpleName(), utf8Text.read());
                for (String line : savedText.split("\n")) {
                    KeyValue kv = StringSeparator.sparate(line, '=');
                    if (kv.getKey().equals("id")) {
                        modelId.setObject(kv.getValue());
                    }
                    if (kv.getKey().equals("pw")) {
                        modelPw.setObject(kv.getValue());
                    }
                }
            }

            final Model<Boolean> modelSave = Model.of(true);
            final Runnable doSave = new Runnable() {
                @Override
                public void run() {
                    if (modelSave.getObject()) {
                        StringBuilder data = new StringBuilder();
                        data.append("id=" + modelId.getObject() + "\n");
                        data.append("pw=" + modelPw.getObject() + "\n");
                        /* 暗号化 */
                        String text = CipherUtils.encrypt(PasswordManager.class.getSimpleName(), data.toString());
                        utf8Text.write(text);
                    }
                }
            };
            final Runnable doLink = new Runnable() {
                @Override
                public void run() {
                    changePage(MainPage.this, new ListPage());
                }
            };

            add(new JicketLabel("[" + sightKey + "]のIDとパスワードを入力してください。"));
            add(new JicketText("ID", modelId));
            add(new JicketPassword("PW", modelPw));
            add(new JicketButton("OK", doSave), new JicketCheckBox("端末に保存", modelSave),
                new JicketLink("保存されているパスワードを整理する", doLink));
        }
    }

    private final class ListPage extends JfPage {

        @Override
        protected void onInitialize() {
            super.onInitialize();

            final Runnable doDelete = new Runnable() {
                @Override
                public void run() {
                    for (File f : saveDir.listFiles()) {
                        f.delete();
                    }
                }
            };
            final Runnable doLink = new Runnable() {
                @Override
                public void run() {
                    changePage(ListPage.this, mainPage);
                }
            };

            add(new JicketLabel("このパソコンには" + saveDir.listFiles().length + "件のパスワードが保存されています。"));
            add(new JicketButton("削除", doDelete), new JicketLink("戻る", doLink));
        }

    }

}