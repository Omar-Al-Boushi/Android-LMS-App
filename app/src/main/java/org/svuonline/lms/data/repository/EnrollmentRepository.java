package org.svuonline.lms.data.repository;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.svuonline.lms.data.db.DBContract;
import org.svuonline.lms.data.db.DatabaseHelper;
import org.svuonline.lms.ui.data.ParticipantData;

import java.util.ArrayList;
import java.util.List;

public class EnrollmentRepository {

    private final DatabaseHelper dbHelper;

    public EnrollmentRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public int getEnrollmentCountByStatus(int userId, String status) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DBContract.Enrollment.TABLE_NAME +
                " WHERE " + DBContract.Enrollment.COL_USER_ID + " = ?" +
                " AND " + DBContract.Enrollment.COL_COURSE_STATUS + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), status});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public boolean isUserEnrolledInCourse(long userId, String courseCode) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DBContract.Enrollment.TABLE_NAME + " e " +
                "JOIN " + DBContract.Course.TABLE_NAME + " c ON e." + DBContract.Enrollment.COL_COURSE_ID + " = c." + DBContract.Course.COL_COURSE_ID +
                " WHERE e." + DBContract.Enrollment.COL_USER_ID + " = ?" +
                " AND c." + DBContract.Course.COL_CODE + " = ?" +
                " AND e." + DBContract.Enrollment.COL_COURSE_STATUS + " = 'Registered'";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), courseCode});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count > 0;
    }

    /**
     * جلب المشاركين في مقرر معين بناءً على courseId مع دعم البحث.
     *
     * @param courseId   معرف المقرر
     * @param searchQuery استعلام البحث (اسم المستخدم أو الاسم)
     * @param isArabic   إذا كان true، يتم البحث في الاسم العربي؛ وإلا في الاسم الإنجليزي
     * @return قائمة ببيانات المشاركين
     */
    public List<ParticipantData> getParticipantsByCourseId(int courseId, String searchQuery, boolean isArabic) {
        List<ParticipantData> participants = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                "u." + DBContract.Users.COL_USER_ID,
                DBContract.Users.COL_EMAIL,
                DBContract.Users.COL_NAME_EN,
                DBContract.Users.COL_NAME_AR,
                DBContract.Users.COL_ROLE,
                DBContract.Users.COL_BIO_EN,
                DBContract.Users.COL_BIO_AR,
                DBContract.Users.COL_PROFILE_PICTURE,
                "e." + DBContract.Enrollment.COL_IS_FAVORITE
        };

        String selection = "u." + DBContract.Users.COL_USER_ID + " = e." + DBContract.Enrollment.COL_USER_ID +
                " AND e." + DBContract.Enrollment.COL_COURSE_ID + " = ?" +
                " AND e." + DBContract.Enrollment.COL_COURSE_STATUS + " IN ('Passed', 'Registered')";
        String[] selectionArgs = {String.valueOf(courseId)};

        if (searchQuery != null && !searchQuery.isEmpty()) {
            selection += " AND (LOWER(u." + DBContract.Users.COL_EMAIL + ") LIKE ? OR " +
                    "u." + (isArabic ? DBContract.Users.COL_NAME_AR : DBContract.Users.COL_NAME_EN) + " LIKE ?)";
            String likeQuery = "%" + searchQuery + "%";
            String[] newArgs = new String[selectionArgs.length + 2];
            System.arraycopy(selectionArgs, 0, newArgs, 0, selectionArgs.length);
            newArgs[selectionArgs.length] = "%" + searchQuery.toLowerCase() + "%";
            newArgs[selectionArgs.length + 1] = likeQuery;
            selectionArgs = newArgs;
        }

        try (Cursor cursor = db.query(
                DBContract.Users.TABLE_NAME + " u, " + DBContract.Enrollment.TABLE_NAME + " e",
                columns,
                selection,
                selectionArgs,
                null, null, null)) {
            if (cursor.moveToFirst()) {
                do {
                    long userId = cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Users.COL_USER_ID));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_EMAIL));
                    String nameEn = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_NAME_EN));
                    String nameAr = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_NAME_AR));
                    String role = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ROLE));
                    String bioEn = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_BIO_EN));
                    String bioAr = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_BIO_AR));
                    String profilePicture = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_PROFILE_PICTURE));
                    boolean isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Enrollment.COL_IS_FAVORITE)) == 1;

                    // اشتقاق اسم المستخدم من البريد الإلكتروني
                    String username = email != null ? email.replace("@svuonline.org", "") : "";

                    ParticipantData participant = new ParticipantData(
                            userId, username, nameEn, nameAr, role, bioEn, bioAr, profilePicture, isFavorite
                    );
                    participants.add(participant);
                } while (cursor.moveToNext());
            }
        }
        Log.d(TAG, "عدد المشاركين المسترجعين: " + participants.size());
        return participants;
    }
}