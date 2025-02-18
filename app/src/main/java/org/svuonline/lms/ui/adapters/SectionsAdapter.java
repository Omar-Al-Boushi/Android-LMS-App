package org.svuonline.lms.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.svuonline.lms.databinding.ItemSectionCourseBinding;
import org.svuonline.lms.ui.data.SectionData;

import java.util.List;

public class SectionsAdapter extends RecyclerView.Adapter<SectionsAdapter.SectionViewHolder> {
    private final List<SectionData> sections;
    private final Context context;
    private String courseCode;
    private String courseTitle;
    private int courseColor;

    public SectionsAdapter(Context context, List<SectionData> sections, String courseCode, String courseTitle, int courseColor) {
        this.context = context;
        this.sections = sections;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.courseColor = courseColor;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSectionCourseBinding binding = ItemSectionCourseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new SectionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        SectionData section = sections.get(position);
        holder.bind(section);
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    class SectionViewHolder extends RecyclerView.ViewHolder {
        private final ItemSectionCourseBinding binding;

        SectionViewHolder(ItemSectionCourseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SectionData section) {
            binding.sectionTitle.setText(section.getTitle());

            // تمرير المعطيات الإضافية إلى ButtonsAdapter
            ButtonsAdapter buttonsAdapter = new ButtonsAdapter(
                    context,
                    section.getButtons(),
                    courseCode,
                    courseTitle,
                    courseColor
            );
            binding.buttonsSectionRecyclerView.setLayoutManager(new GridLayoutManager(itemView.getContext(), 2));
            binding.buttonsSectionRecyclerView.setAdapter(buttonsAdapter);
        }
    }
}
