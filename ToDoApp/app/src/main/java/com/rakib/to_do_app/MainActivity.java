package com.rakib.to_do_app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    private DatabaseHelper dbHelper;

    // --- PERMISSION LAUNCHER ---
    // Registers a callback to handle the user's response to the system permission dialog
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("Permissions", "Notification permission granted");
                    // Once notifications are granted, check for exact alarms
                    checkExactAlarmPermission();
                } else {
                    Log.w("Permissions", "Notification permission denied");
                    Toast.makeText(this, "Notifications are needed for task reminders", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Check Session BEFORE loading layout
        SessionManager session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return; // Stop loading MainActivity
        }

        // 2. Handle Dark Mode Logic
        dbHelper = new DatabaseHelper(this);
        boolean isDarkMode = dbHelper.isDarkModeEnabled();
        int mode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;

        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 3. Setup Navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                    return true;
                } else if (itemId == R.id.nav_calendar) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CalendarFragment()).commit();
                    return true;
                }
                else if (itemId == R.id.nav_add) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
                    return true;
                }
                else if (itemId == R.id.nav_notifications) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NotificationsFragment()).commit();
                    return true;
                }
                else if (itemId == R.id.nav_profile) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                    return true;
                }

                return false;
            }
        });

        // 4. CHECK PERMISSIONS ON STARTUP
        checkAndRequestPermissions();
    }

    // --- HELPER METHODS FOR PERMISSIONS ---

    private void checkAndRequestPermissions() {
        // Check for Notification Permission (Android 13 / SDK 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return; // Stop here, we will check Alarm permission in the callback
            }
        }

        // Check for Exact Alarm Permission (Android 12 / SDK 31+)
        checkExactAlarmPermission();
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // Show a dialog explaining why we need this
                new AlertDialog.Builder(this)
                        .setTitle("Permission Needed")
                        .setMessage("To remind you at the exact time you set, this app needs permission to schedule exact alarms.")
                        .setPositiveButton("Grant", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }
}