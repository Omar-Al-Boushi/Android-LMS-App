package org.svuonline.lms.ui.data;

public class ButtonData {
    private long toolId; // معرف الزر، يتطابق مع tool_id في قاعدة البيانات
    private String label;
    private int buttonColor;
    private String actionType;

    public ButtonData(long toolId, String label, int buttonColor, String actionType) {
        this.toolId = toolId;
        this.label = label;
        this.buttonColor = buttonColor;
        this.actionType = actionType;
    }

    public long getToolId() {
        return toolId;
    }

    public String getLabel() {
        return label;
    }

    public int getButtonColor() {
        return buttonColor;
    }

    public String getActionType() {
        return actionType;
    }
}