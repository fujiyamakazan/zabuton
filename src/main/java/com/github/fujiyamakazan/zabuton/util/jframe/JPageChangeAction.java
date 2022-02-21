package com.github.fujiyamakazan.zabuton.util.jframe;

/**
 * JPageのボタンで行う画面遷移の処理を定義します。
 * @author fujiyama
 */
public class JPageChangeAction extends JPageAction {
    private static final long serialVersionUID = 1L;
    private JPageApplication app;
    private JPage from;
    private JPage to;

    /**
     * コンストラクタです。
     */
    public JPageChangeAction(JPageApplication app, JPage from, JPage to) {
        this.app = app;
        this.from = from;
        this.to = to;
    }

    @Override
    public void run() {
        this.app.changePage(this.from, this.to);
    }

}
