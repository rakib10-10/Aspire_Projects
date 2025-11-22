package com.rakib.to_do_app;

import android.widget.RadioGroup;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Task {
    private long id;
    private String title;
    private String description;
    private String date;
    private String startTime;
    private String endTime;
    private String category;
    private String status;
    private String priority;


    public Task() {
        this.id = System.currentTimeMillis();
        this.status = "running";
    }

    public Task(String title, String description, String date, String startTime, String endTime, String category, String priority) {
        this.id = System.currentTimeMillis();
        this.title = title;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.status = "running";
        this.priority = priority;
    }

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

    public Task(String title, String date) {
        this();
        this.title = title;
        this.date = date;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; } // Add getter
    public void setPriority(String priority) { this.priority = priority; } // Add setter




    public Date getDueDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            return sdf.parse(this.date);
        } catch (Exception e) {
            return new Date();
        }
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}