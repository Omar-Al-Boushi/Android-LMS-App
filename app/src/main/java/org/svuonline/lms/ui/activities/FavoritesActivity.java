package org.svuonline.lms.ui.activities;

import android.app.Dialog;
import android.content.Intent;
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

/**
 * نشاط لعرض المقررات المفضلة مع خيارات تبديل العرض وإعادة التعيين.
 */
public class FavoritesActivity extends BaseActivity {

    // مفاتيح SharedPreferences
    private static final String PREFS_NAME = "FavoritesViewPrefs";
    private static final String PREF_VIEW_MODE = "view_mode";

    // عناصر واجهة المستخدم
    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private TextView emptyMessage;
    private ShapeableImageView ivProfile;
    private MaterialButton resetButton;
    private MaterialButton cardsBtn;
    private MaterialButton listBtn;

    // المستودعات
    private UserRepository userRepository;

    // بيانات النشاط
    private long userId;
    private boolean isArabic;
    private boolean isListView;
    private List<CourseCardData> favoritesList;
    private CourseCardAdapter adapter;
    private SharedPreferences viewPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // تهيئة المكونات
        initComponents();

        // تهيئة الواجهة
        initViews();

        // التحقق من بيانات المستخدم
        if (!validateUserData()) {
            return;
        }

        // تهيئة البيانات
        initData();

