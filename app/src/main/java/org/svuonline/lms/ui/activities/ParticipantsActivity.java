package org.svuonline.lms.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.svuonline.lms.R;
import org.svuonline.lms.data.repository.CourseRepository;
import org.svuonline.lms.data.repository.EnrollmentRepository;
import org.svuonline.lms.ui.adapters.ParticipantsAdapter;
import org.svuonline.lms.ui.data.ParticipantData;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.util.List;

/**
 * نشاط لعرض المشاركين في المقرر مع دعم البحث وإدارة المفضلة.
 */
public class ParticipantsActivity extends BaseActivity {

    // عناصر واجهة المستخدم
    private TextView courseCodeTextView;
    private TextView courseTitleTextView;
    private ConstraintLayout courseHeaderContainer;
    private TextView sectionTitle;
    private MaterialButton backButton;
    private MaterialButton favoriteButton;
    private RecyclerView recyclerView;
    private TextInputEditText searchBar;
    private TextInputLayout textInputLayout;
    private NestedScrollView nestedScrollView;


    // بيانات النشاط
    private String courseCode;
    private String courseTitle;
    private String buttonLabel;
    private int courseColor;
    private long userId;
    private boolean isFavorite;
    private boolean isArabic;

    // المستودعات
    private EnrollmentRepository enrollmentRepository;
    private CourseRepository courseRepository;

    // مكونات إضافية
    private ParticipantsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_participants);

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

    /**
     * دالة لتطبيق المساحات الداخلية (Insets) بشكل برمجي.
     * هذا يضمن أن محتوى الواجهة لا يتداخل مع أشرطة النظام.
     */
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
        enrollmentRepository = new EnrollmentRepository(this);
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

        // جلب اللغة
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        isArabic = "ar".equals(prefs.getString("selected_language", "en"));

        // جلب بيانات Intent
        Intent intent = getIntent();
        courseCode = intent.getStringExtra("course_code");
        courseTitle = intent.getStringExtra("course_title");
        courseColor = intent.getIntExtra("course_color_value", -1);
        buttonLabel = intent.getStringExtra("button_label");

        if (courseCode == null) {
            showSnackbar(R.string.course_not_found);
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
        sectionTitle = findViewById(R.id.sectionTitle);
        backButton = findViewById(R.id.backButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        recyclerView = findViewById(R.id.filesRecyclerView);
        searchBar = findViewById(R.id.search_bar);
        textInputLayout = findViewById(R.id.outlinedTextField);
        nestedScrollView = findViewById(R.id.nestedScrollView);
    }

    /**
     * تهيئة البيانات (إعداد الواجهة، المشاركين، المفضلة)
     */
    private void initData() {
        // إعداد الواجهة
        setupUI();

        // إعداد حالة المفضلة
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();

        // إعداد المشاركين
        setupParticipants();

        // إعداد RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * إعداد مستمعات الأحداث (الأزرار، شريط البحث)
     */
    private void setupListeners() {
        // زر الرجوع
        backButton.setOnClickListener(v -> finish());

        // زر المفضلة
        favoriteButton.setOnClickListener(v -> toggleFavorite());

        // إعداد شريط البحث
        setupSearchBar();
    }

    /**
     * إعداد الواجهة
     */
    private void setupUI() {
        courseCodeTextView.setText(courseCode);
        courseTitleTextView.setText(courseTitle != null ? courseTitle : "");
        sectionTitle.setText(buttonLabel != null ? buttonLabel : getString(R.string.btn_participants));

        if (courseColor == -1) {
            courseColor = 0xFF005A82; // اللون الافتراضي
        }
        courseHeaderContainer.setBackgroundColor(courseColor);
        Utils.setSystemBarColorWithColorInt(this, courseColor, getResources().getColor(R.color.Custom_BackgroundColor), 0);
    }

    /**
     * إعداد قائمة المشاركين
     */
    private void setupParticipants() {
        int courseId = courseRepository.getCourseIdByCode(courseCode);
        if (courseId == -1) {
            showSnackbar(R.string.course_not_found);
            finish();
            return;
        }
        List<ParticipantData> participants = enrollmentRepository.getParticipantsByCourseId(courseId, null, isArabic);
        adapter = new ParticipantsAdapter(this, participants, courseCode);
        recyclerView.setAdapter(adapter);
    }

    /**
     * إعداد شريط البحث
     */
    private void setupSearchBar() {
        searchBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                textInputLayout.setStartIconTintList(AppCompatResources.getColorStateList(this, R.color.md_theme_primary));
                textInputLayout.setStartIconDrawable(R.drawable.searchselect);
                new Handler(Looper.getMainLooper()).postDelayed(() -> showKeyboard(searchBar), 100);
            } else {
                textInputLayout.setStartIconTintList(AppCompatResources.getColorStateList(this, R.color.Med_Grey));
                textInputLayout.setStartIconDrawable(R.drawable.search);
            }
        });

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(v.getText().toString());
                hideKeyboard();
                searchBar.clearFocus();
                return true;
            }
            return false;
        });

        textInputLayout.setEndIconOnClickListener(v -> {
            searchBar.setText("");
            performSearch("");
            hideKeyboard();
            searchBar.clearFocus();
        });
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
     * تنفيذ البحث عن المشاركين
     * @param query نص البحث
     */
    private void performSearch(String query) {
        int courseId = courseRepository.getCourseIdByCode(courseCode);
        List<ParticipantData> filteredParticipants = enrollmentRepository.getParticipantsByCourseId(courseId, query, isArabic);
        adapter.updateParticipants(filteredParticipants);
    }

    /**
     * إخفاء لوحة المفاتيح
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
    }

    /**
     * إظهار لوحة المفاتيح
     * @param view العنصر المراد إظهار لوحة المفاتيح له
     */
    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * معالجة أحداث اللمس لإخفاء لوحة المفاتيح
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof TextInputEditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    hideKeyboard();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * تحديث حالة المفضلة عند استئناف النشاط
     */
    @Override
    protected void onResume() {
        super.onResume();
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();
        // إعداد المشاركين
        setupParticipants();
    }

    /**
     * دالة مساعدة لإظهار Snackbar مع ضبط موضعه ليتجنب شريط التنقل السفلي.
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

    /**
     * عرض رسالة Snackbar
     * @param messageRes معرف الرسالة
     */
    private void showSnackbar(int messageRes) {
        showPositionedSnackbar(getString(messageRes), Snackbar.LENGTH_LONG);
    }
}