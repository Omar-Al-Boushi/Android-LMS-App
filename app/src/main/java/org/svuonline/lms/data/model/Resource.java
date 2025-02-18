package org.svuonline.lms.data.model;

public class Resource {
    private long resourceId;
    private long toolId;
    private String fileName;
    private String filePath; // رابط خارجي أو مسار محلي
    private long uploadedBy;
    private String uploadedAt;

    public Resource(long resourceId, long toolId, String fileName, String filePath,
                    long uploadedBy, String uploadedAt) {
        this.resourceId = resourceId;
        this.toolId = toolId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
    }

    // Getters و Setters
    public long getResourceId() { return resourceId; }
    public long getToolId() { return toolId; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public long getUploadedBy() { return uploadedBy; }
    public String getUploadedAt() { return uploadedAt; }

    public void setFilePath(String filePath) { this.filePath = filePath; }
}