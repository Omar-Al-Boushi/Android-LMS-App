package org.svuonline.lms.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.svuonline.lms.R;
import org.svuonline.lms.data.db.DBContract;
import org.svuonline.lms.data.db.DatabaseHelper;
import org.svuonline.lms.data.model.AssignmentSubmission;
import org.svuonline.lms.data.model.Event;
import org.svuonline.lms.notifications.AppNotificationManager;
import org.svuonline.lms.utils.DateTimeUtils;

/**
 * Repository for handling assignment submissions, generating notifications, and creating events.
 */
public class AssignmentSubmissionRepository {

    private final DatabaseHelper dbHelper;
    private final NotificationRepository notifRepo;
    private final EventRepository eventRepository; // إضافة EventRepository
    private final AppNotificationManager appNotif;
    private final Context context;

    public AssignmentSubmissionRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dbHelper = new DatabaseHelper(this.context);
        this.notifRepo = new NotificationRepository(this.context);
        this.eventRepository = new EventRepository(this.context); // تهيئة EventRepository
        this.appNotif = new AppNotificationManager(this.context);
    }

    /**
     * Fetch submission status by assignment and user IDs.
     */
    public AssignmentSubmission getSubmissionStatus(long assignmentId, long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DBContract.AssignmentSubmission.TABLE_NAME +
                " WHERE " + DBContract.AssignmentSubmission.COL_ASSIGNMENT_ID + " = ? AND " +
                DBContract.AssignmentSubmission.COL_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(assignmentId),
                String.valueOf(userId)
        });

        AssignmentSubmission submission = null;
        if (cursor.moveToFirst()) {
            submission = new AssignmentSubmission();
            submission.setSubmissionId(cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.AssignmentSubmission.COL_SUBMISSION_ID)));
            submission.setAssignmentId(cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.AssignmentSubmission.COL_ASSIGNMENT_ID)));
            submission.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.AssignmentSubmission.COL_USER_ID)));
            submission.setSubmittedAt(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.AssignmentSubmission.COL_SUBMITTED_AT)));
            submission.setFilePath(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.AssignmentSubmission.COL_FILE_PATH)));
            submission.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.AssignmentSubmission.COL_STATUS)));
            submission.setGrade(cursor.getFloat(cursor.getColumnIndexOrThrow(DBContract.AssignmentSubmission.COL_GRADE)));
            submission.setGradedBy(cursor.isNull(cursor.getColumnIndexOrThrow(DBContract.AssignmentSubmission.COL_GRADED_BY)) ?
                    0 : cursor.getLong(cursor.getColumnIndexOrThrow(DBContract.AssignmentSubmission.COL_GRADED_BY)));
            submission.setGradedAt(cursor.getString(cursor.getColumnIndexOrThrow(DBContract.AssignmentSubmission.COL_GRADED_AT)));
        }
        cursor.close();
        db.close();
        return submission;
    }

    /**
     * Insert or update a submission, create an event for submission, and send notifications.
     */
    public void insertOrUpdateSubmission(long assignmentId,
                                         long userId,
                                         String submittedAt,
                                         String filePath,
                                         String status,
                                         float grade,
                                         Long gradedBy,
                                         String gradedAt) {
        // --- 1) حفظ أو تحديث سجل التسليم ---
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(DBContract.AssignmentSubmission.COL_ASSIGNMENT_ID, assignmentId);
        v.put(DBContract.AssignmentSubmission.COL_USER_ID, userId);
        v.put(DBContract.AssignmentSubmission.COL_SUBMITTED_AT, submittedAt);
        v.put(DBContract.AssignmentSubmission.COL_FILE_PATH, filePath);
        v.put(DBContract.AssignmentSubmission.COL_STATUS, status);
        v.put(DBContract.AssignmentSubmission.COL_GRADE, grade);
        if (gradedBy != null && gradedBy != 0) {
            v.put(DBContract.AssignmentSubmission.COL_GRADED_BY, gradedBy);
        } else {
            v.putNull(DBContract.AssignmentSubmission.COL_GRADED_BY);
        }
        v.put(DBContract.AssignmentSubmission.COL_GRADED_AT, gradedAt);

        String where = DBContract.AssignmentSubmission.COL_ASSIGNMENT_ID + " = ? AND " +
                DBContract.AssignmentSubmission.COL_USER_ID + " = ?";
        String[] args = {
                String.valueOf(assignmentId),
                String.valueOf(userId)
        };

        int updated = db.update(DBContract.AssignmentSubmission.TABLE_NAME, v, where, args);
        if (updated == 0) {
            db.insert(DBContract.AssignmentSubmission.TABLE_NAME, null, v);
        }
        db.close();

        // --- 2) جلب رمز المقرر لاستخدامه بالرسائل والحدث ---
        NotificationRepository.CourseInfo info = notifRepo.getCourseInfo(
                new NotificationRepository.NotificationEntity(
                        0, (int) userId,
                        "", "", false,
                        "submission",
                        (int) assignmentId,
                        ""
                )
        );
        String code = info.code.isEmpty() ? "–" : info.code;

        // --- 3) إنشاء حدث لتسليم الواجب إذا كان الحالة "Submitted" ---
        if (status.equals("Submitted")) {
            Event event = new Event();
            event.setUserId(userId);
            event.setTitleEn(String.format("Assignment Submitted for %s", code));
            event.setTitleAr(String.format("تم تسليم واجب المقرر %s", code));
            event.setEventDate(submittedAt); // التأكد من أن submittedAt بتنسيق yyyy-MM-dd
            event.setType("assignment_submission");
            event.setRelatedId(assignmentId);
            eventRepository.addEvent(event);
        }

        // --- 4) نصوص الإشعار ---
        String enSubmitMsg = String.format("Your assignment for %s has been submitted.", code);
        String arSubmitMsg = String.format("تم تسليم واجب المقرر %s.", code);

        // --- 5) إنشاء السجل في DB وإرسال إشعار النظام فوراً ---
        long notifyId1 = notifRepo.create(new NotificationRepository.NotificationEntity(
                0, (int) userId,
                enSubmitMsg, arSubmitMsg,
                false,
                "submission",
                (int) assignmentId,
                DateTimeUtils.nowString()
        ));

        // اختر اللغة من SharedPreferences
        SharedPreferences lp = context.getSharedPreferences(
                "AppPreferences", Context.MODE_PRIVATE);
        String lang = lp.getString("selected_language", "en");

        appNotif.notifyOnce(
                (int) notifyId1,
                context.getString(R.string.SubmitTitle),
                lang.equals("ar") ? arSubmitMsg : enSubmitMsg
        );

        // --- 6) إذا كان هناك تقييم جديد، أنشئ الإشعار الثاني وأرسله فوراً ---
        if (updated > 0 && grade > 0) {
            String enGradedMsg = String.format("Your assignment for %s has been graded.", code);
            String arGradedMsg = String.format("تم تقييم واجب المقرر %s.", code);

            long notifyId2 = notifRepo.create(new NotificationRepository.NotificationEntity(
                    0, (int) userId,
                    enGradedMsg, arGradedMsg,
                    false,
                    "assignment_graded",
                    (int) assignmentId,
                    DateTimeUtils.nowString()
            ));
            appNotif.notifyOnce(
                    (int) notifyId2,
                    context.getString(R.string.GradedTitle),
                    lang.equals("ar") ? arGradedMsg : enGradedMsg
            );
        }
    }

    /**
     * Retrieve file paths for a submission.
     */
    public String getSubmissionFilePaths(long assignmentId, long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT " + DBContract.AssignmentSubmission.COL_FILE_PATH +
                " FROM " + DBContract.AssignmentSubmission.TABLE_NAME +
                " WHERE " + DBContract.AssignmentSubmission.COL_ASSIGNMENT_ID + " = ? AND " +
                DBContract.AssignmentSubmission.COL_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(assignmentId),
                String.valueOf(userId)
        });

        String filePaths = null;
        if (cursor.moveToFirst()) {
            filePaths = cursor.getString(cursor.getColumnIndexOrThrow(
                    DBContract.AssignmentSubmission.COL_FILE_PATH));
        }

        cursor.close();
        db.close();
        return filePaths;
    }

    /**
     * Check if the assignment is submitted.
     */
    public boolean isAssignmentSubmitted(long assignmentId, long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT " + DBContract.AssignmentSubmission.COL_STATUS +
                " FROM " + DBContract.AssignmentSubmission.TABLE_NAME +
                " WHERE " + DBContract.AssignmentSubmission.COL_ASSIGNMENT_ID + " = ? AND " +
                DBContract.AssignmentSubmission.COL_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(assignmentId),
                String.valueOf(userId)
        });

        boolean isSubmitted = false;
        if (cursor.moveToFirst()) {
            String st = cursor.getString(cursor.getColumnIndexOrThrow(
                    DBContract.AssignmentSubmission.COL_STATUS));
            isSubmitted = "Submitted".equals(st);
        }

        cursor.close();
        db.close();
        return isSubmitted;
    }
}