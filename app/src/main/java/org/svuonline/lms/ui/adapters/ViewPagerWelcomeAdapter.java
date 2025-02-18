package org.svuonline.lms.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.svuonline.lms.R;
import org.svuonline.lms.ui.data.WelcomeData;

import java.util.List;

public class ViewPagerWelcomeAdapter extends RecyclerView.Adapter<ViewPagerWelcomeAdapter.ViewHolder> {

    private final List<WelcomeData> onboardingItems;

    public ViewPagerWelcomeAdapter(List<WelcomeData> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_welcome, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WelcomeData item = onboardingItems.get(position);
        holder.imageView.setImageResource(item.getImage());
        holder.title.setText(item.getTitle());
    }

    @Override
    public int getItemCount() {
        return onboardingItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            title = itemView.findViewById(R.id.tvTitle);
        }
    }
}
