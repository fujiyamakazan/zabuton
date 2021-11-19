package com.github.fujiyamakazan.zabuton.util.security;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.wicket.model.Model;

import com.github.fujiyamakazan.zabuton.jicket.JfPage;
import com.github.fujiyamakazan.zabuton.jicket.component.JicketButton;
import com.github.fujiyamakazan.zabuton.jicket.component.JicketCheckBox;
import com.github.fujiyamakazan.zabuton.jicket.component.JicketLabel;
import com.github.fujiyamakazan.zabuton.jicket.component.JicketLink;
import com.github.fujiyamakazan.zabuton.jicket.component.JicketPassword;
import com.github.fujiyamakazan.zabuton.jicket.component.JicketText;
import com.github.fujiyamakazan.zabuton.security.CipherUtils;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.JFrameUtils;
import com.github.fujiyamakazan.zabuton.util.KeyValue;
import com.github.fujiyamakazan.zabuton.util.StringSeparator;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public class PasswordManager {

    public static void main(String[] args) {

        String url = "http://www.example.com/test";

        PasswordManager pm = new PasswordManager("SampleApp");
        pm.execute(url);

        String id = pm.getId();
        String pw = pm.getPassword();

        System.out.println("id=" + id);
        System.out.println("pw=" + pw);

    }

    private String appId;
    private String id;
    private String pw;

    public PasswordManager(String appId) {
        this.appId = appId;
    }

    public void execute(String url) {

        String domain;
        try {
            domain = new URI(url).getRawAuthority();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        File appDir = EnvUtils.getAppData(this.appId);
        if (appDir.exists() == false) {
            appDir.mkdir();
        }

        File setting = new File(appDir, domain);
        String savedId = "";
        String savedPw = "";
        Utf8Text utf8Text = new Utf8Text(setting);
        String savedText = CipherUtils.decrypt(PasswordManager.class.getSimpleName(), utf8Text.read());
        if (setting.exists()) {
            for (String line : savedText.split("\n")) {
                KeyValue kv = StringSeparator.sparate(line, '=');
                if (kv.getKey().equals("id")) {
                    savedId = kv.getValue();
                }
                if (kv.getKey().equals("pw")) {
                    savedPw = kv.getValue();
                }
            }
        }
        final Model<String> modelId = Model.of(savedId);
        final Model<String> modelPw = Model.of(savedPw);
        final Model<Boolean> modelSave = Model.of(true);
        final Runnable doSave = new Runnable() {
            @Override
            public void run() {
                if (modelSave.getObject()) {
                    StringBuilder data = new StringBuilder();
                    data.append("domain=" + domain + "\n");
                    data.append("id=" + modelId.getObject() + "\n");
                    data.append("pw=" + modelPw.getObject() + "\n");
                    String text = CipherUtils.encrypt(PasswordManager.class.getSimpleName(), data.toString()); // 暗号化
                    utf8Text.write(text);
                }
            }
        };
        final Runnable doLink = new Runnable() {
            @Override
            public void run() {
                JFrameUtils.showMessageDialog("開発中です。");
            }
        };

        new JfPage() {
            @Override
            protected void onInitialize() {
                add(new JicketLabel("[" + domain + "]のIDとパスワードを入力してください。"));
                add(new JicketText("ID", modelId));
                add(new JicketPassword("PW", modelPw));
                add(new JicketButton(this, "OK", doSave), new JicketCheckBox("端末に保存", modelSave),
                    new JicketLink("保存されているパスワードを整理する", doLink));
            }
        }.show();

        this.id = modelId.getObject();
        this.pw = modelPw.getObject();

    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return pw;
    }

}
