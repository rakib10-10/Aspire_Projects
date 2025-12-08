package com.rakib.to_do_app;

public class Reminder {
    private long id;
    private long taskId;
    private String title;
    private String description;
    private long reminderTime;

    public Reminder() {
    }

    public Reminder(long taskId, String title, String description, long reminderTime) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.reminderTime = reminderTime;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getReminderTime() { return reminderTime; }
    public void setReminderTime(long reminderTime) { this.reminderTime = reminderTime; }
}