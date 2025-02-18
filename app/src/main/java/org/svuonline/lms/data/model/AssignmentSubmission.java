package org.svuonline.lms.data.model;

public class AssignmentSubmission {
    private long submissionId;
    private long assignmentId;
    private long userId;
    private String submittedAt;
    private String filePath;
    private String status;
    private float grade;
    private long gradedBy;
    private String gradedAt;

    public AssignmentSubmission() {
    }

    // Getters and Setters
    public long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(long submissionId) {
        this.submissionId = submissionId;
    }

    public long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getGrade() {
        return grade;
    }

    public void setGrade(float grade) {
        this.grade = grade;
    }

    public long getGradedBy() {
        return gradedBy;
    }

    public void setGradedBy(long gradedBy) {
        this.gradedBy = gradedBy;
    }

    public String getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(String gradedAt) {
        this.gradedAt = gradedAt;
    }
}