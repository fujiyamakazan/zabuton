package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.text.ShiftJisText;

public class Test {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Test.class);


    public static void main(String[] args) {

        File f = new File(EnvUtils.getUserDesktop(), "Rakuten Bank.html");
        String html = new ShiftJisText(f).read();


        int summary = 0;
        Document doc = Jsoup.parse(html);
        Elements trs = doc.select("tr");
        for (Element tr: trs) {

            String line = tr.text();
            if (line.startsWith("● 利用可能残高 ：")) {
                System.out.println(line);
                //line = line.substring("● 利用可能残高 ：".length());
                //summary = MoneyUtils.toInt(line);
                break;
            }


        }
        log.debug(""+summary);






        //String cash = h2s.getElementsByClass("point_total").first().text();

    }


    private static int toInt(String text) {
        text = text.replaceAll(",", "");
        return Integer.parseInt(text);
    }

}

