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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.constraintlayout.widget.ConstraintLayout;
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
 * نشاط لعرض وتحميل الملفات الخاصة بأداة معينة في المقرر.
 */
public class FilesActivity extends BaseActivity implements FilesAdapter.FileDownloadListener {

    // عناصر واجهة المستخدم
    private TextView courseCodeTextView;
    private TextView courseTitleTextView;
    private ConstraintLayout courseHeaderContainer;
    private TextView sectionTitle;
    private MaterialButton backButton;
    private MaterialButton favoriteButton;
    private RecyclerView recyclerView;

    // بيانات النشاط
    private long userId;
    private String toolId;
    private String courseCode;
    private int courseColor;
    private boolean isFavorite;

    // مكونات إدارة التحميل
    private FilesAdapter adapter;
    private DownloadManager downloadManager;
    private Map<Long, FileData> downloadIdToFileData = new HashMap<>();
    private Map<String, Long> downloadingFiles = new HashMap<>();
    private ExecutorService executorService;
    private Handler mainHandler;
    private Vibrator vibrator;

    // المستودعات
    private ResourceRepository resourceRepository;
    private CourseRepository courseRepository;
    private SectionToolRepository sectionToolRepository;

    // ثوابت
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        // تهيئة المكونات
        initComponents();

        // التحقق من بيانات Intent
        if (!validateIntentData()) {
            finish();
            return;
        }

        // تهيئة الواجهة والبيانات
        initViews();
        initData();
        setupListeners();

