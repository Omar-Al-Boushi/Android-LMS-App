package org.svuonline.lms.data.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.core.content.ContextCompat;

import org.svuonline.lms.R;
import org.svuonline.lms.data.db.DBContract;
import org.svuonline.lms.data.db.DatabaseHelper;
import org.svuonline.lms.data.model.Assignment;

import java.util.ArrayList;
import java.util.List;

public class AssignmentRepository {

    private final DatabaseHelper dbHelper;
    private final Context context;

    public AssignmentRepository(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    public List<Assignment> getAssignmentsByUserId(int userId, String statusFilter, String searchQuery, boolean isArabic) {
        List<Assignment> assignments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT a.*, c." + (isArabic ? DBContract.Course.COL_NAME_AR : DBContract.Course.COL_NAME_EN) + " AS courseName, " +
                "c." + DBContract.Course.COL_CODE + ", c." + DBContract.Course.COL_COLOR + ", " +
                "CASE WHEN sub." + DBContract.AssignmentSubmission.COL_STATUS + " = 'Submitted' THEN 'Completed' ELSE 'Progress' END AS status " +
                "FROM " + DBContract.Assignment.TABLE_NAME + " a " +
                "JOIN " + DBContract.SectionTool.TABLE_NAME + " st ON a." + DBContract.Assignment.COL_TOOL_ID + " = st." + DBContract.SectionTool.COL_TOOL_ID +
                " JOIN " + DBContract.CourseSection.TABLE_NAME + " cs ON st." + DBContract.SectionTool.COL_SECTION_ID + " = cs." + DBContract.CourseSection.COL_SECTION_ID +
                " JOIN " + DBContract.Course.TABLE_NAME + " c ON cs." + DBContract.CourseSection.COL_COURSE_ID + " = c." + DBContract.Course.COL_COURSE_ID +
                " JOIN " + DBContract.Enrollment.TABLE_NAME + " e ON c." + DBContract.Course.COL_COURSE_ID + " = e." + DBContract.Enrollment.COL_COURSE_ID +
                " LEFT JOIN " + DBContract.AssignmentSubmission.TABLE_NAME + " sub ON a." + DBContract.Assignment.COL_ASSIGNMENT_ID + " = sub." + DBContract.AssignmentSubmission.COL_ASSIGNMENT_ID +
                " AND sub." + DBContract.AssignmentSubmission.COL_USER_ID + " = e." + DBContract.Enrollment.COL_USER_ID +
                " WHERE e." + DBContract.Enrollment.COL_USER_ID + " = ?";

        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(userId));

        if (statusFilter != null && !statusFilter.isEmpty()) {
            query += " AND (sub." + DBContract.AssignmentSubmission.COL_STATUS + " = ? OR (sub." + DBContract.AssignmentSubmission.COL_STATUS + " IS NULL AND ? = 'Progress'))";
            selectionArgs.add(statusFilter.equals("Completed") ? "Submitted" : "");
            selectionArgs.add(statusFilter);
        }

        if (searchQuery != null && !searchQuery.isEmpty()) {
            query += " AND (a." + DBContract.Assignment.COL_TITLE_EN + " LIKE ? OR a." + DBContract.Assignment.COL_TITLE_AR + " LIKE ? OR c." + DBContract.Course.COL_CODE + " LIKE ?)";
            String searchPattern = "%" + searchQuery + "%";
            selectionArgs.add(searchPattern);
            selectionArgs.add(searchPattern);
            selectionArgs.add(searchPattern);
        }

        Cursor cursor = db.rawQuery(query, selectionArgs.toArray(new String[0]));
        if (cursor.moveToFirst()) {
            do {
                Assignment assignment = new Assignment();
                assignment.setAssignmentId(cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_ASSIGNMENT_ID)));
                assignment.setToolId(cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_TOOL_ID)));
                assignment.setTitleEn(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_TITLE_EN)));
                assignment.setTitleAr(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_TITLE_AR)));
                assignment.setOpenDate(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_OPEN_DATE)));
                assignment.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_DUE_DATE)));
                assignment.setAssignmentFile(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_ASSIGNMENT_FILE)));
                assignment.setCreatedBy(cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_CREATED_BY)));
                assignment.setCourseName(cursor.getString(cursor.getColumnIndexOrThrow("courseName")));
                assignment.setCourseCode(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_CODE)));
                assignment.setColor(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_COLOR)));
                assignment.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                assignments.add(assignment);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return assignments;
    }

    public int getCompletedAssignmentsCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DBContract.AssignmentSubmission.TABLE_NAME +
                " WHERE " + DBContract.AssignmentSubmission.COL_USER_ID + " = ?" +
                " AND " + DBContract.AssignmentSubmission.COL_STATUS + " = 'Submitted'";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public Assignment getNearestDueAssignment(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT a.*, c." + DBContract.Course.COL_NAME_EN + " AS courseName, " +
                "c." + DBContract.Course.COL_CODE + ", c." + DBContract.Course.COL_COLOR + ", " +
                "CASE WHEN sub." + DBContract.AssignmentSubmission.COL_STATUS + " = 'Submitted' THEN 'Completed' ELSE 'Progress' END AS status " +
                "FROM " + DBContract.Assignment.TABLE_NAME + " a " +
                "JOIN " + DBContract.SectionTool.TABLE_NAME + " st ON a." + DBContract.Assignment.COL_TOOL_ID + " = st." + DBContract.SectionTool.COL_TOOL_ID +
                " JOIN " + DBContract.CourseSection.TABLE_NAME + " cs ON st." + DBContract.SectionTool.COL_SECTION_ID + " = cs." + DBContract.CourseSection.COL_SECTION_ID +
                " JOIN " + DBContract.Course.TABLE_NAME + " c ON cs." + DBContract.CourseSection.COL_COURSE_ID + " = c." + DBContract.Course.COL_COURSE_ID +
                " JOIN " + DBContract.Enrollment.TABLE_NAME + " e ON c." + DBContract.Course.COL_COURSE_ID + " = e." + DBContract.Enrollment.COL_COURSE_ID +
                " LEFT JOIN " + DBContract.AssignmentSubmission.TABLE_NAME + " sub ON a." + DBContract.Assignment.COL_ASSIGNMENT_ID + " = sub." + DBContract.AssignmentSubmission.COL_ASSIGNMENT_ID +
                " AND sub." + DBContract.AssignmentSubmission.COL_USER_ID + " = e." + DBContract.Enrollment.COL_USER_ID +
                " WHERE e." + DBContract.Enrollment.COL_USER_ID + " = ?" +
                " AND e." + DBContract.Enrollment.COL_COURSE_STATUS + " = 'Registered'" + // شرط جديد
                " AND a." + DBContract.Assignment.COL_DUE_DATE + " >= date('now')" +
                " ORDER BY a." + DBContract.Assignment.COL_DUE_DATE + " ASC LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        Assignment assignment = null;
        if (cursor.moveToFirst()) {
            assignment = new Assignment();
            assignment.setAssignmentId(cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_ASSIGNMENT_ID)));
            assignment.setToolId(cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_TOOL_ID)));
            assignment.setTitleEn(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_TITLE_EN)));
            assignment.setTitleAr(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_TITLE_AR)));
            assignment.setOpenDate(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_OPEN_DATE)));
            assignment.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_DUE_DATE)));
            assignment.setAssignmentFile(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_ASSIGNMENT_FILE)));
            assignment.setCreatedBy(cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_CREATED_BY)));
            assignment.setCourseName(cursor.getString(cursor.getColumnIndexOrThrow("courseName")));
            assignment.setCourseCode(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_CODE)));
            assignment.setColor(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_COLOR)));
            assignment.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        }
        cursor.close();
        return assignment;
    }

    public Assignment getAssignmentDetails(long assignmentId, boolean isArabic) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT a.*, " +
                "c." + (isArabic ? DBContract.Course.COL_NAME_AR : DBContract.Course.COL_NAME_EN) + " AS courseName, " +
                "c." + DBContract.Course.COL_CODE + ", c." + DBContract.Course.COL_COLOR +
                " FROM " + DBContract.Assignment.TABLE_NAME + " a" +
                " JOIN " + DBContract.SectionTool.TABLE_NAME + " st ON a." + DBContract.Assignment.COL_TOOL_ID + " = st." + DBContract.SectionTool.COL_TOOL_ID +
                " JOIN " + DBContract.CourseSection.TABLE_NAME + " cs ON st." + DBContract.SectionTool.COL_SECTION_ID + " = cs." + DBContract.CourseSection.COL_SECTION_ID +
                " JOIN " + DBContract.Course.TABLE_NAME + " c ON cs." + DBContract.CourseSection.COL_COURSE_ID + " = c." + DBContract.Course.COL_COURSE_ID +
                " WHERE a." + DBContract.Assignment.COL_ASSIGNMENT_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(assignmentId)});

        Assignment assignment = null;
        if (cursor.moveToFirst()) {
            assignment = new Assignment();
            assignment.setAssignmentId(cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_ASSIGNMENT_ID)));
            assignment.setToolId(cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_TOOL_ID)));
            assignment.setTitleEn(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_TITLE_EN)));
            assignment.setTitleAr(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_TITLE_AR)));
            assignment.setOpenDate(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_OPEN_DATE)));
            assignment.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_DUE_DATE)));
            assignment.setAssignmentFile(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_ASSIGNMENT_FILE)));
            assignment.setCreatedBy(cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_CREATED_BY)));
            assignment.setCourseName(cursor.getString(cursor.getColumnIndexOrThrow("courseName")));
            assignment.setCourseCode(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_CODE)));
            assignment.setColor(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Course.COL_COLOR)));

            // تحويل اللون من String إلى int
            String colorString = assignment.getColor();
            int courseColor;
            if (colorString != null && !colorString.isEmpty()) {
                int colorResId = context.getResources().getIdentifier(colorString, "color", context.getPackageName());
                if (colorResId != 0) {
                    courseColor = ContextCompat.getColor(context, colorResId);
                } else {
                    courseColor = ContextCompat.getColor(context, R.color.Custom_MainColorBlue);
                }
            } else {
                courseColor = ContextCompat.getColor(context, R.color.Custom_MainColorBlue);
            }
            assignment.setHeaderColor(courseColor);
        }
        cursor.close();
        return assignment;
    }

    public long getAssignmentIdByToolId(String toolId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long assignmentId = -1;

        String query = "SELECT " + DBContract.Assignment.COL_ASSIGNMENT_ID +
                " FROM " + DBContract.Assignment.TABLE_NAME +
                " WHERE " + DBContract.Assignment.COL_TOOL_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(toolId)});

        if (cursor.moveToFirst()) {
            assignmentId = cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_ASSIGNMENT_ID));
        }

        cursor.close();
        return assignmentId;
    }

    //    جلب tool_id بناءً على assignment_id
    public String getToolIdByAssignmentId(long assignmentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String toolId = null;

        String query = "SELECT " + DBContract.Assignment.COL_TOOL_ID +
                " FROM " + DBContract.Assignment.TABLE_NAME +
                " WHERE " + DBContract.Assignment.COL_ASSIGNMENT_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(assignmentId)});

        if (cursor.moveToFirst()) {
            toolId = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Assignment.COL_TOOL_ID));
        }

        cursor.close();
        return toolId;
    }



}