package com.rakib.to_do_app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

    // Adapter
    private NotificationAdapter notificationAdapter;
    private List<NotificationItem> notificationList;

    // Database
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_notifications, container, false);

        // Initialize Database
        dbHelper = new DatabaseHelper(requireContext());

        // Initialize views
        initializeViews(view);

        // Setup spinners
        setupSpinners();

        // Setup switches
        setupSwitches();

        // Setup recycler view
        setupRecyclerView();

        // Load notification settings and data
        loadNotificationSettings();
        loadNotifications();

        return view;
    }

    private void initializeViews(View view) {
        // Initialize toolbar
        toolbar = view.findViewById(R.id.toolbar);

        // Initialize switches
        switchTaskReminders = view.findViewById(R.id.switch_task_reminders);
        switchDueDateAlerts = view.findViewById(R.id.switch_due_date_alerts);
        switchDailySummary = view.findViewById(R.id.switch_daily_summary);

        // Initialize spinners
        spinnerReminderTime = view.findViewById(R.id.spinner_reminder_time);
        spinnerNotificationSound = view.findViewById(R.id.spinner_notification_sound);

        // Initialize recycler view and empty state
        recyclerViewNotifications = view.findViewById(R.id.recycler_view_notifications);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);

        // Initialize notification list
        notificationList = new ArrayList<>();

        Log.d(TAG, "All views initialized successfully");
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
                saveNotificationSetting("notificationSound", selectedSound);
                Log.d(TAG, "Notification sound selected: " + selectedSound);

                // Test the selected sound
                testNotificationSound(selectedSound);
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
        switchTaskReminders.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveNotificationSetting("taskRemindersEnabled", isChecked);
                Log.d(TAG, "Task reminders: " + (isChecked ? "enabled" : "disabled"));
            }
        });

        // Due Date Alerts switch
        switchDueDateAlerts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveNotificationSetting("dueDateAlertsEnabled", isChecked);
                Log.d(TAG, "Due date alerts: " + (isChecked ? "enabled" : "disabled"));
            }
        });

        // Daily Summary switch
        switchDailySummary.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveNotificationSetting("dailySummaryEnabled", isChecked);
                Log.d(TAG, "Daily summary: " + (isChecked ? "enabled" : "disabled"));
            }
        });
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

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner.getAdapter() != null && value != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveNotificationSetting(String key, Object value) {
        dbHelper.updateNotificationSetting(key, value);
        Log.d(TAG, "Notification setting saved to SQLite: " + key + " = " + value);
    }

    private void loadNotifications() {
        // For now, we'll create some sample notifications
        createSampleNotifications();
    }

    private void createSampleNotifications() {
        notificationList.clear();

        // Add sample notifications
        notificationList.add(new NotificationItem(
                "Task Reminder",
                "Complete 'Buy groceries' task",
                System.currentTimeMillis() - 3600000, // 1 hour ago
                "task"
        ));

        notificationList.add(new NotificationItem(
                "Due Date Alert",
                "'Project deadline' is due tomorrow",
                System.currentTimeMillis() - 86400000, // 1 day ago
                "due_date"
        ));

        notificationList.add(new NotificationItem(
                "Daily Summary",
                "You have 3 tasks due today",
                System.currentTimeMillis() - 172800000, // 2 days ago
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