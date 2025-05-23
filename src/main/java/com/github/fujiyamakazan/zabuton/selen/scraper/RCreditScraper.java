package com.github.fujiyamakazan.zabuton.selen.scraper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.app.rakutenquest.I_JournalCsvRow;
import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
import com.github.fujiyamakazan.zabuton.selen.SelenDeck;
import com.github.fujiyamakazan.zabuton.selen.scraper.RCreditScraper.RCreditDto;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

public class RCreditScraper extends JournalScraper<RCreditDto> {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

    private static final String CACHE1 = "cache1.html";
    private static final String CACHE2 = "cache2.html";
    private static final String CACHE3 = "cache3.html";

    /**
     * 明細情報のDTOです。
     */
    public static class RCreditDto implements I_JournalCsvRow {
        private static final long serialVersionUID = 1L;

        @CsvBindByName(column = "0.ID")
        private int id;

        @CsvBindByName(column = "1.明細番号")
        private String meisaino;

        @CsvBindByName(column = "2.利用日")
        @CsvDate("yyyy-MM-dd")
        private LocalDate date;

        @CsvBindByName(column = "3.利用店名")
        private String name;

        @CsvBindByName(column = "4.利用者")
        private String type;

        @CsvBindByName(column = "5.支払方法")
        private String payment;

        @CsvBindByName(column = "6.支払金額")
        private int amount;

        @CsvBindByName(column = "7.備考") // TODO 未使用
        private String note;

        public String getNote() {
            return note;
        }

        public void setNote(final String note) {
            this.note = note;
        }

        public int getId() {
            return id;
        }

