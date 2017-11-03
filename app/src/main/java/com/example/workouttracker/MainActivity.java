package com.example.workouttracker;

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

import java.util.ArrayList;
import java.util.jar.Attributes;

import static android.R.attr.id;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;


public class MainActivity extends navDrawer implements SavedWorkouts.onLoadWorkout{
    SQLiteHelper sqLiteHelper;
    ExerciseModel exerciseModel;
    ArrayList<ExerciseModel> exercise;
    String titleName;
    FrameLayout frameLayout;


    //Method that displays the home fragment when a workout is clicked in the savedworkout fragment
    public void loadWorkout(String workoutName){
        //gets the workout names in the database
        /*ArrayList<String> tableNames = sqLiteHelper.getTableNames();
        for(String a: tableNames)
            //If the table name matches the workout name
            //Change the title to the workout name and display home fragment
            if(a.equals(workoutName)) {
                exercise = sqLiteHelper.getAllRecords(a);*/
                titleName = workoutName;
                Home home = new Home();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, home, "home");
                ft.commit();
                home.booleanLoadWorkout();
            //}


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = getLayoutInflater();
        View row = layoutInflater.inflate(R.layout.row_container, null);
        frameLayout = (FrameLayout) row.findViewById(R.id.mainmenu);
    }

    //method that disables any input on an EditText
    void disableEditText(EditText editText) {
        //textInputLayout.setFocusable(false);
        //textInputLayout.setFocusableInTouchMode(false);
        //Drawable drawable = textInputLayout.getBackground();
        //drawable.setColorFilter(new PorterDuffColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN));
        //textInputLayout.setBackground(drawable);
        //EditText editText = textInputLayout.getEditText();
        if(editText != null) {
            //editText.setEnabled(false);
            editText.setCursorVisible(false);
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(false);
            //editText.setBackgroundResource(android.R.color.transparent);
            //Drawable d = editText.getBackground();
            //Log.d("asjdkl", String.format("%s", d));
            //d.setColorFilter(new PorterDuffColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN));
            //editText.setBackground(d);
            editText.clearFocus();
            //frameLayout.requestFocus();

            editText.setBackground(null);
            //editText.setBackgroundColor(Color.TRANSPARENT);
            //editText.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_OVER);
        }
    }

    //method that enables input on an EditText
    void enableEditText(TextInputLayout textInputLayout) {
        EditText editText = textInputLayout.getEditText();
        if(editText != null) {
            //editText.setEnabled(true);
            editText.setFocusable(true);
            editText.setCursorVisible(true);
            editText.setFocusableInTouchMode(true);
            //editText.getBackground().clearColorFilter();
        }
    }




}
