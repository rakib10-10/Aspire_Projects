package com.rakib.to_do_app;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private static final String TAG = "TaskManager";
    private static TaskManager instance;
    private final DatabaseHelper dbHelper;
    private List<Task> tasks; // This holds the local, loaded list of tasks.


    private TaskManager(Context context) {
        // Use application context to prevent memory leaks
        this.dbHelper = new DatabaseHelper(context.getApplicationContext());
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

    /**
     * Renamed method to match external usage: List<Task> tasks = taskManager.getAllTasks();
     * This returns the locally cached, currently loaded list of tasks.
     * @return The list of all tasks currently managed by TaskManager.
     */
    public List<Task> getAllTasks() { // <-- FIX: Renamed from getTasks() to getAllTasks()
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        return tasks;
    }


    public long addTask(Task task) {
        if (dbHelper == null) {
            Log.e(TAG, "DatabaseHelper is null - cannot add task");
            return -1;
        }

        long id = dbHelper.addTask(task);


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


    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}