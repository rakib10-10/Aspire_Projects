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
import androidx.appcompat.app.AppCompatDelegate;
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

    // Database
    private DatabaseHelper dbHelper;

    // FIX: Activity Result Launcher for Ringtone Picker
    private ActivityResultLauncher<Intent> ringtonePickerLauncher;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_notifications, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        initializeViews(view);
        setupRingtonePickerLauncher();
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

        // Initialize Dark Theme Switch
        switchDarkTheme = view.findViewById(R.id.switch_dark_theme);

        spinnerReminderTime = view.findViewById(R.id.spinner_reminder_time);
        spinnerNotificationSound = view.findViewById(R.id.spinner_notification_sound);

        recyclerViewNotifications = view.findViewById(R.id.recycler_view_notifications);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);

        notificationList = new ArrayList<>();

        Log.d(TAG, "All views initialized successfully");
    }

    // FIX: Implement the Ringtone Picker Launcher setup
    private void setupRingtonePickerLauncher() {
        ringtonePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                        if (uri != null) {
                            // FIX: Store the URI as a string
                            String uriString = uri.toString();
                            saveNotificationSetting("notificationSound", uriString);

                            // FIX: Update the display name in the Spinner (e.g., switch to a custom name)
                            setSpinnerSelection(spinnerNotificationSound, uriString);

                            Toast.makeText(requireContext(), "Custom sound set!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If picker was cancelled, revert the spinner to the previously saved setting
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
                    // FIX: Launch the Ringtone Picker using the launcher
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

        // DARK THEME SWITCH - FIXED VERSION
        if (switchDarkTheme != null) {
            // First, set the switch state based on saved preference from database
            boolean isDarkModeEnabled = dbHelper.isDarkModeEnabled();
            switchDarkTheme.setChecked(isDarkModeEnabled);

            // Remove any existing listeners to avoid triggering on setChecked
            switchDarkTheme.setOnCheckedChangeListener(null);

            // Set the new listener
            switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Save to database using the direct method
                dbHelper.setDarkModeEnabled(isChecked);

                // Also update via the notification setting method (for consistency)
                saveNotificationSetting("dark_mode_enabled", isChecked);

                // Apply theme change
                int mode = isChecked ?
                        AppCompatDelegate.MODE_NIGHT_YES :
                        AppCompatDelegate.MODE_NIGHT_NO;

                AppCompatDelegate.setDefaultNightMode(mode);

                // Restart activity to apply theme immediately
                requireActivity().recreate();

                Toast.makeText(requireContext(),
                        "Dark Mode " + (isChecked ? "enabled" : "disabled"),
                        Toast.LENGTH_SHORT).show();

                Log.d(TAG, "Dark mode preference saved to DB: " + isChecked);
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

        // Load Dark Mode preference from database
        if (switchDarkTheme != null) {
            // IMPORTANT: Remove listener before setting checked state
            switchDarkTheme.setOnCheckedChangeListener(null);

            boolean isDarkModeEnabled = settings.isDarkModeEnabled();
            switchDarkTheme.setChecked(isDarkModeEnabled);
            Log.d(TAG, "Dark mode loaded from DB: " + isDarkModeEnabled);

            // Re-attach the listener after setting the initial state
            switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                dbHelper.setDarkModeEnabled(isChecked);
                saveNotificationSetting("dark_mode_enabled", isChecked);

                int mode = isChecked ?
                        AppCompatDelegate.MODE_NIGHT_YES :
                        AppCompatDelegate.MODE_NIGHT_NO;
                AppCompatDelegate.setDefaultNightMode(mode);
                requireActivity().recreate();

                Toast.makeText(requireContext(),
                        "Dark Mode " + (isChecked ? "enabled" : "disabled"),
                        Toast.LENGTH_SHORT).show();

                Log.d(TAG, "Dark mode preference saved to DB: " + isChecked);
            });
        }

        setSpinnerSelection(spinnerReminderTime, settings.getDefaultReminderTime());
        setSpinnerSelection(spinnerNotificationSound, settings.getNotificationSound());

        Log.d(TAG, "Notification settings loaded successfully");
    }

    /**
     * FIX: Updated setSpinnerSelection to handle custom URI strings by matching them directly.
     */
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner.getAdapter() != null && value != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                String item = adapter.getItem(i).toString();

                // If value is a standard name OR a custom URI, match it directly
                if (item.equalsIgnoreCase(value) || value.startsWith("content://")) {
                    // For custom URIs, we don't have the original name, so just mark it as selected
                    // by choosing the last item (assuming "Custom..." is the last one if you updated arrays.xml).
                    if (value.startsWith("content://")) {
                        // Check if "Custom..." is an option before setting it
                        if (item.equals("Custom...")) {
                            spinner.setSelection(i);
                            return;
                        } else {
                            continue; // Keep looking for "Custom..." or standard items
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
        // Map the old key names to new database column names
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
            case "dark_mode_enabled": // ADDED: Dark mode key mapping
                databaseKey = "dark_mode_enabled";
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