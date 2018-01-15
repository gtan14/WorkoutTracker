package com.example.workouttracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.jar.Attributes;

import static android.R.attr.id;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;


public class MainActivity extends navDrawer implements SavedWorkouts.onLoadWorkout{
    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    //  Method that displays the home fragment when a workout is loaded
    //  current fragment is replaced with home fragment
    public void loadWorkout(String workoutName){
        Home home = new Home();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, home, "home");
        ft.commit();
        home.booleanLoadWorkout(workoutName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            if(user.getEmail() != null && user.getEmail().contains(".")) {
                String validDatabasePath = user.getEmail().replace(".", "?");
                myRef = database.getReference(validDatabasePath + "WorkoutTracker");
            }

            else if(user.getEmail() != null && !user.getEmail().contains(".")){
                myRef = database.getReference(user.getEmail() + "WorkoutTracker");
            }
        }

        //  removes pause workout if there is one
        myRef.child("workoutPause").removeValue();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();

        //  if activity is destroyed, remove pause workout and any shared prefs needed to be deleted to reset app to normal
        myRef.child("workoutPause").removeValue();
        SharedPreferences countdownMin = getSharedPreferences("countdownMin", Context.MODE_PRIVATE);
        SharedPreferences minPref = getSharedPreferences("min", Context.MODE_PRIVATE);
        SharedPreferences secPref = getSharedPreferences("sec", Context.MODE_PRIVATE);
        countdownMin.edit().clear().apply();
        minPref.edit().clear().apply();
        secPref.edit().clear().apply();
        SharedPreferences sharedPreferences = getSharedPreferences("startClicked", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
        SharedPreferences pref = getSharedPreferences("swipeIndex", Context.MODE_PRIVATE);
        pref.edit().clear().apply();
        SharedPreferences sharedPreferences1 = getSharedPreferences("workoutNameOnPause", Context.MODE_PRIVATE);
        sharedPreferences1.edit().clear().apply();
    }

}
