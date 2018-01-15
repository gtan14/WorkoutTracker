package com.example.workouttracker;

import android.widget.LinearLayout;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gerald on 11/9/2017.
 */

public class PyramidsetRowModel implements RowType{

    public int numberOfChildren;
    public String view;
    private boolean addRest;
    public boolean loadedPyramidSet, disableText;
    private boolean exerciseError, minuteError, secondError;
    private ArrayList<String> weightError, repError;
    private List<ExerciseModel> exerciseModelList;
    private RestRowModel restRowModel;
    private LinearLayout linearLayout;

    public PyramidsetRowModel(){

    }

    public boolean isDisableText(){
        return disableText;
    }

    public void setDisableText(boolean disableText){
        this.disableText = disableText;
    }

    public boolean isExerciseError(){
        return exerciseError;
    }

    public boolean isMinuteError(){
        return minuteError;
    }

    public boolean isSecondError(){
        return secondError;
    }

    public void setExerciseError(boolean exerciseError){
        this.exerciseError = exerciseError;
    }

    public void setMinuteError(boolean minuteError){
        this.minuteError = minuteError;
    }

    public void setSecondError(boolean secondError){
        this.secondError = secondError;
    }

    public ArrayList<String> getWeightError(){
        return weightError;
    }

    public ArrayList<String> getRepError(){
        return repError;
    }

    public void setWeightError(ArrayList<String> weightError){
        this.weightError = weightError;
    }

    public void setRepError(ArrayList<String> repError){
        this.repError = repError;
    }


    public boolean getLoadedPyramidSet(){
        return loadedPyramidSet;
    }

    public void setLoadedPyramidSet(boolean loadedPyramidSet){
        this.loadedPyramidSet = loadedPyramidSet;
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
