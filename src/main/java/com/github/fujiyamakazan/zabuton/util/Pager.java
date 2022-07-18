package com.github.fujiyamakazan.zabuton.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.util.lang.Generics;

/**
 * ページャー（ページ送りの機能）の計算をするオブジェクトです。
 * 全要素数と 1ページ当たりの要素数を元に、さまざまなIndexを算出します。
 *
 * @author fujiyama
 */
public class Pager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Pager.class);

    /** 1ページ当たりの要素数です。 */
    private final int sizeOfPage;

    /** 【現在のページ】のIndexです。 */
    private final int currentPageIndex;

    /** 最初のページのIndexです。（必ずゼロ） */
    private final int firstPageIndex = 0;

    /** 最終ページのIndexです。 */
    private final int lastPageIndex;

    /** 【現在のページ】からいくつ左まで対象とするか。*/
    private final int left;

    /** 【現在のページ】からいくつ右まで対象とするか。*/
    private final int right;

    /**
     * (オプション)ページャーに格納するオブジェクトです。
     * このページャーをデータコンテナとして使用するときに値が入ります。
     */
    @SuppressWarnings("unused")
    private List<Object> items;

    /**
     * コンストラクタです。
     */
    public Pager(int sizeOfItem, int sizeOfPage, int currentItemIndex) {
        this.sizeOfPage = sizeOfPage;

        /*
         * 現在のページのIndexを計算します。
         */
        this.currentPageIndex = getPageIndex(currentItemIndex);

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

        int size = 7; // 7つのページを表示する。

        if (size % 2 == 0) {
            throw new RuntimeException("sizeは奇数を設定してください。");
        }

        size -= 1; // 自身を減算する

        int half = size / 2; // 左右均等にする。

        int right = half;
        int left = half;

        if (currentPageIndex - left > firstPageIndex) {
            left -= 1; // 「最初のページ」が表示される状態なので、左を１つ減らす。
        }
        if (currentPageIndex + right < lastPageIndex) {
            right -= 1; // 「最後のページ」が表示される状態なので、右を１つ減らす。
        }
        for (int i = 0; i <= half; i++) {
            if (currentPageIndex - i < firstPageIndex) { // 左が不足している分、右に振り替える。
                right++;
            }
        }
        for (int i = 0; i <= half; i++) {
            if (currentPageIndex + i > lastPageIndex) { // 右が不足している分、左に振り替える。
                left++;
            }
        }

        this.left = left;
        this.right = right;
    }

    public Pager(List<Object> items, int sizeOfPage) {
        this(items.size(), sizeOfPage, 0);
        this.items = items;
    }

    /**
     * 要素のIndexから、その要素が含まれるページのIndexを返します。
     */
    public int getPageIndex(int itemIndex) {
        return itemIndex / sizeOfPage;
    }

    /**
     * 現在表示しているページのIndexです。
     */
    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    /**
     * 現在表示しているページに、指定された要素のIndexが含まれるかを判定します。
     */
    public boolean hasCurrentPage(int itemIndex) {
        return currentPageIndex == getPageIndex(itemIndex);
    }

    public boolean isActive(Integer pageIndex) {
        return pageIndex == currentPageIndex;
    }

    /**
     * 最初のページが指定されているときにTrueを返します。
     */
    public boolean isFirstPage() {
        return currentPageIndex == firstPageIndex;
    }

    /**
     * 最後のページが指定されているときにTrueを返します。
     */
    public boolean isLastPage() {
        return currentPageIndex == lastPageIndex;
    }

    /**
     * あるページに含まれる要素の先頭のIndexを返します。
     */
    public int getFirstItemIndex(Integer pageIndex) {
        return pageIndex * sizeOfPage;
    }

    /**
     * ページャーに表示するページのIndexを返します。
     * ※ 【先頭】と【末尾】以外、【現在のページ】よりも離れたページは返しません。
     */
    public List<Integer> getPageIndices() {

        List<Integer> indices = Generics.newArrayList();
        for (int index = firstPageIndex; index <= lastPageIndex; index++) {
            if (index == firstPageIndex || index == lastPageIndex) {

                /* 先頭、末尾は必ず使用します。 */
                indices.add(index);

            } else {
                /* 離れているものは除外 */
                if ((index < currentPageIndex - left || currentPageIndex + right < index) == false) {
                    indices.add(index);
                }
            }
        }

        return indices;
    }

    public String getText(int pageIndex) {

        int no = pageIndex + 1; // 表示するページ番号は index + 1 の値（1～）
        final String strNo;

        if (pageIndex == firstPageIndex && pageIndex < currentPageIndex - (left + 1)) { // 先頭
            strNo = no + "...";
        } else if (pageIndex == lastPageIndex
            && currentPageIndex + (right + 1) < pageIndex) { // 末尾
            strNo = "..." + no;
        } else {
            strNo = String.valueOf(no);
        }
        return strNo;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        for (Integer pageIndex : getPageIndices()) {
            if (isActive(pageIndex)) {
                sb.append("[" + getText(pageIndex) + "]");
            } else {
                sb.append("[" + getText(pageIndex) + " ");
            }
        }
        return "Pager [" + sb.toString() + "]";
    }

    /**
     * 動作確認をします。
     */
    public static void main(String[] args) {

        /*
         * データコンテナとして使う検証
         */
        Object[] aiueo = new String[] {
            "あ", "い", "う", "え", "お",
            "か", "き", "★", "け", "こ", // ★=7
            "さ", "し", "す", "せ", "そ",
            "た", "ち", "つ", "て", "と",
            "な", "に", "ぬ", "ね", "の",
            "は", "ひ", "ふ", "へ", "ほ",
            "ま", "み", "む", "め", "も",
            "や", "yi", "ゆ", "ye", "よ",
            "ら", "り", "る", "れ", "ろ",
            "わ", "ゐ", "wu", "ゑ", "を",
            "ん",
        };
        Pager gojuon = new Pager(Arrays.asList(aiueo), 5);
        LOGGER.debug(gojuon.toString());
        LOGGER.debug("" + gojuon.currentPageIndex); // 5ページ区切りのとき 要素index=7 を含むページIndex

    }

}
