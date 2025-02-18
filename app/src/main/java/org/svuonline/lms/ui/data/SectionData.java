package org.svuonline.lms.ui.data;

import java.util.List;

// SectionData.java
public class SectionData {
    private String title;
    private List<ButtonData> buttons;

    public SectionData(String title, List<ButtonData> buttons) {
        this.title = title;
        this.buttons = buttons;
    }

    // Getters

    public String getTitle() {
        return title;
    }

    public List<ButtonData> getButtons() {
        return buttons;
    }
}
