package com.github.fujiyamakazan.zabuton.app.selenbrowser;

import java.io.File;
import java.io.Serializable;

import org.apache.wicket.model.Model;
import org.openqa.selenium.WebDriver;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
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

    public SelenBrowser() {
        this.cmd = new SelenCommonDriver() {

            private static final long serialVersionUID = 1L;

            @Override
            protected File getDownloadDir() {
                File dir = new File("C:\\\\tmp");
                return dir;
            }

            @Override
            protected File getDriverFile() {
                File driverFile = new File("C:\\tmp\\chromedriver.exe");
                return driverFile;
            }
        };
    }

    private void execute() {
        JPageApplication.start(new JPage() {
            private static final long serialVersionUID = 1L;

            private JPageButton okButton = null;

            @Override
            protected void onInitialize() {
                super.onInitialize();

                addLine(new JPageTextField("URL", modelUrl));
                addLine(this.okButton = new JPageButton("Start", new StartAction()));
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
            //cmd.clickAndWait(locator);
//            cmd.originalDriver.findElement(locator).click();
//
//            while (true) {
//
//                //By locator = By.cssSelector("form *[type='submit']");
//
//                WebElement e = cmd.originalDriver.findElement(link);
//                System.out.println("isEnabled:" + e.isEnabled());
//                System.out.println("displayes:" + e.isDisplayed());
//
//
//                Thread.sleep(100);
//
//                cmd.originalDriver.findElement(link).click();
//            }

        }

    }

}
