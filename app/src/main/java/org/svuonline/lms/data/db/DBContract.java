package org.svuonline.lms.data.db;

/**
 * يحتوي هذا الكلاس على تعريف أسماء الجداول والأعمدة في قاعدة البيانات.
 * يُستخدم لتوحيد الاستخدام وتفادي الأخطاء الإملائية عند التعامل مع SQL.
 */
public final class DBContract {

    // منع إنشاء كائن من هذا الكلاس
    private DBContract() { }

    /**
     * تعريفات جدول المستخدمين
     */
    public static final class Users {
        public static final String TABLE_NAME = "users";                       // اسم الجدول
        public static final String COL_USER_ID = "user_id";                      // معرف المستخدم (PK)
        public static final String COL_NAME_EN = "name_en";                      // الاسم بالإنجليزية
        public static final String COL_NAME_AR = "name_ar";                      // الاسم بالعربية
        public static final String COL_EMAIL = "email";                         // البريد الإلكتروني
        public static final String COL_PASSWORD_HASH = "password_hash";          // كلمة المرور (مشفرّة)
        public static final String COL_ROLE = "role";                             // دور المستخدم (طالب، مدرس، …)
        public static final String COL_PROGRAM_ID = "program_id";                 // معرف البرنامج الدراسي (FK)
        public static final String COL_LANGUAGE = "language";                     // اللغة المفضلة للمستخدم
        public static final String COL_THEME = "theme";                           // الثيم أو السمة
        public static final String COL_REMEMBER_ME = "remember_me";               // خيار "تذكرني"
        public static final String COL_NOTIFICATIONS_ENABLED = "notifications_enabled"; // تفعيل الإشعارات (BOOLEAN)
        public static final String COL_ACCOUNT_STATUS = "account_status";         // حالة الحساب (نشط، معلق، …)
        public static final String COL_PHONE = "phone";                           // رقم الهاتف
        public static final String COL_FACEBOOK_URL = "facebook_url";             // رابط فيسبوك
        public static final String COL_WHATSAPP_NUMBER = "whatsapp_number";         // رقم واتساب
        public static final String COL_TELEGRAM_HANDLE = "telegram_handle";         // معرف تلغرام
        public static final String COL_PROFILE_PICTURE = "profile_picture";         // صورة الملف الشخصي
        public static final String COL_BIO_EN = "bio_en";                           // السيرة الذاتية بالإنجليزية
        public static final String COL_BIO_AR = "bio_ar";                           // السيرة الذاتية بالعربية
        public static final String COL_CREATED_AT = "created_at";                   // تاريخ الإنشاء
        public static final String COL_UPDATED_AT = "updated_at";                   // تاريخ آخر تعديل
    }

    /**
     * تعريفات جدول السنة الدراسية
     */
    public static final class AcademicYear {
        public static final String TABLE_NAME = "academic_year";                 // اسم الجدول
        public static final String COL_YEAR_ID = "year_id";                       // معرف السنة (PK)
        public static final String COL_NAME = "name";                             // اسم السنة الدراسية
        public static final String COL_START_DATE = "start_date";                 // تاريخ بدء السنة
        public static final String COL_END_DATE = "end_date";                     // تاريخ انتهاء السنة
    }

    /**
     * تعريفات جدول الفصول الدراسية (Term)
     */
    public static final class Term {
        public static final String TABLE_NAME = "term";                         // اسم الجدول
        public static final String COL_TERM_ID = "term_id";                       // معرف الفصل (PK)
        public static final String COL_ACADEMIC_YEAR_ID = "academic_year_id";      // معرف السنة الدراسية (FK)
        public static final String COL_NAME = "name";                             // اسم الفصل
        public static final String COL_START_DATE = "start_date";                 // تاريخ بدء الفصل
        public static final String COL_END_DATE = "end_date";                     // تاريخ انتهاء الفصل
    }

    /**
     * تعريفات جدول البرامج الأكاديمية
     */
    public static final class AcademicProgram {
        public static final String TABLE_NAME = "academic_program";             // اسم الجدول
        public static final String COL_PROGRAM_ID = "program_id";                 // معرف البرنامج (PK)
        public static final String COL_CODE = "code";                             // رمز البرنامج
        public static final String COL_NAME_EN = "name_en";                       // اسم البرنامج بالإنجليزية
        public static final String COL_NAME_AR = "name_ar";                       // اسم البرنامج بالعربية
        public static final String COL_PROGRAM_DURATION = "program_duration";     // مدة البرنامج (عدد السنوات)
    }

