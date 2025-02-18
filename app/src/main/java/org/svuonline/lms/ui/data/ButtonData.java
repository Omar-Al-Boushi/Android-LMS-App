package org.svuonline.lms.ui.data;

public class ButtonData {
    private String label;
    private int buttonColor; // قيمة اللون الفعلية
    private String buttonId;

    public ButtonData(String label, int buttonColor, String buttonId) {
        this.label = label;
        this.buttonColor = buttonColor;
        this.buttonId = buttonId;
    }

    public String getLabel() {
        return label;
    }

    public int getButtonColor() {
        return buttonColor;
    }

    public String getButtonId() {
        return buttonId;
    }
}
