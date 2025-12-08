package com.rakib.to_do_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        dbHelper = new DatabaseHelper(this);
        boolean isDarkMode = dbHelper.isDarkModeEnabled();
        int mode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;

        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
                else if (itemId == R.id.nav_clock) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PomodoroFragment()).commit();
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
    }
}