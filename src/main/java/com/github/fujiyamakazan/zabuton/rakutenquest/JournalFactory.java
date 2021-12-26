package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.JournalCrawler;

public abstract class JournalFactory implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JournalFactory.class);

    protected final JournalCrawler crawler;
    protected final int year;
    protected final String name;

    public JournalFactory(String name, int year, File dir) {
        this.name = name;
        this.year = year;
        this.crawler = createCrawler(year, dir);
    }

    protected abstract JournalCrawler createCrawler(int year, File dir);

    protected abstract List<Journal> createJournal(List<Journal> templates);

    public List<Journal> execute(List<Journal> existDatas, List<Journal> templates) {

        crawler.download();

        List<Journal> journals = createJournal(templates);

        /* 仕訳済みを除外する */
        for (Iterator<Journal> iterator = journals.iterator(); iterator.hasNext();) {
            Journal journal = iterator.next();
            for (Journal exist : existDatas) {
                if (StringUtils.equals(exist.getSource(), journal.getSource())
                    && StringUtils.equals(exist.getRowIndex(), journal.getRowIndex())) {
                    //log.debug("仕訳済み：" + exist);
                    iterator.remove();
                    break;
                }
            }
        }

        return journals;
    }

    protected void fullupTemplate(List<Journal> templates, List<Journal> journals) {
        for (Journal journal : journals) {
            for (Journal template : templates) {
                if (journal.getSource().equals(template.getSource()) == false) {
                    continue;
                }



                String k = journal.getKeywordOnSource();
                String tk = template.getKeywordOnSource();
                if (tk == null) {
                    tk = "";
                }

                if (k.contains("楽天キャッシュ・チャージ")
                    && tk.contains("楽天キャッシュ・チャージ")) {
                    System.out.println();
                }

                final boolean hit;
                if (tk.equals("*")) {
                    hit = true;
                } else if (tk.startsWith("*") && tk.endsWith("*")) {
                    hit = k.contains(tk.substring(1, tk.length() - 1));
                } else if (tk.startsWith("*")) {
                    hit = k.endsWith(tk.substring(1));
                } else if (tk.endsWith("*")) {
                    hit = k.startsWith(tk.substring(0,tk.length() - 1));
                } else {
                    hit = k.equals(tk);
                }

                if (hit) {
                    journal.setLeft(template.getLeft());
                    journal.setRight((template.getRight()));
                    journal.setActivity(template.getActivity());
                    if (StringUtils.isNotEmpty(template.getMemo())) {
                        if (StringUtils.isNotEmpty(journal.getMemo())) {
                            journal.setMemo(journal.getMemo() + " " + template.getMemo());
                        } else {
                            journal.setMemo(template.getMemo());
                        }
                    }
                    break;
                }
            }
        }
    }

    public final String getName() {
        return this.name;
    }


}
