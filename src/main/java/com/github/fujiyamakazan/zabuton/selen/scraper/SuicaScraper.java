package com.github.fujiyamakazan.zabuton.selen.scraper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;

import com.github.fujiyamakazan.zabuton.selen.SelenCommonDriver;
import com.github.fujiyamakazan.zabuton.selen.scraper.SuicaScraper.SuicaDto;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.date.Chronus;
import com.github.fujiyamakazan.zabuton.util.jframe.JFrameUtils;
import com.github.fujiyamakazan.zabuton.util.jframe.JFrameUtils.JFrameDialogParams;
import com.github.fujiyamakazan.zabuton.util.security.PasswordManager;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;
import com.github.fujiyamakazan.zabuton.util.string.StringCutter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

public class SuicaScraper extends JournalScraper<SuicaDto> {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory
        .getLogger(MethodHandles.lookup().lookupClass());

    private static final String CACHE1 = "page0.cache.html";

    /**
     * 明細情報のDTOです。
     */
    public static class SuicaDto implements Serializable {

        private static final long serialVersionUID = 1L;

        @CsvBindByName(column = "0.ID")
        private int id;

        @CsvBindByName(column = "1.明細番号") // TODO 未使用
        private String meisaino;

        @CsvBindByName(column = "2.利用日")
        @CsvDate("yyyy/MM/dd")
        private LocalDate 月日;

        @CsvBindByName(column = "3.種別")
        private String 種別;

        @CsvBindByName(column = "4.利用場所")
        private String 利用場所;

        @CsvBindByName(column = "5.種別2")
        private String 種別2;

        @CsvBindByName(column = "6.利用場所2")
        private String 利用場所2;

        @CsvBindByName(column = "7.残高")
        private int 残高;

        @CsvBindByName(column = "8.入金利用額")
        private int 入金利用額;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getMeisaino() {
            return meisaino;
        }

        public void setMeisaino(String meisaino) {
            this.meisaino = meisaino;
        }

        public LocalDate get月日() {
            return 月日;
        }

        public void set月日(LocalDate 月日) {
            this.月日 = 月日;
        }

        public String get種別() {
            return 種別;
        }

        public void set種別(String 種別) {
            this.種別 = 種別;
        }

        public String get利用場所() {
            return 利用場所;
        }

        public void set利用場所(String 利用場所) {
            this.利用場所 = 利用場所;
        }

        public String get種別2() {
            return 種別2;
        }

        public void set種別2(String 種別2) {
            this.種別2 = 種別2;
        }

        public String get利用場所2() {
            return 利用場所2;
        }

        public void set利用場所2(String 利用場所2) {
            this.利用場所2 = 利用場所2;
        }

        public int get残高() {
            return 残高;
        }

        public void set残高(int 残高) {
            this.残高 = 残高;
        }

        public int get入金利用額() {
            return 入金利用額;
        }

        public void set入金利用額(int 入金利用額) {
            this.入金利用額 = 入金利用額;
        }

        @Override
        public String toString() {
            return "SuicaDto [id=" + id + ", meisaino=" + meisaino + ", 月日=" + 月日 + ", 種別=" + 種別 + ", 利用場所=" + 利用場所
                + ", 種別2=" + 種別2 + ", 利用場所2=" + 利用場所2 + ", 残高=" + 残高 + ", 入金利用額=" + 入金利用額 + "]";
        }
    }

    public SuicaScraper(final File work, final File selen) {
        super(work, selen);
        addcache(CACHE1);
    }

    @Override
    protected void doDownload(final SelenCommonDriver cmd) {

        /* ログイン */
        //cmd.getDriver().manage().window().setSize(new Dimension(150, 400));
        String url = "https://www.mobilesuica.com/iq/ir/SuicaDisp.aspx";
        cmd.get(url);
        cmd.clickAndWait(By.xpath("//*[text()=\"モバイルSuicaのIDでログイン\"]"));

        PasswordManager pm = createPasswordManager();
        pm.executeByUrl(url);
        cmd.type(By.name("MailAddress"), pm.getId());
        cmd.type(By.name("Password"), pm.getPassword());

        JFrameUtils.showDialog(new JFrameDialogParams().message("Capchaを入力してログインボタンを押してください。")
            .title(getClass().getSimpleName()));
        //cmd.clickAndWait(By.name("LOGIN"));
        cmd.clickLinkPartialAndWait("SF（電子マネー）");

        /* ダウンロード */
        saveCache(cmd, CACHE1);

    }

