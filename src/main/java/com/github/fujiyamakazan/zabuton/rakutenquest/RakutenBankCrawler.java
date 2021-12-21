package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;

import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.text.TextMerger;

public class RakutenBankCrawler extends JournalCrawler {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RakutenBankCrawler.class);

    private final File master = new File(crawlerDir, year + ".csv");

    public RakutenBankCrawler(int year, File appDir) {
        super("Rakuten", year, appDir);
        setMaster(master);
    }


    @Override
    protected void download() {
        /*
         * ダウンロード処理
         */
        String url = "https://fes.rakuten-bank.co.jp/MS/main/RbS?CurrentPageID=START&&COMMAND=LOGIN";
        cmd.get(url);
        cmd.assertTitleContains("ようこそ");

        PasswordManager pm = new PasswordManager(crawlerDir);
        pm.executeBySightKey("rakuten-bank-01");

        cmd.type(By.name("LOGIN:USER_ID"), pm.getId());
        cmd.type(By.name("LOGIN:LOGIN_PASSWORD"), pm.getPassword());
        cmd.clickAndWait(By.partialLinkText("ログイン"));


        final TextMerger textMerger = new StandardMerger(master);

        int roopCounter = -1;
        while (roopCounter < 12) {  // 1年分取得
            roopCounter++;

//            /* 前のループでダウンロードしたファイルを削除します。*/
//            deletePreFile();
//
//            /* 明細をダウンロード */
//            cmd.get("https://www.rakuten-card.co.jp/e-navi/members/statement/index.xhtml?tabNo=" + roopCounter);
//            cmd.assertTitleContains("ご利用明細");
//            cmd.clickAndWait(By.xpath("//a[contains(@class,'stmt-csv-btn')]")); // ダウンロードボタン
//            new DownloadWait().start(); // ファイルダウンロードを待つ
//
//            /* CSVを整形 */
//            File fileOriginal = getDownloadFileOne();
//            List<String> orignalLine = new Utf8Text(fileOriginal).readLines();
//            List<String> tmp = Generics.newArrayList();
//            for (String original : orignalLine) {
//                if (StringUtils.startsWith(original, "\"利用日\"")) { // 見出し行除外
//                    continue;
//                }
//                if (StringUtils.startsWith(original, "\"\",")) { // 追加行
//                    tmp.set(tmp.size() - 1, tmp.get(tmp.size() - 1) + "," + original);
//                    continue;
//                }
//                tmp.add(original);
//            }
//
//            List<String> lines = tmp;

//            if (textMerger.stock(lines) == false) {
//                break;
//            }
        }
        textMerger.flash();
    }




}
