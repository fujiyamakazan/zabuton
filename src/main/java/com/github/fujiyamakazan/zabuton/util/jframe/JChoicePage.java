package com.github.fujiyamakazan.zabuton.util.jframe;

import java.util.List;

import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageButton;
import com.github.fujiyamakazan.zabuton.util.jframe.component.JPageLabel;

public class JChoicePage extends JPage {
    private static final long serialVersionUID = 1L;
    private final List<JPageButton> choices;
    private final String message;
    private boolean horizontal = true;

    protected JChoicePage(String message, List<JPageButton> choices) {
        this.choices = choices;
        this.message = message;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        addLine(new JPageLabel(message));
        if (horizontal) {
            addLine(choices);
        } else {
            for (JPageButton button: choices) {
                addLine(button);
            }
        }
    }

    public void setHorizonal(boolean horizontal) {
        this.horizontal = horizontal;
    }

}