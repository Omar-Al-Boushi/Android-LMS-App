package org.svuonline.lms.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.svuonline.lms.R;
import org.svuonline.lms.data.db.DatabaseHelper;
import org.svuonline.lms.data.model.Assignment;
import org.svuonline.lms.data.model.AssignmentSubmission;
import org.svuonline.lms.data.repository.AssignmentRepository;
import org.svuonline.lms.data.repository.AssignmentSubmissionRepository;
import org.svuonline.lms.data.repository.EnrollmentRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * فراغمنت لعرض إحصائيات المقررات والواجبات في لوحة التحكم.
 */
public class DashboardFragment extends Fragment {

    // واجهات للتواصل مع DashboardActivity
    public interface OnCourseFilterListener {
        void onCourseFilterSelected(String status);
    }

    public interface OnAssignmentFilterListener {
        void onAssignmentFilterSelected(String filter);
    }

    // عناصر واجهة المستخدم
    private TextView numberPassed;
    private TextView numberRegistered;
    private TextView numberRemaining;
    private TextView numberCompletedCircular;
    private TextView numberCircularRemaining;
    private TextView numberAssignment;
    private TextView numberCompletedAssignment;
    private TextView numberRemainingAssignment;
    private TextView numberDaysLeft;
    private CircularProgressBar circularProgressBarCompleted;
    private CircularProgressBar circularProgressBarRemaining;
    private CircularProgressBar circularProgressBarAssignmentCompleted;
    private LinearLayout numberPassedCard;
    private LinearLayout numberRegisteredCard;
    private LinearLayout numberRemainingCard;
    private ConstraintLayout constraintLayoutAllAssignments;
    private ConstraintLayout constraintLayoutCompletedAssignments;
    private ConstraintLayout constraintLayoutProgressAssignments;

    // المستودعات
    private EnrollmentRepository enrollmentRepository;
    private AssignmentRepository assignmentRepository;
    private AssignmentSubmissionRepository assignmentSubmissionRepository;

