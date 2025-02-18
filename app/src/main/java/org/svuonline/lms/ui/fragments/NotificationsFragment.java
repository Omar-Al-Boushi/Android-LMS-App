package org.svuonline.lms.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.svuonline.lms.R;
import org.svuonline.lms.ui.adapters.NotificationCardAdapter;
import org.svuonline.lms.ui.data.NotificationCardData;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationCardAdapter adapter;
    private List<NotificationCardData> notificationList;

    private ProgressBar progressBar;

    // متغيرات التحكم في التحميل
    private int currentPage = 1;
    private final int itemsPerPage = 20;
    private boolean isLoading = false;
    private final int totalItems = 100;

    // مصفوفات البيانات
    private String[] dates;
    private String[] messages;
    private int[] colors;
    private String[] courseCodes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        progressBar = view.findViewById(R.id.progressBar);

        notificationList = new ArrayList<>();
        adapter = new NotificationCardAdapter(requireContext(), notificationList);
        recyclerView.setAdapter(adapter);


        // تحضير البيانات
        prepareDataArrays();
        loadAllNotifications();
    }

    private void prepareDataArrays() {
        dates = new String[]{
                getString(R.string.date_28_nov_2024),
                getString(R.string.date_29_nov_2024_am),
                getString(R.string.date_30_nov_2025),
                getString(R.string.date_1_dec_2025),
                getString(R.string.date_2_dec_2025_pm)
        };

        messages = new String[]{
                getString(R.string.notification_content_added, "CCN403"),
                getString(R.string.notification_assignment_graded, "INT305"),
                getString(R.string.notification_assignment_added, "CEE205"),
                getString(R.string.notification_content_added, "BQM304"),
                getString(R.string.notification_assignment_graded, "GMA205")
        };

        colors = new int[]{
                getResources().getColor(R.color.Custom_MainColorDarkPink),
                getResources().getColor(R.color.Custom_MainColorGolden),
                getResources().getColor(R.color.Custom_MainColorBlue),
                getResources().getColor(R.color.Custom_MainColorPurple),
                getResources().getColor(R.color.Custom_MainColorTeal),
                getResources().getColor(R.color.Custom_MainColorOrange),
                getResources().getColor(R.color.Custom_MainColorGreen),
        };

        courseCodes = new String[]{
                "CCN403",
                "INT305",
                "CEE205",
                "BQM304",
                "GMA205",
                "CCN404",
                "GMA204"
        };
    }

    private void loadAllNotifications() {
        // تحميل جميع الإشعارات دفعة واحدة
        for (int i = 0; i < 30; i++) {
            boolean isRead = (i % 3 == 0);
            int color = colors[i % colors.length];
            String courseCode = courseCodes[i % courseCodes.length];
            String message = messages[i % messages.length];
            String date = dates[i % dates.length];

            notificationList.add(new NotificationCardData(
                    requireContext(),
                    isRead,
                    color,
                    courseCode,
                    message,
                    date
            ));
        }

        adapter.notifyDataSetChanged();
    }
}