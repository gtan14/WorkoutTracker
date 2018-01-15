package com.example.workouttracker;

import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.database.Exclude;

/**
 * Created by Gerald on 7/12/2017.
 */

public class ExerciseModel implements RowType {
    public String exercise, sets, reps, view, minutes, seconds, time, weight, childCount;
    private boolean exerciseError, setError, repError, weightError, weightEmpty, disableText;
    private EditText weightEditText, setsEditText, repsEditText;
    private AutoCompleteTextView exerciseEditText;
    private ImageView dragHandle;
    private ExerciseRowTextWatcher exerciseRowTextWatcher;

    public ExerciseModel(){

    }

    public boolean isDisableText(){
        return disableText;
    }

    public void setDisableText(boolean disableText){
        this.disableText = disableText;
    }

    public boolean isWeightEmpty(){
        return weightEmpty;
    }

    public void setWeightEmpty(boolean weightEmpty){
        this.weightEmpty = weightEmpty;
    }

    public boolean isWeightError(){
        return weightError;
    }

    public void setWeightError(boolean weightError){
        this.weightError = weightError;
    }

    public boolean isExerciseError(){
        return exerciseError;
    }

    public void setExerciseError(boolean exerciseError){
        this.exerciseError = exerciseError;
    }

    public boolean isSetError(){
        return setError;
    }

    public void setSetError(boolean setError){
        this.setError = setError;
    }

    public boolean isRepError(){
        return repError;
    }

    public void setRepError(boolean repError){
        this.repError = repError;
    }

    @Exclude
    public ExerciseRowTextWatcher getExerciseRowTextWatcher(){
        return exerciseRowTextWatcher;
    }

    @Exclude
    public void setExerciseRowTextWatcher(ExerciseRowTextWatcher exerciseRowTextWatcher){
        this.exerciseRowTextWatcher = exerciseRowTextWatcher;
    }


    @Exclude
    public ImageView getDragHandle(){
        return dragHandle;
    }

    @Exclude
    public void setDragHandle(ImageView dragHandle){
        this.dragHandle = dragHandle;
    }

    @Exclude
    public AutoCompleteTextView getExerciseEditText(){
        return exerciseEditText;
    }

    @Exclude
    public void setExerciseEditText(AutoCompleteTextView exerciseEditText){
        this.exerciseEditText = exerciseEditText;
    }

    @Exclude
    public EditText getWeightEditText(){
        return weightEditText;
    }

    @Exclude
    public void setWeightEditText(EditText weightEditText){
        this.weightEditText = weightEditText;
    }

    @Exclude
    public EditText getSetsEditText(){
        return setsEditText;
    }

    @Exclude
    public void setSetsEditText(EditText setsEditText){
        this.setsEditText = setsEditText;
    }

    @Exclude
    public EditText getRepsEditText(){
        return repsEditText;
    }

    @Exclude
    public void setRepsEditText(EditText repsEditText){
        this.repsEditText = repsEditText;
    }

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
