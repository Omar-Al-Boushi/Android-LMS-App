package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
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
import org.svuonline.lms.data.model.User;
import org.svuonline.lms.data.repository.UserRepository;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import android.net.Uri;

public class SettingsActivity extends BaseActivity {

    // --- مفاتيح SharedPreferences لتخزين تفضيلات التطبيق ---
    private static final String APP_PREFS = "AppPreferences";
    private static final String KEY_LANGUAGE = "selected_language";
    private static final String KEY_MODE = "selected_mode";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    // متغير لتتبع ما إذا تمت تغييرات تستدعي إعادة بناء النشاط
    private boolean changesMade = false;

    // --- تعريف عناصر الواجهة ---
    private MaterialCardView cvEnglish, cvArabic, cvLightMode, cvDarkMode,
            cvEnableNotifications, cvDisableNotifications, cvLogout;
    private ShapeableImageView ivProfile; // لعرض الصورة الشخصية

    // --- تعريف متغيرات الألوان المستخدمة ---
    private int colorSelectedCardTint, colorUnselectedCardTint;
    private int colorSelectedIconBg, colorUnselectedIconBg;

    // --- متغيرات إضافية لجلب الصورة ---
    private long userId;
    private UserRepository userRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // إعداد لون شريط النظام
        Utils.setSystemBarColor(this, R.color.Custom_BackgroundColor, R.color.Custom_Med_Black, 0);

        // --- جلب userId من SharedPreferences ---
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = userPrefs.getLong("user_id", -1);
        if (userId == -1) {
            // إذا لم يتم العثور على userId، انتقل إلى شاشة تسجيل الدخول
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // --- تهيئة UserRepository ---
        userRepository = new UserRepository(this);

        // --- إعداد الألوان ---
        colorSelectedCardTint = ContextCompat.getColor(this, R.color.Custom_MainColorGolden);
        colorUnselectedCardTint = ContextCompat.getColor(this, R.color.Custom_BackgroundColor);
        colorSelectedIconBg = ContextCompat.getColor(this, R.color.Custom_MainColorBlue);
        colorUnselectedIconBg = 0xFF2C3330;

        // --- ربط عناصر الواجهة ---
        initViews();

        // --- إعداد الصورة الشخصية ---
        setupProfilePicture();

        // --- إعداد زر العودة في Toolbar ---
        MaterialToolbar toolbar = findViewById(R.id.toolbar_top);
        toolbar.setNavigationOnClickListener(v -> {
            Intent homeIntent = new Intent(SettingsActivity.this, DashboardActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
            finish();
        });

        // --- تحميل تفضيلات المستخدم وتحديث الواجهة ---
        loadAppSettings();

        // --- إعداد مستمعي النقر ---
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
        ivProfile = findViewById(R.id.iv_profile); // ربط الصورة الشخصية
    }

    /**
     * إعداد الصورة الشخصية بناءً على userId
     */
    private void setupProfilePicture() {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            ivProfile.setImageResource(R.drawable.avatar);
            return;
        }
        String pic = user.getProfilePicture();
        if (pic != null && pic.startsWith("@drawable/")) {
            String name = pic.substring("@drawable/".length());
            int resId = getResources().getIdentifier(name, "drawable", getPackageName());
            ivProfile.setImageResource(resId != 0 ? resId : R.drawable.avatar);
        } else if (pic != null && !pic.isEmpty()) {
            try {
                ivProfile.setImageURI(Uri.parse(pic));
            } catch (Exception e) {
                ivProfile.setImageResource(R.drawable.avatar);
            }
        } else {
            ivProfile.setImageResource(R.drawable.avatar);
        }
    }

    /**
     * قراءة التفضيلات المحفوظة وتحديث واجهة الخيارات.
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
     * تحديث واجهة خيارات النمط.
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
     */
    private void updateOptionUI(MaterialCardView optionCard, int layoutBtnId, int iconId, int textId, boolean selected) {
        ConstraintLayout layout = optionCard.findViewById(layoutBtnId);
        if (layout != null) {
            View textCardView = layout.getChildAt(0);
            if (textCardView instanceof MaterialCardView) {
                MaterialCardView innerCard = (MaterialCardView) textCardView;
                innerCard.setBackgroundTintList(ContextCompat.getColorStateList(this,
                        selected ? R.color.Custom_MainColorGolden : R.color.Custom_BackgroundColor));
            }
        }

        TextView tvOption = optionCard.findViewById(textId);
        if (tvOption != null) {
            String mode = getCurrentMode();
            if ("light".equals(mode)) {
                tvOption.setTextColor(selected ? ContextCompat.getColor(this, android.R.color.white)
                        : ContextCompat.getColor(this, android.R.color.black));
            } else {
                tvOption.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            }
        }

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

        cvLogout.setOnClickListener(v -> performLogout());
    }

    /**
     * الحصول على اللغة المختارة من التفضيلات.
     */
    private String getCurrentLanguage() {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    /**
     * الحصول على النمط الحالي من التفضيلات.
     */
    private String getCurrentMode() {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        return prefs.getString(KEY_MODE, "light");
    }

    /**
     * الحصول على حالة الإشعارات من التفضيلات.
     */
    private boolean getCurrentNotificationsStatus() {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        return prefs.getBoolean(KEY_NOTIFICATIONS, true);
    }

    /**
     * تحديث اللغة في التفضيلات وإعادة بناء النشاط.
     */
    private void updateLanguage(String newLanguage) {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, newLanguage).apply();
        changesMade = true;
        updateLanguageUI(newLanguage);
        rebuildActivity();
    }

    /**
     * تحديث النمط في التفضيلات وإعادة بناء النشاط.
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
     * تحديث حالة الإشعارات في التفضيلات وإعادة بناء النشاط.
     */
    private void updateNotifications(boolean enabled) {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
        changesMade = true;
        updateNotificationsUI(enabled);
        rebuildActivity();
    }

    /**
     * تنفيذ عملية تسجيل الخروج.
     */
    private void performLogout() {
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userPrefs.edit().clear().apply();

        SharedPreferences appPrefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        appPrefs.edit().clear().apply();

        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * إعادة بناء النشاط الحالي أو الانتقال للصفحة الرئيسية.
     */
    private void rebuildActivity() {
        if (changesMade) {
            Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            finish();
        }
        finish();
    }

    /**
     * العودة إلى الصفحة الرئيسية عند الضغط على زر العودة.
     */
    @SuppressWarnings("MissingSuperCall")
    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // إعادة تحميل الصورة الشخصية للتأكد من تحديثها
        setupProfilePicture();
    }
}