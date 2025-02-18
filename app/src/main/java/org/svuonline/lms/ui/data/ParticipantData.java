package org.svuonline.lms.ui.data;

public class ParticipantData {
    private long userId;
    private String username; // مشتق من البريد الإلكتروني
    private String nameEn;
    private String nameAr;
    private String role; // الدور (مثل student، doctor، إلخ)
    private String bioEn; // وصف المستخدم (bio)
    private String bioAr;
    private String profilePicture; // اسم مورد الصورة (مثل "mazen_photo")
    private boolean isFavorite; // حالة المفضلة

    public ParticipantData(long userId, String username, String nameEn, String nameAr, String role,
                           String bioEn, String bioAr, String profilePicture, boolean isFavorite) {
        this.userId = userId;
        this.username = username;
        this.nameEn = nameEn;
        this.nameAr = nameAr;
        this.role = role;
        this.bioEn = bioEn;
        this.bioAr = bioAr;
        this.profilePicture = profilePicture;
        this.isFavorite = isFavorite;
    }

    // Getters
    public long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getNameEn() { return nameEn; }
    public String getNameAr() { return nameAr; }
    public String getRole() { return role; }
    public String getBioEn() { return bioEn; }
    public String getBioAr() { return bioAr; }
    public String getProfilePicture() { return profilePicture; }
    public boolean isFavorite() { return isFavorite; }

    // Setter for favorite
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}