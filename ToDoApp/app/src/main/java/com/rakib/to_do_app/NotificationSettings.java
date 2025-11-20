package com.rakib.to_do_app;

public class NotificationSettings {
    private boolean taskRemindersEnabled;
    private boolean dueDateAlertsEnabled;
    private boolean dailySummaryEnabled;
    private String defaultReminderTime;
    private String notificationSound;

    public NotificationSettings() {
        // Default constructor required for Firebase
    }

    public NotificationSettings(boolean taskRemindersEnabled, boolean dueDateAlertsEnabled,
                                boolean dailySummaryEnabled, String defaultReminderTime,
                                String notificationSound) {
        this.taskRemindersEnabled = taskRemindersEnabled;
        this.dueDateAlertsEnabled = dueDateAlertsEnabled;
        this.dailySummaryEnabled = dailySummaryEnabled;
        this.defaultReminderTime = defaultReminderTime;
        this.notificationSound = notificationSound;
    }

    // Getters and setters
    public boolean isTaskRemindersEnabled() { return taskRemindersEnabled; }
    public void setTaskRemindersEnabled(boolean taskRemindersEnabled) { this.taskRemindersEnabled = taskRemindersEnabled; }

    public boolean isDueDateAlertsEnabled() { return dueDateAlertsEnabled; }
    public void setDueDateAlertsEnabled(boolean dueDateAlertsEnabled) { this.dueDateAlertsEnabled = dueDateAlertsEnabled; }

    public boolean isDailySummaryEnabled() { return dailySummaryEnabled; }
    public void setDailySummaryEnabled(boolean dailySummaryEnabled) { this.dailySummaryEnabled = dailySummaryEnabled; }

    public String getDefaultReminderTime() { return defaultReminderTime; }
    public void setDefaultReminderTime(String defaultReminderTime) { this.defaultReminderTime = defaultReminderTime; }

    public String getNotificationSound() { return notificationSound; }
    public void setNotificationSound(String notificationSound) { this.notificationSound = notificationSound; }
}