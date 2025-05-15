package org.svuonline.lms.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.svuonline.lms.data.db.DBContract;
import org.svuonline.lms.data.db.DatabaseHelper;
import org.svuonline.lms.data.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventRepository {
    private final DatabaseHelper dbHelper;

    public EventRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public List<Event> getEventsByUserId(long userId) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DBContract.Event.TABLE_NAME,
                null,
                DBContract.Event.COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                DBContract.Event.COL_EVENT_DATE + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Event event = new Event();
                event.setEventId(cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Event.COL_EVENT_ID)));
                event.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Event.COL_USER_ID)));
                event.setTitleEn(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Event.COL_TITLE_EN)));
                event.setTitleAr(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Event.COL_TITLE_AR)));
                event.setEventDate(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Event.COL_EVENT_DATE)));
                event.setType(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Event.COL_TYPE)));
                event.setRelatedId(cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Event.COL_RELATED_ID)));
                events.add(event);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return events;
    }

    public void addEvent(Event event) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBContract.Event.COL_USER_ID, event.getUserId());
        values.put(DBContract.Event.COL_TITLE_EN, event.getTitleEn());
        values.put(DBContract.Event.COL_TITLE_AR, event.getTitleAr());
        values.put(DBContract.Event.COL_EVENT_DATE, event.getEventDate());
        values.put(DBContract.Event.COL_TYPE, event.getType());
        values.put(DBContract.Event.COL_RELATED_ID, event.getRelatedId());
        db.insert(DBContract.Event.TABLE_NAME, null, values);
        db.close();
    }
}