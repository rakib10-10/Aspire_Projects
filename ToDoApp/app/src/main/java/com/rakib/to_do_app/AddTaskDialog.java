package com.rakib.to_do_app;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import java.util.List;
import java.util.Locale;

public class AddTaskDialog extends DialogFragment {

    private EditText edtTaskName, edtDate, edtStartTime, edtEndTime, edtDescription;
    private AutoCompleteTextView autoCompleteCategory;
    private Button btnStatusRunning, btnStatusCompleted;
    private RadioGroup radioGroupPriority;
    private String selectedCategory = "";
    private String selectedStatus = "running";
    private String selectedPriority = "Medium";
    private String selectedColor = "#2196F3"; // Default blue
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


        dialogView = inflater.inflate(R.layout.dialog_add_task, null, false);

        initializeViews();
        setupCategoryAutoComplete();
        setupStatusButtons();
        setupPrioritySelection();
        setupColorSelection();
        setupDatePicker();
        setupTimePickers();


        if (getArguments() != null && getArguments().containsKey("task_id")) {
            isEditMode = true;
            populateExistingData();
        } else if (getArguments() != null) {
            // Handle prefilled date for Add Mode
            String prefilledDate = getArguments().getString("prefilledDate", "");
            if (!prefilledDate.isEmpty()) {
                edtDate.setText(prefilledDate);
            }
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

        btnStatusRunning = dialogView.findViewById(R.id.btnStatusRunning);
        btnStatusCompleted = dialogView.findViewById(R.id.btnStatusCompleted);

        edtDate.setInputType(InputType.TYPE_NULL);
        edtStartTime.setInputType(InputType.TYPE_NULL);
        edtEndTime.setInputType(InputType.TYPE_NULL);

        // Initializing with some common categories
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
        // Creating adapter for AutoCompleteTextView
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categoryList
        );
        autoCompleteCategory.setAdapter(categoryAdapter);
        autoCompleteCategory.setThreshold(1);

        autoCompleteCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = (String) parent.getItemAtPosition(position);
        });

        // Also allow custom input
        autoCompleteCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String input = autoCompleteCategory.getText().toString().trim();
                if (!input.isEmpty()) {
                    selectedCategory = input;
                    if (!categoryList.contains(input)) {
                        categoryList.add(input);
                        // No need to create a new adapter instance every time, but simplified for brevity
                    }
                }
            }
        });
    }

    // AddTaskDialog.java

    // Update the setupColorSelection method
    private void setupColorSelection() {
        int[] colorViewIds = {
                R.id.color_red, R.id.color_blue, R.id.color_green,
                R.id.color_yellow, R.id.color_purple, R.id.color_orange
        };

        String[] colorValues = {
                "#F44336", "#2196F3", "#4CAF50",
                "#FFEB3B", "#9C27B0", "#FF9800"
        };

        for (int i = 0; i < colorViewIds.length; i++) {
            final View colorView = dialogView.findViewById(colorViewIds[i]);
            final String color = colorValues[i];

            colorView.setOnClickListener(v -> {
                selectedColor = color;
                Log.d("ColorDebug", "Color selected: " + selectedColor);
                highlightSelectedColor(selectedColor);
            });
        }

        // Set default color as selected initially
        highlightSelectedColor(selectedColor);
    }

    private void highlightSelectedColor(String selectedColor) {
        int[] colorViewIds = {
                R.id.color_red, R.id.color_blue, R.id.color_green,
                R.id.color_yellow, R.id.color_purple, R.id.color_orange
        };

        for (int id : colorViewIds) {
            View view = dialogView.findViewById(id);
            String tagColor = (String) view.getTag(); // Ensure android:tag is set in XML

            // We need to manipulate the background drawable to add a stroke
            GradientDrawable background = (GradientDrawable) view.getBackground();

            // Use mutate() to ensure we don't affect other views sharing this drawable state
            background = (GradientDrawable) background.mutate();

            if (tagColor.equals(selectedColor)) {
                // Add a border to indicate selection (White or Dark Gray depending on theme)
                background.setStroke(8, Color.BLACK); // Use a visible contrast color
            } else {
                // Remove border
                background.setStroke(0, Color.TRANSPARENT);
            }
        }
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


        if (selectedStatus.equals("running")) {
            btnStatusRunning.setBackgroundResource(R.drawable.status_selected);
            btnStatusRunning.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            btnStatusCompleted.setBackgroundResource(R.drawable.status_selected);
            btnStatusCompleted.setTextColor(getResources().getColor(android.R.color.white));
        }
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


        if (edtDate.getText().toString().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            String today = sdf.format(Calendar.getInstance().getTime());
            edtDate.setText(today);
        }
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();


        try {
            Calendar current = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            current.setTime(sdf.parse(edtDate.getText().toString()));
            calendar.set(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH));
        } catch (Exception e) {

        }

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


        String existingTime = isStartTime ? edtStartTime.getText().toString() : edtEndTime.getText().toString();
        if (!existingTime.isEmpty()) {
            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Calendar existingCal = Calendar.getInstance();
                existingCal.setTime(timeFormat.parse(existingTime));
                hour = existingCal.get(Calendar.HOUR_OF_DAY);
                minute = existingCal.get(Calendar.MINUTE);
            } catch (Exception e) {

            }
        }

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

                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, selectedMinute, amPm);

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
        } else {
            btnCreate.setText("Create Task");
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


            Log.d("ColorDebug", "Final Selected Color: " + selectedColor);


            Task task;
            TaskManager taskManager = TaskManager.getInstance(requireContext());
            ReminderManager reminderManager = new ReminderManager(requireContext());

            if (isEditMode && existingTaskId != -1) {
                // For editing, we update the existing task object attributes
                task = new Task(existingTaskId, name, desc, date, startTime, endTime, category, selectedStatus);

                // Cancelling old reminder BEFORE updating the task in DB
                reminderManager.cancelReminder(task);

            } else {
                // Creating new task (ID will be assigned by DB)
                task = new Task(name, desc, date, startTime, endTime, category, selectedStatus);
            }

            // Set priority and the CORRECT color tag
            task.setPriority(selectedPriority);
            task.setColorTag(selectedColor);

            Log.d("ColorDebug", "Task color before final save: " + task.getColorTag());

            // 2. SAVE TO SQLITE AND GET ID
            if (isEditMode) {
                taskManager.updateTask(task);
                Toast.makeText(getContext(), "Task updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                // Must get the ID for the reminder manager immediately after creation
                long newId = taskManager.addTask(task);
                task.setId(newId); // Assign the generated ID to the Task object
                Toast.makeText(getContext(), "Task created successfully!", Toast.LENGTH_SHORT).show();
            }

            // 3. SET NEW REMINDER (Requires the task object to have a valid ID set in step 2)

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

            // 4. NOTIFY LISTENER FOR UI UPDATES
            if (listener != null) {
                listener.onTaskCreated(name, desc, date, startTime, endTime, category, selectedStatus, selectedPriority);
            }

            dismiss();
        });
    }

    private void populateExistingData() {
        // Reading data from arguments bundle
        existingTaskId = getArguments().getLong("task_id", -1);
        String title = getArguments().getString("title", "");
        String description = getArguments().getString("description", "");
        String date = getArguments().getString("date", "");
        String startTime = getArguments().getString("startTime", "");
        String endTime = getArguments().getString("endTime", "");
        String category = getArguments().getString("category", "");
        String status = getArguments().getString("status", "running");
        String priority = getArguments().getString("priority", "Medium");
        String color = getArguments().getString("color_tag", "#2196F3");

        // Setting view values
        edtTaskName.setText(title);
        edtDescription.setText(description);
        edtDate.setText(date);
        edtStartTime.setText(startTime);
        edtEndTime.setText(endTime);
        autoCompleteCategory.setText(category);

        // Setting internal state variables
        selectedCategory = category;
        selectedPriority = priority;
        selectedColor = color;

        // Highlight UI based on state
        if (!status.isEmpty()) {
            selectedStatus = status;
            highlightStatusButton(status);
        }

        // Setting priority radio button
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

        // Setting color selection
        highlightSelectedColor(selectedColor);
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