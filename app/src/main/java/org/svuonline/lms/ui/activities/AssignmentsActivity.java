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
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
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

public class AssignmentsActivity extends BaseActivity implements FilesAdapter.FileDownloadListener {

    // تعريف عناصر الواجهة
    private MaterialTextView courseCodeTextView;
    private MaterialTextView courseTitleTextView;
    private ConstraintLayout courseHeaderLayout;
    private MaterialButton backButton, submitOrEdit, favoriteButton;
    private int courseColor;
    private RecyclerView filesRecyclerView;
    private FilesAdapter adapter;
    private TextView openedDate, dueDate, submissionStatus, gradingStatus, timeRemaining, lastModified, assignmentName;
    private boolean isFavorite;
    private long userId;
    private String toolId;
    private long assignmentId;
    private String courseCode;
    private CourseRepository courseRepository;
    private AssignmentRepository assignmentRepository;
    private AssignmentSubmissionRepository submissionRepository;
    private EnrollmentRepository enrollmentRepository;

    private boolean isDueDatePassed; // متغير لتتبع حالة الموعد النهائي

    // مكونات إدارة التحميل
    private DownloadManager downloadManager;
    private Map<Long, FileData> downloadIdToFileData = new HashMap<>();
    private Map<String, Long> downloadingFiles = new HashMap<>();
    private ExecutorService executorService;
    private Handler mainHandler;
    private Vibrator vibrator;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignments);
        Log.d("AssignmentsActivity", "onCreate started");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // تهيئة المستودعات
        courseRepository = new CourseRepository(this);
        assignmentRepository = new AssignmentRepository(this);
        submissionRepository = new AssignmentSubmissionRepository(this);
        enrollmentRepository = new EnrollmentRepository(this); // إضافة تهيئة EnrollmentRepository

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

        // جلب userId من SharedPreferences
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = userPrefs.getLong("user_id", -1);
        Log.d("AssignmentsActivity", "userId: " + userId);

        // جلب معرف الأداة من Intent
        Intent intent = getIntent();
        toolId = intent.getStringExtra("button_id");
        courseCode = intent.getStringExtra("course_code");
        Log.d("AssignmentsActivity", "معرف الأداة: " + toolId + ", course_code=" + courseCode);
        if (toolId == null || courseCode == null) {
            Log.e("AssignmentsActivity", "معرف الأداة غير موجود أو course_code غير موجود");
            Snackbar.make(findViewById(R.id.main), "بيانات الأداة غير صحيحة", Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        // جلب assignment_id بناءً على tool_id
        assignmentId = assignmentRepository.getAssignmentIdByToolId(toolId);
        Log.d("AssignmentsActivity", "Fetched assignment_id=" + assignmentId + " for tool_id=" + toolId);
        if (assignmentId == -1) {
            Log.e("AssignmentsActivity", "No assignment found for tool_id=" + toolId);
            Snackbar.make(findViewById(R.id.main), R.string.assignment_not_found, Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        // ربط عناصر الواجهة من الـ Layout
        courseCodeTextView = findViewById(R.id.courseCodeTextView);
        courseTitleTextView = findViewById(R.id.courseTitleTextView);
        courseHeaderLayout = findViewById(R.id.courseHeaderLayout);
        backButton = findViewById(R.id.backButton);
        submitOrEdit = findViewById(R.id.submitOrEdit);
        filesRecyclerView = findViewById(R.id.filesRecyclerView);
        openedDate = findViewById(R.id.openedDate);
        dueDate = findViewById(R.id.DueDate);
        submissionStatus = findViewById(R.id.SubmissionStatusDate);
        gradingStatus = findViewById(R.id.GradingStatusDate);
        timeRemaining = findViewById(R.id.TimeRemainingDate);
        lastModified = findViewById(R.id.LastModifiedDate);
        assignmentName = findViewById(R.id.assignmentName);
        favoriteButton = findViewById(R.id.favoriteButton);

        // جلب بيانات الوظيفة من قاعدة البيانات
        boolean isArabic = isArabicLocale();
        Log.d("AssignmentsActivity", "Fetching assignment details for assignment_id=" + assignmentId + ", isArabic=" + isArabic);
        Assignment assignment = assignmentRepository.getAssignmentDetails(assignmentId, isArabic);
        if (assignment == null) {
            Log.e("AssignmentsActivity", "Assignment not found for assignment_id=" + assignmentId);
            Snackbar.make(findViewById(R.id.main), R.string.assignment_not_found, Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d("AssignmentsActivity", "Assignment fetched: title=" + (isArabic ? assignment.getTitleAr() : assignment.getTitleEn()));


        // تعيين بيانات الواجهة
        courseCodeTextView.setText(assignment.getCourseCode());
        courseTitleTextView.setText(assignment.getCourseName());
        assignmentName.setText(isArabic ? assignment.getTitleAr() : assignment.getTitleEn());
        courseColor = assignment.getHeaderColor();
        courseHeaderLayout.setBackgroundColor(courseColor);
        Utils.setSystemBarColorWithColorInt(this, courseColor, getResources().getColor(R.color.Custom_BackgroundColor), 0);
        assignmentName.setTextColor(courseColor);
        submitOrEdit.setBackgroundTintList(ColorStateList.valueOf(courseColor));

        // التحقق من حالة التسجيل
        boolean isEnrolled = enrollmentRepository.isUserEnrolledInCourse(userId, courseCode);
        Log.d("AssignmentsActivity", "Is user enrolled in course " + courseCode + ": " + isEnrolled);

        // تعيين تواريخ الوظيفة
        openedDate.setText(assignment.getOpenDate());
        dueDate.setText(assignment.getDueDate());

        // حساب الوقت المتبقي وإعداد زر الإرسال/التعديل
        timeRemaining.setText(calculateTimeRemaining(assignment.getDueDate()));

        // جلب حالة الإرسال
        Log.d("AssignmentsActivity", "Fetching submission status for assignment_id=" + assignmentId + ", user_id=" + userId);
        AssignmentSubmission submission = submissionRepository.getSubmissionStatus(assignmentId, userId);
        if (submission != null) {
            submissionStatus.setText(submission.getStatus());
            submissionStatus.setTypeface(ResourcesCompat.getFont(this, R.font.cairo_bold));
            submissionStatus.setTextColor(getStatusColor(submission.getStatus()));
            lastModified.setText(submission.getSubmittedAt() != null ? submission.getSubmittedAt() : "-");
            gradingStatus.setText(submission.getGrade() > 0 ? String.format(Locale.getDefault(), "%.2f", submission.getGrade()) : getString(R.string.not_graded));
            Log.d("AssignmentsActivity", "Submission found: status=" + submission.getStatus() + ", submitted_at=" + submission.getSubmittedAt());
        } else {
            submissionStatus.setText(R.string.no_attempt);
            lastModified.setText("-");
            gradingStatus.setText(R.string.not_graded);
            Log.d("AssignmentsActivity", "No submission found for assignment_id=" + assignmentId);
        }

        // إعداد ملف الوظيفة
        List<FileData> files = new ArrayList<>();
        if (assignment.getAssignmentFile() != null && !assignment.getAssignmentFile().isEmpty()) {
            String fileName = assignment.getTitleEn() + ".pdf"; // استخدام العنوان الإنجليزي دائمًا
            files.add(new FileData(assignment.getAssignmentId(), fileName, assignment.getAssignmentFile(), this));
            Log.d("AssignmentsActivity", "Added file: name=" + fileName + ", path=" + assignment.getAssignmentFile());
        } else {
            Log.w("AssignmentsActivity", "No assignment file found for assignment_id=" + assignmentId);
        }
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FilesAdapter(this, files, courseColor, this);
        filesRecyclerView.setAdapter(adapter);

        // إعداد زر الرجوع
        backButton.setOnClickListener(v -> finish());

        submitOrEdit.setOnClickListener(v -> {
            // التحقق من حالة التسجيل
            if (!isEnrolled) {
                Snackbar.make(findViewById(R.id.main),
                        R.string.not_enrolled_in_course, Snackbar.LENGTH_LONG).show();
                Log.d("AssignmentsActivity", "محاولة النقر على زر الإرسال بدون تسجيل في المقرر");
                return;
            }
            // التحقق من الموعد النهائي
            if (isDueDatePassed) {
                Snackbar.make(findViewById(R.id.main),
                        R.string.due_date_passed, Snackbar.LENGTH_LONG).show();
                Log.d("AssignmentsActivity", "محاولة النقر على زر الإرسال بعد انتهاء الموعد النهائي");
                return;
            }
            // إذا تم استيفاء جميع الشروط، انتقل إلى صفحة التحميل
            Intent intentUpload = new Intent(this, AssignmentUploadActivity.class);
            intentUpload.putExtra("course_code", assignment.getCourseCode());
            intentUpload.putExtra("assignment_id", assignmentId);
            intentUpload.putExtra("course_color_value", courseColor);
            Log.d("AssignmentsActivity", "Starting AssignmentUploadActivity with assignment_id=" + assignmentId);
            startActivity(intentUpload);
        });

        // إعداد زر المفضلة
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        Log.d("AssignmentsActivity", "isFavorite: " + isFavorite + " for course_code=" + courseCode);
        updateFavoriteButton();
        favoriteButton.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            courseRepository.setCourseFavorite(userId, courseCode, isFavorite);
            updateFavoriteButton();
            String message = isFavorite ? getString(R.string.added_to_favorites) :
                    getString(R.string.removed_from_favorites);
            Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_SHORT).show();
            Log.d("AssignmentsActivity", "Favorite toggled: isFavorite=" + isFavorite);
        });

        // التحقق مما إذا كانت عملية الرفع تمت
        if (intent.getBooleanExtra("upload_success", false)) {
            String lastModifiedValue = intent.getStringExtra("last_modified");
            String submissionStatusValue = intent.getStringExtra("submission_status");
            String submissionStatusColor = intent.getStringExtra("submission_status_color");
            Log.d("AssignmentsActivity", "Upload success: last_modified=" + lastModifiedValue + ", status=" + submissionStatusValue);

            if (lastModifiedValue != null) {
                lastModified.setText(lastModifiedValue);
            }
            if (submissionStatusValue != null) {
                submissionStatus.setText(submissionStatusValue);
                submissionStatus.setTypeface(ResourcesCompat.getFont(this, R.font.cairo_bold));
            }
            if (submissionStatusColor != null) {
                submissionStatus.setTextColor(Color.parseColor(submissionStatusColor));
            }
        }

        // تسجيل BroadcastReceiver لمراقبة اكتمال التحميل
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        ContextCompat.registerReceiver(this, downloadReceiver, filter, ContextCompat.RECEIVER_EXPORTED);
    }


    /**
     * معالجة النقر على الملف
     */
    @Override
    public void onFileClicked(FileData fileData) {
        Log.d("AssignmentsActivity", "تم النقر على الملف: " + fileData.getFileName() + ", محمل: " + fileData.isDownloaded());
        String fileName = fileData.getFileName();
        if (downloadingFiles.containsKey(fileName)) {
            Snackbar.make(findViewById(R.id.main),
                    getString(R.string.file_download_in_progress, fileName), Snackbar.LENGTH_SHORT).show();
            Log.d("AssignmentsActivity", "الملف قيد التحميل: " + fileName);
            return;
        }
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
        if (!isNetworkAvailable()) {
            Snackbar.make(findViewById(R.id.main),
                    R.string.check_internet_connection, Snackbar.LENGTH_SHORT).show();
            return;
        }

        String fileName = fileData.getFileName();
        if (downloadingFiles.containsKey(fileName)) {
            Snackbar.make(findViewById(R.id.main),
                    R.string.file_already_downloading, Snackbar.LENGTH_SHORT).show();
            return;
        }

        try {
            String fileUrl = fileData.getFilePath();
            Log.d("AssignmentsActivity", "تحميل الملف: " + fileName + " من: " + fileUrl);

            if (!isValidUrl(fileUrl)) {
                Log.e("AssignmentsActivity", "رابط التحميل غير صالح: " + fileUrl);
                Snackbar.make(findViewById(R.id.main),
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

            Snackbar.make(findViewById(R.id.main),
                    getString(R.string.download_started, fileName), Snackbar.LENGTH_SHORT).show();

            trackDownloadProgress(downloadId, fileData);

        } catch (Exception e) {
            Log.e("AssignmentsActivity", "فشل بدء التحميل: " + e.getMessage(), e);
            downloadingFiles.remove(fileName);
            Snackbar.make(findViewById(R.id.main),
                    R.string.download_failed, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * فتح الملف المحمل
     */
    private void openFile(FileData fileData) {
        try {
            String cleanFileName = fileData.getFileName().replaceAll("[^a-zA-Z0-9._-]", "_");
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    cleanFileName);
            Log.d("AssignmentsActivity", "محاولة فتح الملف: " + file.getAbsolutePath());

            if (!file.exists() || file.length() == 0) {
                Log.e("AssignmentsActivity", "الملف غير موجود أو تالف: " + file.getAbsolutePath());
                Snackbar.make(findViewById(R.id.main),
                        getString(R.string.file_not_found_or_corrupted, fileData.getFileName()),
                        Snackbar.LENGTH_SHORT).show();
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
                Log.e("AssignmentsActivity", "لا يوجد تطبيق لفتح نوع الملف: " + mimeType);
                Snackbar.make(findViewById(R.id.main),
                        R.string.no_app_to_open_file, Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("AssignmentsActivity", "فشل فتح الملف: " + e.getMessage(), e);
            Snackbar.make(findViewById(R.id.main),
                    R.string.failed_to_open_file, Snackbar.LENGTH_SHORT).show();
            updateFileList();
        }
    }

    /**
     * إرجاع نوع MIME للملف
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
                Log.w("AssignmentsActivity", "نوع ملف غير معروف: " + fileType);
                return "application/octet-stream";
        }
    }

    /**
     * تتبع تقدم التحميل
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
                            Log.e("AssignmentsActivity", "أعمدة الحالة مفقودة");
                            break;
                        }

                        int status = cursor.getInt(statusIndex);
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            Log.d("AssignmentsActivity", "اكتمل التحميل: " + fileData.getFileName());
                            downloadingFiles.remove(fileData.getFileName());
                            downloadIdToFileData.remove(downloadId);
                            mainHandler.post(() -> {
                                triggerVibration();
                                Snackbar.make(findViewById(R.id.main),
                                        getString(R.string.download_completed1, fileData.getFileName()),
                                        Snackbar.LENGTH_SHORT).show();
                                updateFileList();
                            });
                            break;
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            Log.e("AssignmentsActivity", "فشل التحميل: " + fileData.getFileName());
                            downloadingFiles.remove(fileData.getFileName());
                            downloadIdToFileData.remove(downloadId);
                            mainHandler.post(() -> {
                                Snackbar.make(findViewById(R.id.main),
                                        getString(R.string.download_failed_with_file, fileData.getFileName()),
                                        Snackbar.LENGTH_SHORT).show();
                            });
                            break;
                        }
                    } else {
                        Log.e("AssignmentsActivity", "لم يتم العثور على بيانات التحميل لـ: " + downloadId);
                        downloadingFiles.remove(fileData.getFileName());
                        downloadIdToFileData.remove(downloadId);
                        break;
                    }
                } catch (Exception e) {
                    Log.e("AssignmentsActivity", "خطأ في تتبع التحميل: " + e.getMessage());
                    downloadingFiles.remove(fileData.getFileName());
                    downloadIdToFileData.remove(downloadId);
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("AssignmentsActivity", "تم مقاطعة تتبع التحميل", e);
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
            Log.d("AssignmentsActivity", "تم تشغيل الاهتزاز");
        } else {
            Log.w("AssignmentsActivity", "جهاز الاهتزاز غير متاح");
        }
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
                String fileName = assignment.getTitleEn() + ".pdf"; // استخدام العنوان الإنجليزي دائمًا
                files.add(new FileData(assignment.getAssignmentId(), fileName, assignment.getAssignmentFile(), this));
                Log.d("AssignmentsActivity", "Added file: name=" + fileName + ", path=" + assignment.getAssignmentFile());
            }
            adapter = new FilesAdapter(this, files, courseColor, this);
            filesRecyclerView.setAdapter(adapter);
            Log.d("AssignmentsActivity", "تم إعادة بناء قائمة الملفات");
        });
    }

    /**
     * استقبال إشعارات اكتمال التحميل
     */
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("AssignmentsActivity", "تم استلام إشعار اكتمال التحميل");
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            FileData fileData = downloadIdToFileData.get(downloadId);
            if (fileData == null) {
                Log.e("AssignmentsActivity", "لم يتم العثور على بيانات الملف لمعرف: " + downloadId);
                return;
            }

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            try (Cursor cursor = downloadManager.query(query)) {
                if (cursor.moveToFirst()) {
                    int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (statusIndex == -1) {
                        Log.e("AssignmentsActivity", "لم يتم العثور على عمود الحالة");
                        handleDownloadFailure(fileData, downloadId, "عمود الحالة مفقود");
                        return;
                    }

                    int status = cursor.getInt(statusIndex);
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        Log.d("AssignmentsActivity", "اكتمل تحميل الملف: " + fileData.getFileName());
                        downloadingFiles.remove(fileData.getFileName());
                        mainHandler.post(() -> {
                            triggerVibration();
                            Snackbar.make(findViewById(R.id.main),
                                    getString(R.string.download_completed1, fileData.getFileName()),
                                    Snackbar.LENGTH_SHORT).show();
                            updateFileList();
                        });
                    } else {
                        Log.e("AssignmentsActivity", "فشل تحميل الملف: " + fileData.getFileName());
                        handleDownloadFailure(fileData, downloadId, "حالة التحميل: " + status);
                    }
                } else {
                    Log.e("AssignmentsActivity", "لم يتم العثور على بيانات التحميل");
                    handleDownloadFailure(fileData, downloadId, "لا توجد بيانات تحميل");
                }
            } catch (Exception e) {
                Log.e("AssignmentsActivity", "خطأ في معالجة التحميل: " + e.getMessage(), e);
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
            Snackbar.make(findViewById(R.id.main),
                    getString(R.string.download_failed_with_file, fileData.getFileName()),
                    Snackbar.LENGTH_SHORT).show();
        });
        Log.e("AssignmentsActivity", "فشل التحميل: " + fileData.getFileName() + ", التفاصيل: " + reason);
    }

    private void updateFavoriteButton() {
        favoriteButton.setIconResource(isFavorite ? R.drawable.star_selected : R.drawable.star);
        favoriteButton.setIconTint(ColorStateList.valueOf(Color.WHITE));
    }

    private boolean isArabicLocale() {
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String selectedLanguage = preferences.getString("selected_language", "en");
        Log.d("AssignmentsActivity", "Language: " + selectedLanguage);
        return "ar".equals(selectedLanguage);
    }

    private String calculateTimeRemaining(String dueDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",  Locale.ENGLISH);
            Date due = sdf.parse(dueDate);
            Date now = new Date();
            assert due != null;
            long diff = due.getTime() - now.getTime();
            if (diff <= 0) {
                isDueDatePassed = true; // تعيين حالة الموعد النهائي
                timeRemaining.setTextColor(Color.RED); // تعيين اللون الأحمر
                return getString(R.string.due_date_passed);
            }
            isDueDatePassed = false; // الموعد لم ينته بعد
            timeRemaining.setTextColor(ContextCompat.getColor(this, R.color.Custom_Black)); // لون افتراضي
            long days = diff / (1000 * 60 * 60 * 24);
            long hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
            return String.format( Locale.ENGLISH, "%d days %d hours", days, hours);
        } catch (ParseException e) {
            Log.e("AssignmentsActivity", "Error parsing due date: " + e.getMessage());
            isDueDatePassed = false;
            timeRemaining.setTextColor(ContextCompat.getColor(this, R.color.Custom_Black));
            return "-";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 0. إعادة حساب الوقت المتبقّي إنجليزي
        Assignment assignment = assignmentRepository.getAssignmentDetails(assignmentId, /* isArabic */ false);
        if (assignment != null) {
            timeRemaining.setText(calculateTimeRemaining(assignment.getDueDate()));
        }

        // 1. جلب حالة الإرسال
        AssignmentSubmission submission = submissionRepository.getSubmissionStatus(assignmentId, userId);

        if (submission != null) {
            // أ. حالة الإرسال
            submissionStatus.setText(submission.getStatus());
            submissionStatus.setTypeface(ResourcesCompat.getFont(this, R.font.cairo_bold));
            submissionStatus.setTextColor(getStatusColor(submission.getStatus()));

            // ب. آخر تعديل بصيغة d-M-yyyy
            String submittedAt = submission.getSubmittedAt();
            if (submittedAt != null && !submittedAt.isEmpty()) {
                try {
                    SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    Date date = parser.parse(submittedAt);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-M-d", Locale.ENGLISH);
                    lastModified.setText(formatter.format(date));
                } catch (ParseException e) {
                    lastModified.setText(submittedAt);
                }
            } else {
                lastModified.setText("-");
            }
        } else {
            // لم تُرسل بعد
            submissionStatus.setText(R.string.no_attempt);
            lastModified.setText("-");
        }

        // 2. إعادة بناء قائمة الملفات
        updateFileList();
    }





    private int getStatusColor(String status) {
        if (status == null) return ContextCompat.getColor(this, R.color.Custom_Black);
        switch (status.toLowerCase()) {
            case "submitted":
                return ContextCompat.getColor(this, R.color.colorCustomColor3);
            case "late":
                return ContextCompat.getColor(this, R.color.colorCustomColor1);
            default:
                return ContextCompat.getColor(this, R.color.Custom_Black);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(downloadReceiver);
        } catch (IllegalArgumentException e) {
            Log.e("AssignmentsActivity", "المتلقي غير مسجل", e);
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}