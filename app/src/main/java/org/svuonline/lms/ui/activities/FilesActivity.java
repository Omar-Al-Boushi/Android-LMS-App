package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.os.Handler;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.svuonline.lms.R;
import org.svuonline.lms.ui.adapters.FilesAdapter;
import org.svuonline.lms.ui.data.FileData;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;
import org.svuonline.lms.ui.adapters.FilesAdapter.FileDownloadListener;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class FilesActivity extends BaseActivity implements FileDownloadListener {

    private MaterialTextView courseCodeTextView;
    private MaterialTextView courseTitleTextView;
    private ConstraintLayout courseHeaderLayout;
    private TextView sectionTitle;
    private MaterialButton backButton;
    private RecyclerView recyclerView;
    private int courseColor;
    private String currentButtonId;
    private FilesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        // استقبال البيانات من الـ Intent
        Intent intent = getIntent();
        currentButtonId = intent.getStringExtra("button_id");
        String courseCode = intent.getStringExtra("course_code");
        String courseTitle = intent.getStringExtra("course_title");
        int courseColorValue = intent.getIntExtra("course_color_value", -1);
        String buttonLabel = intent.getStringExtra("button_label");

        // طباعة البيانات للتأكد
        Log.d("FilesActivity", "buttonId: " + currentButtonId);
        Log.d("FilesActivity", "courseCode: " + courseCode);
        Log.d("FilesActivity", "courseTitle: " + courseTitle);
        Log.d("FilesActivity", "courseColorValue: " + courseColorValue);
        Log.d("FilesActivity", "buttonLabel: " + buttonLabel);

        // ربط العناصر من الـ layout
        courseCodeTextView = findViewById(R.id.courseCodeTextView);
        courseTitleTextView = findViewById(R.id.courseTitleTextView);
        courseHeaderLayout = findViewById(R.id.courseHeaderLayout);
        sectionTitle = findViewById(R.id.sectionTitle);
        backButton = findViewById(R.id.backButton);
        recyclerView = findViewById(R.id.filesRecyclerView);

        // تحديث العنوان واللون
        if (courseCode != null) {
            courseCodeTextView.setText(courseCode);
        }
        if (courseTitle != null) {
            courseTitleTextView.setText(courseTitle);
        }
        if (courseColorValue != -1) {
            courseColor = courseColorValue; // قيمة اللون الفعلية
            courseHeaderLayout.setBackgroundColor(courseColor);
        } else {
            // استخدام لون افتراضي
            courseColor = 0xFF005A82;
            courseHeaderLayout.setBackgroundColor(courseColor);
        }
        if (buttonLabel != null) {
            sectionTitle.setText(buttonLabel);
        }

        Utils.setSystemBarColorWithColorInt(this, courseColor, getResources().getColor(R.color.Custom_BackgroundColor), 0);

        // إعداد زر الرجوع
        backButton.setOnClickListener(v -> finish());

        // جلب الملفات بناءً على buttonId
        List<FileData> files = getFilesForButton(currentButtonId);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // إنشاء الأدابتر وتمرير this كـ FileDownloadListener
        adapter = new FilesAdapter(this, files, courseColor, this);
        recyclerView.setAdapter(adapter);
    }

    private List<FileData> getFilesForButton(String buttonId) {
        List<FileData> files = new ArrayList<>();

        if (buttonId == null) {
            // إذا لم يتم تمرير buttonId
            return files;
        }

        switch (buttonId) {
            case "course_identification":
                files.add(new FileData("Course identification.pdf", false, R.drawable.pdf));
                break;

            case "book_pdf":
                files.add(new FileData("chapter 1.pdf", false, R.drawable.pdf));
                files.add(new FileData("chapter 2.pdf", false, R.drawable.pdf));
                files.add(new FileData("chapter 3.pdf", false, R.drawable.pdf));
                files.add(new FileData("chapter 4.pdf", false, R.drawable.pdf));
                files.add(new FileData("chapter 5.pdf", false, R.drawable.pdf));
                files.add(new FileData("chapter 6.pdf", false, R.drawable.pdf));
                break;

            case "training_exam":
                files.add(new FileData("Training Exam 1.pdf", false, R.drawable.pdf));
                files.add(new FileData("Training Exam 2.pdf", false, R.drawable.pdf));
                break;

            case "recorded_sessions":
                files.add(new FileData("Session Number 1.lrec", false, R.drawable.lrec));
                files.add(new FileData("Session Number 2.lrec", false, R.drawable.lrec));
                files.add(new FileData("Session Number 3.lrec", false, R.drawable.lrec));
                files.add(new FileData("Session Number 4.lrec", false, R.drawable.lrec));
                files.add(new FileData("Session Number 5.lrec", false, R.drawable.lrec));
                break;

            case "references":
                files.add(new FileData("Reference 1.pdf", false, R.drawable.pdf));
                files.add(new FileData("Reference 2.pdf", false, R.drawable.pdf));
                break;

            case "semester_plan":
                files.add(new FileData("Semester plan.pdf", false, R.drawable.pdf));
                break;

            case "slides_powerpoint":
                break;

            case "assignments":
                files.add(new FileData("Assignment 1.pdf", false, R.drawable.pdf));
                files.add(new FileData("Assignment 2.pdf", false, R.drawable.pdf));
                break;

            case "tools":
                break;

            default:
                files.add(new FileData(getString(R.string.no_files_available), false, R.drawable.download_done));
                break;
        }

        return files;
    }

    @Override
    public void onDownloadRequested(FileData fileData) {
        // محاكاة عملية التحميل بتأخير زمني
        new Handler().postDelayed(() -> {
            // بعد انتهاء "التحميل"، قم بتحديث حالة الملف
            fileData.setDownloaded(true);
            adapter.notifyDataSetChanged();
        }, 500);

        // إذا كنت ترغب في تنفيذ تحميل فعلي، يمكنك إضافة منطق التحميل هنا
    }
}
