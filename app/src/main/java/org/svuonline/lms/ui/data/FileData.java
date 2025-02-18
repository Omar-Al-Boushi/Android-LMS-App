package org.svuonline.lms.ui.data;

import org.svuonline.lms.R;

public class FileData {
    private String fileName;
    private boolean isDownloaded;
    private int fileTypeIcon;

    public FileData(String fileName, boolean isDownloaded, int fileTypeIcon) {
        this.fileName = fileName;
        this.isDownloaded = isDownloaded;
        this.fileTypeIcon = fileTypeIcon;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.isDownloaded = downloaded;
    }

    public int getFileTypeIcon() {
        return fileTypeIcon;
    }

    public int getDownloadIcon() {
        // يمكنك تعديل هذا المنطق بناءً على الأيقونات المتوفرة
        return isDownloaded ? R.drawable.done : R.drawable.download;
    }
}
