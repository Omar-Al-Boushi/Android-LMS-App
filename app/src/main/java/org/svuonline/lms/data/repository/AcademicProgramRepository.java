package org.svuonline.lms.data.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.svuonline.lms.data.db.DBContract;
import org.svuonline.lms.data.db.DatabaseHelper;

public class AcademicProgramRepository {
    private final DatabaseHelper dbHelper;

    public AcademicProgramRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public String getProgramNameById(long programId, boolean isArabic) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String column = isArabic ? DBContract.AcademicProgram.COL_NAME_AR : DBContract.AcademicProgram.COL_NAME_EN;
        String name = "";
        Cursor cursor = db.query(
                DBContract.AcademicProgram.TABLE_NAME,
                new String[]{column},
                DBContract.AcademicProgram.COL_PROGRAM_ID + " = ?",
                new String[]{String.valueOf(programId)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(column));
        }
        cursor.close();
        db.close();
        return name;
    }
}