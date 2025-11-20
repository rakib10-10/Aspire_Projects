package com.rakib.to_do_app;

public class NotificationSettings {
    private boolean taskRemindersEnabled;
    private boolean dueDateAlertsEnabled;
    private boolean dailySummaryEnabled;
    private String defaultReminderTime; // Add this field
    private String notificationSound;

    public NotificationSettings() {
        // Default values
        this.taskRemindersEnabled = true;
        this.dueDateAlertsEnabled = true;
        this.dailySummaryEnabled = false;
        this.defaultReminderTime = "15 minutes before";
        this.notificationSound = "Default";
    }

    // Getters and Setters
    public boolean isTaskRemindersEnabled() { return taskRemindersEnabled; }
    public void setTaskRemindersEnabled(boolean taskRemindersEnabled) {
        this.taskRemindersEnabled = taskRemindersEnabled;
    }

    public boolean isDueDateAlertsEnabled() { return dueDateAlertsEnabled; }
    public void setDueDateAlertsEnabled(boolean dueDateAlertsEnabled) {
        this.dueDateAlertsEnabled = dueDateAlertsEnabled;
    }

    public boolean isDailySummaryEnabled() { return dailySummaryEnabled; }
    public void setDailySummaryEnabled(boolean dailySummaryEnabled) {
        this.dailySummaryEnabled = dailySummaryEnabled;
    }

    public String getDefaultReminderTime() { return defaultReminderTime; } // Add getter
    public void setDefaultReminderTime(String defaultReminderTime) { // Add setter
        this.defaultReminderTime = defaultReminderTime;
    }

    public String getNotificationSound() { return notificationSound; }
    public void setNotificationSound(String notificationSound) {
        this.notificationSound = notificationSound;
    }
}