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

    private final class MainPage extends JPage {

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
            final JPageAction doLink = new JPageChangeAction(PasswordManager.this, MainPage.this,  new ListPage());

            addLine(new JPageLabel("[" + sightKey + "]のIDとパスワードを入力してください。"));
            addLine(new JPageTextField("ID", modelId));
            addLine(new JPagePassword("PW", modelPw));
            addLine(new JPageButton("OK", doSave), new JPageCheckBox("端末に保存", modelSave),
                new JPageLink("保存されているパスワードを整理する", doLink));
        }
    }

    private final class ListPage extends JPage {

        @Override
        protected void onInitialize() {
            super.onInitialize();

            final JPageAction doDelete = new JPageAction() {
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
