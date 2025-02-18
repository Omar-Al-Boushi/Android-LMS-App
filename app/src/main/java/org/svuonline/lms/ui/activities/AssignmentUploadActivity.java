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
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// نشاط لرفع ملفات الواجبات مع دعم التعديل والحذف وإدارة المفضلة وعرض الإرسالات السابقة
public class AssignmentUploadActivity extends BaseActivity {

    // عناصر واجهة المستخدم
    private MaterialTextView courseCodeTextView, courseTitleTextView; // نصوص لعرض رمز المقرر وعنوانه
    private ConstraintLayout courseHeaderLayout; // تخطيط رأس المقرر
    private MaterialButton backButton, submitOrEdit, favoriteButton; // أزرار للرجوع، الإرسال/التعديل، وإضافة المفضلة
    private ConstraintLayout addEditFilesParent, selectFilesParent; // تخطيطات لإضافة/تعديل الملفات واختيار الملفات
    private RecyclerView selectedFilesRecyclerView; // قائمة لعرض الملفات المختارة
    private SelectedFilesAdapter selectedFilesAdapter; // محول لإدارة عرض الملفات في القائمة
    private List<Uri> selectedFiles = new ArrayList<>(); // قائمة URIs للملفات المختارة
    private ConstraintLayout addButtonCard, editButtonCard, deleteButtonCard; // أزرار لإضافة، تعديل، وحذف الملفات
    private int courseColor; // لون المقرر
    private String courseCode, courseTitle, assignmentTitle; // رمز المقرر، عنوانه، وعنوان الواجب
    private long assignmentId, userId; // معرف الواجب ومعرف المستخدم
    private boolean isFavorite; // حالة المفضلة (مضاف أم لا)

    // متغيرات لإدارة الملفات
    private static final int PICK_FILES_REQUEST_CODE = 1; // رمز طلب اختيار ملفات متعددة
    private static final int PICK_FILE_FOR_EDIT_REQUEST_CODE = 2; // رمز طلب اختيار ملف للتعديل
    private int positionToEdit = -1; // موقع الملف المراد تعديله
    private boolean isEditMode = false; // حالة وضع التعديل
    private boolean isRemoveMode = false; // حالة وضع الحذف
    private static final double MAX_FILE_SIZE_MB = 200.0; // الحد الأقصى لحجم الملف (ميجابايت)

    // المستودعات لإدارة البيانات
    private CourseRepository courseRepository; // مستودع المقررات
    private AssignmentRepository assignmentRepository; // مستودع الواجبات
    private AssignmentSubmissionRepository submissionRepository; // مستودع إرسالات الواجبات
    private EnrollmentRepository enrollmentRepository; // مستودع التسجيل في المقررات

