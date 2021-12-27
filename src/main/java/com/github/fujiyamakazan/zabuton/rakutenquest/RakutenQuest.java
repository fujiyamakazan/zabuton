package com.github.fujiyamakazan.zabuton.rakutenquest;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import org.apache.wicket.model.Model;

import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.MajicaCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.RakutenBankCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.RakutenCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.ShonanShinkinCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.UCSCardCrawler;
import com.github.fujiyamakazan.zabuton.rakutenquest.crawler.YahooCardCrawler;
import com.github.fujiyamakazan.zabuton.util.EnvUtils;
import com.github.fujiyamakazan.zabuton.util.jframe.JPage;
import com.github.fujiyamakazan.zabuton.util.jframe.JPageApplication;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageActiveLabel;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageButton;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageComponent;

public abstract class RakutenQuest implements Serializable {

    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RakutenQuest.class);

    public static File APP_DIR = new File(EnvUtils.getUserDesktop(), "RakutenQuest3");
    private static final int YEAR = 2021;

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        if (APP_DIR.exists() == false) {
            APP_DIR.mkdirs();
        }
        Model<Boolean> model = Model.of(false);
        JPageApplication app = new JPageApplication();
        app.invokePage(new JPage() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onInitialize() {
                super.onInitialize();
                addLine(new JPageActiveLabel("メッセージです。あめんぼあかいなあいうえお。"));
                addLine(new JPageButton("OK", model));
            }

            @Override
            protected void onAfterShow() {
                super.onAfterShow();
                for (JPageComponent<?> pc : components) {
                    if (pc instanceof JPageActiveLabel) {
                        ((JPageActiveLabel)pc).atvie();

                    }
                }
            }
        });
        System.out.println(model.getObject());
        //playmidi();
        //playMidi();
        //playJournal();

    }

    private static void playJournal() {
        RakutenCrawler rakuten = new RakutenCrawler(YEAR, APP_DIR);
        rakuten.download();
        System.out.println("Rakuten-Card:" + rakuten.getAssetRakutenCredit());
        System.out.println("Rakuten-Point:" + rakuten.getAssetRakutenPoint());

        UCSCardCrawler ucs = new UCSCardCrawler(YEAR, APP_DIR);
        ucs.download();
        System.out.println("UCS-Card:" + ucs.getAssetUCSCredit());

        YahooCardCrawler yahoo = new YahooCardCrawler(YEAR, APP_DIR);
        yahoo.download();
        System.out.println("Yahoo-Card:" + yahoo.getAssetYahooCredit());

        ShonanShinkinCrawler shonan = new ShonanShinkinCrawler(YEAR, APP_DIR);
        shonan.download();
        System.out.println("Shonan:" + shonan.getAssetShonanShinkin());

        MajicaCrawler majica = new MajicaCrawler(YEAR, APP_DIR);
        majica.download();
        System.out.println("Majica:" + majica.getAssetMajicaMoney());

        RakutenBankCrawler bank = new RakutenBankCrawler(YEAR, APP_DIR);
        majica.download();
        System.out.println("Rakuten-Bank:" + bank.getAssetBank());
        System.out.println("Rakuten-Securities:" + bank.getAssetSecurities());
    }

    private static void playmidi() {
        //File file = new File(APP_DIR, "bgm\\GM115-110921-youseihouse-wav.wav");
        File file = new File(APP_DIR, "bgm\\kettei-01.wav");

        AudioClip ac;
        try {
            ac = Applet.newAudioClip(file.toURI().toURL());
        } catch (MalformedURLException e1) {
            throw new RuntimeException(e1);
        }

        //BGMのループ再生
        //ac.loop();

        //BGMを一回再生
        ac.play();

        try {
            Thread.sleep(1000 * 30);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //BGMを停止
        ac.stop();
    }

    private static void playMidi() {
        //        // MIDIデータを再生するハードウェア/ソフトウェア・デバイスのインスタンス。
        Sequencer sequencer = null;
        //
        try {
            // デバイスに接続されたデフォルトのSequencerを取得する。
            sequencer = MidiSystem.getSequencer();

            // デバイスを開き、リソースを獲得する。
            sequencer.open();
        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }

        //        // コンソール入力からMIDIファイルのパスを取得する。
        //        Scanner scanner = new Scanner(System.in);
        //        System.out.print("MIDIファイルのパス >> ");
        //        String path = scanner.next();

        try {
            // MIDIファイルからMIDIデータ(Sequenceオブジェクト)を取得。
            File file = new File(APP_DIR, "bgm\\魔王魂  フィールド11.mid");
            Sequence sequence = MidiSystem.getSequence(file);

            // 取得したMIDIデータをシーケンサに設定する。
            sequencer.setSequence(sequence);
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // シーケンサー再生
        sequencer.start();

        try {
            Thread.sleep(1000 * 5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //        // キー入力待ち
        //        System.out.println("適当に文字列を入力すると終了します。");
        //        System.out.print(">>");
        //        scanner.next();
        //        scanner.close();

        // シーケンサー停止
        sequencer.stop();

        // シーケンサーを閉じ、使用していたリソース解放する。
        sequencer.close();
    }

    public static void sound() {
        //File file = new File(APP_DIR, "bgm\\GM115-110921-youseihouse-wav.wav");
        File file = new File(APP_DIR, "bgm\\se\\決定、ボタン押下2.wav");

        AudioClip ac;
        try {
            ac = Applet.newAudioClip(file.toURI().toURL());
        } catch (MalformedURLException e1) {
            throw new RuntimeException(e1);
        }

        //BGMのループ再生
        //ac.loop();

        //BGMを一回再生
        ac.play();
//
//        try {
//            Thread.sleep(1000 * 30);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        //BGMを停止
//        ac.stop();
    }

    public static void beep() {
//        try {
//            TimeUnit.SECONDS.sleep(1);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        Toolkit.getDefaultToolkit().beep();
        //System.out.print("\007");
    }

}
