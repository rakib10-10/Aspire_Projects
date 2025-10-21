package com.rakib.to_do_app;

public class CalendarDay {
    private int day;
    private int month;
    private int year;
    private boolean isCurrentMonth;
    private boolean isToday;
    private int taskCount;

    public CalendarDay(int day, int month, int year, boolean isCurrentMonth, boolean isToday, int taskCount) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.isCurrentMonth = isCurrentMonth;
        this.isToday = isToday;
        this.taskCount = taskCount;
    }

    public int getDay() { return day; }
    public int getMonth() { return month; }
    public int getYear() { return year; }
    public boolean isCurrentMonth() { return isCurrentMonth; }
    public boolean isToday() { return isToday; }
    public int getTaskCount() { return taskCount; }

    public String getFormattedDate() {
        return String.format("%02d-%02d-%04d", day, month + 1, year);
    }
}