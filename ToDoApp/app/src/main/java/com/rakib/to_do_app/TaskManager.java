package com.rakib.to_do_app;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private static final String TAG = "TaskManager";
    private static TaskManager instance;
    private final DatabaseHelper dbHelper; // Field is final and initialized in constructor
    private List<Task> tasks;
    // Removed unused 'context' field, as dbHelper already holds the database connection

    // Private constructor with Context for database operations
    private TaskManager(Context context) {
        // Use application context to prevent memory leaks
        this.dbHelper = new DatabaseHelper(context.getApplicationContext());
        this.tasks = new ArrayList<>();
        loadTasksFromDatabase();
    }

    // Public static method to get instance with Context
    public static synchronized TaskManager getInstance(Context context) {
        if (instance == null) {
            instance = new TaskManager(context);
        }
        return instance;
    }

    // Alternative getInstance without parameters (use only after initialization)
    public static TaskManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TaskManager must be initialized first with getInstance(Context)");
        }
        return instance;
    }

    public List<Task> getTasks() {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        return tasks;
    }

    // FIX: Use the class field dbHelper and return the long ID.
    public long addTask(Task task) {
        if (dbHelper == null) {
            Log.e(TAG, "DatabaseHelper is null - cannot add task");
            return -1;
        }

        long id = dbHelper.addTask(task);

        // Add to local list immediately if successful
        if (id != -1) {
            task.setId(id);
            tasks.add(task);
            Log.d(TAG, "Task added with ID: " + id);
        }
        return id;
    }

    public void removeTask(Task task) {
        if (dbHelper != null && tasks != null) {
            dbHelper.deleteTask(task.getId());

            // FIX: Use task ID for robust removal from local list
            tasks.removeIf(t -> t.getId() == task.getId());

            Log.d(TAG, "Task removed: " + task.getTitle());
        } else {
            Log.e(TAG, "DatabaseHelper or tasks list is null - cannot remove task");
        }
    }

    public void updateTask(Task updatedTask) {
        if (dbHelper != null && tasks != null) {
            dbHelper.updateTask(updatedTask);

            // Update in local list
            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                if (task.getId() == updatedTask.getId()) {
                    tasks.set(i, updatedTask);
                    break;
                }
            }
            Log.d(TAG, "Task updated: " + updatedTask.getTitle());
        } else {
            Log.e(TAG, "DatabaseHelper or tasks list is null - cannot update task");
        }
    }

    private void loadTasksFromDatabase() {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }

        if (dbHelper != null) {
            tasks.clear();
            List<Task> databaseTasks = dbHelper.getAllTasks();
            if (databaseTasks != null) {
                tasks.addAll(databaseTasks);
            }
            Log.d(TAG, "Tasks loaded from database: " + tasks.size());
        } else {
            Log.e(TAG, "DatabaseHelper is null - cannot load tasks");
        }
    }

    public void refreshTasks() {
        loadTasksFromDatabase();
    }

    public Task getTaskById(long taskId) {
        if (tasks != null) {
            for (Task task : tasks) {
                if (task.getId() == taskId) {
                    return task;
                }
            }
        }
        return null;
    }

    // Optional: Method to close database when needed
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}