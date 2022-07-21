package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        calcIndex(currentItemIndex);
    }

    /**
     * 「現在の要素」のIndexを指定して計算をします。
     *
     * @param currentItemIndex 「現在の要素」のIndex
     */
    public void calcIndex(int currentItemIndex) {

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
            if (currentPageIndex - i < firstPageIndex) { // 左が不足している分、右に振り替える。
                right++;
            }
        }
        int left = half;
        for (int i = 0; i <= half; i++) {
            if (currentPageIndex + i > lastPageIndex) { // 右が不足している分、左に振り替える。
                left++;
            }
        }

        /*
         * 選択可能な最小、最大のページを計算します。
         */
        this.minPageIndex = currentPageIndex - left;
        this.maxPageIndex = currentPageIndex + right;

        /*
         * 対象ページのIndexを返します。範囲の終端には必ず「最初のページ」「最後のページ」を配置ます。
         * 名称も設定します。
         */
        indices.clear();
        texts.clear();
        for (int i = minPageIndex; i <= maxPageIndex; i++) {
            if (i >= firstPageIndex && i <= lastPageIndex) {
                indices.add(i);
                texts.add(String.valueOf(i + 1)); // 表示するページ番号は index + 1 の値（1～）
            }
        }

        /* 先頭を最初のページとする */
        replaceHead = indices.get(0) != firstPageIndex;
        if (replaceHead) {
            indices.set(0, firstPageIndex);
            texts.set(0, String.valueOf(firstPageIndex + 1) + "...");
        }

        /* 末尾を最終ページとする */
        int tailIndex = indices.size() - 1;
        replaceTail = indices.get(tailIndex) != lastPageIndex;
        if (replaceTail) {
            indices.set(tailIndex, lastPageIndex);
            texts.set(tailIndex, "..." + String.valueOf(lastPageIndex + 1));
        }
    }

    /**
     * 「現在のページ」のIndexを返します。
     */
    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    /**
     * 1つ前のページのIndexを返します。
     */
    public int getPrePageIndex() {
        return currentPageIndex - 1;
    }

    /**
     * 1つ後のページのIndexを返します。
     */
    public int getNextPageIndex() {
        return currentPageIndex + 1;
    }

    /**
     * 要素のIndexから、その要素が含まれるページのIndexを返します。
     */
    public int getPageIndexByItemIndex(int itemIndex) {
        return itemIndex / sizeOfPage;
    }

    /**
     * あるページに含まれる先頭の要素のIndexを返します。
     */
    public int getFirstItemIndex(Integer pageIndex) {
        return pageIndex * sizeOfPage;
    }

    /**
     * 表示対象のページIndexを返します。
     */
    public List<Integer> getPageIndices() {
        return indices;
    }

    /**
     * 表示する文字列を返します。
     */
    public String getText(int pageIndex) {
        for (int i = 0; i < indices.size(); i++) {
            if (indices.get(i).equals(pageIndex)) {
                return texts.get(i);
            }
        }
        throw new RuntimeException(pageIndex + "のテキストが不明");
    }

    /**
     * 最初のページが指定されているときにTrueを返します。
     * この状態とのき、「前へ」ボタンは使用できません。
     */
    public boolean isCurrentFirstPage() {
        return currentPageIndex == firstPageIndex;
    }

    /**
     * 最後のページが指定されているときにTrueを返します。
     * この状態のとき、「次へ」ボタンは使用できません。
     */
    public boolean isCurrentLastPage() {
        return currentPageIndex == lastPageIndex;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Integer pageIndex : getPageIndices()) {
            if (pageIndex == currentPageIndex) {
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

        LOGGER.debug("40件を10ページ区切りとし、itemIndex=20を現在のページとするページャーを作成する。");
        Pager me1 = new Pager(40, 10, 20);
        LOGGER.debug("→：" + me1.toString());

        LOGGER.debug("各ページIndexを表示");
        LOGGER.debug("→ PageIndices:" + Arrays.asList(me1.getPageIndices()).stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ")));
        LOGGER.debug("→ firstPageIndex：" + me1.firstPageIndex);
        LOGGER.debug("→ lastPageIndex：" + me1.lastPageIndex);

        LOGGER.debug("itemIndexを指定して、それがどのページ目かを算出する。");
        LOGGER.debug("→ ItemIndex=0 は PageIndex=0:" +  me1.getPageIndexByItemIndex(0));
        LOGGER.debug("→ ItemIndex=9 は PageIndex=0:" +  me1.getPageIndexByItemIndex(9));
        LOGGER.debug("→ ItemIndex=10 は PageIndex=1:" +  me1.getPageIndexByItemIndex(10));
        LOGGER.debug("→ ItemIndex=21 は PageIndex=2:" +  me1.getPageIndexByItemIndex(10));

        LOGGER.debug("41件を10ページ区切りとし、itemIndex=21を現在のページとするページャーを作成する。");
        Pager me2 = new Pager(41, 10, 21);
        LOGGER.debug("→総ページ数が1つ増える：" + me2.toString());

        LOGGER.debug("現在のページを移動する");
        Pager me3 = new Pager(100, 10, 0);
        for (int pageIndex = 0; pageIndex <= me3.lastPageIndex; pageIndex++) {
            int currentItemIndex = me3.getFirstItemIndex(pageIndex);
            me3.calcIndex(currentItemIndex);
            LOGGER.debug(pageIndex + ">" + me3.toString());
        }
    }
}
