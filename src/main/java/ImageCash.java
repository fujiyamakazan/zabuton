
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

/**
 * 画像のHIGHTとWIDTHを高速に取得する仕組みです。
 *
 * 初回の利用では、コンストラクタで縦横情報をテキストファイルに書き出します。
 * 既にテキストファイルが存在する状態で呼び出すと、画像の解析を省略して、
 * 画像の縦横情報を返すようになります。
 *
 * 画像を保存するときにあらかじめ一度利用しておくと、初回から高速になります。
 */
public class ImageCash {

    private final File meta;
    private final int height;
    private final int width;

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    /**
     * キャッシュを作成するコンストラクタです。
     */
    public ImageCash(File f) {

        /*
         * 縦横情報を保存するテキストファイルを指定します。
         * TODO 適切な場所の指定
         */

        try {
            File dirMeta = new File(f.getParentFile().getAbsoluteFile() + "_meta");
            FileUtils.forceMkdir(dirMeta);
            meta = new File(dirMeta, f.getName() + ".height_width");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (meta.exists()) {
            String str;
            try {
                str = FileUtils.readFileToString(meta, Charset.forName("UTF-8"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            height = Integer.parseInt(str.split(",")[0]);
            width = Integer.parseInt(str.split(",")[1]);
        } else {
            try {
                BufferedImage buffer = ImageIO.read(f);
                height = buffer.getHeight();
                width = buffer.getWidth();
                FileUtils.write(meta, height + "," + width, Charset.forName("UTF-8"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 利用サンプルです。
     *
     * 対象
     * 　3.21GB 446ファイル
     * @throws IOException
     *
     */
    public static void main(String[] args) throws IOException {

        long start = System.currentTimeMillis();
        File dir = new File("C:\\tmp\\test2");
        for (File f: dir.listFiles()) {

            final int height;
            final int width;

            /*
             * 通常の処理
             * 3.21GB 446ファイル → 172401[ms]
             */
            //BufferedImage buffer = ImageIO.read(f);
            //height = buffer.getHeight();
            //width = buffer.getWidth();

            /*
             * ImageCash利用（1回目）
             * 3.21GB 446ファイル → 174877[ms]（通常処理と同じ動きの為）
             *
             * ImageCash利用（2回目）
             * 3.21GB 446ファイル → 433[ms]（高速化成功）
             */
            ImageCash ic = new ImageCash(f);
            height = ic.getHeight();
            width = ic.getWidth();


            System.out.println(f.getAbsolutePath() + " " + height + "×" + width);
        }
        System.out.println((System.currentTimeMillis() - start) + "[ms]");
    }

}
