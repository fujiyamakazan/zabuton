package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.rakutenquest.JournalCsv.Row;
import com.github.fujiyamakazan.zabuton.util.date.Chronus;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;

public abstract class JournalFactory implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JournalFactory.class);

    protected final JournalCrawler crawler;
    protected final JournalsTerm term;
    protected final String name;

    /**
     * コンストラクタです。
     */
    public JournalFactory(String name, JournalsTerm term, File dir) {
        this.name = name;
        this.term = term;
        this.crawler = createCrawler(dir);
    }

    protected abstract JournalCrawler createCrawler(File dir);

    /**
     * ダウンロードをします。
     */
    public void download(List<Journal> existDatas, List<Journal> templates) {
        this.crawler.downloadOnly();
    }

    /**
     * 仕訳データを作成します。
     */
    public List<Journal> createJurnals(List<Journal> existDatas, List<Journal> templates) {

        this.crawler.downloadAfter();


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

            common(row);

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

    protected void common(Row row) {
        /* 拡張ポイント */
    }

    protected JournalCsv selectMaster() {
        return this.crawler.getMaster();
    }

    //protected abstract Date pickupDate(JournalCsv.Row row);
    protected abstract String pickupStringDate(JournalCsv.Row row);

    protected abstract String pickupDatePattern(JournalCsv.Row row);

    protected abstract String pickupKeywordOnsource(JournalCsv.Row row);

    protected abstract String pickupMemo(JournalCsv.Row row);

    protected abstract String pickupAmount(JournalCsv.Row row);
    //        return row.get("取引日", new SimpleDateFormat(Chronus.POPULAR_JP));
    //    }

    private static void fullupTemplate(List<Journal> templates, List<Journal> journals) {
        if (templates == null) {
            return;
        }
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

                //                if (k.contains("楽天キャッシュ・チャージ")
                //                    && tk.contains("楽天キャッシュ・チャージ")) {
                //                    System.out.println();
                //                }

                final boolean hit;
                if (tk.equals("*")) {
                    hit = true;
                } else if (tk.startsWith("*") && tk.endsWith("*")) {
                    hit = k.contains(tk.substring(1, tk.length() - 1));
                } else if (tk.startsWith("*")) {
                    hit = k.endsWith(tk.substring(1));
                } else if (tk.endsWith("*")) {
                    hit = k.startsWith(tk.substring(0, tk.length() - 1));
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