    // دالة الإنشاء الأساسية للنشاط
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_upload); // تحديد تخطيط الواجهة

        initializeViews(); // تهيئة عناصر الواجهة
        initializeRepositories(); // تهيئة المستودعات
        setupUserId(); // إعداد معرف المستخدم
        loadAssignmentData(); // تحميل بيانات الواجب
        setupRecyclerView(); // إعداد قائمة الملفات
        loadPreviousSubmission(); // تحميل الإرسال السابق إن وجد
        setupFavoriteButton(); // إعداد زر المفضلة
        setupFileSelection(); // إعداد اختيار الملفات
        setupSubmitButton(); // إعداد زر الإرسال
    }

    // تهيئة عناصر واجهة المستخدم
    private void initializeViews() {
        courseCodeTextView = findViewById(R.id.courseCodeTextView); // نص رمز المقرر
        courseTitleTextView = findViewById(R.id.courseTitleTextView); // نص عنوان المقرر
        courseHeaderLayout = findViewById(R.id.courseHeaderLayout); // تخطيط رأس المقرر
        backButton = findViewById(R.id.backButton); // زر الرجوع
        submitOrEdit = findViewById(R.id.submitOrEdit); // زر الإرسال/التعديل
        favoriteButton = findViewById(R.id.favoriteButton); // زر المفضلة
        addEditFilesParent = findViewById(R.id.addEditFilesParent); // تخطيط إضافة/تعديل الملفات
        selectFilesParent = findViewById(R.id.selectFilesParent); // تخطيط اختيار الملفات
        selectedFilesRecyclerView = findViewById(R.id.selectedFilesRecyclerView); // قائمة الملفات
        addButtonCard = findViewById(R.id.addBtn1); // زر إضافة ملف
        editButtonCard = findViewById(R.id.editBtn1); // زر تعديل ملف
        deleteButtonCard = findViewById(R.id.removeBtn1); // زر حذف ملف
    }

    // تهيئة مستودعات البيانات
    private void initializeRepositories() {
        courseRepository = new CourseRepository(this); // إنشاء مستودع المقررات
        assignmentRepository = new AssignmentRepository(this); // إنشاء مستودع الواجبات
        submissionRepository = new AssignmentSubmissionRepository(this); // إنشاء مستودع الإرسالات
        enrollmentRepository = new EnrollmentRepository(this); // إنشاء مستودع التسجيل
    }

    // إعداد معرف المستخدم من التفضيلات المشتركة
    private void setupUserId() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = preferences.getLong("user_id", -1); // جلب معرف المستخدم
        Log.d("AssignmentUpload", "userId: " + userId); // تسجيل معرف المستخدم
    }

    // تحميل بيانات الواجب والمقرر
    private void loadAssignmentData() {
        Intent intent = getIntent();
        assignmentId = intent.getLongExtra("assignment_id", -1); // جلب معرف الواجب
        courseCode = intent.getStringExtra("course_code"); // جلب رمز المقرر
        courseTitle = intent.getStringExtra("course_title"); // جلب عنوان المقرر
        courseColor = intent.getIntExtra("course_color_value", -1); // جلب لون المقرر

        // التحقق من صحة بيانات الواجب
        if (assignmentId == -1 || courseCode == null) {
            Snackbar.make(findViewById(android.R.id.content), R.string.invalid_assignment_data, Snackbar.LENGTH_LONG).show();
            finish(); // إنهاء النشاط إذا كانت البيانات غير صالحة
            return;
        }

        boolean isArabic = isArabicLocale(); // التحقق من اللغة (عربية أم إنجليزية)
        Assignment assignment = assignmentRepository.getAssignmentDetails(assignmentId, isArabic); // جلب تفاصيل الواجب
        if (assignment == null) {
            Snackbar.make(findViewById(android.R.id.content), R.string.assignment_not_found, Snackbar.LENGTH_LONG).show();
            finish(); // إنهاء النشاط إذا لم يتم العثور على الواجب
            return;
        }

        // تعيين بيانات المقرر والواجب مع التحقق من القيم الفارغة
        if (courseCodeTextView != null && assignment.getCourseCode() != null) {
            courseCodeTextView.setText(assignment.getCourseCode()); // تعيين رمز المقرر
        } else {
            Log.w("AssignmentUpload", "courseCodeTextView or courseCode is null");
        }
        if (courseTitleTextView != null && assignment.getCourseName() != null) {
            courseTitleTextView.setText(assignment.getCourseName()); // تعيين عنوان المقرر
        } else {
            Log.w("AssignmentUpload", "courseTitleTextView or courseName is null");
        }
        assignmentTitle = isArabic ? assignment.getTitleAr() : assignment.getTitleEn(); // تعيين عنوان الواجب حسب اللغة
        if (assignmentTitle == null) {
            Log.w("AssignmentUpload", "assignmentTitle is null");
            assignmentTitle = ""; // قيمة افتراضية
        }

        // إذا لم يتم تمرير لون المقرر، جلب اللون من مستودع المقررات
        if (courseColor == -1) {
            CourseData courseData = courseRepository.getCourseData(courseCode, isArabic);
            if (courseData != null) {
                courseColor = courseData.getHeaderColor(); // جلب لون المقرر
            } else {
                courseColor = ContextCompat.getColor(this, R.color.Custom_MainColorBlue); // لون افتراضي
                Log.w("AssignmentUpload", "CourseData is null, using default color");
            }
        }
        courseHeaderLayout.setBackgroundColor(courseColor); // تعيين لون خلفية رأس المقرر
        addEditFilesParent.getBackground().setColorFilter(courseColor, PorterDuff.Mode.SRC_ATOP); // تطبيق اللون على تخطيط الإضافة/التعديل
        selectFilesParent.getBackground().setColorFilter(courseColor, PorterDuff.Mode.SRC_ATOP); // تطبيق اللون على تخطيط اختيار الملفات
        submitOrEdit.setBackgroundTintList(ColorStateList.valueOf(courseColor)); // تطبيق اللون على زر الإرسال
        Utils.setSystemBarColorWithColorInt(this, courseColor, getResources().getColor(R.color.Custom_BackgroundColor), 0); // تعيين لون شريط النظام
    }

    // تحميل الإرسال السابق إن وجد
    private void loadPreviousSubmission() {
        String filePaths = submissionRepository.getSubmissionFilePaths(assignmentId, userId); // جلب مسارات الملفات من الإرسال السابق
        if (filePaths != null && !filePaths.isEmpty()) {
            String[] paths = filePaths.split(","); // تقسيم المسارات إلى مصفوفة
            for (String path : paths) {
                path = path.trim();
                if (!path.isEmpty()) {
                    File file = new File(path);
                    if (file.exists()) {
                        Uri fileUri = Uri.fromFile(file); // تحويل المسار إلى URI
                        selectedFiles.add(fileUri); // إضافة الملف إلى القائمة
                    } else {
                        Log.w("AssignmentUpload", "File does not exist: " + path);
                    }
                }
            }
            if (!selectedFiles.isEmpty()) {
                selectedFilesAdapter.notifyDataSetChanged(); // تحديث قائمة الملفات
                selectFilesParent.setVisibility(View.GONE); // إخفاء تخطيط اختيار الملفات
                addEditFilesParent.setVisibility(View.VISIBLE); // إظهار تخطيط الإضافة/التعديل
            }
        }
    }

    // إعداد زر المفضلة
    private void setupFavoriteButton() {
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode); // التحقق من حالة المفضلة
        updateFavoriteButton(); // تحديث أيقونة الزر

        favoriteButton.setOnClickListener(v -> {
            isFavorite = !isFavorite; // تبديل حالة المفضلة
            courseRepository.setCourseFavorite(userId, courseCode, isFavorite); // تحديث حالة المفضلة في المستودع
            updateFavoriteButton(); // تحديث أيقونة الزر
            String message = isFavorite ? getString(R.string.added_to_favorites) : getString(R.string.removed_from_favorites);
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show(); // عرض رسالة تأكيد
        });
    }

    // تحديث أيقونة زر المفضلة
    private void updateFavoriteButton() {
        favoriteButton.setIconResource(isFavorite ? R.drawable.star_selected : R.drawable.star); // تعيين أيقونة النجمة
        favoriteButton.setIconTint(ColorStateList.valueOf(Color.WHITE)); // تعيين لون الأيقونة
    }

    // إعداد قائمة الملفات المختارة
    private void setupRecyclerView() {
        selectedFilesRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // تعيين إدارة التخطيط الخطي
        selectedFilesAdapter = new SelectedFilesAdapter(this, selectedFiles, courseColor); // إنشاء المحول
        selectedFilesRecyclerView.setAdapter(selectedFilesAdapter); // ربط المحول بالقائمة

        // معالج النقر على عنصر في القائمة
        selectedFilesAdapter.setOnItemClickListener(position -> {
            if (isEditMode) {
                positionToEdit = position; // تحديد موقع الملف للتعديل
                openFilePicker(PICK_FILE_FOR_EDIT_REQUEST_CODE, false); // فتح نافذة اختيار ملف
                isEditMode = false; // إنهاء وضع التعديل
            } else if (isRemoveMode) {
                showDeleteConfirmation(position); // عرض تأكيد الحذف
                isRemoveMode = false; // إنهاء وضع الحذف
            }
        });
    }

    // إعداد اختيار الملفات
    private void setupFileSelection() {
        selectFilesParent.setOnClickListener(v -> openFilePicker(PICK_FILES_REQUEST_CODE, true)); // فتح نافذة اختيار ملفات عند النقر

        addButtonCard.setOnClickListener(v -> openFilePicker(PICK_FILES_REQUEST_CODE, true)); // فتح نافذة اختيار ملفات عند النقر على زر الإضافة

        editButtonCard.setOnClickListener(v -> {
            if (selectedFiles.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), R.string.no_files_to_edit, Snackbar.LENGTH_SHORT).show(); // رسالة إذا لم تكن هناك ملفات
            } else {
                isEditMode = true; // تفعيل وضع التعديل
                Snackbar.make(findViewById(android.R.id.content), R.string.choose_file_to_edit, Snackbar.LENGTH_SHORT).show(); // رسالة لاختيار ملف للتعديل
            }
        });

        deleteButtonCard.setOnClickListener(v -> {
            if (selectedFiles.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), R.string.no_files_to_delete, Snackbar.LENGTH_SHORT).show(); // رسالة إذا لم تكن هناك ملفات
            } else {
                isRemoveMode = true; // تفعيل وضع الحذف
                Snackbar.make(findViewById(android.R.id.content), R.string.choose_file_to_delete, Snackbar.LENGTH_SHORT).show(); // رسالة لاختيار ملف للحذف
            }
        });
    }

    // إعداد زر الإرسال
    private void setupSubmitButton() {
        submitOrEdit.setOnClickListener(v -> {
            if (selectedFiles.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), R.string.no_files_selected, Snackbar.LENGTH_SHORT).show(); // رسالة إذا لم يتم اختيار ملفات
                return;
            }

            // التحقق من حجم الملفات
            for (Uri fileUri : selectedFiles) {
                double sizeInMB = getFileSizeInMB(fileUri);
                if (sizeInMB > MAX_FILE_SIZE_MB) {
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.file_size_exceeds, MAX_FILE_SIZE_MB), Snackbar.LENGTH_LONG).show(); // رسالة إذا تجاوز الحجم الحد
                    return;
                }
            }

            showSubmissionConfirmDialog(); // عرض نافذة تأكيد الإرسال
        });

        backButton.setOnClickListener(v -> finish()); // إنهاء النشاط عند النقر على زر الرجوع
    }

    // فتح نافذة اختيار الملفات
    private void openFilePicker(int requestCode, boolean allowMultiple) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // السماح بجميع أنواع الملفات
        if (allowMultiple) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // السماح باختيار ملفات متعددة
        }
        startActivityForResult(intent, requestCode); // بدء نشاط اختيار الملف
    }

    // معالجة نتائج اختيار الملفات
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_FILES_REQUEST_CODE) {
                // معالجة اختيار ملفات متعددة
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri fileUri = data.getClipData().getItemAt(i).getUri();
                        if (!selectedFiles.contains(fileUri)) {
                            selectedFiles.add(fileUri); // إضافة الملف إذا لم يكن موجودًا
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), R.string.file_already_added, Snackbar.LENGTH_SHORT).show(); // رسالة إذا كان الملف مضافًا
                        }
                    }
                } else if (data.getData() != null) {
                    // معالجة اختيار ملف واحد
                    Uri fileUri = data.getData();
                    if (!selectedFiles.contains(fileUri)) {
                        selectedFiles.add(fileUri); // إضافة الملف
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), R.string.file_already_added, Snackbar.LENGTH_SHORT).show(); // رسالة إذا كان الملف مضافًا
                    }
                }
                selectedFilesAdapter.notifyDataSetChanged(); // تحديث قائمة الملفات
                selectFilesParent.setVisibility(View.GONE); // إخفاء تخطيط اختيار الملفات
                addEditFilesParent.setVisibility(View.VISIBLE); // إظهار تخطيط الإضافة/التعديل
            } else if (requestCode == PICK_FILE_FOR_EDIT_REQUEST_CODE) {
                // معالجة تعديل ملف
                if (data.getData() != null && positionToEdit >= 0) {
                    Uri fileUri = data.getData();
                    if (!selectedFiles.contains(fileUri)) {
                        selectedFiles.set(positionToEdit, fileUri); // استبدال الملف القديم
                        selectedFilesAdapter.notifyItemChanged(positionToEdit); // تحديث العنصر في القائمة
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), R.string.file_already_added, Snackbar.LENGTH_SHORT).show(); // رسالة إذا كان الملف مضافًا
                    }
                    positionToEdit = -1; // إعادة تعيين موقع التعديل
                }
            }
        }
    }

    // عرض نافذة تأكيد حذف ملف
    private void showDeleteConfirmation(final int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete)) // عنوان النافذة
                .setMessage(getString(R.string.are_you_sure_delete)) // رسالة التأكيد
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    selectedFiles.remove(position); // حذف الملف من القائمة
                    selectedFilesAdapter.notifyItemRemoved(position); // تحديث القائمة
                    if (selectedFiles.isEmpty()) {
                        addEditFilesParent.setVisibility(View.GONE); // إخفاء تخطيط الإضافة/التعديل
                        selectFilesParent.setVisibility(View.VISIBLE); // إظهار تخطيط اختيار الملفات
                    }
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss()) // إلغاء الحذف
                .create()
                .show();
    }

    // عرض نافذة تأكيد الإرسال
    private void showSubmissionConfirmDialog() {
        View customView = LayoutInflater.from(this).inflate(R.layout.item_dialog_confirm, null); // تحميل تخطيط النافذة
        MaterialCardView cardView = customView.findViewById(R.id.cardDialogReset);
        cardView.setBackgroundTintList(ColorStateList.valueOf(courseColor)); // تعيين لون الخلفية

        TextView message = customView.findViewById(R.id.tvMessage);
        message.setText(R.string.confirm_submission_message); // تعيين رسالة التأكيد

        MaterialButton btnCancel = customView.findViewById(R.id.btnCancel); // زر الإلغاء
        MaterialButton btnConfirm = customView.findViewById(R.id.btnConfirm); // زر التأكيد

        Dialog dialog = new Dialog(this);
        dialog.setContentView(customView);
        dialog.setCancelable(false); // منع إغلاق النافذة بالنقر خارجها

        // إعداد أبعاد النافذة
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

        btnCancel.setOnClickListener(v -> dialog.dismiss()); // إغلاق النافذة عند النقر على الإلغاء
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss(); // إغلاق النافذة
            simulateUpload(); // بدء عملية الرفع
        });

        dialog.show(); // عرض النافذة
    }

    // حساب حجم الملف بالميجابايت
    private double getFileSizeInMB(Uri uri) {
        double sizeInBytes = 0;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    sizeInBytes = cursor.getLong(sizeIndex); // جلب حجم الملف بالبايت
                }
            }
        } catch (Exception ignored) {
            Log.w("AssignmentUpload", "Error getting file size for URI: " + uri);
        }
        return sizeInBytes / (1024.0 * 1024.0); // تحويل البايت إلى ميجابايت
    }

    // محاكاة عملية رفع الملفات
    private void simulateUpload() {
        Snackbar.make(findViewById(android.R.id.content), R.string.uploading_files, Snackbar.LENGTH_INDEFINITE).show(); // عرض رسالة "جارٍ الرفع"

        new Thread(() -> {
            try {
                Thread.sleep(2000); // محاكاة تأخير الرفع (2 ثانية)

                // التحقق من تسجيل المستخدم في المقرر
                if (!enrollmentRepository.isUserEnrolledInCourse(userId, courseCode)) {
                    runOnUiThread(() -> {
                        Snackbar.make(findViewById(android.R.id.content), R.string.not_enrolled_in_course, Snackbar.LENGTH_LONG).show();
                    });
                    return;
                }

                // التحقق من الموعد النهائي للواجب
                Assignment assignment = assignmentRepository.getAssignmentDetails(assignmentId, isArabicLocale());
                if (assignment == null) {
                    runOnUiThread(() -> {
                        Snackbar.make(findViewById(android.R.id.content), R.string.assignment_not_found, Snackbar.LENGTH_LONG).show();
                    });
                    return;
                }
                if (isDueDatePassed(assignment.getDueDate())) {
                    runOnUiThread(() -> {
                        Snackbar.make(findViewById(android.R.id.content), R.string.due_date_passed, Snackbar.LENGTH_LONG).show();
                    });
                    return;
                }

                // حفظ الملفات محليًا وجمع مساراتها
                List<String> filePaths = new ArrayList<>();
                for (Uri fileUri : selectedFiles) {
                    String fileName = getFileNameFromUri(fileUri); // جلب اسم الملف
                    String filePath = saveFileLocally(fileUri, fileName); // حفظ الملف محليًا
                    if (filePath != null) {
                        filePaths.add(filePath); // إضافة المسار إلى القائمة
                    } else {
                        Log.w("AssignmentUpload", "Failed to save file: " + fileName);
                    }
                }

                // التحقق من نجاح حفظ الملفات
                if (filePaths.isEmpty()) {
                    runOnUiThread(() -> {
                        Snackbar.make(findViewById(android.R.id.content), R.string.file_save_failed, Snackbar.LENGTH_LONG).show();
                    });
                    return;
                }

                // إنشاء إرسال جديد أو تحديث إرسال موجود
                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()); // التاريخ الحالي
                String status = isDueDatePassed(assignment.getDueDate()) ? "Late" : "Submitted"; // حالة الإرسال

                submissionRepository.insertOrUpdateSubmission(
                        assignmentId,
                        userId,
                        currentDate,
                        String.join(",", filePaths), // تجميع مسارات الملفات
                        status,
                        0.0f, // الدرجة (افتراضيًا 0)
                        0L, // معرف المصحح (افتراضيًا 0)
                        null // تعليقات المصحح (افتراضيًا null)
                );

                // تحديث الواجهة بعد الإرسال الناجح
                runOnUiThread(() -> {
                    Snackbar.make(findViewById(android.R.id.content), R.string.upload_successful, Snackbar.LENGTH_SHORT).show(); // رسالة النجاح
                    finish(); // إنهاء النشاط الحالي
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Snackbar.make(findViewById(android.R.id.content), R.string.upload_failed, Snackbar.LENGTH_LONG).show(); // رسالة الفشل
                });
            }
        }).start();
    }

    // التحقق من انتهاء الموعد النهائي
    private boolean isDueDatePassed(String dueDate) {
        if (dueDate == null) {
            Log.w("AssignmentUpload", "Due date is null");
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date due = sdf.parse(dueDate); // تحويل تاريخ الاستحقاق إلى كائن Date
            Date now = new Date(); // التاريخ الحالي
            return due != null && due.getTime() - now.getTime() <= 0; // إذا كان التاريخ قد انتهى
        } catch (ParseException e) {
            Log.e("AssignmentUpload", "Error parsing due date: " + e.getMessage()); // تسجيل خطأ التحويل
            return false;
        }
    }

    // التحقق من اللغة (عربية أم إنجليزية)
    private boolean isArabicLocale() {
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String selectedLanguage = preferences.getString("selected_language", "en"); // جلب اللغة المختارة
        return "ar".equals(selectedLanguage); // إرجاع true إذا كانت اللغة عربية
    }

    // جلب اسم الملف من URI
    private String getFileNameFromUri(Uri uri) {
        String fileName = "file_" + System.currentTimeMillis(); // اسم افتراضي
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex); // جلب اسم الملف الأصلي
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("AssignmentUpload", "Error getting file name: " + e.getMessage()); // تسجيل خطأ
        }
        return fileName;
    }

    // حفظ الملف محليًا
    private String saveFileLocally(Uri fileUri, String fileName) {
        try {
            File directory = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Submissions"); // إنشاء مجلد للتخزين
            if (!directory.exists()) {
                directory.mkdirs(); // إنشاء المجلد إذا لم يكن موجودًا
            }
            File destinationFile = new File(directory, fileName); // إنشاء ملف الوجهة
            try (InputStream in = getContentResolver().openInputStream(fileUri);
                 OutputStream out = new FileOutputStream(destinationFile)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len); // كتابة الملف
                }
            }
            return destinationFile.getAbsolutePath(); // إرجاع المسار المطلق
        } catch (IOException e) {
            Log.e("AssignmentUpload", "Error saving file: " + e.getMessage()); // تسجيل خطأ
            return null;
        }
    }
}