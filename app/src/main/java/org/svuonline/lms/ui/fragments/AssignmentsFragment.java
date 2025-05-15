package org.svuonline.lms.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.svuonline.lms.R;
import org.svuonline.lms.data.model.Assignment;
import org.svuonline.lms.data.repository.AssignmentRepository;
import org.svuonline.lms.data.repository.AssignmentSubmissionRepository;
import org.svuonline.lms.data.repository.EnrollmentRepository;
import org.svuonline.lms.ui.adapters.AssignmentCardAdapter;
import org.svuonline.lms.ui.data.AssignmentCardData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * فراغمنت لعرض الواجبات مع خيارات التصفية، الترتيب، والبحث.
 */
public class AssignmentsFragment extends Fragment {

    // مفاتيح SharedPreferences
    private static final String PREFS_NAME = "AppPreferences";
    private static final String PREF_ORDER_NAME = "AssignmentOrderPrefs";
    private static final String PREF_ORDER_KEY = "assignment_order";
    private static final String PREF_IDS_KEY = "assignment_ids";

    // عناصر واجهة المستخدم
    private RecyclerView recyclerView;
    private TextInputEditText searchBar;
    private TextInputLayout textInputLayout;
    private MaterialButton filterBtn;
    private MaterialButton sortBtn;
    private MaterialButton startFirstBtn;
    private MaterialButton endFirstBtn;
    private MaterialButton progressBtn;
    private MaterialButton completedBtn;
    private LinearLayout statusBtnLayout;
    private LinearLayout sortStatusLayout;

    // المستودعات
    private AssignmentRepository assignmentRepository;
    private EnrollmentRepository enrollmentRepository;
    private AssignmentSubmissionRepository submissionRepository;

    // بيانات الفراغمنت
    private int userId;
    private String currentStatusFilter = "";
    private String currentSortOrder = "";
    private String currentSearchQuery = "";
    private List<AssignmentCardData> assignmentList;
    private AssignmentCardAdapter adapter;
    private SharedPreferences preferences;
    private SharedPreferences orderPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assignments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // تهيئة المكونات
        initComponents();

        // تهيئة الواجهة
        initViews(view);

        // التحقق من بيانات المستخدم
        if (!validateUserData()) {
            return;
        }

        // تهيئة البيانات
        initData();

        // إعداد مستمعات الأحداث
        setupListeners();

        // التعامل مع التصفية من Bundle
        handleBundleFilter();

