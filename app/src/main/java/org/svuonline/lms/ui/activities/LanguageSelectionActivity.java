package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;

import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.svuonline.lms.R;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

/**
 * نشاط اختيار اللغة مع حفظ الاختيار والانتقال إلى اختيار الوضع.
 */
public class LanguageSelectionActivity extends BaseActivity {

    // مفاتيح SharedPreferences
    private static final String PREFS_NAME = "AppPreferences";
    private static final String KEY_LANGUAGE = "selected_language";

    // عناصر واجهة المستخدم
    private MaterialButton btnEnglish;
    private MaterialButton btnArabic;
    private MaterialButton btnNext;

    // البيانات والخدمات
    private String selectedLanguage = "";
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        // تهيئة المكونات
        initComponents();

        // تهيئة الواجهة
        initViews();

        // إعداد الإعدادات العامة
        setupPreferences();

        // تحميل اللغة المحفوظة
        loadSavedLanguage();

        // إعداد مستمعات الأحداث
        setupListeners();
    }

    /**
     * تهيئة المكونات (الاهتزاز)
     */
    private void initComponents() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    /**
     * تهيئة عناصر الواجهة
     */
    private void initViews() {
        btnEnglish = findViewById(R.id.btnEnglish);
        btnArabic = findViewById(R.id.btnArabic);
        btnNext = findViewById(R.id.btnGetStarted);
    }

    /**
     * إعداد الإعدادات العامة (شريط الحالة)
     */
    private void setupPreferences() {
        Utils.setSystemBarColor(this, R.color.Custom_MainColorBlue, R.color.Custom_MainColorBlue, 0);
    }

    /**
     * تحميل اللغة المحفوظة من SharedPreferences
     */
    private void loadSavedLanguage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedLanguage = prefs.getString(KEY_LANGUAGE, "");
        updateButtonSelection();
    }

    /**
     * إعداد مستمعات الأحداث للأزرار
     */
    private void setupListeners() {
        btnEnglish.setOnClickListener(v -> handleLanguageSelection("en", btnEnglish, btnArabic));
        btnArabic.setOnClickListener(v -> handleLanguageSelection("ar", btnArabic, btnEnglish));
        btnNext.setOnClickListener(v -> {
            if (selectedLanguage.isEmpty()) {
                showLanguageNotSelectedError();
            } else {
                navigateToModeSelection();
            }
        });
    }

    /**
     * معالجة اختيار اللغة وحفظها
     * @param languageCode رمز اللغة (en/ar)
     * @param selectedButton الزر المختار
     * @param otherButton الزر الآخر
     */
    private void handleLanguageSelection(String languageCode, MaterialButton selectedButton, MaterialButton otherButton) {
        selectedLanguage = languageCode;
        saveSelectedLanguage(languageCode);
        setButtonSelected(selectedButton, true);
        setButtonSelected(otherButton, false);
    }

    /**
     * عرض رسالة خطأ عند عدم اختيار لغة
     */
    private void showLanguageNotSelectedError() {
        Snackbar.make(findViewById(android.R.id.content), R.string.select_language_message, Snackbar.LENGTH_SHORT)
                .setAction("OK", v -> {
                })
                .show();
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(300, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(300);
            }
        }
    }

    /**
     * حفظ اللغة المختارة في SharedPreferences
     * @param languageCode رمز اللغة
     */
    private void saveSelectedLanguage(String languageCode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LANGUAGE, languageCode);
        editor.apply();
    }

    /**
     * تحديث أنماط الأزرار بناءً على اللغة المختارة
     */
    private void updateButtonSelection() {
        setButtonSelected(btnEnglish, selectedLanguage.equals("en"));
        setButtonSelected(btnArabic, selectedLanguage.equals("ar"));
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
     * الانتقال إلى ModeSelectionActivity
     */
    private void navigateToModeSelection() {
        Intent intent = new Intent(this, ModeSelectionActivity.class);
        startActivity(intent);
    }
}