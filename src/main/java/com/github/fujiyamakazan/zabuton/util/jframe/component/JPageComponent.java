package com.github.fujiyamakazan.zabuton.util.jframe.component;

import java.awt.Font;
import java.io.Serializable;
import java.util.List;

import javax.swing.JComponent;

import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Generics;

import com.github.fujiyamakazan.zabuton.util.jframe.JPage;

/**
 * ページコンポーネントのモデルです。
 * １つのデータモデルを持ちます。JFrameのコンポーネントを1つ以上使用します。
 *
 * @author fujiyama
 * @param <T> 保持するデータモデルの型
 */
public abstract class JPageComponent<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final Model<T> model;

    private JPage page;

    private final List<JComponent> jcomponents = Generics.newArrayList();

    public JPageComponent(Model<T> model) {
        this.model = model;
    }

    /**
     * 画面にJFrameのコンポーネントを追加します。
     * @param component JFrameのコンポーネント
     */
    protected void addJFrameComponent(JComponent component) {
        component.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 20));
        this.jcomponents.add(component);
    }

    /**
     * ボタンが押されたときに画面コンポーネントの値でモデルを更新する処理を実装する必要があります。
     */
    public abstract void updateModel();

    public List<JComponent> getJComponents() {
        return this.jcomponents;
    }

    public void setPage(JPage page) {
        this.page = page;
    }

    public Model<T> getModel() {
        return this.model;
    }

    public void onInitialize() {
        /* 必要に応じて処理追加 */
    }

    public void onBeforeShow() {
        /* 必要に応じて処理追加 */
    }

    public JPage getPage() {
        return this.page;
    }

    @Override
    public String toString() {
        return "JPageComponent [" + this.model.getObject() + "]";
    }

}