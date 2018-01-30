package com.example.workouttracker;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.example.workouttracker.CountdownService.COUNTDOWN_SECONDS;

/**
 * Created by Gerald on 11/9/2017.
 */

public class DisplayWorkoutAdapter extends RecyclerView.Adapter implements ItemTouchHelperAdapter{

    public List<RowType> rowTypeList;
    private Home home;
    private final OnStartDragListener mDragStartListener;
    public boolean startClicked;
    public boolean stopClicked = true;
    public boolean updatePositionOnButtonClicked;
    public boolean addExerciseClicked;
    public boolean addRestClicked;
    public boolean addSupersetClicked;
    public boolean addPyramidsetClicked;
    private SaveChanges saveChanges;
    private StopWorkout stopWorkout;
    public int index;
    public boolean startTimer;
    public RestRowModel secondsTVParent;
    public EditText secondsTV;
    public EditText minuteTV;
    public boolean swipeListener;
    public boolean inputError;
    public RestCountDown restCountDown;
    private DecreaseSetEditText decreaseSetEditText;
    private DatabaseReference loadedWorkoutReference;

    public static class ExerciseRowHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder{

        public ImageView dragHandle;
        public EditText weight;
        public EditText set;
        public EditText rep;
        public AutoCompleteTextView exercise;
        public ExerciseRowTextWatcher exerciseRowTextWatcher;
        public ConstraintLayout constraintLayout;

        public ExerciseRowHolder(View view, DisplayWorkoutAdapter displayWorkoutAdapter){
            super(view);

            dragHandle = view.findViewById(R.id.reorder);
            weight = view.findViewById(R.id.weight);
            set = view.findViewById(R.id.sets);
            rep = view.findViewById(R.id.reps);
            exercise = view.findViewById(R.id.autoCompleteTextView);
            constraintLayout = view.findViewById(R.id.exercise_row);

            exerciseRowTextWatcher = new ExerciseRowTextWatcher(displayWorkoutAdapter, exercise, weight, set, rep);
        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

        }

        //  updates position in adapter
        public void updatePosition(int position){
            exerciseRowTextWatcher.updatePosition(position);
        }

    }

    public static class ButtonHolder extends RecyclerView.ViewHolder{
        public Button start;
        public Button addExercise;
        public Button addRest;
        public Button addSuperset;
        public Button addPyramidSet;
        public Button stop;

