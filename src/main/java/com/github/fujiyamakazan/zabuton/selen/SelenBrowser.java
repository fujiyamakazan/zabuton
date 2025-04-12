package com.github.fujiyamakazan.zabuton.selen;

import java.io.File;
import java.io.Serializable;

import org.apache.wicket.model.Model;
import org.openqa.selenium.WebDriver;

import com.github.fujiyamakazan.zabuton.selen.driverfactory.ChoromeDriverFactory;
import com.github.fujiyamakazan.zabuton.util.jframe.JPage;
import com.github.fujiyamakazan.zabuton.util.jframe.JPageAction;
import com.github.fujiyamakazan.zabuton.util.jframe.JPageApplication;
import com.github.fujiyamakazan.zabuton.util.jframe.JPageButtonAction;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageButton;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageTextField;

public class SelenBrowser implements Serializable {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SelenBrowser.class);

    private SelenCommonDriver cmd;
    Model<String> modelUrl = Model.of("https://www.yahoo.co.jp/");

    public static void main(String[] args) {

        new SelenBrowser().execute();
    }

    /**
     * コンストラクタです。
     */
    public SelenBrowser() {
        this.cmd = new ChoromeDriverFactory(new File("C:\\\\tmp"))
            .downloadDir(new File("C:\\tmp"))
            .build();
    }

    private void execute() {
        JPageApplication.start(new JPage() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onInitialize() {
                super.onInitialize();

                addLine(new JPageTextField("URL", modelUrl));
                addLine(new JPageButton("Start", new StartAction()));
                addLine(new JPageButton("END", new EndAction()));
            }

        });
    }

    private class EndAction extends JPageAction {

        private static final long serialVersionUID = 1L;

        @Override
        public void run() {
            super.run();
            cmd.quit();
        }
    }

    private class StartAction extends JPageButtonAction {

        private static final long serialVersionUID = 1L;

        @Override
        public void run() {
            super.run();
            WebDriver driver = cmd.getDriver();
            driver.get(modelUrl.getObject());

        }

    }

}
