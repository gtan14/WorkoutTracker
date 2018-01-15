package com.example.workouttracker;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by Gerald on 12/7/2017.
 */

public class DecreaseSetEditText {

    private DisplayWorkoutAdapter displayWorkoutAdapter;
    private Home home;

    public DecreaseSetEditText(DisplayWorkoutAdapter displayWorkoutAdapter, Home home){
        this.displayWorkoutAdapter = displayWorkoutAdapter;
        this.home = home;
    }

    //  Method for decreasing set in exercise row
    public void decreaseSet() {
        int numberOfItems = displayWorkoutAdapter.getItemCount() - 1;
        RowType rowType = displayWorkoutAdapter.rowTypeList.get(numberOfItems);
        if (rowType.getClass() == ExerciseModel.class) {
            EditText set = ((ExerciseModel) rowType).getSetsEditText();
            String setString = set.getText().toString();
            int intString = Integer.parseInt(setString);

            //  if set > 1, decrease by 1 and assign set new int
            if (intString > 1) {
                --intString;
                set.setText(String.valueOf(intString));
            }

            //  if set = 1, remove the view
            else if (intString == 1) {
                displayWorkoutAdapter.removeViewHolder(numberOfItems);
            }
        }
    }

    //  method for decreasing pyramid set
    public void decreasePyramidSet(LinearLayout layout){
        LayoutInflater layoutInflater = (LayoutInflater) home.activity.getLayoutInflater();
        View exerciseRow = layoutInflater.inflate(R.layout.row, null);

        //  if first child is an exercise row and it is the only child
        if(layout.getChildAt(0).getId() == exerciseRow.getId() && layout.getChildCount() == 1) {

            //  if this exercise row is the only child, remove the row and remove the view from container
            layout.removeViewAt(0);
            displayWorkoutAdapter.removeViewHolder(displayWorkoutAdapter.rowTypeList.size() - 1);
        }

        //  else if the next child is another exercise row, get the weight and reps of the second row and replace the first row with it. Delete second row
        else if(layout.getChildAt(0).getId() == exerciseRow.getId() && layout.getChildAt(1).getId() == exerciseRow.getId()){

            View first = layout.getChildAt(0);
            View second = layout.getChildAt(1);

            EditText weightFirst = (EditText) first.findViewById(R.id.weight);
            EditText repsFirst = (EditText) first.findViewById(R.id.reps);
            EditText weightSecond = (EditText) second.findViewById(R.id.weight);
            EditText repsSecond = (EditText) second.findViewById(R.id.reps);

            weightFirst.setText(weightSecond.getText().toString());
            repsFirst.setText(repsSecond.getText().toString());

            List<ExerciseModel> exerciseModelList = ((PyramidsetRowModel) displayWorkoutAdapter.rowTypeList.get(displayWorkoutAdapter.getItemCount() - 1)).getExerciseModelList();
            String exercise = exerciseModelList.get(0).getExercise();
            exerciseModelList.remove(0);
            exerciseModelList.get(0).setExercise(exercise);
            ((PyramidsetRowModel) displayWorkoutAdapter.rowTypeList.get(displayWorkoutAdapter.getItemCount() - 1)).setNumberOfChildren(((PyramidsetRowModel) displayWorkoutAdapter.rowTypeList.get(displayWorkoutAdapter.getItemCount() - 1)).getNumberOfChildren() - 1);
            layout.removeViewAt(1);
        }
    }


    //  Method for decreasing set of exercise row in a superset row
    public void decreaseSuperset(int position, LinearLayout layout, SharedPreferences pref) {
        LayoutInflater layoutInflater = (LayoutInflater) home.activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View exerciseRow = layoutInflater.inflate(R.layout.row, null);
        View restRow = layoutInflater.inflate(R.layout.rest_row, null);
        if(layout.getChildAt(position).getId() == exerciseRow.getId()) {
            View v = layout.getChildAt(position);
            EditText set = (EditText) v.findViewById(R.id.sets);
            String setString = set.getText().toString();
            int intString = Integer.parseInt(setString);

            //  if set > 1, decrease set, and assign index to shared pref
            //  updates rowTypeList with new set #
            if (intString > 1) {
                --intString;
                set.setText(String.valueOf(intString));
                displayWorkoutAdapter.index++;
                pref.edit().putInt("index", displayWorkoutAdapter.index).apply();
                ((SupersetRowModel) displayWorkoutAdapter.rowTypeList.get(displayWorkoutAdapter.getItemCount() - 1)).getExerciseModelList().get(position).setSets(String.format("%s", intString));
            }

            //  if set = 1, remove from superset and update number of children in superset
            else if (intString == 1){
                List<ExerciseModel> exerciseModelList = ((SupersetRowModel) displayWorkoutAdapter.rowTypeList.get(displayWorkoutAdapter.getItemCount() - 1)).getExerciseModelList();
                exerciseModelList.remove(position);

                layout.removeViewAt(position);
                ((SupersetRowModel) displayWorkoutAdapter.rowTypeList.get(displayWorkoutAdapter.getItemCount() - 1)).setNumberOfChildren(((SupersetRowModel) displayWorkoutAdapter.rowTypeList.get(displayWorkoutAdapter.getItemCount() - 1)).getNumberOfChildren() - 1);
            }
        }

        //  If current position is the last exercise, restart index back to 0
        //  If current position is not the last exercise, increase index
        if(layout.getChildCount() > 0) {
            if (displayWorkoutAdapter.index == layout.getChildCount() - 1 && layout.getChildAt(layout.getChildCount() - 1).getId() == restRow.getId()) {
                displayWorkoutAdapter.index = 0;
                pref.edit().clear().apply();
            } else if (displayWorkoutAdapter.index == layout.getChildCount() && layout.getChildAt(layout.getChildCount() - 1).getId() != restRow.getId()) {
                displayWorkoutAdapter.index = 0;
                pref.edit().clear().apply();
            }
        }

        //  if superset does not have any children, remove viewholder from rowTypeList
        else if(layout.getChildCount() == 0){
            displayWorkoutAdapter.removeViewHolder(displayWorkoutAdapter.getItemCount() - 1);
        }
    }
}
