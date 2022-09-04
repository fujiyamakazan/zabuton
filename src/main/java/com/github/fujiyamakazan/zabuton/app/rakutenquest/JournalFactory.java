package com.github.fujiyamakazan.zabuton.app.rakutenquest;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.date.Chronus;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;

public abstract class JournalFactory implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JournalFactory.class);

    protected final JournalCrawler crawler;
    protected final JournalsTerm term;
    protected final String name;

    /**
     * コンストラクタです。
     * クローラーの作成をします。
     */
    public JournalFactory(String name, JournalsTerm term, File dir) {
        this.name = name;
        this.term = term;
        this.crawler = createCrawler(dir);
    }

    protected abstract JournalCrawler createCrawler(File dir);

    /**
     * クローラーを使って明細の元となるデータをダウンロードをします。
     */
    public void download() {
        this.crawler.download();
    }

    /**
     * 仕訳データを作成します。
     */
    public List<Journal> createJurnals(List<Journal> existDatas, List<Journal> templates) {

        this.crawler.prepareForCreateJurnals();

        List<Journal> journals = Generics.newArrayList();
        for (JournalCsv.Row row : selectMaster().getRrows()) {

            Journal journal = new Journal();
            journal.setSource(this.name);
            journal.setRawOnSource(row.getData());
            journal.setRowIndex(String.valueOf(row.getIndex()));

            //Date date = pickupDate(row);
            String strDate = pickupStringDate(row);
            String pattern = pickupDatePattern(row);

            /* "yyyy/"を付与 */
            if (StringUtils.length(strDate) == 5) {
                strDate = new SimpleDateFormat("yyyy").format(new Date()) + "/" + strDate;
                pattern = "yyyy/" + pattern;
            }

            if (this.term.in(strDate, pattern) == false) {
                continue;
            }

            //common(row);

            journal.setDate(Chronus.parse(strDate, pattern));
            journal.setKeywordOnSource(pickupKeywordOnsource(row));
            journal.setMemo(pickupMemo(row));
            journal.setAmount(MoneyUtils.toInt(pickupAmount(row)));

            journals.add(journal);

        }

        /* テンプレート適用 */
        fullupTemplate(templates, journals);

        /* 仕訳済みを除外する */
        for (Iterator<Journal> iterator = journals.iterator(); iterator.hasNext();) {
            Journal journal = iterator.next();
            if (existDatas != null) {
                for (Journal exist : existDatas) {
                    if (StringUtils.equals(exist.getSource(), journal.getSource())
                        && StringUtils.equals(exist.getRowIndex(), journal.getRowIndex())) {
                        //log.debug("仕訳済み：" + exist);
                        iterator.remove();
                        break;
                    }
                }
            }
        }

        return journals;
    }

    private static void fullupTemplate(List<Journal> templates, List<Journal> journals) {
        if (templates == null) {
            return;
        }
        for (Journal journal : journals) {
            for (Journal template : templates) {
                if (journal.getSource().equals(template.getSource()) == false) {
                    continue;
                }
                String source = journal.getKeywordOnSource();
                String templateSource = template.getKeywordOnSource();
                if (templateSource == null) {
                    templateSource = "";
                }

                final boolean hit;
                if (templateSource.equals("*")) {
                    hit = true;
                } else if (templateSource.startsWith("*") && templateSource.endsWith("*")) {
                    hit = source.contains(templateSource.substring(1, templateSource.length() - 1));
                } else if (templateSource.startsWith("*")) {
                    hit = source.endsWith(templateSource.substring(1));
                } else if (templateSource.endsWith("*")) {
                    hit = source.startsWith(templateSource.substring(0, templateSource.length() - 1));
                } else {
                    hit = source.equals(templateSource);
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

    protected JournalCsv selectMaster() {
        return this.crawler.getMaster();
    }

    protected abstract String pickupStringDate(JournalCsv.Row row);

    protected abstract String pickupDatePattern(JournalCsv.Row row);

    protected abstract String pickupKeywordOnsource(JournalCsv.Row row);

    protected abstract String pickupMemo(JournalCsv.Row row);

    protected abstract String pickupAmount(JournalCsv.Row row);



    public final String getName() {
        return this.name;
    }

    protected boolean isEmptyAmount(String str) {
        if (StringUtils.isEmpty(str)) {
            return true;
        }
        return MoneyUtils.toInt(str) <= 0;
    }

    protected boolean isNotEmptyAmount(String str) {
        return isEmptyAmount(str) == false;
    }

}
