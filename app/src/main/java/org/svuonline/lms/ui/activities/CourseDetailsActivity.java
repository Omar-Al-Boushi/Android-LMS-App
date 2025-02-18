package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.svuonline.lms.R;
import org.svuonline.lms.databinding.ActivityCourseDetailsBinding;
import org.svuonline.lms.ui.adapters.SectionsAdapter;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;
import org.svuonline.lms.ui.data.ButtonData;
import org.svuonline.lms.ui.data.CourseData;
import org.svuonline.lms.ui.data.SectionData;

import java.util.ArrayList;
import java.util.List;

public class CourseDetailsActivity extends BaseActivity {
    private ActivityCourseDetailsBinding binding;
    MaterialButton favoriteButton;
    final boolean[] isFavorite = {false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCourseDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // استقبال البيانات من الـ Intent
        Intent intent = getIntent();
        String courseCode = intent.getStringExtra("course_code");
        String courseName = intent.getStringExtra("course_name");
        int courseColorRes = intent.getIntExtra("course_color", -1);
        favoriteButton = findViewById(R.id.favoriteButton);



        // الحصول على قيمة اللون الفعلية
        int courseColor;
        if (courseColorRes != -1) {
            courseColor = courseColorRes; // قيمة اللون الفعلية
        } else {
            // لون افتراضي
            courseColor = 0xFF005A82; // يمكنك تغيير هذا اللون إذا رغبت
        }
        Utils.setSystemBarColorWithColorInt(this, courseColor, getResources().getColor(R.color.Custom_BackgroundColor),0);


        Log.d("CourseDetailsActivity", "Received courseCode: " + courseCode);
        Log.d("CourseDetailsActivity", "Received courseName: " + courseName);
        Log.d("CourseDetailsActivity", "Received courseColor: " + courseColor);

        // إنشاء بيانات الكورس بناءً على البيانات المستلمة
        CourseData courseData = createCourseData(courseCode, courseName, courseColor);

        // تعيين البيانات في الواجهة
        setupUI(courseData);

        // إعداد زر الرجوع
        binding.backButton.setOnClickListener(v -> finish());
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

    private CourseData createCourseData(String courseCode, String courseName, int courseColor) {
        List<SectionData> sections = new ArrayList<>();

        // القسم الأول: "Course materials"
        String section1Title = getString(R.string.section_course_materials);

        List<ButtonData> section1Buttons = new ArrayList<>();
        section1Buttons.add(new ButtonData(getString(R.string.btn_course_identification), courseColor, "course_identification"));
        section1Buttons.add(new ButtonData(getString(R.string.btn_book_pdf), courseColor, "book_pdf"));
        section1Buttons.add(new ButtonData(getString(R.string.btn_training_exam), courseColor, "training_exam"));
        section1Buttons.add(new ButtonData(getString(R.string.btn_participants), courseColor, "participants_button"));
        section1Buttons.add(new ButtonData(getString(R.string.btn_recorded_sessions), courseColor, "recorded_sessions"));
        section1Buttons.add(new ButtonData(getString(R.string.btn_references), courseColor, "references"));

        sections.add(new SectionData(section1Title, section1Buttons));

        // القسم الثاني: "S24 Semester"
        String section2Title = getString(R.string.section_s24_semester);

        List<ButtonData> section2Buttons = new ArrayList<>();
        section2Buttons.add(new ButtonData(getString(R.string.btn_semester_plan), courseColor, "semester_plan"));
        section2Buttons.add(new ButtonData(getString(R.string.btn_slides_powerpoint), courseColor, "slides_powerpoint"));
        section2Buttons.add(new ButtonData(getString(R.string.btn_assignments), courseColor, "assignments"));
        section2Buttons.add(new ButtonData(getString(R.string.btn_tools), courseColor, "tools"));

        sections.add(new SectionData(section2Title, section2Buttons));

        return new CourseData(courseCode, courseName, courseColor, sections);
    }


    private void setupUI(CourseData courseData) {
        // تعيين بيانات الكورس في العناصر
        binding.courseCodeTextView.setText(courseData.getCourseCode());
        binding.courseTitleTextView.setText(courseData.getCourseTitle());
        binding.courseHeaderLayout.setBackgroundColor(courseData.getHeaderColor());

        // إعداد RecyclerView لعرض الأقسام
        SectionsAdapter sectionsAdapter = new SectionsAdapter(
                this,
                courseData.getSections(),
                courseData.getCourseCode(),
                courseData.getCourseTitle(),
                courseData.getHeaderColor()
        );
        binding.sectionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.sectionRecyclerView.setAdapter(sectionsAdapter);
    }
}
