package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.ListToStringer;
import com.github.fujiyamakazan.zabuton.util.date.Chronus;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

public class JournalBook implements Serializable {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JournalBook.class);

    private File file;
    private List<Journal> journals = null;

    public JournalBook(File file) {
        this.file = file;
    }

    public String getName() {
        return file.getName().substring("journals".length(), file.getName().length() - ".txt".length());
    }

    public static JournalBook create(File dir, String termPattern) {
        JournalsTerm term = new JournalsTerm(Integer.parseInt(termPattern));
        File f = new File(dir, "journals" + term.getName() + ".txt");
        String head = "日付\t金額\t借方科目\t貸方科目\tメモ\tメモ2\t活動科目\t記録元\t#";
        new Utf8Text(f).write(head);
        return new JournalBook(f);
    }

    public void addJournal(Journal journal) {
        if (journals == null) {
            loadJournals();
        }
        journals.add(journal);
    }

    public Journal getJournalLast() {
        if (journals == null) {
            loadJournals();
        }
        return journals.get(journals.size() - 1);
    }

    private void loadJournals() {
        journals = Generics.newArrayList();
        if (this.file.exists()) {
            FileReader fileReader;
            CSVReader csvReader;
            try {

                fileReader = new FileReader(file);
                CSVParser parser = new CSVParserBuilder().withSeparator('\t').build();
                csvReader = new CSVReaderBuilder(fileReader).withCSVParser(parser).build();

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            String[] nextLine = null;
            try {
                while ((nextLine = csvReader.readNext()) != null) {
                    if (StringUtils.equals(nextLine[0], "日付")) {
                        continue;
                    }
                    if (StringUtils.isEmpty(nextLine[0])) {
                        continue;
                    }
                    Journal journal = new Journal();
                    journal.setDate(Chronus.parse(nextLine[0], Chronus.POPULAR_JP));
                    journal.setAmount(MoneyUtils.toInt(nextLine[1]));
                    journal.setLeft(nextLine[2]);
                    journal.setRight(nextLine[3]);
                    journal.setMemo(nextLine[4]);
                    journal.setMemo2(nextLine[5]);
                    journal.setActivity(nextLine[6]);
                    journal.setSource(nextLine[7]);
                    this.journals.add(journal);
                }
            } catch (Exception e) {
                log.error(ListToStringer.convert(nextLine));
                throw new RuntimeException(e);
            } finally {
                try {
                    csvReader.close();
                    fileReader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    public void save() {

        FileWriter fileWriter = null;
        ICSVWriter csvWriter = null;
        try {
            fileWriter = new FileWriter(file);
            csvWriter = new CSVWriterBuilder(fileWriter).withSeparator('\t').build();

            for (Journal journal : this.journals) {
                csvWriter.writeNext(new String[] {
                    DateFormatUtils.format(journal.getDate(), Chronus.POPULAR_JP),
                    String.valueOf(journal.getAmount()),
                    journal.getLeft(),
                    journal.getRight(),
                    journal.getMemo(),
                    journal.getMemo2(),
                    journal.getActivity(),
                    journal.getSource()
                });
            }
            csvWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                csvWriter.close();
                fileWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
