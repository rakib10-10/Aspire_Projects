package com.rakib.to_do_app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Locale;

public class PomodoroService extends Service {
    private static final String TAG = "PomodoroService";
    private static final String CHANNEL_ID = "pomodoro_channel";
    private static final int NOTIFICATION_ID = 1;

    private final IBinder binder = new LocalBinder();
    private CountDownTimer countDownTimer;

    private long startTimeInMillis = 25 * 60 * 1000;
    private long timeLeftInMillis = startTimeInMillis;
    private boolean isTimerRunning = false;
    private TimerCallback callback;

    public interface TimerCallback {
        void onTimerTick(long millisUntilFinished);
        void onTimerFinish();
    }

    public class LocalBinder extends Binder {
        PomodoroService getService() {
            return PomodoroService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallback(TimerCallback callback) {
        this.callback = callback;
    }

    public void startTimer(long durationInMillis) {
        if (isTimerRunning && countDownTimer != null) {
            countDownTimer.cancel();
        }

        startTimeInMillis = durationInMillis;
        timeLeftInMillis = durationInMillis;
        isTimerRunning = true;

        startForeground(NOTIFICATION_ID, createNotification("Focusing...", timeLeftInMillis));

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateNotification("Focusing...", timeLeftInMillis);
                if (callback != null) {
                    callback.onTimerTick(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                isTimerRunning = false;
                updateNotification("Session Complete!", 0);
                if (callback != null) {
                    callback.onTimerFinish();
                }
                playAlarm();
                stopForeground(false);
            }
        }.start();

        Log.d(TAG, "Timer started for " + durationInMillis + " ms");
    }

    public void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        isTimerRunning = false;
        updateNotification("Paused", timeLeftInMillis);
        Log.d(TAG, "Timer paused");
    }

    public void resumeTimer() {
        if (!isTimerRunning && timeLeftInMillis > 0) {
            startTimer(timeLeftInMillis);
        }
    }

    public void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        isTimerRunning = false;
        timeLeftInMillis = startTimeInMillis;
        updateNotification("Ready", timeLeftInMillis);
        stopForeground(true);
        Log.d(TAG, "Timer reset");
    }

    public long getTimeLeftInMillis() {
        return timeLeftInMillis;
    }

    public boolean isTimerRunning() {
        return isTimerRunning;
    }

    public long getStartTimeInMillis() {
        return startTimeInMillis;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pomodoro Timer",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Pomodoro Timer Notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String title, long timeLeft) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("fragment", "pomodoro");
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        String timeFormatted = formatTime(timeLeft);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(timeFormatted)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void updateNotification(String title, long timeLeft) {
        Notification notification = createNotification(title, timeLeft);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, notification);
    }

    private String formatTime(long millis) {
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void playAlarm() {
        // TODO: Implement alarm sound
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        Log.d(TAG, "Service destroyed");
    }
}