package org.svuonline.lms.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

/**
 * فراغمنت لعرض الإشعارات مع خيارات التصفية وإدارة حالة القراءة.
 */
public class NotificationsFragment extends Fragment {

    // مفاتيح SharedPreferences وقناة الإشعارات
    private static final String PREFS_NAME = "AppPreferences";
    private static final String USER_PREFS_NAME = "user_prefs";
    private static final String CHANNEL_ID = "lms_channel";
    private static final String CHANNEL_NAME = "LMS Alerts";

    // عناصر واجهة المستخدم
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Button btnAll;
    private Button btnUnread;
    private Button btnMarkAll;

    // المستودعات والبيانات
    private NotificationRepository notificationRepository;
    private NotificationManager notificationManager;
    private NotificationCardAdapter adapter;
    private List<NotificationCardData> notificationList;
    private List<Integer> notificationIds;
    private int userId;
    private String language;
    private boolean unreadOnly = false;
    private int currentPage = 1;
    private final int itemsPerPage = 20;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // تهيئة المكونات
        initComponents();

        // تهيئة الواجهة
        initViews(view);

        // التحقق من بيانات المستخدم
        if (!validateUserData()) {
            return;
        }

        // تهيئة البيانات
        initData();

        // إعداد مستمعات الأحداث
        setupListeners();

        // تحميل الإشعارات
        loadNotifications();
    }

    /**
     * تهيئة المستودعات والإعدادات
     */
    private void initComponents() {
        notificationRepository = new NotificationRepository(requireContext());
        notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    /**
     * تهيئة عناصر الواجهة
     * @param view الواجهة المراد تهيئتها
     */
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewNotifications);
        progressBar = view.findViewById(R.id.progressBar);
        btnAll = view.findViewById(R.id.btnAll);
        btnUnread = view.findViewById(R.id.btnUnread);
        btnMarkAll = view.findViewById(R.id.btnMarkAllRead);
    }

    /**
     * التحقق من صحة بيانات المستخدم
     * @return صحيح إذا كانت البيانات صالحة
     */
    private boolean validateUserData() {
        SharedPreferences userPrefs = requireActivity().getSharedPreferences(USER_PREFS_NAME, Context.MODE_PRIVATE);
        userId = (int) userPrefs.getLong("user_id", -1);
        if (userId == -1) {
            return false;
        }

        SharedPreferences appPrefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        language = appPrefs.getString("selected_language", "en");
        return true;
    }

    /**
     * تهيئة البيانات (RecyclerView، القوائم)
     */
    private void initData() {
        notificationList = new ArrayList<>();
        notificationIds = new ArrayList<>();
        adapter = new NotificationCardAdapter(
                requireContext(),
                notificationList,
                notificationIds,
                notificationRepository
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        setFilterButtonStyles();
    }

    /**
     * إعداد مستمعات الأحداث (التصفية، إدارة القراءة)
     */
    private void setupListeners() {
        btnAll.setOnClickListener(v -> {
            unreadOnly = false;
            setFilterButtonStyles();
            loadNotifications();
        });

        btnUnread.setOnClickListener(v -> {
            unreadOnly = true;
            setFilterButtonStyles();
            loadNotifications();
        });

        btnMarkAll.setOnClickListener(v -> {
            notificationRepository.markAllReadForUser(userId);
            unreadOnly = false;
            setFilterButtonStyles();
            loadNotifications();
            showToast(R.string.all_notifications_marked_read);
        });
    }

    /**
     * تحديث أنماط أزرار التصفية
     */
    private void setFilterButtonStyles() {
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.md_theme_primary);
        int defaultColor = ContextCompat.getColor(requireContext(), R.color.dark_grey);

        btnAll.setBackgroundTintList(ColorStateList.valueOf(unreadOnly ? defaultColor : selectedColor));
        btnUnread.setBackgroundTintList(ColorStateList.valueOf(unreadOnly ? selectedColor : defaultColor));
    }

    /**
     * تحميل الإشعارات من قاعدة البيانات
     */
    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);

        List<NotificationRepository.NotificationEntity> entries = notificationRepository.getNotifications(
                currentPage, itemsPerPage, userId, unreadOnly);

        notificationList.clear();
        notificationIds.clear();

        for (NotificationRepository.NotificationEntity entry : entries) {
            NotificationRepository.CourseInfo info = notificationRepository.getCourseInfo(entry);
            String message = language.equals("ar") ? entry.contentAr : entry.contentEn;

            notificationList.add(new NotificationCardData(
                    requireContext(),
                    entry.isRead,
                    info.color,
                    info.code,
                    message,
                    entry.createdAt
            ));
            notificationIds.add(entry.id);
        }

        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);

        if (notificationList.isEmpty()) {
            showToast(R.string.no_notifications_found);
        }
    }

    /**
     * إنشاء قناة الإشعارات
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("System notifications for LMS");
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * تحديث الإشعارات عند استئناف الفراغمنت
     */
    @Override
    public void onResume() {
        super.onResume();
        if (userId != -1) {
            loadNotifications();
        }
    }

    /**
     * عرض رسالة Toast
     * @param messageRes معرف الرسالة
     */
    private void showToast(int messageRes) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), messageRes, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}