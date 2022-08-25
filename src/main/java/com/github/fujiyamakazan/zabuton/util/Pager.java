package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

/**
 * ページャー（ページ送りの機能）の計算をするオブジェクトです。
 *
 * 「全ての要素の数」と「1ページ当たりの要素数」を元に、さまざまなIndexを算出します。
 *
 * （事例1）
 * ----------------------------
 * 1  2  3 [4] 5  6  ...10
 * ----------------------------
 * 現在のページを「4」としたとき、前後に6つのページを表示します。
 * 範囲の終端には必ず「最初のページ」「最後のページ」を配置ます。
 * 上の例では、右端が7となるところですが、終端が10なので、差替えています。
 * 差替えたときは「...」を付与します。
 *
 * （事例2）
 * ----------------------------
 *  [ 1...  5  6  7 [8] 9  10 ]
 * ----------------------------
 * 現在のページを「8」としたとき、
 * 後には2つしかページがありません。前を4ページとすることで、
 * 「合計6ページ」を保ちます。
 *
 * @author fujiyama
 */
public class Pager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Pager.class);

    /** 全要素の数です。コンストラクタで指定します。 */
    private final int sizeOfItem;

    /** 「1ページ当たりの要素数」です。コンストラクタで指定します。 */
    private final int sizeOfPage;


    /** 「現在のページ」のIndexです。 */
    private int currentPageIndex;

    /** 最初のページのIndexです。（必ずゼロ） */
    private final int firstPageIndex = 0;

    /** 最後のページのIndexです。 */
    private int lastPageIndex;

    /** 表示対象の最小のページIndexです。（「最初のページ」で上書きされる可能性があります。） */
    private int minPageIndex;

    /** 表示対象の最小を「最初のページ」に差替えるかどうかの判定です。 */
    private boolean replaceHead;

    /** 表示対象の最大のページIndexです。（「最後のページ」で上書きされる可能性があります。） */
    private int maxPageIndex;

    /** 表示対象の最大を「最後のページ」に差替えるかどうかの判定です。 */
    private boolean replaceTail;

    /** 表示対象とするページのIndexです。 */
    private List<Integer> indices = Generics.newArrayList();

    /** 表示対象とするページに表示する文字列です。(index=0→「1」) */
    private List<String> texts = Generics.newArrayList();

    /**
     * コンストラクタです。
     * @param sizeOfItem 全ての要素の数
     * @param sizeOfPage 「1ページ当たりの要素数」
     * @param currentItemIndex 「現在の要素」のIndex
     */
    public Pager(int sizeOfItem, int sizeOfPage, int currentItemIndex) {
        this.sizeOfItem = sizeOfItem;
        this.sizeOfPage = sizeOfPage;
        calcIndex(sizeOfItem, sizeOfPage, currentItemIndex);
    }

    /**
     * 「現在の要素」のIndexを変更して再計算します。
     *
     * @param currentItemIndex 「現在の要素」のIndex
     */
    public void moveCurrentPageIndex(int currentItemIndex) {
        calcIndex(this.sizeOfItem, this.sizeOfPage, currentItemIndex);
    }

    private void calcIndex(int sizeOfItem, int sizeOfPage, int currentItemIndex) {

        /*「現在のページ」のIndexを計算します。 */
        this.currentPageIndex = getPageIndexByItemIndex(currentItemIndex);

        /*
         * 最終のページのIndexを計算します。
         */
        int length = sizeOfItem / sizeOfPage;
        if (sizeOfItem % sizeOfPage != 0) { // 割り切れなければ１ページ追加
            length = length + 1;
        }
        this.lastPageIndex = length - 1; // 最終要素のIndexは Length - 1

        /*
         * 対象の範囲を計算します。
         */
        int size = 6; // 前後に6つのページを表示する。
        if (size % 2 != 0) {
            throw new RuntimeException("sizeは偶数を設定してください。");
        }

        int half = size / 2; // 左右均等にする。
        int right = half;
        for (int i = 0; i <= half; i++) {
            if (this.currentPageIndex - i < this.firstPageIndex) { // 左が不足している分、右に振り替える。
                right++;
            }
        }
        int left = half;
        for (int i = 0; i <= half; i++) {
            if (this.currentPageIndex + i > this.lastPageIndex) { // 右が不足している分、左に振り替える。
                left++;
            }
        }

        /*
         * 選択可能な最小、最大のページを計算します。
         */
        this.minPageIndex = this.currentPageIndex - left;
        this.maxPageIndex = this.currentPageIndex + right;

        /*
         * 対象ページのIndexを返します。範囲の終端には必ず「最初のページ」「最後のページ」を配置ます。
         * 名称も設定します。
         */
        this.indices.clear();
        this.texts.clear();
        for (int i = this.minPageIndex; i <= this.maxPageIndex; i++) {
            if (i >= this.firstPageIndex && i <= this.lastPageIndex) {
                this.indices.add(i);
                this.texts.add(String.valueOf(i + 1)); // 表示するページ番号は index + 1 の値（1～）
            }
        }

        /* 先頭を最初のページとする */
        this.replaceHead = this.indices.get(0) != this.firstPageIndex;
        if (this.replaceHead) {
            this.indices.set(0, this.firstPageIndex);
            this.texts.set(0, String.valueOf(this.firstPageIndex + 1) + "...");
        }

        /* 末尾を最終ページとする */
        int tailIndex = this.indices.size() - 1;
        this.replaceTail = this.indices.get(tailIndex) != this.lastPageIndex;
        if (this.replaceTail) {
            this.indices.set(tailIndex, this.lastPageIndex);
            this.texts.set(tailIndex, "..." + String.valueOf(this.lastPageIndex + 1));
        }
    }

    /**
     * 「現在のページ」のIndexを返します。
     */
    public int getCurrentPageIndex() {
        return this.currentPageIndex;
    }

    /**
     * 1つ前のページのIndexを返します。
     */
    public int getPrePageIndex() {
        return this.currentPageIndex - 1;
    }

    /**
     * 1つ後のページのIndexを返します。
     */
    public int getNextPageIndex() {
        return this.currentPageIndex + 1;
    }

    /**
     * 要素のIndexから、その要素が含まれるページのIndexを返します。
     */
    public int getPageIndexByItemIndex(int itemIndex) {
        return itemIndex / this.sizeOfPage;
    }

    /**
     * あるページに含まれる先頭の要素のIndexを返します。
     */
    public int getFirstItemIndex(Integer pageIndex) {
        return pageIndex * this.sizeOfPage;
    }

    /**
     * 表示対象のページIndexを返します。
     */
    public List<Integer> getPageIndices() {
        return this.indices;
    }

    /**
     * 表示する文字列を返します。
     */
    public String getText(int pageIndex) {
        for (int i = 0; i < this.indices.size(); i++) {
            if (this.indices.get(i).equals(pageIndex)) {
                return this.texts.get(i);
            }
        }
        throw new RuntimeException(pageIndex + "のテキストが不明");
    }

    /**
     * 最初のページが指定されているときにTrueを返します。
     * この状態とのき、「前へ」ボタンは使用できません。
     */
    public boolean isCurrentFirstPage() {
        return this.currentPageIndex == this.firstPageIndex;
    }

    /**
     * 最後のページが指定されているときにTrueを返します。
     * この状態のとき、「次へ」ボタンは使用できません。
     */
    public boolean isCurrentLastPage() {
        return this.currentPageIndex == this.lastPageIndex;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Integer pageIndex : getPageIndices()) {
            if (pageIndex == this.currentPageIndex) {
                sb.append("[" + getText(pageIndex) + "]");
            } else {
                sb.append(" " + getText(pageIndex) + " ");
            }
        }
        return "Pager [" + sb.toString() + "]";
    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        LOGGER.debug("----------------------------------");
        for (int pageIndex = 0; pageIndex < 10; pageIndex++) {
            Pager pager = new Pager(20, 2, pageIndex * 2);
            LOGGER.debug(pageIndex + ">" + pager.toString());
        }
    }
}
