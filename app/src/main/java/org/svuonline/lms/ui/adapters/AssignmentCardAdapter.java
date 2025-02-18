package org.svuonline.lms.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.svuonline.lms.R;
import org.svuonline.lms.data.repository.AssignmentRepository;
import org.svuonline.lms.ui.activities.AssignmentUploadActivity;
import org.svuonline.lms.ui.activities.AssignmentsActivity;
import org.svuonline.lms.ui.data.AssignmentCardData;
import java.util.List;

public class AssignmentCardAdapter extends RecyclerView.Adapter<AssignmentCardAdapter.ViewHolder> {
    private final Context context;
    private final List<AssignmentCardData> assignmentList;

    public AssignmentCardAdapter(Context context, List<AssignmentCardData> assignmentList) {
        this.context = context;
        this.assignmentList = assignmentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssignmentCardData assignment = assignmentList.get(position);
        holder.courseName.setText(assignment.getCourseName());
        holder.courseCode.setText(assignment.getCourseCode());
        holder.timeStart.setText(assignment.getTimeStart());
        holder.timeEnd.setText(assignment.getTimeEnd());
        holder.status.setText(assignment.getStatus());
        holder.cardView.setBackgroundColor(assignment.getBackgroundColor());

        // تعديل الدوران بناءً على اتجاه النص
        boolean isRTL = context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        adjustRotation(holder.parentStatus, isRTL);

        holder.goDetails.setOnClickListener(v -> {
            AssignmentRepository assignmentRepository = new AssignmentRepository(context);
            String toolId = assignmentRepository.getToolIdByAssignmentId(assignment.getAssignmentId());

            if (toolId == null) {
                Log.e("AssignmentCardAdapter", "لم يتم العثور على tool_id لـ assignment_id: " + assignment.getAssignmentId());
                Snackbar.make(holder.itemView, "خطأ: الأداة غير متوفرة", Snackbar.LENGTH_LONG).show();
                return;
            }

            Intent intent = new Intent(context, AssignmentsActivity.class);
            intent.putExtra("course_code", assignment.getCourseCode());
            intent.putExtra("course_title", assignment.getCourseName());
            intent.putExtra("course_color_value", assignment.getBackgroundColor());
            intent.putExtra("button_id", toolId); // تمرير tool_id
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return assignmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseName, courseCode, timeStart, timeEnd, status;
        ConstraintLayout cardView;
        ConstraintLayout parentStatus;  // إضافة حقل parentStatus
        MaterialButton goDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseName = itemView.findViewById(R.id.courseName);
            courseCode = itemView.findViewById(R.id.courseCode);
            timeStart = itemView.findViewById(R.id.timeStart);
            timeEnd = itemView.findViewById(R.id.timeEnd);
            status = itemView.findViewById(R.id.status);
            cardView = itemView.findViewById(R.id.container_assignment_card);
            parentStatus = itemView.findViewById(R.id.parentStatus);  // العثور على parentStatus
            goDetails = itemView.findViewById(R.id.goDetailsBtn);
        }
    }

    private void adjustRotation(View view, boolean isRTL) {
        if (isRTL) {
            view.setRotation(-45);  // عكس التدوير للمحاذاة من اليمين لليسار
        } else {
            view.setRotation(45);   // التدوير الطبيعي للمحاذاة من اليسار لليمين
        }
    }
}
