package com.muhammadzaini.ujiankita.browser;

import android.os.CountDownTimer;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeCounter {

    private final TextView textClock;
    private final long timeInMillis;

    public TimeCounter(TextView textClock) {
        this.textClock = textClock;
        // Set the format of the TextClock to hh:mm:ss

        // Example string representing the duration in minutes
        String durationInMinutes = BrowserData.duration; // Change this to your desired duration in minutes

        // Convert the string to an integer
        int duration = Integer.parseInt(durationInMinutes);

        // Calculate the time in milliseconds
        timeInMillis = TimeUnit.MINUTES.toMillis(duration);

        // Start the countdown timer
        startCountdownTimer();
    }


    private void startCountdownTimer() {
        new CountDownTimer(timeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the TextClock with the remaining time
                textClock.setText(getFormattedTime(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                // Countdown finished, do something
            }
        }.start();
    }

    private String getFormattedTime(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}
