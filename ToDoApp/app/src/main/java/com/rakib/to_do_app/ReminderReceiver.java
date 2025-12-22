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
import java.util.Date;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "ReminderReceiver";
    private static final String CHANNEL_ID = "task_reminder_channel";
    private static final int MAX_ATTEMPTS = 5;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "🔴 === REMINDER RECEIVER TRIGGERED ===");

        long taskId = intent.getLongExtra("task_id", -1);
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        boolean isRepeatingAlarm = intent.getBooleanExtra("is_repeating_alarm", false);

        if (taskId == -1) return;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        TaskManager taskManager = TaskManager.getInstance(context);
        ReminderManager reminderManager = new ReminderManager(context);

        Task currentTask = dbHelper.getTask(taskId);

        if (currentTask == null) {
            reminderManager.cancelReminder(taskId);
            return;
        }

        // Check if task is already done
        if (!currentTask.getStatus().equals("running")) {
            Log.d(TAG, "⚠️ Task not running. Stopping.");
            reminderManager.cancelReminder(currentTask);
            return;
        }

        // Show the Notification
        showNotification(context, title, description, taskId);

        // Handle Repetition
        if (isRepeatingAlarm) {
            int currentAttempts = dbHelper.incrementReminderAttempt(taskId);
            Log.d(TAG, "🔄 Attempt Count: " + currentAttempts + " / " + MAX_ATTEMPTS);

            if (currentAttempts < MAX_ATTEMPTS) {
                // --- CALCULATE NEXT TIME ---
                NotificationSettings settings = dbHelper.getNotificationSettings();
                int intervalMinutes = settings.getUnfinishedNotificationInterval();
                if (intervalMinutes <= 0) intervalMinutes = 60; // Default safety

                // 🔴 IMPORTANT: SWITCHING BACK TO REAL TIME
                // long intervalMs = 30 * 1000L; // (Test Mode: 30 Seconds)
                long intervalMs = intervalMinutes * 60 * 1000L; // (Real Mode: Minutes)

                long nextAlarmTime = System.currentTimeMillis() + intervalMs;
                String nextTitle = currentTask.getTitle() + " (Reminder " + (currentAttempts + 1) + ")";

                reminderManager.scheduleNextReminder(taskId, nextTitle, description, nextAlarmTime);
                Log.d(TAG, "📅 Next alarm scheduled for: " + new Date(nextAlarmTime).toString());

            } else {
                // --- MAX ATTEMPTS REACHED: MARK AS UNFINISHED ---
                Log.d(TAG, "🛑 Max attempts reached. Marking task as UNFINISHED.");

                currentTask.setStatus("unfinished"); // 1. Change Status Object
                taskManager.updateTask(currentTask); // 2. Update Database

                // Optional: Show a final notification saying it's marked unfinished
                showNotification(context, "Task Missed: " + title, "Marked as unfinished due to lack of response.", taskId);

                reminderManager.cancelReminder(currentTask); // 3. Clean up
            }
        }
    }

    private void showNotification(Context context, String title, String description, long taskId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        createNotificationChannel(notificationManager);

        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, (int) taskId, appIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationSettings settings = new DatabaseHelper(context).getNotificationSettings();
        android.net.Uri soundUri = NotificationHelper.getSoundUri(context, settings.getNotificationSound());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.outline_add_alert_24)
                .setContentTitle(title)
                .setContentText(description)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (soundUri != null) builder.setSound(soundUri);
        else builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);

        notificationManager.notify((int) taskId, builder.build());
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Task Reminders", NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
    }
}