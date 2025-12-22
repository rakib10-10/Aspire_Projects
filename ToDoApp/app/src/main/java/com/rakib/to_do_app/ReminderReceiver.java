package com.rakib.to_do_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import java.util.Date;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";
    private static final int MAX_ATTEMPTS = 5;

    @Override
    public void onReceive(Context context, Intent intent) {
        long taskId = intent.getLongExtra("task_id", -1);
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        boolean isRepeatingAlarm = intent.getBooleanExtra("is_repeating_alarm", false);

        if (taskId == -1) return;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        ReminderManager reminderManager = new ReminderManager(context);
        Task currentTask = dbHelper.getTask(taskId);

        if (currentTask == null || !"running".equals(currentTask.getStatus())) {
            reminderManager.cancelReminder(taskId);
            return;
        }

        // --- SHOW NOTIFICATION ---
        showNotification(context, title, description, taskId);

        // --- REPEAT LOGIC ---
        if (isRepeatingAlarm) {
            int currentAttempts = dbHelper.incrementReminderAttempt(taskId);

            if (currentAttempts < MAX_ATTEMPTS) {
                // Schedule next alarm
                NotificationSettings settings = dbHelper.getNotificationSettings();
                int intervalMinutes = settings.getUnfinishedNotificationInterval();
                if (intervalMinutes <= 0) intervalMinutes = 30; // Default safety

                long intervalMs = intervalMinutes * 60 * 1000L; // Real Time (Minutes)
                // long intervalMs = 15 * 1000L; // Uncomment for 15-second testing

                long nextAlarmTime = System.currentTimeMillis() + intervalMs;
                String nextTitle = currentTask.getTitle() + " (Reminder " + (currentAttempts + 1) + ")";

                reminderManager.scheduleNextReminder(taskId, nextTitle, description, nextAlarmTime);

            } else {
                // Max Attempts Reached -> Mark as Unfinished
                currentTask.setStatus("unfinished");
                dbHelper.updateTask(currentTask);

                // Notify UI to refresh
                context.sendBroadcast(new Intent("com.rakib.to_do_app.UPDATE_UI"));

                // Update Reminder State
                dbHelper.updateReminderStatus(taskId, "max_reached");

                // Final Notification
                showNotification(context, "Task Missed: " + title, "Task marked as unfinished.", taskId);

                // Stop future alarms
                reminderManager.cancelReminder(currentTask);
            }
        }
    }

    private void showNotification(Context context, String title, String description, long taskId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        // 1. Get Sound Settings
        NotificationSettings settings = new DatabaseHelper(context).getNotificationSettings();
        String soundName = settings.getNotificationSound();
        Uri soundUri = NotificationHelper.getSoundUri(context, soundName);

        // 2. Generate Dynamic Channel ID (Crucial for unique sounds)
        String cleanSoundName = (soundName == null) ? "default" : soundName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        String dynamicChannelId = "channel_sound_" + cleanSoundName;

        // 3. Create Channel
        createNotificationChannel(notificationManager, dynamicChannelId, soundUri);

        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, (int) taskId, appIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, dynamicChannelId)
                .setSmallIcon(R.drawable.outline_add_alert_24)
                .setContentTitle(title)
                .setContentText(description)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (soundUri != null) {
            builder.setSound(soundUri);
        } else {
            builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
        }

        notificationManager.notify((int) taskId, builder.build());
    }

    private void createNotificationChannel(NotificationManager notificationManager, String channelId, Uri soundUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Check if this SPECIFIC channel exists (reuse if yes)
            if (notificationManager.getNotificationChannel(channelId) != null) {
                return;
            }

            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Task Reminders", // Display name in settings
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            channel.setDescription("Notifications for task reminders");

            if (soundUri != null) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                channel.setSound(soundUri, audioAttributes);
            }

            notificationManager.createNotificationChannel(channel);
        }
    }
}