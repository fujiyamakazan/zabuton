package com.github.fujiyamakazan.zabuton.util.text;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

public class TextMerger implements Serializable {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {

        File file1 = new File("C:\\tmp\\社内アクセスログチェック1.txt");
        File file2 = new File("C:\\tmp\\社内アクセスログチェック2.txt");
        File file3 = new File("C:\\tmp\\社内アクセスログチェック3.txt");

        List<File> files = Generics.newArrayList();
        files.add(file2);
        files.add(file3);

        final String finalLine;

        Utf8Text utf8Text = new Utf8Text(file1);
        if (file1.exists()) {
            finalLine = utf8Text.getFinalLine();
        } else {
            finalLine = null;
        }

        List<String> newLines = Generics.newArrayList();

        boolean find = false;
        for (int i = files.size() - 1; i >= 0; i--) {
            File f = files.get(i);
            List<String> list = new Utf8Text(f).readLines();
            Collections.reverse(list);

            for (String line : list) {
                if (StringUtils.equals(line, finalLine)) {
                    find = true;
                    break;
                }
                newLines.add(0, line);
            }
            if (find) {
                break;
            }
        }

        for (String newLine : newLines) {
            System.out.println(newLine);
        }

        utf8Text.writeLines(newLines, true);


    }

}
