package org.svuonline.lms.ui.activities;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import org.svuonline.lms.R;
import org.svuonline.lms.data.model.User;
import org.svuonline.lms.data.repository.UserRepository;
import org.svuonline.lms.ui.adapters.CourseCardAdapter;
import org.svuonline.lms.ui.data.CourseCardData;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoritesActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private CourseCardAdapter adapter;
    private List<CourseCardData> favoritesList;
    private TextView emptyMessage;
    private ShapeableImageView ivProfile;
    private MaterialButton resetButton, cardsBtn, listBtn;
    private boolean isListView; // سيتم تحميله من SharedPreferences
    private long userId;
    private boolean isArabic;
    private UserRepository userRepository;
    private SharedPreferences viewPrefs;
    private static final String PREF_VIEW_MODE = "view_mode";
    private static final String PREFS_NAME = "FavoritesViewPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // 1) اللغة
        SharedPreferences prefsLang = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        isArabic = "ar".equals(prefsLang.getString("selected_language", "en"));

        // 2) userId
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = userPrefs.getLong("user_id", -1);
        if (userId == -1) {
            finish(); // إنهاء النشاط إذا لم يتم العثور على userId
            return;
        }

        // 3) تهيئة SharedPreferences لتخزين حالة العرض
        viewPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isListView = viewPrefs.getBoolean(PREF_VIEW_MODE, false); // الافتراضي: بطاقات

        // 4) تهيئة UserRepository
        userRepository = new UserRepository(this);

        // 5) ربط العناصر
        recyclerView = findViewById(R.id.recycler_view);
        emptyMessage = findViewById(R.id.empty_message);
        ivProfile = findViewById(R.id.iv_profile);
        resetButton = findViewById(R.id.materialButton);
        cardsBtn = findViewById(R.id.cardsBtn);
        listBtn = findViewById(R.id.listBtn);
        MaterialToolbar toolbarTop = findViewById(R.id.toolbar_top);

        // 6) إعداد لون شريط النظام
        Utils.setSystemBarColorWithColorInt(this, getResources().getColor(R.color.Custom_BackgroundColor), getResources().getColor(R.color.Custom_BackgroundColor), 0);

        // 7) إعداد الصورة الشخصية
        setupProfilePicture();

        // 8) إعداد زر الرجوع في Toolbar
        toolbarTop.setNavigationOnClickListener(v -> finish());

        // 9) تهيئة قائمة المفضلة وتحميل البيانات
        favoritesList = new ArrayList<>();
        loadFavoriteCourses();

        // 10) إعداد RecyclerView
        adapter = new CourseCardAdapter(favoritesList, isListView);
        updateRecyclerViewLayout();
        recyclerView.setAdapter(adapter);

        // 11) إعداد زر إعادة التعيين
        resetButton.setOnClickListener(v -> showResetDialog());

        // 12) إعداد أزرار تبديل العرض (بطاقات/قوائم)
        updateButtonStates(); // تحديث حالة الأزرار بناءً على isListView

        cardsBtn.setOnClickListener(v -> {
            if (isListView) {
                isListView = false;
                adapter.setListView(isListView);
                updateRecyclerViewLayout();
                adapter.notifyDataSetChanged();
                updateButtonStates();
                saveViewMode(); // حفظ الحالة
            }
        });

        listBtn.setOnClickListener(v -> {
            if (!isListView) {
                isListView = true;
                adapter.setListView(isListView);
                updateRecyclerViewLayout();
                adapter.notifyDataSetChanged();
                updateButtonStates();
                saveViewMode(); // حفظ الحالة
            }
        });
    }

    /**
     * حفظ حالة العرض في SharedPreferences
     */
    private void saveViewMode() {
        SharedPreferences.Editor editor = viewPrefs.edit();
        editor.putBoolean(PREF_VIEW_MODE, isListView);
        editor.apply();
    }

    /**
     * إعداد الصورة الشخصية بناءً على userId
     */
    private void setupProfilePicture() {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            ivProfile.setImageResource(R.drawable.avatar);
            return;
        }
        String pic = user.getProfilePicture();
        if (pic != null && pic.startsWith("@drawable/")) {
            String name = pic.substring("@drawable/".length());
            int resId = getResources().getIdentifier(name, "drawable", getPackageName());
            ivProfile.setImageResource(resId != 0 ? resId : R.drawable.avatar);
        } else if (pic != null && !pic.isEmpty()) {
            try {
                ivProfile.setImageURI(Uri.parse(pic));
            } catch (Exception e) {
                ivProfile.setImageResource(R.drawable.avatar);
            }
        } else {
            ivProfile.setImageResource(R.drawable.avatar);
        }
    }

    /**
     * تحميل المقررات المفضلة من قاعدة البيانات
     */
    private void loadFavoriteCourses() {
        if (userId != -1) {
            // جلب القائمة الخام (تحتوي على معرفات موارد الألوان)
            List<CourseCardData> raw = userRepository.getFavoriteCourses(userId, isArabic);

            // تحويل معرفات الألوان إلى ألوان فعلية وفرز
            favoritesList.clear();
            for (CourseCardData c : raw) {
                int colorInt = ContextCompat.getColor(this, c.getBackgroundColor());
                c.setBackgroundColor(colorInt);
                favoritesList.add(c);
            }

            // فرز: المسجلة أولاً، ثم الناجحة
            Collections.sort(favoritesList, (a, b) -> {
                if (a.isRegistered() != b.isRegistered()) {
                    return a.isRegistered() ? -1 : 1;
                }
                if (a.isPassed() != b.isPassed()) {
                    return a.isPassed() ? 1 : -1;
                }
                return 0;
            });

            // تحديث UI
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            emptyMessage.setVisibility(favoritesList.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(favoritesList.isEmpty() ? View.GONE : View.VISIBLE);
        } else {
            emptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    /**
     * إظهار حوار تأكيد إعادة تعيين المفضلة
     */
    private void showResetDialog() {
        View customView = getLayoutInflater().inflate(R.layout.item_dialog_confirm, null);
        TextView tvMessage = customView.findViewById(R.id.tvMessage);
        MaterialButton btnCancel = customView.findViewById(R.id.btnCancel);
        MaterialButton btnConfirm = customView.findViewById(R.id.btnConfirm);

        tvMessage.setText(getString(R.string.fav_reset_dialog_message));

        Dialog dialog = new Dialog(this);
        dialog.setContentView(customView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());

            int heightInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int marginInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

            layoutParams.width = displayMetrics.widthPixels - (2 * marginInPx);
            layoutParams.height = heightInPx;
            layoutParams.gravity = Gravity.CENTER;

            dialog.getWindow().setAttributes(layoutParams);
        }

        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            boolean success = userRepository.resetFavoriteCourses(userId);
            if (success) {
                favoritesList.clear();
                adapter.notifyDataSetChanged();
                emptyMessage.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
            dialog.dismiss();
        });
    }

    /**
     * تحديث تخطيط RecyclerView بناءً على وضع العرض
     */
    private void updateRecyclerViewLayout() {
        if (isListView) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, calculateNoOfColumns(400)));
        }
        recyclerView.setAdapter(adapter);
    }

    /**
     * حساب عدد الأعمدة لعرض البطاقات
     */
    private int calculateNoOfColumns(int columnWidthDp) {
        float screenWidthDp = getResources().getDisplayMetrics().widthPixels /
                getResources().getDisplayMetrics().density;
        return (int) (screenWidthDp / columnWidthDp + 0.5);
    }

    /**
     * تحديث حالة أزرار تبديل العرض
     */
    private void updateButtonStates() {
        if (isListView) {
            cardsBtn.setSelected(false);
            listBtn.setSelected(true);
            listBtn.setIconTintResource(R.color.md_theme_primary);
            cardsBtn.setIconTintResource(R.color.Custom_Black);
        } else {
            cardsBtn.setSelected(true);
            listBtn.setSelected(false);
            cardsBtn.setIconTintResource(R.color.md_theme_primary);
            listBtn.setIconTintResource(R.color.Custom_Black);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // إعادة تحميل الصورة الشخصية والمقررات المفضلة
        setupProfilePicture();
        loadFavoriteCourses();
    }
}