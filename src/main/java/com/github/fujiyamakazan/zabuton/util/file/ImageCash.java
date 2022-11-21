package com.github.fujiyamakazan.zabuton.util.file;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import com.github.fujiyamakazan.zabuton.util.EnvUtils;

/**
 * 画像ファイルのHIGHTとWIDTHを高速に取得する仕組みです。
 *
 * 初回の利用では、コンストラクタで縦横情報をテキストファイルに書き出します。
 * 既にテキストファイルが存在する状態で呼び出すと、画像の解析を省略して、
 * 画像の縦横情報を返すようになります。
 *
 * 画像を保存するときにあらかじめ一度利用しておくと、初回から高速になります。
 */
public class ImageCash {

    private String info;

    /**
     * 高さを返します。
     */
    public Integer getHeight() {
        if (info == null) {
            return null;
        }
        return Integer.parseInt(info.split(",")[1]);
    }

    /**
     * 幅を返します。
     */
    public Integer getWidth() {
        if (info == null) {
            return null;
        }
        return Integer.parseInt(info.split(",")[2]);
    }

    /**
     * キャッシュをつくるコンストラクタです。
     */
    public ImageCash(File f) throws IOException {

        /*
         * 画像ファイルの情報を保存するテキストファイルを指定します。
         */
        String dir = EnvUtils.getAppData() + "/" + f.getParentFile().getAbsolutePath().replaceAll(":", "/");
        try {
            FileUtils.forceMkdir(new File(dir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final File infoFile = new File(dir, f.getName() + ".imageCash.txt");

        if (infoFile.exists()) {
            /* テキストファイルがあれば、画像ファイルの情報を取得します。 */
            try {
                info = FileUtils.readFileToString(infoFile, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            long metaLast = Long.parseLong(info.split(",")[0]);
            if (metaLast != f.lastModified()) {
                infoFile.delete(); /* 更新日付が異なれば削除します。 */
            }
        }

        if (infoFile.exists() == false) {
            /* テキストファイルがなければ、画像ファイルの情報を保存します。 */
            BufferedImage img = ImageIO.read(f);
            if (img == null) {
                return;
            }
            info = f.lastModified() + "," + img.getHeight() + "," + img.getWidth();
            try {
                FileUtils.write(infoFile, info, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 利用サンプルです。
     *
     * 317ファイル(合計 830MB)を処理しました。
     * 初回はキャッシュを作成しながら処理するため112,820[ms]かかりましたが、
     * 2回目は1,016[ms]でｓきた。
     */
    public static void main(String[] args) throws IOException {

        long start = System.currentTimeMillis();
        File dir = new File("C:\\tmp\\img");
        for (File f : FileUtils.listFiles(dir, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter())) {

            ImageCash ic = new ImageCash(f);
            Integer height = ic.getHeight();
            Integer width = ic.getWidth();
            System.out.println(f.getAbsolutePath() + " " + height + "×" + width);

        }
        System.out.println((System.currentTimeMillis() - start) + "[ms]");
    }

}
