package com.example.workouttracker;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Gerald on 9/24/2017.
 */

public class Max extends Fragment {
    MainActivity activity;
    EditText squatMax;
    EditText benchMax;
    EditText deadliftMax;
    EditText cjMax;
    EditText snatchMax;
    EditText powerCleanMax;
    Button saveMax;
    ConstraintLayout constraintLayout;
    boolean maxTableExists;
    final String maxTable = "maxxxxxxxxxx";

    //Assigns activity to MainActivity context once Home is attached
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof MainActivity){
            activity = (MainActivity) context;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returns layout file
        View v= inflater.inflate(R.layout.max, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {

        //View initialization
        squatMax = (EditText) v.findViewById(R.id.squatMax);
        benchMax = (EditText) v.findViewById(R.id.benchMax);
        deadliftMax = (EditText) v.findViewById(R.id.deadliftMax);
        cjMax = (EditText) v.findViewById(R.id.cjMax);
        snatchMax = (EditText) v.findViewById(R.id.snatchMax);
        powerCleanMax = (EditText) v.findViewById(R.id.powerCleanMax);
        saveMax = (Button) v.findViewById(R.id.saveMax);
        constraintLayout = (ConstraintLayout) v.findViewById(R.id.maxConstraint);

    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        activity.setTitle("Max");
        final SQLiteHelper sqLiteHelper = new SQLiteHelper(activity);

        //sqLiteHelper.deleteTable(maxTable);

        //checks to see if 1rm has already been saved
        maxTableExists = sqLiteHelper.tableExists(maxTable);

        if(maxTableExists){
            ArrayList<maxModel> maxModelArrayList = sqLiteHelper.getMaxes(maxTable);
            maxModel maxModel = maxModelArrayList.get(0);
            squatMax.setText(maxModel.getMaxSquat());
            benchMax.setText(maxModel.getMaxBench());
            deadliftMax.setText(maxModel.getMaxDeadlift());
            cjMax.setText(maxModel.getMaxCJ());
            snatchMax.setText(maxModel.getMaxSnatch());
            powerCleanMax.setText(maxModel.getMaxPowerClean());
        }

        //save button on click
        saveMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String saveSuccessful = "Save successful";

                //if no 1rm has been saved, create new max table and save the 1rm for all lifts
                //shows a toast for successful save
                if(!maxTableExists){
                    sqLiteHelper.createMaxTable(maxTable);
                    maxModel maxModel = new maxModel();
                    maxModel.setMaxSquat(squatMax.getText().toString());
                    maxModel.setMaxBench(benchMax.getText().toString());
                    maxModel.setMaxDeadlift(deadliftMax.getText().toString());
                    maxModel.setMaxCJ(cjMax.getText().toString());
                    maxModel.setMaxSnatch(snatchMax.getText().toString());
                    maxModel.setMaxPowerClean(powerCleanMax.getText().toString());
                    sqLiteHelper.insertMax(maxModel, maxTable);
                    constraintLayout.requestFocus();
                    Toast toast = Toast.makeText(activity, saveSuccessful, Toast.LENGTH_SHORT);
                    toast.show();
                }

                //if 1rm table already exists, delete table and create a new table with new 1rm
                //shows a toast for successful save
                else{
                    sqLiteHelper.deleteTable(maxTable);
                    sqLiteHelper.createMaxTable(maxTable);
                    maxModel maxModel = new maxModel();
                    maxModel.setMaxSquat(squatMax.getText().toString());
                    maxModel.setMaxBench(benchMax.getText().toString());
                    maxModel.setMaxDeadlift(deadliftMax.getText().toString());
                    maxModel.setMaxCJ(cjMax.getText().toString());
                    maxModel.setMaxSnatch(snatchMax.getText().toString());
                    maxModel.setMaxPowerClean(powerCleanMax.getText().toString());
                    sqLiteHelper.insertMax(maxModel, maxTable);
                    constraintLayout.requestFocus();
                    Toast toast = Toast.makeText(activity, saveSuccessful, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

    }


}
