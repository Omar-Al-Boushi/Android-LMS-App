package org.svuonline.lms.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

import org.svuonline.lms.R;
import org.svuonline.lms.data.model.User;
import org.svuonline.lms.data.repository.UserRepository;
import org.svuonline.lms.ui.fragments.AssignmentsFragment;
import org.svuonline.lms.ui.fragments.CoursesFragment;
import org.svuonline.lms.ui.fragments.DashboardFragment;
import org.svuonline.lms.ui.fragments.NotificationsFragment;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.util.Objects;

/**
 * النشاط الرئيسي لعرض لوحة التحكم مع قائمة جانبية، تنقل سفلي، وصفحات فراغمنت.
 */
public class DashboardActivity extends BaseActivity implements
        DashboardFragment.OnCourseFilterListener, DashboardFragment.OnAssignmentFilterListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private TextView toolbarTitle;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private ShapeableImageView profileImage;
    private SharedPreferences preferences;
    private UserRepository userRepository;
    private long currentUserId;
    private long userId;
    private static final String PREFS_NAME = "AppPreferences";
    private static final String PREF_MODE_KEY = "selected_mode";
    private static final String MODE_DARK = "dark";
    private static final String MODE_LIGHT = "light";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Utils.setSystemBarColor(this, R.color.Custom_BackgroundColor, R.color.Custom_BackgroundColor, 0);

        // جلب userId من SharedPreferences
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = userPrefs.getLong("user_id", -1);
        Log.d("DashboardActivity", "userId: " + userId);
        preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        userRepository = new UserRepository(this);


        initViews();
        loadUserData();
        setupViewPager();
        setupBottomNavigation();
        updateToolbarTitle(0);
        setupDrawerNavigation();
        setupBackHandler();

        String filterStatus = getIntent().getStringExtra("filter_status");
        if (filterStatus != null) {
            navigateToCoursesFragment(filterStatus);
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbarTop);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        profileImage = findViewById(R.id.profileImage);
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        profileImage.setOnClickListener(v -> openProfile());
    }

    private void loadUserData() {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            redirectToLogin();
            return;
        }

        View header = navigationView.getHeaderView(0);
        ShapeableImageView navImage = header.findViewById(R.id.nav_header_profile_image);
        TextView tvName = header.findViewById(R.id.nav_header_username);
        TextView tvProgram = header.findViewById(R.id.nav_header_program);
        MaterialButton appearanceBtn = header.findViewById(R.id.appearanceBtn);

        tvName.setText(isArabicLocale() ? user.getNameAr() : user.getNameEn());
        String programName = userRepository.getProgramNameById(user.getProgramId(), isArabicLocale());
        tvProgram.setText(programName.isEmpty() ? getString(R.string.unspecified) : programName);

        loadProfileImage(navImage, user.getProfilePicture());
        loadProfileImage(profileImage, user.getProfilePicture());

        appearanceBtn.setOnClickListener(v -> toggleDarkMode(appearanceBtn));
        updateAppearanceButton(appearanceBtn, isNightModeEnabled());
        header.setOnClickListener(v -> openProfile());
    }

    private void loadProfileImage(ShapeableImageView imageView, String picRef) {
        if (picRef != null && picRef.startsWith("@drawable/")) {
            int resId = getResources().getIdentifier(picRef.substring(10), "drawable", getPackageName());
            imageView.setImageResource(resId);
        } else if (picRef != null && !picRef.isEmpty()) {
            try {
                imageView.setImageURI(Uri.parse(picRef));
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.profile);
            }
        } else {
            imageView.setImageResource(R.drawable.profile);
        }
    }

    private void setupDrawerNavigation() {
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                viewPager.postDelayed(this::openProfile, 200);
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (itemId == R.id.nav_favourites) {
                startActivity(new Intent(this, FavoritesActivity.class));
            } else if (itemId == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
            }
            return true;
        });
    }

    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("is_current_user", true);
        startActivity(intent);
    }

    private void setupViewPager() {
        viewPager.setAdapter(new DashboardPagerAdapter(this));
        viewPager.setUserInputEnabled(true);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.fragment_dashboard) viewPager.setCurrentItem(0);
            else if (itemId == R.id.fragment_courses) viewPager.setCurrentItem(1);
            else if (itemId == R.id.fragment_assignments) viewPager.setCurrentItem(2);
            else if (itemId == R.id.fragment_notifications) viewPager.setCurrentItem(3);
            return true;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                updateToolbarTitle(position);
            }
        });
    }

    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmDialog();
            }
        });
    }

    private class DashboardPagerAdapter extends FragmentStateAdapter {
        private final DashboardActivity activity;

        DashboardPagerAdapter(@NonNull DashboardActivity activity) {
            super(activity);
            this.activity = activity;
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
                    throw new IllegalArgumentException("موضع غير صالح: " + position);
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }

    private void updateToolbarTitle(int position) {
        int[] titles = {R.string.dashboard, R.string.courses, R.string.assignment, R.string.notifications};
        toolbarTitle.setText(getString(titles[position]));
    }

    private void showExitConfirmDialog() {
        Dialog dialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.item_dialog_confirm, null);
        MaterialCardView card = view.findViewById(R.id.cardDialogReset);
        card.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.Custom_MainColorBlue));
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(R.string.exit_confirmation_message);

        dialog.setContentView(view);
        dialog.setCancelable(false);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        params.width = metrics.widthPixels - 2 * (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        params.height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 170, getResources().getDisplayMetrics());
        params.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(params);

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            dialog.dismiss();
            finishAffinity();
        });

        dialog.show();
    }

    private boolean isArabicLocale() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String selectedLanguage = prefs.getString("selected_language", "en");
        return "ar".equals(selectedLanguage);
    }

    private boolean isNightModeEnabled() {
        return preferences.getString(PREF_MODE_KEY, MODE_LIGHT).equals(MODE_DARK);
    }

    private void toggleDarkMode(MaterialButton button) {
        boolean isNightMode = isNightModeEnabled();
        preferences.edit().putString(PREF_MODE_KEY, isNightMode ? MODE_LIGHT : MODE_DARK).apply();
        updateAppearanceButton(button, !isNightMode);
        applyFadeAnimationAndRestart();
    }

    private void updateAppearanceButton(MaterialButton button, boolean isNightMode) {
        button.setIconResource(isNightMode ? R.drawable.darkmode : R.drawable.lightmode);
        button.setIconTint(ContextCompat.getColorStateList(
                this, isNightMode ? R.color.md_theme_onSurface_highContrast : R.color.Custom_MainColorGolden));
    }

    private void applyFadeAnimationAndRestart() {
        View root = findViewById(android.R.id.content);
        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(300);
        fadeOut.setFillAfter(true);
        fadeOut.setAnimationListener(new AlphaAnimation.AnimationListener() {
            @Override
            public void onAnimationStart(android.view.animation.Animation animation) {
            }

            @Override
            public void onAnimationEnd(android.view.animation.Animation animation) {
                restartActivityWithAnimation();
            }

            @Override
            public void onAnimationRepeat(android.view.animation.Animation animation) {
            }
        });
        root.startAnimation(fadeOut);
    }

    private void restartActivityWithAnimation() {
        Intent intent = getIntent();
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(intent);
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onCourseFilterSelected(String status) {
        navigateToCoursesFragment(status);
    }

    @Override
    public void onAssignmentFilterSelected(String filter) {
        navigateToAssignmentFragment(filter);
    }

    private void navigateToCoursesFragment(String status) {
        viewPager.setCurrentItem(1, true); // الانتقال إلى CoursesFragment مع تأثير سلس
        bottomNavigationView.setSelectedItemId(R.id.fragment_courses);

        // البحث عن CoursesFragment الحالي
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getAdapter().getItemId(1));
        if (currentFragment instanceof CoursesFragment) {
            ((CoursesFragment) currentFragment).applyFilter(status);
        } else {
            // إذا لم يتم العثور على CoursesFragment، مرر الحالة عبر Bundle
            CoursesFragment fragment = new CoursesFragment();
            Bundle args = new Bundle();
            args.putString("filter_status", status);
            fragment.setArguments(args);
            // تحديث المحول لإعادة إنشاء CoursesFragment
            viewPager.setAdapter(new DashboardPagerAdapter(this));
            viewPager.setCurrentItem(1, true);
        }
    }

    private void navigateToAssignmentFragment(String filter) {
        viewPager.setCurrentItem(2, true); // الانتقال إلى CoursesFragment مع تأثير سلس
        bottomNavigationView.setSelectedItemId(R.id.fragment_assignments);

        // البحث عن AssignmentsFragment الحالي
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getAdapter().getItemId(2));
        if (currentFragment instanceof AssignmentsFragment) {
            ((AssignmentsFragment) currentFragment).applyFilter(filter);
        } else {
            // إذا لم يتم العثور على CoursesFragment، مرر الحالة عبر Bundle
            AssignmentsFragment fragment = new AssignmentsFragment();
            Bundle args = new Bundle();
            args.putString("filter_status2", filter);
            fragment.setArguments(args);
            // تحديث المحول لإعادة إنشاء CoursesFragment
            viewPager.setAdapter(new DashboardPagerAdapter(this));
            viewPager.setCurrentItem(2, true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // إعادة تحميل بيانات المستخدم لتعكس أي تغييرات
        loadUserData();
    }
}