package com.rakib.to_do_app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

public class ReminderManager {
    private final Context context;
    private DatabaseHelper dbHelper;
    private static final String TAG = "ReminderManager";
    private static final long ONE_HOUR_MS = 60 * 60 * 1000;

    public ReminderManager(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    public void setReminder(Task task) {
        Log.d(TAG, "Calculating reminder for task: " + task.getTitle());

        long dueDateTimeMillis = task.getDueDate().getTime();
        long currentTime = System.currentTimeMillis();
        long reminderTime;

        // --- INTELLIGENT REMINDER LOGIC ---
        // 1. Try to set it 1 hour before
        if (dueDateTimeMillis - ONE_HOUR_MS > currentTime) {
            reminderTime = dueDateTimeMillis - ONE_HOUR_MS;
            Log.d(TAG, "Setting reminder 1 hour before due date.");
        }
        // 2. If 1 hour before is passed, try exact due time
        else if (dueDateTimeMillis > currentTime) {
            reminderTime = dueDateTimeMillis;
            Log.d(TAG, "1 hour warning passed. Setting reminder at exact due time.");
        }
        // 3. If due time is also passed (e.g., creating a task for right now), set for 1 min future
        else {
            reminderTime = currentTime + (60 * 1000); // 1 minute from now
            Log.d(TAG, "Time passed. Setting immediate reminder (1 min delay).");
        }

        dbHelper.resetReminderState(task.getId(), task.getStatus());
        setSingleAlarm(task.getId(), task.getTitle(), task.getDescription(), reminderTime);
        saveReminderToDatabase(task.getId(), task.getTitle(), task.getDescription(), reminderTime);
    }

    public void scheduleDailySummary() {
        Log.d(TAG, "Attempting to schedule Daily Summary...");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DailySummaryReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                888, // Unique Request Code for Daily Summary
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // --- PRODUCTION MODE: Set to 7:00 AM ---
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If 7 AM already passed today, set for tomorrow
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Schedule repeating alarm every 24 hours
        if (alarmManager != null) {
            try {
                alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                );
                // LOG SUCCESS
                Log.d(TAG, "✅ Daily Summary Scheduled! Next run at: " + new Date(calendar.getTimeInMillis()).toString());
            } catch (Exception e) {
                // LOG FAILURE
                Log.e(TAG, "❌ Failed to schedule Daily Summary: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "❌ AlarmManager is null. Cannot schedule Daily Summary.");
        }
    }

    public void scheduleNextReminder(long taskId, String title, String description, long nextAlarmTime) {
        // Just reuse the single alarm logic
        setSingleAlarm(taskId, title, description, nextAlarmTime);
    }

    private void setSingleAlarm(long taskId, String title, String description, long reminderTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("task_id", taskId);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("is_repeating_alarm", true); // Ensure repetition is enabled

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                setExactAlarm(alarmManager, reminderTime, pendingIntent);
            } else {
                // Fallback to inexact if permission missing, prompt handled elsewhere
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            }
        } else {
            setExactAlarm(alarmManager, reminderTime, pendingIntent);
        }
    }

    private void setExactAlarm(AlarmManager alarmManager, long reminderTime, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }
    }

    public void setInexactReminder(Task task) {
        setReminder(task);
    }

    public void cancelReminder(Task task) {
        cancelReminder(task.getId());
    }

    public void cancelReminder(long taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        dbHelper.deleteReminder(taskId);
        dbHelper.deleteReminderState(taskId);
    }

    private void saveReminderToDatabase(long taskId, String title, String description, long reminderTime) {
        dbHelper.saveReminder(taskId, title, description, reminderTime);
    }

    public void cancelDailySummary() {
        Log.d(TAG, "Cancelling Daily Summary..."); // LOG ADDED

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DailySummaryReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                888,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "✅ Daily Summary Cancelled."); // LOG ADDED
        }
    }
}