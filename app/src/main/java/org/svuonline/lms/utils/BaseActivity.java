package org.svuonline.lms.utils;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // تطبيق محاذاة النصوص بناءً على اللغة
        setTextAlignment();

        // تطبيق تفضيلات اللغة والنمط
        applyAppPreferences();
    }

    // دالة لتطبيق محاذاة النص بناءً على اللغة
    private void setTextAlignment() {
        String currentLanguage = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                .getString("selected_language", "en");

        // تحديد محاذاة النص بناءً على اللغة
        if ("ar".equals(currentLanguage)) {
            // إذا كانت اللغة عربية، تعيين المحاذاة لتكون RTL
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            // إذا كانت اللغة غير عربية، تعيين المحاذاة لتكون LTR
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

    // دالة لتطبيق تفضيلات اللغة والنمط
    private void applyAppPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        // تطبيق اللغة المحفوظة
        String selectedLanguage = sharedPreferences.getString("selected_language", "en");
        setLocale(selectedLanguage);

        // تطبيق النمط المحفوظ
        String selectedMode = sharedPreferences.getString("selected_mode", "light");
        if ("light".equals(selectedMode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // نمط الفاتح
        } else if ("dark".equals(selectedMode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // نمط داكن
        }
    }

    // دالة لتعيين اللغة
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        Resources resources = getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}

