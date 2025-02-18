package org.svuonline.lms.utils;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowInsetsControllerCompat;
import java.util.Locale;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class Utils {

    // دالة لتطبيق تفضيلات التطبيق (اللغة + النمط)
    public static void applyAppPreferences(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        String savedLanguage = sharedPreferences.getString("selected_language", "en");
        String savedMode = sharedPreferences.getString("selected_mode", "light");

        // تطبيق اللغة والمحاذاة فقط إذا تغيرت
        if (!getCurrentLanguage(activity).equals(savedLanguage)) {
            setLocale(activity, savedLanguage);
        }

        // تطبيق النمط فقط إذا تغير
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        int newMode = "light".equals(savedMode) ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;

        if (currentMode != newMode) {
            AppCompatDelegate.setDefaultNightMode(newMode);
            activity.recreate();  // إعادة تشغيل النشاط فقط عند تغيير النمط
        }
    }

    // دالة لتعيين اللغة + المحاذاة
    private static void setLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        Resources resources = activity.getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // تحديث المحاذاة بناءً على اللغة
        applyLayoutDirection(activity, languageCode);
    }

    // دالة ديناميكية لتغيير المحاذاة فقط دون التأثير على اللغة
    public static void applyLayoutDirection(Activity activity, String languageCode) {
        View rootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        if (rootView != null) {
            int layoutDirection = "ar".equals(languageCode) ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR;
            rootView.setLayoutDirection(layoutDirection);

            // تطبيق الاتجاه على جميع العناصر داخل الصفحة لضمان التأثير
            updateViewGroupLayoutDirection((ViewGroup) rootView, layoutDirection);
        }
    }

    // تحديث اتجاه المحاذاة لجميع العناصر داخل الصفحة
    private static void updateViewGroupLayoutDirection(ViewGroup parent, int layoutDirection) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
                updateViewGroupLayoutDirection((ViewGroup) child, layoutDirection);
            }
            child.setLayoutDirection(layoutDirection);
        }
    }

    // الحصول على اللغة الحالية
    public static String getCurrentLanguage(Context context) {
        return context.getResources().getConfiguration().locale.getLanguage();
    }

    // دالة لتغيير لون شريط الحالة وشريط التنقل

    // الدالة الأصلية التي تستقبل معرفات موارد الألوان (لا تغيير)
    public static void setSystemBarColor(Activity activity,
                                         @ColorRes int statusBarColorRes,
                                         @ColorRes int navigationBarColorRes,
                                         long duration) {
        int statusBarColor = ContextCompat.getColor(activity, statusBarColorRes);
        int navigationBarColor = ContextCompat.getColor(activity, navigationBarColorRes);
        setSystemBarColorInternal(activity, statusBarColor, navigationBarColor, duration);
    }

    // الدالة الجديدة التي تستقبل قيم الألوان الفعلية
    public static void setSystemBarColorWithColorInt(Activity activity,
                                                     @ColorInt int statusBarColor,
                                                     @ColorInt int navigationBarColor,
                                                     long duration) {
        setSystemBarColorInternal(activity, statusBarColor, navigationBarColor, duration);
    }

    // الدالة الداخلية المشتركة لتجنب تكرار الكود
    private static void setSystemBarColorInternal(Activity activity,
                                                  @ColorInt int statusBarColor,
                                                  @ColorInt int navigationBarColor,
                                                  long duration) {
        int currentStatusBarColor = activity.getWindow().getStatusBarColor();
        int currentNavigationBarColor = activity.getWindow().getNavigationBarColor();

        // تغيير لون شريط الحالة باستخدام ObjectAnimator
        ObjectAnimator statusBarColorAnim = ObjectAnimator.ofObject(
                activity.getWindow(),
                "statusBarColor",
                new ArgbEvaluator(),
                currentStatusBarColor,
                statusBarColor
        );
        statusBarColorAnim.setDuration(duration);
        statusBarColorAnim.start();

        // تغيير لون شريط التنقل باستخدام ObjectAnimator
        ObjectAnimator navigationBarColorAnim = ObjectAnimator.ofObject(
                activity.getWindow(),
                "navigationBarColor",
                new ArgbEvaluator(),
                currentNavigationBarColor,
                navigationBarColor
        );
        navigationBarColorAnim.setDuration(duration);
        navigationBarColorAnim.start();

        // تغيير المظهر بناءً على سطوع الألوان
        WindowInsetsControllerCompat windowInsetsController = new
                WindowInsetsControllerCompat(activity.getWindow(),
                activity.getWindow().getDecorView());
        windowInsetsController.setAppearanceLightStatusBars(
                isLightColor(statusBarColor));
        windowInsetsController.setAppearanceLightNavigationBars(
                isLightColor(navigationBarColor));
    }

    // التحقق من إذا كان اللون فاتحًا
    public static boolean isLightColor(int color) {
        double darkness = 1 - (0.299 * ((color >> 16) & 0xFF)
                + 0.587 * ((color >> 8) & 0xFF)
                + 0.114 * (color & 0xFF)) / 255;
        return darkness < 0.4;
    }
}

