package org.svuonline.lms.data.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.svuonline.lms.data.db.DBContract;
import org.svuonline.lms.data.db.DatabaseHelper;
import org.svuonline.lms.ui.data.ButtonData;
import java.util.ArrayList;
import java.util.List;

public class SectionToolRepository {
    private final DatabaseHelper dbHelper;

    public SectionToolRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public List<ButtonData> getToolsBySectionId(long sectionId, boolean isArabic, int courseColor) {
        List<ButtonData> tools = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT " +
                DBContract.SectionTool.COL_TOOL_ID + ", " +
                (isArabic ? DBContract.SectionTool.COL_NAME_AR : DBContract.SectionTool.COL_NAME_EN) + " AS toolName, " +
                DBContract.SectionTool.COL_ACTION_TYPE +
                " FROM " + DBContract.SectionTool.TABLE_NAME +
                " WHERE " + DBContract.SectionTool.COL_SECTION_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(sectionId)});
        while (cursor.moveToNext()) {
            long toolId = cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.SectionTool.COL_TOOL_ID));
            String toolName = cursor.getString(cursor.getColumnIndexOrThrow("toolName"));
            String actionType = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.SectionTool.COL_ACTION_TYPE));
            tools.add(new ButtonData(toolId,toolName, courseColor, actionType));
        }
        cursor.close();
        return tools;
    }

    /**
     * جلب رمز المقرر (course_code) بناءً على معرف الأداة (toolId).
     */
    public String getCourseCodeByToolId(long toolId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT " + DBContract.Course.COL_CODE +
                " FROM " + DBContract.Course.TABLE_NAME +
                " INNER JOIN " + DBContract.CourseSection.TABLE_NAME +
                " ON " + DBContract.Course.TABLE_NAME + "." + DBContract.Course.COL_COURSE_ID +
                " = " + DBContract.CourseSection.TABLE_NAME + "." + DBContract.CourseSection.COL_COURSE_ID +
                " INNER JOIN " + DBContract.SectionTool.TABLE_NAME +
                " ON " + DBContract.CourseSection.TABLE_NAME + "." + DBContract.CourseSection.COL_SECTION_ID +
                " = " + DBContract.SectionTool.TABLE_NAME + "." + DBContract.SectionTool.COL_SECTION_ID +
                " WHERE " + DBContract.SectionTool.TABLE_NAME + "." + DBContract.SectionTool.COL_TOOL_ID + " = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(toolId)})) {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_CODE));
            }
            return null;
        }
    }

    /**
     * جلب اسم الأداة بناءً على معرف الأداة (toolId) واللغة.
     */
    public String getToolName(long toolId, boolean isArabic) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String column = isArabic ? DBContract.SectionTool.COL_NAME_AR : DBContract.SectionTool.COL_NAME_EN;
        String query = "SELECT " + column +
                " FROM " + DBContract.SectionTool.TABLE_NAME +
                " WHERE " + DBContract.SectionTool.COL_TOOL_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(toolId)});
        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(column));
            }
            return null;
        } finally {
            cursor.close();
        }
    }
}