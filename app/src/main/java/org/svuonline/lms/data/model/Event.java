package org.svuonline.lms.data.model;

public class Event {
    private long eventId;
    private long userId;
    private String titleEn;
    private String titleAr;
    private String eventDate; // بتنسيق yyyy-MM-dd
    private String type; // نوع الحدث، مثل assignment_submission, assignment_open, term_start, إلخ
    private long relatedId; // معرف العنصر المرتبط (مثل assignmentId أو termId)

    // Getters and Setters
    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getTitleAr() {
        return titleAr;
    }

    public void setTitleAr(String titleAr) {
        this.titleAr = titleAr;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(long relatedId) {
        this.relatedId = relatedId;
    }
}