        public void setId(final int id) {
            this.id = id;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(final LocalDate date) {
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getMeisaino() {
            return meisaino;
        }

        public void setMeisaino(final String meisaino) {
            this.meisaino = meisaino;
        }

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public String getPayment() {
            return payment;
        }

        public void setPayment(final String payment) {
            this.payment = payment;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(final int amount) {
            this.amount = amount;
        }

        @Override
        public String toString() {
            return String.format("[%s]%s %s %s円(明細番号%s)", id, date, name, amount, meisaino);
        }
    }

    //public RCreditScraper(final File work, final File selen) {
    public RCreditScraper(final SelenDeck deck, final File work) {
        //super(work, selen);
        super(deck, work);
        addcache(CACHE1); // 直近
        addcache(CACHE2); // ひと月前
        addcache(CACHE3); // ふた月前
    }

    @Override
    protected SelenCommonDriver createCmd() {
        return SelenCommonDriver.ofEdge();
    }

    @Override
    protected void doDownload(final SelenCommonDriver cmd) {

        /* ログインします。 */
        final PasswordManager pm = createPasswordManager();
        new RCardLogin().login(pm, cmd);

        // カード切替え
        final String type = selectCardType();
        if (StringUtils.isNotEmpty(type)) {
            cmd.choiceByText(By.xpath("//select[@id='cardChangeForm:cardtype']"), type);
        }

        /* ダウンロード */

        final String url = "https://www.rakuten-card.co.jp/e-navi/members/statement/"
            + "index.xhtml"
            + "?tabNo=%s&l-id=enavi_top_info-card_statement";

        cmd.get(String.format(url, "0"));
        saveCache(cmd, CACHE1);

        cmd.get(String.format(url, "1"));
        saveCache(cmd, CACHE2);

        cmd.get(String.format(url, "2"));
        saveCache(cmd, CACHE3);

    }

    protected String selectCardType() {
        return null;
    }

    @Override
    public JournalScraper<RCreditDto> updateMaster(final File masterCsv) {

        int nextId = 1; // IDの初期値

        //        /*
        //         * 旧バージョンのマスターデータ収集
        //         */
        //        class Old001 {
        //            private String id;
        //            private LocalDate date;
        //            private String name;
        //            private int amount;
        //            public void setId(String id) {
        //                this.id = id;
        //            }
        //            public void setDate(LocalDate date) {
        //                this.date = date;
        //            }
        //            public void setName(String name) {
        //                this.name = name;
        //            }
        //            public void setAmount(int amount) {
        //                this.amount = amount;
        //            }
        //            @Override
        //            public String toString() {
        //                return String.format("[%s]%s %s %s円", id, date, name, amount);
        //            }
        //        }
        //        List<Old001> oldDatas = Generics.newArrayList();
        //        final String oldFileName = "master.old001.csv";
        //        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(new File(root, oldFileName)))) {
        //            String[] row;
        //            while ((row = reader.readNext()) != null) {
        //                Old001 t = new Old001();
        //                t.setId(row[0]);
        //                t.setDate(LocalDate.parse(row[1], DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.JAPAN)));
        //                t.setName(row[2]);
        //                t.setAmount(MoneyUtils.toInt(row[5]));
        //                oldDatas.add(t);
        //                int i = Integer.parseInt(t.id);
        //                if (i >= nextId) {
        //                    nextId = (i + 1); // カーソル更新
        //                }
        //            }
        //        } catch (Exception e) {
        //            throw new RuntimeException(e);
        //        }

        /*
         * 保存済みのマスターデータ収集
         */
        final List<String> meisainos = Generics.newArrayList(); // 既知の明細番号
        final List<RCreditDto> meisais = Generics.newArrayList();
        //File masterCsv = new File(root, "master.csv");
        if (masterCsv.exists()) {
            try (Reader reader = new FileReader(masterCsv)) {
                final CsvToBean<RCreditDto> csvToBean = new CsvToBeanBuilder<RCreditDto>(reader)
                    .withType(RCreditDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
                final List<RCreditDto> masters = csvToBean.parse();
                for (final RCreditDto master : masters) {
                    if (master.getId() >= nextId) {
                        nextId = (master.getId() + 1); // カーソル更新
                    }
                    meisais.add(master); // 明細リストに登録
                    meisainos.add(master.getMeisaino()); // 明細番号リストに登録
                }
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        /*
         * 明細データ収集
         */
        for (final File cache : getCaches()) {
            String html;
            try {
                html = Files.readString(Path.of(cache.getAbsolutePath()));
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            final Document doc = Jsoup.parse(html);
            final Elements tbls = doc.select(".stmt-current-payment-list-body .stmt-payment-lists__tbl");
            for (final Element t : tbls) { // テーブル一行のスクレイピング
                //tbls.forEach(t -> {
                final RCreditDto meisai = new RCreditDto();
                final Elements datas = t.select(".stmt-payment-lists__data");
                int i = 0;
                meisai.setDate(
                    LocalDate.parse(datas.get(i++).text(),
                        DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.JAPAN)));
                meisai.setName(datas.get(i++).text());
                meisai.setType(datas.get(i++).text());
                meisai.setPayment(datas.get(i++).text());
                meisai.setAmount(MoneyUtils.toInt(datas.get(i++).text()));

                /* アコーディオンから明細番号取得 */
                final Element accordion = t.nextElementSibling();
                for (final Element accordionSpan : accordion.getElementsByTag("span")) {
                    if (accordionSpan.text().equals("明細番号")) {
                        meisai.setMeisaino(accordionSpan.nextElementSibling().select("span>span").text());
                    }
                }

                /* 保存済みマスターに同一のデータがあれば追加しない */
                if (meisainos.contains(meisai.getMeisaino())) {
                    //LOGGER.debug("明細" + meisai.getMeisaino() + "はmasterに登録済み");
                    continue;
                }

                //                /* 旧バージョンのマスターに同一日付、同一金額のデータがあればメモ書き */
                //                for (Old001 old : oldDatas) {
                //                    if (old.date.equals(meisai.getDate()) && old.amount == meisai.getAmount()) {
                //                        String note = oldFileName + "に日付と金額が同じデータがあります。" + old.toString();
                //                        //LOGGER.warn(note);
                //                        meisai.setNote(note);
                //                    }
                //                }

                /* 明細として登録 */
                meisai.setId(nextId++);
                meisais.add(meisai); // 明細リストに登録
                meisainos.add(meisai.getMeisaino()); // 明細番号リストに登録

            } // テーブル一行のスクレイピング

        }

        /*
         * マスター作成
         */
        if (masterCsv.exists()) {
            try {
                FileUtils.copyFile(masterCsv, new File(masterCsv.getAbsolutePath() + "." + LocalDate.now())); // バックアップ作成
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (FileWriter writer = new FileWriter(masterCsv)) {
            final StatefulBeanToCsv<RCreditDto> beanToCsv = new StatefulBeanToCsvBuilder<RCreditDto>(writer).build();
            beanToCsv.write(meisais);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    @Override
    public int getAsset() {
        int zandaka = 0;

        for (final File cache : getCaches()) {
            String html;
            try {
                html = Files.readString(Path.of(cache.getAbsolutePath()));
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            final Document doc = Jsoup.parse(html);
            int kingaku = 0;
            for (final Element div : doc.getElementsByTag("div")) {
                final String text = div.text();
                if ((StringUtils.startsWith(text, "お支払い金額")
                    || StringUtils.startsWith(text, "お支払い予定金額"))
                    && StringUtils.endsWith(text, "円")) {
                    /* 金額の取得 */
                    final Pattern pattern2 = Pattern.compile("(\\d{1,3}(,\\d{3})*)");
                    final Matcher matcher2 = pattern2.matcher(text);
                    if (matcher2.find()) {
                        final String strKingaku = matcher2.group(1);
                        //LOGGER.debug("金額：" + strKingaku);
                        kingaku = MoneyUtils.toInt(strKingaku);
                    } else {
                        throw new RuntimeException("金額解析失敗：" + text);
                    }
                }
            }
            for (final Element div : doc.getElementsByTag("div")) {
                final String text = div.text();
                if (StringUtils.startsWith(text, "お支払い日")
                    && StringUtils.endsWith(text, ")")) {
                    final Pattern pattern = Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日");
                    final Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        // 抽出した年・月・日を取得
                        final int year = Integer.parseInt(matcher.group(1));
                        final int month = Integer.parseInt(matcher.group(2));
                        final int day = Integer.parseInt(matcher.group(3));
                        final LocalDate extractedDate = LocalDate.of(year, month, day);
                        final LocalDate today = LocalDate.now();

                        // 過去の日付か判定
                        if (extractedDate.isBefore(today)) {
                            //LOGGER.debug("この日付は過去の日付です。" + extractedDate);
                        } else {
                            //LOGGER.debug("この日付は未来または今日です。" + extractedDate);
                            zandaka += kingaku;
                        }
                    } else {
                        throw new RuntimeException("引落し日解析失敗：" + text);
                    }

                }
            }
        }

        //LOGGER.debug("残高：" + zandaka);
        return zandaka;
    }


    public static void main(final String[] args) {
        final File work = EnvUtils.getUserDesktop(RCreditScraper.class.getSimpleName());
        final RCreditScraper scraper = new RCreditScraper(null, work);
        scraper.download();
        scraper.updateMaster(new File(work, "master.csv"));
        LOGGER.debug("" + scraper.getAsset());
    }

}
