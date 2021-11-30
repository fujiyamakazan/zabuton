package com.github.fujiyamakazan.zabuton.util;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.util.lang.Generics;

/**
 * ファイル情報を表すオブジェクトです。
 * @author fujiyama
 */
public class FileInfo {

    private final String name;
    private final List<FileInfo> children;
    private final FileInfo parent;
    private File file;

    /**
     * コンストラクタです。必ず親のオブジェクトを示します。
     * ファイルの名前に「\」が含まれるときは、自動的に子要素が展開されます。
     *
     * @param parent 親のオブジェクト
     * @param name ファイルの名前
     */
    public FileInfo(FileInfo parent, String name) {
        this.parent = parent;
        this.children = Generics.newArrayList();
        int index = name.indexOf('\\');
        if (index == -1) {
            this.name = name;

        } else {
            this.name = name.substring(0, index);
            this.children.add(new FileInfo(this, name.substring(index + 1)));
        }

    }

    public static FileInfo ofRoot(String name) {
        return new FileInfo(null, name);
    }

    public String getName() {
        return this.name;
    }

    public FileInfo getParent() {
        return parent;
    }

    /**
     * 親要素の名称も含めた完全な名前を返します。
     */
    public String getFullName() {
        if (this.parent == null) {
            return this.name;
        }
        String parentFullName = getParent().getFullName();
        if (StringUtils.isEmpty(parentFullName)) {
            return this.name;
        } else {
            return parentFullName + "/" + this.name;
        }

    }

    /**
     * 下階層の要素を返します。
     * @param offset 0のときは自身。子は「1」、孫は「2」を指定します。
     */
    public List<FileInfo> getLayer(int offset) {
        List<FileInfo> result = Generics.newArrayList();
        if (offset == 0) {
            result.add(this);
            return result;

        } else {
            for (FileInfo child : children) {
                result.addAll(child.getLayer(offset - 1));
            }
            return result;
        }
    }

    /**
     * 自分自身を含めた全部の要素を返します。
     */
    public List<FileInfo> getAll() {

        List<FileInfo> results = null;
        for (FileInfo child : this.children) {
            if (results == null) {
                results = Generics.newArrayList();
            }
            results.addAll(child.getAll());
        }
        if (results == null) {
            results = Generics.newArrayList();
        }
        results.add(0, this);
        return results;
    }

    /**
     * 名称を指定して下階層から要素を検索します。見つからない場合はnullを返します。
     * @param name 指定する名称。
     */
    public FileInfo find(String name) {
        if (StringUtils.equals(this.name, name)) {
            return this;
        } else {
            for (FileInfo child : this.children) {
                FileInfo f = child.find(name);
                if (f != null) {
                    return f;
                }
            }
            return null;
        }
    }

    /**
     * マージします
     */
    public void merge(FileInfo other) {
        //        /* 同じものがあればマージ */
        //
        //        FileInfo same = null;
        //        for (FileInfo child : this.children) {
        //            if (StringUtils.equals(child.getName(), other.getName())) {
        //                same = child;
        //                break;
        //            }
        //        }
        //        if (same == null) {
        //            /* 単純追加 */
        //            this.children.add(other);
        //
        //        } else {
        //            /* 同じ要素に孫要素をJoin */
        //            for (FileInfo otherChild : other.children) {
        //                same.joinChild(otherChild);
        //            }
        //        }

        /* 名称が異なればエラー */
        if (this.name.equals(other.name) == false) {
            throw new RuntimeException();
        }

        /* parentが異なればエラー */
        if (this.parent != null || other.parent != null) {
            if (this.parent.name.equals(other.parent.name) == false) {
                throw new RuntimeException();
            }
        }

        /*
         * 子をマージ
         */
        for (FileInfo otherChild : other.children) {
            FileInfo sameChild = null;
            for (FileInfo child : this.children) {
                if (otherChild.name.equals(child.name)) {
                    sameChild = child;
                    break;
                }
            }
            if (sameChild != null) {
                sameChild.merge(otherChild);
            } else {
                this.children.add(otherChild);
            }
        }

    }

    @Override
    public String toString() {
        String string = name + "[" + children.size() + "]";
        for (FileInfo child : this.children) {
            String childString = child.toString();
            string += "\n└" + childString.toString().replaceAll("\n", "\n　");
        }
        return string;
    }

    /**
     * ファイル名が一致するかを判定します。
     * @param other 比較対象
     */
    public boolean isSame(FileInfo other) {

        if (getName().equals(other.getName()) == false) {
            return false;
        }

        /* 「トラック」という名前は同一とは言えない */
        if (getName().contains("トラック")
            || getName().contains("ﾄﾗｯｸ")
            || getName().toLowerCase().contains("track")) {
            return false;
        }

        File me = getReal();
        File otherFile = other.getReal();

        if (me.getName().equals(otherFile.getName())
            //&& me.length() == otherFile.length()
            ) {
            return true;

        } else {
            return false;

        }
    }

    public File getReal() {
        if (this.file == null) {
            this.file = new File(this.getFullName());
            if (this.file.exists() == false) {
                throw new RuntimeException("[" + this.file.getAbsolutePath() + "]がありません。");
            }
        }
        return this.file;
    }

}