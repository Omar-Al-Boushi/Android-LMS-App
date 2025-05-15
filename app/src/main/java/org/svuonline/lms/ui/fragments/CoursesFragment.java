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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.svuonline.lms.R;
import org.svuonline.lms.data.db.DatabaseHelper;
import org.svuonline.lms.data.model.Course;
import org.svuonline.lms.data.repository.AcademicProgramRepository;
import org.svuonline.lms.data.repository.CourseRepository;
import org.svuonline.lms.ui.adapters.CourseCardAdapter;
import org.svuonline.lms.ui.data.CourseCardData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * فراغمنت لعرض المقررات مع خيارات التصفية، البحث، وتبديل العرض.
 */
public class CoursesFragment extends Fragment {

    // مفاتيح SharedPreferences
    private static final String PREFS_NAME = "AppPreferences";
    private static final String PREF_ORDER_NAME = "CourseOrderPrefs";
    private static final String PREF_ORDER_KEY = "full_course_order";

    // عناصر واجهة المستخدم
    private RecyclerView recyclerView;
    private TextInputEditText searchBar;
    private TextInputLayout textInputLayout;
    private MaterialButton cardsBtn;
    private MaterialButton listBtn;
    private MaterialButton filterBtn;
    private MaterialButton passedBtn;
    private MaterialButton registeredBtn;
    private MaterialButton remainingBtn;
    private LinearLayout statusBtnLayout;

    // المستودعات
    private CourseRepository courseRepository;
    private AcademicProgramRepository programRepository;

