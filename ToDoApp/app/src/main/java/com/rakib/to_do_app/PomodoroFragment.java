package com.rakib.to_do_app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.slider.Slider;

import java.util.Locale;

public class PomodoroFragment extends Fragment implements PomodoroService.TimerCallback {

    private static final String TAG = "PomodoroFragment";

    private ProgressBar progressBarTimer;
    private TextView tvTimer, tvStatus;
    private Slider timeSlider;
    private ExtendedFloatingActionButton btnPlayPause, btnReset;

    private PomodoroService pomodoroService;
    private boolean isServiceBound = false;
    private long startTimeInMillis = 25 * 60 * 1000;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PomodoroService.LocalBinder binder = (PomodoroService.LocalBinder) service;
            pomodoroService = binder.getService();
            pomodoroService.setCallback(PomodoroFragment.this);
            isServiceBound = true;

            updateUIFromService();
            Log.d(TAG, "Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            pomodoroService = null;
            Log.d(TAG, "Service disconnected");
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");

        Intent serviceIntent = new Intent(requireContext(), PomodoroService.class);
        requireActivity().startService(serviceIntent);
        requireActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pomodoro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        progressBarTimer.setMax(10000);

        // Set slider to default 25 or whatever was last set
        timeSlider.setValue((float) (startTimeInMillis / (60 * 1000)));

        setupListeners();

        if (isServiceBound && pomodoroService != null) {
            updateUIFromService();
        }
    }

    private void initializeViews(View view) {
        progressBarTimer = view.findViewById(R.id.progressBarTimer);
        tvTimer = view.findViewById(R.id.tvTimer);
        tvStatus = view.findViewById(R.id.tvStatus);
        timeSlider = view.findViewById(R.id.timeSlider);
        btnPlayPause = view.findViewById(R.id.btnPlayPause);
        btnReset = view.findViewById(R.id.btnReset);
    }

    private void setupListeners() {
        timeSlider.addOnChangeListener((slider, value, fromUser) -> {
            // Only update local time if timer is NOT running
            if (!isServiceBound || pomodoroService == null || !pomodoroService.isTimerRunning()) {
                startTimeInMillis = (long) value * 60 * 1000;
                updateTimerDisplay(startTimeInMillis);
                tvStatus.setText("Ready to Focus?");
                btnPlayPause.setText("Start Focus");
            }
        });

        btnPlayPause.setOnClickListener(v -> {
            if (!isServiceBound || pomodoroService == null) return;

            if (pomodoroService.isTimerRunning()) {
                // PAUSE LOGIC
                pomodoroService.pauseTimer();
                btnPlayPause.setText("Resume");
                btnPlayPause.setIconResource(R.drawable.outline_autoplay_24);
                tvStatus.setText("Paused");
                timeSlider.setEnabled(true);
            } else {
                // START / RESUME LOGIC

                // FIX: Check if time is 0 OR if user changed the slider (startTime mismatch)
                boolean timeIsFinished = pomodoroService.getTimeLeftInMillis() <= 0;
                boolean sliderChanged = pomodoroService.getStartTimeInMillis() != startTimeInMillis;

                if (timeIsFinished || sliderChanged) {
                    // Start FRESH timer with new slider value
                    pomodoroService.startTimer(startTimeInMillis);
                } else {
                    // Resume existing timer
                    pomodoroService.resumeTimer();
                }

                btnPlayPause.setText("Pause");
                btnPlayPause.setIconResource(R.drawable.outline_alarm_pause_24);
                tvStatus.setText("Focusing...");
                timeSlider.setEnabled(false);
            }
        });

        btnReset.setOnClickListener(v -> {
            if (!isServiceBound || pomodoroService == null) return;

            pomodoroService.resetTimer();

            // Reset local variable to slider value
            startTimeInMillis = (long) timeSlider.getValue() * 60 * 1000;

            btnPlayPause.setText("Start Focus");
            btnPlayPause.setIconResource(R.drawable.outline_autoplay_24);
            tvStatus.setText("Ready to Focus?");
            timeSlider.setEnabled(true);
            updateTimerDisplay(startTimeInMillis);
        });
    }

    private void updateUIFromService() {
        if (pomodoroService == null) return;

        long timeLeft = pomodoroService.getTimeLeftInMillis();
        boolean isRunning = pomodoroService.isTimerRunning();

        // Sync local start time with service start time if running
        if (isRunning || timeLeft > 0) {
            startTimeInMillis = pomodoroService.getStartTimeInMillis();
            timeSlider.setValue((float) (startTimeInMillis / (60 * 1000)));
        }

        updateTimerDisplay(timeLeft);

        if (isRunning) {
            btnPlayPause.setText("Pause");
            btnPlayPause.setIconResource(R.drawable.outline_alarm_pause_24);
            tvStatus.setText("Focusing...");
            timeSlider.setEnabled(false);
        } else {
            btnPlayPause.setText(timeLeft > 0 && timeLeft < startTimeInMillis ? "Resume" : "Start Focus");
            btnPlayPause.setIconResource(R.drawable.outline_autoplay_24);
            tvStatus.setText(timeLeft > 0 && timeLeft < startTimeInMillis ? "Paused" : "Ready to Focus?");
            timeSlider.setEnabled(true);
        }
    }

    private void updateTimerDisplay(long millis) {
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        tvTimer.setText(timeFormatted);

        if (startTimeInMillis > 0) {
            double progress = (double) millis / startTimeInMillis;
            progressBarTimer.setProgress((int) (progress * 10000));
        } else {
            progressBarTimer.setProgress(0);
        }
    }

    @Override
    public void onTimerTick(long millisUntilFinished) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                updateTimerDisplay(millisUntilFinished);
            });
        }
    }

    @Override
    public void onTimerFinish() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                tvTimer.setText("00:00");
                tvStatus.setText("Session Complete!");
                btnPlayPause.setText("Start Focus");
                btnPlayPause.setIconResource(R.drawable.outline_autoplay_24);
                progressBarTimer.setProgress(0);
                timeSlider.setEnabled(true);

                // Reset start time to slider value for next run
                startTimeInMillis = (long) timeSlider.getValue() * 60 * 1000;

                Toast.makeText(requireContext(), "Pomodoro session finished!", Toast.LENGTH_LONG).show();
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            requireActivity().unbindService(serviceConnection);
            isServiceBound = false;
        }
    }
}