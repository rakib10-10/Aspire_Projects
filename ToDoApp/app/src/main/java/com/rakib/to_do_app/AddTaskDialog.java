package com.rakib.to_do_app;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddTaskDialog extends DialogFragment {

    private EditText edtTaskName, edtDate, edtStartTime, edtEndTime, edtDescription;
    private AutoCompleteTextView autoCompleteCategory;

    // Changed to MaterialButton to allow stroke manipulation
    private MaterialButton btnStatusRunning, btnStatusCompleted;

    private RadioGroup radioGroupPriority;
    private String selectedCategory = "";
    private String selectedStatus = "running";
    private String selectedPriority = "Medium";
    private String selectedColor = "#2196F3";
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
    public void setOnTaskCreatedListener(OnTaskCreatedListener listener) { this.listener = listener; }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.DialogTheme); // Ensure DialogTheme exists or remove style
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_add_task, null, false);

        initializeViews();
        setupCategoryAutoComplete();
        setupStatusButtons(); // Contains the fix for button toggling
        setupPrioritySelection();
        setupColorSelection();
        setupDatePicker();
        setupTimePickers();

        if (getArguments() != null && getArguments().containsKey("task_id")) {
            isEditMode = true;
            populateExistingData();
        } else if (getArguments() != null) {
            String prefilledDate = getArguments().getString("prefilledDate", "");
            if (!prefilledDate.isEmpty()) edtDate.setText(prefilledDate);
        }

        setupSaveButton();
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

        // Cast to MaterialButton
        btnStatusRunning = dialogView.findViewById(R.id.btnStatusRunning);
        btnStatusCompleted = dialogView.findViewById(R.id.btnStatusCompleted);

        edtDate.setInputType(InputType.TYPE_NULL);
        edtStartTime.setInputType(InputType.TYPE_NULL);
        edtEndTime.setInputType(InputType.TYPE_NULL);

        categoryList.add("Work"); categoryList.add("Personal"); categoryList.add("Study");
        categoryList.add("Health"); categoryList.add("Shopping");
    }

    private void setupStatusButtons() {
        // 1. Resolve Colors
        int colorPrimary;
        TypedValue typedValue = new TypedValue();
        if (requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)) {
            colorPrimary = typedValue.data;
        } else {
            colorPrimary = ContextCompat.getColor(requireContext(), R.color.purple_500); // Fallback color
        }

        int colorWhite = ContextCompat.getColor(requireContext(), android.R.color.white);
        int colorTransparent = ContextCompat.getColor(requireContext(), android.R.color.transparent);

        int finalColorPrimary = colorPrimary; // Needed for lambda

        // 2. Define "Running" Click Listener
        btnStatusRunning.setOnClickListener(v -> {
            selectedStatus = "running";

            // Style Running as Selected (Filled)
            btnStatusRunning.setBackgroundColor(finalColorPrimary);
            btnStatusRunning.setTextColor(colorWhite);
            btnStatusRunning.setStrokeWidth(0);

            // Style Completed as Unselected (Outlined)
            btnStatusCompleted.setBackgroundColor(colorTransparent);
            btnStatusCompleted.setTextColor(finalColorPrimary);
            btnStatusCompleted.setStrokeColor(ColorStateList.valueOf(finalColorPrimary));
            btnStatusCompleted.setStrokeWidth(3); // 1dp thickness
        });

        // 3. Define "Completed" Click Listener
        btnStatusCompleted.setOnClickListener(v -> {
            selectedStatus = "completed";

            // Style Completed as Selected (Filled)
            btnStatusCompleted.setBackgroundColor(finalColorPrimary);
            btnStatusCompleted.setTextColor(colorWhite);
            btnStatusCompleted.setStrokeWidth(0);

            // Style Running as Unselected (Outlined)
            btnStatusRunning.setBackgroundColor(colorTransparent);
            btnStatusRunning.setTextColor(finalColorPrimary);
            btnStatusRunning.setStrokeColor(ColorStateList.valueOf(finalColorPrimary));
            btnStatusRunning.setStrokeWidth(3);
        });

        // 4. Set Initial State
        if (selectedStatus.equals("running")) {
            btnStatusRunning.performClick();
        } else {
            btnStatusCompleted.performClick();
        }
    }

    private void setupCategoryAutoComplete() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryList);
        autoCompleteCategory.setAdapter(categoryAdapter);
        autoCompleteCategory.setThreshold(1);
        autoCompleteCategory.setOnItemClickListener((parent, view, position, id) -> selectedCategory = (String) parent.getItemAtPosition(position));
        autoCompleteCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String input = autoCompleteCategory.getText().toString().trim();
                if (!input.isEmpty()) { selectedCategory = input; if (!categoryList.contains(input)) categoryList.add(input); }
            }
        });
    }

    private void setupColorSelection() {
        int[] colorViewIds = {R.id.color_red, R.id.color_blue, R.id.color_green, R.id.color_yellow, R.id.color_purple, R.id.color_orange};
        String[] colorValues = {"#F44336", "#2196F3", "#4CAF50", "#FFEB3B", "#9C27B0", "#FF9800"};
        for (int i = 0; i < colorViewIds.length; i++) {
            final String color = colorValues[i];
            dialogView.findViewById(colorViewIds[i]).setOnClickListener(v -> { selectedColor = color; highlightSelectedColor(selectedColor); });
        }
        highlightSelectedColor(selectedColor);
    }

    private void highlightSelectedColor(String selectedColor) {
        int[] colorViewIds = {R.id.color_red, R.id.color_blue, R.id.color_green, R.id.color_yellow, R.id.color_purple, R.id.color_orange};
        for (int id : colorViewIds) {
            View view = dialogView.findViewById(id);
            String tagColor = (String) view.getTag();
            GradientDrawable background = (GradientDrawable) ((GradientDrawable) view.getBackground()).mutate();
            if (tagColor != null && tagColor.equals(selectedColor)) background.setStroke(8, Color.BLACK);
            else background.setStroke(0, Color.TRANSPARENT);
        }
    }

    private void setupPrioritySelection() {
        radioGroupPriority.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_low) selectedPriority = "Low";
            else if (checkedId == R.id.radio_high) selectedPriority = "High";
            else selectedPriority = "Medium";
        });
    }

    private void setupDatePicker() {
        edtDate.setOnClickListener(v -> showDatePickerDialog());
        if (edtDate.getText().toString().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            edtDate.setText(sdf.format(Calendar.getInstance().getTime()));
        }
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            calendar.setTime(sdf.parse(edtDate.getText().toString()));
        } catch (Exception e) {}

        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            edtDate.setText(sdf.format(selected.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupTimePickers() {
        edtStartTime.setOnClickListener(v -> showTimePicker(true));
        edtEndTime.setOnClickListener(v -> showTimePicker(false));
    }

    private void showTimePicker(boolean isStartTime) {
        final Calendar calendar = Calendar.getInstance();
        try {
            String timeText = isStartTime ? edtStartTime.getText().toString() : edtEndTime.getText().toString();
            if (!timeText.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
                calendar.setTime(sdf.parse(timeText));
            }
        } catch (Exception e) {}

        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            cal.set(Calendar.MINUTE, minute);
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            String formattedTime = sdf.format(cal.getTime());

            if (isStartTime) edtStartTime.setText(formattedTime);
            else edtEndTime.setText(formattedTime);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    private void setupSaveButton() {
        Button btnCreate = dialogView.findViewById(R.id.saveTaskButton);
        btnCreate.setText(isEditMode ? "Update Task" : "Create Task");

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
            TaskManager taskManager = TaskManager.getInstance(requireContext());
            ReminderManager reminderManager = new ReminderManager(requireContext());
            Task task;

            if (isEditMode && existingTaskId != -1) {
                // Update mode: Use existing ID
                task = new Task(existingTaskId, name, desc, date, startTime, endTime, category, selectedStatus);
                reminderManager.cancelReminder(task); // Cancel old alarms before setting new ones
            } else {
                // Create mode
                task = new Task(name, desc, date, startTime, endTime, category, selectedStatus);
            }
            task.setPriority(selectedPriority);
            task.setColorTag(selectedColor);

            // Save to DB first to generate the ID (Crucial for PendingIntent uniqueness)
            if (isEditMode) {
                taskManager.updateTask(task);
            } else {
                long id = taskManager.addTask(task);
                task.setId(id); // Assign the database ID to the task object
            }

            // --- FIXED REMINDER LOGIC START ---
            Date dueDateTime = task.getDueDateTime();

            if (dueDateTime != null) {
                long currentTime = System.currentTimeMillis();
                long taskTime = dueDateTime.getTime();

                // 1. Set Reminder EXACTLY at task start time (Remove the -1 hour logic)
                long reminderTime = taskTime;

                // Optional: If you strictly want it 5 mins before, use:
                // long reminderTime = taskTime - (5 * 60 * 1000);

                if (reminderTime > currentTime) {
                    // Time is in the future: Set the Alarm
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
                        if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                            reminderManager.setInexactReminder(task); // Fallback
                        } else {
                            reminderManager.setReminder(task); // Exact
                        }
                    } else {
                        reminderManager.setReminder(task);
                    }
                    Toast.makeText(getContext(), "Task saved with reminder!", Toast.LENGTH_SHORT).show();
                } else {
                    // Time is in the past (or too close to now if using offset)
                    Toast.makeText(getContext(), "Task saved (No reminder: Time has passed)", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Task saved (Invalid date format)", Toast.LENGTH_SHORT).show();
            }
            // --- FIXED REMINDER LOGIC END ---

            if (listener != null) {
                listener.onTaskCreated(name, desc, date, startTime, endTime, category, selectedStatus, selectedPriority);
            }
            dismiss();
        });
    }

    private void populateExistingData() {
        existingTaskId = getArguments().getLong("task_id", -1);
        edtTaskName.setText(getArguments().getString("title", ""));
        edtDescription.setText(getArguments().getString("description", ""));
        edtDate.setText(getArguments().getString("date", ""));
        edtStartTime.setText(getArguments().getString("startTime", ""));
        edtEndTime.setText(getArguments().getString("endTime", ""));
        autoCompleteCategory.setText(getArguments().getString("category", ""));

        selectedStatus = getArguments().getString("status", "running");
        selectedPriority = getArguments().getString("priority", "Medium");
        selectedColor = getArguments().getString("color_tag", "#2196F3");

        // Trigger the click logic to set visual state
        if ("completed".equals(selectedStatus)) {
            btnStatusCompleted.performClick();
        } else {
            btnStatusRunning.performClick();
        }

        if ("Low".equals(selectedPriority)) radioGroupPriority.check(R.id.radio_low);
        else if ("High".equals(selectedPriority)) radioGroupPriority.check(R.id.radio_high);
        else radioGroupPriority.check(R.id.radio_medium);

        highlightSelectedColor(selectedColor);
    }
}