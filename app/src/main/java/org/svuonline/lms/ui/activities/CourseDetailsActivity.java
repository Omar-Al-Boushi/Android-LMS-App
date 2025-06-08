package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import org.svuonline.lms.R;
import org.svuonline.lms.data.repository.CourseRepository;
import org.svuonline.lms.databinding.ActivityCourseDetailsBinding;
import org.svuonline.lms.ui.adapters.SectionsAdapter;
import org.svuonline.lms.ui.data.CourseData;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

public class CourseDetailsActivity extends BaseActivity {

    private ActivityCourseDetailsBinding binding;
    private CourseRepository courseRepository;
    private long userId;
    private String courseCode;
    private boolean isFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- تفعيل وضع Edge-to-Edge ---
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityCourseDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- تطبيق المساحات الداخلية (Insets) ---
        applyInsets();

        initComponents();

        if (!validateIntentData()) {
            finish();
            return;
        }

        initData();
        setupListeners();
    }

    /**
     * دالة جديدة لتطبيق الـ Insets بشكل برمجي.
     * هذا يضمن أن محتوى الواجهة لا يتداخل مع أشرطة النظام.
     */
    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            // الحصول على أبعاد شريط الحالة (من الأعلى) وشريط التنقل (من الأسفل)
            int systemBarsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // تطبيق padding على الجزء العلوي من ترويسة المقرر (courseHeaderLayout)
            // حتى لا تختفي الأزرار والنصوص خلف شريط الحالة.
            binding.courseHeaderLayout.setPadding(0, systemBarsTop, 0, 0);

            // نرجع الـ insets الأصلية لنسمح للنظام بمواصلة معالجتها
            return WindowInsetsCompat.CONSUMED;
        });
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
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = userPrefs.getLong("user_id", -1);
        if (userId == -1) {
            showSnackbar(R.string.user_id_not_found);
            return false;
        }

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
        CourseData courseData = courseRepository.getCourseData(courseCode, isArabic);
        if (courseData == null) {
            showSnackbar(R.string.course_not_found);
            finish();
            return;
        }

        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();

        // الآن، هذه الدالة ستعمل بشكل صحيح وموثوق لأننا نتحكم بالنافذة بالكامل
        Utils.setSystemBarColorWithColorInt(this, courseData.getHeaderColor(),
                getResources().getColor(R.color.Custom_BackgroundColor), 0);

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

    @Override
    protected void onResume() {
        super.onResume();
        if (courseCode != null) { // التأكد من أن courseCode ليس null
            isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
            updateFavoriteButton();
        }
    }
}