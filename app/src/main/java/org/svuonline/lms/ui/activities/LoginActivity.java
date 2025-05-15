package org.svuonline.lms.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.svuonline.lms.R;
import org.svuonline.lms.data.repository.UserRepository;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.util.Objects;

/**
 * نشاط تسجيل الدخول مع دعم "تذكرني" والتحقق من البيانات.
 */
public class LoginActivity extends BaseActivity {

    // مفاتيح SharedPreferences
    private static final String PREFS_NAME = "user_prefs";
    private static final int SHAKE_DISTANCE = 10;

    // عناصر واجهة المستخدم
    private TextInputEditText editTextUsername;
    private TextInputEditText editTextPassword;
    private TextInputLayout textInputLayoutUsername;
    private TextInputLayout textInputLayoutPassword;
    private MaterialCheckBox checkboxRememberMe;
    private MaterialButton buttonSignIn;
    private ConstraintLayout parentLayout;

    // المستودعات
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // تهيئة المكونات
        initComponents();

        // تهيئة الواجهة
        initViews();

        // إعداد الإعدادات العامة
        setupPreferences();

        // التحقق من حالة "تذكرني"
        if (checkRememberMe()) {
            return;
        }

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
     * تهيئة عناصر الواجهة
     */
    private void initViews() {
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        textInputLayoutUsername = findViewById(R.id.textInputLayoutUsername);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        checkboxRememberMe = findViewById(R.id.checkbox_remember_me);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        parentLayout = findViewById(R.id.parentLayout);
    }

    /**
     * إعداد الإعدادات العامة (شريط الحالة، التفضيلات)
     */
    private void setupPreferences() {
        Utils.setSystemBarColor(this, R.color.Custom_MainColorBlue, R.color.Custom_MainColorBlue, 0);
        Utils.applyAppPreferences(this);
    }

