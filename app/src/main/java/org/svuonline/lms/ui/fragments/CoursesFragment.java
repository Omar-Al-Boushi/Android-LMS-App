package org.svuonline.lms.ui.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.svuonline.lms.R;
import org.svuonline.lms.ui.adapters.CourseCardAdapter;
import org.svuonline.lms.ui.data.CourseCardData;

import java.util.ArrayList;
import java.util.List;

public class CoursesFragment extends Fragment {

    private RecyclerView recyclerView;
    private CourseCardAdapter adapter;
    private List<CourseCardData> courseList;
    private boolean isListView = false;
    private LinearLayout statusBtn;

    private MaterialButton cardsBtn, listBtn, filterBtn,passedBtn,registeredBtn,remainingBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText searchBar = view.findViewById(R.id.search_bar);
        TextInputLayout textInputLayout = view.findViewById(R.id.outlinedTextField);
        recyclerView = view.findViewById(R.id.recycler_view);
        cardsBtn = view.findViewById(R.id.cardsBtn);
        listBtn = view.findViewById(R.id.listBtn);
        filterBtn = view.findViewById(R.id.filterBtn);
        statusBtn = view.findViewById(R.id.statusBtn);
        passedBtn = view.findViewById(R.id.passedBtn);
        registeredBtn = view.findViewById(R.id.registeredBtn);
        remainingBtn = view.findViewById(R.id.remainingBtn);
        // قائمة الأزرار للتحكم بها
        MaterialButton[] buttons = {passedBtn, registeredBtn, remainingBtn};
        // اللون الافتراضي واللون المخصص
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.dark_grey);

        cardsBtn.setSelected(true);  // تعيين الزر إلى حالة التحديد الأولية
        searchBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        filterBtn.setOnClickListener(v -> {
            if (statusBtn.getVisibility() == View.GONE) {
                statusBtn.setVisibility(View.VISIBLE);
                filterBtn.setSelected(true);
                filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
            } else {
                statusBtn.setVisibility(View.GONE);
                filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.Custom_Black));
                filterBtn.setSelected(false);
            }
        });

        // ضبط حدث النقر على كل زر
        for (MaterialButton button : buttons) {
            button.setOnClickListener(v -> {
                // عند النقر، اجعل الزر الحالي باللون المحدد والباقي باللون الافتراضي
                for (MaterialButton btn : buttons) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(btn == button ? selectedColor : defaultColor));
                }
            });
        }


        searchBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                textInputLayout.setStartIconTintList(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
                textInputLayout.setStartIconDrawable(R.drawable.searchselect);
                new Handler(Looper.getMainLooper()).postDelayed(() -> showKeyboard(searchBar), 100);
            } else {
                textInputLayout.setStartIconTintList(AppCompatResources.getColorStateList(requireContext(), R.color.Med_Grey));
                textInputLayout.setStartIconDrawable(R.drawable.search);
            }
        });

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(v.getText().toString());
                hideKeyboard(view);
                searchBar.clearFocus();
                return true;
            }
            return false;
        });

        view.setOnTouchListener((v, event) -> {
            searchBar.clearFocus();
            hideKeyboard(view);
            v.performClick();
            return false;
        });

        setupRecyclerView();
        setupButtons();
        updateButtonStates();

    }

    private void setupRecyclerView() {
        courseList = new ArrayList<>();
        courseList.add(new CourseCardData("CEE308", getResources().getString(R.string.bachelor_in_communications_technology_bact), getString(R.string.course_title_3), false, false, true, false, getResources().getColor(R.color.Custom_MainColorBlue)));
        courseList.add(new CourseCardData("GMA205", getResources().getString(R.string.bachelor_in_communications_technology_bact), getString(R.string.course_title_2), false, false, true, false, getResources().getColor(R.color.Custom_MainColorTeal)));
        courseList.add(new CourseCardData("BQM304", getResources().getString(R.string.bachelor_in_communications_technology_bact), getString(R.string.course_title_4), false, false, false, true, getResources().getColor(R.color.Custom_MainColorPurple)));
        courseList.add(new CourseCardData("INT305", getResources().getString(R.string.bachelor_in_communications_technology_bact), getString(R.string.course_title_1), true, true, false, false, getResources().getColor(R.color.Custom_MainColorGolden)));

        adapter = new CourseCardAdapter(courseList, isListView);
        updateRecyclerViewLayout();
        recyclerView.setAdapter(adapter);
    }

    private void setupButtons() {
        cardsBtn.setOnClickListener(v -> {
            if (!isListView) return;
            isListView = false;
            adapter.setListView(false);
            updateRecyclerViewLayout(); // إعادة تعيين التخطيط
            adapter.notifyDataSetChanged(); // تحديث بيانات الـ Adapter
            updateButtonStates();
        });

        listBtn.setOnClickListener(v -> {
            if (isListView) return;
            isListView = true;
            adapter.setListView(true);
            updateRecyclerViewLayout(); // إعادة تعيين التخطيط
            adapter.notifyDataSetChanged(); // تحديث بيانات الـ Adapter
            updateButtonStates();
        });


    }

    private void updateRecyclerViewLayout() {
        if (isListView) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), calculateNoOfColumns(400)));
        }
        recyclerView.setAdapter(adapter); // إعادة تعيين الـ Adapter لضمان التحديث
    }


    private int calculateNoOfColumns(int columnWidthDp) {
        float screenWidthDp = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().density;
        return (int) (screenWidthDp / columnWidthDp + 0.5);
    }

    private void updateButtonStates() {
        // تحديث حالة الأزرار بناءً على طريقة العرض (قائمة أو بطاقات)
        if (isListView) {
            cardsBtn.setSelected(false);
            listBtn.setSelected(true);
            listBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
            cardsBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.Custom_Black));
        } else {
            cardsBtn.setSelected(true);
            listBtn.setSelected(false);
            cardsBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
            listBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.Custom_Black));
        }
    }


    private void performSearch(String query) {
        showCustomSnackbar(getView());
    }

    private void hideKeyboard(View view) {
        if (getActivity() != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void showKeyboard(View view) {
        if (getActivity() != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void showCustomSnackbar(View view) {
        Snackbar snackbar = Snackbar.make(view, "النتائج غير موجودة حالياً", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
}
