package com.example.workouttracker;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Gerald on 9/24/2017.
 */

public class maxModel {

    private String exercise, max;
    private MaxRowTextWatcher maxRowTextWatcher;

    public String getExercise(){
        return exercise;
    }

    public String getMax(){
        return max;
    }

    public void setExercise(String exercise){
        this.exercise = exercise;
    }

    public void setMax(String max){
        this.max = max;
    }

    @Exclude
    public MaxRowTextWatcher getMaxRowTextWatcher(){
        return maxRowTextWatcher;
    }

    @Exclude
    public void setMaxRowTextWatcher(MaxRowTextWatcher maxRowTextWatcher){
        this.maxRowTextWatcher = maxRowTextWatcher;
    }

}
