
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

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
        meta = new File(f.getAbsolutePath() + ".meta");
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
     * 動作確認をします。
     */
    public static void main(String[] args) {

        long start = System.currentTimeMillis();

        File f = new File("C:\\tmp\\test.jpg");


        ImageCash ic = new ImageCash(f); // 一度ImageCashインスタンスを生成すると、２回目からは高速になります。


        int height = ic.getHeight();
        int width = ic.getWidth();
        System.out.println(height);
        System.out.println(width);

        System.out.println((System.currentTimeMillis()-start) + "[ms]");
    }

}
