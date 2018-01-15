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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    Intent countSec = new Intent(COUNTDOWN_SECONDS);

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

        //  checks the user settings to see if sound or vibrate is checked
        //  assigns that boolean to sound and vibrate

        v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        SharedPreferences restAlert = getApplicationContext().getSharedPreferences("restAlert", Context.MODE_PRIVATE);
        sound = restAlert.getBoolean("sound", false);
        vibrate = restAlert.getBoolean("vibrate", false);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
        Log.i(TAG, "Timer cancelled");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //  once service is started, starts a timer
        //  seconds and minutes are the original values before the timer started
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
                    //  sends the value of the seconds during the countdown to the receiver every tick
                    countSec.putExtra("countdownSeconds", millisUntilFinished);
                    sendBroadcast(countSec);
                }

                @Override
                public void onFinish() {

                    //  when timer is finished, and minutes has a value greater than 0
                    //  decrement value of minutes and set the new value to minuteTV and start new timer
                    if(min > 0){
                        min--;
                        SharedPreferences minPref = getApplicationContext().getSharedPreferences("countdownMin", Context.MODE_PRIVATE);
                        minPref.edit().putInt("minCountdown", min).apply();

                        sendBroadcast(countSec);
                        timer(60, min, intent);
                    }

                    //  if value of min = 0, alert user through vibration or sound, depending on settings
                    //  send value of original min and sec to receiver
                    //  stop service
                    else{
                        alertRestDone();

                        countSec.putExtra("min", inputtedMinutes);
                        countSec.putExtra("sec", inputtedSeconds);

                        SharedPreferences minPref = getApplicationContext().getSharedPreferences("min", Context.MODE_PRIVATE);
                        SharedPreferences secPref = getApplicationContext().getSharedPreferences("sec", Context.MODE_PRIVATE);

                        minPref.edit().putInt("inputtedMin", inputtedMinutes).apply();
                        secPref.edit().putInt("inputtedSec", inputtedSeconds).apply();

                        sendBroadcast(countSec);
                        stopService(intent);
                    }

                }
            };
            countDownTimer.start();
        }
    }

    //  method that is responsible for vibrating or decreasing volume halfway for 1 sec then increasing back to normal
    public void alertRestDone(){
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
