package org.svuonline.lms.ui.data;

import java.io.Serializable;

public class CourseCardData implements Serializable {
    private long courseId;
    private String courseCode;
    private String courseProgram;
    private String courseName;
    private boolean isNew;
    private boolean isRegistered;
    private boolean isPassed;
    private boolean isRemaining;
    private int backgroundColor;

    public CourseCardData(long courseId, String courseCode, String courseProgram, String courseName,
                          boolean isNew, boolean isRegistered, boolean isPassed, boolean isRemaining, int backgroundColor) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseProgram = courseProgram;
        this.courseName = courseName;
        this.isNew = isNew;
        this.isRegistered = isRegistered;
        this.isPassed = isPassed;
        this.isRemaining = isRemaining;
        this.backgroundColor = backgroundColor;
    }

    // Getters and Setters
    public long getCourseId() { return courseId; }
    public void setCourseId(long courseId) { this.courseId = courseId; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseProgram() { return courseProgram; }
    public void setCourseProgram(String courseProgram) { this.courseProgram = courseProgram; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public boolean isNew() { return isNew; }
    public void setNew(boolean aNew) { isNew = aNew; }
    public boolean isRegistered() { return isRegistered; }
    public void setRegistered(boolean registered) { isRegistered = registered; }
    public boolean isPassed() { return isPassed; }
    public void setPassed(boolean passed) { isPassed = passed; }
    public boolean isRemaining() { return isRemaining; }
    public void setRemaining(boolean remaining) { isRemaining = remaining; }
    public int getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(int backgroundColor) { this.backgroundColor = backgroundColor; }

    // تجاوز دالتي equals() و hashCode() للمقارنة بناءً على courseId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CourseCardData that = (CourseCardData) o;
        return courseId == that.courseId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(courseId);
    }

}