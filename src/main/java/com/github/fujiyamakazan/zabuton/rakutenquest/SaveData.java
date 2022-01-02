package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.text.KeyValuesText;
import com.github.fujiyamakazan.zabuton.util.text.SeparateKeyValuesText;
import com.github.fujiyamakazan.zabuton.util.text.Utf8Text;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;

public class SaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SaveData.class);

    private final File dir;

    private List<JournalBook> books = null;
    private JournalBook selectedBook;

    public SaveData(File dir) {
        this.dir = dir;
    }

    public String getName() {
        return FilenameUtils.removeExtension(this.dir.getName());
    }

    public List<String> getItems() {
        List<String> items = Generics.newArrayList();
        items.addAll(getFirstCoumns("assets.txt"));
        items.addAll(getFirstCoumns("dbits.txt"));
        items.addAll(getFirstCoumns("capitals.txt"));
        items.addAll(getFirstCoumns("costs.txt"));
        items.addAll(getFirstCoumns("incomes.txt"));
        return items;
    }

    public List<String> getItemsLeft() {
        List<String> items = Generics.newArrayList();
        items.addAll(getFirstCoumns("assets.txt"));
        items.addAll(getFirstCoumns("dbits.txt"));
        items.addAll(getFirstCoumns("capitals.txt"));
        items.addAll(getFirstCoumns("costs.txt"));
        //        items.addAll(getFirstCoumns("incomes.txt"));
        return items;
    }

    public List<String> getItemsRight() {
        List<String> items = Generics.newArrayList();
        items.addAll(getFirstCoumns("assets.txt"));
        items.addAll(getFirstCoumns("dbits.txt"));
        items.addAll(getFirstCoumns("capitals.txt"));
        //        items.addAll(getFirstCoumns( "costs.txt"));
        items.addAll(getFirstCoumns("incomes.txt"));
        return items;
    }

    public List<String> getActions() {
        return getFirstCoumns("actions.txt");
    }

    public List<String> getSourcies() {
        return getFirstCoumns("sourcies.txt");
    }

    public String getSetting(String key) {
        KeyValuesText text = new SeparateKeyValuesText(new Utf8Text(new File(dir, "settings.txt")));
        return text.get(key);
    }

    public File getDir() {
        return dir;
    }

    public void select(JournalBook book) {
        this.selectedBook = book;
    }

    public JournalBook getBookSelected() {
        return this.selectedBook;
    }


    private List<String> getFirstCoumns(String fileName) {
        CSVParser parser = new CSVParserBuilder().withSeparator('\t').build();
        List<String> list = Generics.newArrayList();
        for (String str : new Utf8Text(new File(dir, fileName)).readLines()) {
            try {
                list.add(parser.parseLine(str)[0]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return list;
    }

    public List<JournalBook> getBooks() {

        final List<JournalBook> list = Generics.newArrayList();
        for (File f : dir.listFiles()) {
            if (f.getName().startsWith("journals") && f.getName().endsWith(".txt")) {
                list.add(new JournalBook(f));
            }
        }
        return list;
    }


}
