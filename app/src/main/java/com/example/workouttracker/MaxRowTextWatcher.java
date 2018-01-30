package com.example.workouttracker;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

/**
 * Created by Gerald on 1/2/2018.
 */

public class MaxRowTextWatcher {

    private MaxAdapter maxAdapter;
    private ExerciseMaxTextListener exerciseMaxTextListener;
    private MaxNumberMaxTextListener maxNumberMaxTextListener;

    //  public constructor
    //  initializes text listener and adds listener to edit text
    public MaxRowTextWatcher(MaxAdapter maxAdapter, EditText exercise, EditText max){
        this.maxAdapter = maxAdapter;
        exerciseMaxTextListener = new ExerciseMaxTextListener();
        maxNumberMaxTextListener = new MaxNumberMaxTextListener();
        //updatePosition(position);
        exercise.addTextChangedListener(exerciseMaxTextListener);
        max.addTextChangedListener(maxNumberMaxTextListener);
    }

    //  update position of text listener
    public void updatePosition(int position){
        exerciseMaxTextListener.updatePosition(position);
        maxNumberMaxTextListener.updatePosition(position);
    }

    /*
        Each listener onTextChanged sets either exercise or max to the text
     */

    private class ExerciseMaxTextListener implements TextWatcher {
        private int position;


        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            if(position <= maxAdapter.maxModelList.size() - 1) {
                maxAdapter.maxModelList.get(position).setExercise(charSequence.toString());
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    private class MaxNumberMaxTextListener implements TextWatcher {
        private int position;


        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if(position <= maxAdapter.maxModelList.size() - 1) {
                maxAdapter.maxModelList.get(position).setMax(charSequence.toString());
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

}
