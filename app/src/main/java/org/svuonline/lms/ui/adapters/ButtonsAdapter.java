package org.svuonline.lms.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.svuonline.lms.databinding.ItemButtonSectionBinding;
import org.svuonline.lms.ui.activities.FilesActivity;
import org.svuonline.lms.ui.activities.ParticipantsActivity;
import org.svuonline.lms.ui.data.ButtonData;

import java.util.List;

public class ButtonsAdapter extends RecyclerView.Adapter<ButtonsAdapter.ButtonViewHolder> {
    private final List<ButtonData> buttons;
    private final Context context;
    private String courseCode;
    private String courseTitle;
    private int courseColor;

    public ButtonsAdapter(Context context, List<ButtonData> buttons, String courseCode, String courseTitle, int courseColor) {
        this.context = context;
        this.buttons = buttons;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.courseColor = courseColor;
    }

    @NonNull
    @Override
    public ButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemButtonSectionBinding binding = ItemButtonSectionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ButtonViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ButtonViewHolder holder, int position) {
        ButtonData button = buttons.get(position);
        holder.bind(button);
    }

    @Override
    public int getItemCount() {
        return buttons.size();
    }

    class ButtonViewHolder extends RecyclerView.ViewHolder {
        private final ItemButtonSectionBinding binding;

        ButtonViewHolder(ItemButtonSectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ButtonData button) {
            binding.buttonSection.setText(button.getLabel());
            binding.buttonSection.setBackgroundTintList(ColorStateList.valueOf(button.getButtonColor()));


            // إضافة مستمع للنقر على الزر
            binding.buttonSection.setOnClickListener(v -> {
                Intent intent;
                if (button.getButtonId().equals("participants_button")) {
                    // فتح ParticipantsActivity
                    intent = new Intent(context, ParticipantsActivity.class);
                } else {
                    // فتح FilesActivity للأزرار الأخرى
                    intent = new Intent(context, FilesActivity.class);
                }
                // تمرير البيانات مع الـ Intent
                intent.putExtra("button_id", button.getButtonId());
                intent.putExtra("course_code", courseCode);
                intent.putExtra("course_title", courseTitle);
                intent.putExtra("course_color_value", courseColor);
                intent.putExtra("button_label", button.getLabel());
                context.startActivity(intent);
            });
        }
    }
}
