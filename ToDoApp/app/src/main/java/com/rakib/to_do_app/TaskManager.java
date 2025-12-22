package com.rakib.to_do_app;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private static final String TAG = "TaskManager";
    private static TaskManager instance;
    private final DatabaseHelper dbHelper;
    private final SessionManager sessionManager;
    private List<Task> tasks;

    private TaskManager(Context context) {
        Context appContext = context.getApplicationContext();
        this.dbHelper = new DatabaseHelper(appContext);
        this.sessionManager = new SessionManager(appContext);
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

    // Call this when logging out to ensure the next user gets a fresh list
    public static void clearInstance() {
        instance = null;
    }

    public List<Task> getAllTasks() {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        return tasks;
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

    public long addTask(Task task) {
        if (dbHelper == null) {
            Log.e(TAG, "DatabaseHelper is null - cannot add task");
            return -1;
        }

        // Get Current User Email from Session
        String userEmail = sessionManager.getUserEmail();

        // Pass email to DatabaseHelper
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

            // Use standard loop for compatibility with older Android versions
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getId() == task.getId()) {
                    tasks.remove(i);
                    break;
                }
            }
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

            // Get Current User Email
            String userEmail = sessionManager.getUserEmail();

            // Fetch tasks ONLY for this user
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