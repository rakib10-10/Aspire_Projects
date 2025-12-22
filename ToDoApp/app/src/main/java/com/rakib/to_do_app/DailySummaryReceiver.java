package com.rakib.to_do_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DailySummaryReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "daily_summary_channel";
    private static final int NOTIFICATION_ID = 888;

    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SessionManager sessionManager = new SessionManager(context); // 1. Initialize SessionManager

        // 2. Check if feature is enabled
        if (!dbHelper.getNotificationSettings().isDailySummaryEnabled()) {
            return;
        }

        // 3. Get Today's Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        String todayDate = sdf.format(Calendar.getInstance().getTime());

        // 4. Get Current User Email
        String userEmail = sessionManager.getUserEmail();

        // 5. Fetch tasks for THIS specific user
        List<Task> allTasks = dbHelper.getAllTasks(userEmail); // Fixed: Passed email argument

        int todayTaskCount = 0;
        int pendingCount = 0;

        for (Task task : allTasks) {
            if (task.getDate() != null && task.getDate().equals(todayDate)) {
                todayTaskCount++;
                if ("running".equals(task.getStatus())) {
                    pendingCount++;
                }
            }
        }

        // 6. Only show notification if there are tasks
        if (todayTaskCount > 0) {
            showSummaryNotification(context, todayTaskCount, pendingCount);
        }
    }

    private void showSummaryNotification(Context context, int total, int pending) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Daily Summary", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, NOTIFICATION_ID, appIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String message = "You have " + total + " tasks scheduled for today. " + pending + " are still pending.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.outline_add_alert_24)
                .setContentTitle("📅 Today's Plan")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}