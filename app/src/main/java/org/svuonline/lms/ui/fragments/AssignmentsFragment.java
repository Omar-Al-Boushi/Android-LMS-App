package org.svuonline.lms.ui.fragments;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.svuonline.lms.R;
import org.svuonline.lms.ui.adapters.AssignmentCardAdapter;
import org.svuonline.lms.ui.data.AssignmentCardData;
import java.util.ArrayList;
import java.util.List;

public class AssignmentsFragment extends Fragment {

    private AssignmentCardAdapter adapter;
    private List<AssignmentCardData> assignmentList;
    private MaterialButton filterBtn, sortBtn;
    private LinearLayout statusBtn,sortStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assignments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // تعريف الأزرار
        filterBtn = view.findViewById(R.id.filterBtn);
        statusBtn = view.findViewById(R.id.statusBtn);
        sortBtn = view.findViewById(R.id.sortBtn);
        sortStatus = view.findViewById(R.id.sortStatus);
        MaterialButton startFirstBtn = view.findViewById(R.id.startFirstBtn);
        MaterialButton endFirstBtn = view.findViewById(R.id.endFirstBtn);
        MaterialButton progressBtn = view.findViewById(R.id.progressBtn);
        MaterialButton completedBtn = view.findViewById(R.id.completedBtn);
        TextInputEditText searchBar = view.findViewById(R.id.search_bar);
        TextInputLayout textInputLayout = view.findViewById(R.id.outlinedTextField);


        searchBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        // اللون الافتراضي واللون المخصص
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.dark_grey);

        // قائمة الأزرار للتحكم بها
        MaterialButton[] buttonsSorting = {startFirstBtn, endFirstBtn};
        MaterialButton[] buttonsFiltering = {progressBtn, completedBtn};

        // تعيين استماعات الضغط
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

        sortBtn.setOnClickListener(v -> {
            if (sortStatus.getVisibility() == View.GONE) {
                sortStatus.setVisibility(View.VISIBLE);
                sortBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
                sortBtn.setSelected(true);
            } else {
                sortStatus.setVisibility(View.GONE);
                sortBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.Custom_Black));
                sortBtn.setSelected(false);
            }
        });

        // ضبط حدث النقر على كل زر
        for (MaterialButton button : buttonsSorting) {
            button.setOnClickListener(v -> {
                // عند النقر، اجعل الزر الحالي باللون المحدد والباقي باللون الافتراضي
                for (MaterialButton btn : buttonsSorting) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(btn == button ? selectedColor : defaultColor));
                }
            });
        }

        // ضبط حدث النقر على كل زر
        for (MaterialButton button : buttonsSorting) {
            button.setOnClickListener(v -> {
                // عند النقر، اجعل الزر الحالي باللون المحدد والباقي باللون الافتراضي
                for (MaterialButton btn : buttonsSorting) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(btn == button ? selectedColor : defaultColor));
                }
            });
        }

        // ضبط حدث النقر على كل زر
        for (MaterialButton button : buttonsFiltering) {
            button.setOnClickListener(v -> {
                // عند النقر، اجعل الزر الحالي باللون المحدد والباقي باللون الافتراضي
                for (MaterialButton btn : buttonsFiltering) {
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

        // تجهيز البيانات
        assignmentList = new ArrayList<>();
        loadAssignments();

        // إنشاء الأدابتر وتمرير السياق
        adapter = new AssignmentCardAdapter(requireContext(), assignmentList);
        recyclerView.setAdapter(adapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadAssignments() {
        // إضافة بيانات تجريبية لاختبار العرض
        assignmentList.add(new AssignmentCardData(getString(R.string.course_title_3), "CEE308", "20-10-2024", "18-12-2024", getResources().getString(R.string.completed), getResources().getColor(R.color.Custom_MainColorBlue)));
        assignmentList.add(new AssignmentCardData(getString(R.string.course_title_1), "INT305", "20-10-2024", "18-12-2024", getResources().getString(R.string.progress), getResources().getColor(R.color.Custom_MainColorGolden)));
        assignmentList.add(new AssignmentCardData(getString(R.string.course_title_4), "CCN403", "20-10-2024", "18-12-2024", getResources().getString(R.string.progress), getResources().getColor(R.color.Custom_MainColorDarkPink)));

        // تحديث الأدابتر بعد إضافة البيانات
        if (adapter != null) {
            adapter.notifyDataSetChanged();
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