    // بيانات الفراغمنت
    private int userId;
    private SharedPreferences preferences;
    private OnCourseFilterListener courseFilterListener;
    private OnAssignmentFilterListener assignmentFilterListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCourseFilterListener) {
            courseFilterListener = (OnCourseFilterListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnCourseFilterListener");
        }
        if (context instanceof OnAssignmentFilterListener) {
            assignmentFilterListener = (OnAssignmentFilterListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnAssignmentFilterListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // تهيئة المكونات
        initComponents();

        // تهيئة الواجهة
        initViews(view);

        // التحقق من بيانات المستخدم
        if (!validateUserData()) {
            return view;
        }

        // تهيئة البيانات
        initData();

        // إعداد مستمعات الأحداث
        setupListeners();

        return view;
    }

    /**
     * تهيئة المستودعات
     */
    private void initComponents() {
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        enrollmentRepository = new EnrollmentRepository(requireContext());
        assignmentRepository = new AssignmentRepository(requireContext());
        assignmentSubmissionRepository = new AssignmentSubmissionRepository(requireContext());
        preferences = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
    }

    /**
     * تهيئة عناصر الواجهة
     * @param view الواجهة المراد تهيئتها
     */
    private void initViews(View view) {
        numberPassed = view.findViewById(R.id.numberPassed);
        numberRegistered = view.findViewById(R.id.numberRegistered);
        numberRemaining = view.findViewById(R.id.numberRemaining);
        numberCompletedCircular = view.findViewById(R.id.numberCompletedCircular);
        numberCircularRemaining = view.findViewById(R.id.numberCircularRemaining);
        numberAssignment = view.findViewById(R.id.numberAssignment);
        numberCompletedAssignment = view.findViewById(R.id.numberCompletedAssignment);
        numberRemainingAssignment = view.findViewById(R.id.numberRemainingAssignment);
        numberDaysLeft = view.findViewById(R.id.numberDaysLeft);
        circularProgressBarCompleted = view.findViewById(R.id.circularProgressBarCompleted);
        circularProgressBarRemaining = view.findViewById(R.id.circularProgressBarRemaining);
        circularProgressBarAssignmentCompleted = view.findViewById(R.id.circularProgressBarAssignmentCompleted);
        numberPassedCard = view.findViewById(R.id.numberPassedCard);
        numberRegisteredCard = view.findViewById(R.id.numberRegisteredCard);
        numberRemainingCard = view.findViewById(R.id.numberRemainingCard);
        constraintLayoutAllAssignments = view.findViewById(R.id.constraintLayout6);
        constraintLayoutCompletedAssignments = view.findViewById(R.id.constraintLayout5);
        constraintLayoutProgressAssignments = view.findViewById(R.id.constraintLayout12);
    }

    /**
     * التحقق من صحة بيانات المستخدم
     * @return صحيح إذا كانت البيانات صالحة
     */
    private boolean validateUserData() {
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userId = (int) userPrefs.getLong("user_id", -1);
        return userId != -1;
    }

    /**
     * تهيئة البيانات (إحصائيات المقررات والواجبات)
     */
    private void initData() {
        updateCourseStats();
        updateAssignmentStats();
        updateDaysLeft();
    }

    /**
     * إعداد مستمعات الأحداث (تصفية المقررات والواجبات)
     */
    private void setupListeners() {
        numberPassedCard.setOnClickListener(v -> courseFilterListener.onCourseFilterSelected("Passed"));
        numberRegisteredCard.setOnClickListener(v -> courseFilterListener.onCourseFilterSelected("Registered"));
        numberRemainingCard.setOnClickListener(v -> courseFilterListener.onCourseFilterSelected("Remaining"));
        constraintLayoutAllAssignments.setOnClickListener(v -> assignmentFilterListener.onAssignmentFilterSelected("All"));
        constraintLayoutCompletedAssignments.setOnClickListener(v -> assignmentFilterListener.onAssignmentFilterSelected("Completed"));
        constraintLayoutProgressAssignments.setOnClickListener(v -> assignmentFilterListener.onAssignmentFilterSelected("Progress"));
    }

    /**
     * تحديث إحصائيات المقررات
     */
    private void updateCourseStats() {
        int passedCount = enrollmentRepository.getEnrollmentCountByStatus(userId, "Passed");
        int registeredCount = enrollmentRepository.getEnrollmentCountByStatus(userId, "Registered");
        int remainingCount = enrollmentRepository.getEnrollmentCountByStatus(userId, "Remaining");

        int totalCourses = passedCount + registeredCount + remainingCount;
        float completedPercentage = totalCourses > 0 ? (passedCount * 100.0f) / totalCourses : 0;
        float remainingPercentage = totalCourses > 0 ? 100 - completedPercentage : 0;

        numberPassed.setText(String.valueOf(passedCount));
        numberRegistered.setText(String.valueOf(registeredCount));
        numberRemaining.setText(String.valueOf(remainingCount));
        numberCompletedCircular.setText(String.format(Locale.US, "%.0f%%", completedPercentage));
        numberCircularRemaining.setText(String.format(Locale.US, "%.0f%%", remainingPercentage));

        circularProgressBarCompleted.setProgress(completedPercentage);
        circularProgressBarRemaining.setProgress(remainingPercentage * -1);
    }

    /**
     * تحديث إحصائيات الواجبات
     */
    private void updateAssignmentStats() {
        boolean isArabic = isArabicLocale();
        List<Assignment> allAssignments = assignmentRepository.getAssignmentsByUserId(userId, "", "", isArabic);

        List<Assignment> registeredAssignments = new ArrayList<>();
        for (Assignment assignment : allAssignments) {
            if (enrollmentRepository.isUserEnrolledInCourse(userId, assignment.getCourseCode())) {
                registeredAssignments.add(assignment);
            }
        }

        int totalAssignments = registeredAssignments.size();
        int completedAssignments = 0;
        for (Assignment assignment : registeredAssignments) {
            AssignmentSubmission submission = assignmentSubmissionRepository.getSubmissionStatus(
                    assignment.getAssignmentId(), userId);
            if (submission != null && "Submitted".equalsIgnoreCase(submission.getStatus())) {
                completedAssignments++;
            }
        }
        int remainingAssignments = totalAssignments - completedAssignments;
        float completedPercentage = totalAssignments > 0 ? (completedAssignments * 100f) / totalAssignments : 0f;

        numberAssignment.setText(String.valueOf(totalAssignments));
        numberCompletedAssignment.setText(String.valueOf(completedAssignments));
        numberRemainingAssignment.setText(String.valueOf(remainingAssignments));
        circularProgressBarAssignmentCompleted.setProgress(completedPercentage);
    }

    /**
     * تحديث عدد الأيام المتبقية للواجب الأقرب
     */
    private void updateDaysLeft() {
        Assignment nearestAssignment = assignmentRepository.getNearestDueAssignment(userId);
        if (nearestAssignment == null) {
            numberDaysLeft.setText("0");
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date dueDate = sdf.parse(nearestAssignment.getDueDate());
            Date today = Calendar.getInstance().getTime();

            long diffInMillies = dueDate.getTime() - today.getTime();
            long daysLeft = diffInMillies / (1000 * 60 * 60 * 24);

            numberDaysLeft.setText(String.valueOf(daysLeft >= 0 ? daysLeft : 0));
        } catch (Exception e) {
            showToast(R.string.date_parse_error);
            numberDaysLeft.setText("0");
        }
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
     * تحديث الإحصائيات عند استئناف الفراغمنت
     */
    @Override
    public void onResume() {
        super.onResume();
        if (userId != -1) {
            updateCourseStats();
            updateAssignmentStats();
            updateDaysLeft();
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