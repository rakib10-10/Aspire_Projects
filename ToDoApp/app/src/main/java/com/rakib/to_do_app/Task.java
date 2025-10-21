package com.rakib.to_do_app;

public class Task {
    private String title;
    private String description;
    private String date;
    private String startTime;
    private String endTime;
    private String category;
    private String status; // "all", "running", "completed"

    // Primary constructor with all fields
    public Task(String title, String description, String date, String startTime, String endTime, String category, String status) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.status = status;
    }

    // Constructor with default status
    public Task(String title, String description, String date, String startTime, String endTime, String category) {
        this(title, description, date, startTime, endTime, category, "running");
    }

    // Simple constructor
    public Task(String title, String date) {
        this(title, "", date, "", "", "", "running");
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setCategory(String category) { this.category = category; }
    public void setStatus(String status) { this.status = status; }
}