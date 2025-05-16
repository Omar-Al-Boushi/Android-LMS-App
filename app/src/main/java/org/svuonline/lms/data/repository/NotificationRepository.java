package org.svuonline.lms.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.core.content.ContextCompat;

import org.svuonline.lms.R;
import org.svuonline.lms.data.db.DBContract;
import org.svuonline.lms.data.db.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {
    private final DatabaseHelper dbHelper;
    private final Context appContext;

    public NotificationRepository(Context context) {
        this.dbHelper   = new DatabaseHelper(context);
        this.appContext = context.getApplicationContext();
    }

    public static class NotificationEntity {
        public int     id;
        public int     userId;
        public String  contentEn;
        public String  contentAr;
        public boolean isRead;
        public String  relatedType;
        public int     relatedId;
        public String  createdAt;

        public NotificationEntity(int id, int userId,
                                  String contentEn, String contentAr,
                                  boolean isRead, String relatedType,
                                  int relatedId, String createdAt) {
            this.id          = id;
            this.userId      = userId;
            this.contentEn   = contentEn;
            this.contentAr   = contentAr;
            this.isRead      = isRead;
            this.relatedType = relatedType;
            this.relatedId   = relatedId;
            this.createdAt   = createdAt;
        }
    }

    public static class CourseInfo {
        public final String code;
        public final int    color;
        public CourseInfo(String code, int color) {
            this.code  = code;
            this.color = color;
        }
    }

    /** جلب قائمة الإشعارات مع دعم التصفية بالصفحات والحالة */
    public List<NotificationEntity> getNotifications(int page, int pageSize, int userId, boolean unreadOnly) {
        List<NotificationEntity> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] cols = {
                DBContract.Notification.COL_NOTIFICATION_ID,
                DBContract.Notification.COL_USER_ID,
                DBContract.Notification.COL_CONTENT_EN,
                DBContract.Notification.COL_CONTENT_AR,
                DBContract.Notification.COL_IS_READ,
                DBContract.Notification.COL_RELATED_TYPE,
                DBContract.Notification.COL_RELATED_ID,
                DBContract.Notification.COL_CREATED_AT
        };
        // احسب الإزاحة (offset) ثمكون الصيغة "offset,count"
        int offset = (page - 1) * pageSize;
        String limit = offset + "," + pageSize;
        String where = DBContract.Notification.COL_USER_ID + " = ?";
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(userId));
        if (unreadOnly) {
            where += " AND " + DBContract.Notification.COL_IS_READ + " = 0";
        }

        Cursor c = db.query(
                DBContract.Notification.TABLE_NAME,
                cols,
                where,
                args.toArray(new String[0]),
                null, null,
                DBContract.Notification.COL_CREATED_AT + " DESC",
                limit
        );

        if (c.moveToFirst()) {
            do {
                list.add(new NotificationEntity(
                        c.getInt(c.getColumnIndexOrThrow(DBContract.Notification.COL_NOTIFICATION_ID)),
                        c.getInt(c.getColumnIndexOrThrow(DBContract.Notification.COL_USER_ID)),
                        c.getString(c.getColumnIndexOrThrow(DBContract.Notification.COL_CONTENT_EN)),
                        c.getString(c.getColumnIndexOrThrow(DBContract.Notification.COL_CONTENT_AR)),
                        c.getInt(c.getColumnIndexOrThrow(DBContract.Notification.COL_IS_READ)) == 1,
                        c.getString(c.getColumnIndexOrThrow(DBContract.Notification.COL_RELATED_TYPE)),
                        c.getInt(c.getColumnIndexOrThrow(DBContract.Notification.COL_RELATED_ID)),
                        c.getString(c.getColumnIndexOrThrow(DBContract.Notification.COL_CREATED_AT))
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    /** تعليم إشعار واحد كمقروء */
    public void markAsRead(int notificationId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBContract.Notification.COL_IS_READ, 1);
        db.update(
                DBContract.Notification.TABLE_NAME,
                cv,
                DBContract.Notification.COL_NOTIFICATION_ID + " = ?",
                new String[]{ String.valueOf(notificationId) }
        );
    }

    /** تعليم جميع إشعارات المستخدم كمقروءة */
    public void markAllReadForUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBContract.Notification.COL_IS_READ, 1);
        db.update(
                DBContract.Notification.TABLE_NAME,
                cv,
                DBContract.Notification.COL_USER_ID + " = ?",
                new String[]{ String.valueOf(userId) }
        );
    }

    /** إضافة إشعار جديد */
    public long create(NotificationEntity e) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DBContract.Notification.COL_USER_ID,     e.userId);
        cv.put(DBContract.Notification.COL_CONTENT_EN,   e.contentEn);
        cv.put(DBContract.Notification.COL_CONTENT_AR,   e.contentAr);
        cv.put(DBContract.Notification.COL_IS_READ,    e.isRead ? 1 : 0);
        cv.put(DBContract.Notification.COL_RELATED_TYPE, e.relatedType);
        cv.put(DBContract.Notification.COL_RELATED_ID,   e.relatedId);
        cv.put(DBContract.Notification.COL_CREATED_AT,   e.createdAt);
        return db.insert(DBContract.Notification.TABLE_NAME, null, cv);
    }

    /**
     * جلب رمز المقرر ولونه بناءً على نوع الإشعار.
     * يدعم الآن الأنواع:
     *   - assignment, submission, assignment_graded
     *   - resource
     *   - course_sections, course_tool, course_files
     */
    public CourseInfo getCourseInfo(NotificationEntity e) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // الافتراضي إذا لم يطابق أي نوع
        CourseInfo info = new CourseInfo(
                "", ContextCompat.getColor(appContext, R.color.Custom_MainColorPurple)
        );

        String related = e.relatedType;
        String sql;
        String[] args = { String.valueOf(e.relatedId) };

        if (related.equals("assignment") ||
                related.equals("submission") ||
                related.equals("assignment_graded")) {

            sql =
                    "SELECT c." + DBContract.Course.COL_CODE +
                            ", c."     + DBContract.Course.COL_COLOR +
                            " FROM "   + DBContract.Assignment.TABLE_NAME + " a" +
                            " JOIN "   + DBContract.SectionTool.TABLE_NAME + " st ON a." + DBContract.Assignment.COL_TOOL_ID +
                            " = st."   + DBContract.SectionTool.COL_TOOL_ID +
                            " JOIN "   + DBContract.CourseSection.TABLE_NAME + " cs ON st." + DBContract.SectionTool.COL_SECTION_ID +
                            " = cs."   + DBContract.CourseSection.COL_SECTION_ID +
                            " JOIN "   + DBContract.Course.TABLE_NAME + " c ON cs." + DBContract.CourseSection.COL_COURSE_ID +
                            " = c."    + DBContract.Course.COL_COURSE_ID +
                            " WHERE a." + DBContract.Assignment.COL_ASSIGNMENT_ID + " = ?";

        } else if (related.equals("resource")) {

            sql =
                    "SELECT c." + DBContract.Course.COL_CODE +
                            ", c."     + DBContract.Course.COL_COLOR +
                            " FROM "   + DBContract.Resource.TABLE_NAME + " r" +
                            " JOIN "   + DBContract.SectionTool.TABLE_NAME + " st ON r." + DBContract.Resource.COL_TOOL_ID +
                            " = st."   + DBContract.SectionTool.COL_TOOL_ID +
                            " JOIN "   + DBContract.CourseSection.TABLE_NAME + " cs ON st." + DBContract.SectionTool.COL_SECTION_ID +
                            " = cs."   + DBContract.CourseSection.COL_SECTION_ID +
                            " JOIN "   + DBContract.Course.TABLE_NAME + " c ON cs." + DBContract.CourseSection.COL_COURSE_ID +
                            " = c."    + DBContract.Course.COL_COURSE_ID +
                            " WHERE r." + DBContract.Resource.COL_RESOURCE_ID + " = ?";

        } else if (related.equals("course_sections") ||
                related.equals("course_tool")     ||
                related.equals("course_files")) {

            // استعلام عام لجميع أنواع course_*
            sql =
                    "SELECT c." + DBContract.Course.COL_CODE +
                            ", c."     + DBContract.Course.COL_COLOR +
                            " FROM "   + DBContract.Course.TABLE_NAME + " c" +
                            " WHERE c." + DBContract.Course.COL_COURSE_ID + " = ?";

        } else {
            db.close();
            return info;
        }

        try (Cursor cur = db.rawQuery(sql, args)) {
            if (cur.moveToFirst()) {
                String code      = cur.getString(cur.getColumnIndexOrThrow(DBContract.Course.COL_CODE));
                String colorName = cur.getString(cur.getColumnIndexOrThrow(DBContract.Course.COL_COLOR));
                int resId = appContext.getResources()
                        .getIdentifier(colorName, "color", appContext.getPackageName());
                int color = (resId == 0)
                        ? ContextCompat.getColor(appContext, R.color.Custom_MainColorPurple)
                        : ContextCompat.getColor(appContext, resId);
                info = new CourseInfo(code, color);
            }
        }

        db.close();
        return info;
    }
}
