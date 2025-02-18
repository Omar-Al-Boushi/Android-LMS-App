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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;

import org.svuonline.lms.R;
import org.svuonline.lms.data.model.User;
import org.svuonline.lms.data.repository.UserRepository;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.io.IOException;

public class EditProfileActivity extends BaseActivity {

    // Views
    private MaterialButton btnBack, btnSave, btnCancel;
    private TextInputLayout tilPhone, tilWhatsapp, tilFacebook, tilTelegram, tilEmail, tilBioEn, tilBioAr;
    private EditText etPhone, etWhatsapp, etFacebook, etTelegram, etEmail, etBioEn, etBioAr;
    private ShapeableImageView ivProfile;

    // State
    private Uri selectedImageUri;
    private Bitmap originalProfileBitmap;
    private UserRepository userRepo;
    private long currentUserId;
    private User currentUser;
    private boolean isArabic;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Utils.setSystemBarColorWithColorInt(this, getResources().getColor(R.color.Custom_MainColorBlue), getResources().getColor(R.color.Custom_BackgroundColor), 0);

        isArabic = "ar".equals(
                getSharedPreferences("AppPreferences", MODE_PRIVATE)
                        .getString("selected_language", "en")
        );

        // Bind views
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        tilPhone = findViewById(R.id.tilPhone);
        tilWhatsapp = findViewById(R.id.tilWhatsapp);
        tilFacebook = findViewById(R.id.tilFacebook);
        tilTelegram = findViewById(R.id.tilTelegram);
        tilEmail = findViewById(R.id.tilEmail);
        tilBioEn = findViewById(R.id.tilBioEn);
        tilBioAr = findViewById(R.id.tilBioAr);

        etPhone = findViewById(R.id.etPhone);
        etWhatsapp = findViewById(R.id.etWhatsapp);
        etFacebook = findViewById(R.id.etFacebook);
        etTelegram = findViewById(R.id.etTelegram);
        etEmail = findViewById(R.id.etEmail);
        etBioEn = findViewById(R.id.etBioEn);
        etBioAr = findViewById(R.id.etBioAr);

        ivProfile = findViewById(R.id.ivProfile);

        // Repo and user
        userRepo = new UserRepository(this);
        currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getLong("user_id", -1);
        currentUser = userRepo.getUserById(currentUserId);
        if (currentUser == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // حفظ الصورة الأصلية
        originalProfileBitmap = getBitmapFromImageView(ivProfile);

        // تحميل الحقول
        etPhone.setText(stripPrefix(tilPhone, currentUser.getPhone()));
        etWhatsapp.setText(stripPrefix(tilWhatsapp, currentUser.getWhatsappNumber()));
        etFacebook.setText(stripPrefix(tilFacebook, currentUser.getFacebookUrl()));
        etTelegram.setText(stripPrefix(tilTelegram, currentUser.getTelegramHandle()));
        etEmail.setText(stripPrefix(tilEmail, currentUser.getEmail()));

        etBioEn.setText(isArabic ? currentUser.getBioAr() : currentUser.getBioEn());
        etBioAr.setText(isArabic ? currentUser.getBioEn() : currentUser.getBioAr());

        // تحميل صورة البروفايل
        String pic = currentUser.getProfilePicture();
        if (pic != null && pic.startsWith("content://")) {
            ivProfile.setImageURI(Uri.parse(pic));
        } else if (pic != null && pic.startsWith("@drawable/")) {
            String name = pic.substring("@drawable/".length());
            int resId = getResources().getIdentifier(name, "drawable", getPackageName());
            ivProfile.setImageResource(resId != 0 ? resId : R.drawable.avatar);
        } else {
            ivProfile.setImageResource(R.drawable.avatar);
        }

        // إعداد اللانشر لاختيار الصورة
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // منح صلاحية دائمة للوصول للصورة
                            try {
                                getContentResolver().takePersistableUriPermission(
                                        selectedImageUri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }

                            try {
                                ivProfile.setImageBitmap(
                                        MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri)
                                );
                            } catch (IOException e) {
                                Toast.makeText(this, "فشل تحميل الصورة", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        // Listeners
        btnBack.setOnClickListener(v -> finish());
        ivProfile.setOnClickListener(v -> openImagePicker());
        btnCancel.setOnClickListener(v -> {
            Toast.makeText(this, "تم إلغاء التعديلات", Toast.LENGTH_SHORT).show();
            finish();
        });
        btnSave.setOnClickListener(v -> saveProfileChanges());
    }

    private String stripPrefix(TextInputLayout til, String full) {
        if (full == null || full.isEmpty()) {
            return "";
        }

        // الحصول على البادئة من الحقل
        CharSequence prefix = til.getPrefixText();
        String prefixStr = prefix != null ? prefix.toString() : "";

        // قائمة البادئات المحتملة المخزنة في قاعدة البيانات
        String[] possiblePrefixes = new String[] {
                "https://wa.me/", "wa.me/", // لـ WhatsApp
                "https://fb.com/", "facebook.com/", // لـ Facebook
                "https://t.me/", "t.me/", // لـ Telegram
                "+963" // للهاتف
        };

        // إزالة أي بادئة مطابقة من النص
        for (String possiblePrefix : possiblePrefixes) {
            if (full.startsWith(possiblePrefix)) {
                return full.substring(possiblePrefix.length());
            }
        }

        // إذا لم يتم العثور على بادئة، إرجاع النص كما هو
        return full;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }

    private Bitmap getBitmapFromImageView(ShapeableImageView iv) {
        Drawable d = iv.getDrawable();
        if (d instanceof BitmapDrawable) {
            return ((BitmapDrawable) d).getBitmap();
        }
        Bitmap bmp = Bitmap.createBitmap(iv.getWidth(), iv.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        d.setBounds(0, 0, c.getWidth(), c.getHeight());
        d.draw(c);
        return bmp;
    }

    private void saveProfileChanges() {
        // جلب النصوص من الحقول
        String phone = etPhone.getText().toString().trim();
        String whatsapp = etWhatsapp.getText().toString().trim();
        String facebook = etFacebook.getText().toString().trim();
        String telegram = etTelegram.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String bioAr = etBioAr.getText().toString().trim();
        String bioEn = etBioEn.getText().toString().trim();

        // إضافة البادئات فقط إذا لم تكن موجودة
        String phoneToSave = phone.startsWith("+963") ? phone : "+963" + phone;
        String whatsappToSave = whatsapp.startsWith("wa.me/") ? whatsapp : "wa.me/" + whatsapp;
        String facebookToSave = facebook.startsWith("facebook.com/") ? facebook : "facebook.com/" + facebook;
        String telegramToSave = telegram.startsWith("t.me/") ? telegram : "t.me/" + telegram;

        // صورة الملف الشخصي
        String picUri = selectedImageUri != null
                ? selectedImageUri.toString()
                : currentUser.getProfilePicture();

        // تحديث البيانات
        boolean ok = userRepo.updateUser(
                currentUserId,
                phoneToSave, whatsappToSave, facebookToSave, telegramToSave, email,
                isArabic ? bioAr : bioEn, isArabic ? bioEn : bioAr, picUri
        );

        if (ok) {
            Toast.makeText(this, "تم تحديث الملف الشخصي", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "فشل التحديث", Toast.LENGTH_SHORT).show();
        }
    }
}