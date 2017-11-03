package com.example.workouttracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import static com.example.workouttracker.R.id.minutesTV;
import static com.example.workouttracker.R.id.secondsTV;
import static com.example.workouttracker.R.string.sound;
import static com.example.workouttracker.R.string.vibrate;

/**
 * Created by Gerald on 10/25/2017.
 */

public class CountdownService extends Service {
    private final static String TAG = "BroadcastService";

    public static final String COUNTDOWN_SECONDS = "com.example.workouttracker.countdown_seconds";
    //public static final String COUNTDOWN_MINUTES = "com.example.workouttracker.countdown_minutes";
    Intent countSec = new Intent(COUNTDOWN_SECONDS);
    //Intent countMin = new Intent(COUNTDOWN_MINUTES);

    CountDownTimer countDownTimer = null;
    int inputtedMinutes;
    int inputtedSeconds;
    int min;
    private boolean sound;
    private boolean vibrate;
    private Vibrator v;


    @Override
    public void onCreate() {
        super.onCreate();
        v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        SharedPreferences soundNotification = getApplicationContext().getSharedPreferences("sound", Context.MODE_PRIVATE);
        SharedPreferences vibrateNotification = getApplicationContext().getSharedPreferences("vibrate", Context.MODE_PRIVATE);
        sound = soundNotification.getBoolean("soundCheckbox", false);
        vibrate = vibrateNotification.getBoolean("vibrateCheckbox", false);
    }

    @Override
    public void onDestroy() {

        countDownTimer.cancel();
        Log.i(TAG, "Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            int seconds = intent.getIntExtra("seconds", -1);
            int minutes = intent.getIntExtra("minutes", -1);
            timer(seconds, minutes, intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void timer(int seconds, int minutes, final Intent intent){
        if(seconds != -1) {
            min = minutes;
            inputtedMinutes = intent.getIntExtra("minutes", -1);
            inputtedSeconds = intent.getIntExtra("seconds", -1);
            countDownTimer = new CountDownTimer(seconds * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    countSec.putExtra("countdownSeconds", millisUntilFinished);
                    sendBroadcast(countSec);
                }

                @Override
                public void onFinish() {
                    if(min > 0){
                        min--;
                        SharedPreferences minPref = getApplicationContext().getSharedPreferences("countdownMin", Context.MODE_PRIVATE);
                        minPref.edit().putInt("minCountdown", min).apply();
                        //countSec.putExtra("min", inputtedMinutes);
                        //countSec.putExtra("sec", inputtedSeconds);
                        sendBroadcast(countSec);
                        timer(60, min, intent);
                    }

                    else{
                        alertRestDone();
                        countSec.putExtra("min", inputtedMinutes);
                        countSec.putExtra("sec", inputtedSeconds);

                        SharedPreferences minPref = getApplicationContext().getSharedPreferences("min", Context.MODE_PRIVATE);
                        SharedPreferences secPref = getApplicationContext().getSharedPreferences("sec", Context.MODE_PRIVATE);
                        SharedPreferences timerPref = getApplicationContext().getSharedPreferences("timer", Context.MODE_PRIVATE);
                        minPref.edit().putInt("inputtedMin", inputtedMinutes).apply();
                        secPref.edit().putInt("inputtedSec", inputtedSeconds).apply();
                        timerPref.edit().putBoolean("timerFinished", true).apply();

                        sendBroadcast(countSec);
                    }

                }
            };
            countDownTimer.start();
        }
    }

    public void alertRestDone(){
        Log.d("alert", "alert");
        if (vibrate) {
            v.vibrate(1000);
        }
        if (sound) {
            final AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            final int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            float percent = 0.5f;
            final int halfVolume = (int) (currentVolume * percent);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, halfVolume, 0);
            android.os.Handler handler = new android.os.Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
                }
            }, 1000);
        }
    }
}
