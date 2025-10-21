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

    public ReminderManager(Context context) {
        this.context = context;
    }

    // Method that accepts Task object - MAIN METHOD TO USE
    public void setReminder(Task task) {
        setReminder(task.getId(), task.getTitle(), task.getDescription(), task.getDueDate().getTime());
    }

    // Method that accepts Task object for inexact alarms
    public void setInexactReminder(Task task) {
        setInexactReminder(task.getId(), task.getTitle(), task.getDescription(), task.getDueDate().getTime());
    }

    // Method to cancel reminder for a Task
    public void cancelReminder(Task task) {
        cancelReminder(task.getId());
    }

    // Original setReminder method with permission handling
    public void setReminder(long taskId, String title, String description, long reminderTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e("ReminderManager", "AlarmManager is null");
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

        // Check if we can set exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                // We have permission, set exact alarm
                setExactAlarm(alarmManager, reminderTime, pendingIntent);
                Log.d("ReminderManager", "Exact alarm set for task: " + title);
            } else {
                // Fallback to inexact alarm
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
                Log.d("ReminderManager", "Inexact alarm set for task: " + title + " (no exact alarm permission)");
                // Request permission from user
                requestExactAlarmPermission();
            }
        } else {
            // For older Android versions, use exact alarms directly
            setExactAlarm(alarmManager, reminderTime, pendingIntent);
            Log.d("ReminderManager", "Exact alarm set for task: " + title + " (Android < 12)");
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
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e("ReminderManager", "AlarmManager is null");
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
        Log.d("ReminderManager", "Inexact alarm set for task: " + title);

        // Save reminder to Firestore
        saveReminderToFirestore(taskId, title, description, reminderTime);
    }

    // Method to cancel reminder by task ID
    public void cancelReminder(long taskId) {
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

        Log.d("ReminderManager", "Reminder cancelled for task ID: " + taskId);

        // Remove reminder from Firestore
        removeReminderFromFirestore(taskId);
    }

    // Method to set reminder with date and time strings (convenience method)
    public void setReminderWithDateTime(long taskId, String title, String description, String dateStr, String timeStr) {
        try {
            // Parse date and time to get timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            String dateTimeStr = dateStr + " " + convertTo24HourFormat(timeStr);
            Date dateTime = dateFormat.parse(dateTimeStr);

            if (dateTime != null) {
                setReminder(taskId, title, description, dateTime.getTime());
            }
        } catch (ParseException e) {
            Log.e("ReminderManager", "Error parsing date/time: " + e.getMessage());
            // Fallback: set reminder for the date at 9 AM
            setReminderForDate(taskId, title, description, dateStr);
        }
    }

    // Helper method to convert 12-hour format to 24-hour format
    private String convertTo24HourFormat(String time12Hour) {
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            SimpleDateFormat storageFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = displayFormat.parse(time12Hour);
            return storageFormat.format(date);
        } catch (ParseException e) {
            return "09:00"; // Default to 9:00 AM if parsing fails
        }
    }

    // Fallback method to set reminder for a specific date
    private void setReminderForDate(long taskId, String title, String description, String dateStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = dateFormat.parse(dateStr);

            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, 9); // 9 AM
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                setReminder(taskId, title, description, calendar.getTimeInMillis());
            }
        } catch (ParseException e) {
            Log.e("ReminderManager", "Error parsing date: " + e.getMessage());
        }
    }

    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e("ReminderManager", "Cannot open exact alarm settings: " + e.getMessage());
                // Fallback: open app info screen
                try {
                    Intent appInfoIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    appInfoIntent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
                    appInfoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(appInfoIntent);
                } catch (Exception ex) {
                    Log.e("ReminderManager", "Cannot open app info: " + ex.getMessage());
                }
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
                            Log.d("ReminderManager", "Reminder saved to Firestore for task: " + title))
                    .addOnFailureListener(e ->
                            Log.e("ReminderManager", "Error saving reminder to Firestore: " + e.getMessage()));
        } catch (Exception e) {
            Log.e("ReminderManager", "Error saving to Firestore: " + e.getMessage());
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
                            Log.d("ReminderManager", "Reminder removed from Firestore for task ID: " + taskId))
                    .addOnFailureListener(e ->
                            Log.e("ReminderManager", "Error removing reminder from Firestore: " + e.getMessage()));
        } catch (Exception e) {
            Log.e("ReminderManager", "Error removing from Firestore: " + e.getMessage());
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

    // Method to reschedule all reminders (useful after app update or device restart)
    public void rescheduleAllReminders() {
        // This would typically load reminders from Firestore and reschedule them
        // Implementation depends on your app's architecture
        Log.d("ReminderManager", "Reschedule all reminders called");
    }
}