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
    // We use a specific ID for general notifications, distinct from the dynamic reminder ones
    private static final String GENERAL_CHANNEL_ID = "todo_general_channel";
    private static Ringtone currentRingtone;

    public static Uri getSoundUri(Context context, String soundNameOrUri) {
        // 1. Handle Null: Return Default
        if (soundNameOrUri == null || soundNameOrUri.isEmpty()) {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        // 2. Handle Direct URIs (content:// or file://)
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

            // A. Check if user selected "Custom" in general settings
            if ("Custom".equalsIgnoreCase(soundNameOrUri)) {
                NotificationSettings settings = dbHelper.getNotificationSettings();
                String customUri = settings.getCustomRingtoneUri();
                if (customUri != null && !customUri.isEmpty()) {
                    return Uri.parse(customUri);
                }
            }

            // B. Check specific custom sound names
            String customUriString = dbHelper.getCustomSoundUri(soundNameOrUri);
            if (customUriString != null && !customUriString.isEmpty()) {
                return Uri.parse(customUriString);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error looking up custom sound: " + e.getMessage());
        }

        // 5. Fallback
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

    /**
     * RESTORED: This method is useful for "Test Notification" buttons
     * or generic app alerts that are NOT task reminders.
     */
    public static void sendNotification(Context context, String title, String message, String soundPreference) {
        try {
            Uri soundUri = getSoundUri(context, soundPreference);
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Generate a dynamic ID just like we do in ReminderReceiver
            // This ensures the "Test" notification actually plays the right sound
            String cleanName = (soundPreference == null) ? "default" : soundPreference.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
            String dynamicId = "test_channel_" + cleanName;

            createDynamicChannel(notificationManager, dynamicId, soundUri);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, dynamicId)
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

    // Helper to create channel dynamically (Duplicate of ReminderReceiver logic but necessary here for testing)
    private static void createDynamicChannel(NotificationManager manager, String channelId, Uri soundUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Check if exists first
            if (manager.getNotificationChannel(channelId) != null) return;

            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("General alerts");

            if (soundUri != null) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                channel.setSound(soundUri, audioAttributes);
            }

            manager.createNotificationChannel(channel);
        }
    }
}