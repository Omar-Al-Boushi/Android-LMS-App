package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import org.svuonline.lms.R;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

public class SettingsActivity extends BaseActivity {

    // --- مفاتيح SharedPreferences لتخزين تفضيلات التطبيق ---
    private static final String APP_PREFS = "AppPreferences";
    private static final String KEY_LANGUAGE = "selected_language";
    private static final String KEY_MODE = "selected_mode";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    // متغير لتتبع ما إذا تمت تغييرات تستدعي إعادة بناء النشاط
    private boolean changesMade = false;

    // --- تعريف عناصر الواجهة (المستخلصة من ملف XML) ---
    private MaterialCardView cvEnglish, cvArabic, cvLightMode, cvDarkMode,
            cvEnableNotifications, cvDisableNotifications, cvLogout;

    // --- تعريف متغيرات الألوان المستخدمة ---
    private int colorSelectedCardTint, colorUnselectedCardTint;
    private int colorSelectedIconBg, colorUnselectedIconBg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // تعيين الـ layout الخاص بالصفحة
        setContentView(R.layout.activity_settings);
        // إعداد لون شريط النظام (status bar)
        Utils.setSystemBarColor(this, R.color.Custom_BackgroundColor, R.color.Custom_Med_Black, 0);

        // --- إعداد الألوان ---
        // لون الخلفية للبطاقة المختارة (golden) وللغير مختارة (الخلفية الافتراضية)
        colorSelectedCardTint = ContextCompat.getColor(this, R.color.Custom_MainColorGolden);
        colorUnselectedCardTint = ContextCompat.getColor(this, R.color.Custom_BackgroundColor);
        // لون خلفية الأيقونات؛ عند تحديد الخيار يتم استخدام لون معين (أزرق) وإلا يتم استخدام قيمة ثابتة (مثال ARGB)
        colorSelectedIconBg = ContextCompat.getColor(this, R.color.Custom_MainColorBlue);
        colorUnselectedIconBg = 0x32A18F5A; // اللون الثابت للعنصر غير المختار

        // --- ربط عناصر الواجهة من ملف XML ---
        initViews();

        // --- إعداد زر العودة في Toolbar ---
        MaterialToolbar toolbar = findViewById(R.id.toolbar_top);
        toolbar.setNavigationOnClickListener(v -> {
            // عند الضغط على أيقونة العودة، انتقل إلى DashboardActivity مع مسح النشاطات السابقة
            Intent homeIntent = new Intent(SettingsActivity.this, DashboardActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
            finish();
        });

        // --- تحميل تفضيلات المستخدم وتحديث الواجهة بناءً عليها ---
        loadAppSettings();

        // --- إعداد مستمعي النقر على كافة الخيارات ---
        setupClickListeners();
    }

    /**
     * ربط عناصر الواجهة حسب المعرفات الموجودة في XML.
     */
    private void initViews() {
        cvEnglish = findViewById(R.id.cv_english);
        cvArabic = findViewById(R.id.cv_arabic);
        cvLightMode = findViewById(R.id.cv_light_mode);
        cvDarkMode = findViewById(R.id.cv_dark_mode);
        cvEnableNotifications = findViewById(R.id.cv_enable_notifications);
        cvDisableNotifications = findViewById(R.id.cv_disable_notifications);
        cvLogout = findViewById(R.id.cv_logout);
    }

    /**
     * قراءة التفضيلات المحفوظة وتحديث واجهة الخيارات بناءً عليها.
     */
    private void loadAppSettings() {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        String selectedLanguage = prefs.getString(KEY_LANGUAGE, "en");
        String selectedMode = prefs.getString(KEY_MODE, "light");
        boolean notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true);

