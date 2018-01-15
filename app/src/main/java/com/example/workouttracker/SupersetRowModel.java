package com.example.workouttracker;

import android.inputmethodservice.Keyboard;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gerald on 11/9/2017.
 */

public class SupersetRowModel implements RowType {
    private LinearLayout linearLayout;
    public int numberOfChildren;
    public String view;
    private boolean addRest;
    private boolean loadedSuperset, disableText;
    private ArrayList<String> exerciseError, weightError, setError, repError;
    private boolean minuteError, secondError;
    private List<ExerciseModel> exerciseModelList;
    private RestRowModel restRowModel;
    private EditText minutes;
    private EditText seconds;
    private SupersetExerciseRowTextWatcher supersetExerciseRowTextWatcher;
    private SupersetRestRowTextWatcher supersetRestRowTextWatcher;


    public boolean isDisableText(){
        return disableText;
    }

    public void setDisableText(boolean disableText){
        this.disableText = disableText;
    }

    public boolean isMinuteError(){
        return minuteError;
    }

    public void setMinuteError(boolean minuteError){
        this.minuteError = minuteError;
    }

    public boolean isSecondError(){
        return secondError;
    }

    public void setSecondError(boolean secondError){
        this.secondError = secondError;
    }

    public ArrayList<String> getExerciseError(){
        return exerciseError;
    }

    public void setExerciseError(ArrayList<String> exerciseError){
        this.exerciseError = exerciseError;
    }

    public ArrayList<String> getWeightError(){
        return weightError;
    }

    public ArrayList<String> getSetError(){
        return setError;
    }

    public ArrayList<String> getRepError(){
        return repError;
    }

    public void setWeightError(ArrayList<String> weightError){
        this.weightError = weightError;
    }

    public void setSetError(ArrayList<String> setError){
        this.setError = setError;
    }

    public void setRepError(ArrayList<String> repError){
        this.repError = repError;
    }

    @Exclude
    public EditText getMinutes(){
        return minutes;
    }

    @Exclude
    public EditText getSeconds(){
        return seconds;
    }

    @Exclude
    public void setMinutes(EditText minutes){
        this.minutes = minutes;
    }

    @Exclude
    public void setSeconds(EditText seconds){
        this.seconds = seconds;
    }

    @Exclude
    public void setSupersetExerciseRowTextWatcher(SupersetExerciseRowTextWatcher supersetExerciseRowTextWatcher){
        this.supersetExerciseRowTextWatcher = supersetExerciseRowTextWatcher;
    }

    @Exclude
    public SupersetRestRowTextWatcher supersetRestRowTextWatcher(){
        return supersetRestRowTextWatcher;
    }

    @Exclude
    public void setSupersetRestRowTextWatcher(SupersetRestRowTextWatcher supersetRestRowTextWatcher){
        this.supersetRestRowTextWatcher = supersetRestRowTextWatcher;
    }

    public SupersetRowModel(){

    }

    public boolean getLoadedSuperset(){
        return loadedSuperset;
    }

    public void setLoadedSuperset(boolean loadedSuperset){
        this.loadedSuperset = loadedSuperset;
    }

    public String getViewType(){
        return view;
    }

    public void setViewType(String view){
        this.view = view;
    }

    @Exclude
    public LinearLayout getLinearLayout(){
        return linearLayout;
    }

    @Exclude
    public void setLinearLayout(LinearLayout linearLayout){
        this.linearLayout = linearLayout;
    }


    public void setExerciseModelList(List<ExerciseModel> exerciseModelList){
        this.exerciseModelList = exerciseModelList;
    }

    public List<ExerciseModel> getExerciseModelList(){
        return exerciseModelList;
    }

    public void setRestRowModel(RestRowModel restRowModel){
        this.restRowModel = restRowModel;
    }

    public RestRowModel getRestRowModel(){
        return restRowModel;
    }

    public void addView(View view){
        linearLayout.addView(view);
    }

    public int getNumberOfChildren(){
        return numberOfChildren;
    }

    public void setNumberOfChildren(int numberOfChildren){
        this.numberOfChildren = numberOfChildren;
    }

    public boolean getAddRest(){
        return addRest;
    }

    public void setAddRest(boolean addRest){
        this.addRest = addRest;
    }
}
