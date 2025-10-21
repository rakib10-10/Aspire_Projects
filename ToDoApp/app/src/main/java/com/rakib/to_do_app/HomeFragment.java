package com.rakib.to_do_app;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private Button btnMyTasks, btnInProgress, btnCompleted;
    private RecyclerView recyclerTasks;
    private TextView txtEmptyState;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks = new ArrayList<>();
    private String currentTab = "all";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        setupTaskAdapter();
        setupClickListeners();
        loadTasks();

        return view;
    }

    private void initializeViews(View view) {
        btnMyTasks = view.findViewById(R.id.btnMyTasks);
        btnInProgress = view.findViewById(R.id.btnInProgress);
        btnCompleted = view.findViewById(R.id.btnCompleted);
        recyclerTasks = view.findViewById(R.id.recyclerTasks);
        txtEmptyState = view.findViewById(R.id.txtEmptyState);
    }

    private void setupTaskAdapter() {
        taskAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onTaskEdit(int position) {
                Task task = taskAdapter.getTaskAt(position);
                if (task != null) {
                    openEditTaskDialog(task);
                }
            }

            @Override
            public void onTaskDelete(int position) {
                Task task = taskAdapter.getTaskAt(position);
                if (task != null) {
                    // Cancel reminder first
                    ReminderManager reminderManager = new ReminderManager(requireContext());
                    reminderManager.cancelReminder(task);

                    // Then remove from TaskManager (which handles Firestore)
                    TaskManager.getInstance().removeTask(task);
                    allTasks.remove(task);
                    filterTasksByStatus(currentTab);
                    Toast.makeText(getContext(), "Task and reminder deleted", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTaskStatusChange(int position, String newStatus) {
                Task task = taskAdapter.getTaskAt(position);
                if (task != null) {
                    task.setStatus(newStatus);
                    TaskManager.getInstance().updateTask(task);
                    filterTasksByStatus(currentTab);
                    String statusText = newStatus.equals("running") ? "In Progress" : "Completed";
                    Toast.makeText(getContext(), "Task marked as " + statusText, Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTasks.setAdapter(taskAdapter);
    }

    private void setupClickListeners() {
        btnMyTasks.setOnClickListener(v -> {
            selectTab(btnMyTasks);
            currentTab = "all";
            filterTasksByStatus("all");
        });

        btnInProgress.setOnClickListener(v -> {
            selectTab(btnInProgress);
            currentTab = "running";
            filterTasksByStatus("running");
        });

        btnCompleted.setOnClickListener(v -> {
            selectTab(btnCompleted);
            currentTab = "completed";
            filterTasksByStatus("completed");
        });
    }

    private void selectTab(Button selectedButton) {
        btnMyTasks.setBackgroundResource(R.drawable.btn_unselected);
        btnInProgress.setBackgroundResource(R.drawable.btn_unselected);
        btnCompleted.setBackgroundResource(R.drawable.btn_unselected);

        btnMyTasks.setTextColor(getResources().getColor(android.R.color.darker_gray));
        btnInProgress.setTextColor(getResources().getColor(android.R.color.darker_gray));
        btnCompleted.setTextColor(getResources().getColor(android.R.color.darker_gray));

        selectedButton.setBackgroundResource(R.drawable.btn_selected);
        selectedButton.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void loadTasks() {
        allTasks = TaskManager.getInstance().getTasks();
        filterTasksByStatus(currentTab);
    }

    private void filterTasksByStatus(String status) {
        List<Task> filtered = new ArrayList<>();

        for (Task task : allTasks) {
            if (status.equals("all")) {
                filtered.add(task);
            } else if (task.getStatus() != null && task.getStatus().equals(status)) {
                filtered.add(task);
            }
        }

        taskAdapter.updateList(filtered);

        if (filtered.isEmpty()) {
            recyclerTasks.setVisibility(View.GONE);
            txtEmptyState.setVisibility(View.VISIBLE);
            switch (status) {
                case "all":
                    txtEmptyState.setText("No tasks yet. Create your first task!");
                    break;
                case "running":
                    txtEmptyState.setText("No tasks in progress");
                    break;
                case "completed":
                    txtEmptyState.setText("No completed tasks yet");
                    break;
            }
        } else {
            recyclerTasks.setVisibility(View.VISIBLE);
            txtEmptyState.setVisibility(View.GONE);
        }
    }

    private void openEditTaskDialog(Task task) {
        AddTaskDialog dialog = new AddTaskDialog();

        Bundle args = new Bundle();
        args.putString("title", task.getTitle());
        args.putString("description", task.getDescription());
        args.putString("date", task.getDate());
        args.putString("startTime", task.getStartTime());
        args.putString("endTime", task.getEndTime());
        args.putString("category", task.getCategory());
        args.putString("status", task.getStatus());
        dialog.setArguments(args);

        dialog.setOnTaskCreatedListener(new AddTaskDialog.OnTaskCreatedListener() {
            @Override
            public void onTaskCreated(String title, String description, String date, String startTime, String endTime, String category, String status) {
                // Cancel old reminder
                ReminderManager reminderManager = new ReminderManager(requireContext());
                reminderManager.cancelReminder(task);

                // Update task
                task.setTitle(title);
                task.setDescription(description);
                task.setDate(date);
                task.setStartTime(startTime);
                task.setEndTime(endTime);
                task.setCategory(category);
                task.setStatus(status);

                // Update in TaskManager (which handles Firestore)
                TaskManager.getInstance().updateTask(task);

                // Set new reminder
                reminderManager.setReminder(task);

                // Refresh the task list
                filterTasksByStatus(currentTab);

                Toast.makeText(requireContext(), "Task updated with new reminder!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show(getParentFragmentManager(), "EditTaskDialog");
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }
}