package org.svuonline.lms.ui.data;

import java.util.List;

public class CourseData {
    private String courseCode;
    private String courseTitle;
    private int headerColor; // قيمة اللون الفعلية
    private List<SectionData> sections;

    public CourseData(String courseCode, String courseTitle, int headerColor, List<SectionData> sections) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.headerColor = headerColor;
        this.sections = sections;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public int getHeaderColor() {
        return headerColor;
    }

    public List<SectionData> getSections() {
        return sections;
    }
}
