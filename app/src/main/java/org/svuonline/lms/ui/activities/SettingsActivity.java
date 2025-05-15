package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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

/**
 * نشاط لإدارة إعدادات التطبيق (اللغة، النمط، الإشعارات، تسجيل الخروج).
 */
public class SettingsActivity extends BaseActivity {

    // مفاتيح SharedPreferences
    private static final String APP_PREFS = "AppPreferences";
    private static final String KEY_LANGUAGE = "selected_language";
    private static final String KEY_MODE = "selected_mode";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    // عناصر واجهة المستخدم
    private MaterialToolbar toolbar;
    private MaterialCardView cvEnglish;
    private MaterialCardView cvArabic;
    private MaterialCardView cvLightMode;
    private MaterialCardView cvDarkMode;
    private MaterialCardView cvEnableNotifications;
    private MaterialCardView cvDisableNotifications;
    private MaterialCardView cvLogout;
    private ShapeableImageView ivProfile;

    // المستودعات
    private UserRepository userRepository;

    // بيانات النشاط
    private long userId;
    private boolean changesMade;
    private int colorSelectedCardTint;
    private int colorUnselectedCardTint;
    private int colorSelectedIconBg;
    private int colorUnselectedIconBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // تهيئة المكونات
        initComponents();

        // تهيئة الواجهة
        initViews();

        // التحقق من بيانات المستخدم
        if (!validateUserData()) {
            return;
        }

        // تهيئة البيانات
        initData();

