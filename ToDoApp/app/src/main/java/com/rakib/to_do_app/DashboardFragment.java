package com.rakib.to_do_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView; // Use CardView from support lib or MaterialCardView

import com.google.android.material.card.MaterialCardView;

public class DashboardFragment extends Fragment {

    private MaterialCardView cardPomodoro, cardNotes, cardAddTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Use the layout file you created: layout_dashboard_cards.xml
        // Note: Ensure the root element in your XML has an android:background="?android:attr/colorBackground"
        // or wrap it in a ScrollView/ConstraintLayout if it's not filling the screen correctly.
        View view = inflater.inflate(R.layout.layout_dashboard_cards, container, false);

        initializeViews(view);
        setupClickListeners();

        return view;
    }

    private void initializeViews(View view) {
        cardPomodoro = view.findViewById(R.id.cardPomodoro);
        cardNotes = view.findViewById(R.id.cardNotes);
        cardAddTask = view.findViewById(R.id.cardAddTask);
    }

    private void setupClickListeners() {
        // 1. Open Pomodoro Timer
        cardPomodoro.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PomodoroFragment())
                    .addToBackStack(null) // Allows user to press back to return to dashboard
                    .commit();
        });

        // 2. Open Notes
        cardNotes.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NotesFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // 3. Open Add Task Dialog
        cardAddTask.setOnClickListener(v -> {
            AddTaskDialog dialog = new AddTaskDialog();

            // Optional: Refresh home list if needed, though this dialog is independent
            dialog.setOnTaskCreatedListener((title, desc, date, start, end, cat, status, priority) -> {
                // If you want to show a confirmation or refresh data globally
            });

            dialog.show(getParentFragmentManager(), "AddTaskDialog");
        });
    }
}