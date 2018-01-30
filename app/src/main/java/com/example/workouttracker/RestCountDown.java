package com.example.workouttracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.example.workouttracker.CountdownService.COUNTDOWN_SECONDS;

/**
 * Created by Gerald on 12/7/2017.
 */

public class RestCountDown {

    private Home home;
    private DisplayWorkoutAdapter displayWorkoutAdapter;

    //  public constructor
    public RestCountDown(Home home, DisplayWorkoutAdapter displayWorkoutAdapter) {
        this.home = home;
        this.displayWorkoutAdapter = displayWorkoutAdapter;
    }

    //  Method for when countdown is about to start for rest row
    //  assigns the seconds and minutes edittext to secondsTV and minuteTV
    //  registers receiver and starts service for rest countdown
    public void restCountdownSeconds() {

        List<RowType> list = displayWorkoutAdapter.rowTypeList;

        //  if the first child is an exercise row and the second is a rest row
        if (list.get(list.size() - 2).getClass() == RestRowModel.class && list.get(list.size() - 1).getClass() == ExerciseModel.class) {
            displayWorkoutAdapter.secondsTVParent = (RestRowModel) list.get(list.size() - 2);
            displayWorkoutAdapter.secondsTV = displayWorkoutAdapter.secondsTVParent.getSecondsEditText();
            displayWorkoutAdapter.minuteTV = displayWorkoutAdapter.secondsTVParent.getMinutesEditText();
            registerReceiverAndStartService();
        }

        //  if the first child is a rest row
        else if (list.get(list.size() - 1).getClass() == RestRowModel.class) {
            displayWorkoutAdapter.secondsTVParent = (RestRowModel) list.get(list.size() - 1);
            displayWorkoutAdapter.secondsTV = displayWorkoutAdapter.secondsTVParent.getSecondsEditText();
            displayWorkoutAdapter.minuteTV = displayWorkoutAdapter.secondsTVParent.getMinutesEditText();
            registerReceiverAndStartService();
        }
    }

    //  method for when countdown is about to start for superset
    public void restCountdownSecondsSuperset(LinearLayout layout) {

        displayWorkoutAdapter.secondsTV = (EditText) layout.findViewById(R.id.secondsTV);
        displayWorkoutAdapter.minuteTV = (EditText) layout.findViewById(R.id.minutesTV);
        registerReceiverAndStartService();
    }

    //  method for when countdown is about to start for pyramidset
    public void restCountdownSecondsPyramidSet(LinearLayout layout) {
        displayWorkoutAdapter.secondsTV = (EditText) layout.findViewById(R.id.secondsTV);
        displayWorkoutAdapter.minuteTV = (EditText) layout.findViewById(R.id.minutesTV);
        registerReceiverAndStartService();
    }

    //  method responsible for starting service and registering receiver that is used in rest countdown
    //  uses the values from minuteTV and secondsTV and passes it as an extra in an intent for the service
    //  registers the receiver that will receive the service
    private void registerReceiverAndStartService(){
        int min = Integer.valueOf(displayWorkoutAdapter.minuteTV.getText().toString());
        int sec = Integer.valueOf(displayWorkoutAdapter.secondsTV.getText().toString());
        if (!displayWorkoutAdapter.startTimer) {
            displayWorkoutAdapter.startTimer = true;
            home.serviceIntent.putExtra("seconds", sec);
            home.serviceIntent.putExtra("minutes", min);
            home.activity.registerReceiver(secondReceiver, new IntentFilter(COUNTDOWN_SECONDS));
            home.activity.startService(home.serviceIntent);
        }
    }

