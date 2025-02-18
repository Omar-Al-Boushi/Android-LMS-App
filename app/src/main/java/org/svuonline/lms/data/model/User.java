package org.svuonline.lms.data.model;

/**
 * نموذج بيانات يمثل المستخدم في التطبيق.
 */
public class User {
    private final long userId;
    private final String nameEn;
    private final String nameAr;
    private final String email;
    private final String role;
    private final String accountStatus;
    private final String phone;
    private final String facebookUrl;
    private final String whatsappNumber;
    private final String telegramHandle;
    private final String profilePicture;
    private final String bioEn;
    private final String bioAr;
    private final int programId; // حقل جديد لمعرف البرنامج الأكاديمي

    /**
     * باني لإنشاء كائن مستخدم.
     *
     * @param userId         معرف المستخدم
     * @param nameEn         الاسم بالإنجليزية
     * @param nameAr         الاسم بالعربية
     * @param email          البريد الإلكتروني
     * @param role           دور المستخدم (مثل student)
     * @param accountStatus  حالة الحساب (مثل active)
     * @param phone          رقم الهاتف
     * @param facebookUrl    رابط فيسبوك
     * @param whatsappNumber رقم واتساب
     * @param telegramHandle معرف تيليغرام
     * @param profilePicture رابط صورة الملف الشخصي
     * @param bioEn          السيرة الذاتية بالإنجليزية
     * @param bioAr          السيرة الذاتية بالعربية
     * @param programId      معرف البرنامج الأكاديمي
     */
    public User(long userId, String nameEn, String nameAr, String email, String role, String accountStatus,
                String phone, String facebookUrl, String whatsappNumber, String telegramHandle,
                String profilePicture, String bioEn, String bioAr, int programId) {
        this.userId = userId;
        this.nameEn = nameEn;
        this.nameAr = nameAr;
        this.email = email;
        this.role = role;
        this.accountStatus = accountStatus;
        this.phone = phone;
        this.facebookUrl = facebookUrl;
        this.whatsappNumber = whatsappNumber;
        this.telegramHandle = telegramHandle;
        this.profilePicture = profilePicture;
        this.bioEn = bioEn;
        this.bioAr = bioAr;
        this.programId = programId;
    }

    // الوظائف المساعدة لاسترجاع البيانات
    public long getUserId() { return userId; }
    public String getNameEn() { return nameEn; }
    public String getNameAr() { return nameAr; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getAccountStatus() { return accountStatus; }
    public String getPhone() { return phone; }
    public String getFacebookUrl() { return facebookUrl; }
    public String getWhatsappNumber() { return whatsappNumber; }
    public String getTelegramHandle() { return telegramHandle; }
    public String getProfilePicture() { return profilePicture; }
    public String getBioEn() { return bioEn; }
    public String getBioAr() { return bioAr; }
    public int getProgramId() { return programId; }
}