package org.svuonline.lms.ui.data;

import java.util.ArrayList;
import java.util.List;

public class SectionData {
    private int sectionId; // معرف القسم
    private String title;  // عنوان القسم
    private List<ButtonData> buttons; // قائمة الأدوات

    // مُنشئ للاستخدام مع قاعدة البيانات
    public SectionData(int sectionId, String title) {
        this.sectionId = sectionId;
        this.title = title;
        this.buttons = new ArrayList<>();
    }

    // مُنشئ للاستخدام في أماكن أخرى
    public SectionData(String title, List<ButtonData> buttons) {
        this.sectionId = -1; // قيمة افتراضية
        this.title = title;
        this.buttons = buttons != null ? buttons : new ArrayList<>();
    }

    public int getSectionId() { return sectionId; }
    public String getTitle() { return title; }
    public List<ButtonData> getButtons() { return buttons; }

    // طريقة لتحديث الأدوات
    public void setButtons(List<ButtonData> buttons) {
        this.buttons = buttons;
    }
}