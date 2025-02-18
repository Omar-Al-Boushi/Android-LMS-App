package org.svuonline.lms.ui.activities;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import org.svuonline.lms.R;
import org.svuonline.lms.ui.adapters.ParticipantsAdapter;
import org.svuonline.lms.ui.data.ParticipantData;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ParticipantsActivity extends BaseActivity {
    private MaterialTextView courseCodeTextView;
    private MaterialTextView courseTitleTextView;
    private ConstraintLayout courseHeaderLayout;
    private TextView sectionTitle;
    private MaterialButton backButton;
    private RecyclerView recyclerView;
    private int courseColor;
    private TextInputEditText searchBar;
    TextInputLayout textInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants);

        RecyclerView recyclerView = findViewById(R.id.filesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchBar = findViewById(R.id.search_bar);
        textInputLayout = findViewById(R.id.outlinedTextField);

        // استقبال البيانات من الـ Intent
        Intent intent = getIntent();
        String buttonId = intent.getStringExtra("button_id");
        String courseCode = intent.getStringExtra("course_code");
        String courseTitle = intent.getStringExtra("course_title");
        int courseColorValue = intent.getIntExtra("course_color_value", -1);
        String buttonLabel = intent.getStringExtra("button_label");

        // طباعة البيانات للتأكد
        Log.d("FilesActivity", "buttonId: " + buttonId);
        Log.d("FilesActivity", "courseCode: " + courseCode);
        Log.d("FilesActivity", "courseTitle: " + courseTitle);
        Log.d("FilesActivity", "courseColorValue: " + courseColorValue);
        Log.d("FilesActivity", "buttonLabel: " + buttonLabel);

        // ربط العناصر من الـ layout
        courseCodeTextView = findViewById(R.id.courseCodeTextView);
        courseTitleTextView = findViewById(R.id.courseTitleTextView);
        courseHeaderLayout = findViewById(R.id.courseHeaderLayout);
        sectionTitle = findViewById(R.id.sectionTitle);
        backButton = findViewById(R.id.backButton);

        // تحديث العنوان واللون
        if (courseCode != null) {
            courseCodeTextView.setText(courseCode);
        }
        if (courseTitle != null) {
            courseTitleTextView.setText(courseTitle);
        }
        if (courseColorValue != -1) {
            courseColor = courseColorValue; // قيمة اللون الفعلية
            courseHeaderLayout.setBackgroundColor(courseColor);
        } else {
            // استخدام لون افتراضي
            courseColor = 0xFF005A82;
            courseHeaderLayout.setBackgroundColor(courseColor);
        }
        if (buttonLabel != null) {
            sectionTitle.setText(buttonLabel);
        }

        Utils.setSystemBarColorWithColorInt(this, courseColor, getResources().getColor(R.color.Custom_BackgroundColor), 0);

        // إعداد زر الرجوع
        backButton.setOnClickListener(v -> finish());

        List<ParticipantData> participants = new ArrayList<>();
        participants.add(new ParticipantData(R.string.mazen_Al_boushi, R.string.student_role, R.string.john_description, R.drawable.mazen_photo));
        participants.add(new ParticipantData(R.string.raouf_hamdan, R.string.doctor_role, R.string.jane_description, R.drawable.raouf_photo));
        participants.add(new ParticipantData(R.string.radwan_kastantin, R.string.coordinator_role, R.string.alice_description, R.drawable.radwan_photo));
        participants.add(new ParticipantData(R.string.lana_kaddourah, R.string.student_role, R.string.john_description, R.drawable.lana_photo));
        participants.add(new ParticipantData(R.string.abeer_kharfan, R.string.student_role, R.string.john_description, R.drawable.abeer_photo));

        ParticipantsAdapter adapter = new ParticipantsAdapter(participants);
        recyclerView.setAdapter(adapter);

        searchBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        searchBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                textInputLayout.setStartIconTintList(AppCompatResources.getColorStateList(this, R.color.md_theme_primary));
                textInputLayout.setStartIconDrawable(R.drawable.searchselect);
                new Handler(Looper.getMainLooper()).postDelayed(() -> showKeyboard(searchBar), 100);
            } else {
                textInputLayout.setStartIconTintList(AppCompatResources.getColorStateList(this, R.color.Med_Grey));
                textInputLayout.setStartIconDrawable(R.drawable.search);
            }
        });

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(v.getText().toString());
                hideKeyboard();
                searchBar.clearFocus();
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    hideKeyboard();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void performSearch(String query) {
        // تنفيذ البحث هنا
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
