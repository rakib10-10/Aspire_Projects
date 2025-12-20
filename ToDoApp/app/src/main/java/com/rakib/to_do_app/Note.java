package com.rakib.to_do_app;

public class Note {
    private long id;
    private String title;
    private String content; // Stores HTML for formatting
    private String dateTime;
    private String color; // Hex code

    public Note() {}

    public Note(long id, String title, String content, String dateTime, String color) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.dateTime = dateTime;
        this.color = color;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}