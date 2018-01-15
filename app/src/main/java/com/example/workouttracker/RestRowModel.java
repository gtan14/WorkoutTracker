package com.example.workouttracker;

import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.database.Exclude;

/**
 * Created by Gerald on 11/9/2017.
 */

public class RestRowModel implements RowType{

    private String minutes, seconds, view;
    private EditText minutesEditText, secondsEditText;
    private ImageView dragHandle;
    private boolean minuteError, secondError, disableText;
    private RestRowTextWatcher restRowTextWatcher;

    @Exclude
    public RestRowTextWatcher getRestRowTextWatcher(){
        return restRowTextWatcher;
    }

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

    @Exclude
    public void setRestRowTextWatcher(RestRowTextWatcher restRowTextWatcher){
        this.restRowTextWatcher = restRowTextWatcher;
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
    public EditText getMinutesEditText(){
        return minutesEditText;
    }

    @Exclude
    public void setMinutesEditText(EditText minutesEditText){
        this.minutesEditText = minutesEditText;
    }

    @Exclude
    public EditText getSecondsEditText(){
        return secondsEditText;
    }

    @Exclude
    public void setSecondsEditText(EditText secondsEditText){
        this.secondsEditText = secondsEditText;
    }

    public String getView(){
        return view;
    }

    public void setView(String view){
        this.view = view;
    }

    public String getMinutes(){
        return minutes;
    }

    public String getSeconds(){
        return seconds;
    }

    public void setMinutes(String minutes){
        this.minutes = minutes;
    }

    public void setSeconds(String seconds){
        this.seconds = seconds;
    }
}
