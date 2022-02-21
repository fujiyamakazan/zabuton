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

@SuppressWarnings("deprecation")
public class RSounds implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RSounds.class);

    private final File appDir;
    private boolean enabel;

    public RSounds(File dir, boolean enabel) {
        this.appDir = dir;
        this.enabel = enabel;
    }

    //    @SuppressWarnings({ "unused" })
    //    private void playmidi() {
    //        if (enabel == false) {
    //            return;
    //        }
    //
    //
    //        //File file = new File(APP_DIR, "bgm\\GM115-110921-youseihouse-wav.wav");
    //        File file = new File(APP_DIR, "se\\kettei-01.wav");
    //
    //        AudioClip ac;
    //        try {
    //            ac = Applet.newAudioClip(file.toURI().toURL());
    //        } catch (MalformedURLException e1) {
    //            throw new RuntimeException(e1);
    //        }
    //
    //        //BGMのループ再生
    //        //ac.loop();
    //
    //        //BGMを一回再生
    //        ac.play();
    //
    //        try {
    //            Thread.sleep(1000 * 30);
    //        } catch (InterruptedException e) {
    //            throw new RuntimeException(e);
    //        }
    //
    //        //BGMを停止
    //        ac.stop();
    //    }

    @SuppressWarnings("unused")
    private void playMidi() {
        if (this.enabel == false) {
            return;
        }

        //        // MIDIデータを再生するハードウェア/ソフトウェア・デバイスのインスタンス。
        Sequencer sequencer = null;
        //
        try {
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
                File file = new File(this.appDir, "魔王魂  フィールド11.mid");
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
        } finally {
            // シーケンサーを閉じ、使用していたリソース解放する。
            sequencer.close();
        }

    }

    /**
     * クリック音を鳴らします。
     */
    public void soundClick() {
        if (this.enabel == false) {
            return;
        }

        //File file = new File(APP_DIR, "bgm\\GM115-110921-youseihouse-wav.wav");
        File file = new File(this.appDir, "se\\決定、ボタン押下2.wav");

        AudioClip ac;
        try {
            ac = Applet.newAudioClip(file.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
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

    /**
     * 決定音を鳴らします。
     */
    public void soundKettei() {
        if (this.enabel == false) {
            return;
        }

        //File file = new File(APP_DIR, "bgm\\GM115-110921-youseihouse-wav.wav");
        File file = new File(this.appDir, "se\\kettei-01.wav");

        AudioClip ac;
        try {
            ac = Applet.newAudioClip(file.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
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

    /**
     * ビープ音を鳴らします。
     */
    public void beep() {
        if (this.enabel == false) {
            return;
        }

        //        try {
        //            TimeUnit.SECONDS.sleep(1);
        //        } catch (InterruptedException e) {
        //            throw new RuntimeException(e);
        //        }
        Toolkit.getDefaultToolkit().beep();
        //System.out.print("\007");
    }
}
