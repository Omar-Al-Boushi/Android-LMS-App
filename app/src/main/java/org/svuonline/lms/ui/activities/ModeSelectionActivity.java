package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import org.svuonline.lms.R;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;


public class ModeSelectionActivity extends BaseActivity {

    private MaterialButton btnLightMode;
    private MaterialButton btnDarkMode;
    private String selectedMode = ""; // لا يوجد وضع مختار بشكل افتراضي
    private Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_selection);




        // تعريف الأزرار
        btnLightMode = findViewById(R.id.btnLightMode);
        btnDarkMode = findViewById(R.id.btnDarkMode);
        MaterialButton btnGetStarted = findViewById(R.id.btnGetStarted); // زر بدء

        // تعريف الاهتزاز
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        Utils.setSystemBarColor(this, R.color.Custom_MainColorBlue, R.color.Custom_MainColorBlue, 0);


        // الاستماع لاختيار الوضع الفاتح
        btnLightMode.setOnClickListener(v -> handleModeSelection("light", btnLightMode, btnDarkMode));

        // الاستماع لاختيار الوضع الداكن
        btnDarkMode.setOnClickListener(v -> handleModeSelection("dark", btnDarkMode, btnLightMode));

        // زر بدء للتحقق من اختيار النمط
        btnGetStarted.setOnClickListener(v -> handleGetStarted());
    }

    // معالجة اختيار الوضع
    private void handleModeSelection(String mode, MaterialButton selectedButton, MaterialButton otherButton) {
        setButtonSelected(selectedButton, true);
        setButtonSelected(otherButton, false);
        selectedMode = mode;
    }

    // التحقق من اختيار النمط عند النقر على زر البدء
    private void handleGetStarted() {
        if (selectedMode.isEmpty()) {
            // عرض رسالة عند عدم اختيار وضع
            showSnackbar(getString(R.string.select_mode_message));

            // إضافة تأثير اهتزاز
            vibrateDevice();
        } else {
            // حفظ النمط المختار في SharedPreferences
            saveSelectedMode(selectedMode);

            // الانتقال إلى الصفحة الرئيسية أو صفحة أخرى
            Intent intent = new Intent(ModeSelectionActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    // تغيير الخلفية والحدود حسب الاختيار
    private void setButtonSelected(MaterialButton button, boolean isSelected) {
        if (isSelected) {
            button.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.Custom_MainColorGolden));
            button.setStrokeColor(null);
        } else {
            button.setBackgroundTintList(AppCompatResources.getColorStateList(this, android.R.color.transparent));
            button.setStrokeColor(AppCompatResources.getColorStateList(this, R.color.Custom_MainColorGolden));
        }
    }

    // حفظ النمط المختار في SharedPreferences
    private void saveSelectedMode(String mode) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selected_mode", mode);
        editor.apply();
    }

    // عرض رسالة باستخدام Snackbar
    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
                .setAction("OK", v -> {})
                .show();
    }

    // تنفيذ الاهتزاز
    private void vibrateDevice() {
        if (vibrator != null) {
            vibrator.vibrate(500);
        }
    }
}
