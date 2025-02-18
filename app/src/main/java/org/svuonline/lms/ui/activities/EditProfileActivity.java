package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;

import org.svuonline.lms.R;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.io.IOException;

public class EditProfileActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    // تعريف العناصر باستخدام مسميات معبرة
    private MaterialButton btnBack;
    private FloatingActionButton fabBackToTop; // إن كانت مستخدمة في التخطيط
    private MaterialButton btnSave, btnCancel;
    private TextInputLayout tilPhone, tilWhatsapp, tilFacebook, tilTelegram, tilEmail, tilBio;
    private EditText etPhone, etWhatsapp, etFacebook, etTelegram, etEmail, etBio;
    private ShapeableImageView ivProfile;

    // بيانات مؤقتة لتخزين الحالة الأولية للصورة
    private Uri selectedImageUri = null;
    private Bitmap originalProfileBitmap = null; // تخزين الصورة الأصلية

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        // يمكنك ضبط لون الشريط هنا إن رغبت أو تركه بدون تغيير
        Utils.setSystemBarColorWithColorInt(this,getResources().getColor(R.color.Custom_MainColorBlue),
                getResources().getColor(R.color.Custom_BackgroundColor), 0);

        // تهيئة العناصر
        btnBack = findViewById(R.id.btnBack);
        ivProfile = findViewById(R.id.ivProfile);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        tilPhone = findViewById(R.id.tilPhone);
        tilWhatsapp = findViewById(R.id.tilWhatsapp);
        tilFacebook = findViewById(R.id.tilFacebook);
        tilTelegram = findViewById(R.id.tilTelegram);
        tilEmail = findViewById(R.id.tilEmail);
        tilBio = findViewById(R.id.tilBio);

        etPhone = findViewById(R.id.etPhone);
        etWhatsapp = findViewById(R.id.etWhatsapp);
        etFacebook = findViewById(R.id.etFacebook);
        etTelegram = findViewById(R.id.etTelegram);
        etEmail = findViewById(R.id.etEmail);
        etBio = findViewById(R.id.etBio);

        // تحميل بيانات تجريبية
        loadTestData();

        // الضغط على زر الرجوع => اغلاق النشاط
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // عند النقر على الصورة يتم فتح مستعرض الصور لاختيار صورة جديدة
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
            }
        });

        // زر حفظ التغييرات
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfileChanges();
            }
        });

        // زر الغاء التغييرات
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelChanges();
            }
        });
    }

    /**
     * تحميل بيانات تجريبية لحقول الإدخال
     */
    private void loadTestData() {
        etPhone.setText("956200828");
        etWhatsapp.setText("+963956200828");
        etFacebook.setText("Omar.Al.Boushi1");
        etTelegram.setText("OmarAlBoushi");
        etEmail.setText("test@example.com");
        etBio.setText("This is a sample bio.");
        // ضبط صورة الملف الشخصي الافتراضية
        ivProfile.setImageResource(R.drawable.omar_photo);
    }

    /**
     * فتح مستعرض الصور لاختيار صورة من معرض الجهاز
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } else {
            Toast.makeText(this, "No app found to pick an image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    // حفظ الصورة الحالية كأصلية إذا لم تكن محفوظة سابقاً
                    if (originalProfileBitmap == null) {
                        originalProfileBitmap = getBitmapFromImageView(ivProfile);
                    }
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    ivProfile.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * دالة لتحويل الصورة المعروضة في ImageView إلى Bitmap
     */
    private Bitmap getBitmapFromImageView(ShapeableImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

    /**
     * حفظ التغييرات وتطبيقها على ProfileActivity
     */
    private void saveProfileChanges() {
        // جمع بيانات الحقول
        String phone = etPhone.getText().toString().trim();
        String whatsapp = etWhatsapp.getText().toString().trim();
        String facebook = etFacebook.getText().toString().trim();
        String telegram = etTelegram.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        // يمكنك عمل التحقق من صحة البيانات هنا

        // إنشاء Intent لنقل البيانات إلى ProfileActivity
        Intent profileIntent = new Intent(EditProfileActivity.this, ProfileActivity.class);

        // تمرير البيانات للتعديل في نشاط البروفايل
        // إذا تم اختيار صورة جديدة، يتم تمرير URI الصورة كنص
        if (selectedImageUri != null) {
            profileIntent.putExtra("profile_image_uri", selectedImageUri.toString());
        } else {
            // يمكن تمرير قيمة فارغة أو قيمة افتراضية
            profileIntent.putExtra("profile_image_uri", "");
        }
        // تمرير البيانات الأخرى
        profileIntent.putExtra("contact_phone", phone);
        profileIntent.putExtra("contact_whatsapp", whatsapp);
        profileIntent.putExtra("contact_facebook", facebook);
        profileIntent.putExtra("contact_telegram", telegram);
        profileIntent.putExtra("contact_email", email);
        profileIntent.putExtra("profile_bio", bio);
        // إذا كان لديك بيانات أخرى مثل اسم البروفايل أو الألوان، يمكنك تمريرها
        profileIntent.putExtra("profile_name", "Omar Al boushi"); // مثال للاسم
        profileIntent.putExtra("header_color", getResources().getColor(R.color.Custom_MainColorBlue));
        profileIntent.putExtra("text_color", getResources().getColor(R.color.md_theme_primary));
        profileIntent.putExtra("is_current_user", true);

        Toast.makeText(this, "Profile changes saved", Toast.LENGTH_SHORT).show();

        startActivity(profileIntent);
        finish();
    }

    /**
     * في حالة الضغط على زر الغاء التغييرات يعاد استعادة الصورة الأصلية وباقي البيانات
     */
    private void cancelChanges() {
        if (originalProfileBitmap != null) {
            ivProfile.setImageBitmap(originalProfileBitmap);
        }
        Toast.makeText(this, "Changes canceled", Toast.LENGTH_SHORT).show();
        finish();
    }
}
