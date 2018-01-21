package com.example.workouttracker;

import android.inputmethodservice.Keyboard;

import com.google.firebase.database.Exclude;

import java.util.List;

/**
 * Created by Gerald on 1/15/2018.
 */

public class ShareWorkout {
    private String id, token, workoutName;
    private List<RowType> workout;

    public String getWorkoutName(){
        return workoutName;
    }

    public void setWorkoutName(String workoutName){
        this.workoutName = workoutName;
    }

    public String getId(){
        return id;
    }

    public String getToken(){
        return token;
    }


    public List<RowType> getWorkout(){
        return workout;
    }

    public void setId(String id){
        this.id = id;
    }

    public void setToken(String token){
        this.token = token;
    }


    public void setWorkout(List<RowType> workout){
        this.workout = workout;
    }
}
