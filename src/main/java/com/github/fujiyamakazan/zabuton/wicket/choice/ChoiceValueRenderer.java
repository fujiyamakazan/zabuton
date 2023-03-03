package com.github.fujiyamakazan.zabuton.wicket.choice;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.model.IModel;

/**
 * WicketのDropDownChoiceやRadioChoiceで使う選択肢のレンダラです。
 * valueには選択肢の値をそのまま使います。
 *
 * @author fujiyama
 */
public class ChoiceValueRenderer extends ChoiceRenderer<IChoice> {
    private static final long serialVersionUID = 1L;

    @Override
    public String getIdValue(IChoice object, int index) {
        if (object == null) {
            return null;
        }
        return object.getValue(); // 選択肢の値をそのまま使う。
    }

    @Override
    public Object getDisplayValue(IChoice object) {
        if (object == null) {
            return null;
        }
        return object.getDisplay();
    }

    @Override
    public IChoice getObject(String id, IModel<? extends List<? extends IChoice>> choices) {
        for (IChoice choice : choices.getObject()) {
            if (choice == null) {
                continue;
            }
            if (StringUtils.equals(choice.getValue(), id)) {
                return choice;
            }
        }
        return null;
    }

}