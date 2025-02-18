// ProfileActivity.java
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class ProfileActivity extends BaseActivity {

    private ConstraintLayout courseHeaderLayout;
    private MaterialButton backButton, editButton;
    private NestedScrollView nestedScrollView;
    private RecyclerView coursesRecyclerView;
    private FloatingActionButton fabBackToTop;
    private ShapeableImageView imgProfile;
    private TextView profileName, profileBio;

    private UserRepository userRepo;
    private long profileUserId;
    private boolean isCurrentUser;
    private boolean isArabic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // bind views
        courseHeaderLayout  = findViewById(R.id.courseHeaderLayout);
        backButton          = findViewById(R.id.backButton);
        editButton          = findViewById(R.id.editButton);
        nestedScrollView    = findViewById(R.id.nestedScrollView);
        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);
        fabBackToTop        = findViewById(R.id.fabBackToTop);
        imgProfile          = findViewById(R.id.img_profile);
        profileName         = findViewById(R.id.profileName);
        profileBio          = findViewById(R.id.descriptionBio);

        // 1) اللغة
        SharedPreferences prefsLang = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        isArabic = "ar".equals(prefsLang.getString("selected_language", "en"));

        // 2) userId
        SharedPreferences prefsUser = getSharedPreferences("user_prefs", MODE_PRIVATE);
        long currentUserId = prefsUser.getLong("user_id", -1);
        if (getIntent().hasExtra("profile_user_id")) {
            profileUserId = getIntent().getLongExtra("profile_user_id", -1);
        } else {
            profileUserId = currentUserId;
        }
        isCurrentUser = currentUserId == profileUserId;

        // 3) بيانات المستخدم
        userRepo = new UserRepository(this);
        User user = userRepo.getUserById(profileUserId);
        if (user == null) { finish(); return; }

        // 4) ألوان الهيدر/نص
        int headerColor = getIntent().getIntExtra("header_color",
                ContextCompat.getColor(this, R.color.Custom_MainColorBlue));
        // بعد جلب headerColor
        boolean darkMode = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

    // في الوضع الداكن نخفف اللون بنسبة 30% نحو الأبيض لرفع التباين، وفي الوضع الفاتح نستخدم headerColor كما هو
        int textColor = darkMode
                ? ColorUtils.blendARGB(headerColor, Color.WHITE, 0.5f)
                : headerColor;
        courseHeaderLayout.setBackgroundColor(headerColor);
        Utils.setSystemBarColorWithColorInt(this, headerColor, getResources().getColor(R.color.Custom_BackgroundColor), 0);

        // 5) الصورة
        String pic = user.getProfilePicture();
        if (pic != null && pic.startsWith("@drawable/")) {
            String name = pic.substring("@drawable/".length());
            int resId = getResources().getIdentifier(name, "drawable", getPackageName());
            imgProfile.setImageResource(resId != 0 ? resId : R.drawable.avatar);
        } else if (pic != null && !pic.isEmpty()) {
            try {
                imgProfile.setImageURI(Uri.parse(pic));
            } catch (Exception e) {
                imgProfile.setImageResource(R.drawable.avatar);
            }
        } else {
            imgProfile.setImageResource(R.drawable.avatar);
        }

        // 6) الاسم والبايو
        profileName.setText(isArabic ? user.getNameAr() : user.getNameEn());
        profileBio.setText(isArabic ? user.getBioAr() : user.getBioEn());

        // 7) عناوين الأقسام
        TextView sectionContact = findViewById(R.id.sectionContact);
        TextView sectionBio     = findViewById(R.id.sectionBio);
        TextView sectionCourses = findViewById(R.id.sectionCourses);
        sectionContact.setTextColor(textColor);
        sectionBio.setTextColor(textColor);
        sectionCourses.setTextColor(textColor);

        // 8) بطاقات التواصل
        int[] cardIds = {R.id.cardPhone, R.id.cardWhatsapp, R.id.cardFacebook, R.id.cardEmail, R.id.cardTelegram};
        for (int id : cardIds) {
            MaterialCardView card = findViewById(id);
            card.setBackgroundTintList(ColorStateList.valueOf(headerColor));
        }
        fabBackToTop.setBackgroundTintList(ColorStateList.valueOf(headerColor));

        // 9) روابط التواصل
        setupContact(R.id.parentPhone,    user.getPhone(),          Intent.ACTION_DIAL,   "tel:");
        setupContact(R.id.parentWhatsapp, user.getWhatsappNumber(), Intent.ACTION_VIEW,   "https://wa.me/");
        setupContact(R.id.parentFacebook, user.getFacebookUrl(),    Intent.ACTION_VIEW,   "https://");
        setupContact(R.id.parentEmail,    user.getEmail(),          Intent.ACTION_SENDTO, "mailto:");
        setupContact(R.id.parentTelegram, user.getTelegramHandle(), Intent.ACTION_VIEW,   "https://");

        // 10) زر التعديل
        if (isCurrentUser) {
            editButton.setVisibility(View.VISIBLE);
            editButton.setOnClickListener(v ->
                    startActivity(new Intent(this, EditProfileActivity.class))
            );
        } else {
            editButton.setVisibility(View.GONE);
        }

        // 11) زر الرجوع
        backButton.setOnClickListener(v -> finish());

        // 12) تحميل وعرض الكورسات
        loadUserCourses(profileUserId);

        // 13) زر العودة للأعلى
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, x, y, ox, oy) -> {
            if (y > 600 && fabBackToTop.getVisibility() != View.VISIBLE) {
                fabBackToTop.setAlpha(0f);
                fabBackToTop.setVisibility(View.VISIBLE);
                fabBackToTop.animate()
                        .alpha(1f).setDuration(300)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
            } else if (y <= 600 && fabBackToTop.getVisibility() == View.VISIBLE) {
                fabBackToTop.animate()
                        .alpha(0f).setDuration(300)
                        .withEndAction(() -> fabBackToTop.setVisibility(View.GONE))
                        .start();
            }
        });
        fabBackToTop.setOnClickListener(v ->
                ObjectAnimator.ofInt(nestedScrollView, "scrollY",
                                nestedScrollView.getScrollY(), 0)
                        .setDuration(300)
                        .start()
        );
    }

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
        parent.setOnClickListener(v ->
                startActivity(new Intent(action, Uri.parse(uri)))
        );
    }

    private void loadUserCourses(long userId) {
        // 1) جلب القائمة (backgroundColor فيها res-id)
        List<CourseCardData> raw = userRepo.getUserCourses(userId, isArabic);

        // 2) تحويل res-id إلى لون فعلي وفرز
        List<CourseCardData> list = new ArrayList<>();
        for (CourseCardData c : raw) {
            int colorInt = ContextCompat.getColor(this, c.getBackgroundColor());
            c.setBackgroundColor(colorInt);
            list.add(c);
        }
        // فرز: مسجّلة أولاً ثم ناجحة
        Collections.sort(list, (a, b) -> {
            if (a.isRegistered() != b.isRegistered()) {
                return a.isRegistered() ? -1 : 1;
            }
            if (a.isPassed() != b.isPassed()) {
                return a.isPassed() ? 1 : -1;
            }
            return 0;
        });

        // 3) تمريرها إلى الـ adapter
        CourseCardAdapter adapter = new CourseCardAdapter(list, false);
        int cols = calculateNoOfColumns(400);
        coursesRecyclerView.setLayoutManager(new GridLayoutManager(this, cols));
        coursesRecyclerView.setAdapter(adapter);
    }

    private int calculateNoOfColumns(int columnWidthDp) {
        float wDp = getResources().getDisplayMetrics().widthPixels /
                getResources().getDisplayMetrics().density;
        return (int)(wDp / columnWidthDp + 0.5);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // إعادة تحميل بيانات المستخدم
        User user = userRepo.getUserById(profileUserId);
        if (user == null) { finish(); return; }

        profileName.setText(isArabic ? user.getNameAr() : user.getNameEn());
        profileBio.setText(isArabic ? user.getBioAr() : user.getBioEn());

        // إعادة تحميل الصورة
        String pic = user.getProfilePicture();
        if (pic != null && pic.startsWith("@drawable/")) {
            String name = pic.substring("@drawable/".length());
            int resId = getResources().getIdentifier(name, "drawable", getPackageName());
            imgProfile.setImageResource(resId != 0 ? resId : R.drawable.avatar);
        } else if (pic != null && !pic.isEmpty()) {
            try {
                imgProfile.setImageURI(Uri.parse(pic));
            } catch (Exception e) {
                imgProfile.setImageResource(R.drawable.avatar);
            }
        } else {
            imgProfile.setImageResource(R.drawable.avatar);
        }

        // إعادة تحميل روابط التواصل
        setupContact(R.id.parentPhone,    user.getPhone(),          Intent.ACTION_DIAL,   "tel:");
        setupContact(R.id.parentWhatsapp, user.getWhatsappNumber(), Intent.ACTION_VIEW,   "https://wa.me/");
        setupContact(R.id.parentFacebook, user.getFacebookUrl(),    Intent.ACTION_VIEW,   "https://");
        setupContact(R.id.parentEmail,    user.getEmail(),          Intent.ACTION_SENDTO, "mailto:");
        setupContact(R.id.parentTelegram, user.getTelegramHandle(), Intent.ACTION_VIEW,   "https://");

        // إعادة تحميل الكورسات
        loadUserCourses(profileUserId);
    }

}
