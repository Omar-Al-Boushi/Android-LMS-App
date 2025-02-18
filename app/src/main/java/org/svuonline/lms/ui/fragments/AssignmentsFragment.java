package org.svuonline.lms.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import org.svuonline.lms.data.model.Assignment;
import org.svuonline.lms.data.repository.AssignmentRepository;
import org.svuonline.lms.data.repository.AssignmentSubmissionRepository;
import org.svuonline.lms.data.repository.EnrollmentRepository;
import org.svuonline.lms.ui.adapters.AssignmentCardAdapter;
import org.svuonline.lms.ui.data.AssignmentCardData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AssignmentsFragment extends Fragment {

    private static final String TAG = "AssignmentsFragment";
    private RecyclerView recyclerView;
    private AssignmentCardAdapter adapter;
    private List<AssignmentCardData> assignmentList;
    private MaterialButton filterBtn, sortBtn, startFirstBtn, endFirstBtn, progressBtn, completedBtn;
    private LinearLayout statusBtn, sortStatus;
    private TextInputEditText searchBar;
    private TextInputLayout textInputLayout;
    private AssignmentRepository assignmentRepository;
    private EnrollmentRepository enrollmentRepository;
    private int userId;
    private String currentStatusFilter = "";
    private String currentSortOrder = "";
    private String currentSearchQuery = "";
    private SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assignments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // تهيئة المكونات
        recyclerView = view.findViewById(R.id.recycler_view);
        filterBtn = view.findViewById(R.id.filterBtn);
        statusBtn = view.findViewById(R.id.statusBtn);
        sortBtn = view.findViewById(R.id.sortBtn);
        sortStatus = view.findViewById(R.id.sortStatus);
        startFirstBtn = view.findViewById(R.id.startFirstBtn);
        endFirstBtn = view.findViewById(R.id.endFirstBtn);
        progressBtn = view.findViewById(R.id.progressBtn);
        completedBtn = view.findViewById(R.id.completedBtn);
        searchBar = view.findViewById(R.id.search_bar);
        textInputLayout = view.findViewById(R.id.outlinedTextField);

        // تهيئة المستودعات
        assignmentRepository = new AssignmentRepository(requireContext());
        enrollmentRepository = new EnrollmentRepository(requireContext());

        // تهيئة SharedPreferences
        preferences = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);

        // جلب userId من SharedPreferences
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userId = (int) userPrefs.getLong("user_id", -1);

        // إعداد RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        assignmentList = new ArrayList<>();
        adapter = new AssignmentCardAdapter(requireContext(), assignmentList);
        recyclerView.setAdapter(adapter);

        // إعداد الألوان
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.dark_grey);

        // قوائم الأزرار
        MaterialButton[] buttonsSorting = {startFirstBtn, endFirstBtn};
        MaterialButton[] buttonsFiltering = {progressBtn, completedBtn};

        // إعداد زر الفلتر
        filterBtn.setOnClickListener(v -> toggleFilter(buttonsFiltering, defaultColor));

        // إعداد زر الترتيب
        sortBtn.setOnClickListener(v -> toggleSort(buttonsSorting, defaultColor));

        // إعداد أزرار التصفية
        for (MaterialButton button : buttonsFiltering) {
            button.setOnClickListener(v -> {
                for (MaterialButton btn : buttonsFiltering) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(btn == button ? selectedColor : defaultColor));
                }
                currentStatusFilter = button == progressBtn ? "Progress" : "Completed";
                loadAssignments();
            });
        }

        // إعداد أزرار الترتيب
        for (MaterialButton button : buttonsSorting) {
            button.setOnClickListener(v -> {
                for (MaterialButton btn : buttonsSorting) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(btn == button ? selectedColor : defaultColor));
                }
                currentSortOrder = button == startFirstBtn ? "StartFirst" : "EndFirst";
                loadAssignments();
            });
        }

        // إعداد البحث
        searchBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentSearchQuery = v.getText().toString().trim();
                loadAssignments();
                hideKeyboard(view);
                searchBar.clearFocus();
                return true;
            }
            return false;
        });

        textInputLayout.setEndIconOnClickListener(v -> {
            searchBar.setText("");
            currentSearchQuery = "";
            loadAssignments();
        });

        view.setOnTouchListener((v, event) -> {
            searchBar.clearFocus();
            hideKeyboard(view);
            v.performClick();
            return false;
        });

        // التحقق مما إذا كانت هناك حالة تصفية مرسلة عبر الـ Bundle
        Bundle args = getArguments();
        if (args != null && args.containsKey("filter_status2")) {
            String filterStatus2 = args.getString("filter_status2");
            if (filterStatus2 != null && !filterStatus2.isEmpty()) {
                applyFilter(filterStatus2);
            }
        }

        // تحميل الوظائف
        loadAssignments();

    }

    // دالة جديدة لتطبيق الفلتر
    public void applyFilter(String filter) {
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.dark_grey);

        if ("Completed".equals(filter)) {
            currentStatusFilter = "Completed";
            completedBtn.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
            progressBtn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            filterBtn.setSelected(true);
            filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
            statusBtn.setVisibility(View.VISIBLE);
            loadAssignments();

        } else if ("Progress".equals(filter)) {
            currentStatusFilter = "Progress";
            progressBtn.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
            completedBtn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            filterBtn.setSelected(true);
            filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
            statusBtn.setVisibility(View.VISIBLE);
            loadAssignments();
        } else if ("All".equals(filter)) {
            currentStatusFilter = "";
            filterBtn.setSelected(false);
            filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.Custom_Black));
            statusBtn.setVisibility(View.GONE);
            completedBtn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            progressBtn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            loadAssignments();
        }
    }

    private void toggleFilter(MaterialButton[] buttonsFiltering, int defaultColor) {
        if (statusBtn.getVisibility() == View.GONE) {
            statusBtn.setVisibility(View.VISIBLE);
            filterBtn.setSelected(true);
            filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
            for (MaterialButton btn : buttonsFiltering) {
                btn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            }
        } else {
            statusBtn.setVisibility(View.GONE);
            filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.Custom_Black));
            filterBtn.setSelected(false);
            for (MaterialButton btn : buttonsFiltering) {
                btn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            }
            currentStatusFilter = "";
            loadAssignments();
        }
    }

    private void toggleSort(MaterialButton[] buttonsSorting, int defaultColor) {
        if (sortStatus.getVisibility() == View.GONE) {
            sortStatus.setVisibility(View.VISIBLE);
            sortBtn.setSelected(true);
            sortBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
            for (MaterialButton btn : buttonsSorting) {
                btn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            }
        } else {
            sortStatus.setVisibility(View.GONE);
            sortBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.Custom_Black));
            sortBtn.setSelected(false);
            for (MaterialButton btn : buttonsSorting) {
                btn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            }
            currentSortOrder = "";
            loadAssignments();
        }
    }

    private void loadAssignments() {
        assignmentList.clear();
        // 1. جلب كل المهام حسب الفلاتر من AssignmentRepository
        List<Assignment> assignments = assignmentRepository.getAssignmentsByUserId(
                userId, currentStatusFilter, currentSearchQuery, isArabicLocale());

        // 2. ترشيح المهام للكورسات المسجلة فقط
        List<Assignment> registered = new ArrayList<>();
        for (Assignment a : assignments) {
            if (enrollmentRepository.isUserEnrolledInCourse(userId, a.getCourseCode())) {
                registered.add(a);
            }
        }

        // 3. تهيئة AssignmentSubmissionRepository
        AssignmentSubmissionRepository submissionRepository = new AssignmentSubmissionRepository(requireContext());

        // 4. تحويل المهام إلى بيانات العرض
        List<AssignmentCardData> tempList = new ArrayList<>();
        for (Assignment assignment : registered) {
            int color = resolveColor(assignment.getColor());
            String courseName = resolveCourseName(assignment);

            // التحقق من حالة الإرسال باستخدام الدالة الجديدة
            boolean isSubmitted = submissionRepository.isAssignmentSubmitted(assignment.getAssignmentId(), userId);
            String translatedStatus = isSubmitted ? getString(R.string.completed) : getString(R.string.progress);

            tempList.add(new AssignmentCardData(
                    courseName,
                    assignment.getCourseCode(),
                    assignment.getOpenDate(),
                    assignment.getDueDate(),
                    translatedStatus,
                    color,
                    assignment.getAssignmentId()));
        }

        // 5. ترتيب وخلط مع قيود اللون
        List<AssignmentCardData> arranged = arrangeAssignmentsWithColorConstraints(tempList);
        if (!currentSortOrder.isEmpty()) {
            arranged = sortAssignments(arranged, currentSortOrder);
        }

        assignmentList.addAll(arranged);
        adapter.notifyDataSetChanged();

        if (assignmentList.isEmpty()) {
            showCustomSnackbar(getView(), getString(R.string.no_results_found));
        }
    }

    private int resolveColor(String colorName) {
        if (colorName != null && !colorName.isEmpty()) {
            int colorResId = requireContext().getResources().getIdentifier(
                    colorName, "color", requireContext().getPackageName());
            if (colorResId != 0) return ContextCompat.getColor(requireContext(), colorResId);
        }
        return ContextCompat.getColor(requireContext(), R.color.Custom_MainColorBlue);
    }

    private String resolveCourseName(Assignment assignment) {
        String courseName = isArabicLocale() ? assignment.getTitleAr() : assignment.getTitleEn();
        return (courseName == null || courseName.isEmpty()) ?
                getString(R.string.unknown_assignment) : courseName;
    }
    private List<AssignmentCardData> arrangeAssignmentsWithColorConstraints(List<AssignmentCardData> assignments) {
        if (assignments.size() <= 1) {
            return assignments;
        }

        // جلب SharedPreferences لتخزين ترتيب الوظائف
        SharedPreferences orderPrefs = requireActivity().getSharedPreferences("AssignmentOrderPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = orderPrefs.edit();

        // جلب جميع الوظائف (بدون تصفية أو بحث) للتحقق من تغيير القائمة
        List<Assignment> allAssignments = assignmentRepository.getAssignmentsByUserId(userId, "", "", isArabicLocale());
        Set<String> currentAssignmentIds = new HashSet<>();
        for (Assignment assignment : allAssignments) {
            currentAssignmentIds.add(String.valueOf(assignment.getAssignmentId()));
        }

        // جلب الترتيب المحفوظ
        String savedOrder = orderPrefs.getString("assignment_order", "");
        Set<String> savedAssignmentIds = orderPrefs.getStringSet("assignment_ids", new HashSet<>());

        // التحقق مما إذا كان الترتيب المحفوظ صالحًا
        boolean isOrderValid = !savedOrder.isEmpty() && savedAssignmentIds.equals(currentAssignmentIds);

        List<AssignmentCardData> result = new ArrayList<>();
        if (isOrderValid) {
            // استرجاع الترتيب المحفوظ
            String[] orderArray = savedOrder.split(",");
            List<Integer> orderIds = new ArrayList<>();
            for (String id : orderArray) {
                try {
                    orderIds.add(Integer.parseInt(id));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Invalid assignment ID in saved order: " + id);
                }
            }

            // ترتيب الوظائف المصفاة حسب الترتيب المحفوظ
            for (Integer id : orderIds) {
                for (AssignmentCardData assignment : assignments) {
                    if (assignment.getAssignmentId() == id) {
                        result.add(assignment);
                        break;
                    }
                }
            }
        } else {
            // خلط جميع الوظائف بشكل عشوائي مع منع تكرار الألوان
            List<AssignmentCardData> allAssignmentCards = new ArrayList<>();
            for (Assignment assignment : allAssignments) {
                int color;
                String colorName = assignment.getColor();
                if (colorName != null && !colorName.isEmpty()) {
                    int colorResId = getResources().getIdentifier(colorName, "color", requireContext().getPackageName());
                    if (colorResId != 0) {
                        color = ContextCompat.getColor(requireContext(), colorResId);
                    } else {
                        color = ContextCompat.getColor(requireContext(), R.color.Custom_MainColorBlue);
                    }
                } else {
                    color = ContextCompat.getColor(requireContext(), R.color.Custom_MainColorBlue);
                }

                String courseName = isArabicLocale() ? assignment.getTitleAr() : assignment.getTitleEn();
                if (courseName == null || courseName.isEmpty()) {
                    courseName = getString(R.string.unknown_assignment);
                }

                // ترجمة الحالة للوظائف في الخلط الأولي
                String translatedStatus;
                if ("Completed".equals(assignment.getStatus())) {
                    translatedStatus = getString(R.string.completed);
                } else {
                    translatedStatus = getString(R.string.progress);
                }

                AssignmentCardData cardData = new AssignmentCardData(
                        courseName,
                        assignment.getCourseCode(),
                        assignment.getOpenDate(),
                        assignment.getDueDate(),
                        translatedStatus,
                        color,
                        assignment.getAssignmentId()
                );
                allAssignmentCards.add(cardData);
            }

            List<AssignmentCardData> remaining = new ArrayList<>(allAssignmentCards);
            List<AssignmentCardData> shuffledResult = new ArrayList<>();
            Collections.shuffle(remaining);

            while (!remaining.isEmpty()) {
                boolean added = false;
                for (int i = 0; i < remaining.size(); i++) {
                    AssignmentCardData candidate = remaining.get(i);
                    if (shuffledResult.isEmpty() || shuffledResult.get(shuffledResult.size() - 1).getBackgroundColor() != candidate.getBackgroundColor()) {
                        shuffledResult.add(candidate);
                        remaining.remove(i);
                        added = true;
                        break;
                    }
                }
                if (!added && !remaining.isEmpty()) {
                    shuffledResult.add(remaining.get(0));
                    remaining.remove(0);
                }
            }

            // حفظ الترتيب الجديد
            StringBuilder newOrder = new StringBuilder();
            for (int i = 0; i < shuffledResult.size(); i++) {
                newOrder.append(shuffledResult.get(i).getAssignmentId());
                if (i < shuffledResult.size() - 1) {
                    newOrder.append(",");
                }
            }
            editor.putString("assignment_order", newOrder.toString());
            editor.putStringSet("assignment_ids", currentAssignmentIds);
            editor.apply();

            // تصفية الوظائف بناءً على الترتيب المحفوظ
            for (AssignmentCardData assignment : shuffledResult) {
                if (assignments.contains(assignment)) {
                    result.add(assignment);
                }
            }
        }

        return result;
    }

    private List<AssignmentCardData> sortAssignments(List<AssignmentCardData> assignments, String sortOrder) {
        List<AssignmentCardData> sortedList = new ArrayList<>(assignments);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        Comparator<AssignmentCardData> comparator;
        if ("StartFirst".equals(sortOrder)) {
            comparator = (a1, a2) -> {
                try {
                    Date date1 = dateFormat.parse(a1.getTimeStart());
                    Date date2 = dateFormat.parse(a2.getTimeStart());
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    Log.w(TAG, "Error parsing start date: " + e.getMessage());
                    return 0;
                }
            };
        } else {
            comparator = (a1, a2) -> {
                try {
                    Date date1 = dateFormat.parse(a1.getTimeEnd());
                    Date date2 = dateFormat.parse(a2.getTimeEnd());
                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    Log.w(TAG, "Error parsing end date: " + e.getMessage());
                    return 0;
                }
            };
        }

        Collections.sort(sortedList, comparator);
        return sortedList;
    }

    private boolean isArabicLocale() {
        String selectedLanguage = preferences.getString("selected_language", "en");
        return "ar".equals(selectedLanguage);
    }

    private void showCustomSnackbar(View view, String message) {
        // استخدام العرض الجذر للـ Activity
        View rootView = requireActivity().getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
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

    @Override
    public void onResume() {
        super.onResume();
        // عند كل عودة للـ Fragment نعيد تحميل الإحصائيات
        loadAssignments();
    }
}