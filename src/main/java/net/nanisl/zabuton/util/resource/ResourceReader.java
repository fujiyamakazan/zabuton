package net.nanisl.zabuton.util.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class  ResourceReader{
    /**
     * baseのクラスファイルの場所を起点にリソースのテキストを読み込む
     *
     * Jarファイルとしてエクスポートした状態にも対応
     *
     * @param base 起点
     * @param fileName ファイルの相対パス
     * @return
     *
     * @deprecated ResourceFileUtils#readResource の利用を検討
     *
     */
    public static List<String> readResource(Class<?> base, String fileName) {
        List<String> lines;
        BufferedReader reader = null;
        InputStream inputStream = null;
        try {

            inputStream = base.getResourceAsStream(fileName);
            if (inputStream == null) {
                throw new RuntimeException("ResourceAsStream is Null of " + fileName);
            }
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            lines = IOUtils.readLines(reader);


        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(reader);
        }
        return lines;
    }

}
