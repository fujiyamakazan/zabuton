package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public abstract class JournalFactory implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JournalFactory.class);

    protected final JournalCrawler crawler;
    protected final int year;

    public JournalFactory(int year, File dir) {
        this.year = year;
        this.crawler = createCrawler(year, dir);
    }

    protected abstract JournalCrawler createCrawler(int year, File dir);

    protected abstract List<Journal> createJournal();

    public List<Journal> execute(List<Journal> existDatas) {

        crawler.doDowoload();

        List<Journal> journals = createJournal();

        /* 仕訳済みを除外する */
        for (Iterator<Journal> iterator = journals.iterator(); iterator.hasNext();) {
            Journal journal = iterator.next();
            for (Journal exist : existDatas) {
                if (StringUtils.equals(exist.getSource(), journal.getSource())
                    && StringUtils.equals(exist.getRowIndex(), journal.getRowIndex())) {
                    log.debug("仕訳済み：" + exist);
                    iterator.remove();
                    break;
                }
            }
        }

        return journals;
    }

}