        updateLanguageUI(selectedLanguage);
        updateThemeUI(selectedMode);
        updateNotificationsUI(notificationsEnabled);
    }

    /**
     * تحديث واجهة خيارات اللغة.
     *
     * @param language اللغة المختارة ("en" أو "ar")
     */
    private void updateLanguageUI(String language) {
        if ("en".equals(language)) {
            updateOptionUI(cvEnglish, R.id.layout_btn_english, R.id.iv_english_icon, R.id.tv_english, true);
            updateOptionUI(cvArabic, R.id.layout_btn_arabic, R.id.iv_arabic_icon, R.id.tv_arabic, false);
        } else {
            updateOptionUI(cvEnglish, R.id.layout_btn_english, R.id.iv_english_icon, R.id.tv_english, false);
            updateOptionUI(cvArabic, R.id.layout_btn_arabic, R.id.iv_arabic_icon, R.id.tv_arabic, true);
        }
    }

    /**
     * تحديث واجهة خيارات النمط (المظهر).
     *
     * @param mode النمط المختار ("light" أو "dark")
     */
    private void updateThemeUI(String mode) {
        if ("light".equals(mode)) {
            updateOptionUI(cvLightMode, R.id.layout_btn_light_mode, R.id.iv_light_mode_icon, R.id.tv_light_mode, true);
            updateOptionUI(cvDarkMode, R.id.layout_btn_dark_mode, R.id.iv_dark_mode_icon, R.id.tv_dark_mode, false);
        } else {
            updateOptionUI(cvLightMode, R.id.layout_btn_light_mode, R.id.iv_light_mode_icon, R.id.tv_light_mode, false);
            updateOptionUI(cvDarkMode, R.id.layout_btn_dark_mode, R.id.iv_dark_mode_icon, R.id.tv_dark_mode, true);
        }
    }

    /**
     * تحديث واجهة خيارات الإشعارات.
     *
     * @param enabled true إذا كانت الإشعارات مفعلة، false إذا كانت معطلة.
     */
    private void updateNotificationsUI(boolean enabled) {
        if (enabled) {
            updateOptionUI(cvEnableNotifications, R.id.layout_btn_enable_notifications, R.id.iv_enable_icon, R.id.tv_enable, true);
            updateOptionUI(cvDisableNotifications, R.id.layout_btn_disable_notifications, R.id.iv_disable_icon, R.id.tv_disable, false);
        } else {
            updateOptionUI(cvEnableNotifications, R.id.layout_btn_enable_notifications, R.id.iv_enable_icon, R.id.tv_enable, false);
            updateOptionUI(cvDisableNotifications, R.id.layout_btn_disable_notifications, R.id.iv_disable_icon, R.id.tv_disable, true);
        }
    }

    /**
     * دالة مساعدة لتحديث واجهة خيار معين.
     *
     * @param optionCard  البطاقة الخارجية للخيار.
     * @param layoutBtnId معرف التخطيط الداخلي الذي يحتوي على البطاقة النصية.
     * @param iconId      معرف أيقونة الخيار.
     * @param textId      معرف عنصر النص.
     * @param selected    true إذا كان الخيار مختاراً، false إذا لم يكن مختاراً.
     */
    private void updateOptionUI(MaterialCardView optionCard, int layoutBtnId, int iconId, int textId, boolean selected) {
        // --- تحديث البطاقة النصية ---
        // الحصول على التخطيط الداخلي الذي يحتوى على البطاقة النصية
        ConstraintLayout layout = optionCard.findViewById(layoutBtnId);
        if (layout != null) {
            // نفترض أن أول طفل في التخطيط هو البطاقة التي تحتوي على النص
            View textCardView = layout.getChildAt(0);
            if (textCardView instanceof MaterialCardView) {
                MaterialCardView innerCard = (MaterialCardView) textCardView;
                // تغيير لون خلفية البطاقة حسب حالة الاختيار
                innerCard.setBackgroundTintList(ContextCompat.getColorStateList(this,
                        selected ? R.color.Custom_MainColorGolden : R.color.Custom_BackgroundColor));
            }
        }

        // --- تحديث لون النص ---
        // يتم تغيير لون النص إذا كان النمط "light": المختار يظهر بالنص الأبيض وغير المختار بالنص الأسود.
        // إذا كان النمط "dark" يتم عرض النص أبيض دائمًا.
        TextView tvOption = optionCard.findViewById(textId);
        if (tvOption != null) {
            String mode = getCurrentMode(); // الحصول على النمط الحالي ("light" أو "dark")
            if ("light".equals(mode)) {
                tvOption.setTextColor(selected ? ContextCompat.getColor(this, android.R.color.white)
                        : ContextCompat.getColor(this, android.R.color.black));
            } else {
                tvOption.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            }
        }

        // --- تحديث خلفية الأيقونة ---
        // يتم تغيير خلفية الأيقونة باستخدام setBackgroundColor
        View iconView = optionCard.findViewById(iconId);
        int bgColor = selected ? colorSelectedIconBg : colorUnselectedIconBg;
        if (iconView != null) {
            if (iconView instanceof ShapeableImageView) {
                ((ShapeableImageView) iconView).setBackgroundColor(bgColor);
            } else {
                iconView.setBackgroundColor(bgColor);
            }
        }
    }

    /**
     * إعداد مستمعي النقر على كافة الخيارات.
     */
    private void setupClickListeners() {
        // --- خيارات اللغة ---
        cvEnglish.setOnClickListener(v -> {
            if (!"en".equals(getCurrentLanguage())) {
                updateLanguage("en");
            }
        });
        cvArabic.setOnClickListener(v -> {
            if (!"ar".equals(getCurrentLanguage())) {
                updateLanguage("ar");
            }
        });

        // --- خيارات النمط ---
        cvLightMode.setOnClickListener(v -> {
            if (!"light".equals(getCurrentMode())) {
                updateTheme("light");
            }
        });
        cvDarkMode.setOnClickListener(v -> {
            if (!"dark".equals(getCurrentMode())) {
                updateTheme("dark");
            }
        });

        // --- خيارات الإشعارات ---
        cvEnableNotifications.setOnClickListener(v -> {
            if (!getCurrentNotificationsStatus()) {
                updateNotifications(true);
            }
        });
        cvDisableNotifications.setOnClickListener(v -> {
            if (getCurrentNotificationsStatus()) {
                updateNotifications(false);
            }
        });

        // --- زر تسجيل الخروج ---
        cvLogout.setOnClickListener(v -> performLogout());
    }

    /**
     * الحصول على اللغة المختارة من التفضيلات.
     *
     * @return قيمة اللغة ("en" أو "ar")
     */
    private String getCurrentLanguage() {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    /**
     * الحصول على النمط الحالي من التفضيلات.
     *
     * @return قيمة النمط ("light" أو "dark")
     */
    private String getCurrentMode() {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        return prefs.getString(KEY_MODE, "light");
    }

    /**
     * الحصول على حالة الإشعارات من التفضيلات.
     *
     * @return true إذا كانت الإشعارات مفعلة، false إن لم تكن كذلك.
     */
    private boolean getCurrentNotificationsStatus() {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        return prefs.getBoolean(KEY_NOTIFICATIONS, true);
    }

    /**
     * تحديث اللغة في التفضيلات وإعادة بناء النشاط لتطبيق التغييرات.
     *
     * @param newLanguage اللغة الجديدة المختارة.
     */
    private void updateLanguage(String newLanguage) {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, newLanguage).apply();
        changesMade = true;
        updateLanguageUI(newLanguage);
        rebuildActivity();
    }

    /**
     * تحديث النمط (المظهر) في التفضيلات وإعادة بناء النشاط لتطبيق التغييرات.
     *
     * @param newMode النمط الجديد ("light" أو "dark")
     */
    private void updateTheme(String newMode) {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        prefs.edit().putString(KEY_MODE, newMode).apply();
        changesMade = true;
        if ("light".equals(newMode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if ("dark".equals(newMode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        updateThemeUI(newMode);
        rebuildActivity();
    }

    /**
     * تحديث حالة الإشعارات في التفضيلات وإعادة بناء النشاط لتطبيق التغييرات.
     *
     * @param enabled true إذا كانت الإشعارات مفعلة.
     */
    private void updateNotifications(boolean enabled) {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
        changesMade = true;
        updateNotificationsUI(enabled);
        rebuildActivity();
    }

    /**
     * تنفيذ عملية تسجيل الخروج:
     * - إعادة ضبط تفضيلات المستخدم وتفضيلات التطبيق.
     * - الانتقال إلى شاشة تسجيل الدخول مع مسح النشاطات السابقة.
     */
    private void performLogout() {
        // إعادة ضبط تفضيلات المستخدم
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userPrefs.edit().clear().apply();

        // إعادة ضبط تفضيلات التطبيق
        SharedPreferences appPrefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        appPrefs.edit().clear().apply();

        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * إعادة بناء النشاط الحالي (أو الانتقال للصفحة الرئيسية) بحسب حدوث تغييرات.
     */
    private void rebuildActivity() {
        if (changesMade) {
            Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Intent homeIntent = new Intent(SettingsActivity.this, DashboardActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
        }
        finish();
    }

    /**
     * عند الضغط على زر العودة (الـ Back) يتم العودة إلى الصفحة الرئيسية (DashboardActivity)
     * مع مسح النشاطات السابقة.
     */
    @SuppressWarnings("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(SettingsActivity.this, DashboardActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }
}
