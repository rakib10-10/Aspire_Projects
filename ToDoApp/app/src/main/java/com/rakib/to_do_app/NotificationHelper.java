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

    /**
     * FIX: Handles stored URI strings (for custom sounds) or predefined names.
     */
    public static Uri getSoundUri(String soundNameOrUri) {
        if (soundNameOrUri == null) return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // 1. Check if it's a stored URI string (e.g., "content://...")
        if (soundNameOrUri.startsWith("content://")) {
            return Uri.parse(soundNameOrUri);
        }

        // 2. Check predefined names
        switch (soundNameOrUri) {
            case "Gentle":
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            case "Urgent":
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            case "Melodic":
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            case "Vibrate only":
            case "Silent":
                return null; // No sound
            case "Default":
            default:
                return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
    }

    /**
     * FIX: Accepts URI string/name and uses the updated getSoundUri method.
     */
    public static void playTestSound(Context context, String soundNameOrUri) {
        try {
            Uri soundUri = getSoundUri(soundNameOrUri);

            if (soundUri == null && "Vibrate only".equals(soundNameOrUri)) {
                // Vibrate only
                android.os.Vibrator vibrator =
                        (android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    // Check for deprecated method use in a full project
                    vibrator.vibrate(500);
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
                    .setSmallIcon(R.drawable.outline_add_alert_24) // Assuming this drawable exists
                    .setContentTitle("Sound Test")
                    .setContentText("Testing: " + soundNameOrUri)
                    .setSound(soundUri)
                    .setAutoCancel(true);

            if (!"Silent".equals(soundNameOrUri)) {
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

            if ("Vibrate only".equals(soundPreference) || (soundUri != null && !"Silent".equals(soundPreference))) {
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