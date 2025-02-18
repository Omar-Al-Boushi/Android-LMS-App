package org.svuonline.lms.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import org.svuonline.lms.R;
import org.svuonline.lms.data.repository.NotificationRepository;
import org.svuonline.lms.ui.adapters.NotificationCardAdapter;
import org.svuonline.lms.ui.data.NotificationCardData;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar  progressBar;
    private Button       btnAll, btnUnread, btnMarkAll;

    private NotificationCardAdapter adapter;
    private List<NotificationCardData> notificationList;
    private List<Integer>              notificationIds;

    private NotificationRepository notificationRepo;
    private NotificationManager    notificationManager;

    private int    currentPage     = 1;
    private final int itemsPerPage = 20;
    private int    userId;
    private String lang;
    private boolean unreadOnly     = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // views
        recyclerView = view.findViewById(R.id.recyclerViewNotifications);
        progressBar  = view.findViewById(R.id.progressBar);
        btnAll       = view.findViewById(R.id.btnAll);
        btnUnread    = view.findViewById(R.id.btnUnread);
        btnMarkAll   = view.findViewById(R.id.btnMarkAllRead);

        // layout manager & lists
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationList  = new ArrayList<>();
        notificationIds   = new ArrayList<>();

        // repo & manager
        notificationRepo    = new NotificationRepository(getContext());
        notificationManager = (NotificationManager)
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        // adapter
        adapter = new NotificationCardAdapter(
                requireContext(),
                notificationList,
                notificationIds,
                notificationRepo
        );
        recyclerView.setAdapter(adapter);

        // load user prefs
        SharedPreferences up = requireActivity()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userId = (int) up.getLong("user_id", -1);

        SharedPreferences lp = requireActivity()
                .getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        lang = lp.getString("selected_language", "en");

        // button listeners
        btnAll.setOnClickListener(v -> {
            unreadOnly = false;
            setFilterButtonStyles();
            loadNotificationsFromDb();
        });
        btnUnread.setOnClickListener(v -> {
            unreadOnly = true;
            setFilterButtonStyles();
            loadNotificationsFromDb();
        });
        btnMarkAll.setOnClickListener(v -> {
            notificationRepo.markAllReadForUser(userId);
            unreadOnly = false;
            setFilterButtonStyles();
            loadNotificationsFromDb();
        });

        // initial load
        setFilterButtonStyles();
        loadNotificationsFromDb();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotificationsFromDb();
    }

    private void setFilterButtonStyles() {
        int sel = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        int def = ContextCompat.getColor(requireContext(), R.color.dark_grey);

        btnAll.setBackgroundTintList(ColorStateList.valueOf(unreadOnly ? def : sel));
        btnUnread.setBackgroundTintList(ColorStateList.valueOf(unreadOnly ? sel : def));
    }

    private void loadNotificationsFromDb() {
        progressBar.setVisibility(View.VISIBLE);

        List<NotificationRepository.NotificationEntity> entries =
                notificationRepo.getNotifications(
                        currentPage, itemsPerPage, userId, unreadOnly);

        notificationList.clear();
        notificationIds.clear();

        for (NotificationRepository.NotificationEntity e : entries) {
            NotificationRepository.CourseInfo info = notificationRepo.getCourseInfo(e);
            String message = lang.equals("ar") ? e.contentAr : e.contentEn;

            // add data and corresponding ID
            notificationList.add(new NotificationCardData(
                    requireContext(),
                    e.isRead,
                    info.color,
                    info.code,
                    message,
                    e.createdAt
            ));
            notificationIds.add(e.id);

        }

        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(
                    "lms_channel",
                    "LMS Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            c.setDescription("System notifications for LMS");
            notificationManager.createNotificationChannel(c);
        }
    }
}