        // إعداد مستمعات الأحداث
        setupListeners();
    }

    /**
     * تهيئة المستودعات
     */
    private void initComponents() {
        userRepository = new UserRepository(this);
    }

    /**
     * تهيئة عناصر الواجهة
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar_top);
        cvEnglish = findViewById(R.id.cv_english);
        cvArabic = findViewById(R.id.cv_arabic);
        cvLightMode = findViewById(R.id.cv_light_mode);
        cvDarkMode = findViewById(R.id.cv_dark_mode);
        cvEnableNotifications = findViewById(R.id.cv_enable_notifications);
        cvDisableNotifications = findViewById(R.id.cv_disable_notifications);
        cvLogout = findViewById(R.id.cv_logout);
        ivProfile = findViewById(R.id.iv_profile);
    }

    /**
     * التحقق من صحة بيانات المستخدم
     * @return صحيح إذا كانت البيانات صالحة، خطأ إذا لزم إنهاء النشاط
     */
    private boolean validateUserData() {
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = userPrefs.getLong("user_id", -1);
        if (userId == -1) {
            showToast(R.string.user_id_not_found);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return false;
        }
        return true;
    }

    /**
     * تهيئة البيانات (إعداد الألوان، الصورة، التفضيلات)
     */
    private void initData() {
        // إعداد لون شريط النظام
        Utils.setSystemBarColor(this, R.color.Custom_BackgroundColor, R.color.Custom_Med_Black, 0);

        // إعداد الألوان
        colorSelectedCardTint = ContextCompat.getColor(this, R.color.Custom_MainColorGolden);
        colorUnselectedCardTint = ContextCompat.getColor(this, R.color.Custom_BackgroundColor);
        colorSelectedIconBg = ContextCompat.getColor(this, R.color.Custom_MainColorBlue);
        colorUnselectedIconBg = 0xFF687570;

        // إعداد الصورة الشخصية
        setupProfilePicture();

        // تحميل التفضيلات
        loadAppSettings();
    }

    /**
     * إعداد مستمعات الأحداث (الشريط العلوي، خيارات الإعدادات)
     */
    private void setupListeners() {
        // إعداد الشريط العلوي
        toolbar.setNavigationOnClickListener(v -> navigateToDashboard());

        // إعداد خيارات الإعدادات
        setupClickListeners();
    }

    /**
     * إعداد الصورة الشخصية
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
     * تحميل تفضيلات التطبيق
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
     * إعداد مستمعي النقر على خيارات الإعدادات
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
     * تحديث واجهة خيارات اللغة
     * @param language اللغة المختارة (en أو ar)
     */
    private void updateLanguageUI(String language) {
        updateOptionUI(cvEnglish, R.id.layout_btn_english, R.id.iv_english_icon, R.id.tv_english, "en".equals(language));
        updateOptionUI(cvArabic, R.id.layout_btn_arabic, R.id.iv_arabic_icon, R.id.tv_arabic, "ar".equals(language));
    }

    /**
     * تحديث واجهة خيارات النمط
     * @param mode النمط المختار (light أو dark)
     */
    private void updateThemeUI(String mode) {
        updateOptionUI(cvLightMode, R.id.layout_btn_light_mode, R.id.iv_light_mode_icon, R.id.tv_light_mode, "light".equals(mode));
        updateOptionUI(cvDarkMode, R.id.layout_btn_dark_mode, R.id.iv_dark_mode_icon, R.id.tv_dark_mode, "dark".equals(mode));
    }

    /**
     * تحديث واجهة خيارات الإشعارات
     * @param enabled حالة الإشعارات (مفعلة أو معطلة)
     */
    private void updateNotificationsUI(boolean enabled) {
        updateOptionUI(cvEnableNotifications, R.id.layout_btn_enable_notifications, R.id.iv_enable_icon, R.id.tv_enable, enabled);
        updateOptionUI(cvDisableNotifications, R.id.layout_btn_disable_notifications, R.id.iv_disable_icon, R.id.tv_disable, !enabled);
    }

    /**
     * تحديث واجهة خيار معين
     * @param optionCard البطاقة الخاصة بالخيار
     * @param layoutBtnId معرف التخطيط
     * @param iconId معرف الأيقونة
     * @param textId معرف النص
     * @param selected حالة الخيار (مختار أو غير مختار)
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
     * الحصول على اللغة الحالية
     * @return اللغة المختارة (en أو ar)
     */
    private String getCurrentLanguage() {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    /**
     * الحصول على النمط الحالي
     * @return النمط المختار (light أو dark)
     */
    private String getCurrentMode() {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        return prefs.getString(KEY_MODE, "light");
    }

    /**
     * الحصول على حالة الإشعارات
     * @return صحيح إذا كانت الإشعارات مفعلة، خطأ إذا كانت معطلة
     */
    private boolean getCurrentNotificationsStatus() {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        return prefs.getBoolean(KEY_NOTIFICATIONS, true);
    }

    /**
     * تحديث اللغة
     * @param newLanguage اللغة الجديدة (en أو ar)
     */
    private void updateLanguage(String newLanguage) {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, newLanguage).apply();
        changesMade = true;
        updateLanguageUI(newLanguage);
        rebuildActivity();
    }

    /**
     * تحديث النمط
     * @param newMode النمط الجديد (light أو dark)
     */
    private void updateTheme(String newMode) {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        prefs.edit().putString(KEY_MODE, newMode).apply();
        changesMade = true;
        if ("light".equals(newMode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        updateThemeUI(newMode);
        rebuildActivity();
    }

    /**
     * تحديث حالة الإشعارات
     * @param enabled حالة الإشعارات الجديدة
     */
    private void updateNotifications(boolean enabled) {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
        changesMade = true;
        updateNotificationsUI(enabled);
        rebuildActivity();
    }

    /**
     * تنفيذ تسجيل الخروج
     */
    private void performLogout() {
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userPrefs.edit().clear().apply();
        SharedPreferences appPrefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        appPrefs.edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * إعادة بناء النشاط بعد التغييرات
     */
    private void rebuildActivity() {
        if (changesMade) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
    }

    /**
     * الانتقال إلى لوحة التحكم
     */
    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * التعامل مع زر العودة
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToDashboard();
    }

    /**
     * تحديث الصورة الشخصية عند استئناف النشاط
     */
    @Override
    protected void onResume() {
        super.onResume();
        setupProfilePicture();
    }

    /**
     * عرض رسالة Toast
     * @param messageRes معرف الرسالة
     */
    private void showToast(int messageRes) {
        android.widget.Toast.makeText(this, messageRes, android.widget.Toast.LENGTH_SHORT).show();
    }
}