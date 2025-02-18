package org.svuonline.lms.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import org.svuonline.lms.R;
import org.svuonline.lms.ui.adapters.CourseCardAdapter;
import org.svuonline.lms.ui.data.CourseCardData;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private CourseCardAdapter adapter;
    private List<CourseCardData> favoritesList;
    private boolean isListView = false; // العرض الافتراضي: بطاقات

    // أزرار إعادة التفضيلات وتبديل العرض (بطاقات/قوائم)
    private MaterialButton resetButton, cardsBtn, listBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // إعداد شريط النظام بلون الخلفية
        Utils.setSystemBarColorWithColorInt(this,
                getResources().getColor(R.color.Custom_BackgroundColor),
                getResources().getColor(R.color.Custom_BackgroundColor), 0);


        // إعداد الـ Toolbar
        MaterialToolbar toolbarTop = findViewById(R.id.toolbar_top);
        // زر الرجوع: عند الضغط ننهي النشاط الحالي (FavoritesActivity)
        toolbarTop.setNavigationOnClickListener(v -> {
            finish();
        });

        // إعداد زر إعادة التفضيلات: عند الضغط يظهر AlertDialog للتأكيد
        resetButton = findViewById(R.id.materialButton);
        resetButton.setOnClickListener(v -> {
            if (adapter != null) {

                // 1. نفخ (Inflate) تصميم الحوار المخصص
                View customView = getLayoutInflater().inflate(R.layout.item_dialog_confirm, null);

                // 2. ربط العناصر داخل الحوار
                TextView tvMessage = customView.findViewById(R.id.tvMessage);
                MaterialButton btnCancel = customView.findViewById(R.id.btnCancel);
                MaterialButton btnConfirm = customView.findViewById(R.id.btnConfirm);

                // 3. تعيين النصوص من الموارد (اللغتين)
                tvMessage.setText(getString(R.string.fav_reset_dialog_message));

                // 4. إنشاء الحوار وتطبيق التصميم
                Dialog dialog = new Dialog(FavoritesActivity.this);
                dialog.setContentView(customView);

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                    layoutParams.copyFrom(dialog.getWindow().getAttributes());

                    // حساب ارتفاع ثابت مثلاً 160 dp
                    int heightInDp = 150;
                    int heightInPx = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            heightInDp,
                            getResources().getDisplayMetrics()
                    );

                    // الحصول على عرض الشاشة بالبكسل
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int screenWidth = displayMetrics.widthPixels;

                    // تحويل المارجن المطلوب (16 dp) إلى px
                    int marginInDp = 8;
                    int marginInPx = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            marginInDp,
                            getResources().getDisplayMetrics()
                    );

                    // حساب العرض المطلوب: عرض الشاشة - (2 * الهوامش)
                    layoutParams.width = screenWidth - (2 * marginInPx);
                    layoutParams.height = heightInPx;

                    // إذا رغبت في تحديد وضعية العرض (مثلاً في المنتصف)
                    layoutParams.gravity = Gravity.CENTER;

                    dialog.getWindow().setAttributes(layoutParams);
                }


                dialog.show();


                // 6. زر الإلغاء
                btnCancel.setOnClickListener(cancelView -> dialog.dismiss());

                // 7. زر التأكيد
                btnConfirm.setOnClickListener(confirmView -> {
                    favoritesList.clear();
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                });
            }
        });


        // إعداد أزرار تبديل العرض (بطاقات وقوائم)
        cardsBtn = findViewById(R.id.cardsBtn);
        listBtn = findViewById(R.id.listBtn);

        // الحالة الافتراضية: يتم تمييز زر البطاقات
        cardsBtn.setSelected(true);
        cardsBtn.setIconTint(AppCompatResources.getColorStateList(this, R.color.md_theme_primary));
        listBtn.setSelected(false);

        // النقر على زر البطاقات: إذا كان العرض الحالي قائمة، نقوم بالتبديل للب
        cardsBtn.setOnClickListener(v -> {
            if (isListView) {
                isListView = false;
                adapter.setListView(isListView);
                updateRecyclerViewLayout();
                adapter.notifyDataSetChanged();
                updateButtonStates();
            }
        });

        // النقر على زر القوائم: إذا كان العرض الحالي بطاقات، نقوم بالتبديل للقائمة
        listBtn.setOnClickListener(v -> {
            if (!isListView) {
                isListView = true;
                adapter.setListView(isListView);
                updateRecyclerViewLayout();
                adapter.notifyDataSetChanged();
                updateButtonStates();
            }
        });

        // إعداد الـ RecyclerView وربطه بنفس المُكيّف والبيانات المستخدمة في صفحة الكورسات
        recyclerView = findViewById(R.id.recycler_view);
        favoritesList = getDummyFavorites(); // استخدام بيانات افتراضية، ويمكن استبدالها لاحقاً بقاعدة البيانات
        adapter = new CourseCardAdapter(favoritesList, isListView);
        updateRecyclerViewLayout();
        recyclerView.setAdapter(adapter);
    }

    /**
     * تحديث تخطيط الـ RecyclerView بناءً على طريقة العرض المختارة.
     */
    private void updateRecyclerViewLayout() {
        if (isListView) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, calculateNoOfColumns(400)));
        }
        recyclerView.setAdapter(adapter); // إعادة تعيين الـ Adapter لضمان تطبيق التحديثات
    }

    /**
     * حساب عدد الأعمدة لنمط البطاقات بناءً على عرض الشاشة.
     */
    private int calculateNoOfColumns(int columnWidthDp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (screenWidthDp / columnWidthDp + 0.5);
    }

    /**
     * تحديث حالة تحديد الأزرار لتبديل العرض بين البطاقات والقوائم مع تطبيق الألوان.
     */
    private void updateButtonStates() {
        if (isListView) {
            cardsBtn.setSelected(false);
            listBtn.setSelected(true);
            listBtn.setIconTint(AppCompatResources.getColorStateList(this, R.color.md_theme_primary));
            cardsBtn.setIconTint(AppCompatResources.getColorStateList(this, R.color.Custom_Black));
        } else {
            cardsBtn.setSelected(true);
            listBtn.setSelected(false);
            cardsBtn.setIconTint(AppCompatResources.getColorStateList(this, R.color.md_theme_primary));
            listBtn.setIconTint(AppCompatResources.getColorStateList(this, R.color.Custom_Black));
        }
    }

    /**
     * دالة وهمية للحصول على بيانات التفضيلات (نعيد استخدام نفس البيانات الموجودة في صفحة الكورسات).
     * يمكنك تعديلها لاسترداد البيانات من قاعدة بيانات لاحقاً.
     */
    private List<CourseCardData> getDummyFavorites() {
        List<CourseCardData> list = new ArrayList<>();
        list.add(new CourseCardData("INT305", getString(R.string.bachelor_in_communications_technology_bact),
                getString(R.string.course_title_1), false, false, false, false, getResources().getColor(R.color.Custom_MainColorBlue)));
        list.add(new CourseCardData("GMA205", getString(R.string.bachelor_in_communications_technology_bact),
                getString(R.string.course_title_2), false, false, false, false, getResources().getColor(R.color.Custom_MainColorTeal)));
        list.add(new CourseCardData("CEE205", getString(R.string.bachelor_in_communications_technology_bact),
                getString(R.string.course_title_3), false, false, false, false, getResources().getColor(R.color.Custom_MainColorPurple)));
        // يمكنك إضافة المزيد من العناصر حسب الحاجة
        return list;
    }
}
