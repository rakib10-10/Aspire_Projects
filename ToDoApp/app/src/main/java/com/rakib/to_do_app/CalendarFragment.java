package com.rakib.to_do_app;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView; // Changed from DatePicker
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView; // Updated View Type
    private RecyclerView recyclerTasks;
    private LinearLayout emptyState; // Added Empty State
    private FloatingActionButton btnAddTask;
    private TextView txtSelectedDate, txtTaskCount;

    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();
    private String selectedDateStr;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        initializeViews(view);

        // Load tasks initially
        taskList = TaskManager.getInstance(requireContext()).getAllTasks();

        setupTaskAdapter();
        setupCalendarView(); // Updated method name
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        calendarView = view.findViewById(R.id.calendarView); // Matches new XML ID
        recyclerTasks = view.findViewById(R.id.recyclerTasks);
        emptyState = view.findViewById(R.id.emptyState); // Matches new XML ID
        btnAddTask = view.findViewById(R.id.btnAddTask);
        txtSelectedDate = view.findViewById(R.id.txtSelectedDate);
        txtTaskCount = view.findViewById(R.id.txtTaskCount);
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
                    confirmDelete(task);
                }
            }

            @Override
            public void onTaskStatusChange(int position, String newStatus) {
                Task task = taskAdapter.getTaskAt(position);
                if (task != null) {
                    task.setStatus(newStatus);
                    TaskManager.getInstance(requireContext()).updateTask(task);
                    filterTasksByDate();
                    String statusText = newStatus.equals("running") ? "In Progress" : "Completed";
                    Toast.makeText(getContext(), "Task marked as " + statusText, Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTasks.setAdapter(taskAdapter);
    }

    private void setupCalendarView() {
        Calendar cal = Calendar.getInstance();
        selectedDateStr = sdf.format(cal.getTime());
        updateDateDisplay(cal);

        // CalendarView Listener
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);

            selectedDateStr = sdf.format(selected.getTime());
            updateDateDisplay(selected);
            filterTasksByDate();
        });

        // Initial filter
        filterTasksByDate();
    }

    private void setupClickListeners() {
        btnAddTask.setOnClickListener(v -> openAddTaskDialog());
    }

    private void updateDateDisplay(Calendar calendar) {
        if (txtSelectedDate != null) {
            txtSelectedDate.setText(displayDateFormat.format(calendar.getTime()));
        }

        // Count update is now handled inside filterTasksByDate to ensure sync
    }

    private void openAddTaskDialog() {
        AddTaskDialog dialog = new AddTaskDialog();

        Bundle args = new Bundle();
        args.putString("prefilledDate", selectedDateStr);
        dialog.setArguments(args);

        dialog.setOnTaskCreatedListener(new AddTaskDialog.OnTaskCreatedListener() {
            @Override
            public void onTaskCreated(String title, String description, String date, String startTime, String endTime, String category, String status, String priority) {
                taskList = TaskManager.getInstance(requireContext()).getAllTasks();
                filterTasksByDate();
            }
        });

        dialog.show(getParentFragmentManager(), "AddTaskDialog");
    }

    private void confirmDelete(Task task) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    ReminderManager reminderManager = new ReminderManager(getContext());
                    reminderManager.cancelReminder(task);

                    TaskManager.getInstance(requireContext()).removeTask(task);

                    taskList = TaskManager.getInstance(requireContext()).getAllTasks();
                    filterTasksByDate();

                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void openEditTaskDialog(Task task) {
        AddTaskDialog dialog = new AddTaskDialog();

        Bundle args = new Bundle();
        args.putLong("task_id", task.getId());
        args.putString("title", task.getTitle());
        args.putString("description", task.getDescription());
        args.putString("date", task.getDate());
        args.putString("startTime", task.getStartTime());
        args.putString("endTime", task.getEndTime());
        args.putString("category", task.getCategory());
        args.putString("status", task.getStatus());
        args.putString("priority", task.getPriority());
        args.putString("color_tag", task.getColorTag());
        dialog.setArguments(args);

        dialog.setOnTaskCreatedListener(new AddTaskDialog.OnTaskCreatedListener() {
            @Override
            public void onTaskCreated(String title, String description, String date, String startTime, String endTime, String category, String status, String priority) {
                taskList = TaskManager.getInstance(requireContext()).getAllTasks();
                filterTasksByDate();
            }
        });

        dialog.show(getParentFragmentManager(), "EditTaskDialog");
    }

    private void filterTasksByDate() {
        if (taskAdapter == null) return;

        List<Task> filtered = new ArrayList<>();
        for (Task task : taskList) {
            if (task.getDate() != null && task.getDate().equals(selectedDateStr)) {
                filtered.add(task);
            }
        }

        // Update Adapter
        taskAdapter.updateList(filtered);

        // Update Count Text
        if (txtTaskCount != null) {
            int count = filtered.size();
            txtTaskCount.setText(count + (count == 1 ? " task" : " tasks"));
        }

        // Toggle Empty State / RecyclerView
        if (filtered.isEmpty()) {
            recyclerTasks.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerTasks.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        taskList = TaskManager.getInstance(requireContext()).getAllTasks();
        filterTasksByDate();
    }
}