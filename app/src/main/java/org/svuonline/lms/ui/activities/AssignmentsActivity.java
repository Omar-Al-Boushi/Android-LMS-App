package org.svuonline.lms.ui.activities;

import android.Manifest;
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

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import org.svuonline.lms.R;
import org.svuonline.lms.data.model.Assignment;
import org.svuonline.lms.data.model.AssignmentSubmission;
import org.svuonline.lms.data.repository.AssignmentRepository;
import org.svuonline.lms.data.repository.AssignmentSubmissionRepository;
import org.svuonline.lms.data.repository.CourseRepository;
import org.svuonline.lms.data.repository.EnrollmentRepository;
import org.svuonline.lms.ui.adapters.FilesAdapter;
import org.svuonline.lms.ui.data.FileData;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * نشاط يعرض تفاصيل الوظيفة، بما في ذلك الملفات، حالة الإرسال، والوقت المتبقي.
 * يتيح تحميل الملفات، إرسال الوظائف، وإدارة المفضلة.
 */
public class AssignmentsActivity extends BaseActivity implements FilesAdapter.FileDownloadListener {

    // تعريف المتغيرات
    // عناصر الواجهة
    private MaterialTextView courseCodeTextView;
    private MaterialTextView courseTitleTextView;
    private ConstraintLayout courseHeaderContainer;
    private MaterialButton backButton;
    private MaterialButton submitOrEditButton;
    private MaterialButton favoriteButton;
    private RecyclerView filesRecyclerView;
    private TextView openedDateTextView;
    private TextView dueDateTextView;
    private TextView submissionStatusTextView;
    private TextView gradingStatusTextView;
    private TextView timeRemainingTextView;
    private TextView lastModifiedTextView;
    private TextView assignmentNameTextView;

    // بيانات النشاط
    private long userId;
    private String toolId;
    private long assignmentId;
    private String courseCode;
    private int courseColor;
    private boolean isFavorite;
    private boolean isDueDatePassed;

    // المستودعات
    private CourseRepository courseRepository;
    private AssignmentRepository assignmentRepository;
    private AssignmentSubmissionRepository submissionRepository;
    private EnrollmentRepository enrollmentRepository;

    // مكونات إدارة التحميل
    private DownloadManager downloadManager;
    private FilesAdapter filesAdapter;
    private Map<Long, FileData> downloadIdToFileData = new HashMap<>();
    private Map<String, Long> downloadingFiles = new HashMap<>();
    private ExecutorService executorService;
    private Handler mainHandler;
    private Vibrator vibrator;

    // ثوابت
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;

    // استقبال إشعارات تحميل الملفات
    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleDownloadComplete(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // --- تفعيل وضع Edge-to-Edge ---
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_assignments);

        // طلب إذن الإشعارات لنظام Android 13 وما فوق
        requestNotificationPermission();

        // تهيئة المكونات
        initComponents();


        // التحقق من بيانات Intent
        if (!validateIntentData()) {
            finish();
            return;
        }

        // تهيئة الواجهة والبيانات
        initViews();

        applyInsets();

        initData();
        setupListeners();

