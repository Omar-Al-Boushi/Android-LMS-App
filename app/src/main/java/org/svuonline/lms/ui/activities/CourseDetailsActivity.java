package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import org.svuonline.lms.R;
import org.svuonline.lms.data.repository.CourseRepository;
import org.svuonline.lms.databinding.ActivityCourseDetailsBinding;
import org.svuonline.lms.ui.adapters.SectionsAdapter;
import org.svuonline.lms.ui.data.CourseData;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

/**
 * نشاط لعرض تفاصيل المقرر والأقسام المرتبطة به.
 */
public class CourseDetailsActivity extends BaseActivity {

    // عناصر الواجهة
    private ActivityCourseDetailsBinding binding;

    // المستودعات
    private CourseRepository courseRepository;

    // بيانات النشاط
    private long userId;
    private String courseCode;
    private boolean isFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCourseDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // تهيئة المكونات
        initComponents();

        // التحقق من بيانات Intent
        if (!validateIntentData()) {
            finish();
            return;
        }

        // تهيئة البيانات والواجهة
        initData();
        setupListeners();
    }

    /**
     * تهيئة المستودعات
     */
    private void initComponents() {
        courseRepository = new CourseRepository(this);
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

        // جلب courseCode
        Intent intent = getIntent();
        courseCode = intent.getStringExtra("course_code");
        if (courseCode == null) {
            showSnackbar(R.string.invalid_course_data);
            return false;
        }

        return true;
    }

    /**
     * تهيئة البيانات (جلب المقرر، تحديث الواجهة، إعداد الأقسام)
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

        // تحديث حالة المفضلة
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();

        // إعداد شريط النظام
        Utils.setSystemBarColorWithColorInt(this, courseData.getHeaderColor(),
                getResources().getColor(R.color.Custom_BackgroundColor), 0);

        // إعداد الواجهة
        setupUI(courseData);
    }

    /**
     * إعداد مستمعات الأحداث (الأزرار)
     */
    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());
        binding.favoriteButton.setOnClickListener(v -> toggleFavorite());
    }

    /**
     * إعداد الواجهة
     * @param courseData بيانات المقرر
     */
    private void setupUI(CourseData courseData) {
        binding.courseCodeTextView.setText(courseData.getCourseCode());
        binding.courseTitleTextView.setText(courseData.getCourseTitle());
        binding.courseHeaderLayout.setBackgroundColor(courseData.getHeaderColor());

        // إعداد RecyclerView للأقسام
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
        binding.favoriteButton.setIconResource(isFavorite ? R.drawable.star_selected : R.drawable.star);
        binding.favoriteButton.setIconTint(ColorStateList.valueOf(Color.WHITE));
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
     * عرض رسالة Snackbar
     * @param messageRes معرف الرسالة
     */
    private void showSnackbar(int messageRes) {
        Snackbar.make(binding.getRoot(), messageRes, Snackbar.LENGTH_LONG).show();
    }

    /**
     * تحديث حالة المفضلة عند استئناف النشاط
     */
    @Override
    protected void onResume() {
        super.onResume();
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();
    }
}