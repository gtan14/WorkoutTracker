package com.example.workouttracker;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by Gerald on 11/18/2017.
 */

public class SupersetRestRowTextWatcher {

    private MinuteTextListener minuteTextListener;
    private SecondsTextListener secondsTextListener;
    private RestRowModel restRowModel;
    private DisplayWorkoutAdapter displayWorkoutAdapter;
    private EditText minutes, seconds;

    public SupersetRestRowTextWatcher(DisplayWorkoutAdapter displayWorkoutAdapter, RestRowModel restRowModel, EditText minutes, EditText seconds){
        minuteTextListener = new MinuteTextListener();
        secondsTextListener = new SecondsTextListener();
        this.restRowModel = restRowModel;
        this.displayWorkoutAdapter = displayWorkoutAdapter;
        this.minutes = minutes;
        this.seconds = seconds;


        minutes.addTextChangedListener(minuteTextListener);
        seconds.addTextChangedListener(secondsTextListener);

    }

    public void updatePosition(int position){
        minuteTextListener.updatePosition(position);
        secondsTextListener.updatePosition(position);
    }


    private class MinuteTextListener implements TextWatcher {
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
            restRowModel.setMinutes(charSequence.toString());

            if(displayWorkoutAdapter.inputError && !charSequence.toString().equals("")) {
                TextInputLayout textInputLayout = displayWorkoutAdapter.getFirstParent(minutes, TextInputLayout.class);
                if (textInputLayout != null) {
                    textInputLayout.setError("");
                }
                restRowModel.setMinuteError(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // no op
        }
    }

    private class SecondsTextListener implements TextWatcher {
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
            restRowModel.setSeconds(charSequence.toString());

            if(displayWorkoutAdapter.inputError && !charSequence.toString().equals("")) {
                TextInputLayout textInputLayout = displayWorkoutAdapter.getFirstParent(seconds, TextInputLayout.class);
                if (textInputLayout != null) {
                    textInputLayout.setError("");
                }
                restRowModel.setSecondError(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // no op
        }
    }
}
