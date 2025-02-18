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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CoursesFragment extends Fragment {

    private static final String TAG = "CoursesFragment";
    private RecyclerView recyclerView;
    private CourseCardAdapter adapter;
    private List<CourseCardData> courseCardList;
    private boolean isListView = false;
    private LinearLayout statusBtnLayout;
    private MaterialButton cardsBtn, listBtn, filterBtn, passedBtn, registeredBtn, remainingBtn;
    private TextInputEditText searchBar;
    private TextInputLayout textInputLayout;
    private CourseRepository courseRepository;
    private AcademicProgramRepository programRepository;
    private long userId;
    private String currentStatusFilter = "";
    private String currentSearchQuery = "";
    private SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // تهيئة المكونات
        searchBar = view.findViewById(R.id.search_bar);
        textInputLayout = view.findViewById(R.id.outlinedTextField);
        recyclerView = view.findViewById(R.id.recycler_view);
        cardsBtn = view.findViewById(R.id.cardsBtn);
        listBtn = view.findViewById(R.id.listBtn);
        filterBtn = view.findViewById(R.id.filterBtn);
        statusBtnLayout = view.findViewById(R.id.statusBtn);
        passedBtn = view.findViewById(R.id.passedBtn);
        registeredBtn = view.findViewById(R.id.registeredBtn);
        remainingBtn = view.findViewById(R.id.remainingBtn);

        // تهيئة المستودعات
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        courseRepository = new CourseRepository(requireContext());
        programRepository = new AcademicProgramRepository(dbHelper);

        // تهيئة SharedPreferences
        preferences = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);

        // جلب userId من SharedPreferences
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userId = userPrefs.getLong("user_id", -1);

        // استرجاع حالة العرض من SharedPreferences
        isListView = preferences.getString("view_mode", "cards").equals("list");

        // إعداد قائمة أزرار التصفية
        MaterialButton[] statusButtons = {passedBtn, registeredBtn, remainingBtn};
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.dark_grey);

        cardsBtn.setSelected(!isListView);
        listBtn.setSelected(isListView);
        searchBar.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        // زر الفلتر: إظهار / إخفاء أزرار التصفية وإعادة تحميل البيانات
        filterBtn.setOnClickListener(v -> {
            if (statusBtnLayout.getVisibility() == View.GONE) {
                statusBtnLayout.setVisibility(View.VISIBLE);
                filterBtn.setSelected(true);
                filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.md_theme_primary));
                for (MaterialButton btn : statusButtons) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
                }
                currentStatusFilter = "";
                loadCourses(currentStatusFilter, currentSearchQuery);
            } else {
                statusBtnLayout.setVisibility(View.GONE);
                filterBtn.setIconTint(AppCompatResources.getColorStateList(requireContext(), R.color.Custom_Black));
                filterBtn.setSelected(false);
                for (MaterialButton btn : statusButtons) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
                }
                currentStatusFilter = "";
                loadCourses(currentStatusFilter, currentSearchQuery);
            }
        });

        // إعداد أزرار التصفية الخاصة بالحالة (Passed, Registered, Remaining)
        for (MaterialButton button : statusButtons) {
            button.setOnClickListener(v -> {
                for (MaterialButton btn : statusButtons) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(btn == button ? selectedColor : defaultColor));
                }
                String status = button == passedBtn ? "Passed" :
                        button == registeredBtn ? "Registered" : "Remaining";
                currentStatusFilter = status;
                loadCourses(currentStatusFilter, currentSearchQuery);
            });
        }

        // إعداد البحث
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
                currentSearchQuery = v.getText().toString().trim();
                loadCourses(currentStatusFilter, currentSearchQuery);
                hideKeyboard(view);
                searchBar.clearFocus();
                return true;
            }
            return false;
        });

        // إعداد أيقونة مسح البحث
        textInputLayout.setEndIconOnClickListener(v -> {
            searchBar.setText("");
            currentSearchQuery = "";
            loadCourses(currentStatusFilter, currentSearchQuery);
        });

        // إخفاء لوحة المفاتيح عند النقر خارج الحقل
        view.setOnTouchListener((v, event) -> {
            searchBar.clearFocus();
            hideKeyboard(view);
            v.performClick();
            return false;
        });

        // إعداد الـ RecyclerView والأزرار
        setupRecyclerView();
        setupButtons();
        updateButtonStates();

        // التحقق مما إذا كانت هناك حالة تصفية مرسلة عبر الـ Bundle
        Bundle args = getArguments();
        if (args != null && args.containsKey("filter_status")) {
            String filterStatus = args.getString("filter_status");
            if (filterStatus != null && !filterStatus.isEmpty()) {
                applyFilter(filterStatus);
            }
        }

        // تحميل المقررات بناءً على الحالة الحالية
        loadCourses(currentStatusFilter, currentSearchQuery);
    }

    /**
     * تطبيق تصفية بناءً على الحالة المحددة.
     *
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

    private void setupRecyclerView() {
        courseCardList = new ArrayList<>();
        adapter = new CourseCardAdapter(courseCardList, isListView);
        updateRecyclerViewLayout();
        recyclerView.setAdapter(adapter);
    }

    private void setupButtons() {
        cardsBtn.setOnClickListener(v -> {
            if (!isListView) return;
            isListView = false;
            adapter.setListView(false);
            updateRecyclerViewLayout();
            adapter.notifyDataSetChanged();
            updateButtonStates();
            preferences.edit().putString("view_mode", "cards").apply();
        });

        listBtn.setOnClickListener(v -> {
            if (isListView) return;
            isListView = true;
            adapter.setListView(true);
            updateRecyclerViewLayout();
            adapter.notifyDataSetChanged();
            updateButtonStates();
            preferences.edit().putString("view_mode", "list").apply();
        });
    }

    private void updateRecyclerViewLayout() {
        if (isListView) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), calculateNoOfColumns(400)));
        }
        recyclerView.setAdapter(adapter);
    }

    private int calculateNoOfColumns(int columnWidthDp) {
        float screenWidthDp = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().density;
        return (int) (screenWidthDp / columnWidthDp + 0.5);
    }

    private void updateButtonStates() {
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

    private boolean isArabicLocale() {
        String selectedLanguage = preferences.getString("selected_language", "en");
        return "ar".equals(selectedLanguage);
    }

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
            int color;
            String colorName = course.getColor();
            Log.d(TAG, "Course ID: " + course.getCourseId() + ", Color Name: " + colorName);
            if (colorName != null && !colorName.isEmpty()) {
                int colorResId = getResources().getIdentifier(colorName, "color", requireContext().getPackageName());
                if (colorResId != 0) {
                    color = ContextCompat.getColor(requireContext(), colorResId);
                } else {
                    Log.w(TAG, "Color resource not found for name: " + colorName + ", Course ID: " + course.getCourseId());
                    color = ContextCompat.getColor(requireContext(), R.color.Custom_MainColorBlue);
                }
            } else {
                Log.w(TAG, "Color name is null or empty for Course ID: " + course.getCourseId());
                color = ContextCompat.getColor(requireContext(), R.color.Custom_MainColorBlue);
            }
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

        // إعادة ترتيب المقررات بناءً على الترتيب الدائم
        // الدالة arrangeCoursesWithColorConstraints تقوم بإعادة ترتيب القائمة المُمررة استنادًا إلى ترتيب "full_course_order"
        courseCardList.addAll(arrangeCoursesWithColorConstraints(tempList));
        adapter.notifyDataSetChanged();
    }

    /**
     * تقوم هذه الدالة بإرجاع القائمة المرتبة بناءً على ترتيب دائم للمقررات (يُخزن في SharedPreferences).
     * إذا كانت قائمة المقررات المُمررة (tempList) نتيجة فلترة، فإن ترتيب العناصر الفرعية يتم وفق ترتيب الدائم للمقررات.
     */
    private List<CourseCardData> arrangeCoursesWithColorConstraints(List<CourseCardData> courses) {
        if (courses.size() <= 1) {
            return courses;
        }

        SharedPreferences orderPrefs = requireActivity().getSharedPreferences("CourseOrderPrefs", Context.MODE_PRIVATE);
        String fullOrderString = orderPrefs.getString("full_course_order", "");

        // إذا لم يُحفظ ترتيب دائم بعد، نحصل على القائمة الكاملة من قاعدة البيانات ونولد ترتيبًا عشوائيًا مع مراعاة خلفيات العناصر
        if (fullOrderString.isEmpty()) {
            List<Course> allCoursesDb = courseRepository.getCoursesByUserId(userId, "", "");
            List<CourseCardData> fullList = new ArrayList<>();
            for (Course course : allCoursesDb) {
                String programName = programRepository.getProgramNameById(course.getProgramId(), isArabicLocale());
                if (programName.isEmpty()) {
                    programName = getString(R.string.unknown_program);
                }
                String courseName = isArabicLocale() ? course.getNameAr() : course.getNameEn();
                int color;
                String colorName = course.getColor();
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
            orderPrefs.edit().putString("full_course_order", fullOrderString).apply();
        }

        // تحويل الترتيب الدائم (fullOrderString) إلى قائمة من المعرفات
        String[] orderArray = fullOrderString.split(",");
        List<Long> fullOrder = new ArrayList<>();
        for (String idStr : orderArray) {
            try {
                fullOrder.add(Long.parseLong(idStr));
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid id in persistent order: " + idStr);
            }
        }

        // ترتيب القائمة المُمررة (سواء فلترة أو لا) بناءً على مواقع العناصر في الترتيب الدائم
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

    // دالة توليد ترتيب عشوائي مع مراعاة عدم تكرار لون الخلفية متجاوراً
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

    // دالة لإنشاء سلسلة نصية تمثل ترتيب المقررات باستخدام courseId مفصول بفواصل
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

    private void hideKeyboard(View view) {
        if (getActivity() != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void showKeyboard(View view) {
        if (getActivity() != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // عند كل عودة للـ Fragment نعيد تحميل الإحصائيات
        loadCourses(currentStatusFilter, currentSearchQuery);
    }
}