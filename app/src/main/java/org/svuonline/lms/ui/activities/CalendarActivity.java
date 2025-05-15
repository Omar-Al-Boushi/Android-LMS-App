package org.svuonline.lms.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;

import org.svuonline.lms.R;
import org.svuonline.lms.data.model.Event;
import org.svuonline.lms.data.model.User;
import org.svuonline.lms.data.repository.EventRepository;
import org.svuonline.lms.data.repository.UserRepository;
import org.svuonline.lms.ui.adapters.EventsAdapter;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * نشاط لعرض التقويم والأحداث المرتبطة بالمستخدم.
 */
public class CalendarActivity extends BaseActivity {

    // عناصر واجهة المستخدم
    private Toolbar toolbar;
    private CalendarView calendarView;
    private RecyclerView recyclerEvents;
    private TextView textEventsHeader;
    private TextView tvNoEvents;
    private ImageView ivProfile;

    // المستودعات
    private EventRepository eventRepository;
    private UserRepository userRepository;

    // بيانات النشاط
    private long userId;
    private String language;
    private final Map<String, List<Event>> eventsMap = new HashMap<>();
    private EventsAdapter eventsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // تهيئة المكونات
        initComponents();

        // تهيئة الواجهة
        initViews();

        // التحقق من بيانات المستخدم
        if (!validateUserData()) {
            return;
        }

        // تهيئة البيانات
        initData();

        // إعداد مستمعات الأحداث
        setupListeners();
    }

    /**
     * تهيئة المستودعات
     */
    private void initComponents() {
        eventRepository = new EventRepository(this);
        userRepository = new UserRepository(this);
    }

    /**
     * تهيئة عناصر الواجهة
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar_top);
        calendarView = findViewById(R.id.calendarView);
        recyclerEvents = findViewById(R.id.recyclerEvents);
        textEventsHeader = findViewById(R.id.text_events_header);
        tvNoEvents = findViewById(R.id.tv_no_events);
        ivProfile = findViewById(R.id.iv_profile);
        textEventsHeader.setVisibility(TextView.GONE);
        tvNoEvents.setVisibility(TextView.GONE);
    }

    /**
     * التحقق من صحة بيانات المستخدم
     * @return صحيح إذا كانت البيانات صالحة، خطأ إذا لزم إنهاء النشاط
     */
    private boolean validateUserData() {
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = userPrefs.getLong("user_id", -1);
        if (userId == -1) {
            showToast(R.string.user_id_not_found);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return false;
        }
        return true;
    }

    /**
     * تهيئة البيانات (إعداد التقويم، الأحداث، الصورة الشخصية)
     */
    private void initData() {
        // إعداد لون شريط النظام
        Utils.setSystemBarColorWithColorInt(this,
                getResources().getColor(R.color.Custom_BackgroundColor),
                getResources().getColor(R.color.Custom_BackgroundColor), 0);

        // جلب اللغة
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        language = prefs.getString("selected_language", "en");

        // تحميل الأحداث
        loadEventsFromDatabase();

        // إعداد الصورة الشخصية
        setupProfilePicture();

        // إعداد RecyclerView
        setupRecycler();
    }

    /**
     * إعداد مستمعات الأحداث (التقويم، الشريط العلوي)
     */
    private void setupListeners() {
        // إعداد الشريط العلوي
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // إعداد التقويم
        setupCalendar();
    }

    /**
     * إعداد الشريط العلوي
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    /**
     * إعداد الصورة الشخصية
     */
    private void setupProfilePicture() {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            ivProfile.setImageResource(R.drawable.avatar);
            return;
        }
        String pic = user.getProfilePicture();
        if (pic != null && pic.startsWith("@drawable/")) {
            String name = pic.substring("@drawable/".length());
            int resId = getResources().getIdentifier(name, "drawable", getPackageName());
            ivProfile.setImageResource(resId != 0 ? resId : R.drawable.avatar);
        } else if (pic != null && !pic.isEmpty()) {
            try {
                ivProfile.setImageURI(Uri.parse(pic));
            } catch (Exception e) {
                ivProfile.setImageResource(R.drawable.avatar);
            }
        } else {
            ivProfile.setImageResource(R.drawable.avatar);
        }
    }

    /**
     * تحميل الأحداث من قاعدة البيانات
     */
    private void loadEventsFromDatabase() {
        List<Event> userEvents = eventRepository.getEventsByUserId(userId);
        eventsMap.clear();
        for (Event event : userEvents) {
            String key = event.getEventDate();
            List<Event> list = eventsMap.get(key);
            if (list == null) {
                list = new ArrayList<>();
                eventsMap.put(key, list);
            }
            list.add(event);
        }
    }

    /**
     * تحويل تاريخ نصي إلى Calendar
     * @param dateString التاريخ بتنسيق yyyy-MM-dd
     * @return كائن Calendar أو null إذا فشل التحويل
     */
    private Calendar stringToCalendar(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            java.util.Date date = sdf.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            showToast(R.string.date_parse_error);
            return null;
        }
    }

    /**
     * إعداد التقويم
     */
    private void setupCalendar() {
        List<EventDay> eventDays = new ArrayList<>();
        for (String date : eventsMap.keySet()) {
            Calendar cal = stringToCalendar(date);
            if (cal != null) {
                eventDays.add(new EventDay(cal, R.drawable.check_mark));
            }
        }
        calendarView.setEvents(eventDays);

        calendarView.setOnDayClickListener(eventDay -> {
            Calendar selectedCal = eventDay.getCalendar();
            String key = String.format(Locale.US, "%04d-%02d-%02d",
                    selectedCal.get(Calendar.YEAR),
                    selectedCal.get(Calendar.MONTH) + 1,
                    selectedCal.get(Calendar.DAY_OF_MONTH));
            List<Event> todaysEvents = eventsMap.containsKey(key) ? eventsMap.get(key) : Collections.emptyList();

            if (todaysEvents.isEmpty()) {
                textEventsHeader.setVisibility(TextView.VISIBLE);
                tvNoEvents.setVisibility(TextView.VISIBLE);
                eventsAdapter.updateEvents(Collections.emptyList());
            } else {
                tvNoEvents.setVisibility(TextView.GONE);
                textEventsHeader.setVisibility(TextView.VISIBLE);
                eventsAdapter.updateEvents(todaysEvents);
            }
        });
    }

    /**
     * إعداد RecyclerView للأحداث
     */
    private void setupRecycler() {
        eventsAdapter = new EventsAdapter(new ArrayList<>(), language);
        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerEvents.setAdapter(eventsAdapter);
        recyclerEvents.setNestedScrollingEnabled(false);
    }

    /**
     * عرض رسالة Toast
     * @param messageRes معرف الرسالة
     */
    private void showToast(int messageRes) {
        android.widget.Toast.makeText(this, messageRes, android.widget.Toast.LENGTH_SHORT).show();
    }
}