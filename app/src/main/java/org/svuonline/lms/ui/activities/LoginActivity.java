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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.svuonline.lms.R;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.util.Objects;

public class LoginActivity extends BaseActivity {

    private TextInputEditText editTextUsername, editTextPassword;
    private TextInputLayout textInputLayoutUsername, textInputLayoutPassword;
    private MaterialCheckBox checkboxRememberMe;
    private static final int SHAKE_DISTANCE = 10; // Default shake distance in pixels

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // تغيير لون شريط الحالة وشريط التنقل باستخدام الدالة المساعدة
        Utils.setSystemBarColor(this, R.color.Custom_MainColorBlue, R.color.Custom_MainColorBlue, 0);
        Utils.applyAppPreferences(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        textInputLayoutUsername = findViewById(R.id.textInputLayoutUsername);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        checkboxRememberMe = findViewById(R.id.checkbox_remember_me);
        MaterialButton buttonSignIn = findViewById(R.id.buttonSignIn);


        // إلغاء تحديد الحقل عند النقر في مكان آخر على الشاشة
        findViewById(R.id.parentLayout).setOnTouchListener(this::onTouchOutside);

        // تحقق من حالة "تذكرني"
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean rememberMe = prefs.getBoolean("remember_me", false);
        if (rememberMe) {
            // إذا كان تذكرني مفعلًا، يتم تخطي شاشة تسجيل الدخول والانتقال إلى DashboardActivity
            String username = prefs.getString("username", "");  // استرجاع اسم المستخدم إذا كنت بحاجة إليه
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            intent.putExtra("username", username); // يمكنك تمرير اسم المستخدم إذا لزم الأمر
            startActivity(intent);
            finish(); // إنهاء النشاط الحالي لمنع العودة إليه
            return; // التوقف عن تنفيذ الكود بعد الانتقال
        }

        // مستمع لزر تسجيل الدخول
        buttonSignIn.setOnClickListener(this::onSignInClick);

        // إخفاء رسائل الخطأ عند الكتابة من جديد
        editTextUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutUsername.setError(null);
                textInputLayoutUsername.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayoutPassword.setError(null);
                textInputLayoutPassword.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // إضافة مستمع للنقر على أيقونة الخطأ
        textInputLayoutUsername.setErrorIconOnClickListener(v -> showErrorText(textInputLayoutUsername));
        textInputLayoutPassword.setErrorIconOnClickListener(v -> showErrorText(textInputLayoutPassword));
    }

    // دالة للتحقق من صحة اسم المستخدم
    private boolean validateUsername() {
        String username = Objects.requireNonNull(editTextUsername.getText()).toString().trim();
        if (username.isEmpty()) {
            textInputLayoutUsername.setErrorEnabled(true);
            textInputLayoutUsername.setError(getString(R.string.error_username_empty));
            shakeView(textInputLayoutUsername);
            return false;
        } else if (!username.matches("^[a-zA-Z0-9_]{3,15}$")) {
            textInputLayoutUsername.setErrorEnabled(true);
            textInputLayoutUsername.setError(getString(R.string.error_username_invalid));
            shakeView(textInputLayoutUsername);
            return false;
        } else {
            textInputLayoutUsername.setError(null);
            textInputLayoutUsername.setErrorEnabled(false);
            return true;
        }
    }

    // دالة للتحقق من صحة كلمة المرور
    private boolean validatePassword() {
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();
        if (password.isEmpty()) {
            textInputLayoutPassword.setErrorEnabled(true);
            textInputLayoutPassword.setError(getString(R.string.error_password_empty));
            shakeView(textInputLayoutPassword);
            return false;
        } else {
            textInputLayoutPassword.setError(null);
            textInputLayoutPassword.setErrorEnabled(false);
            return true;
        }
    }

    // دالة لتطبيق تأثير اهتزاز على الحقل عند وجود خطأ
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

    // دالة لإلغاء تحديد الحقل وإخفاء لوحة المفاتيح
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

    // دالة لمعالجة النقر خارج حقول الإدخال
    private boolean onTouchOutside(@NonNull View view, @NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            clearFocus();
        }
        return true; // Consume the touch event
    }

    // دالة لاستجابة زر تسجيل الدخول
    private void onSignInClick(View view) {
        boolean isUsernameValid = validateUsername();
        boolean isPasswordValid = validatePassword();

        if (isUsernameValid && isPasswordValid) {
            // حفظ حالة "تذكرني" في SharedPreferences
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("remember_me", checkboxRememberMe.isChecked());
            editor.putString("username", Objects.requireNonNull(editTextUsername.getText()).toString());
            editor.apply();

            Toast toast = Toast.makeText(this, getString(R.string.sign_in_success), Toast.LENGTH_SHORT);
            toast.show();
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish(); // إنهاء هذه الصفحة لتجنب العودة إليها
        } else {
            // عرض رسالة الخطأ
            Snackbar.make(view, R.string.sign_in_failed, Snackbar.LENGTH_SHORT).show();
        }
    }

    // دالة لعرض نص الخطأ
    private void showErrorText(TextInputLayout textInputLayout) {
        String errorText = textInputLayout.getError() != null ? textInputLayout.getError().toString() : "Unknown error";
        Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show();
    }
}
