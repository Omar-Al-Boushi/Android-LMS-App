package org.svuonline.lms.data.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.svuonline.lms.data.db.DBContract;
import org.svuonline.lms.data.db.DatabaseHelper;
import org.svuonline.lms.data.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class ResourceRepository {
    private DatabaseHelper dbHelper;

    public ResourceRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public List<Resource> getResourcesByToolId(long toolId) {
        List<Resource> resources = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + DBContract.Resource.TABLE_NAME +
                " WHERE " + DBContract.Resource.COL_TOOL_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(toolId)});

        if (cursor.moveToFirst()) {
            do {
                Resource resource = new Resource(
                        cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Resource.COL_RESOURCE_ID)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Resource.COL_TOOL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Resource.COL_FILE_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Resource.COL_FILE_PATH)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.Resource.COL_UPLOADED_BY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DBContract.Resource.COL_UPLOADED_AT))
                );
                resources.add(resource);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return resources;
    }
}