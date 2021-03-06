package net.nanisl.zabuton.file;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;


/**
 * テキストファイルのデータアクセスオブジェクト
 *
 * @author fujiyama
 */
public class TextDataAccess implements Serializable {

    private static final long serialVersionUID = 1L;

    /** データを保存しているディレクトリのパス */
    private static final String PATH_DATA = "data";

    private static final String HEADER_WORD = "■";

	public List<Map<String, String>> readFiles(String dataName) {
		List<Map<String, String>> maps = Generics.newArrayList();
		File dir = getSubDir(dataName);
		List<File> files = Arrays.asList(dir.listFiles());
		for (File file: files) {
			Map<String, String> map = Generics.newHashMap();
			maps.add(map);
			Utf8File f= Utf8File.of(file);
			String head = null;
			StringBuilder sb = new StringBuilder();
			for (String line: f.readLines()) {
				/* 見出し行を検知 */
				if (StringUtils.startsWith(line, HEADER_WORD)) {
					String buff = sb.toString();
					if (buff.isEmpty() == false) {
						/* バッファの処理 */
						map.put(head, buff.trim());
						head = "";
						sb = new StringBuilder();
					}
					/* 新たなバッファの準備 */
					head = line.substring(HEADER_WORD.length()).trim();
					sb = new StringBuilder();
					continue;
				} else {
					sb.append(line + "\n");
				}
			}
			String buff = sb.toString();
			if (buff.isEmpty() == false) {
				map.put(head, buff.trim());
			}
		}
		return maps;
	}

    public void writeFile(String groupId, String fileName, LinkedHashMap<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry: map.entrySet()) {
            sb.append(HEADER_WORD + entry.getKey() + "\n");
            sb.append(entry.getValue() + "\n");
        }
        File dir = getSubDir(groupId);
        Utf8File f= Utf8File.of(new File(dir, fileName + ".txt"));
        f.writeString(sb.toString());
    }



    /**
     * １行が key:value で構成されているテキストのファイルからデータを取得する
     * @return key-valueのMapオブジェクト
     */
    public Map<String, String> getKeyValues(String entityName) {
        Map<String, String> map = Generics.newHashMap();
        File file = getFile(entityName);
        for (String line: Utf8File.of(file).readLines()) {
            if (line.contains(":")) {
                map.put(line.split(":")[0], line.split(":")[1]);
            }
        }
        return map;
    }
    /**
     * １行が key:value で構成されているテキストのファイルにデータを追加する
     */
    public void addKeyValue(String entityName,String key, String value) {
        Utf8File f = Utf8File.of(getFile(entityName));
        String existText = f.readFileToString();
        boolean isNotLnTail = StringUtils.isNotEmpty(existText) && StringUtils.endsWith(existText, "\n") == false;
        String line = key + ":" + value;
        if(isNotLnTail) { // ファイルの最後が改行でなければ、改行を追加
            line = "\n" + line;
        }
        f.writeString(line, true); // 追記モード
    }
    /**
     * １行が key:value で構成されているテキストからデータを削除する
     */
    public void removeKeyValue(String entityName, String key) {
        Utf8File f = Utf8File.of(getFile(entityName));
        List<String> lines = Generics.newArrayList();
        for (String line: f.readLines()) {
            if (StringUtils.startsWith(line, key + ":") == false) {
                lines.add(line);
            }
        }
        f.writeListString(lines);
    }

    /**
     * データ保存用ディレクトリオブジェクトの取得
     * ※ 無ければ作成する
     */
    private static File getDataDir() {
        File dataDir = new File(PATH_DATA);
        if (dataDir.exists() == false) {
            dataDir.mkdirs();
        }
        return dataDir;
    }

    /**
     * データ保存用サブディレクトリオブジェクトの取得
     * ※ 無ければ作成する
     */
    private static File getSubDir(String subDirName) {
        File dir = new File(getDataDir(), subDirName);
        if (dir.exists() == false) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * ファイルオブジェクトの取得
     * ※ 無ければ作成する
     */
    private static File getFile(String entityName) {
        File file = new File(getDataDir(), entityName + ".txt");
        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }




}
