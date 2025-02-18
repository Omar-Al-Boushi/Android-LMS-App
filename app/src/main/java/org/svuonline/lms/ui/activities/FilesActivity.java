package org.svuonline.lms.ui.activities;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.svuonline.lms.R;
import org.svuonline.lms.data.model.Resource;
import org.svuonline.lms.data.repository.CourseRepository;
import org.svuonline.lms.data.repository.ResourceRepository;
import org.svuonline.lms.data.repository.SectionToolRepository;
import org.svuonline.lms.ui.adapters.FilesAdapter;
import org.svuonline.lms.ui.data.CourseData;
import org.svuonline.lms.ui.data.FileData;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Locale;

/**
 * نشاط لعرض وتحميل الملفات الخاصة بأداة معينة في المقرر
 */
public class FilesActivity extends BaseActivity implements FilesAdapter.FileDownloadListener {
    // عناصر واجهة المستخدم
    private TextView courseCodeTextView; // لعرض رمز المقرر
    private TextView courseTitleTextView; // لعرض عنوان المقرر
    private ConstraintLayout courseHeaderLayout; // رأس الصفحة
    private TextView sectionTitle; // عنوان الأداة
    private MaterialButton backButton; // زر الرجوع
    private MaterialButton favoriteButton; // زر المفضلة
    private RecyclerView recyclerView; // قائمة الملفات

    // بيانات المقرر والمستخدم
    private int courseColor; // لون رأس المقرر
    private String toolId; // معرف الأداة
    private String courseCode; // رمز المقرر
    private boolean isFavorite; // حالة المفضلة
    private long userId; // معرف المستخدم

    // مكونات إدارة التحميل
    private FilesAdapter adapter; // محول لعرض الملفات
    private DownloadManager downloadManager; // لإدارة التحميلات
    private Map<Long, FileData> downloadIdToFileData = new HashMap<>(); // تتبع معرفات التحميل
    private Map<String, Long> downloadingFiles = new HashMap<>(); // تتبع الملفات قيد التحميل
    private ExecutorService executorService; // لتتبع تقدم التحميل
    private Handler mainHandler; // لتحديث واجهة المستخدم
    private Vibrator vibrator; // لتشغيل الاهتزاز

    // المستودعات
    private ResourceRepository resourceRepository; // لجلب الملفات
    private CourseRepository courseRepository; // لجلب بيانات المقرر
    private SectionToolRepository sectionToolRepository; // لجلب بيانات الأداة

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100; // رمز طلب الأذونات

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        // تهيئة المستودعات
        resourceRepository = new ResourceRepository(this);
        courseRepository = new CourseRepository(this);
        sectionToolRepository = new SectionToolRepository(this);

        // تهيئة مكونات التحميل
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // تهيئة الاهتزاز
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        // جلب معرف المستخدم
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = userPrefs.getLong("user_id", -1);
        if (userId == -1) {
            Log.e("FilesActivity", "معرف المستخدم غير موجود");
            finish();
            return;
        }

        // جلب معرف الأداة من Intent
        Intent intent = getIntent();
        toolId = intent.getStringExtra("button_id");
        Log.d("FilesActivity", "معرف الأداة: " + toolId);
        if (toolId == null) {
            Log.e("FilesActivity", "معرف الأداة غير موجود");
            finish();
            return;
        }

