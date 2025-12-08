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

    private static final String DATABASE_NAME = "TodoApp.db";
    private static final int DATABASE_VERSION = 6; // INCREMENTED VERSION

    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_USER_PROFILE = "user_profile";
    private static final String TABLE_NOTIFICATION_SETTINGS = "notification_settings";
    private static final String TABLE_REMINDERS = "reminders";
    private static final String TABLE_REMINDER_STATE = "reminder_state";

    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    private static final String KEY_TASK_TITLE = "title";
    private static final String KEY_TASK_DESCRIPTION = "description";
    private static final String KEY_TASK_DATE = "date";
    private static final String KEY_TASK_START_TIME = "start_time";
    private static final String KEY_TASK_END_TIME = "end_time";
    private static final String KEY_TASK_CATEGORY = "category";
    private static final String KEY_TASK_STATUS = "status";
    private static final String KEY_TASK_PRIORITY = "priority";
    private static final String KEY_TASK_COLOR_TAG = "color_tag";

    private static final String KEY_PROFILE_NAME = "name";
    private static final String KEY_PROFILE_EMAIL = "email";
    private static final String KEY_PROFILE_PHONE = "phone";
    private static final String KEY_PROFILE_BIO = "bio";
    private static final String KEY_PROFILE_IMAGE_BASE64 = "profile_image_base64";

    private static final String KEY_NOTIF_TASK_REMINDERS = "task_reminders_enabled";
    private static final String KEY_NOTIF_DUE_DATE_ALERTS = "due_date_alerts_enabled";
    private static final String KEY_NOTIF_DAILY_SUMMARY = "daily_summary_enabled";
    private static final String KEY_NOTIF_DEFAULT_REMINDER_TIME = "default_reminder_time";
    private static final String KEY_NOTIF_SOUND = "notification_sound";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";
    private static final String KEY_CUSTOM_RINGTONE_URI = "custom_ringtone_uri";

    private static final String KEY_REMINDER_TASK_ID = "task_id";
    private static final String KEY_REMINDER_TITLE = "title";
    private static final String KEY_REMINDER_DESCRIPTION = "description";
    private static final String KEY_REMINDER_TIME = "reminder_time";

    // Reminder State Table - NEW Columns
    private static final String KEY_STATE_TASK_ID = "task_id";
    private static final String KEY_STATE_ATTEMPTS = "attempts";
    private static final String KEY_STATE_STATUS = "status";

    private static final String CREATE_TABLE_TASKS = "CREATE TABLE " + TABLE_TASKS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_TASK_TITLE + " TEXT,"
            + KEY_TASK_DESCRIPTION + " TEXT,"
            + KEY_TASK_DATE + " TEXT,"
            + KEY_TASK_START_TIME + " TEXT,"
            + KEY_TASK_END_TIME + " TEXT,"
            + KEY_TASK_CATEGORY + " TEXT,"
            + KEY_TASK_STATUS + " TEXT DEFAULT 'running',"
            + KEY_TASK_PRIORITY + " TEXT DEFAULT 'Medium',"
            + KEY_TASK_COLOR_TAG + " TEXT DEFAULT '#2196F3',"
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
            + KEY_DARK_MODE + " INTEGER DEFAULT 0,"
            + KEY_CUSTOM_RINGTONE_URI + " TEXT DEFAULT '',"
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

    private static final String CREATE_TABLE_REMINDER_STATE = "CREATE TABLE " + TABLE_REMINDER_STATE + "("
            + KEY_STATE_TASK_ID + " INTEGER PRIMARY KEY,"
            + KEY_STATE_ATTEMPTS + " INTEGER DEFAULT 0,"
            + KEY_STATE_STATUS + " TEXT DEFAULT 'running',"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TASKS);
        db.execSQL(CREATE_TABLE_USER_PROFILE);
        db.execSQL(CREATE_TABLE_NOTIFICATION_SETTINGS);
        db.execSQL(CREATE_TABLE_REMINDERS);
        db.execSQL(CREATE_TABLE_REMINDER_STATE);

        insertDefaultNotificationSettings(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int version = oldVersion + 1; version <= newVersion; version++) {
            switch (version) {
                case 4:
                    try {
                        db.execSQL("ALTER TABLE " + TABLE_NOTIFICATION_SETTINGS +
                                " ADD COLUMN " + KEY_DARK_MODE + " INTEGER DEFAULT 0");
                    } catch (Exception e) { }
                    break;
                case 5:
                    try {
                        db.execSQL("ALTER TABLE " + TABLE_NOTIFICATION_SETTINGS +
                                " ADD COLUMN " + KEY_CUSTOM_RINGTONE_URI + " TEXT DEFAULT ''");
                    } catch (Exception e) { }
                    break;
                case 6:
                    try {
                        db.execSQL(CREATE_TABLE_REMINDER_STATE);
                    } catch (Exception e) { }
                    break;
            }
        }
    }

    private void insertDefaultNotificationSettings(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(KEY_NOTIF_TASK_REMINDERS, 1);
        values.put(KEY_NOTIF_DUE_DATE_ALERTS, 1);
        values.put(KEY_NOTIF_DAILY_SUMMARY, 0);
        values.put(KEY_NOTIF_DEFAULT_REMINDER_TIME, "15 minutes before");
        values.put(KEY_NOTIF_SOUND, "Default");
        values.put(KEY_DARK_MODE, 0);
        values.put(KEY_CUSTOM_RINGTONE_URI, "");

        db.insert(TABLE_NOTIFICATION_SETTINGS, null, values);
    }

    public boolean isDarkModeEnabled() {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean darkModeEnabled = false;
        Cursor cursor = db.query(TABLE_NOTIFICATION_SETTINGS,
                new String[]{KEY_DARK_MODE}, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            darkModeEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DARK_MODE)) == 1;
        }
        cursor.close();
        db.close();
        return darkModeEnabled;
    }

    public void setDarkModeEnabled(boolean enabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DARK_MODE, enabled ? 1 : 0);
        db.update(TABLE_NOTIFICATION_SETTINGS, values, null, null);
        db.close();
    }

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
        values.put(KEY_TASK_PRIORITY, task.getPriority());
        values.put(KEY_TASK_COLOR_TAG, task.getColorTag());

        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    public Task getTask(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Task task = null;
        Cursor cursor = db.query(TABLE_TASKS, null, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor.moveToFirst()) {
            task = new Task();
            task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
            task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_TITLE)));
            task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_DESCRIPTION)));
            task.setDate(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_DATE)));
            task.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_START_TIME)));
            task.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_END_TIME)));
            task.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_CATEGORY)));
            task.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_STATUS)));
            task.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_PRIORITY)));
            task.setColorTag(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_COLOR_TAG)));
        }

        cursor.close();
        db.close();
        return task;
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
                task.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_PRIORITY)));
                task.setColorTag(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TASK_COLOR_TAG)));

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
        values.put(KEY_TASK_PRIORITY, task.getPriority());
        values.put(KEY_TASK_COLOR_TAG, task.getColorTag());

        int rowsAffected = db.update(TABLE_TASKS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
        db.close();
        return rowsAffected;
    }

    public void deleteTask(long taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, KEY_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    public long saveUserProfile(UserProfile profile) {
        SQLiteDatabase db = this.getWritableDatabase();

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
        return result;
    }

    public UserProfile getUserProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        UserProfile profile = null;

        Cursor cursor = db.query(TABLE_USER_PROFILE, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            profile = new UserProfile();
            profile.setUserId("local_user");
            profile.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_NAME)));
            profile.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_EMAIL)));
            profile.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_PHONE)));
            profile.setBio(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_BIO)));
            profile.setProfileImageBase64(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_IMAGE_BASE64)));
        } else {
            profile = createDefaultProfile();
        }

        cursor.close();
        db.close();
        return profile;
    }

    private UserProfile createDefaultProfile() {
        UserProfile defaultProfile = new UserProfile(
                "local_user",
                "Rakibul Hasan Bhuiyan",
                "mdtara0199@gmail.com",
                "+880 1316787455",
                "Passionate about productivity and task management. Love creating efficient workflows!",
                ""
        );
        saveUserProfile(defaultProfile);
        return defaultProfile;
    }

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
            settings.setDarkModeEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DARK_MODE)) == 1);
            settings.setCustomRingtoneUri(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CUSTOM_RINGTONE_URI)));

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
    }

    public void saveCustomRingtoneUri(String uri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CUSTOM_RINGTONE_URI, uri);
        values.put(KEY_NOTIF_SOUND, "Custom");

        db.update(TABLE_NOTIFICATION_SETTINGS, values, null, null);
        db.close();
    }

    public long saveReminder(long taskId, String title, String description, long reminderTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_REMINDER_TASK_ID, taskId);
        values.put(KEY_REMINDER_TITLE, title);
        values.put(KEY_REMINDER_DESCRIPTION, description);
        values.put(KEY_REMINDER_TIME, reminderTime);

        long id = db.insert(TABLE_REMINDERS, null, values);
        db.close();
        return id;
    }

    public void deleteReminder(long taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REMINDERS, KEY_REMINDER_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)});
        db.close();
    }

    public List<Reminder> getAllReminders() {
        List<Reminder> reminders = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_REMINDERS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Reminder reminder = new Reminder();
                reminder.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
                reminder.setTaskId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_REMINDER_TASK_ID)));
                reminder.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_REMINDER_TITLE)));
                reminder.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_REMINDER_DESCRIPTION)));
                reminder.setReminderTime(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_REMINDER_TIME)));

                reminders.add(reminder);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return reminders;
    }

    // ==================== REMINDER STATE METHODS ====================

    public void resetReminderState(long taskId, String initialStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_STATE_ATTEMPTS, 1);
        values.put(KEY_STATE_STATUS, initialStatus);

        int rows = db.update(TABLE_REMINDER_STATE, values, KEY_STATE_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});

        if (rows == 0) {
            values.put(KEY_STATE_TASK_ID, taskId);
            db.insert(TABLE_REMINDER_STATE, null, values);
        }
        db.close();
    }

    public int incrementReminderAttempt(long taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int attempts = 0;

        Cursor cursor = db.query(TABLE_REMINDER_STATE, new String[]{KEY_STATE_ATTEMPTS}, KEY_STATE_TASK_ID + "=?", new String[]{String.valueOf(taskId)}, null, null, null);
        if (cursor.moveToFirst()) {
            attempts = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STATE_ATTEMPTS));
        }
        cursor.close();

        attempts++;
        ContentValues values = new ContentValues();
        values.put(KEY_STATE_ATTEMPTS, attempts);

        db.update(TABLE_REMINDER_STATE, values, KEY_STATE_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
        return attempts;
    }

    public void deleteReminderState(long taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REMINDER_STATE, KEY_STATE_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
    }
}