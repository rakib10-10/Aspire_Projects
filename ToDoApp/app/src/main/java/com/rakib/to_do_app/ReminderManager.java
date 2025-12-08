package com.rakib.to_do_app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
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
        Log.d(TAG, "Setting initial reminder sequence for task: " + task.getTitle());

        long dueDateTimeMillis = task.getDueDate().getTime();
        long reminderTime = dueDateTimeMillis - ONE_HOUR_MS;

        dbHelper.resetReminderState(task.getId(), task.getStatus());

        setSingleAlarm(task.getId(), task.getTitle(), task.getDescription(), reminderTime);

        saveReminderToDatabase(task.getId(), task.getTitle(), task.getDescription(), reminderTime);
    }


    public void scheduleNextReminder(long taskId, String title, String description, long nextAlarmTime) {
        setSingleAlarm(taskId, title, description, nextAlarmTime);
    }


    private void setSingleAlarm(long taskId, String title, String description, long reminderTime) {
        Log.d(TAG, "=== SETTING SINGLE ALARM (ID: " + taskId + ") ===");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("task_id", taskId);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("is_repeating_alarm", true);

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
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
                requestExactAlarmPermission();
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
        long dueDateTimeMillis = task.getDueDate().getTime();
        long reminderTime = dueDateTimeMillis - ONE_HOUR_MS;
        setSingleAlarm(task.getId(), task.getTitle(), task.getDescription(), reminderTime);
        saveReminderToDatabase(task.getId(), task.getTitle(), task.getDescription(), reminderTime);
    }

    public void cancelReminder(Task task) {
        cancelReminder(task.getId());
    }


    public void cancelReminder(long taskId) {
        Log.d(TAG, "Cancelling reminder sequence for task ID: " + taskId);
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

        dbHelper.deleteReminder(taskId);
        dbHelper.deleteReminderState(taskId);

        Log.d(TAG, "✓ Reminder sequence cancelled for task ID: " + taskId);
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
    }
}