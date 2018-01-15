package com.example.workouttracker;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

/**
 * Created by Gerald on 11/17/2017.
 */

public class RestRowTextWatcher {

    private DisplayWorkoutAdapter displayWorkoutAdapter;
    private MinuteTextWatcher minuteTextWatcher;
    private SecondsTextWatcher secondsTextWatcher;


    //  public constructor that initializes text watchers and adds the listener to the edit texts
    public RestRowTextWatcher(DisplayWorkoutAdapter displayWorkoutAdapter, EditText minutes, EditText seconds){
        this.displayWorkoutAdapter = displayWorkoutAdapter;
        minuteTextWatcher = new MinuteTextWatcher();
        secondsTextWatcher = new SecondsTextWatcher();

        minutes.addTextChangedListener(minuteTextWatcher);
        seconds.addTextChangedListener(secondsTextWatcher);

    }

    //  update the position of the text watchers
    public void updatePosition(int position){
        minuteTextWatcher.updatePosition(position);
        secondsTextWatcher.updatePosition(position);
    }

    private class MinuteTextWatcher implements TextWatcher {
        private int position;


        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            // no op
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            //  sets the minutes for the rest row in rowTypeList
            if(displayWorkoutAdapter.rowTypeList.get(position).getClass() == RestRowModel.class) {
                ((RestRowModel) displayWorkoutAdapter.rowTypeList.get(position)).setMinutes(charSequence.toString());
            }


            //  removes textInputLayout error when text is changed
            if(displayWorkoutAdapter.inputError && !charSequence.toString().equals("")) {
                TextInputLayout textInputLayout = displayWorkoutAdapter.getFirstParent(((RestRowModel) displayWorkoutAdapter.rowTypeList.get(position)).getMinutesEditText(), TextInputLayout.class);
                if (textInputLayout != null) {
                    textInputLayout.setError("");
                }

                ((RestRowModel) displayWorkoutAdapter.rowTypeList.get(position)).setMinuteError(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // no op
        }
    }

    private class SecondsTextWatcher implements TextWatcher {
        private int position;


        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            // no op
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            //  sets the seconds for the rest row in rowTypeList
            if(displayWorkoutAdapter.rowTypeList.get(position).getClass() == RestRowModel.class) {
                ((RestRowModel) displayWorkoutAdapter.rowTypeList.get(position)).setSeconds(charSequence.toString());
            }


            //  removes textInputLayout error when text is changed
            if(displayWorkoutAdapter.inputError && !charSequence.toString().equals("")) {
                TextInputLayout textInputLayout = displayWorkoutAdapter.getFirstParent(((RestRowModel) displayWorkoutAdapter.rowTypeList.get(position)).getSecondsEditText(), TextInputLayout.class);
                if (textInputLayout != null) {
                    textInputLayout.setError("");
                }

                ((RestRowModel) displayWorkoutAdapter.rowTypeList.get(position)).setSecondError(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // no op
        }
    }


}