        // تسجيل BroadcastReceiver
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        ContextCompat.registerReceiver(this, downloadReceiver, filter, ContextCompat.RECEIVER_EXPORTED);
    }

    /**
     * تهيئة المستودعات ومكونات التحميل
     */
    private void initComponents() {
        resourceRepository = new ResourceRepository(this);
        courseRepository = new CourseRepository(this);
        sectionToolRepository = new SectionToolRepository(this);

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    /**
     * التحقق من صحة بيانات Intent
     * @return صحيح إذا كانت البيانات صالحة، خطأ إذا لزم إنهاء النشاط
     */
    private boolean validateIntentData() {
        // جلب userId
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = userPrefs.getLong("user_id", -1);
        if (userId == -1) {
            showSnackbar(R.string.user_id_not_found);
            return false;
        }

        // جلب toolId
        Intent intent = getIntent();
        toolId = intent.getStringExtra("button_id");
        if (toolId == null) {
            showSnackbar(R.string.invalid_tool_id);
            return false;
        }

        // جلب courseCode
        try {
            courseCode = sectionToolRepository.getCourseCodeByToolId(Long.parseLong(toolId));
            if (courseCode == null) {
                showSnackbar(R.string.course_not_found);
                return false;
            }
        } catch (NumberFormatException e) {
            showSnackbar(R.string.invalid_tool_id);
            return false;
        }

        return true;
    }

    /**
     * تهيئة عناصر الواجهة
     */
    private void initViews() {
        courseCodeTextView = findViewById(R.id.courseCodeTextView);
        courseTitleTextView = findViewById(R.id.courseTitleTextView);
        courseHeaderContainer = findViewById(R.id.courseHeaderLayout);
        sectionTitle = findViewById(R.id.sectionTitle);
        backButton = findViewById(R.id.backButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        recyclerView = findViewById(R.id.filesRecyclerView);
    }

    /**
     * تهيئة البيانات (جلب المقرر، الأداة، الملفات، تحديث الواجهة)
     */
    private void initData() {
        boolean isArabic = isArabicLocale();

        // جلب بيانات المقرر
        CourseData courseData = courseRepository.getCourseData(courseCode, isArabic);
        if (courseData == null) {
            showSnackbar(R.string.course_not_found);
            finish();
            return;
        }

        // جلب اسم الأداة
        String buttonLabel;
        try {
            buttonLabel = sectionToolRepository.getToolName(Long.parseLong(toolId), isArabic);
        } catch (NumberFormatException e) {
            buttonLabel = null;
        }
        if (buttonLabel == null) {
            buttonLabel = getString(R.string.files);
        }

        // تحديث الواجهة
        courseCodeTextView.setText(courseData.getCourseCode());
        courseTitleTextView.setText(courseData.getCourseTitle());
        courseColor = courseData.getHeaderColor();
        courseHeaderContainer.setBackgroundColor(courseColor);
        sectionTitle.setText(buttonLabel);
        Utils.setSystemBarColorWithColorInt(this, courseColor, getResources().getColor(R.color.Custom_BackgroundColor), 0);

        // إعداد حالة المفضلة
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();

        // إعداد قائمة الملفات
        List<FileData> files = getFilesForTool(toolId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FilesAdapter(this, files, courseColor, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * إعداد مستمعات الأحداث (الأزرار، اختيار الملفات)
     */
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        favoriteButton.setOnClickListener(v -> toggleFavorite());
    }

    /**
     * تبديل حالة المفضلة
     */
    private void toggleFavorite() {
        isFavorite = !isFavorite;
        courseRepository.setCourseFavorite(userId, courseCode, isFavorite);
        updateFavoriteButton();
        int messageRes = isFavorite ? R.string.added_to_favorites : R.string.removed_from_favorites;
        showSnackbar(messageRes);
    }

    /**
     * تحديث أيقونة زر المفضلة
     */
    private void updateFavoriteButton() {
        favoriteButton.setIconResource(isFavorite ? R.drawable.star_selected : R.drawable.star);
        favoriteButton.setIconTint(ColorStateList.valueOf(Color.WHITE));
    }

    /**
     * جلب قائمة الملفات للأداة
     * @param toolId معرف الأداة
     * @return قائمة بيانات الملفات
     */
    private List<FileData> getFilesForTool(String toolId) {
        List<FileData> files = new ArrayList<>();
        try {
            long toolIdLong = Long.parseLong(toolId);
            List<Resource> resources = resourceRepository.getResourcesByToolId(toolIdLong);
            for (Resource resource : resources) {
                files.add(new FileData(resource, this));
            }
        } catch (NumberFormatException ignored) {
        }
        return files;
    }

    /**
     * معالجة النقر على الملف
     * @param fileData بيانات الملف
     */
    @Override
    public void onFileClicked(FileData fileData) {
        if (fileData.isDownloaded()) {
            openFile(fileData);
        } else {
            downloadFile(fileData);
        }
    }

    /**
     * التحقق من الاتصال بالإنترنت
     * @return صحيح إذا كان هناك اتصال
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * التحقق من صلاحية رابط التحميل
     * @param url رابط التحميل
     * @return صحيح إذا كان الرابط صالحًا
     */
    private boolean isValidUrl(String url) {
        return url != null && !url.isEmpty() && (url.startsWith("http://") || url.startsWith("https://"));
    }

    /**
     * بدء تحميل الملف
     * @param fileData بيانات الملف
     */
    private void downloadFile(FileData fileData) {
        if (!isNetworkAvailable()) {
            showSnackbar(R.string.check_internet_connection);
            return;
        }

        String fileName = fileData.getFileName();
        if (downloadingFiles.containsKey(fileName)) {
            showSnackbar(R.string.file_already_downloading);
            return;
        }

        try {
            String fileUrl = fileData.getFilePath();
            if (!isValidUrl(fileUrl)) {
                showSnackbar(R.string.invalid_download_url);
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

            showSnackbar(getString(R.string.download_started, fileName));
            trackDownloadProgress(downloadId, fileData);
        } catch (IllegalArgumentException e) {
            downloadingFiles.remove(fileName);
            showSnackbar(R.string.invalid_download_url);
        } catch (Exception e) {
            downloadingFiles.remove(fileName);
            showSnackbar(R.string.download_failed);
        }
    }

    /**
     * فتح الملف المحمل
     * @param fileData بيانات الملف
     */
    private void openFile(FileData fileData) {
        try {
            String cleanFileName = fileData.getFileName().replaceAll("[^a-zA-Z0-9._-]", "_");
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), cleanFileName);

            if (!file.exists() || file.length() == 0) {
                showSnackbar(getString(R.string.file_not_found_or_corrupted, fileData.getFileName()));
                updateFileList();
                return;
            }

            Uri fileUri = FileProvider.getUriForFile(this, "org.svuonline.lms.fileprovider", file);
            String mimeType = getMimeType(fileData.getFileType());

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);

            try {
                startActivity(Intent.createChooser(intent, getString(R.string.open_with)));
            } catch (android.content.ActivityNotFoundException e) {
                showSnackbar(R.string.no_app_to_open_file);
            }
        } catch (Exception e) {
            showSnackbar(R.string.failed_to_open_file);
            updateFileList();
        }
    }

    /**
     * إرجاع نوع MIME للملف
     * @param fileType نوع الملف
     * @return نوع MIME
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
            default: return "application/octet-stream";
        }
    }

    /**
     * تنسيق حجم الملف
     * @param bytes حجم الملف بالبايت
     * @return الحجم المنسق
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
     * إعادة بناء قائمة الملفات
     */
    private void updateFileList() {
        mainHandler.post(() -> {
            List<FileData> files = getFilesForTool(toolId);
            adapter = new FilesAdapter(FilesActivity.this, files, courseColor, FilesActivity.this);
            recyclerView.setAdapter(adapter);
        });
    }

    /**
     * تتبع تقدم التحميل
     * @param downloadId معرف التحميل
     * @param fileData بيانات الملف
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
                            downloadingFiles.remove(fileData.getFileName());
                            downloadIdToFileData.remove(downloadId);
                            break;
                        }

                        int status = cursor.getInt(statusIndex);
                        long bytesDownloaded = cursor.getLong(bytesDownloadedIdx);
                        long bytesTotal = cursor.getLong(bytesTotalIdx);
                        String sizeText = formatFileSize(bytesDownloaded);
                        if (bytesTotal > 0) {
                            sizeText += " / " + formatFileSize(bytesTotal);
                        }

                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            downloadingFiles.remove(fileData.getFileName());
                            downloadIdToFileData.remove(downloadId);
                            mainHandler.post(() -> {
                                triggerVibration();
                                showSnackbar(getString(R.string.download_completed1, fileData.getFileName()));
                                updateFileList();
                            });
                            break;
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            downloadingFiles.remove(fileData.getFileName());
                            downloadIdToFileData.remove(downloadId);
                            mainHandler.post(() -> showSnackbar(getString(R.string.download_failed_with_file, fileData.getFileName())));
                            break;
                        }
                    } else {
                        downloadingFiles.remove(fileData.getFileName());
                        downloadIdToFileData.remove(downloadId);
                        break;
                    }
                } catch (Exception e) {
                    downloadingFiles.remove(fileData.getFileName());
                    downloadIdToFileData.remove(downloadId);
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
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
        }
    }

    /**
     * استقبال إشعارات اكتمال التحميل
     */
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            FileData fileData = downloadIdToFileData.get(downloadId);
            if (fileData == null) {
                return;
            }

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            try (Cursor cursor = downloadManager.query(query)) {
                if (cursor.moveToFirst()) {
                    int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (statusIndex == -1) {
                        handleDownloadFailure(fileData, downloadId);
                        return;
                    }

                    int status = cursor.getInt(statusIndex);
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        downloadingFiles.remove(fileData.getFileName());
                        mainHandler.post(() -> {
                            triggerVibration();
                            showSnackbar(getString(R.string.download_completed1, fileData.getFileName()));
                            updateFileList();
                        });
                    } else {
                        handleDownloadFailure(fileData, downloadId);
                    }
                } else {
                    handleDownloadFailure(fileData, downloadId);
                }
            } catch (Exception e) {
                handleDownloadFailure(fileData, downloadId);
            }
            downloadIdToFileData.remove(downloadId);
        }
    };

    /**
     * معالجة فشل التحميل
     * @param fileData بيانات الملف
     * @param downloadId معرف التحميل
     */
    private void handleDownloadFailure(FileData fileData, long downloadId) {
        downloadingFiles.remove(fileData.getFileName());
        mainHandler.post(() -> showSnackbar(getString(R.string.download_failed_with_file, fileData.getFileName())));
        downloadIdToFileData.remove(downloadId);
    }

    /**
     * معالجة نتائج طلب الأذونات
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showSnackbar(R.string.storage_permissions_required);
            }
        }
    }

    /**
     * التحقق من اللغة المختارة
     * @return صحيح إذا كانت اللغة عربية
     */
    private boolean isArabicLocale() {
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String selectedLanguage = preferences.getString("selected_language", "en");
        return "ar".equals(selectedLanguage);
    }

    /**
     * تنظيف الموارد
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(downloadReceiver);
        } catch (IllegalArgumentException ignored) {
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /**
     * عرض رسالة Snackbar
     * @param messageRes معرف الرسالة
     */
    private void showSnackbar(int messageRes) {
        Snackbar.make(findViewById(android.R.id.content), messageRes, Snackbar.LENGTH_LONG).show();
    }

    /**
     * عرض رسالة Snackbar مع نص مخصص
     * @param message النص المخصص
     */
    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();
    }
}