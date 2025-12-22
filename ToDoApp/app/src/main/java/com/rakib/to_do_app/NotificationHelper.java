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

    // Static variable to stop previous sound before playing new one
    private static Ringtone currentRingtone;

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

    public static Uri getSoundUri(Context context, String soundNameOrUri) {
        // 1. Handle Null: Return Default
        if (soundNameOrUri == null || soundNameOrUri.isEmpty()) {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        // 2. Handle Direct URIs (content:// or file://)
        // This is useful if you are passing the raw URI directly
        if (soundNameOrUri.contains("://")) {
            try {
                return Uri.parse(soundNameOrUri);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse direct URI: " + soundNameOrUri);
            }
        }

        // 3. Handle Preset Hardcoded Names
        switch (soundNameOrUri) {
            case "Urgent":
            case "Alert":
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            case "Melodic":
            case "Chime":
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            case "Vibrate only":
            case "Silent":
                return null;
            case "Gentle":
            case "Default":
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        // 4. Handle Custom Sounds (Database Lookup)
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);

            // Query the specific table for this specific name
            String customUriString = dbHelper.getCustomSoundUri(soundNameOrUri);

            if (customUriString != null && !customUriString.isEmpty()) {
                return Uri.parse(customUriString);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error looking up custom sound in DB: " + e.getMessage());
        }

        // 5. Fallback if name not found in DB
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    public static void playTestSound(Context context, String soundName) {
        try {
            // Stop previously playing sound to prevent overlap
            if (currentRingtone != null) {
                if (currentRingtone.isPlaying()) {
                    currentRingtone.stop();
                }
                currentRingtone = null;
            }

            Uri soundUri = getSoundUri(context, soundName);

            // If "Silent" or null, just return
            if (soundUri == null) return;

            currentRingtone = RingtoneManager.getRingtone(context, soundUri);

            if (currentRingtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    currentRingtone.setLooping(false);
                }
                currentRingtone.play();
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

            // Logic for vibration
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