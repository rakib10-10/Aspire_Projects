package com.rakib.to_do_app;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private static final String TAG = "NotificationsFragment";

    // UI Components
    private SwitchMaterial switchTaskReminders, switchDueDateAlerts, switchDailySummary;
    private Spinner spinnerReminderTime, spinnerNotificationSound;
    private RecyclerView recyclerViewNotifications;
    private LinearLayout emptyStateLayout;
    private MaterialToolbar toolbar;
    private SwitchMaterial switchDarkTheme;

    // Adapter
    private NotificationAdapter notificationAdapter;
    private List<NotificationItem> notificationList;


    private DatabaseHelper dbHelper;


    private ActivityResultLauncher<Intent> ringtonePickerLauncher;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.activity_notifications, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        initializeViews(view);
        setupRingtonePickerLauncher(); // launcher set up
        setupSpinners();
        setupSwitches();
        setupRecyclerView();

        loadNotificationSettings();
        loadNotifications();

        return view;
    }

    private void initializeViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        switchTaskReminders = view.findViewById(R.id.switch_task_reminders);
        switchDueDateAlerts = view.findViewById(R.id.switch_due_date_alerts);
        switchDailySummary = view.findViewById(R.id.switch_daily_summary);

        // Initializing the Dark Theme Switch
        switchDarkTheme = view.findViewById(R.id.switch_dark_theme);

        spinnerReminderTime = view.findViewById(R.id.spinner_reminder_time);
        spinnerNotificationSound = view.findViewById(R.id.spinner_notification_sound);

        recyclerViewNotifications = view.findViewById(R.id.recycler_view_notifications);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);

        notificationList = new ArrayList<>();

        Log.d(TAG, "All views initialized successfully");
    }

    // Implementing the Ringtone Picker Launcher setup
    private void setupRingtonePickerLauncher() {
        ringtonePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                        if (uri != null) {
                            // Storing the URI as a string
                            String uriString = uri.toString();
                            saveNotificationSetting("notificationSound", uriString);

                            setSpinnerSelection(spinnerNotificationSound, uriString);

                            Toast.makeText(requireContext(), "Custom sound set!", Toast.LENGTH_SHORT).show();
                        }
                    } else {

                        loadNotificationSettings();
                    }
                }
        );
    }


    private void setupSpinners() {
        // Reminder time options
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.reminder_time_options,
                android.R.layout.simple_spinner_item
        );
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminderTime.setAdapter(timeAdapter);

        // Notification sound options
        ArrayAdapter<CharSequence> soundAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.notification_sound_options,
                android.R.layout.simple_spinner_item
        );
        soundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNotificationSound.setAdapter(soundAdapter);

        // Set listeners for spinners
        spinnerReminderTime.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedTime = parent.getItemAtPosition(position).toString();
                saveNotificationSetting("defaultReminderTime", selectedTime);
                Log.d(TAG, "Reminder time selected: " + selectedTime);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        spinnerNotificationSound.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedSound = parent.getItemAtPosition(position).toString();

                if (selectedSound.equals("Custom...")) {
                    // Launch the Ringtone Picker using the launcher
                    Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Notification Sound");
                    ringtonePickerLauncher.launch(intent);

                } else {
                    // Save and test built-in sound
                    saveNotificationSetting("notificationSound", selectedSound);
                    Log.d(TAG, "Notification sound selected: " + selectedSound);
                    testNotificationSound(selectedSound);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void testNotificationSound(String soundName) {
        NotificationHelper.playTestSound(requireContext(), soundName);
    }

    private void setupSwitches() {
        // Task Reminders switch
        switchTaskReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationSetting("taskRemindersEnabled", isChecked);
            Log.d(TAG, "Task reminders: " + (isChecked ? "enabled" : "disabled"));
        });

        // Due Date Alerts switch
        switchDueDateAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationSetting("dueDateAlertsEnabled", isChecked);
            Log.d(TAG, "Due date alerts: " + (isChecked ? "enabled" : "disabled"));
        });

        // Daily Summary switch
        switchDailySummary.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationSetting("dailySummaryEnabled", isChecked);
            Log.d(TAG, "Daily summary: " + (isChecked ? "enabled" : "disabled"));
        });


        if (switchDarkTheme != null) {
            switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Toast.makeText(requireContext(), "Dark Mode is " + (isChecked ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupRecyclerView() {
        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewNotifications.setAdapter(notificationAdapter);
    }

    private void loadNotificationSettings() {
        NotificationSettings settings = dbHelper.getNotificationSettings();

        // Update UI with settings
        switchTaskReminders.setChecked(settings.isTaskRemindersEnabled());
        switchDueDateAlerts.setChecked(settings.isDueDateAlertsEnabled());
        switchDailySummary.setChecked(settings.isDailySummaryEnabled());



        setSpinnerSelection(spinnerReminderTime, settings.getDefaultReminderTime());
        setSpinnerSelection(spinnerNotificationSound, settings.getNotificationSound());

        Log.d(TAG, "Notification settings loaded successfully");
    }

    //spinner option

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner.getAdapter() != null && value != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                String item = adapter.getItem(i).toString();

                // If value is a standard name OR a custom URI, match it directly
                if (item.equalsIgnoreCase(value) || value.startsWith("content://")) {
                    if (value.startsWith("content://")) {
                        // Check if "Custom" is an option before setting it
                        if (item.equals("Custom...")) {
                            spinner.setSelection(i);
                            return;
                        } else {
                            continue;
                        }
                    }

                    if (item.equalsIgnoreCase(value)) {
                        spinner.setSelection(i);
                        return;
                    }
                }
            }
        }
    }

    private void saveNotificationSetting(String key, Object value) {
        // Mapping the old key names to new database column names
        String databaseKey;
        switch (key) {
            case "defaultReminderTime":
                databaseKey = "default_reminder_time";
                break;
            case "notificationSound":
                databaseKey = "notification_sound";
                break;
            case "taskRemindersEnabled":
                databaseKey = "task_reminders_enabled";
                break;
            case "dueDateAlertsEnabled":
                databaseKey = "due_date_alerts_enabled";
                break;
            case "dailySummaryEnabled":
                databaseKey = "daily_summary_enabled";
                break;
            default:
                databaseKey = key;
        }
        dbHelper.updateNotificationSetting(databaseKey, value);
        Log.d(TAG, "Notification setting saved to SQLite: " + key + " = " + value);
    }

    private void loadNotifications() {
        createSampleNotifications();
    }

    private void createSampleNotifications() {
        notificationList.clear();

        notificationList.add(new NotificationItem(
                "Task Reminder",
                "Complete 'Buy groceries' task",
                System.currentTimeMillis() - 3600000,
                "task"
        ));

        notificationList.add(new NotificationItem(
                "Due Date Alert",
                "'Project deadline' is due tomorrow",
                System.currentTimeMillis() - 86400000,
                "due_date"
        ));

        notificationList.add(new NotificationItem(
                "Daily Summary",
                "You have 3 tasks due today",
                System.currentTimeMillis() - 172800000,
                "summary"
        ));

        updateNotificationUI();
    }

    private void updateNotificationUI() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                if (notificationList.isEmpty()) {
                    recyclerViewNotifications.setVisibility(View.GONE);
                    emptyStateLayout.setVisibility(View.VISIBLE);
                } else {
                    recyclerViewNotifications.setVisibility(View.VISIBLE);
                    emptyStateLayout.setVisibility(View.GONE);
                    notificationAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}