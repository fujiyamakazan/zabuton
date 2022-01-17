package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.apache.wicket.model.Model;

import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.MajicaCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.RakutenBankCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.RakutenCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.ShonanShinkinCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.UCSCardCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.YahooCardCrawler;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.date.Chronus;
import com.github.fujiyamakazan.zabuton.util.jframe.JPageAct;
import com.github.fujiyamakazan.zabuton.util.jframe.JPageAction;
import com.github.fujiyamakazan.zabuton.util.jframe.JPageApplication;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageButton;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageDelayLabel;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageLabel;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageSelect;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageTextField;
import com.github.fujiyamakazan.zabuton.util.string.MoneyUtils;

/**
 * 本アプリケーションの中核クラスです。
 *<pre>
 *TODO管理
 *</pre>
 *<p>
 *TODO とりあえず食わせみる
 *TODO 決算期対応
 *
 * ユースケース
 * 　UC01：オープニング
 * 　UC02：メインメニュー選択
 *</p>
 * @author k_inaba
 *
 */
public class RakutenQuest implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RakutenQuest.class);

    public static final File APP_DIR = new File(EnvUtils.getUserDesktop(), "RakutenQuest3");
    private final RSounds sound;
    private final SaveDataManager dataMng;

    /**
     * アプリケーションを起動します。
     */
    public static void main(String[] args) {
        new RakutenQuest().execute();
    }

    public RakutenQuest() {
        this.dataMng = new SaveDataManager(APP_DIR);
        this.sound = new RSounds(new File(APP_DIR, "bgm"), true);
    }

    private void execute() {

        /* オープニング。セーブデータ選択 */
        final SaveData saveData = ucOpenning();

        /* 台帳選択 */
        final Model<JournalBook> selectedBook = ucSelectBook(saveData);

        /* 仕訳 */
        ucResistJournal(saveData, selectedBook.getObject());

        // TODO 台帳メンテナンス

    }

    /**
     * UC03 仕訳します。
     * （事前条件）台帳が選択済み
     * @param saveData
     */
    public void ucResistJournal(SaveData saveData, JournalBook book) {
        JPageApplication.start(new RPage() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onInitialize() {
                super.onInitialize();

                // TODO 日付の入力支援をしたい
                // TODO コンボボックスを打鍵可能とし、絞り込み選択できるとよい

                final Model<String> modelDate = Model.of();
                final Model<String> modelAmount = Model.of();
                final Model<String> modelLeft = Model.of();
                final Model<String> modelRight = Model.of();
                final Model<String> modelMemo = Model.of();
                final Model<String> modelAction = Model.of();
                final Model<String> modelSource = Model.of();

                final JPageAction action1 = new JPageAction() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void run() {
                        super.run();
                        actionCommon(book, modelDate, modelAmount, modelLeft, modelRight, modelMemo, modelAction,
                            modelSource);
                    }
                };
                final JPageAction action2 = new JPageAct() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void run() {
                        super.run();

                        actionCommon(book, modelDate, modelAmount, modelLeft, modelRight, modelMemo, modelAction,
                            modelSource);
                    }
                };
                final JPageAction actionPre = new JPageAct() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void run() {
                        super.run();
                        Journal last = book.getJournalLast();
                        modelAmount.setObject(String.valueOf(last.getAmount()));
                        modelLeft.setObject(last.getLeft());
                        modelRight.setObject(last.getRight());
                        modelMemo.setObject(last.getMemo());
                        modelAction.setObject(last.getActivity());
                        modelSource.setObject(last.getSource());
                        setTextFromModel();
                    }
                };

                final JPageAction actionIkkatsu = new JPageAct() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void run() {
                        super.run();
                        //JournalsTerm term = new JournalsTerm("2021/01/01～2021/12/31");

                        // TODO 期間内にかかる年度別期間を算出。複数のマスタを作成する。


                        RakutenCrawler rakuten = new RakutenCrawler(APP_DIR);
                        rakuten.download();
                        System.out.println("Rakuten-Card:" + rakuten.getAssetRakutenCredit());
                        System.out.println("Rakuten-Point:" + rakuten.getAssetRakutenPoint());

                        UCSCardCrawler ucs = new UCSCardCrawler(APP_DIR);
                        ucs.download();
                        System.out.println("UCS-Card:" + ucs.getAssetUCSCredit());

                        YahooCardCrawler yahoo = new YahooCardCrawler(APP_DIR);
                        yahoo.download();
                        System.out.println("Yahoo-Card:" + yahoo.getAssetYahooCredit());

                        ShonanShinkinCrawler shonan = new ShonanShinkinCrawler(APP_DIR);
                        shonan.download();
                        System.out.println("Shonan:" + shonan.getAssetShonanShinkin());

                        MajicaCrawler majica = new MajicaCrawler(APP_DIR);
                        majica.download();
                        System.out.println("Majica:" + majica.getAssetMajicaMoney());

                        RakutenBankCrawler bank = new RakutenBankCrawler(APP_DIR);
                        majica.download();
                        System.out.println("Rakuten-Bank:" + bank.getAssetBank());
                        System.out.println("Rakuten-Securities:" + bank.getAssetSecurities());
                    }
                };

                addLine(new JPageDelayLabel("ジャーナルを食わせてください") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void sound() {
                        super.sound();
                        sound.soundClick();
                    }
                });
                addLine(new JPageButton("<<", actionPre), new JPageButton("一括登録", actionIkkatsu));
                addLine(new JPageTextField("日付", modelDate, 10));
                addLine(new JPageTextField("金額", modelAmount));
                addLine(new JPageSelect("借方", modelLeft, saveData.getItemsLeft()),
                    new JPageSelect("貸方", modelRight, saveData.getItemsRight()));
                addLine(new JPageTextField("メモ", modelMemo));
                addLine(new JPageSelect("アクション", modelAction, saveData.getActions()));
                addLine(new JPageSelect("ソース", modelSource, saveData.getSourcies()));
                addLine(new JPageButton("登録", action1),
                    new JPageButton("連続登録", action2));
            }

            private void actionCommon(JournalBook book,  final Model<String> modelDate,
                final Model<String> modelAmount, final Model<String> modelLeft, final Model<String> modelRight,
                final Model<String> modelMemo, final Model<String> modelAction,
                final Model<String> modelSource) {
                Journal journal = new Journal();
                journal.setDate(Chronus.parse(modelDate.getObject(), Chronus.POPULAR_JP));
                journal.setAmount(MoneyUtils.toInt(modelAmount.getObject()));
                journal.setLeft(modelLeft.getObject());
                journal.setRight(modelRight.getObject());
                journal.setMemo(modelMemo.getObject());
                journal.setActivity(modelAction.getObject());
                journal.setSource(modelSource.getObject());

                book.addJournal(journal);

                // TODO 入力チェックが欲しい
                book.save();
                sound.soundKettei();
            }
        });
    }

    /**
     * UC02 台帳を選択します。
     * （事前条件）セーブデータが選択済み
     */
    public Model<JournalBook> ucSelectBook(final SaveData saveData) {
        List<JournalBook> books = saveData.getBooks();
        GameWindow<JournalBook> gw = new GameWindow<JournalBook>(sound);
        gw.setMessage("期間を選択してください。");
        for (JournalBook book : books) {
            gw.addChoice(book.getName(), book);
        }
        gw.addChoice("あたらしくつくる", null);
        gw.show();
        Model<JournalBook> selectedBook = new Model<JournalBook>(gw.getSelected());

        if (selectedBook.getObject() == null) {

            JPageApplication.start(new RPage() {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onInitialize() {
                    super.onInitialize();
                    final Model<String> modelTerm = Model.of();
                    addLine(new JPageTextField("期間", modelTerm));
                    addLine(new JPageButton("作成", new JPageAction() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void run() {
                            super.run();
                            JournalBook book = JournalBook.create(saveData.getDir(), modelTerm.getObject());
                            selectedBook.setObject(book);
                        }
                    }));
                }
            });
        }
        saveData.select(selectedBook.getObject());
        return selectedBook;
    }

    /**
     * UC01 オープニングを実行します。
     * 　データを選択する。
     * 　データを新規作成する。
     * （事後条件）データ確定
     * @return
     */
    public SaveData ucOpenning() {

        new RPage() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onInitialize() {
                super.onInitialize();
                addLine(new JPageLabel("ジャーナルクエスト III"));
            }
        }.toast(1);

        final Model<SaveData> selectedData = new Model<SaveData>();
        List<SaveData> datas = dataMng.getDatas();
        GameWindow<SaveData> gw = new GameWindow<SaveData>(sound);
        gw.setMessage("せんたくしてください。");
        for (SaveData sd : datas) {
            gw.addChoice(sd.getName(), sd);
        }
        gw.addChoice("あたらしくつくる", null);
        gw.show();
        selectedData.setObject(gw.getSelected());

        if (selectedData.getObject() == null) {
            log.debug("データ作成処理へ");

            JPageApplication.start(new RPage() {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onInitialize() {
                    super.onInitialize();
                    final Model<String> modelName = Model.of();
                    addLine(new JPageTextField("名前", modelName));
                    addLine(new JPageButton("作成", new JPageAction() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void run() {
                            super.run();
                            selectedData.setObject(dataMng.createSaveData(modelName.getObject()));
                        }
                    }));
                }
            });
        }
        return selectedData.getObject();
    }




}
