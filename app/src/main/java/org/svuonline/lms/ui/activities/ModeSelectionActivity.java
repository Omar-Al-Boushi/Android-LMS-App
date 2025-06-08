package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.svuonline.lms.R;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

/**
 * نشاط اختيار وضع السمة (فاتح/داكن) مع حفظ الاختيار والانتقال إلى تسجيل الدخول.
 */
public class ModeSelectionActivity extends BaseActivity {

    // مفاتيح SharedPreferences
    private static final String PREFS_NAME = "AppPreferences";
    private static final String KEY_MODE = "selected_mode";

    // عناصر واجهة المستخدم
    private MaterialButton btnLightMode;
    private MaterialButton btnDarkMode;
    private MaterialButton btnGetStarted;

    // البيانات والخدمات
    private String selectedMode = "";
    private android.os.Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);

        // تهيئة المكونات
        initComponents();

        // تهيئة الواجهة
        initViews();

        // إعداد الإعدادات العامة
        setupPreferences();

        // تحميل الوضع المحفوظ
        loadSavedMode();

        // إعداد مستمعات الأحداث
        setupListeners();
    }

    /**
     * تهيئة المكونات (الاهتزاز)
     */
    private void initComponents() {
        vibrator = (android.os.Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    /**
     * تهيئة عناصر الواجهة
     */
    private void initViews() {
        btnLightMode = findViewById(R.id.btnLightMode);
        btnDarkMode = findViewById(R.id.btnDarkMode);
        btnGetStarted = findViewById(R.id.btnGetStarted);
    }

    /**
     * إعداد الإعدادات العامة (شريط الحالة)
     */
    private void setupPreferences() {
        Utils.setSystemBarColor(this, R.color.Custom_MainColorBlue, R.color.Custom_MainColorBlue, 0);
    }

    /**
     * تحميل الوضع المحفوظ من SharedPreferences
     */
    private void loadSavedMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedMode = prefs.getString(KEY_MODE, "");
        updateButtonSelection();
        applySelectedMode();
    }

    /**
     * إعداد مستمعات الأحداث للأزرار
     */
    private void setupListeners() {
        btnLightMode.setOnClickListener(v -> handleModeSelection("light", btnLightMode, btnDarkMode));
        btnDarkMode.setOnClickListener(v -> handleModeSelection("dark", btnDarkMode, btnLightMode));
        btnGetStarted.setOnClickListener(v -> handleGetStarted());
    }

    /**
     * معالجة اختيار الوضع
     * @param mode الوضع المختار (light/dark)
     * @param selectedButton الزر المختار
     * @param otherButton الزر الآخر
     */
    private void handleModeSelection(String mode, MaterialButton selectedButton, MaterialButton otherButton) {
        selectedMode = mode;
        setButtonSelected(selectedButton, true);
        setButtonSelected(otherButton, false);
        saveSelectedMode(mode);
        applySelectedMode();
    }

    /**
     * التحقق من اختيار الوضع والانتقال إلى النشاط التالي
     */
    private void handleGetStarted() {
        if (selectedMode.isEmpty()) {
            showSnackbar(R.string.select_mode_message);
            vibrateDevice();
        } else {
            saveSelectedMode(selectedMode);
            navigateToLogin();
        }
    }

    /**
     * تعيين نمط الزر (محدد/غير محدد)
     * @param button الزر
     * @param isSelected حالة التحديد
     */
    private void setButtonSelected(MaterialButton button, boolean isSelected) {
        if (isSelected) {
            button.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.Custom_MainColorGolden));
            button.setStrokeColor(null);
        } else {
            button.setBackgroundTintList(AppCompatResources.getColorStateList(this, android.R.color.transparent));
            button.setStrokeColor(AppCompatResources.getColorStateList(this, R.color.Custom_MainColorGolden));
        }
    }

    /**
     * تطبيق الوضع المختار (فاتح/داكن)
     */
    private void applySelectedMode() {
        if (selectedMode.equals("light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (selectedMode.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    /**
     * حفظ الوضع المختار في SharedPreferences
     * @param mode الوضع المختار
     */
    private void saveSelectedMode(String mode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_MODE, mode);
        editor.apply();
    }

    /**
     * عرض رسالة Snackbar
     * @param messageRes معرف الرسالة
     */
    private void showSnackbar(int messageRes) {
        Snackbar.make(findViewById(android.R.id.content), messageRes, Snackbar.LENGTH_SHORT)
                .setAction("OK", v -> {
                })
                .show();
    }

    /**
     * تنفيذ الاهتزاز
     */
    private void vibrateDevice() {
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
        }
    }

    /**
     * تحديث أنماط الأزرار بناءً على الوضع المختار
     */
    private void updateButtonSelection() {
        setButtonSelected(btnLightMode, selectedMode.equals("light"));
        setButtonSelected(btnDarkMode, selectedMode.equals("dark"));
    }

    /**
     * الانتقال إلى LoginActivity
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}