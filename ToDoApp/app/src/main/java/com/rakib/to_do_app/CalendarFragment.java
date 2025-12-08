package com.rakib.to_do_app;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
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

    private DatePicker datePicker;
    private RecyclerView recyclerTasks;
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
        setupTaskAdapter();
        setupDatePicker();
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        datePicker = view.findViewById(R.id.datePicker);
        recyclerTasks = view.findViewById(R.id.recyclerTasks);
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

    private void setTaskReminder(Task task) {
        ReminderManager reminderManager = new ReminderManager(requireContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {

                reminderManager.setInexactReminder(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getDueDate().getTime()
                );
                return;
            }
        }

        //
        reminderManager.setReminder(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate().getTime()
        );
    }

    private void setupDatePicker() {
        Calendar cal = Calendar.getInstance();
        selectedDateStr = sdf.format(cal.getTime());
        updateDateDisplay(cal);

        datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, monthOfYear, dayOfMonth);
                    selectedDateStr = sdf.format(selected.getTime());
                    updateDateDisplay(selected);
                    filterTasksByDate();
                });

        filterTasksByDate();
    }

    private void setupClickListeners() {
        // calls the dialog method when the FAB is clicked
        btnAddTask.setOnClickListener(v -> openAddTaskDialog());
    }

    private void updateDateDisplay(Calendar calendar) {
        if (txtSelectedDate != null) {
            txtSelectedDate.setText(displayDateFormat.format(calendar.getTime()));
        }

        if (txtTaskCount != null) {
            int taskCount = getTaskCountForSelectedDate();
            txtTaskCount.setText(taskCount + (taskCount == 1 ? " task" : " tasks"));
        }
    }

    private int getTaskCountForSelectedDate() {
        int count = 0;
        for (Task task : taskList) {
            if (task.getDate() != null && task.getDate().equals(selectedDateStr)) {
                count++;
            }
        }
        return count;
    }

    private void openAddTaskDialog() {
        AddTaskDialog dialog = new AddTaskDialog();

        Bundle args = new Bundle();
        args.putString("prefilledDate", selectedDateStr);
        dialog.setArguments(args);

        dialog.setOnTaskCreatedListener(new AddTaskDialog.OnTaskCreatedListener() {


            @Override
            public void onTaskCreated(String title, String description, String date, String startTime, String endTime, String category, String status, String priority) {
                // Removing redundant save logic. The dialog now saves the task itself.


                // Refresh local list
                taskList = TaskManager.getInstance(requireContext()).getAllTasks();
                filterTasksByDate();

                Toast.makeText(requireContext(), "Task Added with reminder!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show(getParentFragmentManager(), "AddTaskDialog");
    }

    private void confirmDelete(Task task) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task? This will also cancel the reminder.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Cancel reminder first
                    ReminderManager reminderManager = new ReminderManager(getContext());
                    reminderManager.cancelReminder(task);

                    // Remove from TaskManager
                    TaskManager.getInstance(requireContext()).removeTask(task);

                    // Refresh local list
                    taskList = TaskManager.getInstance(requireContext()).getAllTasks();
                    filterTasksByDate();

                    Toast.makeText(getContext(), "Task and reminder deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void openEditTaskDialog(Task task) {
        AddTaskDialog dialog = new AddTaskDialog();

        Bundle args = new Bundle();
        // Pass task ID for update query
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
                //  Removing redundant update logic. The dialog now updates the task itself.

                // Refresh local list
                taskList = TaskManager.getInstance(requireContext()).getAllTasks();
                filterTasksByDate();

                Toast.makeText(requireContext(), "Task updated with new reminder!", Toast.LENGTH_SHORT).show();
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
        taskAdapter.updateList(filtered);
        updateDateDisplay(getSelectedCalendar());
    }

    private Calendar getSelectedCalendar() {
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(selectedDateStr));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cal;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh tasks from TaskManager
        taskList = TaskManager.getInstance(requireContext()).getAllTasks();
        filterTasksByDate();
    }
}