        // ربط عناصر واجهة المستخدم
        courseCodeTextView = findViewById(R.id.courseCodeTextView);
        courseTitleTextView = findViewById(R.id.courseTitleTextView);
        courseHeaderLayout = findViewById(R.id.courseHeaderLayout);
        sectionTitle = findViewById(R.id.sectionTitle);
        backButton = findViewById(R.id.backButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        recyclerView = findViewById(R.id.filesRecyclerView);

        // جلب رمز المقرر
        try {
            courseCode = sectionToolRepository.getCourseCodeByToolId(Long.parseLong(toolId));
            if (courseCode == null) {
                Log.e("FilesActivity", "لم يتم العثور على المقرر");
                Snackbar.make(findViewById(android.R.id.content), R.string.course_not_found, Snackbar.LENGTH_LONG).show();
                finish();
                return;
            }
        } catch (NumberFormatException e) {
            Log.e("FilesActivity", "معرف الأداة غير صالح: " + toolId, e);
            finish();
            return;
        }

        // جلب بيانات المقرر
        boolean isArabic = isArabicLocale();
        CourseData courseData = courseRepository.getCourseData(courseCode, isArabic);
        if (courseData == null) {
            Log.e("FilesActivity", "لم يتم العثور على بيانات المقرر");
            Snackbar.make(findViewById(android.R.id.content), R.string.course_not_found, Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        // جلب اسم الأداة
        String buttonLabel = sectionToolRepository.getToolName(Long.parseLong(toolId), isArabic);
        if (buttonLabel == null) {
            buttonLabel = getString(R.string.files);
        }

        // التحقق من حالة المفضلة
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();

        // تحديث واجهة المستخدم
        courseCodeTextView.setText(courseData.getCourseCode());
        courseTitleTextView.setText(courseData.getCourseTitle());
        courseColor = courseData.getHeaderColor();
        courseHeaderLayout.setBackgroundColor(courseColor);
        sectionTitle.setText(buttonLabel);

        // إعداد شريط النظام
        Utils.setSystemBarColorWithColorInt(this, courseColor,
                getResources().getColor(R.color.Custom_BackgroundColor), 0);

        // إعداد زر الرجوع
        backButton.setOnClickListener(v -> finish());

        // إعداد زر المفضلة
        favoriteButton.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            courseRepository.setCourseFavorite(userId, courseCode, isFavorite);
            updateFavoriteButton();
            String message = isFavorite ? getString(R.string.added_to_favorites) :
                    getString(R.string.removed_from_favorites);
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
        });

        // عرض قائمة الملفات
        List<FileData> files = getFilesForTool(toolId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FilesAdapter(this, files, courseColor, this);
        recyclerView.setAdapter(adapter);

        // تسجيل BroadcastReceiver لمراقبة اكتمال التحميل
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        ContextCompat.registerReceiver(this, downloadReceiver, filter, ContextCompat.RECEIVER_EXPORTED);
    }

    /**
     * التحقق من اللغة المختارة
     */
    private boolean isArabicLocale() {
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String selectedLanguage = preferences.getString("selected_language", "en");
        return "ar".equals(selectedLanguage);
    }

    /**
     * تحديث أيقونة المفضلة
     */
    private void updateFavoriteButton() {
        favoriteButton.setIconResource(isFavorite ? R.drawable.star_selected : R.drawable.star);
        favoriteButton.setIconTint(ColorStateList.valueOf(Color.WHITE));
    }

    /**
     * طلب أذونات التخزين
     */
//    private void requestStoragePermissions() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            List<String> permissions = new ArrayList<>();
//            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    != PackageManager.PERMISSION_GRANTED) {
//                permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            }
//            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
//                    != PackageManager.PERMISSION_GRANTED) {
//                permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
//            }
//            if (!permissions.isEmpty()) {
//                Log.d("FilesActivity", "طلب الأذونات: " + permissions);
//                ActivityCompat.requestPermissions(this,
//                        permissions.toArray(new String[0]),
//                        STORAGE_PERMISSION_REQUEST_CODE);
//            } else {
//                Log.d("FilesActivity", "جميع الأذونات ممنوحة");
//            }
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("FilesActivity", "تم منح الأذونات");
            } else {
                Log.w("FilesActivity", "تم رفض الأذونات");
                Snackbar.make(findViewById(android.R.id.content),
                        R.string.storage_permissions_required, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * جلب قائمة الملفات للأداة
     */
    private List<FileData> getFilesForTool(String toolId) {
        List<FileData> files = new ArrayList<>();
        try {
            long toolIdLong = Long.parseLong(toolId);
            List<Resource> resources = resourceRepository.getResourcesByToolId(toolIdLong);
            for (Resource resource : resources) {
                FileData fileData = new FileData(resource, this);
                files.add(fileData);
                Log.d("FilesActivity", "تم إضافة الملف: " + fileData.getFileName());
            }
        } catch (NumberFormatException e) {
            Log.e("FilesActivity", "معرف الأداة غير صالح: " + toolId, e);
        }
        Log.d("FilesActivity", "إجمالي الملفات: " + files.size());
        return files;
    }

    /**
     * معالجة النقر على الملف
     */
    @Override
    public void onFileClicked(FileData fileData) {
        Log.d("FilesActivity", "تم النقر على الملف: " + fileData.getFileName() + ", محمل: " + fileData.isDownloaded());
        if (fileData.isDownloaded()) {
            openFile(fileData);
        } else {
            downloadFile(fileData);
        }
    }

    /**
     * التحقق من الاتصال بالإنترنت
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * التحقق من صلاحية رابط التحميل
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return url.startsWith("http://") || url.startsWith("https://");
    }

    /**
     * بدء تحميل الملف
     */
    private void downloadFile(FileData fileData) {
        // التحقق من الاتصال بالإنترنت
        if (!isNetworkAvailable()) {
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.check_internet_connection, Snackbar.LENGTH_SHORT).show();
            return;
        }


        // التحقق مما إذا كان الملف قيد التحميل
        String fileName = fileData.getFileName();
        if (downloadingFiles.containsKey(fileName)) {
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.file_already_downloading, Snackbar.LENGTH_SHORT).show();
            return;
        }

        try {
            String fileUrl = fileData.getFilePath();
            Log.d("FilesActivity", "تحميل الملف: " + fileName + " من: " + fileUrl);

            // التحقق من صلاحية الرابط
            if (!isValidUrl(fileUrl)) {
                Log.e("FilesActivity", "رابط التحميل غير صالح: " + fileUrl);
                Snackbar.make(findViewById(android.R.id.content),
                        R.string.invalid_download_url, Snackbar.LENGTH_SHORT).show();
                return;
            }

            Uri uri = Uri.parse(fileUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(fileName);
            request.setDescription(getString(R.string.downloading, fileName));
            String cleanFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, cleanFileName);

            long downloadId = downloadManager.enqueue(request);
            downloadIdToFileData.put(downloadId, fileData);
            downloadingFiles.put(fileName, downloadId);

            // إظهار رسالة بدء التحميل
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.download_started, fileName), Snackbar.LENGTH_SHORT).show();

