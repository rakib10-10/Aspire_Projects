package com.rakib.to_do_app;

import android.app.AlertDialog;
import android.os.Bundle;
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
                    TaskManager.getInstance().updateTask(task);
                    filterTasksByDate();
                    String statusText = newStatus.equals("running") ? "In Progress" : "Completed";
                    Toast.makeText(getContext(), "Task marked as " + statusText, Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTasks.setAdapter(taskAdapter);
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
            public void onTaskCreated(String title, String description, String date, String startTime, String endTime, String category, String status) {
                Task task = new Task(title, description, date, startTime, endTime, category, status);
                taskList.add(task);
                TaskManager.getInstance().addTask(task);
                filterTasksByDate();
                Toast.makeText(requireContext(), "Task Added: " + title, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show(getParentFragmentManager(), "AddTaskDialog");
    }

    private void confirmDelete(Task task) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    taskList.remove(task);
                    TaskManager.getInstance().removeTask(task);
                    filterTasksByDate();
                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
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
                task.setTitle(title);
                task.setDescription(description);
                task.setDate(date);
                task.setStartTime(startTime);
                task.setEndTime(endTime);
                task.setCategory(category);
                task.setStatus(status);
                TaskManager.getInstance().updateTask(task);
                filterTasksByDate();
                Toast.makeText(requireContext(), "Task updated!", Toast.LENGTH_SHORT).show();
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
        taskList = TaskManager.getInstance().getTasks();
        filterTasksByDate();
    }
}