package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import org.svuonline.lms.R;
import org.svuonline.lms.data.repository.CourseRepository;
import org.svuonline.lms.databinding.ActivityCourseDetailsBinding;
import org.svuonline.lms.ui.adapters.SectionsAdapter;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;
import org.svuonline.lms.ui.data.CourseData;

public class CourseDetailsActivity extends BaseActivity {
    private ActivityCourseDetailsBinding binding;
    private CourseRepository courseRepository;
    private long userId;
    private boolean isFavorite;
    private String courseCode; // إضافة متغير لتخزين courseCode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCourseDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // تهيئة المستودع
        courseRepository = new CourseRepository(this);

        // جلب userId من SharedPreferences
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = userPrefs.getLong("user_id", -1);

        // استقبال البيانات من الـ Intent
        Intent intent = getIntent();
        courseCode = intent.getStringExtra("course_code");

        // جلب بيانات المقرر من قاعدة البيانات
        boolean isArabic = isArabicLocale();
        CourseData courseData = courseRepository.getCourseData(courseCode, isArabic);
        if (courseData == null) {
            Snackbar.make(binding.getRoot(), R.string.course_not_found, Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        // التحقق من حالة المفضلة
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();

        // تعيين اللون في شريط النظام
        Utils.setSystemBarColorWithColorInt(this, courseData.getHeaderColor(),
                getResources().getColor(R.color.Custom_BackgroundColor), 0);

        // تعيين البيانات في الواجهة
        setupUI(courseData);

        // إعداد زر الرجوع
        binding.backButton.setOnClickListener(v -> finish());

        // إعداد زر المفضلة
        binding.favoriteButton.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            courseRepository.setCourseFavorite(userId, courseCode, isFavorite);
            updateFavoriteButton();
            String message = isFavorite ? getString(R.string.added_to_favorites) :
                    getString(R.string.removed_from_favorites);
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // إعادة التحقق من حالة المفضلة عند استئناف النشاط
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();
    }

    private void setupUI(CourseData courseData) {
        // تعيين بيانات المقرر في العناصر
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

    private void updateFavoriteButton() {
        binding.favoriteButton.setIconResource(isFavorite ? R.drawable.star_selected : R.drawable.star);
        binding.favoriteButton.setIconTint(ColorStateList.valueOf(Color.WHITE));
    }

    private boolean isArabicLocale() {
        SharedPreferences preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String selectedLanguage = preferences.getString("selected_language", "en");
        return "ar".equals(selectedLanguage);
    }
}