package com.github.fujiyamakazan.zabuton.util.security;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.wicket.model.Model;

import com.github.fujiyamakazan.zabuton.security.CipherUtils;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.KeyValue;
import com.github.fujiyamakazan.zabuton.util.StringSeparator;
import com.github.fujiyamakazan.zabuton.util.jframe.JPage;
import com.github.fujiyamakazan.zabuton.util.jframe.JPageAction;
import com.github.fujiyamakazan.zabuton.util.jframe.JPageApplication;
import com.github.fujiyamakazan.zabuton.util.jframe.JPageChangeAction;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageButton;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageCheckBox;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageLabel;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageLink;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPagePassword;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageTextField;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public class PasswordManager extends JPageApplication {
    private static final long serialVersionUID = 1L;

    /**
     * 開発中の動作確認をします。
     */
    public static void main(String[] args) {

        String url = "http://www.example.com/test";

        PasswordManager pm = new PasswordManager("SampleApp");
        pm.executeByUrl(url);

        String id = pm.getId();
        String pw = pm.getPassword();

        System.out.println("id=" + id);
        System.out.println("pw=" + pw);

    }

    //private String url;
    private String sightKey;

    private final Model<String> modelId = Model.of("");
    private final Model<String> modelPw = Model.of("");
    private final File saveDir;
    private MainPage mainPage;
    private boolean autoLogin;

    /**
     * コンストラクタです。
     */
    public PasswordManager(String appId) {
        this.saveDir = new File(EnvUtils.getAppData(appId), "PasswordManager");
        if (saveDir.exists() == false) {
            saveDir.mkdirs();
        }
    }

    /**
     * コンストラクタです。
     */
    public PasswordManager(File appDir) {
        this.saveDir = new File(appDir, "PasswordManager");
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

    public void setIsAutoLogin(boolean autoLogin) {
        this.autoLogin = autoLogin;
    }

    /**
     * 主処理を実行します。
     */
    public void executeBySightKey(String sightKey) {
        this.sightKey = sightKey;

        this.mainPage = new MainPage();
        invokePage(this.mainPage);

    }

    public void executeByUrl(String url) {
        try {
            this.sightKey = new URI(url).getRawAuthority();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        this.mainPage = new MainPage();
        invokePage(this.mainPage);

    }

    private final class MainPage extends JPage {
        private static final long serialVersionUID = 1L;

        private JPageButton saveButton;

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
            final JPageAction doSave = new JPageAction() {
                private static final long serialVersionUID = 1L;

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
            final JPageAction doLink = new JPageChangeAction(PasswordManager.this, MainPage.this, new ListPage());

            addLine(new JPageLabel("[" + sightKey + "]のIDとパスワードを入力してください。"));
            addLine(new JPageTextField("ID", modelId));
            addLine(new JPagePassword("PW", modelPw));
            saveButton = new JPageButton("OK", doSave);
            addLine(saveButton, new JPageCheckBox("端末に保存", modelSave),
                new JPageLink("保存されているパスワードを整理する", doLink));
        }

        @Override
        protected void onAfterShow() {
            super.onAfterShow();
            //
            //            /* 自動的にボタンを押す */
            ////            Model<Boolean> cancel = Model.of(false);
            ////            JPageChoice choice = new JPageChoice("3秒後にログインします。", cancel);
            ////            choice.addChoice("中止", cancel);
            ////            choice.showg();
            //            TheadsSleep.sleep(3 * 1000);
            ////            choice.close();
            ////            if (cancel.getObject() == false) {
            //            saveButton.doClick();
            ////            }

        }

    }

    private final class ListPage extends JPage {

        /** serialVersionUID */
        private static final long serialVersionUID = 1L;

        @Override
        protected void onInitialize() {
            super.onInitialize();

            final JPageAction doDelete = new JPageAction() {
                /** serialVersionUID */
                private static final long serialVersionUID = 1L;

                @Override
                public void run() {
                    for (File f : saveDir.listFiles()) {
                        f.delete();
                    }
                }
            };
            final JPageAction doLink = new JPageChangeAction(PasswordManager.this, ListPage.this, mainPage);

            addLine(new JPageLabel("このパソコンには" + saveDir.listFiles().length + "件のパスワードが保存されています。"));
            addLine(new JPageButton("削除", doDelete), new JPageLink("戻る", doLink));
        }

    }

}
