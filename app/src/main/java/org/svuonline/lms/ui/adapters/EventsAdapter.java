package org.svuonline.lms.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.svuonline.lms.R;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.VH> {

    private final List<String> events;

    public EventsAdapter(List<String> initialEvents) {
        this.events = initialEvents;
    }

    public void updateEvents(List<String> newEvents) {
        events.clear();
        events.addAll(newEvents);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.tv.setText(events.get(position));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tvEventItem);
        }
    }
}
