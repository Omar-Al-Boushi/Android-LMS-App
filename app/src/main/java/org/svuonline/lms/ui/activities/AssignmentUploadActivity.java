package org.svuonline.lms.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import org.svuonline.lms.R;
import org.svuonline.lms.data.model.Assignment;
import org.svuonline.lms.data.repository.AssignmentRepository;
import org.svuonline.lms.data.repository.AssignmentSubmissionRepository;
import org.svuonline.lms.data.repository.CourseRepository;
import org.svuonline.lms.data.repository.EnrollmentRepository;
import org.svuonline.lms.ui.adapters.SelectedFilesAdapter;
import org.svuonline.lms.ui.data.CourseData;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * نشاط لرفع ملفات الواجبات مع دعم التعديل والحذف وإدارة المفضلة وعرض الإرسالات السابقة.
 */
public class AssignmentUploadActivity extends BaseActivity {

    // عناصر واجهة المستخدم
    private MaterialTextView courseCodeTextView;
    private MaterialTextView courseTitleTextView;
    private ConstraintLayout courseHeaderContainer;
    private MaterialButton backButton;
    private MaterialButton submitOrEditButton;
    private MaterialButton favoriteButton;
    private ConstraintLayout addEditFilesParent;
    private ConstraintLayout selectFilesParent;
    private RecyclerView selectedFilesRecyclerView;
    private ShapeableImageView addImage;
    private ShapeableImageView editImage;
    private ShapeableImageView removeImage;
    private ConstraintLayout addButtonCard;
    private ConstraintLayout editButtonCard;
    private ConstraintLayout deleteButtonCard;
    private NestedScrollView nestedScrollView;


    // بيانات النشاط
    private long userId;
    private long assignmentId;
    private String courseCode;
    private String courseTitle;
    private String assignmentTitle;
    private int courseColor;
    private boolean isFavorite;
    private List<Uri> selectedFiles = new ArrayList<>();
    private SelectedFilesAdapter selectedFilesAdapter;
    private int positionToEdit = -1;
    private boolean isEditMode = false;
    private boolean isRemoveMode = false;

    // المستودعات
    private CourseRepository courseRepository;
    private AssignmentRepository assignmentRepository;
    private AssignmentSubmissionRepository submissionRepository;
    private EnrollmentRepository enrollmentRepository;

