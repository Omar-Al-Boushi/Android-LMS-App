package org.svuonline.lms.ui.activities;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;

import org.svuonline.lms.R;
import org.svuonline.lms.ui.adapters.EventsAdapter;
import org.svuonline.lms.utils.BaseActivity;
import org.svuonline.lms.utils.Utils;

import java.util.*;

public class CalendarActivity extends BaseActivity {

    private CalendarView calendarView;
    private RecyclerView recyclerEvents;
    private TextView textEventsHeader, tvNoEvents;
    private EventsAdapter eventsAdapter;

    private final Map<String, List<String>> eventsMap = new HashMap<>();
    private final List<Calendar> eventCalendars = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Utils.setSystemBarColorWithColorInt(this, getResources().getColor(R.color.Custom_BackgroundColor),
                getResources().getColor(R.color.Custom_BackgroundColor), 0);
        setupToolbar();
        bindViews();
        prepareSampleEvents();
        setupCalendar();
        setupRecycler();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_top);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void bindViews() {
        calendarView = findViewById(R.id.calendarView);
        recyclerEvents = findViewById(R.id.recyclerEvents);
        textEventsHeader = findViewById(R.id.text_events_header);
        tvNoEvents = findViewById(R.id.tv_no_events);
        // إخفاء الشاشات الثانوية في البداية
        textEventsHeader.setVisibility(TextView.GONE);
        tvNoEvents.setVisibility(TextView.GONE);
    }

    private void prepareSampleEvents() {
        addEvent(2025, Calendar.APRIL, 17,
                getString(R.string.event_apr17_1),
                getString(R.string.event_apr17_2),
                getString(R.string.event_apr17_3),
                getString(R.string.event_apr17_4));

        addEvent(2025, Calendar.APRIL, 20,
                getString(R.string.event_apr20_1),
                getString(R.string.event_apr20_2),
                getString(R.string.event_apr20_3),
                getString(R.string.event_apr20_4));

        addEvent(2025, Calendar.APRIL, 22,
                getString(R.string.event_apr22_1));

        addEvent(2025, Calendar.MAY, 1,
                getString(R.string.event_may01_1));

        addEvent(2025, Calendar.MAY, 5,
                getString(R.string.event_may05_1));
    }


    private void addEvent(int year, int month, int day, String... items) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        String key = dateKey(cal);
        List<String> list = new ArrayList<>(Arrays.asList(items));
        eventsMap.put(key, list);
        eventCalendars.add(cal);
    }

    private void setupCalendar() {
        // وضع أيقونات للأيام المحتوية أحداث
        List<EventDay> eventDays = new ArrayList<>();
        for (Calendar c : eventCalendars) {
            eventDays.add(new EventDay(c, R.drawable.check_mark));
        }
        calendarView.setEvents(eventDays);
        calendarView.setOnDayClickListener(eventDay -> {
            String key = dateKey(eventDay.getCalendar());
            List<String> todays = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                todays = eventsMap.getOrDefault(key, Collections.emptyList());
            }

            if (todays.isEmpty()) {
                textEventsHeader.setVisibility(TextView.VISIBLE);
                tvNoEvents.setVisibility(TextView.VISIBLE);
                eventsAdapter.updateEvents(Collections.emptyList());
            } else {
                tvNoEvents.setVisibility(TextView.GONE);
                textEventsHeader.setVisibility(TextView.VISIBLE);
                eventsAdapter.updateEvents(todays);
            }
        });
    }

    private void setupRecycler() {
        eventsAdapter = new EventsAdapter(new ArrayList<>());
        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerEvents.setAdapter(eventsAdapter);
        recyclerEvents.setNestedScrollingEnabled(false);
    }

    private String dateKey(Calendar c) {
        return String.format(Locale.US, "%04d-%02d-%02d",
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH));
    }
}
