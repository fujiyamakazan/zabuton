package com.github.fujiyamakazan.zabuton.util.jframe.component;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.apache.wicket.model.Model;

/**
 * セレクトボックスのコンポーネントモデルです。
 */
public class JPageSelect extends JPageComponent<String> {
    private static final long serialVersionUID = 1L;
    private final JComboBox<String> jc;

    /**
     * コンストラクタです。
     * @param ids 選択肢
     */
    public JPageSelect(String label, Model<String> model, List<String> ids) {
        super(model);
        jc = new JComboBox<String>(ids.toArray(new String[ids.size()]));
        jc.setSelectedItem(model.getObject());

        addJFrameComponent(new JLabel(label));
        addJFrameComponent(jc);
    }

    @Override
    public void updateModel() {
        model.setObject((String)jc.getSelectedItem());
    }
}