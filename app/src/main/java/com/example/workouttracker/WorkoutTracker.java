package com.example.workouttracker;

import android.app.Application;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Gerald on 1/2/2018.
 */

public class WorkoutTracker extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //  enables offline usage
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //  ensures that when a user shares a workout with another user, the receiver gets the updated data
        //  this is required due to some instances causing no data to be sent when the receiver accepts a workout
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("shareWorkout");
        databaseReference.keepSynced(true);
    }

}
