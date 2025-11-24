package com.rakib.to_do_app;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AddTaskDialog extends DialogFragment {

    private EditText edtTaskName, edtDate, edtStartTime, edtEndTime, edtDescription;
    private AutoCompleteTextView autoCompleteCategory;
    private Button btnStatusRunning, btnStatusCompleted;
    private RadioGroup radioGroupPriority;
    private String selectedCategory = "";
    private String selectedStatus = "running";
    private String selectedPriority = "Medium";
    private boolean isEditMode = false;
    private long existingTaskId = -1;
    private View dialogView;
    private List<String> categoryList = new ArrayList<>();

    public interface OnTaskCreatedListener {
        void onTaskCreated(String title, String description, String date,
                           String startTime, String endTime, String category,
                           String status, String priority);
    }

    private OnTaskCreatedListener listener;

    public void setOnTaskCreatedListener(OnTaskCreatedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.DialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_add_task, null);

        initializeViews();
        setupCategoryAutoComplete();
        setupStatusButtons();
        setupPrioritySelection();
        setupDatePicker();
        setupTimePickers();
        setupSaveButton();

        if (getArguments() != null) {
            isEditMode = true;
            populateExistingData();
        }

        builder.setView(dialogView);
        return builder.create();
    }

    private void initializeViews() {
        edtTaskName = dialogView.findViewById(R.id.taskTitle);
        edtDate = dialogView.findViewById(R.id.edtDate);
        edtStartTime = dialogView.findViewById(R.id.edtStartTime);
        edtEndTime = dialogView.findViewById(R.id.edtEndTime);
        edtDescription = dialogView.findViewById(R.id.taskDescription);
        autoCompleteCategory = dialogView.findViewById(R.id.autoCompleteCategory);
        radioGroupPriority = dialogView.findViewById(R.id.radio_group_priority);

        btnStatusRunning = dialogView.findViewById(R.id.btnStatusRunning);
        btnStatusCompleted = dialogView.findViewById(R.id.btnStatusCompleted);

        edtDate.setInputType(InputType.TYPE_NULL);
        edtStartTime.setInputType(InputType.TYPE_NULL);
        edtEndTime.setInputType(InputType.TYPE_NULL);

        // Initialize with some common categories
        categoryList.add("Work");
        categoryList.add("Personal");
        categoryList.add("Study");
        categoryList.add("Health");
        categoryList.add("Shopping");
        categoryList.add("Meeting");
        categoryList.add("Exercise");
        categoryList.add("Family");
    }

    private void setupCategoryAutoComplete() {
        // Create adapter for AutoCompleteTextView
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categoryList
        );
        autoCompleteCategory.setAdapter(categoryAdapter);

        // Set threshold to show suggestions after 1 character
        autoCompleteCategory.setThreshold(1);

        // Listen for category selection
        autoCompleteCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = (String) parent.getItemAtPosition(position);
        });

        // Also allow custom input
        autoCompleteCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String input = autoCompleteCategory.getText().toString().trim();
                if (!input.isEmpty()) {
                    selectedCategory = input;
                    // Add to suggestions if it's new
                    if (!categoryList.contains(input)) {
                        categoryList.add(input);
                        ArrayAdapter<String> updatedAdapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                categoryList
                        );
                        autoCompleteCategory.setAdapter(updatedAdapter);
                    }
                }
            }
        });
    }

    private void setupStatusButtons() {
        View.OnClickListener statusClickListener = v -> {
            resetStatusButtons();
            Button clickedButton = (Button) v;
            clickedButton.setBackgroundResource(R.drawable.status_selected);
            clickedButton.setTextColor(getResources().getColor(android.R.color.white));
            selectedStatus = (String) clickedButton.getTag();
        };

        btnStatusRunning.setTag("running");
        btnStatusCompleted.setTag("completed");

        btnStatusRunning.setOnClickListener(statusClickListener);
        btnStatusCompleted.setOnClickListener(statusClickListener);

        btnStatusRunning.setBackgroundResource(R.drawable.status_selected);
        btnStatusRunning.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void setupPrioritySelection() {
        radioGroupPriority.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_low) {
                selectedPriority = "Low";
            } else if (checkedId == R.id.radio_high) {
                selectedPriority = "High";
            } else {
                selectedPriority = "Medium";
            }
        });
    }

    private void resetStatusButtons() {
        btnStatusRunning.setBackgroundResource(R.drawable.status_unselected);
        btnStatusRunning.setTextColor(getResources().getColor(android.R.color.black));
        btnStatusCompleted.setBackgroundResource(R.drawable.status_unselected);
        btnStatusCompleted.setTextColor(getResources().getColor(android.R.color.black));
    }

    private void setupDatePicker() {
        edtDate.setOnClickListener(v -> showDatePickerDialog());

        String prefilledDate = "";
        if (getArguments() != null) {
            prefilledDate = getArguments().getString("prefilledDate", "");
        }

        if (!prefilledDate.isEmpty()) {
            edtDate.setText(prefilledDate);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            String today = sdf.format(Calendar.getInstance().getTime());
            edtDate.setText(today);
        }
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    String formattedDate = sdf.format(selectedDate.getTime());
                    edtDate.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void setupTimePickers() {
        edtStartTime.setOnClickListener(v -> showTimePicker(true));
        edtEndTime.setOnClickListener(v -> showTimePicker(false));
    }

    private void showTimePicker(boolean isStartTime) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (TimePicker view, int selectedHour, int selectedMinute) -> {
                    String amPm;
                    int displayHour = selectedHour;

                    if (selectedHour >= 12) {
                        amPm = "PM";
                        if (selectedHour > 12) displayHour = selectedHour - 12;
                    } else {
                        amPm = "AM";
                        if (selectedHour == 0) displayHour = 12;
                    }

                    String formattedTime = String.format("%02d:%02d %s", displayHour, selectedMinute, amPm);

                    if (isStartTime) {
                        edtStartTime.setText(formattedTime);
                    } else {
                        edtEndTime.setText(formattedTime);
                    }
                },
                hour,
                minute,
                false
        );
        timePickerDialog.show();
    }

    private void setupSaveButton() {
        Button btnCreate = dialogView.findViewById(R.id.saveTaskButton);

        if (isEditMode) {
            btnCreate.setText("Update Task");
        }

        btnCreate.setOnClickListener(v -> {
            String name = edtTaskName.getText().toString().trim();
            String date = edtDate.getText().toString().trim();
            String startTime = edtStartTime.getText().toString().trim();
            String endTime = edtEndTime.getText().toString().trim();
            String desc = edtDescription.getText().toString().trim();
            String category = autoCompleteCategory.getText().toString().trim();

            if (name.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || desc.isEmpty() || category.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedCategory = category;

            // Create task object
            Task task;
            if (isEditMode && existingTaskId != -1) {
                // Update existing task
                task = new Task(existingTaskId, name, desc, date, startTime, endTime, category, selectedStatus);
            } else {
                // Create new task
                task = new Task(name, desc, date, startTime, endTime, category, selectedStatus);
            }

            // Set priority
            task.setPriority(selectedPriority);

            // Save to SQLite via TaskManager
            TaskManager taskManager = TaskManager.getInstance(requireContext());
            if (isEditMode) {
                taskManager.updateTask(task);
                Toast.makeText(getContext(), "Task updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                taskManager.addTask(task);
                Toast.makeText(getContext(), "Task created successfully!", Toast.LENGTH_SHORT).show();
            }

            // Set reminder
            ReminderManager reminderManager = new ReminderManager(requireContext());
            if (isEditMode) {
                // Cancel existing reminder first
                reminderManager.cancelReminder(task);
            }

            // Use the proper permission-aware method
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                    reminderManager.setInexactReminder(task);
                } else {
                    reminderManager.setReminder(task);
                }
            } else {
                reminderManager.setReminder(task);
            }

            // Notify listener for UI updates
            if (listener != null) {
                listener.onTaskCreated(name, desc, date, startTime, endTime, category, selectedStatus, selectedPriority);
            }

            dismiss();
        });
    }

    private void populateExistingData() {
        existingTaskId = getArguments().getLong("task_id", -1);
        String title = getArguments().getString("title", "");
        String description = getArguments().getString("description", "");
        String date = getArguments().getString("date", "");
        String startTime = getArguments().getString("startTime", "");
        String endTime = getArguments().getString("endTime", "");
        String category = getArguments().getString("category", "");
        String status = getArguments().getString("status", "running");
        String priority = getArguments().getString("priority", "Medium");

        edtTaskName.setText(title);
        edtDescription.setText(description);
        edtDate.setText(date);
        edtStartTime.setText(startTime);
        edtEndTime.setText(endTime);
        autoCompleteCategory.setText(category);
        selectedCategory = category;
        selectedPriority = priority;

        if (!status.isEmpty()) {
            selectedStatus = status;
            highlightStatusButton(status);
        }

        // Set priority radio button
        if (priority != null) {
            switch (priority.toLowerCase()) {
                case "low":
                    radioGroupPriority.check(R.id.radio_low);
                    break;
                case "high":
                    radioGroupPriority.check(R.id.radio_high);
                    break;
                default:
                    radioGroupPriority.check(R.id.radio_medium);
                    break;
            }
        }
    }

    private void highlightStatusButton(String status) {
        resetStatusButtons();
        if ("completed".equals(status)) {
            btnStatusCompleted.setBackgroundResource(R.drawable.status_selected);
            btnStatusCompleted.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            btnStatusRunning.setBackgroundResource(R.drawable.status_selected);
            btnStatusRunning.setTextColor(getResources().getColor(android.R.color.white));
        }
    }
}