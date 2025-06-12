package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.svuonline.lms.R;
import org.svuonline.lms.ui.adapters.ViewPagerWelcomeAdapter;
import org.svuonline.lms.ui.data.WelcomeData;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * نشاط شاشة الترحيب مع شرائح تعريفية ومؤشرات مخصصة.
 */
public class WelcomeActivity extends BaseActivity {

    // مفاتيح SharedPreferences
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_REMEMBER_ME = "remember_me";

    // عناصر واجهة المستخدم
    private Button btnGetStarted;
    private ViewPager2 viewPager2;
    private TabLayout tabIndicator;

    // البيانات
    private ViewPagerWelcomeAdapter adapter;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // التحقق من حالة تسجيل الدخول
        if (checkLoginStatus()) {
            return;
        }

        setContentView(R.layout.activity_welcome);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // تهيئة الواجهة
        initViews();

        applyInsets();

        // إعداد شريط الحالة
        setupSystemBar();

        // إعداد ViewPager
        setupViewPager();

        // إعداد مؤشرات TabLayout
        setupTabIndicator();

        // إعداد مستمعات الأحداث
        setupListeners();
    }

    /**
     * دالة لتطبيق المساحات الداخلية (Insets) بشكل برمجي.
     */
    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            // الحصول على أبعاد شريط الحالة (من الأعلى) وشريط التنقل (من الأسفل)
            int systemBarsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // 1. إضافة padding علوي للـ ViewPager2 لتجنب تداخل محتواه مع شريط الحالة
            viewPager2.setPadding(viewPager2.getPaddingLeft(), systemBarsTop, viewPager2.getPaddingRight(), viewPager2.getPaddingBottom());

            // 2. زيادة الهامش السفلي لزر "التالي" لرفعه فوق شريط التنقل
            // ملاحظة: الرقم 24dp هو الهامش الأصلي المفترض من التصميم، قد تحتاج لتعديله
            ViewGroup.MarginLayoutParams buttonParams = (ViewGroup.MarginLayoutParams) btnGetStarted.getLayoutParams();
            int buttonOriginalMarginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
            buttonParams.bottomMargin = buttonOriginalMarginBottom + systemBarsBottom;
            btnGetStarted.setLayoutParams(buttonParams);

            // 3. زيادة الهامش السفلي لمؤشر الصفحات لرفعه أيضاً
            // ملاحظة: الرقم 16dp هو الهامش الأصلي المفترض من التصميم
            ViewGroup.MarginLayoutParams indicatorParams = (ViewGroup.MarginLayoutParams) tabIndicator.getLayoutParams();
            int indicatorOriginalMarginBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            indicatorParams.bottomMargin = indicatorOriginalMarginBottom + systemBarsBottom;
            tabIndicator.setLayoutParams(indicatorParams);


            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // إلغاء تسجيل مستمع ViewPager لتجنب تسرب الذاكرة
        if (viewPager2 != null && pageChangeCallback != null) {
            viewPager2.unregisterOnPageChangeCallback(pageChangeCallback);
        }
    }

    /**
     * التحقق من حالة تسجيل الدخول وإعادة التوجيه إذا لزم الأمر
     * @return true إذا تم إعادة التوجيه، false إذا استمر النشاط
     */
    private boolean checkLoginStatus() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean(KEY_REMEMBER_ME, false);
        if (isLoggedIn) {
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    /**
     * تهيئة عناصر الواجهة
     */
    private void initViews() {
        viewPager2 = findViewById(R.id.viewPager);
        tabIndicator = findViewById(R.id.tabIndicator);
        btnGetStarted = findViewById(R.id.btnGetStarted);
    }

    /**
     * إعداد شريط الحالة
     */
    private void setupSystemBar() {
        Utils.setSystemBarColor(this, R.color.Custom_MainColorBlue, R.color.Custom_MainColorBlue, 0);
    }

    /**
     * إعداد ViewPager مع بيانات الشرائح
     */
    private void setupViewPager() {
        List<WelcomeData> items = new ArrayList<>();
        items.add(new WelcomeData(R.drawable.explore, getString(R.string.explore)));
        items.add(new WelcomeData(R.drawable.learn, getString(R.string.learn)));
        items.add(new WelcomeData(R.drawable.connect, getString(R.string.connect)));
        items.add(new WelcomeData(R.drawable.study, getString(R.string.study)));

        adapter = new ViewPagerWelcomeAdapter(items);
        viewPager2.setAdapter(adapter);
    }

    /**
     * إعداد مؤشرات TabLayout المخصصة
     */
    private void setupTabIndicator() {
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_selected},
                        new int[]{-android.R.attr.state_selected}
                },
                new int[]{
                        ContextCompat.getColor(this, R.color.Custom_MainColorGolden),
                        ContextCompat.getColor(this, R.color.white)
                }
        );

        int selectedSize = (int) getResources().getDimension(R.dimen.tab_selected_size);
        int unselectedSize = (int) getResources().getDimension(R.dimen.tab_unselected_size);

        new TabLayoutMediator(tabIndicator, viewPager2, (tab, position) -> {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.circle);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            int size = position == viewPager2.getCurrentItem() ? selectedSize : unselectedSize;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
            layoutParams.setMargins(4, 0, 4, 0);
            imageView.setLayoutParams(layoutParams);
            imageView.setImageTintList(colorStateList);

            tab.setCustomView(imageView);
        }).attach();

        tabIndicator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ImageView imageView = (ImageView) tab.getCustomView();
                if (imageView != null) {
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            selectedSize, selectedSize
                    );
                    layoutParams.setMargins(4, 0, 4, 0);
                    imageView.setLayoutParams(layoutParams);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                ImageView imageView = (ImageView) tab.getCustomView();
                if (imageView != null) {
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            unselectedSize, unselectedSize
                    );
                    layoutParams.setMargins(4, 0, 4, 0);
                    imageView.setLayoutParams(layoutParams);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    /**
     * إعداد مستمعات الأحداث للأزرار وViewPager
     */
    private void setupListeners() {
        btnGetStarted.setOnClickListener(view -> {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem < adapter.getItemCount() - 1) {
                viewPager2.setCurrentItem(currentItem + 1);
            } else {
                navigateToLanguageSelection();
            }
        });

        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateButtonState();
            }
        };
        viewPager2.registerOnPageChangeCallback(pageChangeCallback);
    }

    /**
     * تحديث حالة زر "التالي/ابدأ" بناءً على الشريحة الحالية
     */
    private void updateButtonState() {
        if (viewPager2.getCurrentItem() == adapter.getItemCount() - 1) {
            btnGetStarted.setText(R.string.get_started);
            btnGetStarted.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.Custom_MainColorGolden));
        } else {
            btnGetStarted.setText(R.string.next);
            btnGetStarted.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.md_theme_onPrimaryFixedVariant));
        }
    }

    /**
     * الانتقال إلى LanguageSelectionActivity
     */
    private void navigateToLanguageSelection() {
        Intent intent = new Intent(this, LanguageSelectionActivity.class);
        startActivity(intent);
    }
}