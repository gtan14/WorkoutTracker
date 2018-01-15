package com.example.workouttracker;

/**
 * Created by Gerald on 11/7/2017.
 */

public class SavedWorkoutsModel {

    private String workoutName, completedTime;

    public SavedWorkoutsModel(){

    }

    public SavedWorkoutsModel(String workoutName, String completedTime){
        this.workoutName = workoutName;
        this.completedTime = completedTime;
    }

    public String getWorkoutName(){
        return workoutName;
    }

    public String getCompletedTime(){
        return completedTime;
    }

    public void setWorkoutName(String workoutName){
        this.workoutName = workoutName;
    }

    public void setCompletedTime(String completedTime){
        this.completedTime = completedTime;
    }
}
