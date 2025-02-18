package org.svuonline.lms.data.model;

import java.io.Serializable;

public class Course implements Serializable {
    private long courseId;
    private long programId;
    private long termId;
    private String code;
    private String nameEn;
    private String nameAr;
    private long createdBy;
    private String createdAt;
    private int creditHours;
    private String color;
    private String status; // Passed, Registered, Remaining
    private boolean isNew; // لتحديد إذا كان المقرر جديدًا

    // Constructor
    public Course(long courseId, long programId, long termId, String code, String nameEn, String nameAr,
                  long createdBy, String createdAt, int creditHours, String color, String status, boolean isNew) {
        this.courseId = courseId;
        this.programId = programId;
        this.termId = termId;
        this.code = code;
        this.nameEn = nameEn;
        this.nameAr = nameAr;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.creditHours = creditHours;
        this.color = color;
        this.status = status;
        this.isNew = isNew;
    }

    // Getters and Setters
    public long getCourseId() { return courseId; }
    public void setCourseId(long courseId) { this.courseId = courseId; }
    public long getProgramId() { return programId; }
    public void setProgramId(long programId) { this.programId = programId; }
    public long getTermId() { return termId; }
    public void setTermId(long termId) { this.termId = termId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public String getNameAr() { return nameAr; }
    public void setNameAr(String nameAr) { this.nameAr = nameAr; }
    public long getCreatedBy() { return createdBy; }
    public void setCreatedBy(long createdBy) { this.createdBy = createdBy; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public int getCreditHours() { return creditHours; }
    public void setCreditHours(int creditHours) { this.creditHours = creditHours; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isNew() { return isNew; }
    public void setNew(boolean aNew) { isNew = aNew; }
}