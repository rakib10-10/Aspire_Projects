package com.rakib.to_do_app;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Enable persistence (optional)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}