            // تتبع تقدم التحميل
            trackDownloadProgress(downloadId, fileData);

        } catch (IllegalArgumentException e) {
            Log.e("FilesActivity", "رابط التحميل غير صالح أو خطأ في الطلب: " + e.getMessage());
            downloadingFiles.remove(fileName);
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.invalid_download_url, Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("FilesActivity", "فشل بدء التحميل: " + e.getMessage(), e);
            downloadingFiles.remove(fileName);
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.download_failed, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * فتح الملف المحمل مع التحقق من سلامته
     */
    private void openFile(FileData fileData) {
        try {
            // تنظيف اسم الملف
            String cleanFileName = fileData.getFileName().replaceAll("[^a-zA-Z0-9._-]", "_");
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), cleanFileName);

            // إضافة سجلات لتتبع الملف
            Log.d("FilesActivity", "اسم الملف المنظف: " + cleanFileName);
            Log.d("FilesActivity", "المسار الكامل: " + file.getAbsolutePath());
            Log.d("FilesActivity", "هل الملف موجود؟ " + file.exists());
            Log.d("FilesActivity", "حجم الملف: " + file.length() + " bytes");

            // التحقق من وجود الملف وحجمه
            if (!file.exists() || file.length() == 0) {
                Log.e("FilesActivity", "الملف غير موجود أو تالف: " + file.getAbsolutePath());
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.file_not_found_or_corrupted, fileData.getFileName()),
                        Snackbar.LENGTH_SHORT).show();
                updateFileList();
                return;
            }

            // توليد URI باستخدام FileProvider
            Uri fileUri = FileProvider.getUriForFile(this, "org.svuonline.lms.fileprovider", file);
            Log.d("FilesActivity", "URI المولد: " + fileUri.toString());

            // تحديد نوع MIME
            String mimeType = getMimeType(fileData.getFileType());
            Log.d("FilesActivity", "MIME Type: " + mimeType);

            // إعداد Intent لفتح الملف
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);

            // محاولة إطلاق الـ Intent
            try {
                startActivity(Intent.createChooser(intent, getString(R.string.open_with)));
                Log.d("FilesActivity", "تم إطلاق الـ Intent بنجاح");
            } catch (android.content.ActivityNotFoundException e) {
                Log.e("FilesActivity", "لا يوجد تطبيق لفتح نوع الملف: " + mimeType);
                Snackbar.make(findViewById(android.R.id.content),
                        R.string.no_app_to_open_file, Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("FilesActivity", "فشل فتح الملف: " + e.getMessage(), e);
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.failed_to_open_file, Snackbar.LENGTH_SHORT).show();
            updateFileList();
        }
    }
    /**
     * إرجاع نوع MIME للملف
     */
    private String getMimeType(String fileType) {
        switch (fileType.toLowerCase()) {
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "mp3": return "audio/mpeg";
            case "mp4": return "video/mp4";
            case "png": return "image/png";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "rar": return "application/x-rar-compressed";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt": return "application/vnd.ms-powerpoint";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "zip": return "application/zip";
            case "txt": return "text/plain";
            case "m": return "text/plain";
            case "pka": return "application/octet-stream";
            default:
                Log.w("FilesActivity", "نوع ملف غير معروف: " + fileType);
                return "application/octet-stream";
        }
    }

    /**
     * تنسيق حجم الملف بالإنجليزية فقط
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " " + getString(R.string.byte_unit);
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String unit = getResources().getStringArray(R.array.file_size_units)[exp - 1];
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024, exp), unit);
    }

    /**
     * إعادة بناء قائمة الملفات لتحديث الواجهة
     */
    private void updateFileList() {
        mainHandler.post(() -> {
            List<FileData> files = getFilesForTool(toolId);
            adapter = new FilesAdapter(FilesActivity.this, files, courseColor, FilesActivity.this);
            recyclerView.setAdapter(adapter);
            Log.d("FilesActivity", "تم إعادة بناء قائمة الملفات");
        });
    }

    /**
     * تتبع تقدم التحميل ومعالجة الاكتمال
     */
    private void trackDownloadProgress(long downloadId, FileData fileData) {
        executorService.execute(() -> {
            while (downloadingFiles.containsValue(downloadId)) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                try (Cursor cursor = downloadManager.query(query)) {
                    if (cursor.moveToFirst()) {
                        int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int bytesDownloadedIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                        int bytesTotalIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                        if (statusIndex == -1 || bytesDownloadedIdx == -1 || bytesTotalIdx == -1) {
                            Log.e("FilesActivity", "أعمدة الحالة أو الحجم مفقودة");
                            break;
                        }

                        int status = cursor.getInt(statusIndex);
                        long bytesDownloaded = cursor.getLong(bytesDownloadedIdx);
                        long bytesTotal = cursor.getLong(bytesTotalIdx);
                        String sizeText = formatFileSize(bytesDownloaded);
                        if (bytesTotal > 0) {
                            sizeText += " / " + formatFileSize(bytesTotal);
                        }
                        Log.d("FilesActivity", "تقدم التحميل: " + sizeText);

                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            Log.d("FilesActivity", "اكتمل التحميل يدويًا: " + fileData.getFileName());
                            downloadingFiles.remove(fileData.getFileName());
                            downloadIdToFileData.remove(downloadId);
                            mainHandler.post(() -> {
                                triggerVibration();
                                Snackbar.make(findViewById(android.R.id.content),
                                        getString(R.string.download_completed1, fileData.getFileName()),
                                        Snackbar.LENGTH_SHORT).show();
                                updateFileList();
                            });
                            break;
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            Log.e("FilesActivity", "فشل التحميل يدويًا: " + fileData.getFileName());
                            downloadingFiles.remove(fileData.getFileName());
                            downloadIdToFileData.remove(downloadId);
                            mainHandler.post(() -> {
                                Snackbar.make(findViewById(android.R.id.content),
                                        getString(R.string.download_failed_with_file, fileData.getFileName()),
                                        Snackbar.LENGTH_SHORT).show();
                            });
                            break;
                        }
                    } else {
                        Log.e("FilesActivity", "لم يتم العثور على بيانات التحميل لـ: " + downloadId);
                        downloadingFiles.remove(fileData.getFileName());
                        downloadIdToFileData.remove(downloadId);
                        break;
                    }
                } catch (Exception e) {
                    Log.e("FilesActivity", "خطأ في تتبع التحميل: " + e.getMessage());
                    downloadingFiles.remove(fileData.getFileName());
                    downloadIdToFileData.remove(downloadId);
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("FilesActivity", "تم مقاطعة تتبع التحميل", e);
                    break;
                }
            }
            Log.d("FilesActivity", "انتهت حلقة تتبع التحميل لـ: " + fileData.getFileName());
        });
    }

    /**
     * تشغيل اهتزاز قصير
     */
    private void triggerVibration() {
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(200);
            }
            Log.d("FilesActivity", "تم تشغيل الاهتزاز");
        } else {
            Log.w("Files - FilesActivity", "جهاز الاهتزاز غير متاح");
        }
    }

    /**
     * استقبال إشعارات اكتمال التحميل
     */
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("FilesActivity", "تم استلام إشعار اكتمال التحميل");
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            FileData fileData = downloadIdToFileData.get(downloadId);
            if (fileData == null) {
                Log.e("FilesActivity", "لم يتم العثور على بيانات الملف لمعرف: " + downloadId);
                return;
            }

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            try (Cursor cursor = downloadManager.query(query)) {
                if (cursor.moveToFirst()) {
                    int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                    if (statusIndex == -1) {
                        Log.e("FilesActivity", "لم يتم العثور على عمود الحالة");
                        handleDownloadFailure(fileData, downloadId, "عمود الحالة مفقود");
                        return;
                    }

                    int status = cursor.getInt(statusIndex);
                    int reason = reasonIndex != -1 ? cursor.getInt(reasonIndex) : -1;
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        Log.d("FilesActivity", "اكتمل تحميل الملف: " + fileData.getFileName());
                        downloadingFiles.remove(fileData.getFileName());
                        mainHandler.post(() -> {
                            triggerVibration();
                            Snackbar.make(findViewById(android.R.id.content),
                                    getString(R.string.download_completed1, fileData.getFileName()),
                                    Snackbar.LENGTH_SHORT).show();
                            updateFileList();
                        });
                    } else {
                        Log.e("FilesActivity", "فشل تحميل الملف: " + fileData.getFileName() + ", السبب: " + reason);
                        handleDownloadFailure(fileData, downloadId, "حالة التحميل: " + status + ", السبب: " + reason);
                    }
                } else {
                    Log.e("FilesActivity", "لم يتم العثور على بيانات التحميل");
                    handleDownloadFailure(fileData, downloadId, "لا توجد بيانات تحميل");
                }
            } catch (Exception e) {
                Log.e("FilesActivity", "خطأ في معالجة التحميل: " + e.getMessage(), e);
                handleDownloadFailure(fileData, downloadId, "استثناء: " + e.getMessage());
            }
            downloadIdToFileData.remove(downloadId);
        }
    };

    /**
     * معالجة فشل التحميل
     */
    private void handleDownloadFailure(FileData fileData, long downloadId, String reason) {
        downloadingFiles.remove(fileData.getFileName());
        mainHandler.post(() -> {
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.download_failed_with_file, fileData.getFileName()),
                    Snackbar.LENGTH_SHORT).show();
        });
        Log.e("FilesActivity", "فشل التحميل: " + fileData.getFileName() + ", التفاصيل: " + reason);
    }

    /**
     * تنظيف الموارد عند إغلاق النشاط
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(downloadReceiver);
        } catch (IllegalArgumentException e) {
            Log.e("FilesActivity", "المتلقي غير مسجل", e);
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}