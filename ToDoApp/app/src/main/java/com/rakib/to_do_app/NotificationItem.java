package com.rakib.to_do_app;

public class NotificationItem {
    private String title;
    private String message;
    private long timestamp;
    private String type; // "task", "due_date", "summary"

    public NotificationItem() {
        // Default constructor required for Firebase
    }

    public NotificationItem(String title, String message, long timestamp, String type) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}