package com.rakib.to_do_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "todo_app_channel";
    private static final String CHANNEL_NAME = "ToDo App Notifications";

    // Create notification channel (required for Android 8.0+)
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for task reminders and due dates");

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Get sound URI based on selection
    public static Uri getSoundUri(String soundName) {
        switch (soundName) {
            case "Gentle":
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            case "Urgent":
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            case "Melodic":
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            case "Vibrate only":
                return null; // No sound, only vibrate
            case "Silent":
                return null; // No sound and no vibrate
            case "Default":
            default:
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
    }

    // Play test sound when user selects a sound
    public static void playTestSound(Context context, String soundName) {
        try {
            Uri soundUri = getSoundUri(soundName);

            if (soundUri == null && "Vibrate only".equals(soundName)) {
                // Vibrate only
                android.os.Vibrator vibrator =
                        (android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    vibrator.vibrate(500); // Vibrate for 500ms
                }
                return;
            } else if (soundUri == null) {
                // Silent - do nothing
                return;
            }

            // Create a test notification with the selected sound
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            createNotificationChannel(context);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.outline_add_alert_24)
                    .setContentTitle("Sound Test")
                    .setContentText("Testing: " + soundName)
                    .setSound(soundUri)
                    .setAutoCancel(true);

            if (!"Silent".equals(soundName)) {
                builder.setVibrate(new long[]{0, 300, 200, 300}); // Vibrate pattern
            }

            notificationManager.notify(9999, builder.build());

        } catch (Exception e) {
            Log.e(TAG, "Error playing test sound: " + e.getMessage());
        }
    }

    // Method to create actual notifications for tasks/reminders
    public static void sendNotification(Context context, String title, String message, String soundPreference) {
        try {
            Uri soundUri = getSoundUri(soundPreference);
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            createNotificationChannel(context);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.outline_add_alert_24)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true);

            if (soundUri != null) {
                builder.setSound(soundUri);
            }

            if ("Vibrate only".equals(soundPreference) || soundUri != null) {
                builder.setVibrate(new long[]{0, 300, 200, 300});
            }

            // Use current time as notification ID to avoid overwriting
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, builder.build());

        } catch (Exception e) {
            Log.e(TAG, "Error sending notification: " + e.getMessage());
        }
    }
}