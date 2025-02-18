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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
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

public class ParticipantsActivity extends BaseActivity {
    private TextView courseCodeTextView;
    private TextView courseTitleTextView;
    private ConstraintLayout courseHeaderLayout;
    private TextView sectionTitle;
    private MaterialButton backButton;
    private MaterialButton favoriteButton;
    private RecyclerView recyclerView;
    private TextInputEditText searchBar;
    private TextInputLayout textInputLayout;
    private ParticipantsAdapter adapter;
    private EnrollmentRepository enrollmentRepository;
    private CourseRepository courseRepository;
    private String courseCode;
    private boolean isArabic;
    private int courseColor;
    private long userId;
    private boolean isFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants);

        // تهيئة المستودعات
        enrollmentRepository = new EnrollmentRepository(this);
        courseRepository = new CourseRepository(this);

        // جلب userId من SharedPreferences
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = userPrefs.getLong("user_id", -1);

        // جلب اللغة من SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        isArabic = "ar".equals(prefs.getString("selected_language", "en"));

        // ربط العناصر من الـ layout
        courseCodeTextView = findViewById(R.id.courseCodeTextView);
        courseTitleTextView = findViewById(R.id.courseTitleTextView);
        courseHeaderLayout = findViewById(R.id.courseHeaderLayout);
        sectionTitle = findViewById(R.id.sectionTitle);
        backButton = findViewById(R.id.backButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        recyclerView = findViewById(R.id.filesRecyclerView);
        searchBar = findViewById(R.id.search_bar);
        textInputLayout = findViewById(R.id.outlinedTextField);

        // تهيئة RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // استقبال البيانات من الـ Intent
        Intent intent = getIntent();
        String buttonId = intent.getStringExtra("button_id");
        courseCode = intent.getStringExtra("course_code");
        String courseTitle = intent.getStringExtra("course_title");
        int courseColorValue = intent.getIntExtra("course_color_value", -1);
        String buttonLabel = intent.getStringExtra("button_label");

        // التحقق من وجود courseCode
        if (courseCode == null) {
            Log.e("ParticipantsActivity", "لم يتم تمرير courseCode");
            Snackbar.make(findViewById(android.R.id.content), R.string.course_not_found, Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }

        // إعداد الواجهة
        setupUI(courseTitle, courseColorValue, buttonLabel);

        // التحقق من حالة المفضلة
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();

        // إعداد زر الرجوع
        backButton.setOnClickListener(v -> finish());

        // إعداد زر المفضلة
        favoriteButton.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            courseRepository.setCourseFavorite(userId, courseCode, isFavorite);
            updateFavoriteButton();
            String message = isFavorite ? getString(R.string.added_to_favorites) :
                    getString(R.string.removed_from_favorites);
            Snackbar.make(v, message, Snackbar.LENGTH_SHORT).show();
        });

        // جلب المشاركين من قاعدة البيانات
        setupParticipants();

        // إعداد شريط البحث
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

        // إعداد مستمع لأيقونة مسح النص
        textInputLayout.setEndIconOnClickListener(v -> {
            searchBar.setText("");
            performSearch("");
            hideKeyboard();
            searchBar.clearFocus();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // إعادة التحقق من حالة المفضلة عند استئناف النشاط
        isFavorite = courseRepository.isCourseFavorite(userId, courseCode);
        updateFavoriteButton();
    }

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

    private void setupUI(String courseTitle, int courseColorValue, String buttonLabel) {
        courseCodeTextView.setText(courseCode);
        if (courseTitle != null) {
            courseTitleTextView.setText(courseTitle);
        }
        if (courseColorValue != -1) {
            courseColor = courseColorValue;
            courseHeaderLayout.setBackgroundColor(courseColor);
        } else {
            courseColor = 0xFF005A82; // اللون الافتراضي
            courseHeaderLayout.setBackgroundColor(courseColor);
        }
        if (buttonLabel != null) {
            sectionTitle.setText(buttonLabel);
        }

        // تعيين لون شريط النظام
        Utils.setSystemBarColorWithColorInt(this, courseColor, getResources().getColor(R.color.Custom_BackgroundColor), 0);
    }

    private void setupParticipants() {
        int courseId = courseRepository.getCourseIdByCode(courseCode);
        if (courseId == -1) {
            Log.e("ParticipantsActivity", "لم يتم العثور على المقرر: " + courseCode);
            Snackbar.make(findViewById(android.R.id.content), R.string.course_not_found, Snackbar.LENGTH_LONG).show();
            finish();
            return;
        }
        List<ParticipantData> participants = enrollmentRepository.getParticipantsByCourseId(courseId, null, isArabic);
        adapter = new ParticipantsAdapter(this, participants, courseCode);
        recyclerView.setAdapter(adapter);
    }

    private void performSearch(String query) {
        int courseId = courseRepository.getCourseIdByCode(courseCode);
        List<ParticipantData> filteredParticipants = enrollmentRepository.getParticipantsByCourseId(courseId, query, isArabic);
        adapter.updateParticipants(filteredParticipants);
        Log.d("ParticipantsActivity", "تم البحث باستعلام: " + query + ", عدد النتائج: " + filteredParticipants.size());
    }

    private void updateFavoriteButton() {
        favoriteButton.setIconResource(isFavorite ? R.drawable.star_selected : R.drawable.star);
        favoriteButton.setIconTint(ColorStateList.valueOf(Color.WHITE));
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}