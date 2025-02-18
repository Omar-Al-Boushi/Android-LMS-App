package org.svuonline.lms.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import org.svuonline.lms.R;

import java.util.HashSet;
import java.util.Set;

public class AppNotificationManager {
    private static final String PREFS_NAME   = "sent_notifications";
    private static final String KEY_SENT_SET = "sent_ids";
    private static final String CHANNEL_ID   = "lms_channel";

    private final NotificationManager systemManager;
    private final SharedPreferences prefs;
    private final Context ctx;

    public AppNotificationManager(Context context) {
        this.ctx = context.getApplicationContext();
        this.systemManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        this.prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        createChannel();  // Create notification channel on init
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Delete existing channel to apply changes
            systemManager.deleteNotificationChannel(CHANNEL_ID);

            // Sound URI from raw folder
            Uri soundUri = Uri.parse("android.resource://" + ctx.getPackageName() + "/" + R.raw.custom_notification);

            // Audio attributes
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            // Create channel
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "LMS Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("LMS notifications");
            channel.setSound(soundUri, audioAttributes);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 300, 250, 300});
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            systemManager.createNotificationChannel(channel);
        }
    }

    public void notifyOnce(int id, String title, String content) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Set<String> sent = prefs.getStringSet(KEY_SENT_SET, new HashSet<>());
        String key = String.valueOf(id);
        if (sent.contains(key)) return;

        Bitmap largeIcon = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.logo_main);
        int accentColor = ContextCompat.getColor(ctx, R.color.md_theme_primary);
        Uri soundUri = Uri.parse("android.resource://" + ctx.getPackageName() + "/" + R.raw.custom_notification);

        // 👉 "نية وهمية" فقط لجعل الإشعار قابل للنقر بدون فتح شيء
        Intent dummyIntent = new Intent();  // لا يفعل شيء
        PendingIntent pendingIntent = PendingIntent.getActivity(
                ctx,
                id,
                dummyIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_secondary)
                .setLargeIcon(largeIcon)
                .setColor(accentColor)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setSound(soundUri)
                .setVibrate(new long[]{0, 300, 250, 300})
                .setPriority(NotificationCompat.PRIORITY_HIGH)               // 👈 لظهور heads-up
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)            // 👈 نوع إشعار قابل للانخفاض
                .setContentIntent(pendingIntent)                             // 👈 يجعل الإشعار قابل للنقر
                .setAutoCancel(true);                                        // 👈 يختفي عند الضغط

        systemManager.notify(id, builder.build());

        sent.add(key);
        prefs.edit().putStringSet(KEY_SENT_SET, sent).apply();
    }
}
