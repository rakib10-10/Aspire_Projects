package com.rakib.to_do_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set HomeFragment as default when app starts
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_home); // Highlight home in navigation
        }

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    // Open HomeFragment
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();
                    return true;
                } else if (itemId == R.id.nav_calendar) {
                    // Open CalendarFragment
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new CalendarFragment())
                            .commit();
                    return true;
                }
                else if (itemId == R.id.nav_notifications) {
                    // Open NotificationsFragment
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new NotificationsFragment())
                            .commit();
                    return true;
                }
                else if (itemId == R.id.nav_profile) {
                    // Open ProfileFragment
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment())
                            .commit();
                    return true;
                }

                return false;
            }
        });
    }
}