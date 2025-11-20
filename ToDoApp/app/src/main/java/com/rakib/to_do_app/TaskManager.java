package com.rakib.to_do_app;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private static TaskManager instance;
    private DatabaseHelper dbHelper;
    private List<Task> tasks;

    // Private constructor with Context for database operations
    private TaskManager(Context context) {
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

    public void addTask(Task task) {
        if (dbHelper != null) {
            long taskId = dbHelper.addTask(task);
            task.setId(taskId);

            if (tasks == null) {
                tasks = new ArrayList<>();
            }
            tasks.add(0, task); // Add to beginning for newest first
            Log.d("TaskManager", "Task added: " + task.getTitle());
        } else {
            Log.e("TaskManager", "DatabaseHelper is null - cannot add task");
        }
    }

    public void removeTask(Task task) {
        if (dbHelper != null && tasks != null) {
            dbHelper.deleteTask(task.getId());
            tasks.remove(task);
            Log.d("TaskManager", "Task removed: " + task.getTitle());
        } else {
            Log.e("TaskManager", "DatabaseHelper or tasks list is null - cannot remove task");
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
            Log.d("TaskManager", "Task updated: " + updatedTask.getTitle());
        } else {
            Log.e("TaskManager", "DatabaseHelper or tasks list is null - cannot update task");
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
            Log.d("TaskManager", "Tasks loaded from database: " + tasks.size());
        } else {
            Log.e("TaskManager", "DatabaseHelper is null - cannot load tasks");
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