    /**
     * التحقق من حالة "تذكرني" وإعادة التوجيه إذا لزم الأمر
     * @return صحيح إذا تم إعادة التوجيه
     */
    private boolean checkRememberMe() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = prefs.getBoolean("remember_me", false);
        if (rememberMe) {
            long userId = prefs.getLong("user_id", -1);
            if (userId != -1) {
                navigateToDashboard(userId);
                return true;
            }
        }
        return false;
    }

    /**
     * إعداد مستمعات الأحداث (الإدخال، النقر، اللمس)
     */
    private void setupListeners() {
        // مستمع زر تسجيل الدخول
        buttonSignIn.setOnClickListener(this::onSignInClick);

        // إخفاء رسائل الخطأ عند الكتابة
        editTextUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutUsername.setError(null);
                textInputLayoutUsername.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutPassword.setError(null);
                textInputLayoutPassword.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // مستمع أيقونة الخطأ
        textInputLayoutUsername.setErrorIconOnClickListener(v -> showErrorText(textInputLayoutUsername));
        textInputLayoutPassword.setErrorIconOnClickListener(v -> showErrorText(textInputLayoutPassword));

        // إلغاء تحديد الحقل عند النقر خارج الواجهة
        parentLayout.setOnTouchListener(this::onTouchOutside);
    }

    /**
     * التحقق من صحة اسم المستخدم أو البريد الإلكتروني
     * @return صحيح إذا كان الإدخال صالحًا
     */
    private boolean validateUsername() {
        String username = Objects.requireNonNull(editTextUsername.getText()).toString().trim();
        if (username.isEmpty()) {
            textInputLayoutUsername.setErrorEnabled(true);
            textInputLayoutUsername.setError(getString(R.string.error_username_empty));
            shakeView(textInputLayoutUsername);
            return false;
        }
        if (!username.contains("@") && !username.matches("^[a-zA-Z0-9_]{3,50}$")) {
            textInputLayoutUsername.setErrorEnabled(true);
            textInputLayoutUsername.setError(getString(R.string.error_username_invalid));
            shakeView(textInputLayoutUsername);
            return false;
        }
        if (username.contains("@") && !android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            textInputLayoutUsername.setErrorEnabled(true);
            textInputLayoutUsername.setError(getString(R.string.error_username_invalid));
            shakeView(textInputLayoutUsername);
            return false;
        }
        textInputLayoutUsername.setError(null);
        textInputLayoutUsername.setErrorEnabled(false);
        return true;
    }

    /**
     * التحقق من صحة كلمة المرور
     * @return صحيح إذا كانت كلمة المرور صالحة
     */
    private boolean validatePassword() {
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();
        if (password.isEmpty()) {
            textInputLayoutPassword.setErrorEnabled(true);
            textInputLayoutPassword.setError(getString(R.string.error_password_empty));
            shakeView(textInputLayoutPassword);
            return false;
        }
        textInputLayoutPassword.setError(null);
        textInputLayoutPassword.setErrorEnabled(false);
        return true;
    }

    /**
     * تحريك العنصر عند حدوث خطأ
     * @param view العنصر المراد تحريكه
     */
    private void shakeView(View view) {
        view.animate()
                .translationX(SHAKE_DISTANCE)
                .setDuration(50)
                .withEndAction(() -> view.animate()
                        .translationX(-SHAKE_DISTANCE)
                        .setDuration(50)
                        .withEndAction(() -> view.animate()
                                .translationX(0)
                                .setDuration(50)
                                .start())
                        .start())
                .start();
    }

    /**
     * إخفاء لوحة المفاتيح وإلغاء التحديد
     */
    private void clearFocus() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    /**
     * معالجة النقر خارج الحقول
     * @param view العنصر
     * @param event الحدث
     * @return صحيح إذا تم المعالجة
     */
    private boolean onTouchOutside(@NonNull View view, @NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            clearFocus();
        }
        return true;
    }

    /**
     * معالجة النقر على زر تسجيل الدخول
     * @param view زر تسجيل الدخول
     */
    private void onSignInClick(View view) {
        boolean isUsernameValid = validateUsername();
        boolean isPasswordValid = validatePassword();

        if (isUsernameValid && isPasswordValid) {
            String emailOrUsername = Objects.requireNonNull(editTextUsername.getText()).toString().trim().toLowerCase();
            String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();

            String passwordHash = Utils.hashPassword(password);
            if (passwordHash == null) {
                showToast(R.string.error_password_hash_failed);
                return;
            }

            long userId = userRepository.loginUser(emailOrUsername, passwordHash);
            if (userId != -1) {
                saveUserPreferences(userId, emailOrUsername);
                showToast(R.string.sign_in_success);
                navigateToDashboard(userId);
            } else {
                textInputLayoutUsername.setErrorEnabled(true);
                textInputLayoutUsername.setError(getString(R.string.error_invalid_credentials));
                textInputLayoutPassword.setErrorEnabled(true);
                textInputLayoutPassword.setError(getString(R.string.error_invalid_credentials));
                shakeView(textInputLayoutUsername);
                shakeView(textInputLayoutPassword);
                showToast(R.string.sign_in_failed);
            }
        } else {
            showToast(R.string.sign_in_failed);
        }
    }

    /**
     * حفظ تفضيلات المستخدم
     * @param userId معرف المستخدم
     * @param emailOrUsername اسم المستخدم أو البريد الإلكتروني
     */
    private void saveUserPreferences(long userId, String emailOrUsername) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("remember_me", checkboxRememberMe.isChecked());
        editor.putString("email", emailOrUsername.contains("@") ? emailOrUsername : emailOrUsername + "@svuonline.org");
        editor.putLong("user_id", userId);
        editor.apply();
    }

    /**
     * الانتقال إلى DashboardActivity
     * @param userId معرف المستخدم
     */
    private void navigateToDashboard(long userId) {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
        finishAffinity();
    }

    /**
     * عرض رسالة الخطأ
     * @param textInputLayout حقل الإدخال
     */
    private void showErrorText(TextInputLayout textInputLayout) {
        String errorText = textInputLayout.getError() != null ? textInputLayout.getError().toString() : getString(R.string.unknown_error);
        showToast(errorText);
    }

    /**
     * عرض رسالة Toast
     * @param messageRes معرف الرسالة
     */
    private void showToast(int messageRes) {
        if (getApplicationContext() != null) {
            android.widget.Toast.makeText(getApplicationContext(), messageRes, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * عرض رسالة Toast بنص مخصص
     * @param message النص
     */
    private void showToast(String message) {
        if (getApplicationContext() != null) {
            android.widget.Toast.makeText(getApplicationContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}