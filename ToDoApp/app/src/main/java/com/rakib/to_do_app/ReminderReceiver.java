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

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "=== REMINDER RECEIVER TRIGGERED ===");

        // Get the correct extras that were sent from ReminderManager
        long taskId = intent.getLongExtra("task_id", -1);
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");

        Log.d(TAG, "Received task ID: " + taskId);
        Log.d(TAG, "Received title: " + title);
        Log.d(TAG, "Received description: " + description);

        if (title == null || title.isEmpty()) {
            Log.e(TAG, "Title is null or empty! Cannot show notification");
            title = "Task Reminder";
        }

        if (description == null) {
            description = "You have a task reminder";
        }

        showNotification(context, title, description, taskId);
    }

    private void showNotification(Context context, String title, String description, long taskId) {
        Log.d(TAG, "Showing notification: " + title);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            Log.e(TAG, "NotificationManager is null!");
            return;
        }

        createNotificationChannel(notificationManager);

        // Create intent to open app when notification is clicked
        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) taskId,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.outline_add_alert_24) // Make sure you have this icon
                .setContentTitle("Task Reminder")
                .setContentText(title)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(title + "\n\n" + description))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL); // Sound, vibration, etc.

        // Show notification with unique ID
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
        Log.d(TAG, "✓ Notification displayed with ID: " + notificationId);
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
            Log.d(TAG, "Notification channel created");
        }
    }
}