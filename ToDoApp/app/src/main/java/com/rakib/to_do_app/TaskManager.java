package com.rakib.to_do_app;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private static final String TAG = "TaskManager";
    private static TaskManager instance;
    private final DatabaseHelper dbHelper;
    private final SessionManager sessionManager; // 1. Add SessionManager
    private List<Task> tasks;

    private TaskManager(Context context) {
        Context appContext = context.getApplicationContext();
        this.dbHelper = new DatabaseHelper(appContext);
        this.sessionManager = new SessionManager(appContext); // 2. Initialize it
        this.tasks = new ArrayList<>();
        loadTasksFromDatabase();
    }

    public static synchronized TaskManager getInstance(Context context) {
        if (instance == null) {
            instance = new TaskManager(context);
        }
        return instance;
    }

    public static TaskManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TaskManager must be initialized first with getInstance(Context)");
        }
        return instance;
    }

    public List<Task> getAllTasks() {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        return tasks;
    }

    // Method to get a task by ID from the local cache
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

    public long addTask(Task task) {
        if (dbHelper == null) {
            Log.e(TAG, "DatabaseHelper is null - cannot add task");
            return -1;
        }

        // 3. Get Current User Email
        String userEmail = sessionManager.getUserEmail();

        // 4. Pass email to DatabaseHelper
        long id = dbHelper.addTask(task, userEmail);

        if (id != -1) {
            task.setId(id);
            tasks.add(task);
        }
        return id;
    }

    public void removeTask(Task task) {
        if (dbHelper != null && tasks != null) {
            dbHelper.deleteTask(task.getId());
            tasks.removeIf(t -> t.getId() == task.getId());
        } else {
            Log.e(TAG, "DatabaseHelper or tasks list is null - cannot remove task");
        }
    }

    public void updateTask(Task updatedTask) {
        if (dbHelper != null && tasks != null) {
            dbHelper.updateTask(updatedTask);

            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                if (task.getId() == updatedTask.getId()) {
                    tasks.set(i, updatedTask);
                    break;
                }
            }
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

            // 5. Get Current User Email
            String userEmail = sessionManager.getUserEmail();

            // 6. Fetch tasks ONLY for this user
            List<Task> databaseTasks = dbHelper.getAllTasks(userEmail);

            if (databaseTasks != null) {
                tasks.addAll(databaseTasks);
            }
        } else {
            Log.e(TAG, "DatabaseHelper is null - cannot load tasks");
        }
    }

    public void refreshTasks() {
        loadTasksFromDatabase();
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}