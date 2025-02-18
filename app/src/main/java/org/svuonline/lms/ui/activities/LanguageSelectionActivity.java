package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.svuonline.lms.R;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

public class LanguageSelectionActivity extends BaseActivity {

    private MaterialButton btnEnglish;
    private MaterialButton btnArabic;
    private String selectedLanguage = "";
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        // تعريف الأزرار
        btnEnglish = findViewById(R.id.btnEnglish);
        btnArabic = findViewById(R.id.btnArabic);
        MaterialButton btnNext = findViewById(R.id.btnGetStarted);

        // تعريف الاهتزاز
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        Utils.setSystemBarColor(this, R.color.Custom_MainColorBlue, R.color.Custom_MainColorBlue, 0);

        // تعيين استماع لاختيار اللغة
        btnEnglish.setOnClickListener(v -> handleLanguageSelection("en", btnEnglish, btnArabic));
        btnArabic.setOnClickListener(v -> handleLanguageSelection("ar", btnArabic, btnEnglish));

        // زر التالي للتحقق من اختيار اللغة
        btnNext.setOnClickListener(v -> {
            if (selectedLanguage.isEmpty()) {
                showLanguageNotSelectedError();
            } else {
                // الانتقال إلى النشاط التالي
                Intent intent = new Intent(LanguageSelectionActivity.this, ModeSelectionActivity.class);
                startActivity(intent);
            }
        });

        // تحديث واجهة الأزرار بناءً على اللغة المحفوظة
        updateButtonSelection();
    }

    private void handleLanguageSelection(String languageCode, MaterialButton selectedButton, MaterialButton otherButton) {
        selectedLanguage = languageCode;
        saveSelectedLanguage(languageCode);
        setButtonSelected(selectedButton, true);
        setButtonSelected(otherButton, false);
    }

    private void showLanguageNotSelectedError() {
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.select_language_message), Snackbar.LENGTH_SHORT)
                .setAction("OK", v1 -> {
                })
                .show();
        if (vibrator != null) {
            vibrator.vibrate(300); // تقليل مدة الاهتزاز
        }
    }

    private void setButtonSelected(MaterialButton button, boolean isSelected) {
        if (isSelected) {
            button.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.Custom_MainColorGolden));
            button.setStrokeColor(null);
        } else {
            button.setBackgroundTintList(AppCompatResources.getColorStateList(this, android.R.color.transparent));
            button.setStrokeColor(AppCompatResources.getColorStateList(this, R.color.Custom_MainColorGolden));
        }
    }

    private void saveSelectedLanguage(String languageCode) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selected_language", languageCode);
        editor.apply();
    }

    private void updateButtonSelection() {
        if (selectedLanguage.equals("en")) {
            setButtonSelected(btnEnglish, true);
            setButtonSelected(btnArabic, false);
        } else if (selectedLanguage.equals("ar")) {
            setButtonSelected(btnArabic, true);
            setButtonSelected(btnEnglish, false);
        }
    }
}