    @Override
    public SuicaScraper updateMaster(File masterCsv) {

        int nextId = 1; // IDの初期値

        final List<SuicaDto> meisais = Generics.newArrayList();
//        /*
//         * 旧バージョンのマスターデータ収集
//         */
//        List<SuicaDto> oldDatas = Generics.newArrayList();
//        final String oldFileName = "master.old001.csv";
//        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(
//            new FileReader(new File(masterCsv.getParentFile(), oldFileName)))) {
//            String[] row;
//            while ((row = reader.readNext()) != null) {
//                SuicaDto o = new SuicaDto();
//                int i = 0;
//                o.id = Integer.parseInt(row[i++]);
//                String dateStr = row[i++];
//                //LOGGER.debug(dateStr);
//                o.月日 = Chronus.localDateOf(dateStr, "yyyy/MM/dd");
//                o.種別 = row[i++];
//                o.利用場所 = row[i++];
//                o.種別2 = row[i++];
//                o.利用場所2 = row[i++];
//                o.残高 = MoneyUtils.toInt(row[i++]);
//                o.入金利用額 = MoneyUtils.toInt(row[i++]);
//                oldDatas.add(o);
//                if (o.id >= nextId) {
//                    nextId = (o.id + 1); // カーソル更新
//                }
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        /*
         * 保存済みのマスターデータ収集
         */
        if (masterCsv.exists()) {
            try (Reader reader = new FileReader(masterCsv)) {
                CsvToBean<SuicaDto> csvToBean = new CsvToBeanBuilder<SuicaDto>(reader)
                    .withType(SuicaDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
                List<SuicaDto> masters = csvToBean.parse();
                for (SuicaDto master : masters) {
                    if (master.getId() >= nextId) {
                        nextId = (master.getId() + 1); // カーソル更新
                    }
                    meisais.add(master); // 明細リストに登録
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /*
         * 明細データ収集
         */
        File cache = getCache(CACHE1);
        String html;
        try {
            html = Files.readString(Path.of(cache.getAbsolutePath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Document doc = Jsoup.parse(html);
        Elements trs = doc.select(".historyTable tr");
        LocalDate now = LocalDate.now();
        int yearNow = now.getYear();
        int monthNow = now.getMonthValue();

        /* DTOに変換 */
        //List<String> lines = Generics.newArrayList();
        for (Element tr : trs) {
            String line = tr.text();
            if (StringUtils.isEmpty(line)) {
                continue;
            }

            Elements tds = tr.select("td");

            //List<String> row = Generics.newArrayList();
            SuicaDto dto = new SuicaDto();

            /* 月日 */
            String date = tds.get(1).text();
            if (date.equals("月日")) {
                continue; // 見出し行
            }
            String mm = StringCutter.left(date, "/");
            if (Integer.parseInt(mm) > monthNow) {
                date = (yearNow - 1) + "/" + tds.get(1).text();
            } else {
                date = yearNow + "/" + tds.get(1).text();
            }
            //row.add(date);
            dto.set月日(Chronus.localDateOf(date, "yyyy/MM/dd"));

            String td2 = tds.get(2).text();
            //row.add(td2); // 種別
            dto.種別 = td2;
            if (td2.equals("繰")) {
                continue;
            }

            //row.add(tds.get(3).text()); // 利用場所
            dto.利用場所 = tds.get(3).text();
            //row.add(tds.get(4).text()); // 種別
            dto.種別2 = tds.get(4).text();
            //row.add(tds.get(5).text()); // 利用場所
            dto.利用場所2 = tds.get(5).text();
            //row.add(tds.get(6).text()); // 残高
            dto.残高 = MoneyUtils.toInt(tds.get(6).text());
            //row.add(tds.get(7).text()); // 入金・利用額
            dto.入金利用額 = MoneyUtils.toInt(tds.get(7).text());

//            // 旧バージョンのマスターデータに同一データがあればスキップ
//            if (containsWithoutId(oldDatas, dto)) {
//                LOGGER.debug("明細スキップ");
//                continue;
//            }

            // 登録済みのマスターデータに同一データがあればスキップ
            if (containsWithoutId(meisais, dto)) {
                //LOGGER.debug("明細スキップ");
                continue;
            }

            //LOGGER.debug("明細追加");
            dto.setId(nextId++);
            meisais.add(dto);

            ///* 日付の降順となるように前に追加 */ 仕組みが変わったので一旦コメントアウト
            //String str = CsvUtils.convertString(row);
            //lines.add(0, str);

        }

        //meisais.forEach(m -> LOGGER.debug(m.toString()));

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
            StatefulBeanToCsv<SuicaDto> beanToCsv = new StatefulBeanToCsvBuilder<SuicaDto>(writer).build();
            beanToCsv.write(meisais);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * dtoのid以外で比較
     */
    private static boolean containsWithoutId(List<SuicaDto> list, SuicaDto dto) {
        return list.stream().anyMatch(obj1 -> {
            try {
                for (Field field : obj1.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.getName().equals("id"))
                        continue; // IDを無視
                    Object value1 = field.get(obj1);
                    if (value1==null) {
                        value1="";
                    }
                    Object value2 = field.get(dto);
                    if (value2==null) {
                        value2="";
                    }
                    if (value1 == null ? value2 != null : !value1.equals(value2)) {
                        return false;
                    }
                }
                return true;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public int getAsset() {
        int asset = 0;
        File cache = getCache(CACHE1);
        String html;
        try {
            html = Files.readString(Path.of(cache.getAbsolutePath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Document doc = Jsoup.parse(html);
        Elements trs = doc.select(".historyTable tr");
        Outer: for (Element tr : trs) {
            for (Element td : tr.getElementsByTag("td")) {
                if (td.hasClass("MoneyText")) {
                    asset = MoneyUtils.toInt(td.text());
                    break Outer;
                }
            }
        }
        //LOGGER.debug("残高：" + asset);
        return asset;
    }

    public static void main(String[] args) throws IOException {
        String workName = MethodHandles.lookup().lookupClass().getSimpleName() + "Test";
        File work = EnvUtils.getUserDesktop(workName);
        FileUtils.forceMkdir(work);
        new SuicaScraper(work, work)
            .download()
            .updateMaster(new File(work, "master.csv"))
            .getAsset();
    }

    @Override
    protected File getAppDir() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public void execute() {
        // TODO 自動生成されたメソッド・スタブ
        
    }
}
