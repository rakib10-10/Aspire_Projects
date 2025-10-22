package com.rakib.to_do_app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReminderManager {
    private final Context context;
    private static final String TAG = "ReminderManager";

    public ReminderManager(Context context) {
        this.context = context;
    }

    // Method that accepts Task object - MAIN METHOD TO USE
    public void setReminder(Task task) {
        Log.d(TAG, "Setting reminder for task: " + task.getTitle());
        long dueDateMillis = task.getDueDate().getTime();
        Log.d(TAG, "Due date: " + new Date(dueDateMillis));
        setReminder(task.getId(), task.getTitle(), task.getDescription(), dueDateMillis);
    }

    // Method that accepts Task object for inexact alarms
    public void setInexactReminder(Task task) {
        Log.d(TAG, "Setting inexact reminder for task: " + task.getTitle());
        setInexactReminder(task.getId(), task.getTitle(), task.getDescription(), task.getDueDate().getTime());
    }

    // Method to cancel reminder for a Task
    public void cancelReminder(Task task) {
        cancelReminder(task.getId());
    }

    // Original setReminder method with permission handling
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
                // We have permission, set exact alarm
                setExactAlarm(alarmManager, reminderTime, pendingIntent);
                Log.d(TAG, "✓ Exact alarm set for task: " + title);
            } else {
                // Fallback to inexact alarm
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
                Log.d(TAG, "✓ Inexact alarm set for task: " + title + " (no exact alarm permission)");
                // Request permission from user
                requestExactAlarmPermission();
            }
        } else {
            // For older Android versions, use exact alarms directly
            setExactAlarm(alarmManager, reminderTime, pendingIntent);
            Log.d(TAG, "✓ Exact alarm set for task: " + title + " (Android < 12)");
        }

        // Save reminder to Firestore
        saveReminderToFirestore(taskId, title, description, reminderTime);
    }

    // Helper method to set exact alarm based on Android version
    private void setExactAlarm(AlarmManager alarmManager, long reminderTime, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }
    }

    // Alternative method using inexact alarms (no permission required)
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

        // Use set() instead of setExact() - this doesn't require special permission
        alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        Log.d(TAG, "✓ Inexact alarm set for task: " + title);

        // Save reminder to Firestore
        saveReminderToFirestore(taskId, title, description, reminderTime);
    }

    // Method to cancel reminder by task ID
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

        // Remove reminder from Firestore
        removeReminderFromFirestore(taskId);
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

    // Save reminder to Firestore for persistence
    private void saveReminderToFirestore(long taskId, String title, String description, long reminderTime) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> reminder = new HashMap<>();
            reminder.put("taskId", taskId);
            reminder.put("title", title);
            reminder.put("description", description);
            reminder.put("reminderTime", reminderTime);
            reminder.put("createdAt", System.currentTimeMillis());

            db.collection("reminders")
                    .document(String.valueOf(taskId))
                    .set(reminder)
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "✓ Reminder saved to Firestore for task: " + title))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error saving reminder to Firestore: " + e.getMessage()));
        } catch (Exception e) {
            Log.e(TAG, "Error saving to Firestore: " + e.getMessage());
        }
    }

    // Remove reminder from Firestore
    private void removeReminderFromFirestore(long taskId) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("reminders")
                    .document(String.valueOf(taskId))
                    .delete()
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "✓ Reminder removed from Firestore for task ID: " + taskId))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error removing reminder from Firestore: " + e.getMessage()));
        } catch (Exception e) {
            Log.e(TAG, "Error removing from Firestore: " + e.getMessage());
        }
    }

    // Check if exact alarm permission is available (Android 12+)
    public boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true; // Permission not required for Android < 12
    }
}