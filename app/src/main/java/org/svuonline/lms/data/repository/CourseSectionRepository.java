package org.svuonline.lms.data.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.svuonline.lms.data.db.DBContract;
import org.svuonline.lms.data.db.DatabaseHelper;
import org.svuonline.lms.ui.data.SectionData;
import java.util.ArrayList;
import java.util.List;

public class CourseSectionRepository {
    private final DatabaseHelper dbHelper;

    public CourseSectionRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public List<SectionData> getSectionsByCourseId(long courseId, boolean isArabic) {
        List<SectionData> sections = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT " +
                DBContract.CourseSection.COL_SECTION_ID + ", " +
                (isArabic ? DBContract.CourseSection.COL_TITLE_AR : DBContract.CourseSection.COL_TITLE_EN) + " AS sectionTitle " +
                " FROM " + DBContract.CourseSection.TABLE_NAME +
                " WHERE " + DBContract.CourseSection.COL_COURSE_ID + " = ?" +
                " ORDER BY " + DBContract.CourseSection.COL_DISPLAY_ORDER + " ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(courseId)});
        while (cursor.moveToNext()) {
            int sectionId = cursor.getInt(cursor.getColumnIndexOrThrow(DBContract.CourseSection.COL_SECTION_ID));
            String sectionTitle = cursor.getString(cursor.getColumnIndexOrThrow("sectionTitle"));
            sections.add(new SectionData(sectionId, sectionTitle));
        }
        cursor.close();
        return sections;
    }
}