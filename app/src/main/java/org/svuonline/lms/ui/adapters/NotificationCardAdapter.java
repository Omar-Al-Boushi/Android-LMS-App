package org.svuonline.lms.ui.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import org.svuonline.lms.R;
import org.svuonline.lms.ui.data.NotificationCardData;

import java.util.List;

public class NotificationCardAdapter extends RecyclerView.Adapter<NotificationCardAdapter.ViewHolder> {
    private final List<NotificationCardData> notificationList;
    private final Context context;

    public NotificationCardAdapter(Context context, List<NotificationCardData> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationCardData notification = notificationList.get(position);

        holder.cardNotification.setCardBackgroundColor(notification.getBackgroundColor());
        holder.imgNotification.setBackgroundColor(notification.getImageColor());
        holder.txtCourseName.setText(notification.getCourseName());
        holder.tvDescription.setText(notification.getDescription());
        holder.tvTime.setText(notification.getTime());

        // عند النقر، نحدث الحالة ونخبر الـ Adapter بتحديث العنصر
        holder.cardNotification.setOnClickListener(v -> {
            if (!notification.isRead()) {
                notification.setRead(true, holder.itemView.getContext());
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardNotification;
        ShapeableImageView imgNotification;
        TextView txtCourseName, tvDescription, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNotification = itemView.findViewById(R.id.card_notification);
            imgNotification = itemView.findViewById(R.id.img_notification);
            txtCourseName = itemView.findViewById(R.id.txt_notification);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
