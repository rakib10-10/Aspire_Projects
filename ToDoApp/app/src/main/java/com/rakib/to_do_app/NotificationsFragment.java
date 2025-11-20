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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    // Firebase
    private DatabaseReference mDatabase;
    private String userId = "default_user";

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_notifications, container, false);

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

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

        // Remove the back button functionality since it's a fragment
        // You can keep the toolbar for consistency with your design

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
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
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
        mDatabase.child("users").child(userId).child("notificationSettings")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Load task reminders setting
                            if (dataSnapshot.child("taskRemindersEnabled").exists()) {
                                Boolean taskReminders = dataSnapshot.child("taskRemindersEnabled").getValue(Boolean.class);
                                if (taskReminders != null) {
                                    switchTaskReminders.setChecked(taskReminders);
                                }
                            }

                            // Load due date alerts setting
                            if (dataSnapshot.child("dueDateAlertsEnabled").exists()) {
                                Boolean dueDateAlerts = dataSnapshot.child("dueDateAlertsEnabled").getValue(Boolean.class);
                                if (dueDateAlerts != null) {
                                    switchDueDateAlerts.setChecked(dueDateAlerts);
                                }
                            }

                            // Load daily summary setting
                            if (dataSnapshot.child("dailySummaryEnabled").exists()) {
                                Boolean dailySummary = dataSnapshot.child("dailySummaryEnabled").getValue(Boolean.class);
                                if (dailySummary != null) {
                                    switchDailySummary.setChecked(dailySummary);
                                }
                            }

                            // Load reminder time setting
                            if (dataSnapshot.child("defaultReminderTime").exists()) {
                                String reminderTime = dataSnapshot.child("defaultReminderTime").getValue(String.class);
                                if (reminderTime != null) {
                                    setSpinnerSelection(spinnerReminderTime, reminderTime);
                                }
                            }

                            // Load notification sound setting
                            if (dataSnapshot.child("notificationSound").exists()) {
                                String notificationSound = dataSnapshot.child("notificationSound").getValue(String.class);
                                if (notificationSound != null) {
                                    setSpinnerSelection(spinnerNotificationSound, notificationSound);
                                }
                            }

                            Log.d(TAG, "Notification settings loaded successfully");
                        } else {
                            // Create default settings if they don't exist
                            createDefaultNotificationSettings();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Failed to load notification settings: " + databaseError.getMessage());
                    }
                });
    }

    private void createDefaultNotificationSettings() {
        NotificationSettings defaultSettings = new NotificationSettings(
                true,  // taskRemindersEnabled
                true,  // dueDateAlertsEnabled
                false, // dailySummaryEnabled
                "15 minutes before", // defaultReminderTime
                "Default" // notificationSound
        );

        mDatabase.child("users").child(userId).child("notificationSettings")
                .setValue(defaultSettings)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Default notification settings created");
                    // Set UI to default values
                    if (isAdded()) { // Check if fragment is attached to activity
                        requireActivity().runOnUiThread(() -> {
                            switchTaskReminders.setChecked(true);
                            switchDueDateAlerts.setChecked(true);
                            switchDailySummary.setChecked(false);
                            setSpinnerSelection(spinnerReminderTime, "15 minutes before");
                            setSpinnerSelection(spinnerNotificationSound, "Default");
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create default notification settings: " + e.getMessage());
                });
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (spinner.getAdapter() != null) {
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
        mDatabase.child("users").child(userId).child("notificationSettings").child(key)
                .setValue(value)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification setting saved: " + key + " = " + value);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save notification setting: " + e.getMessage());
                });
    }

    private void loadNotifications() {
        // For now, we'll create some sample notifications
        // In a real app, you would load these from Firebase
        createSampleNotifications();

        // Uncomment below to load from Firebase when you have the structure ready
        /*
        mDatabase.child("users").child(userId).child("notifications")
                .orderByChild("timestamp")
                .limitToLast(20)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        notificationList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            NotificationItem notification = snapshot.getValue(NotificationItem.class);
                            if (notification != null) {
                                notificationList.add(0, notification); // Add to beginning for reverse chronological order
                            }
                        }
                        updateNotificationUI();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Failed to load notifications: " + databaseError.getMessage());
                    }
                });
        */
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
        if (isAdded()) { // Check if fragment is attached to activity
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