    /**
     * تعريفات جدول المقررات الدراسية
     */
    public static final class Course {
        public static final String TABLE_NAME = "course";                       // اسم الجدول
        public static final String COL_COURSE_ID = "course_id";                   // معرف المقرر (PK)
        public static final String COL_PROGRAM_ID = "program_id";                 // معرف البرنامج الدراسي (FK)
        public static final String COL_TERM_ID = "term_id";                       // معرف الفصل الدراسي (FK)
        public static final String COL_CODE = "code";                             // رمز المقرر
        public static final String COL_NAME_EN = "name_en";                       // اسم المقرر بالإنجليزية
        public static final String COL_NAME_AR = "name_ar";                       // اسم المقرر بالعربية
        public static final String COL_CREATED_BY = "created_by";                 // معرف المستخدم الذي أنشأ المقرر (FK)
        public static final String COL_CREATED_AT = "created_at";                 // تاريخ إنشاء المقرر
        public static final String COL_CREDIT_HOURS = "credit_hours";             // عدد ساعات الاعتماد
        public static final String COL_COLOR = "color";                           // اللون المستخدم للمقرر
        public static final String COL_STATUS = "status";                         // حالة المقرر (نشط، متوقف، …)
    }

    /**
     * تعريفات جدول أقسام المقررات الدراسية
     */
    public static final class CourseSection {
        public static final String TABLE_NAME = "course_section";                // اسم الجدول
        public static final String COL_SECTION_ID = "section_id";                // معرف القسم (PK)
        public static final String COL_COURSE_ID = "course_id";                  // معرف المقرر (FK)
        public static final String COL_TITLE_EN = "title_en";                    // عنوان القسم بالإنجليزية
        public static final String COL_TITLE_AR = "title_ar";                    // عنوان القسم بالعربية
        public static final String COL_DISPLAY_ORDER = "display_order";          // رقم ترتيب العرض
    }

    /**
     * تعريفات جدول أدوات الأقسام (Section Tools)
     */
    public static final class SectionTool {
        public static final String TABLE_NAME = "section_tool";                  // اسم الجدول
        public static final String COL_TOOL_ID = "tool_id";                        // معرف الأداة (PK)
        public static final String COL_SECTION_ID = "section_id";                  // معرف القسم (FK)
        public static final String COL_NAME_EN = "name_en";                        // اسم الأداة بالإنجليزية
        public static final String COL_NAME_AR = "name_ar";                        // اسم الأداة بالعربية
        public static final String COL_ACTION_TYPE = "action_type";                // نوع الإجراء (رفع ملف، واجب، …)
    }

    /**
     * تعريفات جدول الموارد (الملفات المرفوعة)
     */
    public static final class Resource {
        public static final String TABLE_NAME = "resource";                     // اسم الجدول
        public static final String COL_RESOURCE_ID = "resource_id";               // معرف المورد (PK)
        public static final String COL_TOOL_ID = "tool_id";                       // معرف الأداة (FK)
        public static final String COL_FILE_NAME = "file_name";                   // اسم الملف
        public static final String COL_FILE_PATH = "file_path";                   // مسار أو رابط الملف
        public static final String COL_UPLOADED_BY = "uploaded_by";               // معرف المستخدم الذي رفع الملف (FK)
        public static final String COL_UPLOADED_AT = "uploaded_at";               // تاريخ رفع الملف
    }

    /**
     * تعريفات جدول تسجيل المقررات (Enrollment)
     */
    public static final class Enrollment {
        public static final String TABLE_NAME = "enrollment";                   // اسم الجدول
        public static final String COL_ENROLLMENT_ID = "enrollment_id";           // معرف التسجيل (PK)
        public static final String COL_USER_ID = "user_id";                       // معرف المستخدم (FK)
        public static final String COL_COURSE_ID = "course_id";                   // معرف المقرر (FK)
        public static final String COL_COURSE_STATUS = "course_status";           // حالة المقرر بالنسبة للطالب
        public static final String COL_IS_FAVORITE = "is_favorite";               // هل المقرر مفضل (BOOLEAN)
        public static final String COL_ENROLLED_AT = "enrolled_at";               // تاريخ التسجيل
    }

