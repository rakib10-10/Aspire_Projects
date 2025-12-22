package com.rakib.to_do_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TodoApp.db";
    private static final int DATABASE_VERSION = 14; // INCREMENTED FOR NOTES UPDATE

    // --- TABLE NAMES ---
    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_USER_PROFILE = "user_profile";
    private static final String TABLE_NOTIFICATION_SETTINGS = "notification_settings";
    private static final String TABLE_REMINDERS = "reminders";
    private static final String TABLE_REMINDER_STATE = "reminder_state";
    private static final String TABLE_NOTES = "notes";
    private static final String TABLE_AUTH = "auth_users";
    private static final String TABLE_CUSTOM_SOUNDS = "custom_sounds";

    // --- COMMON COLUMNS ---
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // --- TASKS COLUMNS ---
    private static final String KEY_TASK_TITLE = "title";
    private static final String KEY_TASK_DESCRIPTION = "description";
    private static final String KEY_TASK_DATE = "date";
    private static final String KEY_TASK_START_TIME = "start_time";
    private static final String KEY_TASK_END_TIME = "end_time";
    private static final String KEY_TASK_CATEGORY = "category";
    private static final String KEY_TASK_STATUS = "status";
    private static final String KEY_TASK_PRIORITY = "priority";
    private static final String KEY_TASK_COLOR_TAG = "color_tag";
    private static final String KEY_TASK_USER_EMAIL = "user_email"; // User Filter

    // --- PROFILE COLUMNS ---
    private static final String KEY_PROFILE_NAME = "name";
    private static final String KEY_PROFILE_EMAIL = "email";
    private static final String KEY_PROFILE_PHONE = "phone";
    private static final String KEY_PROFILE_BIO = "bio";
    private static final String KEY_PROFILE_IMAGE_BASE64 = "profile_image_base64";

    // --- NOTIFICATION COLUMNS ---
    private static final String KEY_NOTIF_TASK_REMINDERS = "task_reminders_enabled";
    private static final String KEY_NOTIF_DUE_DATE_ALERTS = "due_date_alerts_enabled";
    private static final String KEY_NOTIF_DAILY_SUMMARY = "daily_summary_enabled";
    private static final String KEY_NOTIF_DEFAULT_REMINDER_TIME = "default_reminder_time";
    private static final String KEY_NOTIF_SOUND = "notification_sound";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";
    private static final String KEY_CUSTOM_RINGTONE_URI = "custom_ringtone_uri";
    private static final String KEY_NOTIF_UNFINISHED_INTERVAL = "unfinished_notification_interval";

    // --- REMINDER COLUMNS ---
    private static final String KEY_REMINDER_TASK_ID = "task_id";
    private static final String KEY_REMINDER_TITLE = "title";
    private static final String KEY_REMINDER_DESCRIPTION = "description";
    private static final String KEY_REMINDER_TIME = "reminder_time";

    // --- REMINDER STATE COLUMNS ---
    private static final String KEY_STATE_TASK_ID = "task_id";
    private static final String KEY_STATE_ATTEMPTS = "attempts";
    private static final String KEY_STATE_STATUS = "status";

    // --- NOTES COLUMNS ---
    private static final String KEY_NOTE_TITLE = "title";
    private static final String KEY_NOTE_CONTENT = "content";
    private static final String KEY_NOTE_DATE = "date";
    private static final String KEY_NOTE_COLOR = "color";
    private static final String KEY_NOTE_USER_EMAIL = "user_email"; // NEW COLUMN

    // --- AUTH COLUMNS ---
    private static final String KEY_AUTH_ID = "id";
    private static final String KEY_AUTH_NAME = "name";
    private static final String KEY_AUTH_EMAIL = "email";
    private static final String KEY_AUTH_PASSWORD = "password";
    private static final String KEY_AUTH_PHONE = "phone";
    private static final String KEY_AUTH_BIO = "bio";
    private static final String KEY_AUTH_IMAGE = "profile_image";

    // --- CUSTOM SOUND COLUMNS ---
    private static final String COL_SOUND_NAME = "sound_name";
    private static final String COL_SOUND_URI = "sound_uri";

    // --- CREATE STATEMENTS ---
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
            + KEY_TASK_USER_EMAIL + " TEXT," // ADDED
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";

    private static final String CREATE_TABLE_USER_PROFILE = "CREATE TABLE " + TABLE_USER_PROFILE + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_PROFILE_NAME + " TEXT,"
            + KEY_PROFILE_EMAIL + " TEXT,"
            + KEY_PROFILE_PHONE + " TEXT,"
            + KEY_PROFILE_BIO + " TEXT,"
            + KEY_PROFILE_IMAGE_BASE64 + " TEXT,"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";

    private static final String CREATE_TABLE_NOTIFICATION_SETTINGS = "CREATE TABLE " + TABLE_NOTIFICATION_SETTINGS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_NOTIF_TASK_REMINDERS + " INTEGER DEFAULT 1,"
            + KEY_NOTIF_DUE_DATE_ALERTS + " INTEGER DEFAULT 1,"
            + KEY_NOTIF_DAILY_SUMMARY + " INTEGER DEFAULT 0,"
            + KEY_NOTIF_DEFAULT_REMINDER_TIME + " TEXT DEFAULT '15 minutes before',"
            + KEY_NOTIF_SOUND + " TEXT DEFAULT 'Default',"
            + KEY_DARK_MODE + " INTEGER DEFAULT 0,"
            + KEY_CUSTOM_RINGTONE_URI + " TEXT DEFAULT '',"
            + KEY_NOTIF_UNFINISHED_INTERVAL + " INTEGER DEFAULT 30,"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";

    private static final String CREATE_TABLE_REMINDERS = "CREATE TABLE " + TABLE_REMINDERS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_REMINDER_TASK_ID + " INTEGER,"
            + KEY_REMINDER_TITLE + " TEXT,"
            + KEY_REMINDER_DESCRIPTION + " TEXT,"
            + KEY_REMINDER_TIME + " INTEGER,"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";

    private static final String CREATE_TABLE_REMINDER_STATE = "CREATE TABLE " + TABLE_REMINDER_STATE + "("
            + KEY_STATE_TASK_ID + " INTEGER PRIMARY KEY,"
            + KEY_STATE_ATTEMPTS + " INTEGER DEFAULT 0,"
            + KEY_STATE_STATUS + " TEXT DEFAULT 'running',"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";

    private static final String CREATE_TABLE_NOTES = "CREATE TABLE " + TABLE_NOTES + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_NOTE_TITLE + " TEXT,"
            + KEY_NOTE_CONTENT + " TEXT,"
            + KEY_NOTE_DATE + " TEXT,"
            + KEY_NOTE_COLOR + " TEXT,"
            + KEY_NOTE_USER_EMAIL + " TEXT," // ADDED
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";

    private static final String CREATE_TABLE_AUTH = "CREATE TABLE " + TABLE_AUTH + "("
            + KEY_AUTH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_AUTH_NAME + " TEXT,"
            + KEY_AUTH_EMAIL + " TEXT,"
            + KEY_AUTH_PASSWORD + " TEXT,"
            + KEY_AUTH_PHONE + " TEXT,"
            + KEY_AUTH_BIO + " TEXT,"
            + KEY_AUTH_IMAGE + " TEXT" + ")";

    private static final String CREATE_TABLE_CUSTOM_SOUNDS = "CREATE TABLE " + TABLE_CUSTOM_SOUNDS + " ("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_SOUND_NAME + " TEXT UNIQUE, "
            + COL_SOUND_URI + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void updateReminderStatus(long taskId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_STATE_STATUS, status);

        // Update the row for this specific task
        db.update(TABLE_REMINDER_STATE, values, KEY_STATE_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TASKS);
        db.execSQL(CREATE_TABLE_USER_PROFILE);
        db.execSQL(CREATE_TABLE_NOTIFICATION_SETTINGS);
        db.execSQL(CREATE_TABLE_REMINDERS);
        db.execSQL(CREATE_TABLE_REMINDER_STATE);
        db.execSQL(CREATE_TABLE_NOTES);
        db.execSQL(CREATE_TABLE_AUTH);
        db.execSQL(CREATE_TABLE_CUSTOM_SOUNDS);
        insertDefaultNotificationSettings(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 11) {
            try { db.execSQL(CREATE_TABLE_CUSTOM_SOUNDS); } catch (Exception e) {}
        }
        if (oldVersion < 12) {
            try { db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + KEY_TASK_USER_EMAIL + " TEXT"); } catch (Exception e) {}
        }
        if (oldVersion < 13) {
            try { db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN " + KEY_NOTE_USER_EMAIL + " TEXT"); } catch (Exception e) {}
        }
        if (oldVersion < 14) {

            try {
                db.execSQL(CREATE_TABLE_AUTH);
            } catch (Exception e) {

            }
        }
    }

    // ==================== TASKS (UPDATED) ====================
    public long addTask(Task task, String userEmail) {
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
        values.put(KEY_TASK_USER_EMAIL, userEmail); // SAVING EMAIL

        long id = db.insert(TABLE_TASKS, null, values);
        db.close();
        return id;
    }

    public List<Task> getAllTasks(String userEmail) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = KEY_TASK_USER_EMAIL + " = ?";
        String[] selectionArgs = { userEmail };
        if (userEmail == null) { selection = KEY_TASK_USER_EMAIL + " IS NULL"; selectionArgs = null; }

        Cursor cursor = db.query(TABLE_TASKS, null, selection, selectionArgs, null, null, KEY_CREATED_AT + " DESC");

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
        int rows = db.update(TABLE_TASKS, values, KEY_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        return rows;
    }

    public void deleteTask(long taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, KEY_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
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

    // ==================== NOTES (UPDATED) ====================
    public long addNote(Note note, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOTE_TITLE, note.getTitle());
        values.put(KEY_NOTE_CONTENT, note.getContent());
        values.put(KEY_NOTE_DATE, note.getDateTime());
        values.put(KEY_NOTE_COLOR, note.getColor());
        values.put(KEY_NOTE_USER_EMAIL, userEmail); // SAVING EMAIL

        long id = db.insert(TABLE_NOTES, null, values);
        db.close();
        return id;
    }

    public List<Note> getAllNotes(String userEmail) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = KEY_NOTE_USER_EMAIL + " = ?";
        String[] selectionArgs = { userEmail };
        if (userEmail == null) { selection = KEY_NOTE_USER_EMAIL + " IS NULL"; selectionArgs = null; }

        Cursor cursor = db.query(TABLE_NOTES, null, selection, selectionArgs, null, null, KEY_ID + " DESC");
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_TITLE)));
                note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_CONTENT)));
                note.setDateTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_DATE)));
                note.setColor(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_COLOR)));
                notes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return notes;
    }

    public void updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOTE_TITLE, note.getTitle());
        values.put(KEY_NOTE_CONTENT, note.getContent());
        values.put(KEY_NOTE_DATE, note.getDateTime());
        values.put(KEY_NOTE_COLOR, note.getColor());
        db.update(TABLE_NOTES, values, KEY_ID + " = ?", new String[]{String.valueOf(note.getId())});
        db.close();
    }

    public void deleteNote(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // ==================== AUTH & OTHER METHODS ====================
    public void addCustomSound(String name, String uri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SOUND_NAME, name);
        values.put(COL_SOUND_URI, uri);
        db.insertWithOnConflict(TABLE_CUSTOM_SOUNDS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }
    public String getCustomSoundUri(String soundName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String uri = null;
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_CUSTOM_SOUNDS, new String[]{COL_SOUND_URI}, COL_SOUND_NAME + "=?", new String[]{soundName}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) uri = cursor.getString(0);
        } catch (Exception e) {} finally { if (cursor != null) cursor.close(); db.close(); }
        return uri;
    }
    public List<String> getAllCustomSoundNames() {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.query(TABLE_CUSTOM_SOUNDS, new String[]{COL_SOUND_NAME}, null, null, null, null, null);
            if (cursor.moveToFirst()) { do { names.add(cursor.getString(0)); } while (cursor.moveToNext()); }
            cursor.close();
        } catch (Exception e) {} db.close();
        return names;
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
        values.put(KEY_NOTIF_UNFINISHED_INTERVAL, 30);
        db.insert(TABLE_NOTIFICATION_SETTINGS, null, values);
    }
    public boolean isDarkModeEnabled() {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean enabled = false;
        Cursor cursor = db.query(TABLE_NOTIFICATION_SETTINGS, new String[]{KEY_DARK_MODE}, null, null, null, null, null);
        if (cursor.moveToFirst()) enabled = cursor.getInt(0) == 1;
        cursor.close();
        db.close();
        return enabled;
    }
    public void setDarkModeEnabled(boolean enabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DARK_MODE, enabled ? 1 : 0);
        db.update(TABLE_NOTIFICATION_SETTINGS, values, null, null);
        db.close();
    }
    public long saveUserProfile(UserProfile profile) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_USER_PROFILE, null, null, null, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        ContentValues values = new ContentValues();
        values.put(KEY_PROFILE_NAME, profile.getName());
        values.put(KEY_PROFILE_EMAIL, profile.getEmail());
        values.put(KEY_PROFILE_PHONE, profile.getPhone());
        values.put(KEY_PROFILE_BIO, profile.getBio());
        values.put(KEY_PROFILE_IMAGE_BASE64, profile.getProfileImageBase64());
        long result = exists ? db.update(TABLE_USER_PROFILE, values, null, null) : db.insert(TABLE_USER_PROFILE, null, values);
        db.close();
        return result;
    }
    public UserProfile getUserProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        UserProfile profile = null;
        Cursor cursor = db.query(TABLE_USER_PROFILE, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            profile = new UserProfile();
            profile.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_NAME)));
            profile.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_EMAIL)));
            profile.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_PHONE)));
            profile.setBio(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_BIO)));
            profile.setProfileImageBase64(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_IMAGE_BASE64)));
        } else {
            profile = new UserProfile("local_user", "User", "user@example.com", "", "", "");
            saveUserProfile(profile);
        }
        cursor.close();
        db.close();
        return profile;
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
            settings.setUnfinishedNotificationInterval(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTIF_UNFINISHED_INTERVAL)));
        }
        cursor.close();
        db.close();
        return settings;
    }
    public void updateNotificationSetting(String key, Object value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (value instanceof Boolean) values.put(key, (Boolean) value ? 1 : 0);
        else if (value instanceof Integer) values.put(key, (Integer) value);
        else values.put(key, value.toString());
        db.update(TABLE_NOTIFICATION_SETTINGS, values, null, null);
        db.close();
    }
    public void saveCustomRingtoneUri(String uri) {
        updateNotificationSetting(KEY_CUSTOM_RINGTONE_URI, uri);
        updateNotificationSetting(KEY_NOTIF_SOUND, "Custom");
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
        db.delete(TABLE_REMINDERS, KEY_REMINDER_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        db.close();
    }
    public List<Reminder> getAllReminders() {
        List<Reminder> reminders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_REMINDERS, null);
        if (cursor.moveToFirst()) {
            do {
                Reminder r = new Reminder();
                r.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
                r.setTaskId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_REMINDER_TASK_ID)));
                r.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_REMINDER_TITLE)));
                r.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_REMINDER_DESCRIPTION)));
                r.setReminderTime(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_REMINDER_TIME)));
                reminders.add(r);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return reminders;
    }
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
        if (cursor.moveToFirst()) attempts = cursor.getInt(0);
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
    public boolean registerUser(String name, String email, String password, String phone, String bio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_AUTH_NAME, name);
        values.put(KEY_AUTH_EMAIL, email);
        values.put(KEY_AUTH_PASSWORD, password);
        values.put(KEY_AUTH_PHONE, phone);
        values.put(KEY_AUTH_BIO, bio);
        values.put(KEY_AUTH_IMAGE, "");
        long result = db.insert(TABLE_AUTH, null, values);
        db.close();
        return result != -1;
    }
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {KEY_AUTH_ID};
        String selection = KEY_AUTH_EMAIL + " = ?" + " AND " + KEY_AUTH_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};
        Cursor cursor = db.query(TABLE_AUTH, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }
    public UserProfile getUserDetails(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        UserProfile user = new UserProfile();
        Cursor cursor = db.query(TABLE_AUTH, null, KEY_AUTH_EMAIL + "=?", new String[]{email}, null, null, null);
        if (cursor.moveToFirst()) {
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_AUTH_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(KEY_AUTH_EMAIL)));
            int phoneIndex = cursor.getColumnIndex(KEY_AUTH_PHONE);
            if(phoneIndex != -1) user.setPhone(cursor.getString(phoneIndex));
            int bioIndex = cursor.getColumnIndex(KEY_AUTH_BIO);
            if(bioIndex != -1) user.setBio(cursor.getString(bioIndex));
            int imgIndex = cursor.getColumnIndex(KEY_AUTH_IMAGE);
            if(imgIndex != -1) user.setProfileImageBase64(cursor.getString(imgIndex));
        }
        cursor.close();
        db.close();
        return user;
    }
    public boolean updateUserInfo(UserProfile user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_AUTH_NAME, user.getName());
        values.put(KEY_AUTH_PHONE, user.getPhone());
        values.put(KEY_AUTH_BIO, user.getBio());
        values.put(KEY_AUTH_IMAGE, user.getProfileImageBase64());
        int result = db.update(TABLE_AUTH, values, KEY_AUTH_EMAIL + " = ?", new String[]{user.getEmail()});
        db.close();
        return result > 0;
    }
}