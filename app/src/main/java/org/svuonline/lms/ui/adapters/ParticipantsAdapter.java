// ParticipantsAdapter.java  (إضافة تمرير profile_user_id)
package org.svuonline.lms.ui.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import org.svuonline.lms.R;
import org.svuonline.lms.ui.activities.ProfileActivity;
import org.svuonline.lms.ui.data.ParticipantData;

import java.util.List;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ViewHolder> {
    private List<ParticipantData> participants;
    private final Context context;
    private final String courseCode;
    private final boolean isArabic;

    public ParticipantsAdapter(Context context, List<ParticipantData> participants, String courseCode) {
        this.context = context;
        this.participants = participants;
        this.courseCode = courseCode;
        SharedPreferences prefs = context.getSharedPreferences("AppPreferences", MODE_PRIVATE);
        this.isArabic = "ar".equals(prefs.getString("selected_language", "en"));
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

        String pic = participant.getProfilePicture();
        if (pic != null && pic.startsWith("@drawable/")) {
            int id = context.getResources().getIdentifier(pic.substring(10), "drawable", context.getPackageName());
            holder.profileImage.setImageResource(id != 0 ? id : R.drawable.profile1);
        } else if (pic != null && !pic.isEmpty()) {
            try {
                holder.profileImage.setImageURI(Uri.parse(pic));
            } catch (Exception e) {
                holder.profileImage.setImageResource(R.drawable.profile1);
            }
        } else {
            holder.profileImage.setImageResource(R.drawable.avatar);
        }

        // الاسم، الدور، الوصف
        String name = isArabic ? participant.getNameAr() : participant.getNameEn();
        String bio = isArabic ? participant.getBioAr() : participant.getBioEn();
        String desc = participant.getUsername();
        if (bio != null && !bio.isEmpty()) desc += ", " + bio;

        // تحديد نص الدور بناءً على اللغة
        String role = participant.getRole();
        String displayRole = isArabic ? getArabicRole(role) : role;

        holder.profileName.setText(name);
        holder.profileRole.setText(displayRole);
        holder.profileDescription.setText(desc);

        // لون الدور
        String roleLowerCase = role.toLowerCase();
        int roleColor;
        switch (roleLowerCase) {
            case "student":
                roleColor = ContextCompat.getColor(context, R.color.Custom_MainColorBlue);
                break;
            case "doctor":
                roleColor = ContextCompat.getColor(context, R.color.Custom_MainColorGolden);
                break;
            case "coordinator":
                roleColor = ContextCompat.getColor(context, R.color.Custom_MainColorDarkPink);
                break;
            case "program_manager":
                roleColor = ContextCompat.getColor(context, R.color.Custom_MainColorPurple);
                break;
            case "system_manager":
                roleColor = ContextCompat.getColor(context, R.color.Custom_MainColorTeal);
                break;
            default:
                roleColor = ContextCompat.getColor(context, R.color.Custom_MainColorOrange);
                break;
        }
        holder.profileRole.setTextColor(roleColor);

        // نمرر profile_user_id بالإضافة إلى البيانات الحالية
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("profile_user_id", participant.getUserId());
            intent.putExtra("profile_name", name);
            intent.putExtra("profile_bio", bio);
            intent.putExtra("header_color", roleColor);
            // نص القسم الثاني يمكن إضافته إذا أردت لون خاص للنص
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    public void updateParticipants(List<ParticipantData> newParticipants) {
        this.participants = newParticipants;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView profileImage;
        TextView profileName, profileRole, profileDescription;

        ViewHolder(View v) {
            super(v);
            profileImage = v.findViewById(R.id.img_profile);
            profileName = v.findViewById(R.id.profileName);
            profileRole = v.findViewById(R.id.profileRole);
            profileDescription = v.findViewById(R.id.profileDescription);
        }
    }

    // دالة مساعدة لتحويل الدور إلى العربية
    private String getArabicRole(String role) {
        switch (role.toLowerCase()) {
            case "student":
                return "طالب";
            case "doctor":
                return "دكتور";
            case "coordinator":
                return "منسق";
            case "program_manager":
                return "مدير برنامج";
            case "system_manager":
                return "مدير نظام";
            default:
                return "غير محدد";
        }
    }
}
