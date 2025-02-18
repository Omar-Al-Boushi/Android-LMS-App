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
        setContentView(R.layout.activity_profile);  // تأكد من تطابق اسم ملف XML

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

        // قراءة البيانات المُرسلة عبر الـ Intent
        Intent intent = getIntent();
        boolean isCurrentUser = intent.getBooleanExtra("is_current_user", false);

        // اسم البروفايل
        String name = intent.getStringExtra("profile_name");
        if (name == null || name.isEmpty()) {
            name = "Default Name";
        }

        // تحديث الصورة: التحقق من وجود URI لصورة البروفايل الجديدة
        String imageUriString = intent.getStringExtra("profile_image_uri");
        if (imageUriString != null && !imageUriString.isEmpty()) {
            imgProfile.setImageURI(Uri.parse(imageUriString));
        } else {
            int imageRes = intent.hasExtra("profile_image_res") ?
                    intent.getIntExtra("profile_image_res", R.drawable.ic_launcher_background) :
                    R.drawable.ic_launcher_background;
            imgProfile.setImageResource(imageRes);
        }

        // البايو (الوصف)
        String bio = intent.getStringExtra("profile_bio");
        if (bio == null || bio.isEmpty()) {
            bio = "Default bio text. This is a description about the user.";
        }

        // قراءة بيانات الاتصال
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

        // قراءة ألوان الهيدر والنص
        int headerColor = intent.hasExtra("header_color") ?
                intent.getIntExtra("header_color", getResources().getColor(R.color.Custom_MainColorBlue)) :
                getResources().getColor(R.color.Custom_MainColorBlue);
        int textColor = intent.hasExtra("text_color") ?
                intent.getIntExtra("text_color", getResources().getColor(R.color.md_theme_primary)) :
                getResources().getColor(R.color.md_theme_primary);

        // إنشاء كائن ProfileData باستخدام البيانات المُجمعة
        ProfileData profileData = new ProfileData(
                isCurrentUser,
                name,
                intent.hasExtra("profile_image_res") ? intent.getIntExtra("profile_image_res", R.drawable.ic_launcher_background) : R.drawable.ic_launcher_background,
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
        profileBio.setText(profileData.getProfileBio());

        // تغيير لون النصوص للأقسام
        TextView sectionContact = findViewById(R.id.sectionContact);
        TextView sectionBio = findViewById(R.id.sectionBio);
        TextView sectionCourses = findViewById(R.id.sectionCourses);

        sectionContact.setTextColor(textColor);
        sectionBio.setTextColor(textColor);
        sectionCourses.setTextColor(textColor);

        // تغيير لون الخلفية لبطاقات البيانات وزر العودة إلى الأعلى
        MaterialCardView cardPhone = findViewById(R.id.cardPhone);
        MaterialCardView cardWhatsapp = findViewById(R.id.cardWhatsapp);
        MaterialCardView cardFacebook = findViewById(R.id.cardFacebook);
        MaterialCardView cardEmail = findViewById(R.id.cardEmail);
        MaterialCardView cardTelegram = findViewById(R.id.cardTelegram);

        cardPhone.setBackgroundTintList(ColorStateList.valueOf(headerColor));
        cardWhatsapp.setBackgroundTintList(ColorStateList.valueOf(headerColor));
        cardFacebook.setBackgroundTintList(ColorStateList.valueOf(headerColor));
        cardEmail.setBackgroundTintList(ColorStateList.valueOf(headerColor));
        cardTelegram.setBackgroundTintList(ColorStateList.valueOf(headerColor));
        fabBackToTop.setBackgroundTintList(ColorStateList.valueOf(headerColor));

        // عرض زر التعديل إذا كان بروفايل المستخدم الحالي
        if (profileData.isCurrentUser()) {
            editButton.setVisibility(View.VISIBLE);
            editButton.setOnClickListener(v -> {
                Intent editIntent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(editIntent);
            });
        } else {
            editButton.setVisibility(View.GONE);
        }

        // إعداد روابط وسائل الاتصال
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
                Intent fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=https://facebook.com/" + profileData.getContactFacebook()));
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

        // إعداد RecyclerView لعرض بيانات الكورسات التجريبية
        setupRecyclerView();

        // مستمع للتمرير لإظهار/إخفاء زر العودة إلى الأعلى
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

        // عند الضغط على زر العودة إلى الأعلى يتم التمرير إلى أعلى الصفحة
        fabBackToTop.setOnClickListener(v -> {
            ObjectAnimator animator = ObjectAnimator.ofInt(nestedScrollView, "scrollY", nestedScrollView.getScrollY(), 0);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(300);
            animator.start();
        });

        // عند الضغط على زر الرجوع في الهيدر، العودة إلى DashboardActivity
        backButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(ProfileActivity.this, DashboardActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
            finish();
        });
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
     * حساب عدد الأعمدة بناءً على عرض العمود المحدد (dp)
     */
    private int calculateNoOfColumns(int columnWidthDp) {
        float screenWidthDp = getResources().getDisplayMetrics().widthPixels /
                getResources().getDisplayMetrics().density;
        return (int) (screenWidthDp / columnWidthDp + 0.5);
    }
}
