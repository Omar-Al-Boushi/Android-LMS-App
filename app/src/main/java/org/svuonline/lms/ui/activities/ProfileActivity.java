package org.svuonline.lms.ui.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;

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
 * نشاط لعرض ملف المستخدم الشخصي مع بيانات التواصل والمقررات المسجلة.
 */
public class ProfileActivity extends BaseActivity {

    // عناصر واجهة المستخدم
    private ConstraintLayout courseHeaderContainer;
    private MaterialButton backButton;
    private MaterialButton editButton;
    private NestedScrollView nestedScrollView;
    private RecyclerView coursesRecyclerView;
    private FloatingActionButton fabBackToTop;
    private ShapeableImageView imgProfile;
    private TextView profileName;
    private TextView profileBio;

    // المستودعات
    private UserRepository userRepository;

    // بيانات النشاط
    private long profileUserId;
    private long currentUserId;
    private boolean isCurrentUser;
    private boolean isArabic;
    private int headerColor;
    private int textColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_profile);

        // تهيئة المكونات
        initComponents();

        // تهيئة الواجهة
        initViews();

        // التحقق من بيانات Intent
        if (!validateIntentData()) {
            finish();
            return;
        }
        applyInsets();
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
    }

    /**
     * دالة لتطبيق المساحات الداخلية (Insets) بشكل برمجي.
     * هذا يضمن أن محتوى الواجهة لا يتداخل مع أشرطة النظام.
     */
    private void applyInsets() {
        // نحصل على الهامش السفلي الأصلي للزر مرة واحدة فقط لتجنب حسابه بشكل متكرر
        final int originalFabMarginBottom = ((ViewGroup.MarginLayoutParams) fabBackToTop.getLayoutParams()).bottomMargin;

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // الحصول على أبعاد شريط الحالة (من الأعلى) وشريط التنقل (من الأسفل)
            int systemBarsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // 1. التعامل مع الترويسة العلوية (هذا الجزء صحيح وموجود لديك)
            courseHeaderContainer.setPadding(0, systemBarsTop, 0, 0);

            // 2. التعامل مع المحتوى القابل للتمرير (جزء جديد)
            // نضيف padding أسفل NestedScrollView حتى لا يختفي آخر عنصر خلف شريط التنقل.
            nestedScrollView.setPadding(0, 0, 0, systemBarsBottom);

            // 3. التعامل مع زر العودة للأعلى (جزء جديد)
            // نزيد الهامش السفلي للزر لرفعه فوق شريط التنقل.
            ViewGroup.MarginLayoutParams fabLayoutParams = (ViewGroup.MarginLayoutParams) fabBackToTop.getLayoutParams();
            fabLayoutParams.bottomMargin = originalFabMarginBottom + systemBarsBottom;
            fabBackToTop.setLayoutParams(fabLayoutParams);

            // نرجع الـ insets لنخبر النظام بأننا تعاملنا معها
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * تهيئة عناصر الواجهة
     */
    private void initViews() {
        courseHeaderContainer = findViewById(R.id.courseHeaderLayout);
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);
        fabBackToTop = findViewById(R.id.fabBackToTop);
        imgProfile = findViewById(R.id.img_profile);
        profileName = findViewById(R.id.profileName);
        profileBio = findViewById(R.id.descriptionBio);
    }

    /**
     * التحقق من صحة بيانات Intent
     * @return صحيح إذا كانت البيانات صالحة، خطأ إذا لزم إنهاء النشاط
     */
    private boolean validateIntentData() {
        // جلب اللغة
        SharedPreferences prefsLang = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        isArabic = "ar".equals(prefsLang.getString("selected_language", "en"));

        // جلب userId
        SharedPreferences prefsUser = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefsUser.getLong("user_id", -1);
        if (currentUserId == -1) {
            showSnackbar(R.string.user_id_not_found);
            return false;
        }

        // جلب profileUserId
        profileUserId = getIntent().getLongExtra("profile_user_id", currentUserId);
        isCurrentUser = currentUserId == profileUserId;

        // التحقق من وجود المستخدم
        User user = userRepository.getUserById(profileUserId);
        if (user == null) {
            showSnackbar(R.string.user_id_not_found);
            return false;
        }

        return true;
    }

    /**
     * تهيئة البيانات (إعداد الواجهة، المستخدم، المقررات)
     */
    private void initData() {
        User user = userRepository.getUserById(profileUserId);
        if (user == null) {
            showSnackbar(R.string.user_id_not_found);
            finish();
            return;
        }

        // إعداد لون الهيدر والنص
        setupHeaderAndTextColor();

        // إعداد الواجهة
        setupUI(user);

        // إعداد روابط التواصل
        setupContacts(user);

        // تحميل المقررات
        loadUserCourses();
    }

    /**
     * إعداد مستمعات الأحداث (الأزرار، زر العودة للأعلى)
     */
    private void setupListeners() {
        // زر الرجوع
        backButton.setOnClickListener(v -> finish());

        // زر التعديل
        if (isCurrentUser) {
            editButton.setVisibility(View.VISIBLE);
            editButton.setOnClickListener(v -> startActivity(new Intent(this, EditProfileActivity.class)));
        } else {
            editButton.setVisibility(View.GONE);
        }

        // زر العودة للأعلى
        setupBackToTopButton();
    }

    /**
     * إعداد لون الهيدر والنص
     */
    private void setupHeaderAndTextColor() {
        headerColor = getIntent().getIntExtra("header_color", ContextCompat.getColor(this, R.color.Custom_MainColorBlue));
        boolean darkMode = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        textColor = darkMode ? ColorUtils.blendARGB(headerColor, Color.WHITE, 0.5f) : headerColor;
        courseHeaderContainer.setBackgroundColor(headerColor);
        Utils.setSystemBarColorWithColorInt(this, headerColor, getResources().getColor(R.color.Custom_BackgroundColor), 0);
    }

    /**
     * إعداد الواجهة
     * @param user بيانات المستخدم
     */
    private void setupUI(User user) {
        // إعداد الصورة
        setupProfilePicture(user.getProfilePicture());

        // إعداد الاسم والبايو
        profileName.setText(isArabic ? user.getNameAr() : user.getNameEn());
        profileBio.setText(isArabic ? user.getBioAr() : user.getBioEn());

        // إعداد عناوين الأقسام
        TextView sectionContact = findViewById(R.id.sectionContact);
        TextView sectionBio = findViewById(R.id.sectionBio);
        TextView sectionCourses = findViewById(R.id.sectionCourses);
        sectionContact.setTextColor(textColor);
        sectionBio.setTextColor(textColor);
        sectionCourses.setTextColor(textColor);

        // إعداد بطاقات التواصل
        int[] cardIds = {R.id.cardPhone, R.id.cardWhatsapp, R.id.cardFacebook, R.id.cardEmail, R.id.cardTelegram};
        for (int id : cardIds) {
            MaterialCardView card = findViewById(id);
            card.setBackgroundTintList(ColorStateList.valueOf(headerColor));
        }

        // إعداد زر العودة للأعلى
        fabBackToTop.setBackgroundTintList(ColorStateList.valueOf(headerColor));
    }

    /**
     * إعداد صورة الملف الشخصي
     * @param picture رابط أو معرف الصورة
     */
    private void setupProfilePicture(String picture) {
        if (picture != null && picture.startsWith("@drawable/")) {
            String name = picture.substring("@drawable/".length());
            int resId = getResources().getIdentifier(name, "drawable", getPackageName());
            imgProfile.setImageResource(resId != 0 ? resId : R.drawable.avatar);
        } else if (picture != null && !picture.isEmpty()) {
            try {
                imgProfile.setImageURI(Uri.parse(picture));
            } catch (Exception e) {
                imgProfile.setImageResource(R.drawable.avatar);
            }
        } else {
            imgProfile.setImageResource(R.drawable.avatar);
        }
    }

    /**
     * إعداد روابط التواصل
     * @param user بيانات المستخدم
     */
    private void setupContacts(User user) {
        setupContact(R.id.parentPhone, user.getPhone(), Intent.ACTION_DIAL, "tel:");
        setupContact(R.id.parentWhatsapp, user.getWhatsappNumber(), Intent.ACTION_VIEW, "https://wa.me/");
        setupContact(R.id.parentFacebook, user.getFacebookUrl(), Intent.ACTION_VIEW, "https://");
        setupContact(R.id.parentEmail, user.getEmail(), Intent.ACTION_SENDTO, "mailto:");
        setupContact(R.id.parentTelegram, user.getTelegramHandle(), Intent.ACTION_VIEW, "https://");
    }

    /**
     * إعداد رابط تواصل واحد
     * @param viewId معرف العنصر
     * @param data بيانات التواصل
     * @param action نوع الإجراء
     * @param prefix بادئة الرابط
     */
    private void setupContact(int viewId, String data, String action, String prefix) {
        View parent = findViewById(viewId);
        if (data == null || data.isEmpty()) {
            parent.setClickable(false);
            return;
        }
        String uri;
        if (viewId == R.id.parentWhatsapp) {
            if (!data.contains("wa.me/")) {
                uri = prefix + data.replaceFirst("^\\+?", "");
            } else if (!data.startsWith("http")) {
                uri = "https://" + data;
            } else {
                uri = data;
            }
        } else if (!data.startsWith("http")) {
            uri = prefix + data.replaceFirst("^\\+?", "");
        } else {
            uri = data;
        }
        parent.setOnClickListener(v -> startActivity(new Intent(action, Uri.parse(uri))));
    }

    /**
     * تحميل وعرض المقررات
     */
    private void loadUserCourses() {
        List<CourseCardData> raw = userRepository.getUserCourses(profileUserId, isArabic);
        List<CourseCardData> list = new ArrayList<>();
        for (CourseCardData course : raw) {
            int colorInt = ContextCompat.getColor(this, course.getBackgroundColor());
            course.setBackgroundColor(colorInt);
            list.add(course);
        }
        Collections.sort(list, (a, b) -> {
            if (a.isRegistered() != b.isRegistered()) {
                return a.isRegistered() ? -1 : 1;
            }
            if (a.isPassed() != b.isPassed()) {
                return a.isPassed() ? 1 : -1;
            }
            return 0;
        });
        CourseCardAdapter adapter = new CourseCardAdapter(list, false);
        int cols = calculateNoOfColumns(400);
        coursesRecyclerView.setLayoutManager(new GridLayoutManager(this, cols));
        coursesRecyclerView.setAdapter(adapter);
    }

    /**
     * حساب عدد الأعمدة لشبكة المقررات
     * @param columnWidthDp عرض العمود بالـ dp
     * @return عدد الأعمدة
     */
    private int calculateNoOfColumns(int columnWidthDp) {
        float wDp = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().density;
        return (int) (wDp / columnWidthDp + 0.5);
    }

    /**
     * إعداد زر العودة للأعلى
     */
    private void setupBackToTopButton() {
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, x, y, ox, oy) -> {
            if (y > 600 && fabBackToTop.getVisibility() != View.VISIBLE) {
                fabBackToTop.setAlpha(0f);
                fabBackToTop.setVisibility(View.VISIBLE);
                fabBackToTop.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
            } else if (y <= 600 && fabBackToTop.getVisibility() == View.VISIBLE) {
                fabBackToTop.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> fabBackToTop.setVisibility(View.GONE))
                        .start();
            }
        });
        fabBackToTop.setOnClickListener(v ->
                ObjectAnimator.ofInt(nestedScrollView, "scrollY", nestedScrollView.getScrollY(), 0)
                        .setDuration(300)
                        .start());
    }

    /**
     * تحديث بيانات المستخدم عند استئناف النشاط
     */
    @Override
    protected void onResume() {
        super.onResume();
        User user = userRepository.getUserById(profileUserId);
        if (user == null) {
            showSnackbar(R.string.user_id_not_found);
            finish();
            return;
        }
        profileName.setText(isArabic ? user.getNameAr() : user.getNameEn());
        profileBio.setText(isArabic ? user.getBioAr() : user.getBioEn());
        setupProfilePicture(user.getProfilePicture());
        setupContacts(user);
        loadUserCourses();
    }

    /**
     * عرض رسالة Snackbar
     * @param messageRes معرف الرسالة
     */
    private void showSnackbar(int messageRes) {
        Snackbar.make(findViewById(android.R.id.content), messageRes, Snackbar.LENGTH_LONG).show();
    }
}