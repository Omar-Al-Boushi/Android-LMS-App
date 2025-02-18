package org.svuonline.lms.ui.data;

public class AssignmentCardData {
    private final String courseName;
    private final String courseCode;
    private final String timeStart;
    private final String timeEnd;
    private final String status;
    private final int backgroundColor;

    public AssignmentCardData(String courseName, String courseCode, String timeStart, String timeEnd, String status, int backgroundColor) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.status = status;
        this.backgroundColor = backgroundColor;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getTimeStart() {
        return timeStart;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public String getStatus() {
        return status;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }
}