    //  broadcast receiver for the seconds
    public BroadcastReceiver secondReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {

            List<RowType> list = displayWorkoutAdapter.rowTypeList;
            displayWorkoutAdapter.startTimer = true;

            //  retrieves all the shared pref related to countdown timer
            //  inputtedMin and inputtedSec are the values for the timer before it started
            //  minCountdown value is the decremented value for the minutes, if minutes has a value above 0
            final SharedPreferences minPref = context.getSharedPreferences("min", Context.MODE_PRIVATE);
            final SharedPreferences secPref = context.getSharedPreferences("sec", Context.MODE_PRIVATE);
            final SharedPreferences countdownMin = context.getSharedPreferences("countdownMin", Context.MODE_PRIVATE);
            final int decrementedMin = countdownMin.getInt("minCountdown", -1);
            final int inputtedMin = minPref.getInt("inputtedMin", -1);
            final int inputtedSec = secPref.getInt("inputtedSec", -1);

            //  gets each countdown sec
            long millis = intent.getLongExtra("countdownSeconds", -1);

            //  updates the second text every second with value of millis
            displayWorkoutAdapter.secondsTV.setText(String.format("%s", millis / 1000));

            //  if minutes on timer is decremented, set minuteTV to new decremented minute
            //  Example: timer went from 6:00 to 5:59
            if(decrementedMin != -1){
                SaveChanges saveChanges = new SaveChanges(home, displayWorkoutAdapter);

                   if(list.get(displayWorkoutAdapter.getItemCount() - 1).getClass() == SupersetRowModel.class){
                       RestRowModel restRowModel = new RestRowModel();
                       restRowModel.setMinutes(String.format("%s", decrementedMin));
                       ((SupersetRowModel) list.get(list.size() - 1)).setRestRowModel(restRowModel);
                       saveChanges.saveToDB("workoutPause", list, true, null, "decrementSuperset", context);
                   }
                   else if(list.get(displayWorkoutAdapter.getItemCount() - 1).getClass() == PyramidsetRowModel.class){
                       RestRowModel restRowModel = new RestRowModel();
                       restRowModel.setMinutes(String.format("%s", decrementedMin));
                       ((PyramidsetRowModel) list.get(list.size() - 1)).setRestRowModel(restRowModel);
                       saveChanges.saveToDB("workoutPause", list, true, null, "decrementPyramidset", context);
                   }
                displayWorkoutAdapter.minuteTV.setText(String.format("%s", decrementedMin));
                countdownMin.edit().clear().apply();
            }

            //  if timer is completely finished
            if (inputtedMin != -1 || inputtedSec != -1) {

                //  if timer is started for just rest row, delete the shared prefs for the original min and sec, remove rest, and unregister receiver
                if (list.get(list.size() - 1).getClass() == RestRowModel.class) {
                    displayWorkoutAdapter.startTimer = false;
                    minPref.edit().clear().apply();
                    secPref.edit().clear().apply();
                    list.remove(list.size() - 1);
                    displayWorkoutAdapter.notifyItemRemoved(displayWorkoutAdapter.rowTypeList.size() - 1);
                    context.unregisterReceiver(secondReceiver);

                }

                //  if timer is started for a exerciseRow
                else if (list.get(list.size() - 1).getClass() == ExerciseModel.class && list.get(list.size() - 2).getClass() == RestRowModel.class) {
                    ExerciseModel exerciseModel = (ExerciseModel) list.get(list.size() - 1);
                    EditText sets = exerciseModel.getSetsEditText();
                    int setsInt = Integer.parseInt(sets.getText().toString());
                    /*
                       If timer is finished and value of set is above 1: reset the rest, but don't start timer, stop service, and unregister receivers
                       If timer is finished and value of set is 1: remove rest row, stop service, and unregister receiver
                       Delete the shared prefs related to the starting value of min and sec
                    */
                    displayWorkoutAdapter.startTimer = false;
                    minPref.edit().clear().apply();
                    secPref.edit().clear().apply();
                    if (setsInt > 1) {
                        displayWorkoutAdapter.minuteTV.setText(String.format("%s", inputtedMin));
                        displayWorkoutAdapter.secondsTV.setText(String.format("%s", inputtedSec));
                        /*((RestRowModel) list.get(list.size() - 2)).setMinutes(String.format("%s", inputtedMin));
                           ((RestRowModel) list.get(list.size() - 2)).setSeconds(String.format("%s", inputtedSec));
                           SaveChanges saveChanges = new SaveChanges(home, displayWorkoutAdapter);
                           saveChanges.saveToDB("workoutPause", list, true, null, "exerciseRowChange", context);*/
                        context.unregisterReceiver(secondReceiver);
                    } else {
                        list.remove(list.size() - 2);
                        displayWorkoutAdapter.notifyItemRemoved(displayWorkoutAdapter.getItemCount() - 2);
                        /*SaveChanges saveChanges = new SaveChanges(home, displayWorkoutAdapter);
                           saveChanges.saveToDB("workoutPause", list, true, null, "exerciseRowRemove", context);*/
                        context.unregisterReceiver(secondReceiver);
                    }
                }

                //  if timer is started for a pyramid set
                //  if pyramid set has more than 2 children, reset timer with starting min and sec values and save to Firebase
                //  if pyramid set does not have more than 2 children, delete timer, and save to Firebase
                //  unregister receiver in both instances and clear the shared prefs for starting min and sec values
                else if (list.get(list.size() - 1).getClass() == PyramidsetRowModel.class) {
                    PyramidsetRowModel pyramidsetRowModel = (PyramidsetRowModel) list.get(list.size() - 1);
                    LinearLayout layout = pyramidsetRowModel.getLinearLayout();
                    boolean removeView = true;
                    displayWorkoutAdapter.startTimer = false;
                    if (layout.getChildCount() > 2) {
                        removeView = false;

                        displayWorkoutAdapter.minuteTV.setText(String.format("%s", inputtedMin));
                        displayWorkoutAdapter.secondsTV.setText(String.format("%s", inputtedSec));

                        RestRowModel restRowModel = new RestRowModel();
                        restRowModel.setSeconds(String.format("%s", inputtedSec));
                        restRowModel.setMinutes(String.format("%s", inputtedMin));
                        ((PyramidsetRowModel) list.get(list.size() - 1)).setRestRowModel(restRowModel);
                        SaveChanges saveChanges = new SaveChanges(home, displayWorkoutAdapter);
                        saveChanges.saveToDB("workoutPause", list, true, null, "pyramidsetChange", context);
                    }
                    if (removeView) {
                        pyramidsetRowModel.setNumberOfChildren(pyramidsetRowModel.getNumberOfChildren() - 1);
                        layout.removeViewAt(layout.getChildCount() - 1);
                        pyramidsetRowModel.setRestRowModel(null);
                        pyramidsetRowModel.setAddRest(false);
                    }
                    context.unregisterReceiver(secondReceiver);
                    minPref.edit().clear().apply();
                    secPref.edit().clear().apply();
                }

                //  if timer is started for superset
                //  unregister receiver
                else if (list.get(list.size() - 1).getClass() == SupersetRowModel.class) {
                    boolean removeView = true;
                    SupersetRowModel supersetRowModel = (SupersetRowModel) list.get(list.size() - 1);
                    final LinearLayout layout = supersetRowModel.getLinearLayout();
                    displayWorkoutAdapter.startTimer = false;

                    //  iterates through all the exercises in the superset row
                    //  if 1 of them has set > 1, don't remove timer, and reset timer
                    //  else remove timer
                    for (int i = 0; i < layout.getChildCount() - 1; i++) {
                        View exerciseView = layout.getChildAt(i);
                        EditText sets = (EditText) exerciseView.findViewById(R.id.sets);
                        int setInt = Integer.valueOf(sets.getText().toString());
                        if (setInt > 1) {
                            removeView = false;
                        }
                        supersetRowModel.getExerciseModelList().get(i).setSets(sets.getText().toString());
                    }

                    //  removes the timer
                    if (removeView) {
                        RestRowModel restRowModel = supersetRowModel.getRestRowModel();
                        if(restRowModel != null){
                            supersetRowModel.setNumberOfChildren(layout.getChildCount() - 1);
                            supersetRowModel.getLinearLayout().removeViewAt(supersetRowModel.getLinearLayout().getChildCount() - 1);
                            supersetRowModel.setRestRowModel(null);
                            supersetRowModel.setAddRest(false);
                        }
                    }

                    //  resets timer with starting min and sec values
                    else {
                        displayWorkoutAdapter.minuteTV.setText(String.format("%s", inputtedMin));
                        displayWorkoutAdapter.secondsTV.setText(String.format("%s", inputtedSec));
                        RestRowModel restRowModel = new RestRowModel();
                        restRowModel.setSeconds(String.format("%s", inputtedSec));
                        restRowModel.setMinutes(String.format("%s", inputtedMin));
                        ((SupersetRowModel) list.get(list.size() - 1)).setRestRowModel(restRowModel);
                    }

                    //  clear the shared prefs for starting min and sec values
                    //  save to Firebase and unregister receiver
                    minPref.edit().clear().apply();
                    secPref.edit().clear().apply();
                    SaveChanges saveChanges = new SaveChanges(home, displayWorkoutAdapter);
                    saveChanges.saveToDB("workoutPause", list, true, null, "supersetRowChange", context);
                    context.unregisterReceiver(secondReceiver);
                }
            }
        }
    };


}