        public ButtonHolder(View view, final DisplayWorkoutAdapter displayWorkoutAdapter){
            super(view);


            start = view.findViewById(R.id.start_workout);
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    displayWorkoutAdapter.saveChanges.startClicked();
                }
            });

            stop = view.findViewById(R.id.stop_workout);

            //  load workout with name of title
            //  display notification with time elapsed and create time table if one doesn't exist
            stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    displayWorkoutAdapter.stopClicked = true;
                    displayWorkoutAdapter.startClicked = false;
                    displayWorkoutAdapter.home.activity.loadWorkout(displayWorkoutAdapter.home.activity.getTitle().toString());
                    displayWorkoutAdapter.stopWorkout.setWorkoutTime();
                }
            });

            /*
                For all the buttons below, if a workout name has not been chosen, display an alert dialog asking for workout name
                Then it will add the corresponding viewholder to rowTypeList
            */

            addExercise = view.findViewById(R.id.addNew);
            addExercise.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!displayWorkoutAdapter.home.titleSet) {
                        displayWorkoutAdapter.addExerciseClicked = true;
                        displayWorkoutAdapter.saveChanges.setActivityTitle();
                    }
                    else{
                        displayWorkoutAdapter.addExerciseClicked = true;
                        displayWorkoutAdapter.saveChanges.buttonClicked();
                    }
                }
            });

            addRest = view.findViewById(R.id.addRest);
            addRest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!displayWorkoutAdapter.home.titleSet) {
                        displayWorkoutAdapter.addRestClicked = true;
                        displayWorkoutAdapter.saveChanges.setActivityTitle();
                    }
                    else{
                        displayWorkoutAdapter.addRestClicked = true;
                        displayWorkoutAdapter.saveChanges.buttonClicked();
                    }
                }
            });

            addSuperset = view.findViewById(R.id.supersetButton);
            addSuperset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!displayWorkoutAdapter.home.titleSet) {
                        displayWorkoutAdapter.addSupersetClicked = true;
                        displayWorkoutAdapter.saveChanges.setActivityTitle();
                    }
                    else{
                        displayWorkoutAdapter.addSupersetClicked = true;
                        displayWorkoutAdapter.saveChanges.buttonClicked();
                    }
                    }
            });

            addPyramidSet = view.findViewById(R.id.addPyramid);
            addPyramidSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!displayWorkoutAdapter.home.titleSet) {
                        displayWorkoutAdapter.addPyramidsetClicked = true;
                        displayWorkoutAdapter.saveChanges.setActivityTitle();
                    }
                    else{
                        displayWorkoutAdapter.addPyramidsetClicked = true;
                        displayWorkoutAdapter.saveChanges.buttonClicked();
                    }
                }
            });

        }

        public void startClicked(){
            addExercise.setVisibility(View.GONE);
            addRest.setVisibility(View.GONE);
            addSuperset.setVisibility(View.GONE);
            addPyramidSet.setVisibility(View.GONE);
            start.setVisibility(View.GONE);
            stop.setVisibility(View.VISIBLE);
        }


        public void stopClicked(){
            addExercise.setVisibility(View.VISIBLE);
            addRest.setVisibility(View.VISIBLE);
            addSuperset.setVisibility(View.VISIBLE);
            addPyramidSet.setVisibility(View.VISIBLE);
            start.setVisibility(View.VISIBLE);
            stop.setVisibility(View.GONE);
        }


    }

    public static class RestRowHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder{

        public EditText minutes;
        public EditText seconds;
        public ImageView dragHandle;
        public RestRowTextWatcher restRowTextWatcher;
        public ConstraintLayout constraintLayout;

        public RestRowHolder(View view, DisplayWorkoutAdapter displayWorkoutAdapter){
            super(view);

            dragHandle = view.findViewById(R.id.reorder);
            minutes = view.findViewById(R.id.minutesTV);
            seconds = view.findViewById(R.id.secondsTV);
            constraintLayout = view.findViewById(R.id.rest_row);

            restRowTextWatcher = new RestRowTextWatcher(displayWorkoutAdapter, minutes, seconds);

        }

        @Override
        public void onItemSelected() {
            //constraintLayout.getBackground().setAlpha(127);
        }

        @Override
        public void onItemClear() {
            //constraintLayout.getBackground().setAlpha(255);
        }

        public void updatePosition(int position){
            restRowTextWatcher.updatePosition(position);
        }
    }

    public static class SupersetRowHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder{
        public LinearLayout supersetContainer;
        public SupersetExerciseRowTextWatcher supersetExerciseRowTextWatcher;
        public SupersetRestRowTextWatcher supersetRestRowTextWatcher;

        public SupersetRowHolder(View view){
            super(view);

            supersetContainer = view.findViewById(R.id.supersetLayout);
        }

        @Override
        public void onItemSelected() {
            //itemView.getBackground().setAlpha(127);
        }

        @Override
        public void onItemClear() {
            //itemView.getBackground().setAlpha(255);
        }

        public void setTextWatcherSupersetExRow(DisplayWorkoutAdapter displayWorkoutAdapter, List<ExerciseModel> exerciseModelList, AutoCompleteTextView exercise, EditText weight, EditText sets, EditText reps, int listPos){
            supersetExerciseRowTextWatcher = new SupersetExerciseRowTextWatcher(displayWorkoutAdapter, exerciseModelList, exercise, weight, sets, reps, listPos);
        }

        public void setTextWatcherSupersetRestRow(DisplayWorkoutAdapter displayWorkoutAdapter, RestRowModel restRowModel, EditText minutes, EditText seconds){
            supersetRestRowTextWatcher = new SupersetRestRowTextWatcher(displayWorkoutAdapter, restRowModel, minutes, seconds);
        }

        public void updatePosition(int position){
            supersetExerciseRowTextWatcher.updatePosition(position);
        }


    }


    public static class PyramidRowHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder{
        public LinearLayout pyramidContainer;
        public PyramidsetRestRowTextWatcher pyramidsetRestRowTextWatcher;
        public PyramidsetExerciseRowTextWatcher pyramidsetExerciseRowTextWatcher;

        public PyramidRowHolder(View view){
            super(view);

            pyramidContainer = view.findViewById(R.id.pyramidContainer);
        }

        @Override
        public void onItemSelected() {
            //pyramidContainer.getBackground().setAlpha(127);
            /*int numChildren = pyramidContainer.getChildCount();
            for(int i = 0; i < numChildren; i++){
                ConstraintLayout constraintLayout = (ConstraintLayout) pyramidContainer.getChildAt(i);
                constraintLayout.getBackground().setAlpha(127);
            }*/
        }

        @Override
        public void onItemClear() {
            //pyramidContainer.getBackground().setAlpha(255);
            /*int numChildren = pyramidContainer.getChildCount();
            for(int i = 0; i < numChildren; i++){
                View view = pyramidContainer.getChildAt(i);
                view.getBackground().setAlpha(255);
            }*/
        }

        public void setTextWatcherPyramidsetExRow(DisplayWorkoutAdapter displayWorkoutAdapter, List<ExerciseModel> exerciseModelList, AutoCompleteTextView exercise, EditText weight, EditText sets, EditText reps, int listPos){
            pyramidsetExerciseRowTextWatcher = new PyramidsetExerciseRowTextWatcher(displayWorkoutAdapter, exerciseModelList, exercise, weight, sets, reps, listPos);
        }

        public void setTextWatcherPyramidsetRestRow(DisplayWorkoutAdapter displayWorkoutAdapter, RestRowModel restRowModel, EditText minutes, EditText seconds){
            pyramidsetRestRowTextWatcher = new PyramidsetRestRowTextWatcher(displayWorkoutAdapter, restRowModel, minutes, seconds);
        }
    }

    @Override
    public void nextSet(){

        /*
            Gets a sharedPref that is used as the current position you are at in terms in supersets and pyramid sets
            If there is no existing sharedPref, index is 0
            If there is existing sharedPref, use that value as index
         */

        SharedPreferences pref = home.activity.getSharedPreferences("swipeIndex", Context.MODE_PRIVATE);
        if(pref.getInt("index", -1) == -1){
            index = 0;
        }
        else{
            index = pref.getInt("index", -1);
        }


        if(!startTimer) {

            //  If exercise you are currently on is a superset, and if a rest timer has not been started and start has been clicked
            if (rowTypeList.get(rowTypeList.size() - 1).getClass() == SupersetRowModel.class) {

                LayoutInflater layoutInflater = home.activity.getLayoutInflater();
                View restRow = layoutInflater.inflate(R.layout.rest_row, null);

                SupersetRowModel supersetRowModel = (SupersetRowModel) rowTypeList.get(rowTypeList.size() - 1);
                LinearLayout layout = supersetRowModel.getLinearLayout();
                int position = layout.getChildCount();

                //  if rest row exists and current position is not the last exercise, decrease the set of exercise at current position
                if (index != position - 2 && layout.getChildAt(position - 1).getId() == restRow.getId()) {
                    decreaseSetEditText.decreaseSuperset(index, layout, pref);
                }

                /*
                    if rest row exists and current position is the last exercise
                    decrease the set of that exercise
                    start rest countdown
                */
                else if (index == position - 2 && layout.getChildAt(index + 1).getId() == restRow.getId()) {

                    decreaseSetEditText.decreaseSuperset(index, layout, pref);

                    restCountDown.restCountdownSecondsSuperset(layout);

                }

                /*
                    if rest row does not exist
                    decrease set of that exercise
                */

                else if (layout.getChildAt(position - 1).getId() != restRow.getId()) {

                    decreaseSetEditText.decreaseSuperset(index, layout, pref);
                }
            }


            /*
                If exercise you are currently on is a pyramidset, and if a rest timer has not been started and start has been clicked
                remove set you are on
            */

            else if(rowTypeList.get(getItemCount() - 1).getClass() == PyramidsetRowModel.class){

                LayoutInflater layoutInflater = home.activity.getLayoutInflater();
                View restRow = layoutInflater.inflate(R.layout.rest_row, null);

                PyramidsetRowModel pyramidsetRowModel = (PyramidsetRowModel) rowTypeList.get(getItemCount() - 1);
                LinearLayout layout = pyramidsetRowModel.getLinearLayout();

                decreaseSetEditText.decreasePyramidSet(layout);

                //  if pyramid set has 2+ children and the last child is a rest row, start rest timer
                if(layout.getChildCount() >= 2) {
                    if (layout.getChildAt(layout.getChildCount() - 1).getId() == restRow.getId()) {
                        restCountDown.restCountdownSecondsPyramidSet(layout);
                    }
                }
            }


            /*
                If current position is either exercise row or rest row and rest timer has not been started, and start has been clicked
                reset index, decrease set and start rest timer if needed
            */
            else {
                index = 0;
                pref.edit().clear().apply();
                restCountDown.restCountdownSeconds();
                decreaseSetEditText.decreaseSet();
            }
        }
    }

    @Override
    public void onItemDismiss(RecyclerView.ViewHolder viewHolder, final int position) {

        /*
            For all viewholders, when dismissed, remove from rowTypeList, show undo snackbar, and update positions of text watchers
        */

        if(viewHolder instanceof DisplayWorkoutAdapter.ExerciseRowHolder){
            ExerciseModel exerciseModel = (ExerciseModel) rowTypeList.get(position);

            rowTypeList.remove(position);
            notifyItemRemoved(position);
            createSnackbar(position, exerciseModel, "Exercise deleted");
            updatePosition();
        }

        else if(viewHolder instanceof DisplayWorkoutAdapter.RestRowHolder){
            RestRowModel restRowModel = (RestRowModel) rowTypeList.get(position);

            rowTypeList.remove(position);
            notifyItemRemoved(position);
            createSnackbar(position, restRowModel, "Rest deleted");
            updatePosition();
        }

        else if(viewHolder instanceof DisplayWorkoutAdapter.SupersetRowHolder){
            SupersetRowModel supersetRowModel = (SupersetRowModel) rowTypeList.get(position);
            rowTypeList.remove(position);
            notifyItemRemoved(position);
            ((SupersetRowHolder) viewHolder).supersetContainer.removeAllViews();
            createSnackbar(position, supersetRowModel, "Superset deleted");
            updatePosition();
        }

        else if(viewHolder instanceof DisplayWorkoutAdapter.PyramidRowHolder){
            PyramidsetRowModel pyramidsetRowModel = (PyramidsetRowModel) rowTypeList.get(position);
            rowTypeList.remove(position);
            notifyItemRemoved(position);
            ((PyramidRowHolder) viewHolder).pyramidContainer.removeAllViews();
            createSnackbar(position, pyramidsetRowModel, "Pyramidset deleted");
            updatePosition();
        }

        /*snackbar.addCallback(new Snackbar.Callback() {

            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                home.myRef.child("savedWorkoutTempDelete").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
            }

            @Override
            public void onShown(Snackbar snackbar) {

            }
        });*/
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {


        //  Swaps positions of viewholders and updates the positions of the textwatchers


        Collections.swap(rowTypeList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        updatePosition();

        return true;
    }

    public DisplayWorkoutAdapter (List<RowType> rowType, Home home, OnStartDragListener onStartDragListener){

        this.home = home;
        mDragStartListener = onStartDragListener;
        this.rowTypeList = rowType;

        saveChanges = new SaveChanges(this.home, this);
        stopWorkout = new StopWorkout(this.home, this);
        restCountDown = new RestCountDown(this.home, this);
        decreaseSetEditText = new DecreaseSetEditText(this, this.home);

        //  used to keep track of the current position in superset or pyramidset
        index = 0;
    }



    @Override
    public int getItemViewType(int position){
        if(rowTypeList.get(position) instanceof ExerciseModel){
            return RowType.EXERCISE_ROW;
        }

        else if(rowTypeList.get(position) instanceof RestRowModel){
            return RowType.REST_ROW;
        }

        else if(rowTypeList.get(position) instanceof SupersetRowModel){
            return RowType.SUPERSET_ROW;
        }

        else if(rowTypeList.get(position) instanceof PyramidsetRowModel){
            return RowType.PYRAMIDSET_ROW;
        }

        else if(rowTypeList.get(position) instanceof ButtonsHolder){
            return RowType.BUTTON_ROW;
        }


        else{
            return -1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(viewType == RowType.EXERCISE_ROW){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
            return new ExerciseRowHolder(view, this);
        }
        else if(viewType == RowType.REST_ROW){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rest_row, parent, false);
            return new RestRowHolder(view, this);
        }
        else if(viewType == RowType.SUPERSET_ROW){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.superset, parent, false);
            return new SupersetRowHolder(view);
        }
        else if(viewType == RowType.PYRAMIDSET_ROW){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pyramidset, parent, false);
            return new PyramidRowHolder(view);
        }
        else if(viewType == RowType.BUTTON_ROW){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.button_holder, parent, false);
            return new ButtonHolder(view, this);
        }
        else{
            return null;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position){


        /*
            Holder for exercise row
            Sets the text for exercise, weight, sets, and reps
            Disables text if save button is clicked
        */

        if(holder instanceof ExerciseRowHolder) {

            int pos = holder.getAdapterPosition();

            //  sets the edit text and text watcher
            ((ExerciseModel) rowTypeList.get(pos)).setExerciseEditText(((ExerciseRowHolder) holder).exercise);
            ((ExerciseModel) rowTypeList.get(pos)).setWeightEditText(((ExerciseRowHolder) holder).weight);
            ((ExerciseModel) rowTypeList.get(pos)).setSetsEditText(((ExerciseRowHolder) holder).set);
            ((ExerciseModel) rowTypeList.get(pos)).setRepsEditText(((ExerciseRowHolder) holder).rep);
            ((ExerciseModel) rowTypeList.get(pos)).setDragHandle(((ExerciseRowHolder) holder).dragHandle);

            //  updates position of text watcher
            ((ExerciseRowHolder) holder).updatePosition(pos);


            ((ExerciseRowHolder) holder).exercise.setText(((ExerciseModel) rowTypeList.get(pos)).getExercise());
            ((ExerciseRowHolder) holder).weight.setText(((ExerciseModel) rowTypeList.get(pos)).getWeight());
            ((ExerciseRowHolder) holder).set.setText(((ExerciseModel) rowTypeList.get(pos)).getSets());
            ((ExerciseRowHolder) holder).rep.setText(((ExerciseModel) rowTypeList.get(pos)).getReps());
            ((ExerciseRowHolder) holder).exercise.setAdapter(getExerciseAdapter(home.getContext()));

            //  if any edit text has an error, show textinputlayout error
            if(((ExerciseModel) rowTypeList.get(pos)).isExerciseError()){
                TextInputLayout textInputLayout = getFirstParent(((ExerciseRowHolder) holder).exercise, TextInputLayout.class);
                if(textInputLayout != null){
                    textInputLayout.setError("Exercise");
                }
            }

            if(((ExerciseModel) rowTypeList.get(pos)).isWeightError()){
                TextInputLayout textInputLayout = getFirstParent(((ExerciseRowHolder) holder).weight, TextInputLayout.class);
                if(textInputLayout != null){
                    textInputLayout.setError("Weight");
                }
            }

            if(((ExerciseModel) rowTypeList.get(pos)).isSetError()){
                TextInputLayout textInputLayout = getFirstParent(((ExerciseRowHolder) holder).set, TextInputLayout.class);
                if(textInputLayout != null){
                    textInputLayout.setError("Set");
                }
            }

            if(((ExerciseModel) rowTypeList.get(pos)).isRepError()){
                TextInputLayout textInputLayout = getFirstParent(((ExerciseRowHolder) holder).rep, TextInputLayout.class);
                if(textInputLayout != null){
                    textInputLayout.setError("Rep");
                }
            }

            ((ExerciseModel) rowTypeList.get(pos)).setExerciseRowTextWatcher(((ExerciseRowHolder) holder).exerciseRowTextWatcher);


            if(updatePositionOnButtonClicked){
                updatePosition();
                updatePositionOnButtonClicked = false;
            }

            ((ExerciseRowHolder) holder).dragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(holder);
                    }
                    return false;
                }
            });

            //  if start is clicked, convert percentage weight if needed and disable text and draghandle
            if(((ExerciseModel) rowTypeList.get(pos)).isDisableText()){
                convert1RMPercent(((ExerciseRowHolder) holder).exercise, ((ExerciseRowHolder) holder).weight);
                if(((ExerciseRowHolder) holder).weight.getText().toString().isEmpty()){
                    TextInputLayout textInputLayout = getFirstParent(((ExerciseRowHolder) holder).weight, TextInputLayout.class);
                    textInputLayout.setHint("");
                }
                home.exDrawable = ((ExerciseRowHolder) holder).exercise.getBackground();
                home.weightDrawable = ((ExerciseRowHolder) holder).weight.getBackground();
                home.setDrawable = ((ExerciseRowHolder) holder).set.getBackground();
                home.repDrawable = ((ExerciseRowHolder) holder).rep.getBackground();
                disableEditText(((ExerciseRowHolder) holder).exercise, ((ExerciseRowHolder) holder).weight, ((ExerciseRowHolder) holder).set, ((ExerciseRowHolder) holder).rep);
                ((ExerciseRowHolder) holder).dragHandle.setVisibility(View.INVISIBLE);
            }

            setSwipeListener(((ExerciseRowHolder) holder).exercise, ((ExerciseRowHolder) holder).weight, ((ExerciseRowHolder) holder).set, ((ExerciseRowHolder) holder).rep);


        }


        /*
            Holder for rest row
            Sets the text for the minutes and seconds
            Disables edit text if save is clicked
        */

        else if(holder instanceof RestRowHolder){

            int pos = holder.getAdapterPosition();


            ((RestRowHolder) holder).updatePosition(pos);
            ((RestRowModel) rowTypeList.get(pos)).setMinutesEditText(((RestRowHolder) holder).minutes);
            ((RestRowModel) rowTypeList.get(pos)).setSecondsEditText(((RestRowHolder) holder).seconds);
            ((RestRowModel) rowTypeList.get(pos)).setDragHandle(((RestRowHolder) holder).dragHandle);


            ((RestRowHolder) holder).minutes.setText(((RestRowModel) rowTypeList.get(pos)).getMinutes());
            ((RestRowHolder) holder).seconds.setText(((RestRowModel) rowTypeList.get(pos)).getSeconds());


            if(((RestRowModel) rowTypeList.get(pos)).isMinuteError()){
                TextInputLayout textInputLayout = getFirstParent(((RestRowHolder) holder).minutes, TextInputLayout.class);
                if(textInputLayout != null){
                    textInputLayout.setError("Minutes");
                }
            }

            if(((RestRowModel) rowTypeList.get(pos)).isSecondError()){
                TextInputLayout textInputLayout = getFirstParent(((RestRowHolder) holder).seconds, TextInputLayout.class);
                if(textInputLayout != null){
                    textInputLayout.setError("Seconds");
                }
            }

            ((RestRowModel) rowTypeList.get(pos)).setRestRowTextWatcher(((RestRowHolder) holder).restRowTextWatcher);

            if(updatePositionOnButtonClicked){
                updatePosition();
                updatePositionOnButtonClicked = false;
            }

            ((RestRowHolder) holder).dragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(holder);
                    }
                    return false;
                }
            });


            if(((RestRowModel) rowTypeList.get(pos)).isDisableText()){
                home.minDrawable = ((RestRowHolder) holder).minutes.getBackground();
                home.secDrawable = ((RestRowHolder) holder).seconds.getBackground();
                disableEditText(((RestRowHolder) holder).minutes, ((RestRowHolder) holder).seconds);
                ((RestRowHolder) holder).dragHandle.setVisibility(View.INVISIBLE);
            }

            setSwipeListener(((RestRowHolder) holder).minutes, ((RestRowHolder) holder).seconds);
        }

        /*
            Holder for superset
            When superset is first created, it will set the data to supersetRowModel and the text watcher
            If superset is already created, it will get the data from the model and set it to the edittexts
        */

        else if(holder instanceof SupersetRowHolder){

            int pos = holder.getAdapterPosition();

            int numberOfChildren = ((SupersetRowModel) rowTypeList.get(pos)).getNumberOfChildren();
            boolean addRest = ((SupersetRowModel) rowTypeList.get(position)).getAddRest();
            boolean loadedSuperset = ((SupersetRowModel) rowTypeList.get(position)).getLoadedSuperset();

            LayoutInflater layoutInflater = home.getLayoutInflater();
            View exerciseView = layoutInflater.inflate(R.layout.row, null);
            View restView = layoutInflater.inflate(R.layout.rest_row, null);

            //  super set is first created, or superset is loaded
            //  add the appropriate number of exercises, and rest depending on if add rest is called
            if(((SupersetRowHolder) holder).supersetContainer.getChildCount() == 0){
                List<ExerciseModel> exerciseModelList;

                //  if superset is being created, create a new arraylist
                //  if superset is being loaded, get existing exercise model list
                if(!loadedSuperset) {
                    exerciseModelList = new ArrayList<>();
                }
                else{
                    exerciseModelList = ((SupersetRowModel) rowTypeList.get(pos)).getExerciseModelList();
                }

                ((SupersetRowModel) rowTypeList.get(pos)).setLinearLayout(((SupersetRowHolder) holder).supersetContainer);

                /*
                    iterate through all the children of the superset
                    for each child excluding the last, add an exercise row to superset container
                    for last child, add either an exercise row or rest row, depending on if rest is added
                    text watcher is set for each edit text in each row
                */

                for(int i = 0; i < numberOfChildren; i++){

                    if(i < numberOfChildren - 1){

                        View exerciseRow = layoutInflater.inflate(R.layout.row, null);

                        ImageView dragHandleExercise = exerciseRow.findViewById(R.id.reorder);
                        AutoCompleteTextView exercise = exerciseRow.findViewById(R.id.autoCompleteTextView);
                        EditText weight = exerciseRow.findViewById(R.id.weight);
                        EditText sets = exerciseRow.findViewById(R.id.sets);
                        EditText reps = exerciseRow.findViewById(R.id.reps);

                        exercise.setAdapter(getExerciseAdapter(home.getContext()));

                        if(!loadedSuperset) {
                            ExerciseModel exerciseModel = new ExerciseModel();
                            exerciseModel.setExerciseEditText(exercise);
                            exerciseModel.setWeightEditText(weight);
                            exerciseModel.setSetsEditText(sets);
                            exerciseModel.setRepsEditText(reps);
                            exerciseModel.setDragHandle(dragHandleExercise);
                            ((SupersetRowModel) rowTypeList.get(pos)).setExerciseModelList(exerciseModelList);
                            exerciseModelList.add(i, exerciseModel);
                        }
                        else{
                            ExerciseModel exerciseModel = exerciseModelList.get(i);

                            exerciseModel.setExerciseEditText(exercise);
                            exerciseModel.setWeightEditText(weight);
                            exerciseModel.setSetsEditText(sets);
                            exerciseModel.setRepsEditText(reps);
                            exerciseModel.setDragHandle(dragHandleExercise);

                            exercise.setText(exerciseModel.getExercise());
                            weight.setText(exerciseModel.getWeight());
                            sets.setText(exerciseModel.getSets());
                            reps.setText(exerciseModel.getReps());

                            if(exerciseModel.isExerciseError()){
                                TextInputLayout textInputLayout = getFirstParent(exercise, TextInputLayout.class);
                                textInputLayout.setError("Exercise");
                            }

                            if(exerciseModel.isWeightError()){
                                TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                                textInputLayout.setError("Weight");
                            }

                            if(exerciseModel.isSetError()){
                                TextInputLayout textInputLayout = getFirstParent(sets, TextInputLayout.class);
                                textInputLayout.setError("Set");
                            }

                            if(exerciseModel.isRepError()){
                                TextInputLayout textInputLayout = getFirstParent(reps, TextInputLayout.class);
                                textInputLayout.setError("Rep");
                            }

                        }

                        ((SupersetRowHolder) holder).setTextWatcherSupersetExRow(this, ((SupersetRowModel) rowTypeList.get(pos)).getExerciseModelList(), exercise, weight, sets, reps, i);

                        //  disable drag handle for all rows after the first
                        if(i > 0){
                            dragHandleExercise.setVisibility(View.INVISIBLE);
                        }

                        if(weight.getText().toString().isEmpty()){
                            TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                            textInputLayout.setHint("Weight");
                        }

                        dragHandleExercise.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                    mDragStartListener.onStartDrag(holder);
                                }
                                return false;
                            }
                        });

                        //  if start is clicked, disable all edit texts and hide the drag handle for the first row
                        if(((SupersetRowModel) rowTypeList.get(pos)).isDisableText()){
                            convert1RMPercent(exercise, weight);
                            disableEditText(exercise, weight, reps, sets);
                            if(i == 0){
                                dragHandleExercise.setVisibility(View.INVISIBLE);
                            }
                            if(weight.getText().toString().isEmpty()){
                                TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                                textInputLayout.setHint("");
                            }
                        }

                        setSwipeListener(exercise, weight, sets, reps);

                        ((SupersetRowHolder) holder).supersetContainer.addView(exerciseRow);
                    }

                    //  for last child, add rest row or exercise row, depending on if addRest is true
                    else if(i == numberOfChildren - 1) {
                        if (addRest) {

                            View restRow = layoutInflater.inflate(R.layout.rest_row, null);
                            ImageView dragHandleRest = restRow.findViewById(R.id.reorder);
                            EditText minutes = restRow.findViewById(R.id.minutesTV);
                            EditText seconds = restRow.findViewById(R.id.secondsTV);


                            if(!loadedSuperset) {
                                RestRowModel restRowModel = new RestRowModel();
                                restRowModel.setSecondsEditText(seconds);
                                restRowModel.setMinutesEditText(minutes);
                                restRowModel.setDragHandle(dragHandleRest);

                                ((SupersetRowModel) rowTypeList.get(pos)).setRestRowModel(restRowModel);
                            }

                            else{
                                RestRowModel restRowModel = ((SupersetRowModel) rowTypeList.get(pos)).getRestRowModel();
                                restRowModel.setSecondsEditText(seconds);
                                restRowModel.setMinutesEditText(minutes);
                                restRowModel.setDragHandle(dragHandleRest);

                                minutes.setText(restRowModel.getMinutes());
                                seconds.setText(restRowModel.getSeconds());

                                if(restRowModel.isMinuteError()){
                                    TextInputLayout textInputLayout = getFirstParent(minutes, TextInputLayout.class);
                                    textInputLayout.setError("Minutes");
                                }

                                if(restRowModel.isSecondError()){
                                    TextInputLayout textInputLayout = getFirstParent(seconds, TextInputLayout.class);
                                    textInputLayout.setError("Seconds");
                                }

                            }

                            ((SupersetRowHolder) holder).setTextWatcherSupersetRestRow(this, ((SupersetRowModel) rowTypeList.get(pos)).getRestRowModel(), minutes, seconds);

                            dragHandleRest.setVisibility(View.INVISIBLE);

                            if(((SupersetRowModel) rowTypeList.get(pos)).isDisableText()){
                                disableEditText(minutes, seconds);
                            }

                            setSwipeListener(minutes, seconds);

                            ((SupersetRowHolder) holder).supersetContainer.addView(restRow);
                        }
                        else{
                            View exerciseRow = layoutInflater.inflate(R.layout.row, null);

                            ImageView dragHandleExercise = exerciseRow.findViewById(R.id.reorder);
                            AutoCompleteTextView exercise = exerciseRow.findViewById(R.id.autoCompleteTextView);
                            EditText weight = exerciseRow.findViewById(R.id.weight);
                            EditText sets = exerciseRow.findViewById(R.id.sets);
                            EditText reps = exerciseRow.findViewById(R.id.reps);

                            exercise.setAdapter(getExerciseAdapter(home.getContext()));
                            ((SupersetRowHolder) holder).setTextWatcherSupersetExRow(this, ((SupersetRowModel) rowTypeList.get(pos)).getExerciseModelList(), exercise, weight, sets, reps, i);

                            if(!loadedSuperset) {
                                ExerciseModel exerciseModel = new ExerciseModel();
                                exerciseModel.setExerciseEditText(exercise);
                                exerciseModel.setWeightEditText(weight);
                                exerciseModel.setSetsEditText(sets);
                                exerciseModel.setRepsEditText(reps);
                                exerciseModel.setDragHandle(dragHandleExercise);
                                exerciseModelList.add(i, exerciseModel);
                                ((SupersetRowModel) rowTypeList.get(pos)).setExerciseModelList(exerciseModelList);
                            }

                            else{
                                ExerciseModel exerciseModel = exerciseModelList.get(i);

                                exerciseModel.setExerciseEditText(exercise);
                                exerciseModel.setWeightEditText(weight);
                                exerciseModel.setSetsEditText(sets);
                                exerciseModel.setRepsEditText(reps);
                                exerciseModel.setDragHandle(dragHandleExercise);

                                exercise.setText(exerciseModel.getExercise());
                                weight.setText(exerciseModel.getWeight());
                                sets.setText(exerciseModel.getSets());
                                reps.setText(exerciseModel.getReps());

                                if(exerciseModel.isExerciseError()){
                                    TextInputLayout textInputLayout = getFirstParent(exercise, TextInputLayout.class);
                                    textInputLayout.setError("Exercise");
                                }

                                if(exerciseModel.isWeightError()){
                                    TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                                    textInputLayout.setError("Weight");
                                }

                                if(exerciseModel.isSetError()){
                                    TextInputLayout textInputLayout = getFirstParent(sets, TextInputLayout.class);
                                    textInputLayout.setError("Set");
                                }

                                if(exerciseModel.isRepError()){
                                    TextInputLayout textInputLayout = getFirstParent(reps, TextInputLayout.class);
                                    textInputLayout.setError("Rep");
                                }
                            }

                            dragHandleExercise.setVisibility(View.INVISIBLE);

                            if(((SupersetRowModel) rowTypeList.get(pos)).isDisableText()) {
                                convert1RMPercent(exercise, weight);
                                disableEditText(exercise, weight, reps, sets);
                                if(weight.getText().toString().isEmpty()){
                                    TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                                    textInputLayout.setHint("");
                                }
                            }

                            setSwipeListener(exercise, weight, sets, reps);

                            ((SupersetRowHolder) holder).supersetContainer.addView(exerciseRow);
                        }
                    }
                }

                if(updatePositionOnButtonClicked){
                    updatePosition();
                    updatePositionOnButtonClicked = false;
                }
            }


            //  when save is clicked and superset is already created
            else if(((SupersetRowHolder) holder).supersetContainer.getChildCount() > 0) {

                List<ExerciseModel> exerciseModelList = ((SupersetRowModel) rowTypeList.get(pos)).getExerciseModelList();

                ((SupersetRowModel) rowTypeList.get(pos)).setLinearLayout(((SupersetRowHolder) holder).supersetContainer);

                ExerciseModel exerciseModel = null;


                for (int i = 0; i < numberOfChildren; i++) {

                    //  get each exercise model from the list
                    if (((SupersetRowModel) rowTypeList.get(pos)).getAddRest()) {
                        if(i < numberOfChildren - 1) {
                            exerciseModel = exerciseModelList.get(i);
                        }
                    }

                    else{
                        exerciseModel = exerciseModelList.get(i);
                    }

                    View view = ((SupersetRowHolder) holder).supersetContainer.getChildAt(i);

                    if (view.getId() == exerciseView.getId()) {

                        AutoCompleteTextView exercise = view.findViewById(R.id.autoCompleteTextView);
                        EditText weight = view.findViewById(R.id.weight);
                        EditText sets = view.findViewById(R.id.sets);
                        EditText reps = view.findViewById(R.id.reps);
                        ImageView dragHandle = view.findViewById(R.id.reorder);


                        //  removes the text input layout hint if no text is inputted in weight
                        if(exerciseModel != null){
                            if(exerciseModel.getWeight() == null){
                                TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                                textInputLayout.setHint("Weight");
                            }

                            else{
                                if(exerciseModel.getWeight().isEmpty()){
                                    TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                                    textInputLayout.setHint("Weight");
                                }
                            }
                        }


                        exercise.setAdapter(getExerciseAdapter(home.getContext()));

                        if(exerciseModel != null) {
                            if (exerciseModel.isExerciseError()) {
                                TextInputLayout textInputLayout = getFirstParent(exercise, TextInputLayout.class);
                                textInputLayout.setError("Exercise");
                            }

                            if (exerciseModel.isWeightError()) {
                                TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                                textInputLayout.setError("Weight");
                            }

                            if (exerciseModel.isSetError()) {
                                TextInputLayout textInputLayout = getFirstParent(sets, TextInputLayout.class);
                                textInputLayout.setError("Set");
                            }

                            if (exerciseModel.isRepError()) {
                                TextInputLayout textInputLayout = getFirstParent(reps, TextInputLayout.class);
                                textInputLayout.setError("Rep");
                            }
                        }

                        ((SupersetRowHolder) holder).setTextWatcherSupersetExRow(this, ((SupersetRowModel) rowTypeList.get(pos)).getExerciseModelList(), exercise, weight, sets, reps, i);


                        dragHandle.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                    mDragStartListener.onStartDrag(holder);
                                }
                                return false;
                            }
                        });

                        if(((SupersetRowModel) rowTypeList.get(pos)).isDisableText()){
                            convert1RMPercent(exercise, weight);
                            disableEditText(exercise, weight, reps, sets);
                            if(i == 0){
                                dragHandle.setVisibility(View.INVISIBLE);
                            }
                            if(weight.getText().toString().isEmpty()){
                                TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                                textInputLayout.setHint("");
                            }
                        }

                        setSwipeListener(exercise, weight, sets, reps);

                    }

                    else if (view.getId() == restView.getId()) {

                        RestRowModel restRowModel = ((SupersetRowModel) rowTypeList.get(pos)).getRestRowModel();
                        EditText minutes = view.findViewById(R.id.minutesTV);
                        EditText seconds = view.findViewById(R.id.secondsTV);

                        if (restRowModel != null) {
                            minutes.setText(restRowModel.getMinutes());
                            seconds.setText(restRowModel.getSeconds());

                            if(restRowModel.isMinuteError()){
                                TextInputLayout textInputLayout = getFirstParent(minutes, TextInputLayout.class);
                                textInputLayout.setError("Minutes");
                            }

                            if(restRowModel.isSecondError()){
                                TextInputLayout textInputLayout = getFirstParent(seconds, TextInputLayout.class);
                                textInputLayout.setError("Seconds");
                            }
                        }

                        else {
                            minutes.setText("");
                            seconds.setText("");
                        }


                        ((SupersetRowHolder) holder).setTextWatcherSupersetRestRow(this, ((SupersetRowModel) rowTypeList.get(pos)).getRestRowModel(), minutes, seconds);

                        if(((SupersetRowModel) rowTypeList.get(pos)).isDisableText()){
                            disableEditText(minutes, seconds);
                        }

                        setSwipeListener(minutes, seconds);
                    }
                }

            }


        }

        else if(holder instanceof PyramidRowHolder){
            int pos = holder.getAdapterPosition();
            int numberOfChildren = ((PyramidsetRowModel) rowTypeList.get(position)).getNumberOfChildren();
            boolean addRest = ((PyramidsetRowModel) rowTypeList.get(position)).getAddRest();
            boolean loadedPyramidset = ((PyramidsetRowModel) rowTypeList.get(position)).getLoadedPyramidSet();
            LayoutInflater layoutInflater = home.getLayoutInflater();
            View exerciseView = layoutInflater.inflate(R.layout.row, null);
            View restView = layoutInflater.inflate(R.layout.rest_row, null);

            if(((PyramidRowHolder) holder).pyramidContainer.getChildCount() == 0){


                List<ExerciseModel> exerciseModelList;

                if(!loadedPyramidset){
                    exerciseModelList = new ArrayList<>();
                }

                else{
                    exerciseModelList = ((PyramidsetRowModel) rowTypeList.get(position)).getExerciseModelList();
                }

                ((PyramidsetRowModel) rowTypeList.get(pos)).setLinearLayout(((PyramidRowHolder) holder).pyramidContainer);
                for(int i = 0; i < numberOfChildren; i++){

                    if(i < numberOfChildren - 1){

                        ExerciseModel exerciseModel;
                        View exerciseRow = layoutInflater.inflate(R.layout.row, null);
                        ImageView dragHandleExercise = exerciseRow.findViewById(R.id.reorder);
                        EditText sets = exerciseRow.findViewById(R.id.sets);
                        EditText reps = exerciseRow.findViewById(R.id.reps);
                        EditText weight = exerciseRow.findViewById(R.id.weight);
                        AutoCompleteTextView exercise = exerciseRow.findViewById(R.id.autoCompleteTextView);
                        TextView x = exerciseRow.findViewById(R.id.X);

                        sets.setVisibility(View.GONE);
                        x.setVisibility(View.GONE);

                        if(!loadedPyramidset) {
                            exerciseModel = new ExerciseModel();
                            exerciseModelList.add(exerciseModel);
                            ((PyramidsetRowModel) rowTypeList.get(pos)).setExerciseModelList(exerciseModelList);
                        }

                        else{
                            exerciseModel = exerciseModelList.get(i);
                            if(i == 0){
                                exercise.setText(exerciseModel.getExercise());
                                weight.setText(exerciseModel.getWeight());
                                reps.setText(exerciseModel.getReps());
                            }

                            else{
                                weight.setText(exerciseModel.getWeight());
                                reps.setText(exerciseModel.getReps());
                            }
                        }

                        if(i == 0){
                            exercise.setAdapter(getExerciseAdapter(home.getContext()));
                            exerciseModel.setDragHandle(dragHandleExercise);
                            exerciseModel.setExerciseEditText(exercise);
                        }

                        else{
                            exercise.setVisibility(View.GONE);
                            dragHandleExercise.setVisibility(View.INVISIBLE);
                        }

                        exerciseModel.setWeightEditText(weight);
                        exerciseModel.setSetsEditText(sets);
                        exerciseModel.setRepsEditText(reps);


                        ((PyramidRowHolder) holder).setTextWatcherPyramidsetExRow(this, ((PyramidsetRowModel) rowTypeList.get(pos)).getExerciseModelList(), exercise, weight, sets, reps, i);

                        dragHandleExercise.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                    mDragStartListener.onStartDrag(holder);
                                }
                                return false;
                            }
                        });

                        if(weight.getText().toString().isEmpty()){
                            TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                            textInputLayout.setHint("Weight");
                        }


                        if(((PyramidsetRowModel) rowTypeList.get(pos)).isDisableText()){
                            convert1RMPercent(exercise, weight);
                            disableEditText(exercise, weight, reps);
                            if(i == 0){
                                dragHandleExercise.setVisibility(View.INVISIBLE);
                            }
                            if(weight.getText().toString().isEmpty()){
                                TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                                textInputLayout.setHint("");
                            }
                        }

                        setSwipeListener(exercise, weight, sets, reps);

                        ((PyramidRowHolder) holder).pyramidContainer.addView(exerciseRow);

                    }

                    if (i == numberOfChildren - 1) {
                        if(addRest) {

                            RestRowModel restRowModel;
                            View restRow = layoutInflater.inflate(R.layout.rest_row, null);
                            ImageView dragHandleRest = restRow.findViewById(R.id.reorder);
                            EditText minutes = restRow.findViewById(R.id.minutesTV);
                            EditText seconds = restRow.findViewById(R.id.secondsTV);

                            dragHandleRest.setVisibility(View.INVISIBLE);

                            if(!loadedPyramidset){
                                restRowModel = new RestRowModel();
                                ((PyramidsetRowModel) rowTypeList.get(pos)).setRestRowModel(restRowModel);
                            }

                            else{
                                restRowModel = ((PyramidsetRowModel) rowTypeList.get(pos)).getRestRowModel();
                                minutes.setText(restRowModel.getMinutes());
                                seconds.setText(restRowModel.getSeconds());
                            }

                            restRowModel.setMinutesEditText(minutes);
                            restRowModel.setSecondsEditText(seconds);
                            restRowModel.setDragHandle(dragHandleRest);

                            ((PyramidRowHolder) holder).setTextWatcherPyramidsetRestRow(this, ((PyramidsetRowModel) rowTypeList.get(pos)).getRestRowModel(), minutes, seconds);

                            if(((PyramidsetRowModel) rowTypeList.get(pos)).isDisableText()){
                                disableEditText(minutes, seconds);
                            }

                            setSwipeListener(minutes, seconds);

                            ((PyramidRowHolder) holder).pyramidContainer.addView(restRow);
                        }

                        else{
                            ExerciseModel exerciseModel;
                            View exerciseRow = layoutInflater.inflate(R.layout.row, null);
                            ImageView dragHandleExercise = exerciseRow.findViewById(R.id.reorder);
                            EditText sets = exerciseRow.findViewById(R.id.sets);
                            EditText reps = exerciseRow.findViewById(R.id.reps);
                            EditText weight = exerciseRow.findViewById(R.id.weight);
                            AutoCompleteTextView exercise = exerciseRow.findViewById(R.id.autoCompleteTextView);
                            TextView x = exerciseRow.findViewById(R.id.X);

                            sets.setVisibility(View.GONE);
                            x.setVisibility(View.GONE);

                            if(!loadedPyramidset){
                                exerciseModel = new ExerciseModel();
                                exerciseModelList.add(exerciseModel);
                            }

                            else{
                                exerciseModel = exerciseModelList.get(i);
                                reps.setText(exerciseModel.getReps());
                                weight.setText(exerciseModel.getWeight());
                            }

                            if(i == 0){
                                exercise.setText(exerciseModel.getExercise());
                            }

                            exercise.setAdapter(getExerciseAdapter(home.getContext()));
                            exerciseModel.setWeightEditText(weight);
                            exerciseModel.setSetsEditText(sets);
                            exerciseModel.setRepsEditText(reps);

                            if(i > 0) {
                                exercise.setVisibility(View.GONE);
                            }

                            dragHandleExercise.setVisibility(View.INVISIBLE);

                            if(weight.getText().toString().isEmpty()){
                                TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                                textInputLayout.setHint("Weight");
                            }

                            if(((PyramidsetRowModel) rowTypeList.get(pos)).isDisableText()){
                                convert1RMPercent(exercise, weight);
                                disableEditText(exercise, weight, reps);
                                if(weight.getText().toString().isEmpty()){
                                    TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                                    textInputLayout.setHint("");
                                }
                            }

                            setSwipeListener(exercise, weight, sets, reps);
                            ((PyramidRowHolder) holder).pyramidContainer.addView(exerciseRow);

                            ((PyramidsetRowModel) rowTypeList.get(pos)).setExerciseModelList(exerciseModelList);
                            ((PyramidRowHolder) holder).setTextWatcherPyramidsetExRow(this, ((PyramidsetRowModel) rowTypeList.get(pos)).getExerciseModelList(), exercise, weight, sets, reps, i);
                        }
                    }
                }

                if(updatePositionOnButtonClicked){
                    updatePosition();
                    updatePositionOnButtonClicked = false;
                }

            }

            else if(((PyramidRowHolder) holder).pyramidContainer.getChildCount() > 0){
                List<ExerciseModel> exerciseModelList = ((PyramidsetRowModel) rowTypeList.get(position)).getExerciseModelList();
                ArrayList<String> weightError = ((PyramidsetRowModel) rowTypeList.get(pos)).getWeightError();
                ArrayList<String> repError = ((PyramidsetRowModel) rowTypeList.get(pos)).getRepError();
                ((PyramidsetRowModel) rowTypeList.get(pos)).setLinearLayout(((PyramidRowHolder) holder).pyramidContainer);
                ExerciseModel exerciseModel = null;


                for (int i = 0; i < numberOfChildren; i++) {

                    if (((PyramidsetRowModel) rowTypeList.get(pos)).getAddRest()) {
                        if(i < numberOfChildren - 1) {
                            exerciseModel = exerciseModelList.get(i);
                        }
                    }

                    else{
                        exerciseModel = exerciseModelList.get(i);
                    }

                    View view = ((PyramidRowHolder) holder).pyramidContainer.getChildAt(i);

                    if (view.getId() == exerciseView.getId()) {
                        AutoCompleteTextView exercise = view.findViewById(R.id.autoCompleteTextView);
                        EditText weight = view.findViewById(R.id.weight);
                        EditText reps = view.findViewById(R.id.reps);
                        EditText set = view.findViewById(R.id.sets);
                        ImageView dragHandle = view.findViewById(R.id.reorder);

                        ((PyramidRowHolder) holder).setTextWatcherPyramidsetExRow(this, ((PyramidsetRowModel) rowTypeList.get(pos)).getExerciseModelList(), exercise, weight, set, reps, i);


                        exercise.setAdapter(getExerciseAdapter(home.getContext()));
                        if (exerciseModel != null) {
                            if(i == 0) {
                                if(((PyramidsetRowModel) rowTypeList.get(pos)).isExerciseError()){
                                    TextInputLayout textInputLayout = getFirstParent(exercise, TextInputLayout.class);
                                    textInputLayout.setError("Exercise");
                                }
                                exercise.setAdapter(getExerciseAdapter(home.getContext()));
                            }

                            if(exerciseModel.getWeight() == null){
                                TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                                textInputLayout.setHint("Weight");
                            }

                            else{
                                if(exerciseModel.getWeight().isEmpty()){
                                    TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                                    textInputLayout.setHint("Weight");
                                }
                            }

                            if(exerciseModel.getSets() == null){
                                TextInputLayout textInputLayout = getFirstParent(set, TextInputLayout.class);
                                textInputLayout.setHint("");
                            }

                            else{
                                if(exerciseModel.getSets().isEmpty()){
                                    TextInputLayout textInputLayout = getFirstParent(set, TextInputLayout.class);
                                    textInputLayout.setHint("");
                                }
                            }
                        }

                        if(weightError != null) {
                            if (weightError.get(i).equals("Yes")) {
                                TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                                textInputLayout.setError("Weight");
                            }
                        }

                        if(repError != null) {
                            if (repError.get(i).equals("Yes")) {
                                TextInputLayout textInputLayout = getFirstParent(reps, TextInputLayout.class);
                                textInputLayout.setError("Rep");
                            }
                        }


                        dragHandle.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                    mDragStartListener.onStartDrag(holder);
                                }
                                return false;
                            }
                        });

                        if(((PyramidsetRowModel) rowTypeList.get(pos)).isDisableText()){
                            convert1RMPercent(exercise, weight);
                            disableEditText(exercise, weight, reps);
                            dragHandle.setVisibility(View.INVISIBLE);

                            if(weight.getText().toString().isEmpty()){
                                TextInputLayout textInputLayout = getFirstParent(weight, TextInputLayout.class);
                                textInputLayout.setHint("");
                            }
                        }

                        setSwipeListener(exercise, weight, set, reps);

                    }

                    else if (view.getId() == restView.getId()) {
                                RestRowModel restRowModel = ((PyramidsetRowModel) rowTypeList.get(position)).getRestRowModel();
                                EditText minutes = view.findViewById(R.id.minutesTV);
                                EditText seconds = view.findViewById(R.id.secondsTV);

                                if (restRowModel != null) {
                                    //minutes.setText(restRowModel.getMinutes());
                                    //seconds.setText(restRowModel.getSeconds());
                                }

                        ((PyramidRowHolder) holder).setTextWatcherPyramidsetRestRow(this, ((PyramidsetRowModel) rowTypeList.get(pos)).getRestRowModel(), minutes, seconds);

                                if(((PyramidsetRowModel) rowTypeList.get(pos)).isMinuteError()){
                                    TextInputLayout textInputLayout = getFirstParent(minutes, TextInputLayout.class);
                                    textInputLayout.setError("Minutes");
                                }

                                if(((PyramidsetRowModel) rowTypeList.get(pos)).isSecondError()){
                                    TextInputLayout textInputLayout = getFirstParent(seconds, TextInputLayout.class);
                                    textInputLayout.setError("Seconds");
                                }


                                if(((PyramidsetRowModel) rowTypeList.get(pos)).isDisableText()){
                                    disableEditText(minutes, seconds);
                                }

                                setSwipeListener(minutes, seconds);
                    }
                }
            }

        }

        else if(holder instanceof ButtonHolder){
            if(startClicked){
                ((ButtonHolder) holder).startClicked();
                home.newWorkoutFAB.setVisibility(View.INVISIBLE);
            }

            else if(stopClicked){
                ((ButtonHolder) holder).stopClicked();
                stopClicked = false;
            }

        }


    }

    @Override
    public int getItemCount(){
        return rowTypeList.size();
    }



    //  exercise adapter for the exercise EditText
    //  displays the options of exercises
    private ArrayAdapter<String> getExerciseAdapter(Context context){
        String[] exercise = new String[]{};
        if(context != null) {
            exercise = context.getResources().getStringArray(R.array.exercises);
        }
        return new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, exercise);
    }

    private void createSnackbar(final int position, final RowType rowType, String displayText){
        Snackbar snackbar = Snackbar
                .make(home.recyclerView, displayText, Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        home.recyclerView.requestFocus();
                        rowTypeList.add(position, rowType);
                        home.recyclerView.smoothScrollToPosition(position);
                        notifyItemInserted(position);
                    }
                });
        snackbar.show();
    }

    //  Method for the dialog that shows when +add superset is clicked
    public void addSupersetLayout(){
        CharSequence colors[] = new CharSequence[] {"2", "3", "4", "5", "6", "7", "8", "9", "10"};

        if(home.getActivity() != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(home.getActivity());
            builder.setTitle("Pick # of exercises");
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {

                    final AlertDialog.Builder builder1 = new AlertDialog.Builder(home.getActivity());
                    builder1.setMessage("Add rest?");
                    builder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SupersetRowModel supersetRowModel = new SupersetRowModel();

                            //  since which is based on 0 index, and rest is being added, it needs to be added by 3
                            //  Example: user picks 2 (index is 0, so 0 + 3 = 3)
                            supersetRowModel.setNumberOfChildren(which + 3);
                            supersetRowModel.setAddRest(true);
                            rowTypeList.add(1, supersetRowModel);
                            notifyItemInserted(1);
                            dialog.dismiss();
                            dialogInterface.dismiss();
                        }
                    });
                    builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SupersetRowModel supersetRowModel = new SupersetRowModel();

                            //  since user is not adding rest, which needs to be added by 2
                            supersetRowModel.setNumberOfChildren(which + 2);
                            supersetRowModel.setAddRest(false);
                            rowTypeList.add(1, supersetRowModel);
                            notifyItemInserted(1);
                            dialog.dismiss();
                            dialogInterface.dismiss();
                        }
                    });
                    builder1.show();
                }
            });

            builder.show();
        }
    }

    //  method that displays alert dialog when user presses add pyramid set
    public void addPyramidSetLayout(){
        CharSequence colors[] = new CharSequence[] {"2", "3", "4", "5", "6", "7", "8", "9", "10"};

        if(home.getActivity() != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(home.getActivity());
            builder.setTitle("Pick # of sets");
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {

                    final AlertDialog.Builder builder1 = new AlertDialog.Builder(home.getActivity());
                    builder1.setMessage("Add rest?");
                    builder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PyramidsetRowModel pyramidsetRowModel = new PyramidsetRowModel();

                            //  which needs to be added by 3 because which is based on 0 index and rest is being added
                            pyramidsetRowModel.setNumberOfChildren(which + 3);
                            pyramidsetRowModel.setAddRest(true);
                            rowTypeList.add(1, pyramidsetRowModel);
                            notifyItemInserted(1);
                            dialog.dismiss();
                            dialogInterface.dismiss();
                        }
                    });
                    builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PyramidsetRowModel pyramidsetRowModel = new PyramidsetRowModel();

                            //  since no rest is added, which only needs to be added by 2
                            pyramidsetRowModel.setNumberOfChildren(which + 2);
                            pyramidsetRowModel.setAddRest(false);
                            rowTypeList.add(1, pyramidsetRowModel);
                            notifyItemInserted(1);
                            dialog.dismiss();
                            dialogInterface.dismiss();
                        }
                    });
                    builder1.show();
                }
            });

            builder.show();
        }
    }


    //  updates the positions of the text watchers for exercise row and rest row
    public void updatePosition(){
        for(int i = getItemCount() - 1; i >= 1; i--){
            RowType rowType = rowTypeList.get(i);

            if(rowType.getClass() == ExerciseModel.class){
                ((ExerciseModel) rowTypeList.get(i)).getExerciseRowTextWatcher().updatePosition(i);
            }

            else if(rowType.getClass() == RestRowModel.class){
                ((RestRowModel) rowTypeList.get(i)).getRestRowTextWatcher().updatePosition(i);
            }

        }
    }

    //  removes viewholder from rowTypeList
    public void removeViewHolder(int position){
        rowTypeList.remove(position);
        notifyItemRemoved(position);
    }

    //  method for setting up the adapter with data from Firebase
    public void setDataForLoadedWorkout(final String workoutName, final DisplayWorkoutAdapter displayWorkoutAdapter, final boolean resume) {

        SharedPreferences autoSaveSharedPref = home.activity.getSharedPreferences("autoSave", Context.MODE_PRIVATE);
        final String autoSave = autoSaveSharedPref.getString("autoPause", "");

        //  reference will change depnding on whether a workout is being resumed
        if(resume){
            loadedWorkoutReference = home.myRef.child(workoutName).child("list");
            displayWorkoutAdapter.startClicked = true;
        }
        else{
            loadedWorkoutReference = home.myRef.child("workouts").child(workoutName).child("list");
        }


        //  uses whatever reference is assigned, and checks if it exists
        loadedWorkoutReference
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            final String exerciseView = "Exercise row";
                            final String restView = "Rest row";

                            //Views for superset
                            final String supersetView = "Superset container";
                            final String exerciseSuperset = "Superset exercise";
                            final String restSuperset = "Superset rest";

                            //Views for pyramid set
                            final String pyramidSetView = "Pyramidset container";
                            final String exercisePyramidSet = "Pyramidset exercise";
                            final String restPyramidSet = "Pyramidset rest";

                            List<ExerciseModel> exerciseModelList = new ArrayList<>();
                            int childrenInContainer = 0;
                            int pos = 0;
                            SupersetRowModel supersetRowModel = new SupersetRowModel();
                            PyramidsetRowModel pyramidsetRowModel = new PyramidsetRowModel();

                            //  iterates through each child in the node and checks to see if it equals a rowType
                            //  if it does, it will set that rowType to disable text if a workout is being resumed or vice versa
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String viewType = (String) snapshot.child("view").getValue();
                                if (viewType != null) {
                                    if (viewType.equals(exerciseView)) {
                                        ExerciseModel exerciseModel = snapshot.getValue(ExerciseModel.class);
                                        if(exerciseModel != null) {
                                            if (resume) {
                                                exerciseModel.setDisableText(true);
                                            } else {
                                                exerciseModel.setDisableText(false);
                                            }
                                            rowTypeList.add(exerciseModel);
                                        }
                                    } else if (viewType.equals(restView)) {
                                        RestRowModel restRowModel = snapshot.getValue(RestRowModel.class);
                                        if(restRowModel != null) {
                                            if (resume) {
                                                restRowModel.setDisableText(true);
                                            } else {
                                                restRowModel.setDisableText(false);
                                            }
                                            rowTypeList.add(restRowModel);
                                        }
                                    } else if (viewType.equals(supersetView)) {
                                        supersetRowModel = snapshot.getValue(SupersetRowModel.class);

                                        if(supersetRowModel != null) {
                                            supersetRowModel.setLoadedSuperset(true);
                                            if (resume) {
                                                supersetRowModel.setDisableText(true);
                                            } else {
                                                supersetRowModel.setDisableText(false);
                                            }
                                            rowTypeList.add(supersetRowModel);
                                            childrenInContainer = supersetRowModel.getNumberOfChildren();
                                        }
                                    } else if (viewType.equals(exerciseSuperset)) {
                                        ExerciseModel exerciseModel = snapshot.getValue(ExerciseModel.class);
                                        exerciseModelList.add(exerciseModel);
                                        pos++;
                                        if (pos == childrenInContainer) {
                                            pos = 0;
                                            childrenInContainer = 0;
                                            if(supersetRowModel != null) {
                                                supersetRowModel.setExerciseModelList(exerciseModelList);
                                                supersetRowModel.setAddRest(false);
                                            }
                                            exerciseModelList = new ArrayList<>();
                                        }
                                    } else if (viewType.equals(restSuperset)) {
                                        RestRowModel restRowModel = snapshot.getValue(RestRowModel.class);
                                        pos++;
                                        if (pos == childrenInContainer) {
                                            if(supersetRowModel != null) {
                                                supersetRowModel.setExerciseModelList(exerciseModelList);
                                                supersetRowModel.setRestRowModel(restRowModel);
                                                supersetRowModel.setAddRest(true);
                                            }
                                            exerciseModelList = new ArrayList<>();
                                            pos = 0;
                                            childrenInContainer = 0;
                                        }
                                    } else if (viewType.equals(pyramidSetView)) {
                                        pyramidsetRowModel = snapshot.getValue(PyramidsetRowModel.class);
                                        if(pyramidsetRowModel != null) {
                                            pyramidsetRowModel.setLoadedPyramidSet(true);
                                            if (resume) {
                                                pyramidsetRowModel.setDisableText(true);
                                            } else {
                                                pyramidsetRowModel.setDisableText(false);
                                            }
                                            rowTypeList.add(pyramidsetRowModel);
                                            childrenInContainer = pyramidsetRowModel.getNumberOfChildren();
                                        }
                                    } else if (viewType.equals(exercisePyramidSet)) {
                                        ExerciseModel exerciseModel = snapshot.getValue(ExerciseModel.class);
                                        exerciseModelList.add(exerciseModel);
                                        pos++;
                                        if (pos == childrenInContainer) {
                                            pyramidsetRowModel.setExerciseModelList(exerciseModelList);
                                            pyramidsetRowModel.setAddRest(false);
                                            exerciseModelList = new ArrayList<>();
                                            pos = 0;
                                            childrenInContainer = 0;
                                        }
                                    } else if (viewType.equals(restPyramidSet)) {
                                        RestRowModel restRowModel = snapshot.getValue(RestRowModel.class);
                                        pos++;
                                        if (pos == childrenInContainer) {
                                            pyramidsetRowModel.setExerciseModelList(exerciseModelList);
                                            pyramidsetRowModel.setRestRowModel(restRowModel);
                                            pyramidsetRowModel.setAddRest(true);
                                            exerciseModelList = new ArrayList<>();
                                            pos = 0;
                                            childrenInContainer = 0;
                                        }
                                    }

                                }
                            }

                            //  adds the buttons to the end of the list since the buttons can't be saved to Firebase
                            rowTypeList.add(0, new ButtonsHolder());
                            stopClicked = true;

                            //  called after notifyDataSetChanged() is finished
                            //  if a workout is being resumed, it will scroll to top and will initialize the edit text for the rest countdown if needed
                            //  if workout is not being resumed, it will just scroll to top
                            home.recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                @Override
                                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                                    if (resume) {

                                        if(home.layoutManager.findLastCompletelyVisibleItemPosition() != getItemCount() -1) {
                                            home.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                                @Override
                                                public void onScrolled(RecyclerView recyclerView,
                                                                       int dx, int dy) {
                                                    super.onScrolled(recyclerView, dx, dy);
                                                    int visibleItemCount = home.layoutManager.getChildCount();
                                                    int totalItemCount = home.layoutManager.getItemCount();
                                                    int pastVisiblesItems = home.layoutManager.findFirstVisibleItemPosition();

                                                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                                                        initializeRestEditTexts();
                                                    }

                                                    home.recyclerView.removeOnScrollListener(this);
                                                }
                                            });
                                            home.recyclerView.getLayoutManager().smoothScrollToPosition(home.recyclerView, null, getItemCount() - 1);
                                        }
                                        else if(home.layoutManager.findLastCompletelyVisibleItemPosition() == getItemCount() - 1 && home.layoutManager.findFirstCompletelyVisibleItemPosition() == 0){
                                            initializeRestEditTexts();
                                        }
                                        else if(home.layoutManager.findLastCompletelyVisibleItemPosition() == getItemCount() - 1 && home.layoutManager.findFirstCompletelyVisibleItemPosition() != 0){
                                            initializeRestEditTexts();

                                        }

                                    }
                                    else{

                                        if(home.layoutManager.findFirstCompletelyVisibleItemPosition() != getItemCount() - 1) {
                                            home.recyclerView.getLayoutManager().smoothScrollToPosition(home.recyclerView, null, getItemCount() - 1);
                                        }

                                    }
                                    home.recyclerView.removeOnLayoutChangeListener(this);
                                }
                            });

                            //  if workout is being resumed, set left to right/right to left swipe enabled on recycler view
                            //  if service is not running, delete the node at reference
                            if(resume){

                                if(!isMyServiceRunning(CountdownService.class)) {
                                    home.myRef.child(workoutName).removeValue();
                                }
                                home.recyclerView.setOnTouchListener(new OnSwipeTouchListener(home.activity, displayWorkoutAdapter));
                                swipeListener = true;

                            }

                            //  if workout is not being resumed, user cannot swipe right to left/left to right to move onto next set/rest
                            //  drag and drop, and swipe to delete are enabled instead
                            else{
                                home.recyclerView.setOnTouchListener(null);
                                swipeListener = false;
                            }
                            displayWorkoutAdapter.notifyDataSetChanged();

                        }

                        /*
                            This will be called if the app crashes unexpectedly while a workout has started
                            This will reset everything on the home page to normal

                            Also called when a user has a workout in progress and pauses home fragment with no children other than the buttons,
                            and user navigates back to home page
                         */
                        else{

                            //  if app crashes unexpectedly, reset title to home
                            if (autoSave.equals("") && !home.titleSet) {
                                home.activity.setTitle("Home");
                            }

                            //  sets the title of an empty workout when loaded
                            //  empty workout meaning no exercises saved in it
                            else if(home.titleSet){
                                home.activity.setTitle(workoutName);
                            }

                            //  sets the title of the workout that is being auto saved
                            //  used when user is resuming home fragment but workout is not being loaded
                            else{
                                home.activity.setTitle(autoSave);
                            }



                            displayWorkoutAdapter.startClicked = false;
                            displayWorkoutAdapter.stopClicked = true;
                            rowTypeList.add(0, new ButtonsHolder());
                            notifyItemInserted(0);

                        }
                        loadedWorkoutReference.removeEventListener(this);
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }

                });
    }

    //  checks to see if a service is running
    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) home.activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //  Initializes secondsTV and minuteTV so that the receivers know which textview to update
    private void initializeRestEditTexts(){

        //  if rest is for exercise row, assign minuteTV and secondTV to the edit text at position getItemCount() - 2
        if(rowTypeList.get(getItemCount() - 2).getClass() == RestRowModel.class && rowTypeList.get(getItemCount() - 1).getClass() == ExerciseModel.class) {
            secondsTVParent = (RestRowModel) rowTypeList.get(getItemCount() - 2);
            secondsTV = secondsTVParent.getSecondsEditText();
            minuteTV = secondsTVParent.getMinutesEditText();
        }

        //if rest is for itself, assign minuteTV and secondTV to the edit text at position getItemCount() - 1
        else if(rowTypeList.get(getItemCount() - 1).getClass() == RestRowModel.class) {
            secondsTVParent = (RestRowModel) rowTypeList.get(getItemCount() - 1);
            secondsTV = secondsTVParent.getSecondsEditText();
            minuteTV = secondsTVParent.getMinutesEditText();
        }

        //if rest is for a superset and superset has a rest row, assign minuteTV and secondTV to the edit text at position getItemCount() - 1
        else if(rowTypeList.get(getItemCount() - 1).getClass() == SupersetRowModel.class){
            View view = home.recyclerView.getLayoutManager().findViewByPosition(getItemCount() - 1);
            LinearLayout linearLayout = view.findViewById(R.id.supersetLayout);
            View possibleRestRow = linearLayout.getChildAt(linearLayout.getChildCount() - 1);
            if(possibleRestRow.getId() == R.id.rest_row){
                secondsTV = (EditText) possibleRestRow.findViewById(R.id.secondsTV);
                minuteTV = (EditText) possibleRestRow.findViewById(R.id.minutesTV);
            }
        }

        //if rest is for a pyramid set and pyramid set has a rest row, assign minuteTV and secondTV to the edit text at position getItemCount() - 1
        else if(rowTypeList.get(getItemCount() - 1).getClass() == PyramidsetRowModel.class){
            View view = home.recyclerView.getLayoutManager().findViewByPosition(getItemCount() - 1);
            LinearLayout linearLayout = view.findViewById(R.id.pyramidContainer);
            View possibleRestRow = linearLayout.getChildAt(linearLayout.getChildCount() - 1);
            if(possibleRestRow.getId() == R.id.rest_row){
                secondsTV = (EditText) possibleRestRow.findViewById(R.id.secondsTV);
                minuteTV = (EditText) possibleRestRow.findViewById(R.id.minutesTV);
            }
        }

        //  if countdown is running, register new receiver, and send broadcast
        //  this will be called if a user returns to the app while the countdown is still ongoing
        if(isMyServiceRunning(CountdownService.class)){
            Intent countSec = new Intent(COUNTDOWN_SECONDS);
            home.activity.registerReceiver(restCountDown.secondReceiver, new IntentFilter(COUNTDOWN_SECONDS));
            home.activity.sendBroadcast(countSec);
        }

        //  this is necessary for when a service finishes while a user has the app in the background
        //  this will allow the rest to reset to its original values if needed
        else{
            SharedPreferences minPref = home.activity.getSharedPreferences("min", Context.MODE_PRIVATE);
            SharedPreferences secPref = home.activity.getSharedPreferences("sec", Context.MODE_PRIVATE);
            int min = minPref.getInt("inputtedMin", -1);
            int sec = secPref.getInt("inputtedSec", -1);
            if(min != -1 && sec != -1){
                Intent countSec = new Intent(COUNTDOWN_SECONDS);
                home.activity.registerReceiver(restCountDown.secondReceiver, new IntentFilter(COUNTDOWN_SECONDS));
                home.activity.sendBroadcast(countSec);
            }
        }
    }

    //  gets the TextInputLayout for edit texts
    public <ParentClass> ParentClass getFirstParent(View view, Class<ParentClass> parentClass) {
        if (view.getParent() instanceof View) {

            if (parentClass.isInstance(view.getParent())) {
                return (ParentClass) view.getParent();
            } else {
                return getFirstParent((View) view.getParent(), parentClass);
            }

        } else {
            return null;
        }
    }

    //  disables edit texts
    public void disableEditText(EditText...editText){
        if (home.activity != null) {
            for(EditText e : editText) {
                if(e != null) {
                    e.setCursorVisible(false);
                    e.setFocusable(false);
                    e.setFocusableInTouchMode(false);
                    e.clearFocus();
                    e.setBackground(null);
                }
            }
        }
    }

    //  method to convert percentage inputted to an int based on max inputted for that exercise
    public void convert1RMPercent(final AutoCompleteTextView exercise, final EditText weight){

        SharedPreferences roundSharedPref = home.activity.getSharedPreferences("round", Context.MODE_PRIVATE);
        SharedPreferences conversionSharedPref = home.activity.getSharedPreferences("weightConversion", Context.MODE_PRIVATE);
        final String conversion = conversionSharedPref.getString("convert", "");
        final String round = roundSharedPref.getString("roundWeight", "");
        home.myRef.child("max")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                maxModel maxModel = snapshot.getValue(maxModel.class);
                                if(maxModel != null) {
                                    String ex = maxModel.getExercise();
                                    String maxNum = maxModel.getMax();
                                    if (ex.equalsIgnoreCase(exercise.getText().toString())) {

                                        //  checks if weight has a percentage
                                        if (weight.getText().toString().contains("%")) {

                                            //  gets the percent as a decimal
                                            String multiplier = weight.getText().toString().replace("%", "");
                                            double decimalMultiplier = Double.parseDouble(multiplier) / 100;


                                            double maxDouble = Double.parseDouble(maxNum);
                                            double result = maxDouble * decimalMultiplier;

                                            //  rounds the converted percent to nearest multiple of 2.5 or 5, depending on user settings
                                            //  sets the weight as result

                                            double roundedResult;

                                            if (round.equals("2.5")) {
                                                roundedResult = round2_5(result);
                                            } else {
                                                roundedResult = round((int) result);
                                            }
                                            DecimalFormat df = new DecimalFormat("###.#");
                                            weight.setText(String.format("%s", df.format(roundedResult)));
                                        }

                                    }
                                }
                            }
                        }

                        //  checks if weight contains kg or lbs
                        //  if it does, it will convert either from kg to lbs, lbs to kg, or no conversion depending on user settings
                        if(weight.getText().toString().toLowerCase().contains("kg")){

                            //  convert from kg to lbs
                            if(conversion.equals("kgToLb")){
                                double weightDouble = Double.parseDouble(weight.getText().toString().toLowerCase().replace(" kg", ""));
                                String result = convertKgToLb(weightDouble) + " lbs";
                                weight.setText(result);
                            }

                        }

                        else if(weight.getText().toString().toLowerCase().contains("lbs")){

                            //  convert from lbs to kg
                            if(conversion.equals("lbToKg")){
                                double weightDouble = Double.parseDouble(weight.getText().toString().toLowerCase().replace(" lbs", ""));
                                String result = convertLbToKg(weightDouble) + " kg";
                                weight.setText(result);
                            }
                        }
                        home.myRef.child("max").removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });


    }

    //  this allows the user to either swipe to move onto next set/exercise/rest or disables that swipe
    private void setSwipeListener(EditText...editTexts){

        if(swipeListener) {
            for (EditText editText : editTexts) {
                editText.setOnTouchListener(new OnSwipeTouchListener(home.activity, this));
            }
        }
        else{
            for (EditText editText : editTexts) {
                editText.setOnTouchListener(null);
            }
        }
    }

    //  method to round to nearest multiple of 5
    private int round(int num) {
        int temp = num%5;
        if (temp<3)
            return num-temp;
        else
            return num+5-temp;
    }

    //  round n up to nearest multiple of m
    private double round2_5(double num){
        return Math.round(num / 2.5) * 2.5;
    }

    //  method to convert from kg to lbs
    //  uses 2.2 as the conversion
    private String convertKgToLb(double weight){
        double convertedWeight = weight * 2.2;
        return String.format(Locale.US, "%.1f", convertedWeight);
    }

    //  method to convert from lb to kg
    private String convertLbToKg(double weight){
        double convertedWeight = weight / 2.2;
        return String.format(Locale.US, "%.1f", convertedWeight);
    }
}
