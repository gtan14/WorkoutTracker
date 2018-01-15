package com.example.workouttracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.SystemClock;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Gerald on 11/24/2017.
 */

public class StopWorkout {
    private Home home;
    private DisplayWorkoutAdapter displayWorkoutAdapter;
    private SaveChanges saveChanges;

    public StopWorkout(Home home, DisplayWorkoutAdapter displayWorkoutAdapter){
        this.home = home;
        this.displayWorkoutAdapter = displayWorkoutAdapter;
        saveChanges = new SaveChanges(home, displayWorkoutAdapter);
    }


    public void setWorkoutTime() {

        //gets the elapsed time from when start button is clicked
        SharedPreferences pref = home.activity.getSharedPreferences("time", Context.MODE_PRIVATE);
        final RestCountDown restCountDown = new RestCountDown(home, displayWorkoutAdapter);
        long startTime = pref.getLong("timeElapsed", -1);
        long endTime = SystemClock.elapsedRealtime();
        long elapsedMilliSeconds = endTime - startTime;
        long seconds = elapsedMilliSeconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        final String TIME = String.format("%s", hours % 24 + ":" + minutes % 60 + ":" + seconds % 60);
        //if current layout is a loaded workout
        if (home.titleSet) {
            final String workoutName = home.activity.getTitle().toString();

            home.myRef.child("workouts").child(workoutName + "TimeTable")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {

                                WorkoutTime workoutTime = dataSnapshot.getValue(WorkoutTime.class);
                                String time = workoutTime.getTime();

                                //gets the time completed for the workout and compares it to the most recent time
                                //if the most recent is faster, replace the old time with the new
                                //if not, use the old time
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(time);
                                SimpleDateFormat sdf = new SimpleDateFormat(TIME);
                                int storedTime = Integer.parseInt(simpleDateFormat.format(new Date()).replace(":", ""));
                                int recentTime = Integer.parseInt(sdf.format(new Date()).replace(":", ""));

                                if (recentTime < storedTime) {
                                    saveChanges.saveToDB(workoutName, null, false, TIME, null, null);
                                }
                            } else {
                                saveChanges.saveToDB(workoutName, null, false, TIME, null, null);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
            final SharedPreferences countdownMin = home.activity.getSharedPreferences("countdownMin", Context.MODE_PRIVATE);
            SharedPreferences minPref = home.activity.getSharedPreferences("min", Context.MODE_PRIVATE);
            SharedPreferences secPref = home.activity.getSharedPreferences("sec", Context.MODE_PRIVATE);
            SharedPreferences swipe = home.activity.getSharedPreferences("swipeIndex", Context.MODE_PRIVATE);
            swipe.edit().clear().apply();
            countdownMin.edit().clear().apply();
            minPref.edit().clear().apply();
            secPref.edit().clear().apply();
            home.myRef.child("workoutPause").removeValue();
            displayWorkoutAdapter.startTimer = false;
            if(displayWorkoutAdapter.isMyServiceRunning(CountdownService.class)){
                home.activity.stopService(home.serviceIntent);

                try {
                    home.activity.unregisterReceiver(restCountDown.secondReceiver);
                }
                catch (IllegalArgumentException e){

                }
            }
            notification(TIME);
            home.newWorkoutFAB.setVisibility(View.VISIBLE);
        }
    }

    private void notification(String time){

        RemoteViews contentView = new RemoteViews(home.activity.getPackageName(), R.layout.notification_drawer);
        contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(home.activity, "TimeNotification")
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Time Elapsed")
                        .setContentText(time)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(Notification.PRIORITY_HIGH);

        // Creates an explicit intent for an Activity in your app
                Intent resultIntent = new Intent(home.activity, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(home.activity);
        // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) home.activity.getSystemService(Context.NOTIFICATION_SERVICE);


        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        final int mNotificationId = 1;
        if(mNotificationManager != null) {
            mNotificationManager.notify(mNotificationId, mBuilder.build());
        }
    }

}
