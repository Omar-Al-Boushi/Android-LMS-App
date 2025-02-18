package org.svuonline.lms.ui.data;

public class AssignmentCardData {
    private final String courseName;
    private final String courseCode;
    private final String timeStart;
    private final String timeEnd;
    private final String status;
    private final int backgroundColor;
    private final int assignmentId;

    public AssignmentCardData(String courseName, String courseCode, String timeStart, String timeEnd, String status, int backgroundColor, int assignmentId) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.status = status;
        this.backgroundColor = backgroundColor;
        this.assignmentId = assignmentId;
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

    public int getAssignmentId() {
        return assignmentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentCardData that = (AssignmentCardData) o;
        return assignmentId == that.assignmentId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(assignmentId);
    }
}