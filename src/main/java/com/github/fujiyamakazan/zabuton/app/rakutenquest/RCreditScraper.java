package com.github.fujiyamakazan.zabuton.app.rakutenquest;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
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
import org.openqa.selenium.Dimension;
import org.slf4j.LoggerFactory;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;
import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

public class RCreditScraper {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private File work;
    private String[] fileNames;
    private List<File> caches;

    /**
     * 明細情報のDTOです。
     */
    public static class RCreditDto implements Serializable {

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

        @CsvBindByName(column = "7.備考")
        private String note;

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMeisaino() {
            return meisaino;
        }

        public void setMeisaino(String meisaino) {
            this.meisaino = meisaino;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPayment() {
            return payment;
        }

        public void setPayment(String payment) {
            this.payment = payment;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        @Override
        public String toString() {
            return String.format("[%s]%s %s %s円(明細番号%s)", id, date, name, amount, meisaino);
        }
    }

    public RCreditScraper() {

        work = EnvUtils.getUserDesktop(getClass().getSimpleName());
        LOGGER.debug("mkdir:" + work.mkdir());

        fileNames = new String[] { "tab1.cache.html", "tab2.cache.html" };
        caches = Generics.newArrayList();
        for (String fileName : fileNames) {
            caches.add(new File(work, fileName));
        }
    }

    /**
     * ダウンロード済みのキャッシュがあるかどうか
     */
    public boolean hasCache() {
        boolean hasRecentFile = false;
        File file = new File(work, fileNames[0]);
        if (file.exists()) {
            long creationTime = file.lastModified();
            long currentTime = System.currentTimeMillis();
            long twentyFourHoursInMillis = TimeUnit.HOURS.toMillis(12);
            if (currentTime - creationTime <= twentyFourHoursInMillis) {
                hasRecentFile = true;
            }
        }
        if (hasRecentFile) {
            LOGGER.debug("有効なキャッシュがあります。");
        } else {
            LOGGER.debug("有効なキャッシュがありません。Webから取得する必要があります。");
        }
        return hasRecentFile;
    }

    public void download() {
        SelenCommonDriver cmd = null;
        try {
            /* ログイン */
            cmd = SelenCommonDriver.createEdgeDriver(work);
            cmd.getDriver().manage().window().setSize(new Dimension(150, 400));
            cmd.get(
                "https://login.account.rakuten.com/sso/authorize"
                    + "?client_id=rakuten_card_enavi_web"
                    + "&redirect_uri=https://www.rakuten-card.co.jp/e-navi/auth/login.xhtml"
                    + "&scope=openid%20profile&response_type=code&prompt=login#/sign_in");

            PasswordManager pm = new PasswordManager(work);
            pm.executeBySightKey("rakuten");
            cmd.type(By.name("username"), pm.getId());
            cmd.clickAndWait(By.xpath("(//body//div[text() = '次へ'])[1]"));
            cmd.type(By.name("password"), pm.getPassword());
            cmd.clickAndWait(By.xpath("(//body//div[text() = '次へ'])[2]"));

            // 更にもう一回
            cmd.sleep(3000);
            cmd.type(By.name("password"), pm.getPassword());
            cmd.sleep(3000);
            cmd.clickAndWait(By.xpath("//body//div[text() = '次へ']"));

            // カード切替え
            cmd.choiceByText(By.xpath("//select[@id='cardChangeForm:cardtype']"), "楽天カード（ＪＣＢ）");

            /* ダウンロード */
            final String url = "https://www.rakuten-card.co.jp/e-navi/members/statement/"
                + "index.xhtml"
                + "?tabNo=%s&l-id=enavi_top_info-card_statement";

            try {
                cmd.get(String.format(url, "1"));
                FileUtils.write(caches.get(0), cmd.getPageSource(), StandardCharsets.UTF_8);
                cmd.get(String.format(url, "2"));
                FileUtils.write(caches.get(1), cmd.getPageSource(), StandardCharsets.UTF_8);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            
        } catch (Exception e) {
            throw new RuntimeException(e);
            
        } finally {
            if (cmd != null) {
                cmd.quit();
            }
        }
    }

    public void updateMaster() {

        int nextId = 1; // IDの初期値

        /*
         * 旧バージョンのマスターデータ収集
         */
        class Old001 {
            private String id;
            private LocalDate date;
            private String name;
            private int amount;

            public void setId(String id) {
                this.id = id;
            }

            public void setDate(LocalDate date) {
                this.date = date;
            }

            public void setName(String name) {
                this.name = name;
            }

            public void setAmount(int amount) {
                this.amount = amount;
            }

            @Override
            public String toString() {
                return String.format("[%s]%s %s %s円", id, date, name, amount);
            }

        }
        List<Old001> oldDatas = Generics.newArrayList();
        final String oldFileName = "master.old001.csv";
        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new FileReader(new File(work, oldFileName)))) {
            String[] row;
            while ((row = reader.readNext()) != null) {
                Old001 t = new Old001();
                t.setId(row[0]);
                t.setDate(LocalDate.parse(row[1], DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.JAPAN)));
                t.setName(row[2]);
                t.setAmount(MoneyUtils.toInt(row[5]));
                oldDatas.add(t);
                int i = Integer.parseInt(t.id);
                if (i >= nextId) {
                    nextId = (i + 1); // カーソル更新
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /*
         * 保存済みのマスターデータ収集
         */
        List<String> meisainos = Generics.newArrayList(); // 既知の明細番号
        List<RCreditDto> meisais = Generics.newArrayList();
        File masterCsv = new File(work, "master.csv");
        if (masterCsv.exists()) {
            try (Reader reader = new FileReader(masterCsv)) {
                CsvToBean<RCreditDto> csvToBean = new CsvToBeanBuilder<RCreditDto>(reader)
                    .withType(RCreditDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
                List<RCreditDto> masters = csvToBean.parse();
                for (RCreditDto master : masters) {
                    if (master.getId() >= nextId) {
                        nextId = (master.getId() + 1); // カーソル更新
                    }
                    meisais.add(master); // 明細リストに登録
                    meisainos.add(master.getMeisaino()); // 明細番号リストに登録
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /*
         * 明細データ収集
         */
        for (File cache : caches) {
            String html;
            try {
                html = Files.readString(Path.of(cache.getAbsolutePath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Document doc = Jsoup.parse(html);
            Elements tbls = doc.select(".stmt-current-payment-list-body .stmt-payment-lists__tbl");
            for (Element t : tbls) { // テーブル一行のスクレイピング
                //tbls.forEach(t -> { 
                RCreditDto meisai = new RCreditDto();
                Elements datas = t.select(".stmt-payment-lists__data");
                int i = 0;
                meisai.setDate(
                    LocalDate.parse(datas.get(i++).text(),
                        DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.JAPAN)));
                meisai.setName(datas.get(i++).text());
                meisai.setType(datas.get(i++).text());
                meisai.setPayment(datas.get(i++).text());
                meisai.setAmount(MoneyUtils.toInt(datas.get(i++).text()));

                /* アコーディオンから明細番号取得 */
                Element accordion = t.nextElementSibling();
                for (Element accordionSpan : accordion.getElementsByTag("span")) {
                    if (accordionSpan.text().equals("明細番号")) {
                        meisai.setMeisaino(accordionSpan.nextElementSibling().select("span>span").text());
                    }
                }

                /* 保存済みマスターに同一のデータがあれば追加しない */
                if (meisainos.contains(meisai.getMeisaino())) {
                    LOGGER.debug("明細" + meisai.getMeisaino() + "はmasterに登録済み");
                    continue;
                }

                /* 旧バージョンのマスターに同一日付、同一金額のデータがあればメモ書き */
                for (Old001 old : oldDatas) {
                    if (old.date.equals(meisai.getDate()) && old.amount == meisai.getAmount()) {
                        String note = oldFileName + "に日付と金額が同じデータがあります。" + old.toString();
                        //LOGGER.warn(note);
                        meisai.setNote(note);
                    }
                }

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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (FileWriter writer = new FileWriter(masterCsv)) {
            StatefulBeanToCsv<RCreditDto> beanToCsv = new StatefulBeanToCsvBuilder<RCreditDto>(writer).build();
            beanToCsv.write(meisais);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getAsset() {
        int zandaka = 0;

        for (File cache : caches) {
            String html;
            try {
                html = Files.readString(Path.of(cache.getAbsolutePath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Document doc = Jsoup.parse(html);
            int kingaku = 0;
            for (Element div : doc.getElementsByTag("div")) {
                String text = div.text();
                if (StringUtils.startsWith(text, "お支払い金額")
                    && StringUtils.endsWith(text, "円")) {
                    /* 金額の取得 */
                    Pattern pattern2 = Pattern.compile("(\\d{1,3}(,\\d{3})*)");
                    Matcher matcher2 = pattern2.matcher(text);
                    if (matcher2.find()) {
                        String strKingaku = matcher2.group(1);
                        LOGGER.debug("金額：" + strKingaku);
                        kingaku = MoneyUtils.toInt(strKingaku);
                    } else {
                        throw new RuntimeException("金額解析失敗：" + text);
                    }
                }
            }
            for (Element div : doc.getElementsByTag("div")) {
                String text = div.text();
                if (StringUtils.startsWith(text, "お支払い日")
                    && StringUtils.endsWith(text, ")")) {
                    Pattern pattern = Pattern.compile("(\\d{4})年(\\d{2})月(\\d{2})日");
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        // 抽出した年・月・日を取得
                        int year = Integer.parseInt(matcher.group(1));
                        int month = Integer.parseInt(matcher.group(2));
                        int day = Integer.parseInt(matcher.group(3));
                        LocalDate extractedDate = LocalDate.of(year, month, day);
                        LocalDate today = LocalDate.now();

                        // 過去の日付か判定
                        if (extractedDate.isBefore(today)) {
                            LOGGER.debug("この日付は過去の日付です。" + extractedDate);
                        } else {
                            LOGGER.debug("この日付は未来または今日です。" + extractedDate);
                            zandaka += kingaku;
                        }
                    } else {
                        throw new RuntimeException("引落し日解析失敗：" + text);
                    }

                }
            }
        }

        LOGGER.debug("残高：" + zandaka);
        return zandaka;
    }

    public static void main(String[] args) throws IOException {
        RCreditScraper scraper = new RCreditScraper();
        if (!scraper.hasCache()) {
            scraper.download();
        }
        scraper.updateMaster();
        scraper.getAsset();

    }
}
