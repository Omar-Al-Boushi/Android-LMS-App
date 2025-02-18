package org.svuonline.lms.ui.data;

import android.content.Context;
import android.content.res.Resources;
import org.svuonline.lms.R;

public class NotificationCardData {
    private boolean isRead;
    private final int imageColor;
    private final String courseName;
    private final String description;
    private final String time;
    private int backgroundColor;

    public NotificationCardData(Context context, boolean isRead, int imageColor, String courseName, String description, String time) {
        this.isRead = isRead;
        this.imageColor = imageColor;
        this.courseName = courseName;
        this.description = description;
        this.time = time;
        setRead(isRead, context);
    }

    public boolean isRead() {
        return isRead;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getImageColor() {
        return imageColor;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getDescription() {
        return description;
    }

    public String getTime() {
        return time;
    }

    public void setRead(boolean isRead, Context context) {
        this.isRead = isRead;
        this.backgroundColor = isRead ? context.getResources().getColor(R.color.Custom_BackgroundColor)
                : context.getResources().getColor(R.color.notificationCourseBackground);
    }
}
