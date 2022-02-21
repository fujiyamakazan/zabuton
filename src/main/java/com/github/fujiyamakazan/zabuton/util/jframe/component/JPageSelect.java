package com.github.fujiyamakazan.zabuton.util.jframe.component;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.wicket.model.Model;

/**
 * セレクトボックスのコンポーネントモデルです。
 */
public class JPageSelect extends JPageComponent<String> implements JPageInputComponent {
    private static final long serialVersionUID = 1L;
    private final JComboBox<String> jc;

    /**
     * コンストラクタです。
     * @param ids 選択肢
     */
    public JPageSelect(String label, Model<String> model, List<String> ids) {
        super(model);
        this.jc = new JComboBox<String>(ids.toArray(new String[ids.size()]));
        this.jc.setSelectedItem(model.getObject());

        addJFrameComponent(new JLabel(label));
        addJFrameComponent(this.jc);

    }

    @Override
    public void updateModel() {
        this.model.setObject((String) this.jc.getSelectedItem());
    }

    @Override
    public void setTextFromModel() {
        String selected = getModel().getObject();

        for (int i = 0; i < this.jc.getItemCount(); i++) {
            if (StringUtils.equals(this.jc.getItemAt(i), selected)) {
                this.jc.setSelectedIndex(i);
                return;
            }

        }
    }
}