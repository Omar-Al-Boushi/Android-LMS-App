package org.svuonline.lms.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import org.svuonline.lms.R;
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
    }

    @Override
    public int getItemCount() {
        return assignmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseName, courseCode, timeStart, timeEnd, status;
        ConstraintLayout cardView;
        ConstraintLayout parentStatus;  // إضافة حقل parentStatus

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseName = itemView.findViewById(R.id.courseName);
            courseCode = itemView.findViewById(R.id.courseCode);
            timeStart = itemView.findViewById(R.id.timeStart);
            timeEnd = itemView.findViewById(R.id.timeEnd);
            status = itemView.findViewById(R.id.status);
            cardView = itemView.findViewById(R.id.container_assignment_card);
            parentStatus = itemView.findViewById(R.id.parentStatus);  // العثور على parentStatus
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
