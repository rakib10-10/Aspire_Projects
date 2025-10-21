package com.rakib.to_do_app;

import java.util.Date;

public class Task {
    private long id;
    private String title;
    private String description;
    private String date;
    private String startTime;
    private String endTime;
    private Date dueDate;
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
        this.id = System.currentTimeMillis(); // Generate unique ID
    }

    // Constructor with ID for existing tasks
    public Task(long id, String title, String description, String date, String startTime, String endTime, String category, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.status = status;
    }

    // Simple constructor
    public Task(String title, String date) {
        this(title, "", date, "", "", "", "running");
    }

    // Getters
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }

    // Get dueDate - convert from string date to Date object
    public Date getDueDate() {
        try {
            // Convert your "dd-MM-yyyy" string to Date
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
            return sdf.parse(this.date);
        } catch (Exception e) {
            return new Date(); // Return current date as fallback
        }
    }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setCategory(String category) { this.category = category; }
    public void setStatus(String status) { this.status = status; }
}