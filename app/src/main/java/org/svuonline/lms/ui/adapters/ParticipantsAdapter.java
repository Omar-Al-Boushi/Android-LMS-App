package org.svuonline.lms.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import org.svuonline.lms.R;
import org.svuonline.lms.ui.activities.ProfileActivity;
import org.svuonline.lms.ui.data.ParticipantData;

import java.util.List;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ViewHolder> {

    private List<ParticipantData> participants;

    public ParticipantsAdapter(List<ParticipantData> participants) {
        this.participants = participants;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cards_participants, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ParticipantData participant = participants.get(position);
        holder.profileImage.setImageResource(participant.getImageResource());

        holder.profileName.setText(holder.itemView.getContext().getString(participant.getNameResourceId()));
        holder.profileRole.setText(holder.itemView.getContext().getString(participant.getRoleResourceId()));
        holder.profileDescription.setText(holder.itemView.getContext().getString(participant.getDescriptionResourceId()));

        // تغيير لون النص بناءً على الدور
        int roleColor;
        int roleColor2;



        if (participant.getRoleResourceId() == R.string.student_role) {
            roleColor = holder.itemView.getContext().getResources().getColor(R.color.Custom_MainColorBlue);
            roleColor2 = holder.itemView.getContext().getResources().getColor(R.color.md_theme_primary);
        } else if (participant.getRoleResourceId() == R.string.doctor_role) {
            roleColor = holder.itemView.getContext().getResources().getColor(R.color.Custom_MainColorGolden);
            roleColor2 = holder.itemView.getContext().getResources().getColor(R.color.md_theme_tertiary);
        } else if (participant.getRoleResourceId() == R.string.coordinator_role) {
            roleColor = holder.itemView.getContext().getResources().getColor(R.color.Custom_MainColorDarkPink);
            roleColor2 = holder.itemView.getContext().getResources().getColor(R.color.colorCustomColor1);
        } else if (participant.getRoleResourceId() == R.string.program_manager_role) {
            roleColor = holder.itemView.getContext().getResources().getColor(R.color.Custom_MainColorPurple);
            roleColor2 = holder.itemView.getContext().getResources().getColor(R.color.colorCustomColor2);
        } else if (participant.getRoleResourceId() == R.string.system_manager_role) {
            roleColor = holder.itemView.getContext().getResources().getColor(R.color.Custom_MainColorTeal);
            roleColor2 = holder.itemView.getContext().getResources().getColor(R.color.colorCustomColor3);
        } else {
            roleColor = holder.itemView.getContext().getResources().getColor(R.color.Custom_MainColorOrange);
            roleColor2 = holder.itemView.getContext().getResources().getColor(R.color.colorCustomColor4);
        }
        holder.profileRole.setTextColor(roleColor);

        // مستمع للنقر على العنصر للانتقال إلى صفحة البروفايل مع إرسال البيانات ديناميكياً
        // مستمع للنقر لإرسال البيانات إلى ProfileActivity
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("profile_image_res", participant.getImageResource()); // إرسال الصورة
            intent.putExtra("profile_name", context.getString(participant.getNameResourceId())); // إرسال الاسم
            intent.putExtra("profile_bio", context.getString(participant.getDescriptionResourceId())); // إرسال الوصف
            intent.putExtra("header_color", roleColor); // إرسال لون الدور
            intent.putExtra("text_color", roleColor2); // إرسال لون الدور

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView profileImage;
        TextView profileName;
        TextView profileRole;
        TextView profileDescription;

        ViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.img_profile);
            profileName = itemView.findViewById(R.id.profileName);
            profileRole = itemView.findViewById(R.id.profileRole);
            profileDescription = itemView.findViewById(R.id.profileDescription);
        }
    }
}
