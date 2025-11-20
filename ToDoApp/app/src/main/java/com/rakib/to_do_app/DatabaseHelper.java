package com.rakib.to_do_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    // Database Information
    private static final String DATABASE_NAME = "TodoApp.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_USER_PROFILE = "user_profile";
    private static final String TABLE_NOTIFICATION_SETTINGS = "notification_settings";
    private static final String TABLE_REMINDERS = "reminders";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // Tasks Table - column names
    private static final String KEY_TASK_TITLE = "title";
    private static final String KEY_TASK_DESCRIPTION = "description";
    private static final String KEY_TASK_DATE = "date";
    private static final String KEY_TASK_START_TIME = "start_time";
    private static final String KEY_TASK_END_TIME = "end_time";
    private static final String KEY_TASK_CATEGORY = "category";
    private static final String KEY_TASK_STATUS = "status";

    // User Profile Table - column names
    private static final String KEY_PROFILE_NAME = "name";
    private static final String KEY_PROFILE_EMAIL = "email";
    private static final String KEY_PROFILE_PHONE = "phone";
    private static final String KEY_PROFILE_BIO = "bio";
    private static final String KEY_PROFILE_IMAGE_BASE64 = "profile_image_base64";

    // Notification Settings Table - column names
    private static final String KEY_NOTIF_TASK_REMINDERS = "task_reminders_enabled";
    private static final String KEY_NOTIF_DUE_DATE_ALERTS = "due_date_alerts_enabled";
    private static final String KEY_NOTIF_DAILY_SUMMARY = "daily_summary_enabled";
    private static final String KEY_NOTIF_DEFAULT_REMINDER_TIME = "default_reminder_time";
    private static final String KEY_NOTIF_SOUND = "notification_sound";

    // Reminders Table - column names
    private static final String KEY_REMINDER_TASK_ID = "task_id";
    private static final String KEY_REMINDER_TITLE = "title";
    private static final String KEY_REMINDER_DESCRIPTION = "description";
    private static final String KEY_REMINDER_TIME = "reminder_time";

    // Table Create Statements
    private static final String CREATE_TABLE_TASKS = "CREATE TABLE " + TABLE_TASKS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_TASK_TITLE + " TEXT,"
            + KEY_TASK_DESCRIPTION + " TEXT,"
            + KEY_TASK_DATE + " TEXT,"
            + KEY_TASK_START_TIME + " TEXT,"
            + KEY_TASK_END_TIME + " TEXT,"
            + KEY_TASK_CATEGORY + " TEXT,"
            + KEY_TASK_STATUS + " TEXT DEFAULT 'running',"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String CREATE_TABLE_USER_PROFILE = "CREATE TABLE " + TABLE_USER_PROFILE + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_PROFILE_NAME + " TEXT,"
            + KEY_PROFILE_EMAIL + " TEXT,"
            + KEY_PROFILE_PHONE + " TEXT,"
            + KEY_PROFILE_BIO + " TEXT,"
            + KEY_PROFILE_IMAGE_BASE64 + " TEXT,"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String CREATE_TABLE_NOTIFICATION_SETTINGS = "CREATE TABLE " + TABLE_NOTIFICATION_SETTINGS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_NOTIF_TASK_REMINDERS + " INTEGER DEFAULT 1,"
            + KEY_NOTIF_DUE_DATE_ALERTS + " INTEGER DEFAULT 1,"
            + KEY_NOTIF_DAILY_SUMMARY + " INTEGER DEFAULT 0,"
            + KEY_NOTIF_DEFAULT_REMINDER_TIME + " TEXT DEFAULT '15 minutes before',"
            + KEY_NOTIF_SOUND + " TEXT DEFAULT 'Default',"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String CREATE_TABLE_REMINDERS = "CREATE TABLE " + TABLE_REMINDERS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_REMINDER_TASK_ID + " INTEGER,"
            + KEY_REMINDER_TITLE + " TEXT,"
            + KEY_REMINDER_DESCRIPTION + " TEXT,"
            + KEY_REMINDER_TIME + " INTEGER,"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");
        db.execSQL(CREATE_TABLE_TASKS);
        db.execSQL(CREATE_TABLE_USER_PROFILE);
        db.execSQL(CREATE_TABLE_NOTIFICATION_SETTINGS);
        db.execSQL(CREATE_TABLE_REMINDERS);

        // Insert default notification settings
        insertDefaultNotificationSettings(db);
        Log.d(TAG, "Database tables created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATION_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        onCreate(db);
    }

    private void insertDefaultNotificationSettings(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(KEY_NOTIF_TASK_REMINDERS, 1);
        values.put(KEY_NOTIF_DUE_DATE_ALERTS, 1);
        values.put(KEY_NOTIF_DAILY_SUMMARY, 0);
        values.put(KEY_NOTIF_DEFAULT_REMINDER_TIME, "15 minutes before");
        values.put(KEY_NOTIF_SOUND, "Default");

        db.insert(TABLE_NOTIFICATION_SETTINGS, null, values);
        Log.d(TAG, "Default notification settings inserted");
    }

    // ==================== TASK OPERATIONS ====================

    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TASK_TITLE, task.getTitle());
        values.put(KEY_TASK_DESCRIPTION, task.getDescription());
        values.put(KEY_TASK_DATE, task.getDate());
        values.put(KEY_TASK_START_TIME, task.getStartTime());
        values.put(KEY_TASK_END_TIME, task.getEndTime());
        values.put(KEY_TASK_CATEGORY, task.getCategory());
        values.put(KEY_TASK_STATUS, task.getStatus());

        long id = db.insert(TABLE_TASKS, null, values);
        db.close();

        Log.d(TAG, "Task added with ID: " + id);
        return id;
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TASKS + " ORDER BY " + KEY_CREATED_AT + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
                task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_TITLE)));
                task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_DESCRIPTION)));
                task.setDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_DATE)));
                task.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_START_TIME)));
                task.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_END_TIME)));
                task.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_CATEGORY)));
                task.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_STATUS)));

                tasks.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return tasks;
    }

    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TASK_TITLE, task.getTitle());
        values.put(KEY_TASK_DESCRIPTION, task.getDescription());
        values.put(KEY_TASK_DATE, task.getDate());
        values.put(KEY_TASK_START_TIME, task.getStartTime());
        values.put(KEY_TASK_END_TIME, task.getEndTime());
        values.put(KEY_TASK_CATEGORY, task.getCategory());
        values.put(KEY_TASK_STATUS, task.getStatus());

        int rowsAffected = db.update(TABLE_TASKS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
        db.close();

        Log.d(TAG, "Task updated, rows affected: " + rowsAffected);
        return rowsAffected;
    }

    public void deleteTask(long taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, KEY_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        Log.d(TAG, "Task deleted with ID: " + taskId);
    }

    // ==================== USER PROFILE OPERATIONS ====================

    public long saveUserProfile(UserProfile profile) {
        SQLiteDatabase db = this.getWritableDatabase();

        // First, check if profile exists
        Cursor cursor = db.query(TABLE_USER_PROFILE, null, null, null, null, null, null);
        boolean profileExists = cursor.getCount() > 0;
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(KEY_PROFILE_NAME, profile.getName());
        values.put(KEY_PROFILE_EMAIL, profile.getEmail());
        values.put(KEY_PROFILE_PHONE, profile.getPhone());
        values.put(KEY_PROFILE_BIO, profile.getBio());
        values.put(KEY_PROFILE_IMAGE_BASE64, profile.getProfileImageBase64());

        long result;
        if (profileExists) {
            result = db.update(TABLE_USER_PROFILE, values, null, null);
        } else {
            result = db.insert(TABLE_USER_PROFILE, null, values);
        }

        db.close();
        Log.d(TAG, "User profile saved, result: " + result);
        return result;
    }

    public UserProfile getUserProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        UserProfile profile = null;

        Cursor cursor = db.query(TABLE_USER_PROFILE, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            profile = new UserProfile();
            profile.setUserId("local_user"); // Since we're using local storage
            profile.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_NAME)));
            profile.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_EMAIL)));
            profile.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_PHONE)));
            profile.setBio(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_BIO)));
            profile.setProfileImageBase64(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_IMAGE_BASE64)));
        } else {
            // Create default profile if none exists
            profile = createDefaultProfile();
        }

        cursor.close();
        db.close();
        return profile;
    }

    private UserProfile createDefaultProfile() {
        UserProfile defaultProfile = new UserProfile(
                "local_user",
                "Rakib Bhuiyan",
                "mdtara0199@gmail.com",
                "+1 234 567 8900",
                "Passionate about productivity and task management. Love creating efficient workflows!",
                ""
        );
        saveUserProfile(defaultProfile);
        return defaultProfile;
    }

    // ==================== NOTIFICATION SETTINGS OPERATIONS ====================

    public NotificationSettings getNotificationSettings() {
        SQLiteDatabase db = this.getReadableDatabase();
        NotificationSettings settings = new NotificationSettings();

        Cursor cursor = db.query(TABLE_NOTIFICATION_SETTINGS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            settings.setTaskRemindersEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTIF_TASK_REMINDERS)) == 1);
            settings.setDueDateAlertsEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTIF_DUE_DATE_ALERTS)) == 1);
            settings.setDailySummaryEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTIF_DAILY_SUMMARY)) == 1);
            settings.setDefaultReminderTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTIF_DEFAULT_REMINDER_TIME)));
            settings.setNotificationSound(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTIF_SOUND)));
        }

        cursor.close();
        db.close();
        return settings;
    }

    public void updateNotificationSetting(String key, Object value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (value instanceof Boolean) {
            values.put(key, (Boolean) value ? 1 : 0);
        } else {
            values.put(key, value.toString());
        }

        db.update(TABLE_NOTIFICATION_SETTINGS, values, null, null);
        db.close();
        Log.d(TAG, "Notification setting updated: " + key + " = " + value);
    }

    // ==================== REMINDER OPERATIONS ====================

    public long saveReminder(long taskId, String title, String description, long reminderTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_REMINDER_TASK_ID, taskId);
        values.put(KEY_REMINDER_TITLE, title);
        values.put(KEY_REMINDER_DESCRIPTION, description);
        values.put(KEY_REMINDER_TIME, reminderTime);

        long id = db.insert(TABLE_REMINDERS, null, values);
        db.close();

        Log.d(TAG, "Reminder saved with ID: " + id);
        return id;
    }

    public void deleteReminder(long taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REMINDERS, KEY_REMINDER_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)});
        db.close();
        Log.d(TAG, "Reminder deleted for task ID: " + taskId);
    }
}