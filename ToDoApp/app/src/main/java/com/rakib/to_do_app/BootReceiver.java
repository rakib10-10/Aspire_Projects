package com.rakib.to_do_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "Device restarted. Rescheduling all alarms...");

            DatabaseHelper dbHelper = new DatabaseHelper(context);
            ReminderManager reminderManager = new ReminderManager(context);
            SessionManager sessionManager = new SessionManager(context);

            // 1. Reschedule Daily Summary (if enabled)
            if (dbHelper.getNotificationSettings().isDailySummaryEnabled()) {
                reminderManager.scheduleDailySummary();
            }

            // 2. Reschedule Task Reminders
            // Note: We need to get tasks for the logged-in user.
            // If SessionManager persists login state, use that email.
            String currentUserEmail = sessionManager.getUserEmail();

            if (currentUserEmail != null) {
                List<Task> allTasks = dbHelper.getAllTasks(currentUserEmail);
                for (Task task : allTasks) {
                    if ("running".equals(task.getStatus())) {
                        reminderManager.setReminder(task);
                    }
                }
            }
        }
    }
}