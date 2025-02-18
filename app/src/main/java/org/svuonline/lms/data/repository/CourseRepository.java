package org.svuonline.lms.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.core.content.ContextCompat;

import org.svuonline.lms.R;
import org.svuonline.lms.data.db.DBContract;
import org.svuonline.lms.data.db.DatabaseHelper;
import org.svuonline.lms.data.model.Course;
import org.svuonline.lms.ui.data.ButtonData;
import org.svuonline.lms.ui.data.CourseData;
import org.svuonline.lms.ui.data.SectionData;

import java.util.ArrayList;
import java.util.List;

public class CourseRepository {
    private final Context context; // متغير للوصول إلى موارد التطبيق
    private final DatabaseHelper dbHelper;

    public CourseRepository(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    public List<Course> getCoursesByUserId(long userId, String statusFilter, String searchQuery) {
        List<Course> courseList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT c.*, e." + DBContract.Enrollment.COL_COURSE_STATUS +
                " FROM " + DBContract.Course.TABLE_NAME + " c" +
                " JOIN " + DBContract.Enrollment.TABLE_NAME + " e ON c." + DBContract.Course.COL_COURSE_ID + " = e." + DBContract.Enrollment.COL_COURSE_ID +
                " WHERE e." + DBContract.Enrollment.COL_USER_ID + " = ?";

        if (statusFilter != null && !statusFilter.isEmpty()) {
            query += " AND e." + DBContract.Enrollment.COL_COURSE_STATUS + " = ?";
        }

        if (searchQuery != null && !searchQuery.isEmpty()) {
            query += " AND (c." + DBContract.Course.COL_CODE + " LIKE ? OR c." + DBContract.Course.COL_NAME_EN + " LIKE ? OR c." + DBContract.Course.COL_NAME_AR + " LIKE ?)";
        }

        List<String> args = new ArrayList<>();
        args.add(String.valueOf(userId));
        if (statusFilter != null && !statusFilter.isEmpty()) {
            args.add(statusFilter);
        }
        if (searchQuery != null && !searchQuery.isEmpty()) {
            String likeQuery = "%" + searchQuery + "%";
            args.add(likeQuery);
            args.add(likeQuery);
            args.add(likeQuery);
        }

        Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));
        if (cursor.moveToFirst()) {
            do {
                long courseId = cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Course.COL_COURSE_ID));
                long programId = cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Course.COL_PROGRAM_ID));
                long termId = cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Course.COL_TERM_ID));
                String code = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_CODE));
                String nameEn = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_NAME_EN));
                String nameAr = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_NAME_AR));
                long createdBy = cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Course.COL_CREATED_BY));
                String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_CREATED_AT));
                int creditHours = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Course.COL_CREDIT_HOURS));
                String color = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_COLOR));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Enrollment.COL_COURSE_STATUS));

                boolean isNew = status.equals("Registered");

                Course course = new Course(courseId, programId, termId, code, nameEn, nameAr, createdBy, createdAt,
                        creditHours, color, status, isNew);
                courseList.add(course);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return courseList;
    }

    public CourseData getCourseData(String courseCode, boolean isArabic) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String courseQuery = "SELECT " +
                DBContract.Course.COL_COURSE_ID + ", " +
                (isArabic ? DBContract.Course.COL_NAME_AR : DBContract.Course.COL_NAME_EN) + " AS courseName, " +
                DBContract.Course.COL_COLOR +
                " FROM " + DBContract.Course.TABLE_NAME +
                " WHERE " + DBContract.Course.COL_CODE + " = ?";
        Cursor courseCursor = db.rawQuery(courseQuery, new String[]{courseCode});

        if (!courseCursor.moveToFirst()) {
            courseCursor.close();
            return null;
        }

        int courseId = courseCursor.getInt(courseCursor.getColumnIndexOrThrow(DBContract.Course.COL_COURSE_ID));
        String courseName = courseCursor.getString(courseCursor.getColumnIndexOrThrow("courseName"));
        String colorString = courseCursor.getString(courseCursor.getColumnIndexOrThrow(DBContract.Course.COL_COLOR));
        courseCursor.close();

        // تحويل اسم اللون إلى قيمة int باستخدام Resource ID
        int courseColor;
        if (colorString != null && !colorString.isEmpty()) {
            int colorResId = context.getResources().getIdentifier(colorString, "color", context.getPackageName());
            if (colorResId != 0) {
                courseColor = ContextCompat.getColor(context, colorResId);
            } else {
                // لون افتراضي إذا لم يتم العثور على المورد
                courseColor = ContextCompat.getColor(context, R.color.Custom_MainColorBlue);
            }
        } else {
            // لون افتراضي إذا كان colorString فارغًا
            courseColor = ContextCompat.getColor(context, R.color.Custom_MainColorBlue);
        }

        // جلب الأقسام باستخدام CourseSectionRepository
        CourseSectionRepository sectionRepo = new CourseSectionRepository(context);
        List<SectionData> sections = sectionRepo.getSectionsByCourseId(courseId, isArabic);

        // جلب الأدوات لكل قسم باستخدام SectionToolRepository
        SectionToolRepository toolRepo = new SectionToolRepository(context);
        for (SectionData section : sections) {
            List<ButtonData> buttons = toolRepo.getToolsBySectionId(section.getSectionId(), isArabic, courseColor);
            section.setButtons(buttons);
        }

        return new CourseData(courseCode, courseName, courseColor, sections);
    }

    public boolean isCourseFavorite(long userId, String courseCode) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT e." + DBContract.Enrollment.COL_IS_FAVORITE +
                " FROM " + DBContract.Enrollment.TABLE_NAME + " e" +
                " JOIN " + DBContract.Course.TABLE_NAME + " c ON e." + DBContract.Enrollment.COL_COURSE_ID + " = c." + DBContract.Course.COL_COURSE_ID +
                " WHERE e." + DBContract.Enrollment.COL_USER_ID + " = ? AND c." + DBContract.Course.COL_CODE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), courseCode});

        boolean isFavorite = false;
        if (cursor.moveToFirst()) {
            isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Enrollment.COL_IS_FAVORITE)) == 1;
        }
        cursor.close();
        return isFavorite;
    }

    public void setCourseFavorite(long userId, String courseCode, boolean isFavorite) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBContract.Enrollment.COL_IS_FAVORITE, isFavorite ? 1 : 0);

        db.update(DBContract.Enrollment.TABLE_NAME,
                values,
                DBContract.Enrollment.COL_USER_ID + " = ? AND " +
                        DBContract.Enrollment.COL_COURSE_ID + " IN (SELECT " + DBContract.Course.COL_COURSE_ID +
                        " FROM " + DBContract.Course.TABLE_NAME +
                        " WHERE " + DBContract.Course.COL_CODE + " = ?)",
                new String[]{String.valueOf(userId), courseCode});
    }

    public int getCourseIdByCode(String courseCode) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT " + DBContract.Course.COL_COURSE_ID +
                " FROM " + DBContract.Course.TABLE_NAME +
                " WHERE " + DBContract.Course.COL_CODE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{courseCode});

        int courseId = -1;
        if (cursor.moveToFirst()) {
            courseId = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Course.COL_COURSE_ID));
        }
        cursor.close();
        return courseId;
    }

    // UserRepository.java (داخل الكلاس)
    public boolean updateUser(long userId,
                              String phone,
                              String whatsapp,
                              String facebook,
                              String telegram,
                              String email,
                              String bio,
                              String profilePictureUri) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBContract.Users.COL_PHONE, phone);
        cv.put(DBContract.Users.COL_WHATSAPP_NUMBER, whatsapp);
        cv.put(DBContract.Users.COL_FACEBOOK_URL, facebook);
        cv.put(DBContract.Users.COL_TELEGRAM_HANDLE, telegram);
        cv.put(DBContract.Users.COL_EMAIL, email);
        cv.put(DBContract.Users.COL_BIO_EN, bio);
        cv.put(DBContract.Users.COL_PROFILE_PICTURE, profilePictureUri);

        int rows = db.update(
                DBContract.Users.TABLE_NAME,
                cv,
                DBContract.Users.COL_USER_ID + " = ?",
                new String[]{ String.valueOf(userId) }
        );
        return rows > 0;
    }

}