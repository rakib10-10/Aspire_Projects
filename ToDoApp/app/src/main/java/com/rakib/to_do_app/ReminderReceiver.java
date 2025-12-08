package com.rakib.to_do_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";
    private static final String CHANNEL_ID = "task_reminder_channel";
    private static final long REPEAT_INTERVAL_MS = 15 * 60 * 1000; // 15 minutes
    private static final int MAX_ATTEMPTS = 3;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "=== REMINDER RECEIVER TRIGGERED ===");

        long taskId = intent.getLongExtra("task_id", -1);
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");


        boolean isRepeatingAlarm = intent.getBooleanExtra("is_repeating_alarm", false);

        if (taskId == -1) {
            Log.e(TAG, "Received invalid task ID.");
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        TaskManager taskManager = TaskManager.getInstance(context);
        ReminderManager reminderManager = new ReminderManager(context);


        Task currentTask = dbHelper.getTask(taskId);

        if (currentTask == null) {
            Log.w(TAG, "Task not found in DB, cancelling future alarms for ID: " + taskId);

            reminderManager.cancelReminder(taskId);
            return;
        }


        if (!currentTask.getStatus().equals("running")) {
            Log.d(TAG, "Task status changed to " + currentTask.getStatus() + ". Cancelling reminder sequence.");
            reminderManager.cancelReminder(currentTask);
            return;
        }


        showNotification(context, title, description, taskId);


        if (isRepeatingAlarm) {
            int currentAttempts = dbHelper.incrementReminderAttempt(taskId);

            if (currentAttempts < MAX_ATTEMPTS) {
                // Schedule the next alarm
                long nextAlarmTime = System.currentTimeMillis() + REPEAT_INTERVAL_MS;
                String nextTitle = currentTask.getTitle() + " (Attempt " + (currentAttempts + 1) + "/" + MAX_ATTEMPTS + ")";
                reminderManager.scheduleNextReminder(taskId, nextTitle, description, nextAlarmTime);
                Log.d(TAG, "Scheduled next reminder for attempt " + (currentAttempts + 1));
            } else {

                Log.d(TAG, "Max attempts reached for task " + taskId + ". Marking as unfinished.");


                currentTask.setStatus("unfinished");
                taskManager.updateTask(currentTask);

                // Cancel the entire sequence and delete state
                reminderManager.cancelReminder(currentTask);
            }
        }
    }

    private void showNotification(Context context, String title, String description, long taskId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            return;
        }

        createNotificationChannel(notificationManager);

        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) taskId,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        NotificationSettings settings = new DatabaseHelper(context).getNotificationSettings();
        android.net.Uri soundUri = NotificationHelper.getSoundUri(context, settings.getNotificationSound());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.outline_add_alert_24)
                .setContentTitle("🚨 Task Reminder")
                .setContentText(title)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(title + "\n\n" + description))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (soundUri != null) {
            builder.setSound(soundUri);
        } else {
            builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
        }

        int notificationId = (int) taskId;
        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for task reminders");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);

            notificationManager.createNotificationChannel(channel);
        }
    }
}