        // تسجيل استقبال إشعارات التحميل
        registerDownloadReceiver();
    }

    /**
     * طلب إذن الإشعارات إذا لزم الأمر
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }


    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // الحصول على أبعاد شريط الحالة (من الأعلى) وشريط التنقل (من الأسفل)
            int systemBarsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // تطبيق padding على ترويسة المقرر (courseHeaderLayout)
            // لتجنب اختفاء الأزرار خلف شريط الحالة.
            courseHeaderContainer.setPadding(0, systemBarsTop, 0, 0);


            // نرجع الـ insets الأصلية للسماح للنظام بمواصلة معالجتها
            return WindowInsetsCompat.CONSUMED;
        });
    }



    /**
     * تهيئة المكونات الأساسية (المستودعات، إدارة التحميل، الاهتزاز)
     */
    private void initComponents() {
        // تهيئة المستودعات
        courseRepository = new CourseRepository(this);
        assignmentRepository = new AssignmentRepository(this);
        submissionRepository = new AssignmentSubmissionRepository(this);
        enrollmentRepository = new EnrollmentRepository(this);

        // تهيئة إدارة التحميل
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
    }

    /**
     * التحقق من صحة بيانات Intent
     * @return صحيح إذا كانت البيانات صالحة، خطأ إذا لزم إنهاء النشاط
     */
    private boolean validateIntentData() {
        // جلب userId من SharedPreferences
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = userPrefs.getLong("user_id", -1);

        // جلب بيانات Intent
        Intent intent = getIntent();
        toolId = intent.getStringExtra("button_id");
        courseCode = intent.getStringExtra("course_code");

        if (toolId == null || courseCode == null) {
            showSnackbar(R.string.invalid_tool_data);
            return false;
        }

        // جلب معرف الوظيفة
        assignmentId = assignmentRepository.getAssignmentIdByToolId(toolId);
        if (assignmentId == -1) {
            showSnackbar(R.string.assignment_not_found);
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
        backButton = findViewById(R.id.backButton);
        submitOrEditButton = findViewById(R.id.submitOrEdit);
        favoriteButton = findViewById(R.id.favoriteButton);
        filesRecyclerView = findViewById(R.id.filesRecyclerView);
        openedDateTextView = findViewById(R.id.openedDate);
        dueDateTextView = findViewById(R.id.DueDate);
        submissionStatusTextView = findViewById(R.id.SubmissionStatusDate);
        gradingStatusTextView = findViewById(R.id.GradingStatusDate);
        timeRemainingTextView = findViewById(R.id.TimeRemainingDate);
        lastModifiedTextView = findViewById(R.id.LastModifiedDate);
        assignmentNameTextView = findViewById(R.id.assignmentName);

        // إعداد RecyclerView
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * تهيئة البيانات (جلب الوظيفة، تحديث الواجهة، إعداد الملفات)
     */
    private void initData() {
        boolean isArabic = isArabicLocale();

        // جلب تفاصيل الوظيفة
        Assignment assignment = assignmentRepository.getAssignmentDetails(assignmentId, isArabic);
        if (assignment == null) {
            showSnackbar(R.string.assignment_not_found);
            finish();
            return;
        }

        // تعيين بيانات الواجهة
        courseCodeTextView.setText(assignment.getCourseCode());
        courseTitleTextView.setText(assignment.getCourseName());
        assignmentNameTextView.setText(isArabic ? assignment.getTitleAr() : assignment.getTitleEn());
        courseColor = assignment.getHeaderColor();
        courseHeaderContainer.setBackgroundColor(courseColor);
        Utils.setSystemBarColorWithColorInt(this, courseColor, getResources().getColor(R.color.Custom_BackgroundColor), 0);
        assignmentNameTextView.setTextColor(courseColor);
        submitOrEditButton.setBackgroundTintList(ColorStateList.valueOf(courseColor));

        // تعيين تواريخ الوظيفة
        openedDateTextView.setText(assignment.getOpenDate());
        dueDateTextView.setText(assignment.getDueDate());
        timeRemainingTextView.setText(calculateTimeRemaining(assignment.getDueDate()));

        // جلب حالة الإرسال
        updateSubmissionStatus();

        // إعداد الملفات
        updateFileList();

        // إعداد حالة المفضلة
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();

        // التحقق من نتيجة الرفع
        handleUploadResult();
    }

    /**
     * إعداد مستمعات الأحداث (الأزرار، المفضلة، الإرسال)
     */
    private void setupListeners() {
        // زر الرجوع
        backButton.setOnClickListener(v -> finish());

        // زر الإرسال/التعديل
        submitOrEditButton.setOnClickListener(v -> handleSubmitOrEdit());

        // زر المفضلة
        favoriteButton.setOnClickListener(v -> toggleFavorite());
    }

    /**
     * معالجة النقر على زر الإرسال/التعديل
     */
    private void handleSubmitOrEdit() {
        // التحقق من حالة التسجيل
        boolean isEnrolled = enrollmentRepository.isUserEnrolledInCourse(userId, courseCode);
        if (!isEnrolled) {
            showSnackbar(R.string.not_enrolled_in_course);
            return;
        }

        // التحقق من الموعد النهائي
        if (isDueDatePassed) {
            showSnackbar(R.string.due_date_passed);
            return;
        }

        // الانتقال إلى نشاط الرفع
        Intent intent = new Intent(this, AssignmentUploadActivity.class);
        intent.putExtra("course_code", courseCode);
        intent.putExtra("assignment_id", assignmentId);
        intent.putExtra("course_color_value", courseColor);
        startActivity(intent);
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
     * إعادة بناء قائمة الملفات
     */
    private void updateFileList() {
        mainHandler.post(() -> {
            boolean isArabic = isArabicLocale();
            Assignment assignment = assignmentRepository.getAssignmentDetails(assignmentId, isArabic);
            List<FileData> files = new ArrayList<>();
            if (assignment != null && assignment.getAssignmentFile() != null && !assignment.getAssignmentFile().isEmpty()) {
                String fileName = assignment.getTitleEn() + ".pdf";
                files.add(new FileData(assignment.getAssignmentId(), fileName, assignment.getAssignmentFile(), this));
            }
            filesAdapter = new FilesAdapter(this, files, courseColor, this);
            filesRecyclerView.setAdapter(filesAdapter);
        });
    }

    /**
     * تحديث حالة الإرسال
     */
    private void updateSubmissionStatus() {
        AssignmentSubmission submission = submissionRepository.getSubmissionStatus(assignmentId, userId);
        if (submission != null) {
            submissionStatusTextView.setText(submission.getStatus());
            submissionStatusTextView.setTypeface(ResourcesCompat.getFont(this, R.font.cairo_bold));
            submissionStatusTextView.setTextColor(getStatusColor(submission.getStatus()));
            lastModifiedTextView.setText(formatDate(submission.getSubmittedAt()));
            gradingStatusTextView.setText(submission.getGrade() > 0 ?
                    String.format(Locale.getDefault(), "%.2f", submission.getGrade()) :
                    getString(R.string.not_graded));
        } else {
            submissionStatusTextView.setText(R.string.no_attempt);
            lastModifiedTextView.setText("-");
            gradingStatusTextView.setText(R.string.not_graded);
        }
    }

    /**
     * تنسيق التاريخ إلى صيغة yyyy-M-d
     * @param dateString التاريخ الأصلي
     * @return التاريخ المنسق أو "-" إذا كان فارغًا
     */
    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "-";
        }
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date date = parser.parse(dateString);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-d", Locale.ENGLISH);
            return formatter.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    /**
     * حساب الوقت المتبقي حتى الموعد النهائي
     * @param dueDate تاريخ الموعد النهائي
     * @return نص يمثل الوقت المتبقي أو رسالة انتهاء الموعد
     */
    private String calculateTimeRemaining(String dueDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date due = sdf.parse(dueDate);
            Date now = new Date();
            assert due != null;
            long diff = due.getTime() - now.getTime();
            if (diff <= 0) {
                isDueDatePassed = true;
                timeRemainingTextView.setTextColor(Color.RED);
                return getString(R.string.due_date_passed);
            }
            isDueDatePassed = false;
            timeRemainingTextView.setTextColor(ContextCompat.getColor(this, R.color.Custom_Black));
            long days = diff / (1000 * 60 * 60 * 24);
            long hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
            return String.format(Locale.ENGLISH, "%d days %d hours", days, hours);
        } catch (ParseException e) {
            isDueDatePassed = false;
            timeRemainingTextView.setTextColor(ContextCompat.getColor(this, R.color.Custom_Black));
            return "-";
        }
    }

    /**
     * إرجاع لون الحالة بناءً على قيمتها
     * @param status حالة الإرسال
     * @return قيمة اللون
     */
    private int getStatusColor(String status) {
        if (status == null) {
            return ContextCompat.getColor(this, R.color.Custom_Black);
        }
        switch (status.toLowerCase()) {
            case "submitted":
                return ContextCompat.getColor(this, R.color.colorCustomColor3);
            case "late":
                return ContextCompat.getColor(this, R.color.colorCustomColor1);
            default:
                return ContextCompat.getColor(this, R.color.Custom_Black);
        }
    }

    /**
     * معالجة نتيجة رفع الوظيفة
     */
    private void handleUploadResult() {
        Intent intent = getIntent();
        if (intent.getBooleanExtra("upload_success", false)) {
            String lastModified = intent.getStringExtra("last_modified");
            String submissionStatus = intent.getStringExtra("submission_status");
            String submissionStatusColor = intent.getStringExtra("submission_status_color");

            if (lastModified != null) {
                lastModifiedTextView.setText(lastModified);
            }
            if (submissionStatus != null) {
                submissionStatusTextView.setText(submissionStatus);
                submissionStatusTextView.setTypeface(ResourcesCompat.getFont(this, R.font.cairo_bold));
            }
            if (submissionStatusColor != null) {
                submissionStatusTextView.setTextColor(Color.parseColor(submissionStatusColor));
            }
        }
    }

    /**
     * التحقق مما إذا كانت اللغة المختارة هي العربية
     * @return صحيح إذا كانت اللغة عربية
     */
    private boolean isArabicLocale() {
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String selectedLanguage = preferences.getString("selected_language", "en");
        return "ar".equals(selectedLanguage);
    }

    /**
     * عرض رسالة Snackbar
     * @param messageRes معرف الرسالة
     */
    private void showSnackbar(int messageRes) {
        Snackbar.make(findViewById(R.id.main), messageRes, Snackbar.LENGTH_LONG).show();
    }

    /**
     * عرض رسالة Snackbar مع نص مخصص
     * @param message النص المخصص
     */
    private void showSnackbar(String message) {
        Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * تسجيل استقبال إشعارات التحميل
     */
    private void registerDownloadReceiver() {
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        ContextCompat.registerReceiver(this, downloadReceiver, filter, ContextCompat.RECEIVER_EXPORTED);
    }

    // دوال إدارة التحميل
    @Override
    public void onFileClicked(FileData fileData) {
        String fileName = fileData.getFileName();
        if (downloadingFiles.containsKey(fileName)) {
            showSnackbar(getString(R.string.file_download_in_progress, fileName));
            return;
        }
        if (fileData.isDownloaded()) {
            openFile(fileData);
        } else {
            downloadFile(fileData);
        }
    }

    /**
     * التحقق من وجود اتصال بالإنترنت
     * @return صحيح إذا كان هناك اتصال
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * التحقق من صحة رابط التحميل
     * @param url الرابط
     * @return صحيح إذا كان الرابط صالحًا
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return url.startsWith("http://") || url.startsWith("https://");
    }

    /**
     * بدء تحميل ملف
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

        } catch (Exception e) {
            downloadingFiles.remove(fileName);
            showSnackbar(R.string.download_failed);
        }
    }

    /**
     * فتح ملف محمل
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
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String mimeType = getMimeType(fileData.getFileType());
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
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default:
                return "application/octet-stream";
        }
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
                        if (statusIndex == -1) {
                            break;
                        }

                        int status = cursor.getInt(statusIndex);
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
     * معالجة اكتمال التحميل
     * @param intent نية تحتوي على معرف التحميل
     */
    private void handleDownloadComplete(Intent intent) {
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
                    handleDownloadFailure(fileData, downloadId, "عمود الحالة مفقود");
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
                    handleDownloadFailure(fileData, downloadId, "حالة التحميل: " + status);
                }
            } else {
                handleDownloadFailure(fileData, downloadId, "لا توجد بيانات تحميل");
            }
        } catch (Exception e) {
            handleDownloadFailure(fileData, downloadId, "استثناء: " + e.getMessage());
        }
        downloadIdToFileData.remove(downloadId);
    }

    /**
     * معالجة فشل التحميل
     * @param fileData بيانات الملف
     * @param downloadId معرف التحميل
     * @param reason سبب الفشل
     */
    private void handleDownloadFailure(FileData fileData, long downloadId, String reason) {
        downloadingFiles.remove(fileData.getFileName());
        mainHandler.post(() -> showSnackbar(getString(R.string.download_failed_with_file, fileData.getFileName())));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // تحديث الوقت المتبقي
        Assignment assignment = assignmentRepository.getAssignmentDetails(assignmentId, false);
        if (assignment != null) {
            timeRemainingTextView.setText(calculateTimeRemaining(assignment.getDueDate()));
        }

        // تحديث حالة الإرسال
        updateSubmissionStatus();

        // تحديث قائمة الملفات
        updateFileList();

        // تحديث حالة المفضلة (إضافة لتحسين السلوك)
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // إلغاء تسجيل متلقي التحميل
        try {
            unregisterReceiver(downloadReceiver);
        } catch (IllegalArgumentException e) {
            // تجاهل الخطأ إذا لم يكن المتلقي مسجلاً
        }

        // إغلاق ExecutorService
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}