    // ثوابت
    private static final int PICK_FILES_REQUEST_CODE = 1;
    private static final int PICK_FILE_FOR_EDIT_REQUEST_CODE = 2;
    private static final double MAX_FILE_SIZE_MB = 200.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_assignment_upload);

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
    }

    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // الحصول على أبعاد شريط الحالة (من الأعلى) وشريط التنقل (من الأسفل)
            int systemBarsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // تطبيق padding على ترويسة المقرر (courseHeaderLayout)
            // لتجنب اختفاء الأزرار خلف شريط الحالة.
            courseHeaderContainer.setPadding(0, systemBarsTop, 0, 0);
            nestedScrollView.setPadding(0, 0, 0, systemBarsBottom);

            // نرجع الـ insets الأصلية للسماح للنظام بمواصلة معالجتها
            return WindowInsetsCompat.CONSUMED;
        });
    }


    /**
     * تهيئة المستودعات
     */
    private void initComponents() {
        courseRepository = new CourseRepository(this);
        assignmentRepository = new AssignmentRepository(this);
        submissionRepository = new AssignmentSubmissionRepository(this);
        enrollmentRepository = new EnrollmentRepository(this);
    }

    /**
     * التحقق من صحة بيانات Intent
     * @return صحيح إذا كانت البيانات صالحة، خطأ إذا لزم إنهاء النشاط
     */
    private boolean validateIntentData() {
        // جلب userId من SharedPreferences
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = preferences.getLong("user_id", -1);

        // جلب بيانات Intent
        Intent intent = getIntent();
        assignmentId = intent.getLongExtra("assignment_id", -1);
        courseCode = intent.getStringExtra("course_code");
        courseTitle = intent.getStringExtra("course_title");
        courseColor = intent.getIntExtra("course_color_value", -1);

        if (assignmentId == -1 || courseCode == null) {
            showSnackbar(R.string.invalid_assignment_data);
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
        addEditFilesParent = findViewById(R.id.addEditFilesParent);
        selectFilesParent = findViewById(R.id.selectFilesParent);
        selectedFilesRecyclerView = findViewById(R.id.selectedFilesRecyclerView);
        addButtonCard = findViewById(R.id.addBtn1);
        editButtonCard = findViewById(R.id.editBtn1);
        deleteButtonCard = findViewById(R.id.removeBtn1);
        addImage = findViewById(R.id.addImg);
        editImage = findViewById(R.id.editImg);
        removeImage = findViewById(R.id.removeImg);
        nestedScrollView = findViewById(R.id.nestedScrollView);
    }

    /**
     * تهيئة البيانات (جلب الواجب، تحديث الواجهة، إعداد الملفات)
     */
    private void initData() {
        boolean isArabic = isArabicLocale();

        // تعيين أيقونات الأزرار حسب اللغة
        if (isArabic) {
            addImage.setImageResource(R.drawable.add_ar);
            editImage.setImageResource(R.drawable.edit_ar);
            removeImage.setImageResource(R.drawable.remove_ar);
        }

        // جلب تفاصيل الواجب
        Assignment assignment = assignmentRepository.getAssignmentDetails(assignmentId, isArabic);
        if (assignment == null) {
            showSnackbar(R.string.assignment_not_found);
            finish();
            return;
        }

        // تعيين بيانات الواجهة
        courseCodeTextView.setText(assignment.getCourseCode() != null ? assignment.getCourseCode() : "");
        courseTitleTextView.setText(assignment.getCourseName() != null ? assignment.getCourseName() : "");
        assignmentTitle = isArabic ? assignment.getTitleAr() : assignment.getTitleEn();
        if (assignmentTitle == null) {
            assignmentTitle = "";
        }

        // إعداد لون المقرر
        if (courseColor == -1) {
            CourseData courseData = courseRepository.getCourseData(courseCode, isArabic);
            courseColor = courseData != null ? courseData.getHeaderColor() : ContextCompat.getColor(this, R.color.Custom_MainColorBlue);
        }
        courseHeaderContainer.setBackgroundColor(courseColor);
        addEditFilesParent.getBackground().setColorFilter(courseColor, PorterDuff.Mode.SRC_ATOP);
        selectFilesParent.getBackground().setColorFilter(courseColor, PorterDuff.Mode.SRC_ATOP);
        submitOrEditButton.setBackgroundTintList(ColorStateList.valueOf(courseColor));
        Utils.setSystemBarColorWithColorInt(this, courseColor, getResources().getColor(R.color.Custom_BackgroundColor), 0);

        // إعداد قائمة الملفات
        setupRecyclerView();

        // تحميل الإرسالات السابقة
        loadPreviousSubmission();

        // إعداد حالة المفضلة
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();
    }

    /**
     * إعداد مستمعات الأحداث (الأزرار، اختيار الملفات، الإرسال)
     */
    private void setupListeners() {
        // زر الرجوع
        backButton.setOnClickListener(v -> finish());

        // زر الإرسال
        submitOrEditButton.setOnClickListener(v -> handleSubmission());

        // زر المفضلة
        favoriteButton.setOnClickListener(v -> toggleFavorite());

        // إعداد اختيار الملفات
        setupFileSelection();
    }

    /**
     * إعداد قائمة الملفات المختارة
     */
    private void setupRecyclerView() {
        selectedFilesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectedFilesAdapter = new SelectedFilesAdapter(this, selectedFiles, courseColor);
        selectedFilesRecyclerView.setAdapter(selectedFilesAdapter);

        selectedFilesAdapter.setOnItemClickListener(position -> {
            if (isEditMode) {
                positionToEdit = position;
                openFilePicker(PICK_FILE_FOR_EDIT_REQUEST_CODE, false);
                isEditMode = false;
            } else if (isRemoveMode) {
                showDeleteConfirmation(position);
                isRemoveMode = false;
            }
        });
    }

    /**
     * إعداد اختيار الملفات
     */
    private void setupFileSelection() {
        selectFilesParent.setOnClickListener(v -> openFilePicker(PICK_FILES_REQUEST_CODE, true));

        addButtonCard.setOnClickListener(v -> openFilePicker(PICK_FILES_REQUEST_CODE, true));

        editButtonCard.setOnClickListener(v -> {
            if (selectedFiles.isEmpty()) {
                showSnackbar(R.string.no_files_to_edit);
            } else {
                isEditMode = true;
                showSnackbar(R.string.choose_file_to_edit);
            }
        });

        deleteButtonCard.setOnClickListener(v -> {
            if (selectedFiles.isEmpty()) {
                showSnackbar(R.string.no_files_to_delete);
            } else {
                isRemoveMode = true;
                showSnackbar(R.string.choose_file_to_delete);
            }
        });
    }

    /**
     * معالجة النقر على زر الإرسال
     */
    private void handleSubmission() {
        if (selectedFiles.isEmpty()) {
            showSnackbar(R.string.no_files_selected);
            return;
        }

        for (Uri fileUri : selectedFiles) {
            double sizeInMB = getFileSizeInMB(fileUri);
            if (sizeInMB > MAX_FILE_SIZE_MB) {
                showSnackbar(getString(R.string.file_size_exceeds, MAX_FILE_SIZE_MB));
                return;
            }
        }

        showSubmissionConfirmDialog();
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
     * تحميل الإرسال السابق إن وجد
     */
    private void loadPreviousSubmission() {
        String filePaths = submissionRepository.getSubmissionFilePaths(assignmentId, userId);
        if (filePaths != null && !filePaths.isEmpty()) {
            for (String path : filePaths.split(",")) {
                path = path.trim();
                if (!path.isEmpty()) {
                    File file = new File(path);
                    if (file.exists()) {
                        selectedFiles.add(Uri.fromFile(file));
                    }
                }
            }
            if (!selectedFiles.isEmpty()) {
                selectedFilesAdapter.notifyDataSetChanged();
                selectFilesParent.setVisibility(View.GONE);
                addEditFilesParent.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * فتح نافذة اختيار الملفات
     * @param requestCode رمز الطلب
     * @param allowMultiple السماح باختيار ملفات متعددة
     */
    private void openFilePicker(int requestCode, boolean allowMultiple) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        if (allowMultiple) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        startActivityForResult(intent, requestCode);
    }

    /**
     * معالجة نتائج اختيار الملفات
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_FILES_REQUEST_CODE) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri fileUri = data.getClipData().getItemAt(i).getUri();
                        if (!selectedFiles.contains(fileUri)) {
                            selectedFiles.add(fileUri);
                        } else {
                            showSnackbar(R.string.file_already_added);
                        }
                    }
                } else if (data.getData() != null) {
                    Uri fileUri = data.getData();
                    if (!selectedFiles.contains(fileUri)) {
                        selectedFiles.add(fileUri);
                    } else {
                        showSnackbar(R.string.file_already_added);
                    }
                }
                selectedFilesAdapter.notifyDataSetChanged();
                selectFilesParent.setVisibility(View.GONE);
                addEditFilesParent.setVisibility(View.VISIBLE);
            } else if (requestCode == PICK_FILE_FOR_EDIT_REQUEST_CODE) {
                if (data.getData() != null && positionToEdit >= 0) {
                    Uri fileUri = data.getData();
                    if (!selectedFiles.contains(fileUri)) {
                        selectedFiles.set(positionToEdit, fileUri);
                        selectedFilesAdapter.notifyItemChanged(positionToEdit);
                    } else {
                        showSnackbar(R.string.file_already_added);
                    }
                    positionToEdit = -1;
                }
            }
        }
    }

    /**
     * عرض نافذة تأكيد حذف ملف
     * @param position موقع الملف في القائمة
     */
    private void showDeleteConfirmation(int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.are_you_sure_delete)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    selectedFiles.remove(position);
                    selectedFilesAdapter.notifyItemRemoved(position);
                    if (selectedFiles.isEmpty()) {
                        addEditFilesParent.setVisibility(View.GONE);
                        selectFilesParent.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    /**
     * عرض نافذة تأكيد الإرسال
     */
    private void showSubmissionConfirmDialog() {
        View customView = LayoutInflater.from(this).inflate(R.layout.item_dialog_confirm, null);
        MaterialCardView cardView = customView.findViewById(R.id.cardDialogReset);
        cardView.setBackgroundTintList(ColorStateList.valueOf(courseColor));

        TextView message = customView.findViewById(R.id.tvMessage);
        message.setText(R.string.confirm_submission_message);

        MaterialButton btnCancel = customView.findViewById(R.id.btnCancel);
        MaterialButton btnConfirm = customView.findViewById(R.id.btnConfirm);

        Dialog dialog = new Dialog(this);
        dialog.setContentView(customView);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(dialog.getWindow().getAttributes());
            int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160, getResources().getDisplayMetrics());
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int screenWidth = metrics.widthPixels;
            int marginPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            params.width = screenWidth - 2 * marginPx;
            params.height = heightPx;
            params.gravity = Gravity.CENTER;
            dialog.getWindow().setAttributes(params);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            simulateUpload();
        });

        dialog.show();
    }

    /**
     * حساب حجم الملف بالميجابايت
     * @param uri معرف الملف
     * @return الحجم بالميجابايت
     */
    private double getFileSizeInMB(Uri uri) {
        double sizeInBytes = 0;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    sizeInBytes = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception ignored) {
        }
        return sizeInBytes / (1024.0 * 1024.0);
    }

    /**
     * محاكاة عملية رفع الملفات
     */
    private void simulateUpload() {
        showSnackbar(R.string.uploading_files, Snackbar.LENGTH_INDEFINITE);

        new Thread(() -> {
            try {
                Thread.sleep(2000);

                if (!enrollmentRepository.isUserEnrolledInCourse(userId, courseCode)) {
                    runOnUiThread(() -> showSnackbar(R.string.not_enrolled_in_course));
                    return;
                }

                Assignment assignment = assignmentRepository.getAssignmentDetails(assignmentId, isArabicLocale());
                if (assignment == null) {
                    runOnUiThread(() -> showSnackbar(R.string.assignment_not_found));
                    return;
                }
                if (isDueDatePassed(assignment.getDueDate())) {
                    runOnUiThread(() -> showSnackbar(R.string.due_date_passed));
                    return;
                }

                List<String> filePaths = new ArrayList<>();
                for (Uri fileUri : selectedFiles) {
                    String fileName = getFileNameFromUri(fileUri);
                    String filePath = saveFileLocally(fileUri, fileName);
                    if (filePath != null) {
                        filePaths.add(filePath);
                    }
                }

                if (filePaths.isEmpty()) {
                    runOnUiThread(() -> showSnackbar(R.string.file_save_failed));
                    return;
                }

                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String status = isDueDatePassed(assignment.getDueDate()) ? "Late" : "Submitted";

                submissionRepository.insertOrUpdateSubmission(
                        assignmentId,
                        userId,
                        currentDate,
                        String.join(",", filePaths),
                        status,
                        0.0f,
                        0L,
                        null
                );

                runOnUiThread(() -> {
                    showSnackbar(R.string.upload_successful);
                    finish();
                });
            } catch (InterruptedException e) {
                runOnUiThread(() -> showSnackbar(R.string.upload_failed));
            }
        }).start();
    }

    /**
     * التحقق من انتهاء الموعد النهائي
     * @param dueDate تاريخ الموعد النهائي
     * @return صحيح إذا انتهى الموعد
     */
    private boolean isDueDatePassed(String dueDate) {
        if (dueDate == null) {
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date due = sdf.parse(dueDate);
            Date now = new Date();
            return due != null && due.getTime() - now.getTime() <= 0;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * جلب اسم الملف من URI
     * @param uri معرف الملف
     * @return اسم الملف
     */
    private String getFileNameFromUri(Uri uri) {
        String fileName = "file_" + System.currentTimeMillis();
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
        }
        return fileName;
    }

    /**
     * حفظ الملف محليًا
     * @param fileUri معرف الملف
     * @param fileName اسم الملف
     * @return المسار المطلق أو null إذا فشل الحفظ
     */
    private String saveFileLocally(Uri fileUri, String fileName) {
        try {
            File directory = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Submissions");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File destinationFile = new File(directory, fileName);
            try (InputStream in = getContentResolver().openInputStream(fileUri);
                 OutputStream out = new FileOutputStream(destinationFile)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
            return destinationFile.getAbsolutePath();
        } catch (IOException e) {
            return null;
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
     * دالة مساعدة لإظهار Snackbar مع ضبط موضعه ليتجنب شريط التنقل السفلي.
     * @param message الرسالة التي ستظهر.
     * @param duration مدة ظهور الرسالة.
     */
    private void showPositionedSnackbar(String message, int duration) {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, message, duration);

        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(rootView);
        if (insets != null) {
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            View snackbarView = snackbar.getView();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) snackbarView.getLayoutParams();
            params.bottomMargin = bottomInset;
            snackbarView.setLayoutParams(params);
        }

        snackbar.show();
    }

    // --- تم تعديل هذه الدوال ---
    /**
     * عرض رسالة Snackbar
     * @param messageRes معرف الرسالة
     */
    private void showSnackbar(int messageRes) {
        showPositionedSnackbar(getString(messageRes), Snackbar.LENGTH_LONG);
    }

    /**
     * عرض رسالة Snackbar مع نص مخصص
     * @param message النص المخصص
     */
    private void showSnackbar(String message) {
        showPositionedSnackbar(message, Snackbar.LENGTH_SHORT);
    }

    /**
     * عرض رسالة Snackbar مع مدة مخصصة
     * @param messageRes معرف الرسالة
     * @param duration مدة العرض
     */
    private void showSnackbar(int messageRes, int duration) {
        showPositionedSnackbar(getString(messageRes), duration);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // تحديث حالة المفضلة
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();
    }
}