package org.svuonline.lms.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;


import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // تطبيق تفضيلات اللغة والنمط
        applyAppPreferences(getApplicationContext());
    }

    // دالة لتطبيق تفضيلات اللغة والنمط
    private void applyAppPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);

        // تحقق إذا كانت تفضيلات اللغة والنمط موجودة، إذا لم تكن موجودة قم بتعيين الافتراضيات بناءً على إعدادات الجهاز
        if (!sharedPreferences.contains("selected_language") || !sharedPreferences.contains("selected_mode")) {
            setDefaultPreferences(context);
        }

        // تطبيق اللغة المحفوظة
        String selectedLanguage = sharedPreferences.getString("selected_language", "en");
        setLocale(context, selectedLanguage);

        // تطبيق النمط المحفوظ
        String selectedMode = sharedPreferences.getString("selected_mode", "light");
        if ("light".equals(selectedMode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // نمط الفاتح
        } else if ("dark".equals(selectedMode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // نمط داكن
        }
    }

    // دالة لتعيين اللغة
    private void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        Resources resources = context.getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    // دالة لتعيين التفضيلات الافتراضية بناءً على إعدادات الجهاز
    private void setDefaultPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // تعيين اللغة الافتراضية بناءً على لغة الجهاز
        String systemLanguage = Locale.getDefault().getLanguage();
        editor.putString("selected_language", systemLanguage); // تعيين لغة الجهاز كإعداد افتراضي
        editor.apply();

        // تعيين النمط الافتراضي بناءً على إعدادات النظام (فاتح أو داكن)
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        String mode = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) ? "dark" : "light";
        editor.putString("selected_mode", mode); // تعيين النمط بناءً على وضع النظام
        editor.apply();
    }
}
