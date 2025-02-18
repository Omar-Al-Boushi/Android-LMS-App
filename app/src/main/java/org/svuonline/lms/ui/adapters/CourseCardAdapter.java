package org.svuonline.lms.ui.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.svuonline.lms.R;
import org.svuonline.lms.ui.activities.CourseDetailsActivity;
import org.svuonline.lms.ui.data.CourseCardData;

import java.util.List;

public class CourseCardAdapter extends RecyclerView.Adapter<CourseCardAdapter.ViewHolder> {
    private final List<CourseCardData> courseCardList;
    private boolean isListView; // متغير لتحديد طريقة العرض

    // Constructor
    public CourseCardAdapter(List<CourseCardData> courseCardList, boolean isListView) {
        this.courseCardList = courseCardList;
        this.isListView = isListView;
    }

    public void setListView(boolean isListView) {
        this.isListView = isListView;
        notifyDataSetChanged(); // تحديث البيانات عند تغيير طريقة العرض
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = isListView ? R.layout.item_list_courses : R.layout.item_cards_courses;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CourseCardData courseCard = courseCardList.get(position);

        holder.tvCourseCode.setText(courseCard.getCourseCode());
        holder.tvCourseProgram.setText(courseCard.getCourseProgram());
        holder.tvCourseName.setText(courseCard.getCourseName());

        holder.containerCourseHeader.setBackgroundColor(courseCard.getBackgroundColor());

        holder.btnCourseNew.setVisibility(courseCard.isNew() ? View.VISIBLE : View.GONE);
        holder.btnCourseRegistered.setVisibility(courseCard.isRegistered() ? View.VISIBLE : View.GONE);
        holder.btnCoursePassed.setVisibility(courseCard.isPassed() ? View.VISIBLE : View.GONE);
        holder.btnCourseRemaining.setVisibility(courseCard.isRemaining() ? View.VISIBLE : View.GONE);

// داخل onBindViewHolder في CourseCardAdapter
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CourseDetailsActivity.class);
            intent.putExtra("course_code", courseCard.getCourseCode());
            intent.putExtra("course_name", courseCard.getCourseName());
            intent.putExtra("course_color", courseCard.getBackgroundColor());
            Log.d("CourseColor", "Color sent: " + courseCard.getBackgroundColor());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return courseCardList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCourseCode, tvCourseProgram, tvCourseName;
        private final Button btnCourseNew, btnCourseRegistered, btnCoursePassed, btnCourseRemaining;
        private final ConstraintLayout containerCourseHeader;

        public ViewHolder(View view) {
            super(view);

            tvCourseCode = view.findViewById(R.id.tv_course_code);
            tvCourseProgram = view.findViewById(R.id.tv_course_program);
            tvCourseName = view.findViewById(R.id.tv_course_name);

            btnCourseNew = view.findViewById(R.id.btn_course_new);
            btnCourseRegistered = view.findViewById(R.id.btn_course_registered);
            btnCoursePassed = view.findViewById(R.id.btn_course_passed);
            btnCourseRemaining = view.findViewById(R.id.btn_course_remaining);

            containerCourseHeader = view.findViewById(R.id.container_course_header);
        }
    }
}
