package com.rakib.to_do_app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskDialog extends DialogFragment {

    private EditText edtTaskName, edtDate, edtStartTime, edtEndTime, edtDescription;
    private Button btnDesign, btnMeeting, btnCoding, btnLearning, btnTesting, btnQuickCall;
    private Button btnStatusRunning, btnStatusCompleted;
    private String selectedCategory = "";
    private String selectedStatus = "running";
    private boolean isEditMode = false;
    private View dialogView;

    public interface OnTaskCreatedListener {
        void onTaskCreated(String title, String description, String date, String startTime, String endTime, String category, String status);
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
        setupCategoryButtons();
        setupStatusButtons();
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

        btnDesign = dialogView.findViewById(R.id.btnDesign);
        btnMeeting = dialogView.findViewById(R.id.btnMeeting);
        btnCoding = dialogView.findViewById(R.id.btnCoding);
        btnLearning = dialogView.findViewById(R.id.btnLearning);
        btnTesting = dialogView.findViewById(R.id.btnTesting);
        btnQuickCall = dialogView.findViewById(R.id.btnQuickCall);

        btnStatusRunning = dialogView.findViewById(R.id.btnStatusRunning);
        btnStatusCompleted = dialogView.findViewById(R.id.btnStatusCompleted);

        edtDate.setInputType(InputType.TYPE_NULL);
        edtStartTime.setInputType(InputType.TYPE_NULL);
        edtEndTime.setInputType(InputType.TYPE_NULL);
    }

    private void setupCategoryButtons() {
        View.OnClickListener categoryClickListener = v -> {
            resetCategoryButtons();
            Button clickedButton = (Button) v;
            clickedButton.setBackgroundResource(R.drawable.category_chip_selected);
            clickedButton.setTextColor(getResources().getColor(android.R.color.white));
            selectedCategory = clickedButton.getText().toString();
        };

        btnDesign.setOnClickListener(categoryClickListener);
        btnMeeting.setOnClickListener(categoryClickListener);
        btnCoding.setOnClickListener(categoryClickListener);
        btnLearning.setOnClickListener(categoryClickListener);
        btnTesting.setOnClickListener(categoryClickListener);
        btnQuickCall.setOnClickListener(categoryClickListener);
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

    private void resetCategoryButtons() {
        int unselectedBackground = R.drawable.category_chip_unselected;
        btnDesign.setBackgroundResource(unselectedBackground);
        btnDesign.setTextColor(getResources().getColor(android.R.color.black));
        btnMeeting.setBackgroundResource(unselectedBackground);
        btnMeeting.setTextColor(getResources().getColor(android.R.color.black));
        btnCoding.setBackgroundResource(unselectedBackground);
        btnCoding.setTextColor(getResources().getColor(android.R.color.black));
        btnLearning.setBackgroundResource(unselectedBackground);
        btnLearning.setTextColor(getResources().getColor(android.R.color.black));
        btnTesting.setBackgroundResource(unselectedBackground);
        btnTesting.setTextColor(getResources().getColor(android.R.color.black));
        btnQuickCall.setBackgroundResource(unselectedBackground);
        btnQuickCall.setTextColor(getResources().getColor(android.R.color.black));
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

            if (name.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || desc.isEmpty() || selectedCategory.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields and select a category", Toast.LENGTH_SHORT).show();
            } else {
                if (listener != null) {
                    listener.onTaskCreated(name, desc, date, startTime, endTime, selectedCategory, selectedStatus);
                }
                dismiss();
            }
        });
    }

    private void populateExistingData() {
        String title = getArguments().getString("title", "");
        String date = getArguments().getString("date", "");
        String category = getArguments().getString("category", "");
        String status = getArguments().getString("status", "running");

        edtTaskName.setText(title);
        edtDate.setText(date);

        if (!category.isEmpty()) {
            selectedCategory = category;
            highlightCategoryButton(category);
        }

        if (!status.isEmpty()) {
            selectedStatus = status;
            highlightStatusButton(status);
        }
    }

    private void highlightCategoryButton(String category) {
        resetCategoryButtons();
        switch (category) {
            case "Design":
                btnDesign.setBackgroundResource(R.drawable.category_chip_selected);
                btnDesign.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "Meeting":
                btnMeeting.setBackgroundResource(R.drawable.category_chip_selected);
                btnMeeting.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "Coding":
                btnCoding.setBackgroundResource(R.drawable.category_chip_selected);
                btnCoding.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "Learning":
                btnLearning.setBackgroundResource(R.drawable.category_chip_selected);
                btnLearning.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "Testing":
                btnTesting.setBackgroundResource(R.drawable.category_chip_selected);
                btnTesting.setTextColor(getResources().getColor(android.R.color.white));
                break;
            case "Quick Call":
                btnQuickCall.setBackgroundResource(R.drawable.category_chip_selected);
                btnQuickCall.setTextColor(getResources().getColor(android.R.color.white));
                break;
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