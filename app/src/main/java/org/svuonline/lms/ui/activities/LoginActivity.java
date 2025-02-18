package org.svuonline.lms.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import org.svuonline.lms.data.repository.UserRepository;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.util.Objects;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";
    private TextInputEditText editTextUsername, editTextPassword;
    private TextInputLayout textInputLayoutUsername, textInputLayoutPassword;
    private MaterialCheckBox checkboxRememberMe;
    private UserRepository userRepository;
    private static final int SHAKE_DISTANCE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // تهيئة قاعدة البيانات
        userRepository = new UserRepository(this);
        userRepository.logAllUsers();
        // تغيير لون شريط الحالة وشريط التنقل
        Utils.setSystemBarColor(this, R.color.Custom_MainColorBlue, R.color.Custom_MainColorBlue, 0);
        Utils.applyAppPreferences(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        textInputLayoutUsername = findViewById(R.id.textInputLayoutUsername);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        checkboxRememberMe = findViewById(R.id.checkbox_remember_me);
        MaterialButton buttonSignIn = findViewById(R.id.buttonSignIn);

        // إلغاء تحديد الحقل عند النقر في مكان آخر
        findViewById(R.id.parentLayout).setOnTouchListener(this::onTouchOutside);

        // التحقق من حالة "تذكرني"
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean rememberMe = prefs.getBoolean("remember_me", false);
        if (rememberMe) {
            long userId = prefs.getLong("user_id", -1);
            if (userId != -1) {
                Log.d(TAG, "Remember me enabled, redirecting to Dashboard with userId: " + userId);
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                finishAffinity();
                return;
            }
        }

        // مستمع لزر تسجيل الدخول
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

        // مستمع لأيقونة الخطأ
        textInputLayoutUsername.setErrorIconOnClickListener(v -> showErrorText(textInputLayoutUsername));
        textInputLayoutPassword.setErrorIconOnClickListener(v -> showErrorText(textInputLayoutPassword));
    }

    private boolean validateUsername() {
        String username = Objects.requireNonNull(editTextUsername.getText()).toString().trim();
        Log.d(TAG, "Validating username: " + username);
        if (username.isEmpty()) {
            textInputLayoutUsername.setErrorEnabled(true);
            textInputLayoutUsername.setError(getString(R.string.error_username_empty));
            shakeView(textInputLayoutUsername);
            Log.d(TAG, "Username validation failed: Empty");
            return false;
        }
        // السماح بإدخال اسم المستخدم (مثل Omar_195450) أو بريد إلكتروني كامل
        if (!username.contains("@") && !username.matches("^[a-zA-Z0-9_]{3,50}$")) {
            textInputLayoutUsername.setErrorEnabled(true);
            textInputLayoutUsername.setError(getString(R.string.error_username_invalid));
            shakeView(textInputLayoutUsername);
            Log.d(TAG, "Username validation failed: Invalid format (no @)");
            return false;
        }
        if (username.contains("@") && !android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            textInputLayoutUsername.setErrorEnabled(true);
            textInputLayoutUsername.setError(getString(R.string.error_username_invalid));
            shakeView(textInputLayoutUsername);
            Log.d(TAG, "Username validation failed: Invalid email format");
            return false;
        }
        textInputLayoutUsername.setError(null);
        textInputLayoutUsername.setErrorEnabled(false);
        Log.d(TAG, "Username validation passed");
        return true;
    }

    private boolean validatePassword() {
        String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();
        Log.d(TAG, "Validating password (length): " + password.length());
        if (password.isEmpty()) {
            textInputLayoutPassword.setErrorEnabled(true);
            textInputLayoutPassword.setError(getString(R.string.error_password_empty));
            shakeView(textInputLayoutPassword);
            Log.d(TAG, "Password validation failed: Empty");
            return false;
        }
        textInputLayoutPassword.setError(null);
        textInputLayoutPassword.setErrorEnabled(false);
        Log.d(TAG, "Password validation passed");
        return true;
    }

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

    private boolean onTouchOutside(@NonNull View view, @NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            clearFocus();
        }
        return true;
    }

    private void onSignInClick(View view) {
        boolean isUsernameValid = validateUsername();
        boolean isPasswordValid = validatePassword();

        Log.d(TAG, "Sign in clicked, usernameValid: " + isUsernameValid + ", passwordValid: " + isPasswordValid);

        if (isUsernameValid && isPasswordValid) {
            String emailOrUsername = Objects.requireNonNull(editTextUsername.getText()).toString().trim().toLowerCase();
            String password = Objects.requireNonNull(editTextPassword.getText()).toString().trim();
            Log.d(TAG, "Attempting login with email/username: " + emailOrUsername);
            Log.d(TAG, "Password (length): " + password.length());

            String passwordHash = Utils.hashPassword(password);
            if (passwordHash == null) {
                Log.e(TAG, "Password hashing failed");
                Snackbar.make(view, R.string.error_password_hash_failed, Snackbar.LENGTH_SHORT).show();
                return;
            }

            long userId = userRepository.loginUser(emailOrUsername, passwordHash);
            if (userId != -1) {
                // تسجيل الدخول ناجح
                Log.d(TAG, "Login successful, saving preferences for userId: " + userId);
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("remember_me", checkboxRememberMe.isChecked());
                editor.putString("email", emailOrUsername.contains("@") ? emailOrUsername : emailOrUsername + "@svuonline.org");
                editor.putLong("user_id", userId);
                editor.apply();

                Toast.makeText(this, getString(R.string.sign_in_success), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                finishAffinity();
            } else {
                // فشل تسجيل الدخول
                Log.d(TAG, "Login failed: Invalid credentials or user not found");
                textInputLayoutUsername.setErrorEnabled(true);
                textInputLayoutUsername.setError(getString(R.string.error_invalid_credentials));
                textInputLayoutPassword.setErrorEnabled(true);
                textInputLayoutPassword.setError(getString(R.string.error_invalid_credentials));
                shakeView(textInputLayoutUsername);
                shakeView(textInputLayoutPassword);
                Snackbar.make(view, R.string.sign_in_failed, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "Login failed: Validation errors");
            Snackbar.make(view, R.string.sign_in_failed, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showErrorText(TextInputLayout textInputLayout) {
        String errorText = textInputLayout.getError() != null ? textInputLayout.getError().toString() : "Unknown error";
        Log.d(TAG, "Showing error: " + errorText);
        Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show();
    }
}