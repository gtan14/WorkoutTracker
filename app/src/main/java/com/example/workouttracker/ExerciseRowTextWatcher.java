package com.example.workouttracker;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

/**
 * Created by Gerald on 11/16/2017.
 */

public class ExerciseRowTextWatcher {

    private DisplayWorkoutAdapter displayWorkoutAdapter;
    private ExerciseTextListener exerciseTextListener;
    private WeightTextListener weightTextListener;
    private SetsTextListener setsTextListener;
    private RepsTextListener repsTextListener;

    //  public constructor
    //  initializes the text listeners and adds the listeners to the edit texts
    public ExerciseRowTextWatcher(DisplayWorkoutAdapter displayWorkoutAdapter, AutoCompleteTextView exercise, EditText weight, EditText sets, EditText reps){
        this.displayWorkoutAdapter = displayWorkoutAdapter;
        exerciseTextListener = new ExerciseTextListener();
        weightTextListener = new WeightTextListener();
        setsTextListener = new SetsTextListener();
        repsTextListener = new RepsTextListener();
        exercise.addTextChangedListener(exerciseTextListener);
        weight.addTextChangedListener(weightTextListener);
        sets.addTextChangedListener(setsTextListener);
        reps.addTextChangedListener(repsTextListener);
    }

    //  updates the position of the text listener
    public void updatePosition(int position){
        exerciseTextListener.updatePosition(position);
        weightTextListener.updatePosition(position);
        setsTextListener.updatePosition(position);
        repsTextListener.updatePosition(position);
    }

    /*
        Each listener onTextChanged sets either exercise, weight, reps, or sets to the text
        Removes textInputLayout error once text is changed
     */

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

            ((ExerciseModel) displayWorkoutAdapter.rowTypeList.get(position)).setExercise(charSequence.toString());


            if(displayWorkoutAdapter.inputError && !charSequence.toString().equals("")) {
                TextInputLayout textInputLayout = displayWorkoutAdapter.getFirstParent(((ExerciseModel) displayWorkoutAdapter.rowTypeList.get(position)).getExerciseEditText(), TextInputLayout.class);
                if (textInputLayout != null) {
                    textInputLayout.setError("");
                }

                ((ExerciseModel) displayWorkoutAdapter.rowTypeList.get(position)).setExerciseError(false);
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
            ((ExerciseModel) displayWorkoutAdapter.rowTypeList.get(position)).setWeight(charSequence.toString());


            if(displayWorkoutAdapter.inputError) {
                int countDecimal = charSequence.toString().length() - charSequence.toString().replace(".", "").length();
                int countPercent = charSequence.toString().length() - charSequence.toString().replace("%", "").length();

                //  if weight has a percentage and the percentage is not at the end or is by itself, display error
                //  if weight has more than one percent sign, display error
                //  if weight starts with or ends with a decimal display error
                //  if weight has more than one decimal display error
                if(!(charSequence.toString().length() == 1 && charSequence.toString().equalsIgnoreCase("%") ||
                        charSequence.toString().length() >= 2 && charSequence.toString().contains("%") && !charSequence.toString().endsWith("%") ||
                        charSequence.toString().length() >= 2 && charSequence.toString().contains("%") && countPercent > 1||
                        charSequence.toString().startsWith(".") || charSequence.toString().endsWith(".") ||
                        countDecimal > 1)) {
                    TextInputLayout textInputLayout = displayWorkoutAdapter.getFirstParent(((ExerciseModel) displayWorkoutAdapter.rowTypeList.get(position)).getWeightEditText(), TextInputLayout.class);
                    if (textInputLayout != null) {
                        textInputLayout.setError("");
                    }

                    ((ExerciseModel) displayWorkoutAdapter.rowTypeList.get(position)).setWeightError(false);
                }
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
            ((ExerciseModel) displayWorkoutAdapter.rowTypeList.get(position)).setSets(charSequence.toString());


            if(displayWorkoutAdapter.inputError && !charSequence.toString().equals("")) {
                TextInputLayout textInputLayout = displayWorkoutAdapter.getFirstParent(((ExerciseModel) displayWorkoutAdapter.rowTypeList.get(position)).getSetsEditText(), TextInputLayout.class);
                if (textInputLayout != null) {
                    textInputLayout.setError("");
                }

                ((ExerciseModel) displayWorkoutAdapter.rowTypeList.get(position)).setSetError(false);
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
            ((ExerciseModel) displayWorkoutAdapter.rowTypeList.get(position)).setReps(charSequence.toString());


            if(displayWorkoutAdapter.inputError && !charSequence.toString().equals("")) {
                TextInputLayout textInputLayout = displayWorkoutAdapter.getFirstParent(((ExerciseModel) displayWorkoutAdapter.rowTypeList.get(position)).getRepsEditText(), TextInputLayout.class);
                if (textInputLayout != null) {
                    textInputLayout.setError("");
                }

                ((ExerciseModel) displayWorkoutAdapter.rowTypeList.get(position)).setRepError(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // no op
        }
    }


}
