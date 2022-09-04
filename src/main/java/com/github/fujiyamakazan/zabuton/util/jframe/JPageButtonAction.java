package com.github.fujiyamakazan.zabuton.util.jframe;

/**
 * JPageのボタンで行う処理を定義します。
 *
 *  クリックした後に終了しないボタンに使用します。
 *
 * @author fujiyama
 */
public class JPageButtonAction extends JPageAction {
    private static final long serialVersionUID = 1L;

    public void run() {
        /* 子クラスで実装 */
    }

    public boolean isFinal() {
        return false;
    }

}
