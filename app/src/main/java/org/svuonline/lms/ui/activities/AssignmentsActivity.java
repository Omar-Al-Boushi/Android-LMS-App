package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import org.svuonline.lms.R;
import org.svuonline.lms.ui.adapters.FilesAdapter;
import org.svuonline.lms.ui.data.FileData;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.graphics.Color;

public class AssignmentsActivity extends BaseActivity implements FilesAdapter.FileDownloadListener {

    // تعريف عناصر الواجهة
    private MaterialTextView courseCodeTextView;
    private MaterialTextView courseTitleTextView;
    private ConstraintLayout courseHeaderLayout;
    private MaterialButton backButton, submitOrEdit;
    private int courseColor;
    private RecyclerView filesRecyclerView;
    private FilesAdapter adapter;
    private TextView openedDate, dueDate, submissionStatus, gradingStatus, timeRemaining, lastModified, assignmentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignments);

        // استقبال البيانات من الـ Intent
        Intent intent = getIntent();
        String courseCode = intent.getStringExtra("course_code");
        String courseTitle = intent.getStringExtra("course_title");
        int courseColorValue = intent.getIntExtra("course_color_value", -1);

        // تسجيل البيانات المستقبلة لغايات التصحيح (Debugging)
        Log.d("FilesActivity", "courseCode: " + courseCode);
        Log.d("FilesActivity", "courseTitle: " + courseTitle);
        Log.d("FilesActivity", "courseColorValue: " + courseColorValue);

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

        // إنشاء خريطة لربط الألوان الأصلية بالألوان المقابلة
        Map<Integer, Integer> colorMapping = new HashMap<>();
        colorMapping.put(Color.parseColor("#005A82"), R.color.md_theme_primary);       // Custom_MainColorBlue
        colorMapping.put(Color.parseColor("#A18F5A"), R.color.md_theme_tertiary);     // Custom_MainColorGolden
        colorMapping.put(Color.parseColor("#82003F"), R.color.colorCustomColor1);     // Custom_MainColorDarkPink
        colorMapping.put(Color.parseColor("#7C5AA1"), R.color.colorCustomColor2);     // Custom_MainColorPurple
        colorMapping.put(Color.parseColor("#450082"), R.color.colorCustomColor6);     // Custom_MainColorDarkPurple
        colorMapping.put(Color.parseColor("#008259"), R.color.colorCustomColor5);     // Custom_MainColorGreen
        colorMapping.put(Color.parseColor("#008268"), R.color.colorCustomColor3);     // Custom_MainColorTeal
        colorMapping.put(Color.parseColor("#823D00"), R.color.colorCustomColor4);     // Custom_MainColorOrange

        // تحديث نصوص العنوان واللون
        if (courseCode != null) {
            courseCodeTextView.setText(courseCode);
        }
        if (courseTitle != null) {
            courseTitleTextView.setText(courseTitle);
        }

        // تعيين لون رأس الصفحة
        if (courseColorValue != -1) {
            courseColor = courseColorValue; // استخدام قيمة اللون المستلمة
            courseHeaderLayout.setBackgroundColor(courseColor);
        } else {
            // استخدام لون افتراضي في حال عدم توفر اللون
            courseColor = Color.parseColor("#005A82");
            courseHeaderLayout.setBackgroundColor(courseColor);
        }

        // تعيين لون شريط الحالة باستخدام الأداة المساعدة
        Utils.setSystemBarColorWithColorInt(this, courseColor, getResources().getColor(R.color.Custom_BackgroundColor), 0);

        // إعداد زر الرجوع لإنهاء النشاط الحالي
        backButton.setOnClickListener(v -> finish());

        // إعداد RecyclerView لعرض الملفات
        filesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // إنشاء قائمة الملفات وإضافتها إلى RecyclerView
        List<FileData> files = new ArrayList<>();
        files.add(new FileData("Mobile Application.pdf", false, R.drawable.pdf));

        // إعداد الأدابتر وتعيينه إلى RecyclerView
        adapter = new FilesAdapter(this, files, courseColor, this);
        filesRecyclerView.setAdapter(adapter);

        // معالجة حدث النقر على زر الإرسال أو التعديل
        submitOrEdit.setOnClickListener(v -> {
            Intent intentUpload = new Intent(this, AssignmentUploadActivity.class);
            intentUpload.putExtra("course_code", courseCode);
            intentUpload.putExtra("course_title", courseTitle);
            intentUpload.putExtra("course_color_value", courseColorValue);
            startActivity(intentUpload);
        });

        // تعيين لون نص اسم المهمة بناءً على خريطة الألوان
        Integer colorResId = colorMapping.get(courseColorValue);
        if (colorResId != null) {
            int adjustedColorValue = ContextCompat.getColor(this, colorResId);
            assignmentName.setTextColor(adjustedColorValue);
        } else {
            assignmentName.setTextColor(courseColorValue);
        }

        // تعيين القيم النصية لبقية العناصر
        openedDate.setText("25 November 2024");
        dueDate.setText("2 January 2025");
        gradingStatus.setText("Not Graded");
        submissionStatus.setText("No Attempt");
        timeRemaining.setText("30 Days 9 Hours");
        lastModified.setText("-");

        // تعيين لون خلفية زر الإرسال أو التعديل
        submitOrEdit.setBackgroundTintList(ColorStateList.valueOf(courseColorValue));
    }

    @Override
    public void onDownloadRequested(FileData fileData) {
        // معالجة طلب تنزيل الملف (يمكن تنفيذ العمليات اللازمة هنا)
    }
}
