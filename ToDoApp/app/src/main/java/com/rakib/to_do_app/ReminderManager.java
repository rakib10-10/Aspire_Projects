package com.rakib.to_do_app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReminderManager {
    private final Context context;
    private DatabaseHelper dbHelper;
    private static final String TAG = "ReminderManager";

    public ReminderManager(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    public void setReminder(Task task) {
        Log.d(TAG, "Setting reminder for task: " + task.getTitle());
        long dueDateMillis = task.getDueDate().getTime();

        // Set reminder 1 hour before due date for demo
        long reminderTime = dueDateMillis - (60 * 60 * 1000);

        Log.d(TAG, "Due date: " + new Date(dueDateMillis));
        Log.d(TAG, "Reminder time: " + new Date(reminderTime));
        setReminder(task.getId(), task.getTitle(), task.getDescription(), reminderTime);
    }

    public void setInexactReminder(Task task) {
        Log.d(TAG, "Setting inexact reminder for task: " + task.getTitle());
        long dueDateMillis = task.getDueDate().getTime();
        long reminderTime = dueDateMillis - (60 * 60 * 1000);
        setInexactReminder(task.getId(), task.getTitle(), task.getDescription(), reminderTime);
    }

    public void cancelReminder(Task task) {
        cancelReminder(task.getId());
    }

    public void setReminder(long taskId, String title, String description, long reminderTime) {
        Log.d(TAG, "=== SETTING REMINDER ===");
        Log.d(TAG, "Task ID: " + taskId);
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Time: " + new Date(reminderTime));
        Log.d(TAG, "Current Time: " + new Date());

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("task_id", taskId);
        intent.putExtra("title", title);
        intent.putExtra("description", description);

        Log.d(TAG, "Intent extras set: task_id=" + taskId + ", title=" + title);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Log.d(TAG, "PendingIntent created with request code: " + taskId);

        // Check if we can set exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                setExactAlarm(alarmManager, reminderTime, pendingIntent);
                Log.d(TAG, "✓ Exact alarm set for task: " + title);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
                Log.d(TAG, "✓ Inexact alarm set for task: " + title + " (no exact alarm permission)");
                requestExactAlarmPermission();
            }
        } else {
            setExactAlarm(alarmManager, reminderTime, pendingIntent);
            Log.d(TAG, "✓ Exact alarm set for task: " + title + " (Android < 12)");
        }

        // Save reminder to SQLite
        saveReminderToDatabase(taskId, title, description, reminderTime);
    }

    private void setExactAlarm(AlarmManager alarmManager, long reminderTime, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }
    }

    public void setInexactReminder(long taskId, String title, String description, long reminderTime) {
        Log.d(TAG, "=== SETTING INEXACT REMINDER ===");
        Log.d(TAG, "Task ID: " + taskId);
        Log.d(TAG, "Title: " + title);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("task_id", taskId);
        intent.putExtra("title", title);
        intent.putExtra("description", description);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        Log.d(TAG, "✓ Inexact alarm set for task: " + title);

        saveReminderToDatabase(taskId, title, description, reminderTime);
    }

    public void cancelReminder(long taskId) {
        Log.d(TAG, "Cancelling reminder for task ID: " + taskId);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        Log.d(TAG, "✓ Reminder cancelled for task ID: " + taskId);

        removeReminderFromDatabase(taskId);
    }

    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Cannot open exact alarm settings: " + e.getMessage());
            }
        }
    }

    private void saveReminderToDatabase(long taskId, String title, String description, long reminderTime) {
        dbHelper.saveReminder(taskId, title, description, reminderTime);
        Log.d(TAG, "✓ Reminder saved to SQLite for task: " + title);
    }

    private void removeReminderFromDatabase(long taskId) {
        dbHelper.deleteReminder(taskId);
        Log.d(TAG, "✓ Reminder removed from SQLite for task ID: " + taskId);
    }

    public boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true;
    }
}