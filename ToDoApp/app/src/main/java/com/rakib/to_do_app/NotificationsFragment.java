package com.rakib.to_do_app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private static final String TAG = "NotificationsFragment";

    private SwitchMaterial switchTaskReminders, switchDueDateAlerts, switchDailySummary;
    private Spinner spinnerReminderTime, spinnerNotificationSound;
    private MaterialToolbar toolbar;
    private SwitchMaterial switchDarkTheme;

    private NotificationAdapter notificationAdapter;
    private List<NotificationItem> notificationList;

    private List<String> soundOptionsList;
    private ArrayAdapter<String> soundAdapter;

    private DatabaseHelper dbHelper;
    private ActivityResultLauncher<Intent> ringtonePickerLauncher;

    private boolean isUserTouched = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_notifications, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        initializeViews(view);
        setupRingtonePickerLauncher();

        initSpinnerData();

        loadNotificationSettings();

        setupSpinnerListeners();
        setupSwitches();

        notificationList = new ArrayList<>();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isUserTouched = false;
    }

    private void initializeViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        switchTaskReminders = view.findViewById(R.id.switch_task_reminders);
        switchDueDateAlerts = view.findViewById(R.id.switch_due_date_alerts);
        switchDailySummary = view.findViewById(R.id.switch_daily_summary);
        switchDarkTheme = view.findViewById(R.id.switch_dark_theme);
        spinnerReminderTime = view.findViewById(R.id.spinner_reminder_time);
        spinnerNotificationSound = view.findViewById(R.id.spinner_notification_sound);
    }

    private void initSpinnerData() {
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.reminder_time_options, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminderTime.setAdapter(timeAdapter);

        String[] defaults = getResources().getStringArray(R.array.notification_sound_options);
        soundOptionsList = new ArrayList<>(Arrays.asList(defaults));

        soundAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, soundOptionsList);
        soundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNotificationSound.setAdapter(soundAdapter);
    }

    private void setupRingtonePickerLauncher() {
        ringtonePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                        if (uri != null) {
                            String uriString = uri.toString();

                            Ringtone ringtone = RingtoneManager.getRingtone(requireContext(), uri);
                            String ringtoneTitle = ringtone.getTitle(requireContext());

                            if (!soundOptionsList.contains(ringtoneTitle)) {
                                soundOptionsList.add(0, ringtoneTitle);
                                soundAdapter.notifyDataSetChanged();
                            }

                            dbHelper.saveCustomRingtoneUri(uriString);
                            saveNotificationSetting("notificationSound", ringtoneTitle);

                            setSpinnerSelection(spinnerNotificationSound, ringtoneTitle);

                            testNotificationSound(uriString);

                            Toast.makeText(requireContext(), ringtoneTitle + " set!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        loadNotificationSettings();
                    }
                    isUserTouched = false;
                }
        );
    }

    private void setupSpinnerListeners() {
        View.OnTouchListener touchListener = (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                isUserTouched = true;
            }
            return false;
        };
        spinnerReminderTime.setOnTouchListener(touchListener);
        spinnerNotificationSound.setOnTouchListener(touchListener);

        spinnerReminderTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isUserTouched) return;
                String selectedTime = parent.getItemAtPosition(position).toString();
                saveNotificationSetting("defaultReminderTime", selectedTime);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerNotificationSound.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isUserTouched) return;

                String selectedSound = parent.getItemAtPosition(position).toString();

                if (selectedSound.equals("Custom...") || selectedSound.equals("Add New Sound")) {
                    launchRingtonePicker();
                } else {
                    saveNotificationSetting("notificationSound", selectedSound);

                    NotificationSettings settings = dbHelper.getNotificationSettings();
                    String customUri = settings.getCustomRingtoneUri();

                    if (customUri != null && !customUri.isEmpty() && selectedSound.equals(getSavedRingtoneTitle(Uri.parse(customUri)))) {
                        testNotificationSound(customUri);
                    } else {
                        testNotificationSound(selectedSound);
                    }
                }

                isUserTouched = false;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private String getSavedRingtoneTitle(Uri uri) {
        try {
            Ringtone r = RingtoneManager.getRingtone(requireContext(), uri);
            return r.getTitle(requireContext());
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private void launchRingtonePicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Sound");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);

        NotificationSettings settings = dbHelper.getNotificationSettings();
        String customUri = settings.getCustomRingtoneUri();
        if (customUri != null && !customUri.isEmpty()) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(customUri));
        }

        ringtonePickerLauncher.launch(intent);
    }

    private void loadNotificationSettings() {
        NotificationSettings settings = dbHelper.getNotificationSettings();

        switchTaskReminders.setChecked(settings.isTaskRemindersEnabled());
        switchDueDateAlerts.setChecked(settings.isDueDateAlertsEnabled());
        switchDailySummary.setChecked(settings.isDailySummaryEnabled());

        switchDarkTheme.setOnCheckedChangeListener(null);
        switchDarkTheme.setChecked(settings.isDarkModeEnabled());

        setSpinnerSelection(spinnerReminderTime, settings.getDefaultReminderTime());

        String savedSoundName = settings.getNotificationSound();

        if (savedSoundName != null && !savedSoundName.isEmpty() && !soundOptionsList.contains(savedSoundName)) {
            soundOptionsList.add(0, savedSoundName);
            soundAdapter.notifyDataSetChanged();
        }

        setSpinnerSelection(spinnerNotificationSound, savedSoundName);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = adapter.getItem(i).toString();
            if (item.equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                return;
            }
        }
        if (adapter.getCount() > 0) spinner.setSelection(0);
    }

    private void setupSwitches() {
        switchTaskReminders.setOnCheckedChangeListener((v, c) -> saveNotificationSetting("taskRemindersEnabled", c));
        switchDueDateAlerts.setOnCheckedChangeListener((v, c) -> saveNotificationSetting("dueDateAlertsEnabled", c));
        switchDailySummary.setOnCheckedChangeListener((v, c) -> saveNotificationSetting("dailySummaryEnabled", c));

        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return;

            dbHelper.setDarkModeEnabled(isChecked);
            saveNotificationSetting("dark_mode_enabled", isChecked);
            int mode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            AppCompatDelegate.setDefaultNightMode(mode);

            buttonView.postDelayed(() -> {
                if (getActivity() != null) getActivity().recreate();
            }, 100);
        });
    }

    private void testNotificationSound(String soundNameOrUri) {
        if (isVisible() && !isRemoving()) {
            NotificationHelper.playTestSound(requireContext(), soundNameOrUri);
        }
    }

    private void saveNotificationSetting(String key, Object value) {
        String dbKey = key;
        if (key.equals("defaultReminderTime")) dbKey = "default_reminder_time";
        else if (key.equals("notificationSound")) dbKey = "notification_sound";
        else if (key.equals("taskRemindersEnabled")) dbKey = "task_reminders_enabled";
        else if (key.equals("dueDateAlertsEnabled")) dbKey = "due_date_alerts_enabled";
        else if (key.equals("dailySummaryEnabled")) dbKey = "daily_summary_enabled";
        else if (key.equals("dark_mode_enabled")) dbKey = "dark_mode_enabled";

        dbHelper.updateNotificationSetting(dbKey, value);
    }
}