package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.util.CsvUtils;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.StringBuilderLn;
import com.github.fujiyamakazan.zabuton.util.exec.RuntimeExc;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.text.TextMerger;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;

public abstract class RakutenQuest implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RakutenQuest.class);

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        File appDir = new File(EnvUtils.getUserDesktop(), "RakutenQuest3");
        if (appDir.exists() == false) {
            appDir.mkdirs();
        }

        final int year = 2021;

        final List<JournalCrawler> crawlers = Generics.newArrayList();
        crawlers.add(new RakutenJournalCrawler(year, appDir));

        JournalCrawler customCrawler = new JournalCrawler("Majika", year, appDir) {

            private static final long serialVersionUID = 1L;

            private final File masterFile = new File(crawlerDir, year + ".csv");

            @Override
            protected void download() {

                /*
                 * ログイン
                 */
                String url = "https://www.giftcard.ne.jp/majica/carduser/";
                cmd.get(url);
                cmd.assertTitleContains("ログイン");

                PasswordManager pm = new PasswordManager(crawlerDir);
                pm.executeByUrl(url);

                cmd.type(By.name("cardNo"), pm.getId());
                cmd.type(By.name("pinOrPassword"), pm.getPassword());
                cmd.clickButtonAndWait("ログイン");

                final TextMerger textMerger = new StandardMerger(masterFile);

                int roopCounter = 0;
                while (roopCounter < 1) { // 1回のみ
                    roopCounter++;

                    /* 前のループでダウンロードしたファイルを削除します。*/
                    deletePreFile();

                    /* 明細を表示 */
                    cmd.get("https://www.giftcard.ne.jp/majica/carduser/ReferPage/open.do");
                    cmd.assertTitleContains("履歴照会");

                    /* HTMLを保存 */
                    String html = this.cmd.getPageSource();
                    saveDaily("html.txt", html);

                    /* CSV形式に変換 */
                    List<String> lines = Generics.newArrayList();
                    Document doc = Jsoup.parse(html);
                    Element table = doc.getElementsByClass("tline").first().getElementsByTag("table").first();
                    for (Element tr : table.getElementsByTag("tr")) {
                        String date = tr.getElementsByTag("td").get(0).text();
                        String body = tr.getElementsByTag("td").get(1).text();
                        String in = tr.getElementsByTag("td").get(2).text();
                        String out = tr.getElementsByTag("td").get(3).text();

                        /* 日付の降順となるように前に追加 */
                        lines.add(0, CsvUtils.convertString(new String[] { date, body, in, out }));
                    }

                    if (textMerger.stock(lines) == false) {
                        break;
                    }
                }
                textMerger.flash();

            }

            @Override
            protected List<Journal> createJournal() {

                List<Journal> results = Generics.newArrayList();
                int rowIndex = 0;
                for (String line : new Utf8Text(masterFile).readLines()) {
                    rowIndex++;

                    String[] csv = CsvUtils.splitCsv(line);
                    String dateTime = csv[0].trim();
                    final String body = csv[1].trim();
                    String inAmount = csv[2].trim();
                    String outAmount = csv[3].trim();

                    if (StringUtils.contains(body, "かんたんチャージ")) {
                        /* チャージはここで扱わない */
                        continue;

                    }

                    Date date;
                    try {
                        date = new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(dateTime);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                    String amount;
                    if (StringUtils.isNotEmpty(inAmount) && StringUtils.isEmpty(outAmount)) {
                        amount = inAmount;
                    } else if (StringUtils.isEmpty(inAmount) && StringUtils.isNotEmpty(outAmount)) {
                        amount = outAmount;
                    } else {
                        throw new RuntimeException();
                    }
                    if (amount.endsWith(" 円")) {
                        amount = amount.substring(0, amount.length() - " 円".length());
                    }
                    amount = amount.replaceAll(",", ""); // 金額のカンマを除去

                    Journal journal = new Journal();
                    journal.setSource("マジカ明細");
                    journal.setRowIndex(String.valueOf(rowIndex));
                    journal.setRawOnSource(line);

                    journal.setDate(date);
                    journal.setAmount(Integer.parseInt(amount));
                    journal.setLeft("★費用★");
                    journal.setRight("マジカ");
                    journal.setActivity("★消費活動★");
                    journal.setMemo(body.trim());
                    results.add(journal);
                }

                return results;
            }
        };

        crawlers.add(customCrawler);

        new RakutenQuest() {
            private static final long serialVersionUID = 1L;

            @Override
            protected File getWorkDir() {
                return appDir;
            }

            @Override
            protected List<Journal> getRecodied() {
                return Generics.newArrayList();
            }

            @Override
            protected List<JournalCrawler> getCrawlers() {
                return crawlers;
            }

        }.execute(year);
    }

    /**
     * 処理を実行します。
     * @param year 処理対象の年を4桁の西暦で示します。
     */
    public void execute(int year) {

        List<Journal> journals = Generics.newArrayList();
        for (JournalCrawler crawler : getCrawlers()) {
            journals.addAll(crawler.execute());

        }

        /* 仕訳済みを除外する */
        if (getRecodied() != null) {
            for (Iterator<Journal> iterator = journals.iterator(); iterator.hasNext();) {
                Journal journal = iterator.next();
                for (Journal exist : getRecodied()) {
                    if (StringUtils.equals(exist.getSource(), journal.getSource())
                            && StringUtils.equals(exist.getRowIndex(), journal.getRowIndex())) {
                        log.debug("仕訳済み：" + exist);
                        iterator.remove();
                        break;
                    }
                }
            }
        }

        /* パターンごとにソート */
        sort(journals);

        StringBuilderLn sb = new StringBuilderLn();
        for (Journal journal : journals) {
            sb.appendLn(journal.getJournalString());
        }

        /* テキストへ書き出す*/
        File resultText = new File(getWorkDir(), "rakuten-quest3.txt");
        new Utf8Text(resultText).write(sb.toString());
        RuntimeExc.execute("notepad", resultText.getAbsolutePath());
    }

    private static void sort(List<Journal> datas) {
        Collections.sort(datas, new Comparator<Journal>() {

            @Override
            public int compare(Journal o1, Journal o2) {
                int compare = 0;
                if (compare == 0) {
                    compare = o1.getSource().compareTo(o2.getSource());
                }
                if (compare == 0) {
                    compare = o1.getActivity().compareTo(o2.getActivity());
                }
                if (compare == 0) {
                    compare = o1.getMemo().compareTo(o2.getMemo());
                }
                if (compare == 0) {
                    compare = o1.getDate().compareTo(o2.getDate());
                }
                return compare;
            }
        });
    }

    protected abstract List<Journal> getRecodied();

    protected abstract List<JournalCrawler> getCrawlers();

    protected abstract File getWorkDir();

}