    /**
     * تعريفات جدول الواجبات
     */
    public static final class Assignment {
        public static final String TABLE_NAME = "assignment";                   // اسم الجدول
        public static final String COL_ASSIGNMENT_ID = "assignment_id";           // معرف الواجب (PK)
        public static final String COL_TOOL_ID = "tool_id";                       // معرف الأداة المرتبطة (FK)
        public static final String COL_TITLE_EN = "title_en";                     // عنوان الواجب بالإنجليزية
        public static final String COL_TITLE_AR = "title_ar";                     // عنوان الواجب بالعربية
        public static final String COL_OPEN_DATE = "open_date";                   // تاريخ فتح الواجب
        public static final String COL_DUE_DATE = "due_date";                     // تاريخ استحقاق الواجب
        public static final String COL_ASSIGNMENT_FILE = "assignment_file";       // رابط/مسار الملف الخاص بالواجب
        public static final String COL_CREATED_BY = "created_by";                 // معرف المستخدم الذي أنشأ الواجب (FK)
    }

    /**
     * تعريفات جدول تسليم الواجبات
     */
    public static final class AssignmentSubmission {
        public static final String TABLE_NAME = "assignment_submission";        // اسم الجدول
        public static final String COL_SUBMISSION_ID = "submission_id";           // معرف التسليم (PK)
        public static final String COL_ASSIGNMENT_ID = "assignment_id";           // معرف الواجب (FK)
        public static final String COL_USER_ID = "user_id";                       // معرف المستخدم الذي سلم الواجب (FK)
        public static final String COL_SUBMITTED_AT = "submitted_at";             // تاريخ تسليم الواجب
        public static final String COL_FILE_PATH = "file_path";                   // مسار الملف المسلّم
        public static final String COL_STATUS = "status";                         // حالة التسليم (مقدم، متأخر، …)
        public static final String COL_GRADE = "grade";                           // الدرجة المُحققة (FLOAT)
        public static final String COL_GRADED_BY = "graded_by";                   // معرف المستخدم الذي قام بتقدير الواجب (FK)
        public static final String COL_GRADED_AT = "graded_at";                   // تاريخ التقدير
    }

    /**
     * تعريفات جدول الإشعارات
     */
    public static final class Notification {
        public static final String TABLE_NAME = "notification";                 // اسم الجدول
        public static final String COL_NOTIFICATION_ID = "notification_id";       // معرف الإشعار (PK)
        public static final String COL_USER_ID = "user_id";                       // معرف المستخدم (FK)
        public static final String COL_CONTENT_EN = "content_en";                 // محتوى الإشعار بالإنجليزية
        public static final String COL_CONTENT_AR = "content_ar";                 // محتوى الإشعار بالعربية
        public static final String COL_IS_READ = "is_read";                       // هل تم قراءة الإشعار (BOOLEAN)
        public static final String COL_RELATED_TYPE = "related_type";             // نوع الكيان المرتبط (مثل واجب أو مقرر)
        public static final String COL_RELATED_ID = "related_id";                 // معرف الكيان المرتبط
        public static final String COL_CREATED_AT = "created_at";                 // تاريخ إنشاء الإشعار
    }

    /**
     * تعريفات جدول الأحداث
     */
    public static final class Event {
        public static final String TABLE_NAME = "event";                        // اسم الجدول
        public static final String COL_EVENT_ID = "event_id";                     // معرف الحدث (PK)
        public static final String COL_USER_ID = "user_id";                       // معرف المستخدم (FK)
        public static final String COL_TITLE_EN = "title_en";                     // عنوان الحدث بالإنجليزية
        public static final String COL_TITLE_AR = "title_ar";                     // عنوان الحدث بالعربية
        public static final String COL_EVENT_DATE = "event_date";                 // تاريخ الحدث (DATETIME)
        public static final String COL_TYPE = "type";                             // نوع الحدث (مثال: تسليم واجب)
        public static final String COL_RELATED_ID = "related_id";                 // معرف الكيان المرتبط بالحدث
    }
}