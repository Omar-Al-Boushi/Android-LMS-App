package org.svuonline.lms.ui.data;

import android.content.Context;
import androidx.core.content.ContextCompat;
import org.svuonline.lms.R;

public class ProfileData {
    private boolean isCurrentUser;
    private String profileName;
    private int profileImageRes;
    private String profileBio;
    private String contactPhone;
    private String contactWhatsapp;
    private String contactFacebook;
    private String contactEmail;
    private String contactTelegram;
    private int headerColor;
    private int textColor;

    // Constructor
    public ProfileData(boolean isCurrentUser, String profileName, int profileImageRes, String profileBio,
                       String contactPhone, String contactWhatsapp, String contactFacebook,
                       String contactEmail, String contactTelegram, int headerColor, int textColor) {
        this.isCurrentUser = isCurrentUser;
        this.profileName = profileName;
        this.profileImageRes = profileImageRes;
        this.profileBio = profileBio;
        this.contactPhone = contactPhone;
        this.contactWhatsapp = contactWhatsapp;
        this.contactFacebook = contactFacebook;
        this.contactEmail = contactEmail;
        this.contactTelegram = contactTelegram;
        this.headerColor = headerColor;
        this.textColor = textColor;
    }

    // Getters
    public boolean isCurrentUser() { return isCurrentUser; }
    public String getProfileName() { return profileName; }
    public int getProfileImageRes() { return profileImageRes; }
    public String getProfileBio() { return profileBio; }
    public String getContactPhone() { return contactPhone; }
    public String getContactWhatsapp() { return contactWhatsapp; }
    public String getContactFacebook() { return contactFacebook; }
    public String getContactEmail() { return contactEmail; }
    public String getContactTelegram() { return contactTelegram; }
    public int getHeaderColor() { return headerColor; }
    public int getTextColor() { return textColor; }

    /**
     * طريقة ثابتة لتوفير بيانات تجريبية للبروفايل.
     * عند استخدام isCurrentUser = true سيتم إرجاع بيانات بروفايل المستخدم الحالي،
     * وإلا سيتم إرجاع بيانات بروفايل شخص آخر.
     */
    public static ProfileData getFakeProfile(Context context, boolean isCurrentUser) {
        if (isCurrentUser) {
            return new ProfileData(
                    true,
                    "Omar Al Boushi",
                    R.drawable.omar_photo,
                    "This is my bio. I am a passionate developer and teacher.",
                    "0123456789",
                    "0123456789",
                    "https://facebook.com/omar",
                    "omar@example.com",
                    "omartelegram",
                    ContextCompat.getColor(context, R.color.Custom_MainColorBlue),
                    ContextCompat.getColor(context, R.color.md_theme_primary)
            );
        } else {
            return new ProfileData(
                    false,
                    "John Doe",
                    R.drawable.ic_launcher_background,
                    "This is John Doe's bio. He is an active participant.",
                    "0987654321",
                    "0987654321",
                    "https://facebook.com/johndoe",
                    "johndoe@example.com",
                    "johndoe_telegram",
                    ContextCompat.getColor(context, R.color.Custom_MainColorTeal),
                    ContextCompat.getColor(context, R.color.md_theme_primary)
            );
        }
    }
}
