package org.svuonline.lms.ui.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

import org.svuonline.lms.R;
import org.svuonline.lms.ui.data.ProfileData;
import org.svuonline.lms.ui.fragments.AssignmentsFragment;
import org.svuonline.lms.ui.fragments.CoursesFragment;
import org.svuonline.lms.ui.fragments.DashboardFragment;
import org.svuonline.lms.ui.fragments.NotificationsFragment;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

public class DashboardActivity extends BaseActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView toolbarTitle;
    private SharedPreferences sharedPreferences;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private ShapeableImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Utils.setSystemBarColor(this, R.color.Custom_BackgroundColor, R.color.Custom_BackgroundColor, 0);

        // تهيئة SharedPreferences
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        // ربط العناصر
        initViews();

        // إعداد ViewPager2
        setupViewPager();

        // ربط BottomNavigationView مع ViewPager2
        setupBottomNavigation();

        // Set initial title
        updateToolbarTitle(0);

        // احصل على مرجع إلى الهيدر وزر تغيير النمط
        View headerView = navigationView.getHeaderView(0);
        MaterialButton appearanceBtn = headerView.findViewById(R.id.appearanceBtn);

        // تحديث الأيقونة بناءً على النمط الحالي
        updateAppearanceButton(appearanceBtn, isNightModeEnabled());

        // مستمع الضغط على الزر لتغيير النمط
        appearanceBtn.setOnClickListener(v -> toggleDarkMode(appearanceBtn));

        // التعامل مع زر الرجوع لإظهار تأكيد الخروج
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmDialog();
            }
        });
    }

    private void showExitConfirmDialog() {
        // نفّذ نفس تصميم item_dialog_confirm.xml
        View customView = LayoutInflater.from(this).inflate(R.layout.item_dialog_confirm, null);
        MaterialCardView cardView = customView.findViewById(R.id.cardDialogReset);
        // تغيير لون الخلفية للبطاقة
        cardView.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.Custom_MainColorBlue));

        TextView message = customView.findViewById(R.id.tvMessage);
        message.setText(R.string.exit_confirmation_message);

        MaterialButton btnCancel = customView.findViewById(R.id.btnCancel);
        MaterialButton btnConfirm = customView.findViewById(R.id.btnConfirm);

        // إنشاء Dialog مخصص
        Dialog dialog = new Dialog(this);
        dialog.setContentView(customView);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            // جعل الخلفية شفافة
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(dialog.getWindow().getAttributes());

            // ارتفاع ثابت 150 dp
            int heightInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    170,
                    getResources().getDisplayMetrics()
            );

            // حساب عرض الشاشة مطروحاً الهامش 8dp على كل جانب
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int screenWidth = metrics.widthPixels;
            int marginPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    8,
                    getResources().getDisplayMetrics()
            );

            params.width = screenWidth - 2 * marginPx;
            params.height = heightInPx;
            params.gravity = Gravity.CENTER;

            dialog.getWindow().setAttributes(params);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            finishAffinity();
        });

        dialog.show();
    }


    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        MaterialToolbar toolbar = findViewById(R.id.toolbarTop);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(navigationView));
        profileImage = findViewById(R.id.profileImage);

        // التعامل مع القائمة الجانبية
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_profile) {
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                    // هنا نقوم بتمرير البيانات المطلوبة يدويًا أو يمكن جلبها من مصادر أخرى
                    intent.putExtra("is_current_user", true);
                    intent.putExtra("profile_name", "Omar Al Boushi");
                    intent.putExtra("profile_image_res", R.drawable.omar_photo);
                    intent.putExtra("profile_bio", "This is Omar's bio. Passionate about teaching and development.");
                    intent.putExtra("contact_phone", "0123456789");
                    intent.putExtra("contact_whatsapp", "0123456789");
                    intent.putExtra("contact_facebook", "https://facebook.com/omar");
                    intent.putExtra("contact_email", "omar@example.com");
                    intent.putExtra("contact_telegram", "omartelegram");
                    intent.putExtra("header_color", getResources().getColor(R.color.Custom_MainColorBlue));
                    intent.putExtra("text_color", getResources().getColor(R.color.md_theme_primary));
                    startActivity(intent);
                }, 100);
                // إغلاق القائمة الجانبية بعد الاختيار
            } else if (id == R.id.nav_settings) {
                Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_favourites) {
                Intent intent = new Intent(DashboardActivity.this, FavoritesActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_calendar) {
                Intent intent = new Intent(DashboardActivity.this, CalendarActivity.class);
                startActivity(intent);
            } else {
                // التعامل مع باقي عناصر القائمة الجانبية (على سبيل المثال، عناصر أخرى مثل الداشبورد، الكورسات، ... إلخ)
                menuItem.setChecked(true);
            }
            // إلغاء التحديد مباشرة قبل إغلاق القائمة
            menuItem.setChecked(false);
            drawerLayout.closeDrawer(GravityCompat.START);
            return false;
        });
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            // هنا نقوم بتمرير البيانات المطلوبة يدويًا أو يمكن جلبها من مصادر أخرى
            intent.putExtra("is_current_user", true);
            intent.putExtra("profile_name", "Omar Al Boushi");
            intent.putExtra("profile_image_res", R.drawable.omar_photo);
            intent.putExtra("profile_bio", "This is Omar's bio. Passionate about teaching and development.");
            intent.putExtra("contact_phone", "0123456789");
            intent.putExtra("contact_whatsapp", "0123456789");
            intent.putExtra("contact_facebook", "https://facebook.com/omar");
            intent.putExtra("contact_email", "omar@example.com");
            intent.putExtra("contact_telegram", "omartelegram");
            intent.putExtra("header_color", getResources().getColor(R.color.Custom_MainColorBlue));
            intent.putExtra("text_color", getResources().getColor(R.color.md_theme_primary));
            startActivity(intent);
        });


        // تهيئة ViewPager2 و BottomNavigationView
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }


    private void setupViewPager() {
        DashboardPagerAdapter pagerAdapter = new DashboardPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // تعطيل التمرير الأفقي إذا لزم الأمر
        viewPager.setUserInputEnabled(true);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.fragment_dashboard) {
                viewPager.setCurrentItem(0);
            } else if (itemId == R.id.fragment_courses) {
                viewPager.setCurrentItem(1);
            } else if (itemId == R.id.fragment_assignments) {
                viewPager.setCurrentItem(2);
            } else if (itemId == R.id.fragment_notifications) {
                viewPager.setCurrentItem(3);
            }
            return true;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                updateToolbarTitle(position);
            }
        });
    }

    // Adapter للفراغمنتات
    private static class DashboardPagerAdapter extends FragmentStateAdapter {

        public DashboardPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new DashboardFragment();
                case 1:
                    return new CoursesFragment();
                case 2:
                    return new AssignmentsFragment();
                case 3:
                    return new NotificationsFragment();
                default:
                    throw new IllegalArgumentException("Invalid position: " + position);
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }

    private void updateToolbarTitle(int position) {
        String title = "";
        switch (position) {
            case 0:
                title = getString(R.string.dashboard); // Use string resources
                break;
            case 1:
                title = getString(R.string.courses); // Use string resources
                break;
            case 2:
                title = getString(R.string.assignment); // Use string resources
                break;
            case 3:
                title = getString(R.string.notifications); // Use string resources
                break;
        }
        toolbarTitle.setText(title);
    }


    // دالة لتحديث الأيقونة بناءً على النمط الحالي
    private void updateAppearanceButton(MaterialButton button, boolean isNightMode) {
        int iconRes = isNightMode ? R.drawable.darkmode : R.drawable.lightmode;
        ColorStateList tint = ContextCompat.getColorStateList(this, isNightMode ? R.color.md_theme_primary : R.color.Custom_MainColorGolden);

        button.setIconResource(iconRes);
        button.setIconTint(tint);
    }

    // دالة لفحص ما إذا كان النمط الداكن مفعلًا
    private boolean isNightModeEnabled() {
        return sharedPreferences.getString("selected_mode", "light").equals("dark");
    }

    // دالة لتبديل النمط وتحديث الواجهة مع تأثير سلسل
    private void toggleDarkMode(MaterialButton button) {
        boolean isNightMode = isNightModeEnabled();
        String newMode = isNightMode ? "light" : "dark";

        // حفظ النمط الجديد
        sharedPreferences.edit().putString("selected_mode", newMode).apply();

        // تحديث الأيقونة قبل إعادة التشغيل
        updateAppearanceButton(button, !isNightMode);

        // تأثير التلاشي قبل إعادة تشغيل النشاط
        applyFadeAnimationAndRestart();
    }

    // دالة لتطبيق تأثير التلاشي ثم إعادة تشغيل النشاط بسلاسة
    private void applyFadeAnimationAndRestart() {
        View rootView = findViewById(android.R.id.content);
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(300); // مدة التلاشي
        fadeOut.setFillAfter(true);

        fadeOut.setAnimationListener(new AlphaAnimation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {
            }

            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                // إعادة تشغيل النشاط بعد انتهاء التلاشي
                restartActivityWithAnimation();
            }

            @Override
            public void onAnimationRepeat(android.view.animation.Animation animation) {
            }
        });

        rootView.startAnimation(fadeOut);
    }

    // دالة لإعادة تشغيل النشاط مع تأثير تلاشي سلسل
    private void restartActivityWithAnimation() {
        Intent intent = getIntent();
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(intent);
    }
}