    // بيانات الفراغمنت
    private long userId;
    private boolean isListView;
    private String currentStatusFilter = "";
    private String currentSearchQuery = "";
    private List<CourseCardData> courseCardList;
    private CourseCardAdapter adapter;
    private SharedPreferences preferences;
    private SharedPreferences orderPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses, container, false);
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

        // تحميل المقررات
        loadCourses(currentStatusFilter, currentSearchQuery);
    }

    /**
     * تهيئة المستودعات
     */
    private void initComponents() {
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        courseRepository = new CourseRepository(requireContext());
        programRepository = new AcademicProgramRepository(databaseHelper);
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
        cardsBtn = view.findViewById(R.id.cardsBtn);
        listBtn = view.findViewById(R.id.listBtn);
        filterBtn = view.findViewById(R.id.filterBtn);
        statusBtnLayout = view.findViewById(R.id.statusBtn);
        passedBtn = view.findViewById(R.id.passedBtn);
        registeredBtn = view.findViewById(R.id.registeredBtn);
        remainingBtn = view.findViewById(R.id.remainingBtn);
    }

    /**
     * التحقق من صحة بيانات المستخدم
     * @return صحيح إذا كانت البيانات صالحة
     */
    private boolean validateUserData() {
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userId = userPrefs.getLong("user_id", -1);
        if (userId == -1) {
            showToast(R.string.user_id_not_found);
            return false;
        }
        return true;
    }

    /**
     * تهيئة البيانات (العرض، RecyclerView)
     */
    private void initData() {
        isListView = preferences.getString("view_mode", "cards").equals("list");
        courseCardList = new ArrayList<>();
        adapter = new CourseCardAdapter(courseCardList, isListView);
        updateRecyclerViewLayout();
        recyclerView.setAdapter(adapter);
        updateButtonStates();
    }

    /**
     * إعداد مستمعات الأحداث (البحث، التصفية، تبديل العرض)
     */
    private void setupListeners() {
        // إعداد أزرار تبديل العرض
        cardsBtn.setOnClickListener(v -> switchToCardsView());
        listBtn.setOnClickListener(v -> switchToListView());

        // إعداد زر الفلتر
        filterBtn.setOnClickListener(v -> toggleFilterButtons());

        // إعداد أزرار التصفية
        MaterialButton[] statusButtons = {passedBtn, registeredBtn, remainingBtn};
        for (MaterialButton button : statusButtons) {
            button.setOnClickListener(v -> applyStatusFilter(button));
        }

        // إعداد البحث
        searchBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchBar.setOnFocusChangeListener((v, hasFocus) -> {
            textInputLayout.setStartIconTintList(AppCompatResources.getColorStateList(
                    requireContext(), hasFocus ? R.color.md_theme_primary : R.color.Med_Grey));
            textInputLayout.setStartIconDrawable(hasFocus ? R.drawable.searchselect : R.drawable.search);
            if (hasFocus) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> showKeyboard(searchBar), 100);
            }
        });

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentSearchQuery = v.getText().toString().trim();
                loadCourses(currentStatusFilter, currentSearchQuery);
                hideKeyboard(v);
                searchBar.clearFocus();
                return true;
            }
            return false;
        });

        textInputLayout.setEndIconOnClickListener(v -> {
            searchBar.setText("");
            currentSearchQuery = "";
            loadCourses(currentStatusFilter, currentSearchQuery);
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
        if (args != null && args.containsKey("filter_status")) {
            String filterStatus = args.getString("filter_status");
            if (filterStatus != null && !filterStatus.isEmpty()) {
                applyFilter(filterStatus);
            }
        }
    }

    /**
     * تطبيق تصفية بناءً على الحالة
     * @param status حالة التصفية (Passed, Registered, Remaining)
     */
    public void applyFilter(String status) {
        if (status == null || status.isEmpty()) {
            return;
        }
        currentStatusFilter = status;
        statusBtnLayout.setVisibility(View.VISIBLE);
        filterBtn.setSelected(true);
        filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));

        MaterialButton selectedButton = status.equals("Passed") ? passedBtn :
                status.equals("Registered") ? registeredBtn : remainingBtn;
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.dark_grey);
        for (MaterialButton btn : new MaterialButton[]{passedBtn, registeredBtn, remainingBtn}) {
            btn.setBackgroundTintList(ColorStateList.valueOf(btn == selectedButton ? selectedColor : defaultColor));
        }
        loadCourses(currentStatusFilter, currentSearchQuery);
    }

    /**
     * تبديل إلى عرض البطاقات
     */
    private void switchToCardsView() {
        if (!isListView) {
            return;
        }
        isListView = false;
        adapter.setListView(false);
        updateRecyclerViewLayout();
        adapter.notifyDataSetChanged();
        updateButtonStates();
        preferences.edit().putString("view_mode", "cards").apply();
    }

    /**
     * تبديل إلى عرض القوائم
     */
    private void switchToListView() {
        if (isListView) {
            return;
        }
        isListView = true;
        adapter.setListView(true);
        updateRecyclerViewLayout();
        adapter.notifyDataSetChanged();
        updateButtonStates();
        preferences.edit().putString("view_mode", "list").apply();
    }

    /**
     * إظهار/إخفاء أزرار التصفية
     */
    private void toggleFilterButtons() {
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.dark_grey);
        MaterialButton[] statusButtons = {passedBtn, registeredBtn, remainingBtn};

        if (statusBtnLayout.getVisibility() == View.GONE) {
            statusBtnLayout.setVisibility(View.VISIBLE);
            filterBtn.setSelected(true);
            filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
            for (MaterialButton btn : statusButtons) {
                btn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            }
            currentStatusFilter = "";
        } else {
            statusBtnLayout.setVisibility(View.GONE);
            filterBtn.setSelected(false);
            filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.Custom_Black));
            for (MaterialButton btn : statusButtons) {
                btn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            }
            currentStatusFilter = "";
        }
        loadCourses(currentStatusFilter, currentSearchQuery);
    }

    /**
     * تطبيق تصفية الحالة
     * @param button الزر المضغوط
     */
    private void applyStatusFilter(MaterialButton button) {
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.dark_grey);
        MaterialButton[] statusButtons = {passedBtn, registeredBtn, remainingBtn};

        for (MaterialButton btn : statusButtons) {
            btn.setBackgroundTintList(ColorStateList.valueOf(btn == button ? selectedColor : defaultColor));
        }
        currentStatusFilter = button == passedBtn ? "Passed" :
                button == registeredBtn ? "Registered" : "Remaining";
        loadCourses(currentStatusFilter, currentSearchQuery);
    }

    /**
     * تحديث تخطيط RecyclerView
     */
    private void updateRecyclerViewLayout() {
        if (isListView) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), calculateNoOfColumns(400)));
        }
        recyclerView.setAdapter(adapter);
    }

    /**
     * حساب عدد الأعمدة لعرض البطاقات
     * @param columnWidthDp عرض العمود بالـ dp
     * @return عدد الأعمدة
     */
    private int calculateNoOfColumns(int columnWidthDp) {
        float screenWidthDp = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().density;
        return (int) (screenWidthDp / columnWidthDp + 0.5);
    }

    /**
     * تحديث حالة أزرار تبديل العرض
     */
    private void updateButtonStates() {
        cardsBtn.setSelected(!isListView);
        listBtn.setSelected(isListView);
        listBtn.setIconTint(AppCompatResources.getColorStateList(
                requireContext(), isListView ? R.color.md_theme_primary : R.color.Custom_Black));
        cardsBtn.setIconTint(AppCompatResources.getColorStateList(
                requireContext(), isListView ? R.color.Custom_Black : R.color.md_theme_primary));
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
     * تحميل المقررات بناءً على التصفية والبحث
     * @param statusFilter حالة التصفية
     * @param searchQuery استعلام البحث
     */
    private void loadCourses(String statusFilter, String searchQuery) {
        courseCardList.clear();
        List<Course> courses = courseRepository.getCoursesByUserId(userId, statusFilter, searchQuery);

        List<CourseCardData> tempList = new ArrayList<>();
        for (Course course : courses) {
            String programName = programRepository.getProgramNameById(course.getProgramId(), isArabicLocale());
            if (programName.isEmpty()) {
                programName = getString(R.string.unknown_program);
            }
            String courseName = isArabicLocale() ? course.getNameAr() : course.getNameEn();
            int color = resolveCourseColor(course.getColor());
            CourseCardData cardData = new CourseCardData(
                    course.getCourseId(),
                    course.getCode(),
                    programName,
                    courseName,
                    course.isNew(),
                    course.getStatus().equals("Registered"),
                    course.getStatus().equals("Passed"),
                    course.getStatus().equals("Remaining"),
                    color
            );
            tempList.add(cardData);
        }

        courseCardList.addAll(arrangeCoursesWithColorConstraints(tempList));
        adapter.notifyDataSetChanged();
    }

    /**
     * تحديد لون المقرر
     * @param colorName اسم اللون
     * @return معرف اللون
     */
    private int resolveCourseColor(String colorName) {
        if (colorName != null && !colorName.isEmpty()) {
            int colorResId = getResources().getIdentifier(colorName, "color", requireContext().getPackageName());
            if (colorResId != 0) {
                return ContextCompat.getColor(requireContext(), colorResId);
            }
        }
        return ContextCompat.getColor(requireContext(), R.color.Custom_MainColorBlue);
    }

    /**
     * ترتيب المقررات مع مراعاة الألوان
     * @param courses قائمة المقررات
     * @return القائمة المرتبة
     */
    private List<CourseCardData> arrangeCoursesWithColorConstraints(List<CourseCardData> courses) {
        if (courses.size() <= 1) {
            return courses;
        }

        String fullOrderString = orderPrefs.getString(PREF_ORDER_KEY, "");
        if (fullOrderString.isEmpty()) {
            List<Course> allCourses = courseRepository.getCoursesByUserId(userId, "", "");
            List<CourseCardData> fullList = new ArrayList<>();
            for (Course course : allCourses) {
                String programName = programRepository.getProgramNameById(course.getProgramId(), isArabicLocale());
                if (programName.isEmpty()) {
                    programName = getString(R.string.unknown_program);
                }
                String courseName = isArabicLocale() ? course.getNameAr() : course.getNameEn();
                int color = resolveCourseColor(course.getColor());
                CourseCardData cardData = new CourseCardData(
                        course.getCourseId(),
                        course.getCode(),
                        programName,
                        courseName,
                        course.isNew(),
                        course.getStatus().equals("Registered"),
                        course.getStatus().equals("Passed"),
                        course.getStatus().equals("Remaining"),
                        color
                );
                fullList.add(cardData);
            }
            List<CourseCardData> persistentOrder = generateRandomOrderWithColorConstraints(fullList);
            fullOrderString = createOrderString(persistentOrder);
            orderPrefs.edit().putString(PREF_ORDER_KEY, fullOrderString).apply();
        }

        String[] orderArray = fullOrderString.split(",");
        List<Long> fullOrder = new ArrayList<>();
        for (String idStr : orderArray) {
            try {
                fullOrder.add(Long.parseLong(idStr));
            } catch (NumberFormatException e) {
                showToast(R.string.invalid_course_order);
            }
        }

        List<CourseCardData> sorted = new ArrayList<>(courses);
        Collections.sort(sorted, (c1, c2) -> {
            int idx1 = fullOrder.indexOf(c1.getCourseId());
            int idx2 = fullOrder.indexOf(c2.getCourseId());
            if (idx1 == -1) idx1 = Integer.MAX_VALUE;
            if (idx2 == -1) idx2 = Integer.MAX_VALUE;
            return Integer.compare(idx1, idx2);
        });
        return sorted;
    }

    /**
     * توليد ترتيب عشوائي مع مراعاة الألوان
     * @param courses قائمة المقررات
     * @return القائمة المرتبة
     */
    private List<CourseCardData> generateRandomOrderWithColorConstraints(List<CourseCardData> courses) {
        List<CourseCardData> remaining = new ArrayList<>(courses);
        List<CourseCardData> shuffledResult = new ArrayList<>();
        Collections.shuffle(remaining);
        while (!remaining.isEmpty()) {
            boolean added = false;
            for (int i = 0; i < remaining.size(); i++) {
                CourseCardData candidate = remaining.get(i);
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
        return shuffledResult;
    }

    /**
     * إنشاء سلسلة تمثل ترتيب المقررات
     * @param orderList قائمة المقررات
     * @return السلسلة النصية
     */
    private String createOrderString(List<CourseCardData> orderList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < orderList.size(); i++) {
            sb.append(orderList.get(i).getCourseId());
            if (i < orderList.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
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
     * تحديث المقررات عند استئناف الفراغمنت
     */
    @Override
    public void onResume() {
        super.onResume();
        if (userId != -1) {
            loadCourses(currentStatusFilter, currentSearchQuery);
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