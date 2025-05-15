package org.svuonline.lms.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.svuonline.lms.R;
import org.svuonline.lms.data.db.DBContract;
import org.svuonline.lms.data.db.DatabaseHelper;
import org.svuonline.lms.data.model.User;
import org.svuonline.lms.ui.data.CourseCardData;

import java.util.ArrayList;
import java.util.List;

/**
 * مستودع البيانات لإدارة عمليات المستخدمين في قاعدة البيانات.
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    private final DatabaseHelper dbHelper;

    /**
     * باني المستودع.
     *
     * @param context السياق للوصول إلى قاعدة البيانات
     */
    public UserRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * التحقق من بيانات تسجيل الدخول للطلاب فقط مع حساب نشط.
     *
     * @param emailOrUsername البريد الإلكتروني أو اسم المستخدم
     * @param passwordHash    هاش كلمة المرور
     * @return معرف المستخدم إذا نجح تسجيل الدخول، أو -1 إذا فشل
     */
    public long loginUser(String emailOrUsername, String passwordHash) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            String input = emailOrUsername.toLowerCase();

            if (!input.contains("@")) {
                input = input + "@svuonline.org";
            }

            String[] columns = {DBContract.Users.COL_USER_ID};
            String selection = DBContract.Users.COL_EMAIL + " = ? AND " +
                    DBContract.Users.COL_PASSWORD_HASH + " = ? AND " +
                    "LOWER(" + DBContract.Users.COL_ROLE + ") = ? AND " +
                    "LOWER(" + DBContract.Users.COL_ACCOUNT_STATUS + ") = ?";
            String[] selectionArgs = {input, passwordHash, "student", "active"};

            try (Cursor cursor = db.query(
                    DBContract.Users.TABLE_NAME,
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null)) {
                if (cursor.moveToFirst()) {
                    long userId = cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Users.COL_USER_ID));
                    return userId;
                } else {
                    return -1;
                }
            }
        }
    }

    /**
     * تسجيل جميع المستخدمين في قاعدة البيانات لأغراض التصحيح.
     */
    public void logAllUsers() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT email, password_hash, role, account_status FROM " +
                     DBContract.Users.TABLE_NAME, null)) {
            if (cursor.moveToFirst()) {
                do {
                    String email = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_EMAIL));
                    String passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_PASSWORD_HASH));
                    String role = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ROLE));
                    String accountStatus = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ACCOUNT_STATUS));

                } while (cursor.moveToNext());
            } else {
            }
        }
    }

    /**
     * استرجاع بيانات المستخدم بناءً على معرفه.
     *
     * @param userId معرف المستخدم
     * @return كائن المستخدم أو null إذا لم يتم العثور على المستخدم
     */
    public User getUserById(long userId) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            String[] columns = {
                    DBContract.Users.COL_USER_ID,
                    DBContract.Users.COL_NAME_EN,
                    DBContract.Users.COL_NAME_AR,
                    DBContract.Users.COL_EMAIL,
                    DBContract.Users.COL_ROLE,
                    DBContract.Users.COL_ACCOUNT_STATUS,
                    DBContract.Users.COL_PHONE,
                    DBContract.Users.COL_FACEBOOK_URL,
                    DBContract.Users.COL_WHATSAPP_NUMBER,
                    DBContract.Users.COL_TELEGRAM_HANDLE,
                    DBContract.Users.COL_PROFILE_PICTURE,
                    DBContract.Users.COL_BIO_EN,
                    DBContract.Users.COL_BIO_AR,
                    DBContract.Users.COL_PROGRAM_ID // إضافة معرف البرنامج الأكاديمي
            };
            String selection = DBContract.Users.COL_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};

            try (Cursor cursor = db.query(
                    DBContract.Users.TABLE_NAME,
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null)) {
                if (cursor.moveToFirst()) {
                    User user = new User(
                            cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Users.COL_USER_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_NAME_EN)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_NAME_AR)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_EMAIL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ROLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_ACCOUNT_STATUS)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_PHONE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_FACEBOOK_URL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_WHATSAPP_NUMBER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_TELEGRAM_HANDLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_PROFILE_PICTURE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_BIO_EN)),
                            cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Users.COL_BIO_AR)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Users.COL_PROGRAM_ID))
                    );
                    return user;
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * استرجاع اسم البرنامج الأكاديمي بناءً على معرفه وإعداد اللغة.
     *
     * @param programId معرف البرنامج الأكاديمي
     * @param isArabic  إذا كان true، يتم استرجاع الاسم العربي؛ وإلا يتم استرجاع الاسم الإنجليزي
     * @return اسم البرنامج الأكاديمي أو سلسلة فارغة إذا لم يتم العثور عليه
     */
    public String getProgramNameById(int programId, boolean isArabic) {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase()) {
            String column = isArabic ? DBContract.AcademicProgram.COL_NAME_AR : DBContract.AcademicProgram.COL_NAME_EN;
            String[] columns = {column};
            String selection = DBContract.AcademicProgram.COL_PROGRAM_ID + " = ?";
            String[] selectionArgs = {String.valueOf(programId)};

            try (Cursor cursor = db.query(
                    DBContract.AcademicProgram.TABLE_NAME,
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null)) {
                if (cursor.moveToFirst()) {
                    String programName = cursor.getString(cursor.getColumnIndexOrThrow(column));
                    return programName != null ? programName : "";
                } else {
                    return "";
                }
            }
        }
    }


    /**
     * جلب قائمة المقررات التي سجل فيها المستخدم (Passed أو Registered).
     *
     * @param userId   معرف المستخدم
     * @param isArabic إذا كان true، يتم استرجاع أسماء المقررات والبرامج بالعربية
     * @return قائمة بكائنات CourseCardData
     */
    public List<CourseCardData> getUserCourses(long userId, boolean isArabic) {
        List<CourseCardData> courseList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                "c." + DBContract.Course.COL_COURSE_ID,
                "c." + DBContract.Course.COL_CODE,
                "c." + DBContract.Course.COL_NAME_EN,
                "c." + DBContract.Course.COL_NAME_AR,
                "c." + DBContract.Course.COL_COLOR,
                "c." + DBContract.Course.COL_PROGRAM_ID,
                "e." + DBContract.Enrollment.COL_IS_FAVORITE,
                "e." + DBContract.Enrollment.COL_COURSE_STATUS
        };

        String selection = "e." + DBContract.Enrollment.COL_USER_ID + " = ?" +
                " AND e." + DBContract.Enrollment.COL_COURSE_ID + " = c." + DBContract.Course.COL_COURSE_ID +
                " AND e." + DBContract.Enrollment.COL_COURSE_STATUS + " IN ('Passed', 'Registered')";
        String[] selectionArgs = {String.valueOf(userId)};

        try (Cursor cursor = db.query(
                DBContract.Course.TABLE_NAME + " c, " +
                        DBContract.Enrollment.TABLE_NAME + " e",
                columns,
                selection,
                selectionArgs,
                null, null, null)) {
            if (cursor.moveToFirst()) {
                do {
                    long courseId = cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Course.COL_COURSE_ID));
                    String courseCode = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_CODE));
                    String courseNameEn = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_NAME_EN));
                    String courseNameAr = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_NAME_AR));
                    String color = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_COLOR));
                    int programId = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Course.COL_PROGRAM_ID));
                    boolean isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Enrollment.COL_IS_FAVORITE)) == 1;
                    String courseStatus = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Enrollment.COL_COURSE_STATUS));

                    // تحديد اسم المقرر بناءً على اللغة
                    String courseName = isArabic && courseNameAr != null && !courseNameAr.isEmpty() ? courseNameAr : courseNameEn;

                    // جلب اسم البرنامج الأكاديمي
                    String programName = getProgramNameById(programId, isArabic);

                    // تحديد اللون
                    int courseColor = getColorFromString(color);

                    // تحديد الحالات
                    boolean isPassed = courseStatus.equals("Passed");
                    boolean isRegistered = courseStatus.equals("Registered");

                    CourseCardData course = new CourseCardData(
                            courseId,
                            courseCode,
                            programName,
                            courseName,
                            false, // isNew (غير متوفر في البيانات، يمكن تعديله إذا لزم الأمر)
                            isRegistered,
                            isPassed,
                            false, // isRemaining (لأننا نستبعد الحالة Remaining)
                            courseColor
                    );
                    courseList.add(course);
                } while (cursor.moveToNext());
            }
        } catch (Exception ignored) {
        }

        return courseList;
    }

    /**
     * تحويل اسم اللون إلى معرف اللون.
     */
    private int getColorFromString(String color) {
        if (color == null) return R.color.Custom_MainColorBlue;
        switch (color) {
            case "Custom_MainColorPurple":
                return R.color.Custom_MainColorPurple;
            case "Custom_MainColorDarkPink":
                return R.color.Custom_MainColorDarkPink;
            case "Custom_MainColorBlue":
                return R.color.Custom_MainColorBlue;
            case "Custom_MainColorGreen":
                return R.color.Custom_MainColorGreen;
            case "Custom_MainColorGolden":
                return R.color.Custom_MainColorGolden;
            case "Custom_MainColorOrange":
                return R.color.Custom_MainColorOrange;
            case "Custom_MainColorTeal":
                return R.color.Custom_MainColorTeal;
            default:
                return R.color.Custom_MainColorBlue; // لون افتراضي
        }
    }

    /** حدّث بيانات المستخدم كاملة مع الـ bio باللغتين */
    public boolean updateUser(long userId,
                              String phone,
                              String whatsapp,
                              String facebook,
                              String telegram,
                              String email,
                              String bioEn,
                              String bioAr,
                              String profilePictureUri) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBContract.Users.COL_PHONE, phone);
        cv.put(DBContract.Users.COL_WHATSAPP_NUMBER, whatsapp);
        cv.put(DBContract.Users.COL_FACEBOOK_URL, facebook);
        cv.put(DBContract.Users.COL_TELEGRAM_HANDLE, telegram);
        cv.put(DBContract.Users.COL_EMAIL, email);
        cv.put(DBContract.Users.COL_BIO_EN, bioEn);
        cv.put(DBContract.Users.COL_BIO_AR, bioAr);
        cv.put(DBContract.Users.COL_PROFILE_PICTURE, profilePictureUri);

        int rows = db.update(
                DBContract.Users.TABLE_NAME,
                cv,
                DBContract.Users.COL_USER_ID + " = ?",
                new String[]{ String.valueOf(userId) }
        );
        return rows > 0;
    }

    /**
     * Reset all favorite courses for a user by setting isFavorite to 0.
     *
     * @param userId The ID of the user
     * @return true if the update was successful, false otherwise
     */
    public boolean resetFavoriteCourses(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBContract.Enrollment.COL_IS_FAVORITE, 0);

        int rowsAffected = db.update(
                DBContract.Enrollment.TABLE_NAME,
                values,
                DBContract.Enrollment.COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        return rowsAffected > 0;
    }

    /**
     * Fetch the list of favorite courses for the user.
     *
     * @param userId   The ID of the user
     * @param isArabic If true, retrieve course and program names in Arabic
     * @return List of CourseCardData objects for favorite courses
     */
    public List<CourseCardData> getFavoriteCourses(long userId, boolean isArabic) {
        List<CourseCardData> favoriteList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                "c." + DBContract.Course.COL_COURSE_ID,
                "c." + DBContract.Course.COL_CODE,
                "c." + DBContract.Course.COL_NAME_EN,
                "c." + DBContract.Course.COL_NAME_AR,
                "c." + DBContract.Course.COL_COLOR,
                "c." + DBContract.Course.COL_PROGRAM_ID,
                "e." + DBContract.Enrollment.COL_IS_FAVORITE,
                "e." + DBContract.Enrollment.COL_COURSE_STATUS
        };

        String selection = "e." + DBContract.Enrollment.COL_USER_ID + " = ?" +
                " AND e." + DBContract.Enrollment.COL_COURSE_ID + " = c." + DBContract.Course.COL_COURSE_ID +
                " AND e." + DBContract.Enrollment.COL_IS_FAVORITE + " = 1";
        String[] selectionArgs = {String.valueOf(userId)};

        try (Cursor cursor = db.query(
                DBContract.Course.TABLE_NAME + " c, " +
                        DBContract.Enrollment.TABLE_NAME + " e",
                columns,
                selection,
                selectionArgs,
                null, null, null)) {
            if (cursor.moveToFirst()) {
                do {
                    long courseId = cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Course.COL_COURSE_ID));
                    String courseCode = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_CODE));
                    String courseNameEn = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_NAME_EN));
                    String courseNameAr = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_NAME_AR));
                    String color = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_COLOR));
                    int programId = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Course.COL_PROGRAM_ID));
                    boolean isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Enrollment.COL_IS_FAVORITE)) == 1;
                    String courseStatus = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Enrollment.COL_COURSE_STATUS));

                    // Determine course name based on language
                    String courseName = isArabic && courseNameAr != null && !courseNameAr.isEmpty() ? courseNameAr : courseNameEn;

                    // Fetch program name
                    String programName = getProgramNameById(programId, isArabic);

                    // Determine color
                    int courseColor = getColorFromString(color);

                    // Determine status flags
                    boolean isPassed = courseStatus.equals("Passed");
                    boolean isRegistered = courseStatus.equals("Registered");
                    boolean isRemaining = courseStatus.equals("Remaining");

                    CourseCardData course = new CourseCardData(
                            courseId,
                            courseCode,
                            programName,
                            courseName,
                            false, // isNew (not available in data, can be modified if needed)
                            isRegistered,
                            isPassed,
                            isRemaining,
                            courseColor
                    );
                    favoriteList.add(course);
                } while (cursor.moveToNext());
            }
        } catch (Exception ignored) {
        }

        return favoriteList;
    }

}