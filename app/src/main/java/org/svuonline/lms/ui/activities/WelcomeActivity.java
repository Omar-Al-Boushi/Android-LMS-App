package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import org.svuonline.lms.R;
import org.svuonline.lms.ui.data.WelcomeData;
import org.svuonline.lms.ui.adapters.ViewPagerWelcomeAdapter;
import org.svuonline.lms.utils.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends BaseActivity {

    private Button btnGetStarted;
    private ViewPager2 viewPager2;
    private ViewPagerWelcomeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // التحقق مما إذا كان المستخدم قد سجل دخوله مسبقًا
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("remember_me", false);
        if (isLoggedIn) {
            // إعادة التوجيه إلى الشاشة الرئيسية
            Intent intent = new Intent(WelcomeActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_welcome);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // تهيئة عناصر واجهة المستخدم
        viewPager2 = findViewById(R.id.viewPager);
        TabLayout tabIndicator = findViewById(R.id.tabIndicator);
        btnGetStarted = findViewById(R.id.btnGetStarted);

        // إنشاء قائمة بيانات الشرائح التعريفية
        List<WelcomeData> items = new ArrayList<>();
        items.add(new WelcomeData(R.drawable.explore, getString(R.string.explore)));
        items.add(new WelcomeData(R.drawable.learn, getString(R.string.learn)));
        items.add(new WelcomeData(R.drawable.connect, getString(R.string.connect)));
        items.add(new WelcomeData(R.drawable.study, getString(R.string.study)));

        adapter = new ViewPagerWelcomeAdapter(items);
        viewPager2.setAdapter(adapter);

        // تعريف ألوان وحجم مؤشر الشرائح
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

            int size = position == 0 ? selectedSize : unselectedSize;
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

        btnGetStarted.setOnClickListener(view -> {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem < adapter.getItemCount() - 1) {
                viewPager2.setCurrentItem(currentItem + 1);
            } else {
                // التعامل مع الحالة النهائية، مثل الانتقال إلى نشاط آخر
                Intent intent = new Intent(WelcomeActivity.this, LanguageSelectionActivity.class);
                startActivity(intent);
            }
            updateButtonState();
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateButtonState();
            }
        });
    }

    /**
     * تحديث حالة زر "التالي/ابدأ" بناءً على الشريحة الحالية.
     */
    private void updateButtonState() {
        if (viewPager2.getCurrentItem() == adapter.getItemCount() - 1) {
            btnGetStarted.setText(getString(R.string.get_started));
            btnGetStarted.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.Custom_MainColorGolden));
        } else {
            btnGetStarted.setText(getString(R.string.next));
            btnGetStarted.setBackgroundTintList(AppCompatResources.getColorStateList(this, R.color.md_theme_onPrimaryFixedVariant));
        }
    }

}
