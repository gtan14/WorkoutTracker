package com.example.workouttracker;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import java.util.List;

/**
 * Created by Gerald on 11/19/2017.
 */

public class PyramidsetExerciseRowTextWatcher {

    private List<ExerciseModel> exerciseModelList;
    private ExerciseTextListener exerciseTextListener;
    private WeightTextListener weightTextListener;
    private SetsTextListener setsTextListener;
    private RepsTextListener repsTextListener;
    private DisplayWorkoutAdapter displayWorkoutAdapter;
    private AutoCompleteTextView exercise;
    private EditText weight, sets, reps;

    public PyramidsetExerciseRowTextWatcher(DisplayWorkoutAdapter displayWorkoutAdapter, List<ExerciseModel> exerciseModelList, AutoCompleteTextView exercise, EditText weight, EditText sets, EditText reps, int listPos){
        this.exerciseModelList = exerciseModelList;
        exerciseTextListener = new ExerciseTextListener();
        weightTextListener = new WeightTextListener();
        setsTextListener = new SetsTextListener();
        repsTextListener = new RepsTextListener();
        exercise.addTextChangedListener(exerciseTextListener);
        weight.addTextChangedListener(weightTextListener);
        sets.addTextChangedListener(setsTextListener);
        reps.addTextChangedListener(repsTextListener);
        this.displayWorkoutAdapter = displayWorkoutAdapter;
        this.exercise = exercise;
        this.weight = weight;
        this.sets = sets;
        this.reps = reps;
        updatePosition(listPos);
    }

    public void updatePosition(int position){
        exerciseTextListener.updatePosition(position);
        weightTextListener.updatePosition(position);
        setsTextListener.updatePosition(position);
        repsTextListener.updatePosition(position);
    }


    private class ExerciseTextListener implements TextWatcher {
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
            exerciseModelList.get(position).setExercise(charSequence.toString());

            if(displayWorkoutAdapter.inputError && !charSequence.toString().equals("")) {
                TextInputLayout textInputLayout = displayWorkoutAdapter.getFirstParent(exercise, TextInputLayout.class);
                if (textInputLayout != null) {
                    textInputLayout.setError("");
                }
                exerciseModelList.get(position).setExerciseError(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // no op
        }
    }

    private class WeightTextListener implements TextWatcher {
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
            exerciseModelList.get(position).setWeight(charSequence.toString());

            if(displayWorkoutAdapter.inputError && !charSequence.toString().equals("")) {
                TextInputLayout textInputLayout = displayWorkoutAdapter.getFirstParent(weight, TextInputLayout.class);
                if (textInputLayout != null) {
                    textInputLayout.setError("");
                }
                exerciseModelList.get(position).setWeightError(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // no op
        }
    }

    private class SetsTextListener implements TextWatcher {
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
            exerciseModelList.get(position).setSets(charSequence.toString());

            if(displayWorkoutAdapter.inputError && !charSequence.toString().equals("")) {
                TextInputLayout textInputLayout = displayWorkoutAdapter.getFirstParent(sets, TextInputLayout.class);
                if (textInputLayout != null) {
                    textInputLayout.setError("");
                }
                exerciseModelList.get(position).setSetError(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // no op
        }
    }

    private class RepsTextListener implements TextWatcher {
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
            exerciseModelList.get(position).setReps(charSequence.toString());

            if(displayWorkoutAdapter.inputError && !charSequence.toString().equals("")) {
                TextInputLayout textInputLayout = displayWorkoutAdapter.getFirstParent(reps, TextInputLayout.class);
                if (textInputLayout != null) {
                    textInputLayout.setError("");
                }
                exerciseModelList.get(position).setRepError(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // no op
        }
    }
}
