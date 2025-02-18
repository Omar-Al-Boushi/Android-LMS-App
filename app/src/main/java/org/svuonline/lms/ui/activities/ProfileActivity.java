package org.svuonline.lms.ui.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import org.svuonline.lms.R;
import org.svuonline.lms.ui.adapters.CourseCardAdapter;
import org.svuonline.lms.ui.data.CourseCardData;
import org.svuonline.lms.ui.data.ProfileData;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseActivity {

    // عناصر الواجهة
    private ConstraintLayout courseHeaderLayout;
    private MaterialButton backButton, editButton;
    private NestedScrollView nestedScrollView;
    private RecyclerView coursesRecyclerView;
    private FloatingActionButton fabBackToTop;
    private ShapeableImageView imgProfile;
    private TextView profileName, profileBio;

    // بيانات الكورسات والمحول
    private List<CourseCardData> courseList;
    private CourseCardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // تأكد من تطابق اسم ملف XML

        // ربط عناصر الواجهة
        courseHeaderLayout = findViewById(R.id.courseHeaderLayout);
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        coursesRecyclerView = findViewById(R.id.coursesRecyclerView);
        fabBackToTop = findViewById(R.id.fabBackToTop);
        imgProfile = findViewById(R.id.img_profile);
        profileName = findViewById(R.id.profileName);
        profileBio = findViewById(R.id.descriptionBio); // عنصر الوصف

        // قراءة البيانات المُرسلة عبر الـ Intent، إذا لم تتوفر يتم استخدام القيم الافتراضية
        Intent intent = getIntent();
        boolean isCurrentUser = intent.getBooleanExtra("is_current_user", false);

        String name = intent.getStringExtra("profile_name");
        if (name == null || name.isEmpty()) {
            name = "Default Name";  // أدخل الاسم الافتراضي هنا
        }

        int imageRes = intent.hasExtra("profile_image_res") ?
                intent.getIntExtra("profile_image_res", R.drawable.ic_launcher_background) :
                R.drawable.ic_launcher_background;  // صورة افتراضية

        String bio = intent.getStringExtra("profile_bio");
        if (bio == null || bio.isEmpty()) {
            bio = "Default bio text. This is a description about the user.";
        }

        String contactPhone = intent.getStringExtra("contact_phone");
        if (contactPhone == null) contactPhone = "";
        String contactWhatsapp = intent.getStringExtra("contact_whatsapp");
        if (contactWhatsapp == null) contactWhatsapp = "";
        String contactFacebook = intent.getStringExtra("contact_facebook");
        if (contactFacebook == null) contactFacebook = "";
        String contactEmail = intent.getStringExtra("contact_email");
        if (contactEmail == null) contactEmail = "";
        String contactTelegram = intent.getStringExtra("contact_telegram");
        if (contactTelegram == null) contactTelegram = "";

        int headerColor = intent.hasExtra("header_color") ?
                intent.getIntExtra("header_color", getResources().getColor(R.color.Custom_MainColorBlue)) :
                getResources().getColor(R.color.Custom_MainColorBlue);
        int textColor = intent.hasExtra("text_color") ?
                intent.getIntExtra("text_color", getResources().getColor(R.color.md_theme_primary)) :
                getResources().getColor(R.color.md_theme_primary);

        // إنشاء كائن ProfileData بناءً على البيانات المُجمعة (ديناميكيًا)
        ProfileData profileData = new ProfileData(
                isCurrentUser,
                name,
                imageRes,
                bio,
                contactPhone,
                contactWhatsapp,
                contactFacebook,
                contactEmail,
                contactTelegram,
                headerColor,
                textColor
        );

        // تحديث الواجهة بناءً على بيانات البروفايل
        courseHeaderLayout.setBackgroundColor(profileData.getHeaderColor());
        Utils.setSystemBarColorWithColorInt(this, profileData.getHeaderColor(),
                getResources().getColor(R.color.Custom_BackgroundColor), 0);
        profileName.setText(profileData.getProfileName());

        // تغيير لون النصوص المطلوبة
        TextView sectionContact = findViewById(R.id.sectionContact);
        TextView sectionBio = findViewById(R.id.sectionBio);
        TextView sectionCourses = findViewById(R.id.sectionCourses);

        sectionContact.setTextColor(textColor);
        sectionBio.setTextColor(textColor);
        sectionCourses.setTextColor(textColor);

// تغيير لون الخلفية للبطاقات
        MaterialCardView cardPhone = findViewById(R.id.cardPhone);
        MaterialCardView cardWhatsapp = findViewById(R.id.cardWhatsapp);
        MaterialCardView cardFacebook = findViewById(R.id.cardFacebook);
        MaterialCardView cardEmail = findViewById(R.id.cardEmail);
        MaterialCardView cardTelegram = findViewById(R.id.cardTelegram);
        FloatingActionButton fabBackToTop = findViewById(R.id.fabBackToTop);

        cardPhone.setBackgroundTintList(ColorStateList.valueOf(headerColor));
        cardWhatsapp.setBackgroundTintList(ColorStateList.valueOf(headerColor));
        cardFacebook.setBackgroundTintList(ColorStateList.valueOf(headerColor));
        cardEmail.setBackgroundTintList(ColorStateList.valueOf(headerColor));
        cardTelegram.setBackgroundTintList(ColorStateList.valueOf(headerColor));
        fabBackToTop.setBackgroundTintList(ColorStateList.valueOf(headerColor));


        profileBio.setText(profileData.getProfileBio());
        imgProfile.setImageResource(profileData.getProfileImageRes());

        // عرض زر التعديل إذا كان بروفايل المستخدم الحالي
        if (profileData.isCurrentUser()) {
            editButton.setVisibility(View.VISIBLE);
            editButton.setOnClickListener(v -> {
                // بدء نشاط تعديل البروفايل، يمكن تعديله حسب الحاجة
//                Intent editIntent = new Intent(ProfileActivity.this, EditProfileActivity.class);
//                startActivity(editIntent);
            });
        } else {
            editButton.setVisibility(View.GONE);
        }

        // إعداد روابط التواصل – يتم هنا ربط كل عنصر بنظام الريبل (يُفترض أن يكون تأثير الريبل محدد في ملف XML لكل عنصر)
        View parentPhone = findViewById(R.id.parentPhone);
        View parentWhatsapp = findViewById(R.id.parentWhatsapp);
        View parentFacebook = findViewById(R.id.parentFacebook);
        View parentEmail = findViewById(R.id.parentEmail);
        View parentTelegram = findViewById(R.id.parentTelegram);

        if (!profileData.getContactPhone().isEmpty()) {
            parentPhone.setOnClickListener(v -> {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + profileData.getContactPhone()));
                startActivity(dialIntent);
            });
        } else {
            parentPhone.setClickable(false);
        }

        if (!profileData.getContactWhatsapp().isEmpty()) {
            parentWhatsapp.setOnClickListener(v -> {
                String url = "https://wa.me/" + profileData.getContactWhatsapp();
                Intent whatsappIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(whatsappIntent);
            });
        } else {
            parentWhatsapp.setClickable(false);
        }

        if (!profileData.getContactFacebook().isEmpty()) {
            parentFacebook.setOnClickListener(v -> {
                Intent fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(profileData.getContactFacebook()));
                startActivity(fbIntent);
            });
        } else {
            parentFacebook.setClickable(false);
        }

        if (!profileData.getContactEmail().isEmpty()) {
            parentEmail.setOnClickListener(v -> {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + profileData.getContactEmail()));
                startActivity(emailIntent);
            });
        } else {
            parentEmail.setClickable(false);
        }

        if (!profileData.getContactTelegram().isEmpty()) {
            parentTelegram.setOnClickListener(v -> {
                Intent tgIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/" + profileData.getContactTelegram()));
                startActivity(tgIntent);
            });
        } else {
            parentTelegram.setClickable(false);
        }

        // إعداد RecyclerView للكورسات (يتم إدخال البيانات يدويًا)
        setupRecyclerView();

        // إعداد مستمع تمرير للـ NestedScrollView لإظهار/إخفاء زر العودة إلى الأعلى بتأثير fade
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > 600) {
                    if (fabBackToTop.getVisibility() != View.VISIBLE) {
                        fabBackToTop.setAlpha(0f);
                        fabBackToTop.setVisibility(View.VISIBLE);
                        fabBackToTop.animate()
                                .alpha(1f)
                                .setDuration(300)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .start();
                    }
                } else {
                    if (fabBackToTop.getVisibility() == View.VISIBLE) {
                        fabBackToTop.animate()
                                .alpha(0f)
                                .setDuration(300)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .withEndAction(() -> fabBackToTop.setVisibility(View.GONE))
                                .start();
                    }
                }
            }
        });

        // عند الضغط على زر العودة إلى الأعلى يتم التمرير إلى أعلى الصفحة بشكل خطي
        fabBackToTop.setOnClickListener(v -> {
            ObjectAnimator animator = ObjectAnimator.ofInt(nestedScrollView, "scrollY", nestedScrollView.getScrollY(), 0);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(300);
            animator.start();
        });

        // إعداد زر الرجوع في الهيدر لإنهاء النشاط عند الضغط
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * إعداد RecyclerView مع بيانات الكورسات التجريبية.
     */
    private void setupRecyclerView() {
        courseList = new ArrayList<>();
        courseList.add(new CourseCardData("CEE308",
                getString(R.string.bachelor_in_communications_technology_bact),
                getString(R.string.course_title_3),
                false, false, true, false,
                getResources().getColor(R.color.Custom_MainColorBlue)));
        courseList.add(new CourseCardData("GMA205",
                getString(R.string.bachelor_in_communications_technology_bact),
                getString(R.string.course_title_2),
                false, false, true, false,
                getResources().getColor(R.color.Custom_MainColorTeal)));
        courseList.add(new CourseCardData("BQM304",
                getString(R.string.bachelor_in_communications_technology_bact),
                getString(R.string.course_title_4),
                false, false, false, true,
                getResources().getColor(R.color.Custom_MainColorPurple)));
        courseList.add(new CourseCardData("INT305",
                getString(R.string.bachelor_in_communications_technology_bact),
                getString(R.string.course_title_1),
                true, true, false, false,
                getResources().getColor(R.color.Custom_MainColorGolden)));

        adapter = new CourseCardAdapter(courseList, false);
        int numColumns = calculateNoOfColumns(400);
        coursesRecyclerView.setLayoutManager(new GridLayoutManager(this, numColumns));
        coursesRecyclerView.setAdapter(adapter);
    }

    /**
     * حساب عدد الأعمدة بناءً على عرض العمود المحدد (بالـ dp)
     */
    private int calculateNoOfColumns(int columnWidthDp) {
        float screenWidthDp = getResources().getDisplayMetrics().widthPixels /
                getResources().getDisplayMetrics().density;
        return (int) (screenWidthDp / columnWidthDp + 0.5);
    }
}