        // إعداد مستمعات الأحداث
        setupListeners();
    }

    /**
     * تهيئة المستودعات
     */
    private void initComponents() {
        userRepository = new UserRepository(this);
        viewPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    /**
     * تهيئة عناصر الواجهة
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar_top);
        recyclerView = findViewById(R.id.recycler_view);
        emptyMessage = findViewById(R.id.empty_message);
        ivProfile = findViewById(R.id.iv_profile);
        resetButton = findViewById(R.id.materialButton);
        cardsBtn = findViewById(R.id.cardsBtn);
        listBtn = findViewById(R.id.listBtn);
    }

    /**
     * التحقق من صحة بيانات المستخدم
     * @return صحيح إذا كانت البيانات صالحة، خطأ إذا لزم إنهاء النشاط
     */
    private boolean validateUserData() {
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = userPrefs.getLong("user_id", -1);
        if (userId == -1) {
            showToast(R.string.user_id_not_found);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return false;
        }
        return true;
    }

    /**
     * تهيئة البيانات (اللغة، العرض، المفضلة، الصورة)
     */
    private void initData() {
        // إعداد لون شريط النظام
        Utils.setSystemBarColorWithColorInt(this,
                getResources().getColor(R.color.Custom_BackgroundColor),
                getResources().getColor(R.color.Custom_BackgroundColor), 0);

        // جلب اللغة
        SharedPreferences prefsLang = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        isArabic = "ar".equals(prefsLang.getString("selected_language", "en"));

        // جلب حالة العرض
        isListView = viewPrefs.getBoolean(PREF_VIEW_MODE, false);

        // تهيئة قائمة المفضلة
        favoritesList = new ArrayList<>();
        loadFavoriteCourses();

        // إعداد RecyclerView
        adapter = new CourseCardAdapter(favoritesList, isListView);
        updateRecyclerViewLayout();
        recyclerView.setAdapter(adapter);

        // إعداد الصورة الشخصية
        setupProfilePicture();

        // تحديث حالة الأزرار
        updateButtonStates();
    }

    /**
     * إعداد مستمعات الأحداث (الشريط العلوي، الأزرار)
     */
    private void setupListeners() {
        // إعداد الشريط العلوي
        toolbar.setNavigationOnClickListener(v -> navigateBack());

        // إعداد زر إعادة التعيين
        resetButton.setOnClickListener(v -> showResetDialog());

        // إعداد أزرار تبديل العرض
        cardsBtn.setOnClickListener(v -> switchToCardsView());
        listBtn.setOnClickListener(v -> switchToListView());
    }

    /**
     * إعداد الصورة الشخصية
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
     * تحميل المقررات المفضلة
     */
    private void loadFavoriteCourses() {
        favoritesList.clear();
        if (userId != -1) {
            List<CourseCardData> raw = userRepository.getFavoriteCourses(userId, isArabic);
            for (CourseCardData c : raw) {
                int colorInt = ContextCompat.getColor(this, c.getBackgroundColor());
                c.setBackgroundColor(colorInt);
                favoritesList.add(c);
            }
            Collections.sort(favoritesList, (a, b) -> {
                if (a.isRegistered() != b.isRegistered()) {
                    return a.isRegistered() ? -1 : 1;
                }
                if (a.isPassed() != b.isPassed()) {
                    return a.isPassed() ? 1 : -1;
                }
                return 0;
            });
        }
        updateFavoritesUI();
    }

    /**
     * تحديث واجهة المفضلة
     */
    private void updateFavoritesUI() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        emptyMessage.setVisibility(favoritesList.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(favoritesList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /**
     * إظهار حوار تأكيد إعادة التعيين
     */
    private void showResetDialog() {
        View customView = getLayoutInflater().inflate(R.layout.item_dialog_confirm, null);
        TextView tvMessage = customView.findViewById(R.id.tvMessage);
        MaterialButton btnCancel = customView.findViewById(R.id.btnCancel);
        MaterialButton btnConfirm = customView.findViewById(R.id.btnConfirm);

        tvMessage.setText(getString(R.string.fav_reset_dialog_message));

        Dialog dialog = new Dialog(this);
        dialog.setContentView(customView);
        configureDialogWindow(dialog);

        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            resetFavorites();
            dialog.dismiss();
        });
    }

    /**
     * تهيئة نافذة الحوار
     * @param dialog الحوار المراد تهيئته
     */
    private void configureDialogWindow(Dialog dialog) {
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
    }

    /**
     * إعادة تعيين المفضلة
     */
    private void resetFavorites() {
        boolean success = userRepository.resetFavoriteCourses(userId);
        if (success) {
            favoritesList.clear();
            updateFavoritesUI();
            showToast(R.string.fav_reset_success);
        } else {
            showToast(R.string.fav_reset_failed);
        }
    }

    /**
     * تحديث تخطيط RecyclerView
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
     * @param columnWidthDp عرض العمود بالـ dp
     * @return عدد الأعمدة
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
        cardsBtn.setSelected(!isListView);
        listBtn.setSelected(isListView);
        listBtn.setIconTintResource(isListView ? R.color.md_theme_primary : R.color.Custom_Black);
        cardsBtn.setIconTintResource(isListView ? R.color.Custom_Black : R.color.md_theme_primary);
    }

    /**
     * التبديل إلى عرض البطاقات
     */
    private void switchToCardsView() {
        if (isListView) {
            isListView = false;
            adapter.setListView(isListView);
            updateRecyclerViewLayout();
            adapter.notifyDataSetChanged();
            updateButtonStates();
            saveViewMode();
        }
    }

    /**
     * التبديل إلى عرض القوائم
     */
    private void switchToListView() {
        if (!isListView) {
            isListView = true;
            adapter.setListView(isListView);
            updateRecyclerViewLayout();
            adapter.notifyDataSetChanged();
            updateButtonStates();
            saveViewMode();
        }
    }

    /**
     * حفظ حالة العرض
     */
    private void saveViewMode() {
        viewPrefs.edit().putBoolean(PREF_VIEW_MODE, isListView).apply();
    }

    /**
     * الانتقال للخلف
     */
    private void navigateBack() {
        finish();
    }

    /**
     * تحديث الصورة والمفضلة عند استئناف النشاط
     */
    @Override
    protected void onResume() {
        super.onResume();
        setupProfilePicture();
        loadFavoriteCourses();
    }

    /**
     * عرض رسالة Toast
     * @param messageRes معرف الرسالة
     */
    private void showToast(int messageRes) {
        android.widget.Toast.makeText(this, messageRes, android.widget.Toast.LENGTH_SHORT).show();
    }
}