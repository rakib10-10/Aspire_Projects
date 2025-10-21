package com.rakib.to_do_app;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {
    private static TaskManager instance;
    private List<Task> tasks = new ArrayList<>();
    private FirebaseFirestore db;

    private TaskManager() {
        db = FirebaseFirestore.getInstance();
        loadTasksFromFirestore();
    }

    public static TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    public void addTask(Task task) {
        tasks.add(task);
        saveTaskToFirestore(task);
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        deleteTaskFromFirestore(task);
    }

    public void updateTask(Task updatedTask) {
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.getTitle().equals(updatedTask.getTitle()) &&
                    task.getDate().equals(updatedTask.getDate())) {
                tasks.set(i, updatedTask);
                updateTaskInFirestore(updatedTask);
                break;
            }
        }
    }

    private void loadTasksFromFirestore() {
        db.collection("tasks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tasks.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task firestoreTask = document.toObject(Task.class);
                            tasks.add(firestoreTask);
                        }
                    }
                });
    }

    private void saveTaskToFirestore(Task task) {
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("title", task.getTitle());
        taskData.put("description", task.getDescription());
        taskData.put("date", task.getDate());
        taskData.put("startTime", task.getStartTime());
        taskData.put("endTime", task.getEndTime());
        taskData.put("category", task.getCategory());
        taskData.put("status", task.getStatus());

        // Use a unique ID for the document
        String taskId = generateTaskId(task);

        db.collection("tasks")
                .document(taskId)
                .set(taskData)
                .addOnSuccessListener(aVoid -> {
                    // Task saved successfully
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    private void updateTaskInFirestore(Task task) {
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("title", task.getTitle());
        taskData.put("description", task.getDescription());
        taskData.put("date", task.getDate());
        taskData.put("startTime", task.getStartTime());
        taskData.put("endTime", task.getEndTime());
        taskData.put("category", task.getCategory());
        taskData.put("status", task.getStatus());

        String taskId = generateTaskId(task);

        db.collection("tasks")
                .document(taskId)
                .set(taskData)
                .addOnSuccessListener(aVoid -> {
                    // Task updated successfully
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    private void deleteTaskFromFirestore(Task task) {
        String taskId = generateTaskId(task);

        db.collection("tasks")
                .document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Task deleted successfully
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    private String generateTaskId(Task task) {
        // Create a unique ID using title and date
        return task.getTitle() + "_" + task.getDate().replace("-", "");
    }
}