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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;

import org.svuonline.lms.R;
import org.svuonline.lms.data.model.User;
import org.svuonline.lms.data.repository.UserRepository;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.io.IOException;

/**
 * نشاط لتعديل بيانات الملف الشخصي للمستخدم.
 */
public class EditProfileActivity extends BaseActivity {

    // عناصر واجهة المستخدم
    private MaterialButton btnBack;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private TextInputLayout tilPhone;
    private TextInputLayout tilWhatsapp;
    private TextInputLayout tilFacebook;
    private TextInputLayout tilTelegram;
    private TextInputLayout tilEmail;
    private TextInputLayout tilBioEn;
    private TextInputLayout tilBioAr;
    private EditText etPhone;
    private EditText etWhatsapp;
    private EditText etFacebook;
    private EditText etTelegram;
    private EditText etEmail;
    private EditText etBioEn;
    private EditText etBioAr;
    private ShapeableImageView ivProfile;

    // المستودعات
    private UserRepository userRepository;

    // بيانات النشاط
    private long currentUserId;
    private User currentUser;
    private boolean isArabic;
    private Uri selectedImageUri;
    private Bitmap originalProfileBitmap;

    // مكونات إضافية
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_edit_profile);

        // تهيئة المكونات
        initComponents();

        // تهيئة الواجهة
        initViews();

        // التحقق من بيانات المستخدم
        if (!validateUserData()) {
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
     * دالة لتطبيق المساحات الداخلية (Insets) بشكل برمجي على الواجهة الجذرية.
     */
    /**
     * دالة لتطبيق المساحات الداخلية (Insets) بشكل برمجي.
     * هذا هو الإصدار الصحيح الذي يستهدف الترويسة والمحتوى القابل للتمرير.
     */
    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clMain), (v, insets) -> {
            // الحصول على أبعاد شريط الحالة (من الأعلى) وشريط التنقل (من الأسفل)
            int systemBarsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // 1. تطبيق الـ padding العلوي على ترويسة الصفحة (clHeader)
            // هذا يضمن عدم تداخل زر الرجوع مع أيقونات شريط الحالة.
            findViewById(R.id.clHeader).setPadding(0, systemBarsTop, 0, 0);

            // 2. تطبيق الـ padding السفلي على المحتوى القابل للتمرير (nsvMain)
            // هذا يضمن عدم تداخل أزرار الحفظ والإلغاء مع شريط التنقل.
            findViewById(R.id.nsvMain).setPadding(0, 0, 0, systemBarsBottom);

            return WindowInsetsCompat.CONSUMED;
        });
    }
    /**
     * تهيئة المستودعات
     */
    private void initComponents() {
        userRepository = new UserRepository(this);
    }

    /**
     * تهيئة عناصر الواجهة
     */
    private void initViews() {
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
    }

    /**
     * التحقق من صحة بيانات المستخدم
     * @return صحيح إذا كانت البيانات صالحة، خطأ إذا لزم إنهاء النشاط
     */
    private boolean validateUserData() {
        // جلب اللغة
        isArabic = "ar".equals(
                getSharedPreferences("AppPreferences", MODE_PRIVATE)
                        .getString("selected_language", "en")
        );

        // جلب userId
        currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getLong("user_id", -1);
        if (currentUserId == -1) {
            showToast(R.string.user_id_not_found);
            return false;
        }

        // جلب المستخدم
        currentUser = userRepository.getUserById(currentUserId);
        if (currentUser == null) {
            showToast(R.string.user_id_not_found);
            return false;
        }

        return true;
    }

    /**
     * تهيئة البيانات (إعداد الواجهة، تحميل الحقول، إعداد اللانشر)
     */
    private void initData() {
        // إعداد لون شريط النظام
        Utils.setSystemBarColorWithColorInt(this,
                getResources().getColor(R.color.Custom_MainColorBlue),
                getResources().getColor(R.color.Custom_BackgroundColor), 0);

        // حفظ الصورة الأصلية
        originalProfileBitmap = getBitmapFromImageView(ivProfile);

        // تحميل الحقول
        setupFields();

        // تحميل صورة الملف الشخصي
        setupProfilePicture();

        // إعداد اللانشر لاختيار الصورة
        setupImagePicker();
    }

    /**
     * إعداد مستمعات الأحداث (الأزرار، الصورة)
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> {
            showToast(R.string.cancel_edit);
            finish();
        });
        btnSave.setOnClickListener(v -> saveProfileChanges());
        ivProfile.setOnClickListener(v -> openImagePicker());
    }

    /**
     * تحميل حقول الإدخال
     */
    private void setupFields() {
        etPhone.setText(stripPrefix(tilPhone, currentUser.getPhone()));
        etWhatsapp.setText(stripPrefix(tilWhatsapp, currentUser.getWhatsappNumber()));
        etFacebook.setText(stripPrefix(tilFacebook, currentUser.getFacebookUrl()));
        etTelegram.setText(stripPrefix(tilTelegram, currentUser.getTelegramHandle()));
        etEmail.setText(stripPrefix(tilEmail, currentUser.getEmail()));
        etBioEn.setText(currentUser.getBioEn());
        etBioAr.setText(currentUser.getBioAr());
    }

    /**
     * تحميل صورة الملف الشخصي
     */
    private void setupProfilePicture() {
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
    }

    /**
     * إعداد اللانشر لاختيار الصورة
     */
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                getContentResolver().takePersistableUriPermission(
                                        selectedImageUri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                                ivProfile.setImageBitmap(
                                        MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri)
                                );
                            } catch (SecurityException e) {
                                showToast(R.string.image_permission_error);
                            } catch (IOException e) {
                                showToast(R.string.image_load_failed);
                            }
                        }
                    }
                }
        );
    }

    /**
     * فتح نافذة اختيار الصورة
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }

    /**
     * إزالة البادئة من النص
     * @param til حقل الإدخال
     * @param full النص الكامل
     * @return النص بعد إزالة البادئة
     */
    private String stripPrefix(TextInputLayout til, String full) {
        if (full == null || full.isEmpty()) {
            return "";
        }
        String[] possiblePrefixes = {
                "https://wa.me/", "wa.me/",
                "https://fb.com/", "facebook.com/",
                "https://t.me/", "t.me/",
                "+963"
        };
        for (String prefix : possiblePrefixes) {
            if (full.startsWith(prefix)) {
                return full.substring(prefix.length());
            }
        }
        return full;
    }

    /**
     * جلب الصورة كـ Bitmap من ImageView
     * @param iv عنصر الصورة
     * @return الصورة كـ Bitmap
     */
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

    /**
     * حفظ التغييرات على الملف الشخصي
     */
    private void saveProfileChanges() {
        String phone = etPhone.getText().toString().trim();
        String whatsapp = etWhatsapp.getText().toString().trim();
        String facebook = etFacebook.getText().toString().trim();
        String telegram = etTelegram.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String bioAr = etBioAr.getText().toString().trim();
        String bioEn = etBioEn.getText().toString().trim();

        String phoneToSave = phone.isEmpty() ? "" : phone.startsWith("+963") ? phone : "+963" + phone;
        String whatsappToSave = whatsapp.isEmpty() ? "" : whatsapp.startsWith("wa.me/") ? whatsapp : "wa.me/" + whatsapp;
        String facebookToSave = facebook.isEmpty() ? "" : facebook.startsWith("facebook.com/") ? facebook : "facebook.com/" + facebook;
        String telegramToSave = telegram.isEmpty() ? "" : telegram.startsWith("t.me/") ? telegram : "t.me/" + telegram;

        String picUri = selectedImageUri != null ? selectedImageUri.toString() : currentUser.getProfilePicture();

        boolean success = userRepository.updateUser(
                currentUserId,
                phoneToSave, whatsappToSave, facebookToSave, telegramToSave, email,
                isArabic ? bioAr : bioEn, isArabic ? bioEn : bioAr, picUri
        );

        if (success) {
            showToast(R.string.profile_updated);
            setResult(RESULT_OK);
            finish();
        } else {
            showToast(R.string.update_failed);
        }
    }

    /**
     * عرض رسالة Toast
     * @param messageRes معرف الرسالة
     */
    private void showToast(int messageRes) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
    }
}