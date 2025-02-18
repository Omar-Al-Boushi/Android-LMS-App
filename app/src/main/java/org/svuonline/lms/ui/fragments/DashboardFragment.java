package org.svuonline.lms.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import org.svuonline.lms.data.model.AssignmentSubmission;
import org.svuonline.lms.data.repository.AssignmentRepository;
import org.svuonline.lms.data.repository.AssignmentSubmissionRepository;
import org.svuonline.lms.data.repository.EnrollmentRepository;
import org.svuonline.lms.data.model.Assignment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView numberPassed, numberRegistered, numberRemaining;
    private TextView numberCompletedCircular, numberCircularRemaining;
    private TextView numberAssignment, numberCompletedAssignment, numberRemainingAssignment, numberDaysLeft;
    private CircularProgressBar circularProgressBarCompleted, circularProgressBarRemaining, circularProgressBarAssignmentCompleted;
    private LinearLayout numberRemainingCard, numberRegisteredCard, numberPassedCard;
    private ConstraintLayout constraintLayout6, constraintLayout5, constraintLayout12;

    private EnrollmentRepository enrollmentRepository;
    private AssignmentRepository assignmentRepository;
    AssignmentSubmissionRepository assignmentSubmissionRepository;
    private int userId;
    private SharedPreferences preferences;

    // واجهة للتواصل مع DashboardActivity
    public interface OnCourseFilterListener {
        void onCourseFilterSelected(String status);
    }
    // واجهة جديدة لتصفية الوظائف
    public interface OnAssignmentFilterListener {
        void onAssignmentFilterSelected(String filter);
    }

    private OnCourseFilterListener filterListener;
    private OnAssignmentFilterListener assignmentFilterListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCourseFilterListener) {
            filterListener = (OnCourseFilterListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnCourseFilterListener");
        }
        if (context instanceof OnAssignmentFilterListener) {
            assignmentFilterListener = (OnAssignmentFilterListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnAssignmentFilterListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // استرجاع user_id وتفضيلات اللغة من SharedPreferences
        assert getActivity() != null;
        SharedPreferences userPrefs = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userId = (int) userPrefs.getLong("user_id", 1L);

        preferences = getActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);

        // تهيئة العناصر من الـ XML
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
        constraintLayout6 = view.findViewById(R.id.constraintLayout6);
        constraintLayout5 = view.findViewById(R.id.constraintLayout5);
        constraintLayout12 = view.findViewById(R.id.constraintLayout12);

        // تهيئة Repository
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        enrollmentRepository = new EnrollmentRepository(getContext());
        assignmentRepository = new AssignmentRepository(getContext());
        assignmentSubmissionRepository = new AssignmentSubmissionRepository(getContext());


        // إعداد معالجات النقر لعناصر التصفية
        numberPassedCard.setOnClickListener(v -> filterListener.onCourseFilterSelected("Passed"));
        numberRegisteredCard.setOnClickListener(v -> filterListener.onCourseFilterSelected("Registered"));
        numberRemainingCard.setOnClickListener(v -> filterListener.onCourseFilterSelected("Remaining"));
        // إعداد معالجات النقر لعناصر تصفية الوظائف
        constraintLayout6.setOnClickListener(v -> assignmentFilterListener.onAssignmentFilterSelected("All"));
        constraintLayout5.setOnClickListener(v -> assignmentFilterListener.onAssignmentFilterSelected("Completed"));
        constraintLayout12.setOnClickListener(v -> assignmentFilterListener.onAssignmentFilterSelected("Progress"));

        // جلب البيانات وتحديث الواجهة
        updateCourseStats();
        updateAssignmentStats();
        updateDaysLeft();

        return view;
    }

    private void updateCourseStats() {
        // جلب عدد المقررات حسب الحالة
        int passedCount = enrollmentRepository.getEnrollmentCountByStatus(userId, "Passed");
        int registeredCount = enrollmentRepository.getEnrollmentCountByStatus(userId, "Registered");
        int remainingCount = enrollmentRepository.getEnrollmentCountByStatus(userId, "Remaining");

        // حساب النسب المئوية
        int totalCourses = passedCount + registeredCount + remainingCount;
        float completedPercentage = totalCourses > 0 ? (passedCount * 100.0f) / totalCourses : 0;
        float remainingPercentage = totalCourses > 0 ? 100 - completedPercentage : 0;

        // تحديث النصوص باستخدام الأرقام الإنجليزية
        numberPassed.setText(String.valueOf(passedCount));
        numberRegistered.setText(String.valueOf(registeredCount));
        numberRemaining.setText(String.valueOf(remainingCount));
        numberCompletedCircular.setText(String.format(Locale.US, "%.0f%%", completedPercentage));
        numberCircularRemaining.setText(String.format(Locale.US, "%.0f%%", remainingPercentage));

        // تحديث دوائر التقدم
        circularProgressBarCompleted.setProgress(completedPercentage);
        circularProgressBarRemaining.setProgress(remainingPercentage * -1);
    }

    private void updateAssignmentStats() {
        // 1. جلب كل الواجبات
        boolean isArabic = isArabicLocale();
        List<Assignment> allAssignments =
                assignmentRepository.getAssignmentsByUserId(userId, "", "", isArabic);

        // 2. تصفية الواجبات بحسب التسجيل (Registered) باستخدام isUserEnrolledInCourse
        List<Assignment> registeredAssignments = new ArrayList<>();
        for (Assignment a : allAssignments) {
            // isUserEnrolledInCourse يفحص حالة التسجيل == "Registered"
            if (enrollmentRepository.isUserEnrolledInCourse(userId, a.getCourseCode())) {
                registeredAssignments.add(a);
            }
        }

        // 3. حساب الإحصائيات على الواجبات المصفاة
        int totalAssignments     = registeredAssignments.size();
        int completedAssignments = 0;
        for (Assignment a : registeredAssignments) {
            // اعتبر أنه "مُنجَز" إذا وجدنا إرسالاً بحالة "Submitted" أو حسب منطقك
            AssignmentSubmission sub = assignmentSubmissionRepository.getSubmissionStatus(a.getAssignmentId(), userId);
            if (sub != null && "Submitted".equalsIgnoreCase(sub.getStatus())) {
                completedAssignments++;
            }
        }
        int remainingAssignments = totalAssignments - completedAssignments;
        float completedPercentage =
                totalAssignments > 0
                        ? (completedAssignments * 100f) / totalAssignments
                        : 0f;

        // 4. تحديث الواجهة بالأرقام الإنجليزية
        numberAssignment.setText(String.valueOf(totalAssignments));
        numberCompletedAssignment.setText(String.valueOf(completedAssignments));
        numberRemainingAssignment.setText(String.valueOf(remainingAssignments));
        circularProgressBarAssignmentCompleted.setProgress(completedPercentage);
    }



    private void updateDaysLeft() {
        // جلب الواجب الأقرب لتاريخ التسليم
        Assignment nearestAssignment = assignmentRepository.getNearestDueAssignment(userId);
        if (nearestAssignment != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date dueDate = sdf.parse(nearestAssignment.getDueDate());
                Date today = Calendar.getInstance().getTime();

                // حساب عدد الأيام المتبقية
                long diffInMillies = dueDate.getTime() - today.getTime();
                long daysLeft = diffInMillies / (1000 * 60 * 60 * 24);

                // تحديث النص باستخدام الأرقام الإنجليزية
                numberDaysLeft.setText(String.valueOf(daysLeft >= 0 ? daysLeft : 0));
            } catch (ParseException e) {
                e.printStackTrace();
                numberDaysLeft.setText("0");
            }
        } else {
            numberDaysLeft.setText("0");
        }
    }

    private boolean isArabicLocale() {
        String selectedLanguage = preferences.getString("selected_language", "en");
        return "ar".equals(selectedLanguage);
    }

    @Override
    public void onResume() {
        super.onResume();
        // عند كل عودة للـ Fragment نعيد تحميل الإحصائيات
        updateCourseStats();
        updateAssignmentStats();
        updateDaysLeft();
    }

}