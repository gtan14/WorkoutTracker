package com.example.workouttracker;

/**
 * Created by Gerald on 7/12/2017.
 */

public class ExerciseModel {
    private String exercise, sets, reps, view, minutes, seconds, time, weight, childCount;

    public String getChildCount(){
        return childCount;
    }

    public void setChildCount(String childCount){
        this.childCount = childCount;
    }

    public String getExercise(){
        return exercise;
    }

    public String getWeight(){
        return weight;
    }

    public String getSets(){
        return sets;
    }

    public String getReps(){
        return reps;
    }

    public String getView(){
        return view;
    }

    public String getMinutes(){
        return minutes;
    }

    public String getSeconds(){
        return seconds;
    }

    public String getTime(){   return time;}

    public void setExercise(String exercise){
        this.exercise = exercise;
    }

    public void setTime(String time){ this.time = time; }

    public void setWeight(String weight){
        this.weight = weight;
    }

    public void setMinutes(String minutes){
        this.minutes = minutes;
    }

    public void setSeconds(String seconds){
        this.seconds = seconds;
    }

    public void setSets(String sets){
        this.sets = sets;
    }

    public void setView(String view){
        this.view = view;
    }

    public void setReps(String reps){
        this.reps = reps;
    }

}
