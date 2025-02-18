package org.svuonline.lms.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import org.svuonline.lms.ui.adapters.SelectedFilesAdapter;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssignmentUploadActivity extends BaseActivity {

    // عناصر الواجهة
    private MaterialTextView courseCodeTextView;
    private MaterialTextView courseTitleTextView;
    private ConstraintLayout courseHeaderLayout;
    private MaterialButton backButton, submitOrEdit;
    private int courseColor;
    private ConstraintLayout addEditFilesParent, selectFilesParent;
    private RecyclerView selectedFilesRecyclerView;
    private SelectedFilesAdapter selectedFilesAdapter;
    private List<Uri> selectedFiles = new ArrayList<>();
    String courseCode, courseTitle;
    int courseColorValue;
    MaterialButton favoriteButton;
    final boolean[] isFavorite = {false};

    // أزرار الإضافة والتعديل والحذف
    private ConstraintLayout addButtonCard, editButtonCard, deleteButtonCard;

    private static final int PICK_FILES_REQUEST_CODE = 1;
    private static final int PICK_FILE_FOR_EDIT_REQUEST_CODE = 2;
    private int positionToEdit = -1;
    private boolean isEditMode = false;
    private boolean isRemoveMode = false;

    // الحجم الأقصى للملف بالميجابايت
    private static final double MAX_FILE_SIZE_MB = 200.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_upload);

        // استقبال البيانات من الـ Intent
        Intent intent = getIntent();
        courseCode = intent.getStringExtra("course_code");
        courseTitle = intent.getStringExtra("course_title");
        courseColorValue = intent.getIntExtra("course_color_value", -1);

        // ربط العناصر من الـ layout
        courseCodeTextView = findViewById(R.id.courseCodeTextView);
        courseTitleTextView = findViewById(R.id.courseTitleTextView);
        courseHeaderLayout = findViewById(R.id.courseHeaderLayout);
        backButton = findViewById(R.id.backButton);
        submitOrEdit = findViewById(R.id.submitOrEdit);
        addEditFilesParent = findViewById(R.id.addEditFilesParent);
        selectFilesParent = findViewById(R.id.selectFilesParent);
        selectedFilesRecyclerView = findViewById(R.id.selectedFilesRecyclerView);
        favoriteButton = findViewById(R.id.favoriteButton);

        // ربط أزرار الإضافة والتعديل والحذف
        addButtonCard = findViewById(R.id.addBtn1);
        editButtonCard = findViewById(R.id.editBtn1);
        deleteButtonCard = findViewById(R.id.removeBtn1);

        // تحديث النصوص والألوان
        if (courseCode != null) {
            courseCodeTextView.setText(courseCode);
        }
        if (courseTitle != null) {
            courseTitleTextView.setText(courseTitle);
        }
        if (courseColorValue != -1) {
            courseColor = courseColorValue;
            courseHeaderLayout.setBackgroundColor(courseColor);
            addEditFilesParent.getBackground().setColorFilter(courseColor, PorterDuff.Mode.SRC_ATOP);
            selectFilesParent.getBackground().setColorFilter(courseColor, PorterDuff.Mode.SRC_ATOP);
        } else {
            courseColor = Color.parseColor("#005A82");
            courseHeaderLayout.setBackgroundColor(courseColor);
        }
        Utils.setSystemBarColorWithColorInt(this, courseColor, getResources().getColor(R.color.Custom_BackgroundColor), 0);
        submitOrEdit.setBackgroundTintList(ColorStateList.valueOf(courseColor));

        // إعداد زر الرجوع
        backButton.setOnClickListener(v -> finish());

        // إعداد RecyclerView والأداپتر مع تمرير لون الستروك (courseColor)
        selectedFilesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectedFilesAdapter = new SelectedFilesAdapter(this, selectedFiles, courseColor);
        selectedFilesRecyclerView.setAdapter(selectedFilesAdapter);

        // تعيين مستمع للنقر على عناصر الريسايكلر (للتعديل أو الحذف)
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

        // إخفاء منطقة التعديل والإضافة في البداية
        addEditFilesParent.setVisibility(View.GONE);

        // عند الضغط على منطقة اختيار الملفات
        selectFilesParent.setOnClickListener(v -> openFilePicker(PICK_FILES_REQUEST_CODE, true));

        // زر الإضافة لإضافة ملفات جديدة
        addButtonCard.setOnClickListener(v -> openFilePicker(PICK_FILES_REQUEST_CODE, true));

        // زر التعديل: تفعيل وضع اختيار الملف للتعديل
        editButtonCard.setOnClickListener(v -> {
            if (selectedFiles.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.no_files_to_edit),
                        Snackbar.LENGTH_SHORT).show();
            } else {
                isEditMode = true;
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.choose_file_to_edit),
                        Snackbar.LENGTH_SHORT).show();
            }
        });

        // زر الحذف: تفعيل وضع اختيار الملف للحذف
        deleteButtonCard.setOnClickListener(v -> {
            if (selectedFiles.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.no_files_to_delete),
                        Snackbar.LENGTH_SHORT).show();
            } else {
                isRemoveMode = true;
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.choose_file_to_delete),
                        Snackbar.LENGTH_SHORT).show();
            }
        });

        // عند الضغط على زر "Submit or edit submission"
        submitOrEdit.setOnClickListener(v -> {
            if (selectedFiles.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.no_files_selected),
                        Snackbar.LENGTH_SHORT).show();
                return;
            }
            // التحقق من حجم كل ملف
            for (Uri fileUri : selectedFiles) {
                double sizeInMB = getFileSizeInMB(fileUri);
                if (sizeInMB > MAX_FILE_SIZE_MB) {
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.file_size_exceeds, MAX_FILE_SIZE_MB),
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

            }

            // إذا كانت أحجام الملفات ضمن الحد المسموح، قم بمحاكاة عملية الرفع
            // عرض تأكيد الإرسال
            showSubmissionConfirmDialog();
        });

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFavorite[0] = !isFavorite[0];
                if (isFavorite[0]) {
                    // إضافة الكورس للمفضلة
                    favoriteButton.setIconResource(R.drawable.star_selected);
                    favoriteButton.setIconTint(ColorStateList.valueOf(Color.WHITE));
                    // يمكن إضافة رسالة للمستخدم باستخدام Snackbar أو Toast
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.added_to_favorites),
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    // إزالة الكورس من المفضلة
                    favoriteButton.setIconResource(R.drawable.star);
                    favoriteButton.setIconTint(ColorStateList.valueOf(Color.WHITE));
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.removed_from_favorites),
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFavorite[0] = !isFavorite[0];
                if (isFavorite[0]) {
                    // إضافة الكورس للمفضلة
                    favoriteButton.setIconResource(R.drawable.star_selected);
                    favoriteButton.setIconTint(ColorStateList.valueOf(Color.WHITE));
                    // يمكن إضافة رسالة للمستخدم باستخدام Snackbar أو Toast
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.added_to_favorites),
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    // إزالة الكورس من المفضلة
                    favoriteButton.setIconResource(R.drawable.star);
                    favoriteButton.setIconTint(ColorStateList.valueOf(Color.WHITE));
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.removed_from_favorites),
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    // طريقة فتح مستعرض الملفات
    private void openFilePicker(int requestCode, boolean allowMultiple) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        if (allowMultiple) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        startActivityForResult(intent, requestCode);
    }

    // استقبال نتيجة اختيار الملفات
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
                            Snackbar.make(findViewById(android.R.id.content),
                                    getString(R.string.file_already_added),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }
                } else if (data.getData() != null) {
                    Uri fileUri = data.getData();
                    if (!selectedFiles.contains(fileUri)) {
                        selectedFiles.add(fileUri);
                    } else {
                        Snackbar.make(findViewById(android.R.id.content),
                                getString(R.string.file_already_added),
                                Snackbar.LENGTH_SHORT).show();
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
                        Snackbar.make(findViewById(android.R.id.content),
                                getString(R.string.file_already_added),
                                Snackbar.LENGTH_SHORT).show();
                    }
                    positionToEdit = -1;
                }
            }
        }
    }

    // عرض نافذة تأكيد لحذف الملف
    private void showDeleteConfirmation(final int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.are_you_sure_delete))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    selectedFiles.remove(position);
                    selectedFilesAdapter.notifyItemRemoved(position);
                    if (selectedFiles.isEmpty()) {
                        addEditFilesParent.setVisibility(View.GONE);
                        selectFilesParent.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showSubmissionConfirmDialog() {
        View customView = LayoutInflater.from(this).inflate(R.layout.item_dialog_confirm, null);
        MaterialCardView cardView = customView.findViewById(R.id.cardDialogReset);
        // تغيير tint وليس الخلفية نفسها
        cardView.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.Custom_MainColorBlue));

        TextView message = customView.findViewById(R.id.tvMessage);
        message.setText(R.string.confirm_submission_message);  // نص مناسب للسياق

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

    // الحصول على حجم الملف بوحدة الميجابايت
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
        return sizeInBytes / (1024.0 * 1024.0); // تحويل البايت إلى ميجابايت
    }

    // محاكاة عملية رفع الملفات
    private void simulateUpload() {
        // عرض Snackbar لإعلام المستخدم بأن عملية الرفع جارية
        Snackbar.make(findViewById(android.R.id.content),
                getString(R.string.uploading_files),
                Snackbar.LENGTH_INDEFINITE).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // محاكاة تأخير عملية الرفع (مثلاً 2 ثانية)
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // الحصول على التاريخ الحالي بصيغة مناسبة
                String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
                // تحديث الواجهة على الـ UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // إظهار رسالة نجاح الرفع
                        Snackbar.make(findViewById(android.R.id.content),
                                getString(R.string.upload_successful),
                                Snackbar.LENGTH_SHORT).show();

                        // إنشاء Intent للانتقال إلى صفحة AssignmentsActivity مع تمرير المعطيات المطلوبة
                        Intent intent = new Intent(AssignmentUploadActivity.this, AssignmentsActivity.class);
                        intent.putExtra("upload_success", true);
                        intent.putExtra("last_modified", currentDate);
                        intent.putExtra("submission_status", getString(R.string.submitted_for_grading));
                        intent.putExtra("submission_status_color", "#00822B");
                        // تمرير معطيات الكورس
                        intent.putExtra("course_code", courseCode);
                        intent.putExtra("course_title", courseTitle);
                        intent.putExtra("course_color_value", courseColorValue);

                        startActivity(intent);
                        finish();
                    }
                });
            }
        }).start();
    }

}
