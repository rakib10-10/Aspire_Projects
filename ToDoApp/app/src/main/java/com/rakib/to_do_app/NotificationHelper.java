package com.rakib.to_do_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "todo_app_channel";
    private static final String CHANNEL_NAME = "ToDo App Notifications";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for task reminders and due dates");

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Resolves the correct Sound URI.
     * ORDER OF OPERATIONS IS CRITICAL HERE:
     * 1. Check for direct URI.
     * 2. Check for Standard Names (Gentle, Urgent, etc.). << MOVED UP
     * 3. Check Database for Custom URI.
     */
    public static Uri getSoundUri(Context context, String soundNameOrUri) {
        // 0. Null check
        if (soundNameOrUri == null) return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // 1. Direct URI check (content:// or file://)
        if (soundNameOrUri.contains("://")) {
            return Uri.parse(soundNameOrUri);
        }

        // 2. CHECK STANDARD NAMES FIRST
        // We must check these BEFORE the database. Otherwise, if you save "Gentle",
        // the DB check will see "Gentle" matches the DB setting and try to play the custom URI.
        switch (soundNameOrUri) {
            case "Urgent":
            case "Alert":
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            case "Melodic":
            case "Chime":
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            case "Vibrate only":
            case "Silent":
                return null; // No sound
            case "Gentle":
            case "Default":
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            // If it's "Custom" or a completely unknown name, we break and check the DB below
        }

        // 3. Check Database for Custom URI
        // Only runs if the name wasn't caught by the switch case above.
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            NotificationSettings settings = dbHelper.getNotificationSettings();
            String storedName = settings.getNotificationSound();
            String storedUri = settings.getCustomRingtoneUri();

            // Logic:
            // A. If input is explicitly "Custom", return the URI.
            // B. If input matches the stored name (e.g., user renamed it), return the URI.
            if ("Custom".equals(soundNameOrUri) || (storedName != null && storedName.equals(soundNameOrUri))) {
                if (storedUri != null && !storedUri.isEmpty()) {
                    return Uri.parse(storedUri);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving custom sound from DB: " + e.getMessage());
        }

        // 4. Final Fallback
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    public static void playTestSound(Context context, String soundName) {
        try {
            Uri soundUri = getSoundUri(context, soundName);

            if (soundUri == null) {
                Log.d(TAG, "Silent sound selected, skipping playback.");
                return;
            }

            Ringtone ringtone = RingtoneManager.getRingtone(context, soundUri);
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone.setLooping(false);
                }
                ringtone.play();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing test sound", e);
        }
    }

    public static void sendNotification(Context context, String title, String message, String soundPreference) {
        try {
            Uri soundUri = getSoundUri(context, soundPreference);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            createNotificationChannel(context);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.outline_add_alert_24)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            if (soundUri != null) {
                builder.setSound(soundUri);
            }

            if ("Vibrate only".equals(soundPreference) || (soundUri != null && !"Silent".equals(soundPreference))) {
                builder.setVibrate(new long[]{0, 500, 200, 500});
            }

            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, builder.build());

        } catch (Exception e) {
            Log.e(TAG, "Error sending notification: " + e.getMessage());
        }
    }
}