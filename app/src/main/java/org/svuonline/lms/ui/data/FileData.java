package org.svuonline.lms.ui.data;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.svuonline.lms.R;
import org.svuonline.lms.data.model.Resource;

import java.io.File;
import java.util.Objects;

/**
 * كائن يمثل بيانات الملف (الاسم، المسار، النوع)
 */
public class FileData {
    private long id; // معرف فريد (يمكن أن يكون assignment_id)
    private String fileName;
    private String filePath; // رابط التحميل الخارجي
    private String fileType; // نوع الملف (pdf, docx، إلخ)
    private Context context;

    public FileData(long id, String fileName, String filePath, Context context) {
        this.id = id;
        this.fileName = fileName != null ? fileName : extractFileNameFromUrl(filePath);
        this.filePath = filePath;
        this.fileType = getFileExtension(this.fileName);
        this.context = context;
    }

    // للتوافق مع الكود الحالي الذي يستخدم Resource
    public FileData(Resource resource, Context context) {
        this.id = resource.getResourceId();
        this.fileName = resource.getFileName();
        this.filePath = resource.getFilePath();
        this.fileType = getFileExtension(fileName);
        this.context = context;
    }

    // استخراج اسم الملف من الرابط إذا لم يتم توفيره
    private String extractFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) return "Unknown";
        String[] parts = url.split("/");
        String filePart = parts[parts.length - 1];
        return filePart.contains("?") ? filePart.substring(0, filePart.indexOf("?")) : filePart;
    }

    // استخراج امتداد الملف من الاسم
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    // التحقق مما إذا كان الملف قد تم تحميله
    public boolean isDownloaded() {
        String cleanFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                cleanFileName);
        boolean exists = file.exists();
        Log.d("FileData", "التحقق من وجود الملف: " + file.getAbsolutePath() + ", موجود: " + exists);
        return exists;
    }

    // Getters
    public long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public String getFileType() { return fileType; }

    // إرجاع أيقونة نوع الملف بناءً على الامتداد وحالة التحميل
    public int getFileTypeIcon() {
        boolean isDownloaded = isDownloaded();
        switch (fileType) {
            case "pdf":
                return isDownloaded ? R.drawable.pdfwhite : R.drawable.pdf;
            case "docx":
                return isDownloaded ? R.drawable.docx_white : R.drawable.docx;
            case "doc":
                return isDownloaded ? R.drawable.doc_white : R.drawable.doc;
            case "lrec":
                return isDownloaded ? R.drawable.lrecwhite : R.drawable.lrec;
            case "mp3":
                return isDownloaded ? R.drawable.mp3_white : R.drawable.mp3;
            case "mp4":
                return isDownloaded ? R.drawable.mp4_white : R.drawable.mp4;
            case "png":
                return isDownloaded ? R.drawable.png_white : R.drawable.png;
            case "jpg":
            case "jpeg":
                return isDownloaded ? R.drawable.jpg_white : R.drawable.jpg;
            case "rar":
                return isDownloaded ? R.drawable.rar_white : R.drawable.rar;
            case "xls":
            case "xlsx":
            case "xlsm":
                return isDownloaded ? R.drawable.xsl_white : R.drawable.xsl;
            case "ppt":
            case "pptx":
                return isDownloaded ? R.drawable.ppt_white : R.drawable.ppt;
            case "zip":
                return isDownloaded ? R.drawable.zip_white : R.drawable.zip;
            case "txt":
                return isDownloaded ? R.drawable.txt_white : R.drawable.txt;
            default:
                return isDownloaded ? R.drawable.file_white : R.drawable.file;
        }
    }

    // إرجاع أيقونة التحميل بناءً على حالة الملف
    public int getDownloadIcon() {
        return isDownloaded() ? R.drawable.done : R.drawable.download;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileData fileData = (FileData) o;
        return id == fileData.id || Objects.equals(fileName, fileData.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fileName);
    }
}