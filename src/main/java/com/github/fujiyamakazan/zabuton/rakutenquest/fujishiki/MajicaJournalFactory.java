package com.github.fujiyamakazan.zabuton.rakutenquest.fujishiki;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.rakutenquest.Journal;
import com.github.fujiyamakazan.zabuton.rakutenquest.JournalCsv;
import com.github.fujiyamakazan.zabuton.rakutenquest.MajicaCrawler;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;
import com.ibm.icu.text.SimpleDateFormat;
import com.opencsv.CSVParser;

public class MajicaJournalFactory implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MajicaJournalFactory.class);

    public static void main(String[] args) throws IOException, ParseException {

        File appDir = new File(EnvUtils.getUserDesktop(), "RakutenQuest3");
        MajicaCrawler crawler = new MajicaCrawler(2021, appDir);
        JournalCsv master = crawler.getMaster();

        List<Journal> journals = Generics.newArrayList();

        Utf8Text text = new Utf8Text(master.getFile());
        int rowIndex = 0;
        for (String line : text.readLines()) {
            //System.out.println(line);
            rowIndex ++;

            String[] csv = new CSVParser().parseLine(line);

            Journal journal = new Journal();
            journals.add(journal);
            journal.setRowIndex(String.valueOf(rowIndex));
            journal.setRawOnSource(line);
            journal.setSource("マジカ明細");

            journal.setDate(new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(csv[0]));
            journal.setMemo(csv[1]);

            String in = csv[2];
            String out = csv[3];
            if (StringUtils.isNotEmpty(in)) {
                if (StringUtils.isNotEmpty(out)) {
                    throw new RuntimeException();
                }

                journal.setAmount(MoneyUtils.toInt(in));
                journal.setLeft("マジカ");
                journal.setRight("チャージ金");
                journal.setActivity("両替");

            } else if (StringUtils.isNotEmpty(out)) {
                if (StringUtils.isNotEmpty(in)) {
                    throw new RuntimeException();
                }

                journal.setAmount(MoneyUtils.toInt(out));
                journal.setLeft("費用");
                journal.setRight("マジカ");
                journal.setActivity("消費");

            } else {
                throw new RuntimeException();
            }

            System.out.println(journal.getJournalString());

        }

    }
}