        // تحميل الواجبات
        loadAssignments();
    }

    /**
     * تهيئة المستودعات
     */
    private void initComponents() {
        assignmentRepository = new AssignmentRepository(requireContext());
        enrollmentRepository = new EnrollmentRepository(requireContext());
        submissionRepository = new AssignmentSubmissionRepository(requireContext());
        preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        orderPrefs = requireActivity().getSharedPreferences(PREF_ORDER_NAME, Context.MODE_PRIVATE);
    }

    /**
     * تهيئة عناصر الواجهة
     * @param view الواجهة المراد تهيئتها
     */
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        searchBar = view.findViewById(R.id.search_bar);
        textInputLayout = view.findViewById(R.id.outlinedTextField);
        filterBtn = view.findViewById(R.id.filterBtn);
        sortBtn = view.findViewById(R.id.sortBtn);
        startFirstBtn = view.findViewById(R.id.startFirstBtn);
        endFirstBtn = view.findViewById(R.id.endFirstBtn);
        progressBtn = view.findViewById(R.id.progressBtn);
        completedBtn = view.findViewById(R.id.completedBtn);
        statusBtnLayout = view.findViewById(R.id.statusBtn);
        sortStatusLayout = view.findViewById(R.id.sortStatus);
    }

    /**
     * التحقق من صحة بيانات المستخدم
     * @return صحيح إذا كانت البيانات صالحة
     */
    private boolean validateUserData() {
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userId = (int) userPrefs.getLong("user_id", -1);
        if (userId == -1) {
            showToast(R.string.user_id_not_found);
            return false;
        }
        return true;
    }

    /**
     * تهيئة البيانات (RecyclerView)
     */
    private void initData() {
        assignmentList = new ArrayList<>();
        adapter = new AssignmentCardAdapter(requireContext(), assignmentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    /**
     * إعداد مستمعات الأحداث (البحث، التصفية، الترتيب)
     */
    private void setupListeners() {
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.dark_grey);
        MaterialButton[] filterButtons = {progressBtn, completedBtn};
        MaterialButton[] sortButtons = {startFirstBtn, endFirstBtn};

        // إعداد زر الفلتر
        filterBtn.setOnClickListener(v -> toggleFilterButtons(filterButtons, defaultColor));

        // إعداد زر الترتيب
        sortBtn.setOnClickListener(v -> toggleSortButtons(sortButtons, defaultColor));

        // إعداد أزرار التصفية
        for (MaterialButton button : filterButtons) {
            button.setOnClickListener(v -> applyStatusFilter(button, filterButtons, selectedColor, defaultColor));
        }

        // إعداد أزرار الترتيب
        for (MaterialButton button : sortButtons) {
            button.setOnClickListener(v -> applySortOrder(button, sortButtons, selectedColor, defaultColor));
        }

        // إعداد البحث
        searchBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentSearchQuery = v.getText().toString().trim();
                loadAssignments();
                hideKeyboard(v);
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

        // إخفاء لوحة المفاتيح عند النقر خارج الحقل
        recyclerView.setOnTouchListener((v, event) -> {
            searchBar.clearFocus();
            hideKeyboard(v);
            return false;
        });
    }

    /**
     * التعامل مع التصفية من Bundle
     */
    private void handleBundleFilter() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("filter_status2")) {
            String filterStatus = args.getString("filter_status2");
            if (filterStatus != null && !filterStatus.isEmpty()) {
                applyFilter(filterStatus);
            }
        }
    }

    /**
     * تطبيق تصفية بناءً على الحالة
     * @param filter الحالة (All, Completed, Progress)
     */
    public void applyFilter(String filter) {
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.dark_grey);

        if ("Completed".equals(filter)) {
            currentStatusFilter = "Completed";
            completedBtn.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
            progressBtn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            filterBtn.setSelected(true);
            filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
            statusBtnLayout.setVisibility(View.VISIBLE);
        } else if ("Progress".equals(filter)) {
            currentStatusFilter = "Progress";
            progressBtn.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
            completedBtn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            filterBtn.setSelected(true);
            filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
            statusBtnLayout.setVisibility(View.VISIBLE);
        } else if ("All".equals(filter)) {
            currentStatusFilter = "";
            filterBtn.setSelected(false);
            filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.Custom_Black));
            statusBtnLayout.setVisibility(View.GONE);
            completedBtn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            progressBtn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
        }
        loadAssignments();
    }

    /**
     * إظهار/إخفاء أزرار التصفية
     * @param filterButtons أزرار التصفية
     * @param defaultColor اللون الافتراضي
     */
    private void toggleFilterButtons(MaterialButton[] filterButtons, int defaultColor) {
        if (statusBtnLayout.getVisibility() == View.GONE) {
            statusBtnLayout.setVisibility(View.VISIBLE);
            filterBtn.setSelected(true);
            filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
            for (MaterialButton btn : filterButtons) {
                btn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            }
            currentStatusFilter = "";
        } else {
            statusBtnLayout.setVisibility(View.GONE);
            filterBtn.setSelected(false);
            filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.Custom_Black));
            for (MaterialButton btn : filterButtons) {
                btn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            }
            currentStatusFilter = "";
        }
        loadAssignments();
    }

    /**
     * إظهار/إخفاء أزرار الترتيب
     * @param sortButtons أزرار الترتيب
     * @param defaultColor اللون الافتراضي
     */
    private void toggleSortButtons(MaterialButton[] sortButtons, int defaultColor) {
        if (sortStatusLayout.getVisibility() == View.GONE) {
            sortStatusLayout.setVisibility(View.VISIBLE);
            sortBtn.setSelected(true);
            sortBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
            for (MaterialButton btn : sortButtons) {
                btn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            }
        } else {
            sortStatusLayout.setVisibility(View.GONE);
            sortBtn.setSelected(false);
            sortBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.Custom_Black));
            for (MaterialButton btn : sortButtons) {
                btn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            }
            currentSortOrder = "";
        }
        loadAssignments();
    }

    /**
     * تطبيق تصفية الحالة
     * @param button الزر المضغوط
     * @param filterButtons أزرار التصفية
     * @param selectedColor اللون المحدد
     * @param defaultColor اللون الافتراضي
     */
    private void applyStatusFilter(MaterialButton button, MaterialButton[] filterButtons, int selectedColor, int defaultColor) {
        for (MaterialButton btn : filterButtons) {
            btn.setBackgroundTintList(ColorStateList.valueOf(btn == button ? selectedColor : defaultColor));
        }
        currentStatusFilter = button == progressBtn ? "Progress" : "Completed";
        loadAssignments();
    }

    /**
     * تطبيق ترتيب التاريخ
     * @param button الزر المضغوط
     * @param sortButtons أزرار الترتيب
     * @param selectedColor اللون المحدد
     * @param defaultColor اللون الافتراضي
     */
    private void applySortOrder(MaterialButton button, MaterialButton[] sortButtons, int selectedColor, int defaultColor) {
        for (MaterialButton btn : sortButtons) {
            btn.setBackgroundTintList(ColorStateList.valueOf(btn == button ? selectedColor : defaultColor));
        }
        currentSortOrder = button == startFirstBtn ? "StartFirst" : "EndFirst";
        loadAssignments();
    }

    /**
     * تحميل الواجبات بناءً على التصفية، الترتيب، والبحث
     */
    private void loadAssignments() {
        assignmentList.clear();
        List<Assignment> assignments = assignmentRepository.getAssignmentsByUserId(
                userId, currentStatusFilter, currentSearchQuery, isArabicLocale());

        List<Assignment> registeredAssignments = new ArrayList<>();
        for (Assignment assignment : assignments) {
            if (enrollmentRepository.isUserEnrolledInCourse(userId, assignment.getCourseCode())) {
                registeredAssignments.add(assignment);
            }
        }

        List<AssignmentCardData> tempList = new ArrayList<>();
        for (Assignment assignment : registeredAssignments) {
            int color = resolveColor(assignment.getColor());
            String courseName = resolveCourseName(assignment);
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

        List<AssignmentCardData> arranged = arrangeAssignmentsWithColorConstraints(tempList);
        if (!currentSortOrder.isEmpty()) {
            arranged = sortAssignments(arranged, currentSortOrder);
        }

        assignmentList.addAll(arranged);
        adapter.notifyDataSetChanged();

        if (assignmentList.isEmpty()) {
            showToast(R.string.no_results_found);
        }
    }

    /**
     * تحديد لون الواجب
     * @param colorName اسم اللون
     * @return معرف اللون
     */
    private int resolveColor(String colorName) {
        if (colorName != null && !colorName.isEmpty()) {
            int colorResId = getResources().getIdentifier(colorName, "color", requireContext().getPackageName());
            if (colorResId != 0) {
                return ContextCompat.getColor(requireContext(), colorResId);
            }
        }
        return ContextCompat.getColor(requireContext(), R.color.Custom_MainColorBlue);
    }

    /**
     * تحديد اسم الواجب
     * @param assignment الواجب
     * @return اسم الواجب
     */
    private String resolveCourseName(Assignment assignment) {
        String courseName = isArabicLocale() ? assignment.getTitleAr() : assignment.getTitleEn();
        return (courseName == null || courseName.isEmpty()) ?
                getString(R.string.unknown_assignment) : courseName;
    }

    /**
     * ترتيب الواجبات مع مراعاة الألوان
     * @param assignments قائمة الواجبات
     * @return القائمة المرتبة
     */
    private List<AssignmentCardData> arrangeAssignmentsWithColorConstraints(List<AssignmentCardData> assignments) {
        if (assignments.size() <= 1) {
            return assignments;
        }

        SharedPreferences.Editor editor = orderPrefs.edit();
        List<Assignment> allAssignments = assignmentRepository.getAssignmentsByUserId(userId, "", "", isArabicLocale());
        Set<String> currentAssignmentIds = new HashSet<>();
        for (Assignment assignment : allAssignments) {
            currentAssignmentIds.add(String.valueOf(assignment.getAssignmentId()));
        }

        String savedOrder = orderPrefs.getString(PREF_ORDER_KEY, "");
        Set<String> savedAssignmentIds = orderPrefs.getStringSet(PREF_IDS_KEY, new HashSet<>());
        boolean isOrderValid = !savedOrder.isEmpty() && savedAssignmentIds.equals(currentAssignmentIds);

        List<AssignmentCardData> result = new ArrayList<>();
        if (isOrderValid) {
            String[] orderArray = savedOrder.split(",");
            List<Integer> orderIds = new ArrayList<>();
            for (String id : orderArray) {
                try {
                    orderIds.add(Integer.parseInt(id));
                } catch (NumberFormatException e) {
                    showToast(R.string.invalid_assignment_order);
                }
            }

            for (Integer id : orderIds) {
                for (AssignmentCardData assignment : assignments) {
                    if (assignment.getAssignmentId() == id) {
                        result.add(assignment);
                        break;
                    }
                }
            }
        } else {
            List<AssignmentCardData> allAssignmentCards = new ArrayList<>();
            for (Assignment assignment : allAssignments) {
                int color = resolveColor(assignment.getColor());
                String courseName = resolveCourseName(assignment);
                boolean isSubmitted = submissionRepository.isAssignmentSubmitted(assignment.getAssignmentId(), userId);
                String translatedStatus = isSubmitted ? getString(R.string.completed) : getString(R.string.progress);

                allAssignmentCards.add(new AssignmentCardData(
                        courseName,
                        assignment.getCourseCode(),
                        assignment.getOpenDate(),
                        assignment.getDueDate(),
                        translatedStatus,
                        color,
                        assignment.getAssignmentId()));
            }

            List<AssignmentCardData> remaining = new ArrayList<>(allAssignmentCards);
            List<AssignmentCardData> shuffledResult = new ArrayList<>();
            Collections.shuffle(remaining);

            while (!remaining.isEmpty()) {
                boolean added = false;
                for (int i = 0; i < remaining.size(); i++) {
                    AssignmentCardData candidate = remaining.get(i);
                    if (shuffledResult.isEmpty() ||
                            shuffledResult.get(shuffledResult.size() - 1).getBackgroundColor() != candidate.getBackgroundColor()) {
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

            StringBuilder newOrder = new StringBuilder();
            for (int i = 0; i < shuffledResult.size(); i++) {
                newOrder.append(shuffledResult.get(i).getAssignmentId());
                if (i < shuffledResult.size() - 1) {
                    newOrder.append(",");
                }
            }
            editor.putString(PREF_ORDER_KEY, newOrder.toString());
            editor.putStringSet(PREF_IDS_KEY, currentAssignmentIds);
            editor.apply();

            for (AssignmentCardData assignment : shuffledResult) {
                if (assignments.contains(assignment)) {
                    result.add(assignment);
                }
            }
        }

        return result;
    }

    /**
     * ترتيب الواجبات حسب التاريخ
     * @param assignments قائمة الواجبات
     * @param sortOrder نوع الترتيب (StartFirst, EndFirst)
     * @return القائمة المرتبة
     */
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
                } catch (Exception e) {
                    showToast(R.string.date_parse_error);
                    return 0;
                }
            };
        } else {
            comparator = (a1, a2) -> {
                try {
                    Date date1 = dateFormat.parse(a1.getTimeEnd());
                    Date date2 = dateFormat.parse(a2.getTimeEnd());
                    return date1.compareTo(date2);
                } catch (Exception e) {
                    showToast(R.string.date_parse_error);
                    return 0;
                }
            };
        }

        Collections.sort(sortedList, comparator);
        return sortedList;
    }

    /**
     * التحقق من اللغة العربية
     * @return صحيح إذا كانت اللغة عربية
     */
    private boolean isArabicLocale() {
        String selectedLanguage = preferences.getString("selected_language", "en");
        return "ar".equals(selectedLanguage);
    }

    /**
     * إخفاء لوحة المفاتيح
     * @param view العنصر المراد إخفاء لوحة المفاتيح له
     */
    private void hideKeyboard(View view) {
        if (getActivity() != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    /**
     * إظهار لوحة المفاتيح
     * @param view العنصر المراد إظهار لوحة المفاتيح له
     */
    private void showKeyboard(View view) {
        if (getActivity() != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    /**
     * تحديث الواجبات عند استئناف الفراغمنت
     */
    @Override
    public void onResume() {
        super.onResume();
        if (userId != -1) {
            loadAssignments();
        }
    }

    /**
     * عرض رسالة Toast
     * @param messageRes معرف الرسالة
     */
    private void showToast(int messageRes) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), messageRes, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}