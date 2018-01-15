package com.example.workouttracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.ExifInterface;
import android.os.SystemClock;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.widget.Toast.makeText;
import static com.example.workouttracker.R.id.minutesTV;
import static com.example.workouttracker.R.id.save_non_transition_alpha;

/**
 * Created by Gerald on 11/14/2017.
 */

public class SaveChanges {
    private Home home;
    private DisplayWorkoutAdapter displayWorkoutAdapter;
    private final String exerciseView = "Exercise row";
    private final String restView = "Rest row";

    //  Views for superset
    private final String supersetView = "Superset container";
    private final String exerciseSuperset = "Superset exercise";
    private final String restSuperset = "Superset rest";

    //  Views for pyramid set
    private final String pyramidSetView = "Pyramidset container";
    private final String exercisePyramidSet = "Pyramidset exercise";
    private final String restPyramidSet = "Pyramidset rest";


    //  public constructor
    public SaveChanges(Home home, DisplayWorkoutAdapter displayWorkoutAdapter){
        this.home = home;
        this.displayWorkoutAdapter = displayWorkoutAdapter;
    }


    //  method to save data to a specific node in Firebase
    public void saveToDB(String tableName, final List<RowType> rowTypeList, boolean pause, String time, final String restCountdownType, final Context context) {
        final DatabaseReference databaseReference;
        List<RowType> list = null;

        //  creates a copy of the List<RowType> so that the original is not tampered with
        if(rowTypeList != null) {
            list = new ArrayList<>(rowTypeList);
        }

        //  deletes the ButtonHolder in list which is at index 0
        //  necessary because this RowType does not have any getters/setters, which will throw an error when fetching or saving to Firebase
        if(list != null && rowTypeList.size() > 0) {
            list.remove(0);
        }

        //  creates a time table for the corresponding workout if time != null
        if (time != null) {
            WorkoutTime workoutTime = new WorkoutTime();
            workoutTime.setTime(time);
            String timeTable = tableName + "TimeTable";
            home.myRef.child("workouts").child(timeTable).setValue(workoutTime);
        }

        //  loops through each RowType and assigns the corresponding view type
        else if(time == null && list != null){
            for (int i = 0; i < list.size(); i++) {
                RowType rowType = list.get(i);

                if (rowType.getClass() == ExerciseModel.class) {
                    ExerciseModel exerciseModel = (ExerciseModel) rowType;
                    exerciseModel.setView(exerciseView);
                } else if (rowType.getClass() == RestRowModel.class) {
                    RestRowModel restRowModel = (RestRowModel) rowType;
                    restRowModel.setView(restView);
                } else if (rowType.getClass() == SupersetRowModel.class) {
                    SupersetRowModel supersetRowModel = (SupersetRowModel) rowType;
                    supersetRowModel.setViewType(supersetView);
                } else if (rowType.getClass() == PyramidsetRowModel.class) {
                    PyramidsetRowModel pyramidsetRowModel = (PyramidsetRowModel) rowType;
                    pyramidsetRowModel.setViewType(pyramidSetView);
                }
            }

            //  called when superset or pyramidset timer is finished
            //  this will update the pause table in Firebase and update the viewholder to show the correct changes
            if(pause){
                databaseReference = home.myRef.child(tableName).child("list");
                databaseReference.setValue(list, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference1) {
                        if(restCountdownType != null && context != null) {
                            if(restCountdownType.equals("supersetRowChange") || restCountdownType.equals("pyramidsetChange")) {
                                /*home.recyclerView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d("notify", "n");
                                        displayWorkoutAdapter.notifyItemChanged(displayWorkoutAdapter.getItemCount() - 1);
                                    }
                                });*/
                                //Log.d("a", ((SupersetRowModel) displayWorkoutAdapter.rowTypeList.get(displayWorkoutAdapter.getItemCount() - 1)).getExerciseModelList().get(0).getSets());
                                //displayWorkoutAdapter.notifyItemChanged(displayWorkoutAdapter.getItemCount() - 1);
                            }
                        }
                    }
                });
            }

            else{
                databaseReference = home.myRef.child("workouts").child(tableName).child("list");
                databaseReference.setValue(list);
            }
        }

    }


    //  method that is called when start is clicked
    //  disables auto save because it is not needed when a workout is in progress
    //  will scroll to the top if the first view holder is not visible
    //  calls scrollFinished() once scrolling is done
    public void startClicked(){

        SharedPreferences autoSave = home.activity.getSharedPreferences("autoSave", Context.MODE_PRIVATE);
        autoSave.edit().clear().apply();


        if(home.layoutManager.findLastCompletelyVisibleItemPosition() != displayWorkoutAdapter.getItemCount() -1) {
            home.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView,
                                       int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    //int visibleItemCount = home.layoutManager.getChildCount();
                    //int totalItemCount = home.layoutManager.getItemCount();
                    //int pastVisiblesItems = home.layoutManager.findFirstVisibleItemPosition() + 1;

                    //if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {

                    //}


                    scrollFinished();
                    home.recyclerView.removeOnScrollListener(this);
                }
            });
            home.recyclerView.getLayoutManager().smoothScrollToPosition(home.recyclerView, null, displayWorkoutAdapter.getItemCount() - 1);
        }

        else{
            scrollFinished();
        }

    }

    //  this method is used to show an alert dialog that will prompt the user to enter a new workout name
    //  when save is clicked, checks to see if a workout with that name already exists
    //  if it does, it will display an alert dialog asking if the user wants to override that workout with a new workout
    public void setActivityTitle() {
        if (home.activity != null) {
            LayoutInflater layoutInflater = (LayoutInflater) home.activity.getSystemService(LAYOUT_INFLATER_SERVICE);
            final View saveWorkoutView = layoutInflater.inflate(R.layout.save_workout_alert_dialog_edit_text, null);
            final AlertDialog.Builder saveWorkoutBuilder = new AlertDialog.Builder(home.activity);
            saveWorkoutBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            })
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setView(saveWorkoutView);
            final AlertDialog dialog = saveWorkoutBuilder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final EditText fileName = (EditText) saveWorkoutView.findViewById(R.id.enterFileName);

                    //  if fileName does not have any input make a toast to show an error
                    if (fileName.getText().toString().length() < 1) {
                        fileName.requestFocus();
                        String enterFileName = "Enter File Name";
                        Toast toast = makeText(home.activity, enterFileName, Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    //  if fileName is larger than 28 characters, make a toast to show an error
                    else if (fileName.getText().toString().length() > 28) {
                        fileName.requestFocus();
                        String tooLong = "File Name Too Long";
                        Toast toast = makeText(home.activity, tooLong, Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    //  if fileName equals 1,2,3,4,5,6,7, or 8
                    else if (fileName.getText().toString().equals("1") || fileName.getText().toString().equals("2") ||
                            fileName.getText().toString().equals("3") || fileName.getText().toString().equals("4") ||
                            fileName.getText().toString().equals("5") || fileName.getText().toString().equals("6") ||
                            fileName.getText().toString().equals("7") || fileName.getText().toString().equals("8")) {

                        fileName.requestFocus();
                        String invalidName = "Invalid name";
                        Toast toast = makeText(home.activity, invalidName, Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        dialog.dismiss();

                        //  iterates through tableNames to find one that matches user input
                        //  if user already has a workout named the same, display new popup window asking if they are sure they want to override
                        home.myRef.child("workouts").child(fileName.getText().toString())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {

                                            AlertDialog.Builder saveOverrideBuilder = new AlertDialog.Builder(home.activity);
                                            saveOverrideBuilder.setMessage("Do you want to override this workout?")
                                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();
                                                            if (saveWorkoutView.getParent() != null) {
                                                                ((ViewGroup) saveWorkoutView.getParent()).removeView(saveWorkoutView);
                                                            }
                                                            setActivityTitle();
                                                        }
                                                    })
                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            dialogInterface.dismiss();

                                                            //  sets the title of the workout and deletes/overrides the existing workout and time table for that workout
                                                            home.activity.setTitle(fileName.getText().toString());
                                                            String tableName = home.activity.getTitle().toString();
                                                            home.myRef.child("workouts").child(tableName).removeValue();
                                                            home.myRef.child("workouts").child(tableName + "TimeTable").removeValue();
                                                            home.titleSet = true;

                                                            buttonClicked();

                                                        }
                                                    })
                                                    .show();


                                        }

                                        //  create a new workout
                                        else {

                                            //  set title of workout
                                            //  inflate layout depending on which button was clicked
                                            home.activity.setTitle(fileName.getText().toString());
                                            home.titleSet = true;
                                            buttonClicked();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                    }

                }
            });

        }
    }

    //  adds a different type of item to position 1 of adapter depending on what button is clicked
    //  updates the position of the textwatchers for each edittext
    public void buttonClicked(){
        if(displayWorkoutAdapter.addExerciseClicked) {
            ExerciseModel exerciseModel = new ExerciseModel();
            displayWorkoutAdapter.rowTypeList.add(1, exerciseModel);
            displayWorkoutAdapter.notifyItemInserted(1);
            displayWorkoutAdapter.updatePositionOnButtonClicked = true;
            displayWorkoutAdapter.addExerciseClicked = false;
        }

        else if(displayWorkoutAdapter.addRestClicked){
            RestRowModel restRowModel = new RestRowModel();
            displayWorkoutAdapter.rowTypeList.add(1, restRowModel);
            displayWorkoutAdapter.notifyItemInserted(1);
            displayWorkoutAdapter.updatePositionOnButtonClicked = true;
            displayWorkoutAdapter.addRestClicked = false;
        }

        else if(displayWorkoutAdapter.addSupersetClicked){
            displayWorkoutAdapter.addSupersetLayout();
            displayWorkoutAdapter.updatePositionOnButtonClicked = true;
            displayWorkoutAdapter.addSupersetClicked = false;
        }

        else if(displayWorkoutAdapter.addPyramidsetClicked){
            displayWorkoutAdapter.addPyramidSetLayout();
            displayWorkoutAdapter.updatePositionOnButtonClicked = true;
            displayWorkoutAdapter.addPyramidsetClicked = false;
        }
    }


    //  called when a user presses start and there are no errors in the edit texts
    //  inserts data into Firebase using insertDataToTable()
    //  converts exercises, that have a max saved in Firebase, from % to integer
    //  will either round to nearest 5 lb or 2.5 lb depending on user preference
    private void insertDataAndDisableEditText(){
        String tableName = null;
        if(home.activity != null) {
            tableName = home.activity.getTitle().toString();
        }

        home.titleSet = true;


        //  iterate through the children of the rowTypeList up until the buttons
        int numberOfViews = displayWorkoutAdapter.getItemCount() - 1;

        if(tableName != null) {

            for (int a = numberOfViews; a >= 1; a--) {
                RowType rowType = displayWorkoutAdapter.rowTypeList.get(a);

                /*
                    child is an exercise row
                    set exercise row to disable text
                    checks if weight is empty, if it is, setter is used to set weight to empty
                */
                if (rowType.getClass() == ExerciseModel.class) {
                    ExerciseModel exerciseModel = (ExerciseModel) rowType;

                    String weightString = exerciseModel.getWeight();

                    //if save button is clicked, disable edit texts and hide drag handle
                    if (weightString.isEmpty()) {
                        ((ExerciseModel) rowType).setWeightEmpty(true);
                    }

                    /*home.exDrawable = exerciseModel.getExerciseEditText().getBackground();
                    home.weightDrawable = exerciseModel.getWeightEditText().getBackground();
                    home.setDrawable = exerciseModel.getSetsEditText().getBackground();
                    home.repDrawable = exerciseModel.getRepsEditText().getBackground();*/

                    exerciseModel.setDisableText(true);
                }

                /*
                    Child is a rest row
                    sets rest row to disable text
                */
                else if (rowType.getClass() == RestRowModel.class) {
                    RestRowModel restRowModel = (RestRowModel) rowType;

                    restRowModel.setDisableText(true);
                }

                /*
                    Child is a superset
                    Loop through children of container
                    If weight is a %, convert to an integer using convert1RMPercent
                    Integer will be rounded to either nearest 5 or 2.5 multiple, depending on settings
                */
                else if (rowType.getClass() == SupersetRowModel.class) {

                    SupersetRowModel supersetRowModel = (SupersetRowModel) rowType;
                    List<ExerciseModel> exerciseModelList = supersetRowModel.getExerciseModelList();
                    RestRowModel restRowModel = supersetRowModel.getRestRowModel();

                    supersetRowModel.setDisableText(true);
                    ExerciseModel exerciseModel;
                    for (int i = 0; i < supersetRowModel.getNumberOfChildren(); i++) {

                        if (i < supersetRowModel.getNumberOfChildren() - 1) {
                            exerciseModel = exerciseModelList.get(i);

                            EditText weight = exerciseModel.getWeightEditText();
                            AutoCompleteTextView exercise = exerciseModel.getExerciseEditText();

                            //displayWorkoutAdapter.convert1RMPercent(exercise, weight);


                        }
                        else if (i == supersetRowModel.getNumberOfChildren() - 1) {
                            if (restRowModel == null) {
                                exerciseModel = exerciseModelList.get(i);

                                EditText weight = exerciseModel.getWeightEditText();
                                AutoCompleteTextView exercise = exerciseModel.getExerciseEditText();

                                //displayWorkoutAdapter.convert1RMPercent(exercise, weight);
                            }
                        }
                    }

                }


                /*
                    Child is a pyramidset row
                    If weight is a %, convert to an integer using convert1RMPercent
                    Integer will be rounded to either nearest 5 or 2.5 multiple, depending on settings
                */
                else if (rowType.getClass() == PyramidsetRowModel.class) {
                    PyramidsetRowModel pyramidsetRowModel = (PyramidsetRowModel) rowType;
                    List<ExerciseModel> exerciseModelList = pyramidsetRowModel.getExerciseModelList();
                    RestRowModel restRowModel = pyramidsetRowModel.getRestRowModel();

                    pyramidsetRowModel.setDisableText(true);
                    ExerciseModel exerciseModel;
                    AutoCompleteTextView exercise = null;


                    for (int i = 0; i < pyramidsetRowModel.getNumberOfChildren(); i++) {
                        if (i < pyramidsetRowModel.getNumberOfChildren() - 1) {
                            exerciseModel = exerciseModelList.get(i);


                            EditText weight = exerciseModel.getWeightEditText();


                            if(i == 0){
                                exercise = exerciseModel.getExerciseEditText();
                            }


                            //displayWorkoutAdapter.convert1RMPercent(exercise, weight);

                        } else if (i == pyramidsetRowModel.getNumberOfChildren() - 1) {
                            if (restRowModel == null) {
                                exerciseModel = exerciseModelList.get(i);

                                EditText weight = exerciseModel.getWeightEditText();

                                //  if save button is clicked, disable edit texts and hide drag handle

                                //displayWorkoutAdapter.convert1RMPercent(exercise, weight);
                            }
                        }
                    }
                }
            }

            //  save data to Firebase and enables left to right/right to left swipe for the recycler view
            saveToDB(tableName, displayWorkoutAdapter.rowTypeList, false, null, null, null);
            home.recyclerView.setOnTouchListener(new OnSwipeTouchListener(home.activity, displayWorkoutAdapter));
        }
    }

    //  called when user presses start and scroll to top is finished
    //  responsible for checking errors in the edit text
    //  if no errors, notifyDataSetChanged() is called and a shared pref is created to keep track of time of workout
    private void scrollFinished(){

        LayoutInflater layoutInflater = (LayoutInflater) home.activity.getSystemService(LAYOUT_INFLATER_SERVICE);

        View exerciseRow = layoutInflater.inflate(R.layout.row, null);
        View restRow = layoutInflater.inflate(R.layout.rest_row, null);

        boolean insertDataAndSave = true;


        int childCount = displayWorkoutAdapter.getItemCount() - 1;
        //  checks for all the children of recyclerView excluding the buttons
        for (int a = childCount; a > 0; a--) {
            RowType rowType = displayWorkoutAdapter.rowTypeList.get(a);

            //if viewholder = exercise row, check for errors
            if (rowType.getClass() == ExerciseModel.class) {
                AutoCompleteTextView exercise = ((ExerciseModel) rowType).getExerciseEditText();
                EditText weight = ((ExerciseModel) rowType).getWeightEditText();
                EditText sets = ((ExerciseModel) rowType).getSetsEditText();
                EditText reps = ((ExerciseModel) rowType).getRepsEditText();


                int countDecimal = weight.getText().toString().length() - weight.getText().toString().replace(".", "").length();
                int countPercent = weight.getText().toString().length() - weight.getText().toString().replace("%", "").length();

                //  error will be displayed if exercise, set, or rep does not have any input
                //  error will also be displayed if set = 0
                if (exercise.getText().toString().length() < 1) {
                    ((ExerciseModel) rowType).setExerciseError(true);
                    insertDataAndSave = false;
                }
                if (sets.getText().toString().length() < 1) {
                    ((ExerciseModel) rowType).setSetError(true);
                    insertDataAndSave = false;
                }
                if (sets.getText().toString().equals("0")){
                    ((ExerciseModel) rowType).setSetError(true);
                    insertDataAndSave = false;
                }
                if (reps.getText().toString().length() < 1) {
                    ((ExerciseModel) rowType).setRepError(true);
                    insertDataAndSave = false;
                }

                //  if weight has a percentage and the percentage is not at the end or is by itself, display error
                //  if weight has more than one percent sign, display error
                //  if weight starts with or ends with a decimal display error
                //  if weight has more than one decimal display error
                if(weight.getText().toString().length() == 1 && weight.getText().toString().equalsIgnoreCase("%") ||
                        weight.getText().toString().length() >= 2 && weight.getText().toString().contains("%") && !weight.getText().toString().endsWith("%") ||
                        weight.getText().toString().length() >= 2 && weight.getText().toString().contains("%") && countPercent > 1||
                        weight.getText().toString().startsWith(".") || weight.getText().toString().endsWith(".") ||
                        countDecimal > 1){
                    ((ExerciseModel) rowType).setWeightError(true);
                    insertDataAndSave = false;
                }
            }

            //  if viewholder = rest row, check to see if there is any input for minutes and seconds
            //  if not, display error
            else if (rowType.getClass() == RestRowModel.class) {
                EditText minutes = ((RestRowModel) rowType).getMinutesEditText();
                EditText seconds = ((RestRowModel) rowType).getSecondsEditText();


                if (minutes.getText().toString().length() < 1) {
                    ((RestRowModel) rowType).setMinuteError(true);
                    insertDataAndSave = false;
                }
                if (seconds.getText().toString().length() < 1) {
                    ((RestRowModel) rowType).setSecondError(true);
                    insertDataAndSave = false;
                }
            }

            //  if viewholder = superset row, iterate through children of superset row
            else if (rowType.getClass() == SupersetRowModel.class) {

                int supersetChildCount = ((SupersetRowModel) rowType).getNumberOfChildren();
                LinearLayout container = ((SupersetRowModel) rowType).getLinearLayout();
                List<ExerciseModel> exerciseModelList = ((SupersetRowModel) rowType).getExerciseModelList();
                ArrayList<String> exerciseError = new ArrayList<>();
                ArrayList<String> weightError = new ArrayList<>();
                ArrayList<String> setError = new ArrayList<>();
                ArrayList<String> repError = new ArrayList<>();
                boolean setErrorBoolean = false;

                for (int i = 0; i < supersetChildCount; i++) {

                    View view = container.getChildAt(i);

                    //  if child at i = exercise row, check to see if any input is there for exercise, sets, and reps
                    //  if not, display error
                    if (view.getId() == exerciseRow.getId()) {
                        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);
                        EditText sets = (EditText) view.findViewById(R.id.sets);
                        EditText reps = (EditText) view.findViewById(R.id.reps);
                        EditText weight = (EditText) view.findViewById(R.id.weight);

                        int countDecimal = weight.getText().toString().length() - weight.getText().toString().replace(".", "").length();
                        int countPercent = weight.getText().toString().length() - weight.getText().toString().replace("%", "").length();
                        if (autoCompleteTextView.getText().toString().length() < 1) {
                            //exerciseError.add(i, "Yes");
                            exerciseModelList.get(i).setExerciseError(true);
                            insertDataAndSave = false;
                        }
                        else{
                            exerciseModelList.get(i).setExerciseError(false);
                            //exerciseError.add(i, "No");
                        }

                        if (sets.getText().toString().length() < 1) {
                            //setErrorBoolean = true;
                            exerciseModelList.get(i).setSetError(true);
                            insertDataAndSave = false;
                        }

                        else if (sets.getText().toString().equals("0")){
                            //setErrorBoolean = true;
                            exerciseModelList.get(i).setSetError(true);
                            insertDataAndSave = false;
                        }

                        else{
                            exerciseModelList.get(i).setSetError(false);
                        }

                        if (reps.getText().toString().length() < 1) {
                            //repError.add(i, "Yes");
                            exerciseModelList.get(i).setRepError(true);
                            insertDataAndSave = false;
                        }
                        else{
                            exerciseModelList.get(i).setRepError(false);
                            //repError.add(i, "No");
                        }

                        /*if(setErrorBoolean){
                            setError.add(i, "Yes");
                        }

                        else{
                            setError.add(i, "No");
                        }*/

                        //  if weight has a percentage and the percentage is not at the end or is by itself, display error
                        //  if weight has more than one percent sign, display error
                        //  if weight starts with or ends with a decimal display error
                        //  if weight has more than one decimal display error
                        if(weight.getText().toString().length() == 1 && weight.getText().toString().equalsIgnoreCase("%") ||
                                weight.getText().toString().length() >= 2 && weight.getText().toString().contains("%") && !weight.getText().toString().endsWith("%") ||
                                weight.getText().toString().length() >= 2 && weight.getText().toString().contains("%") && countPercent > 1||
                                weight.getText().toString().startsWith(".") || weight.getText().toString().endsWith(".") ||
                                countDecimal > 1){
                            //weightError.add(i, "Yes");
                            exerciseModelList.get(i).setWeightError(true);
                            insertDataAndSave = false;
                        }
                        else{
                            exerciseModelList.get(i).setWeightError(false);
                            //weightError.add(i, "No");
                        }


                        /*((SupersetRowModel) rowType).setExerciseError(exerciseError);
                        ((SupersetRowModel) rowType).setWeightError(weightError);
                        ((SupersetRowModel) rowType).setSetError(setError);
                        ((SupersetRowModel) rowType).setRepError(repError);*/
                    }

                    //  if child at i = rest row, check to see if any input is there for minutes and seconds
                    //  if not, display error
                    else if (view.getId() == restRow.getId()) {
                        EditText minutes = (EditText) view.findViewById(R.id.minutesTV);
                        EditText seconds = (EditText) view.findViewById(R.id.secondsTV);


                        if (minutes.getText().toString().length() < 1) {
                            ((SupersetRowModel) rowType).getRestRowModel().setMinuteError(true);
                            insertDataAndSave = false;
                        }

                        else{
                            ((SupersetRowModel) rowType).getRestRowModel().setMinuteError(false);
                        }
                        if (seconds.getText().toString().length() < 1) {
                            ((SupersetRowModel) rowType).getRestRowModel().setSecondError(true);
                            insertDataAndSave = false;
                        }
                        else{
                            ((SupersetRowModel) rowType).getRestRowModel().setSecondError(false);
                        }
                    }
                }
            }

            //  if viewholder = pyramid set row, iterate through children of pyramid set row
            else if (rowType.getClass() == PyramidsetRowModel.class) {

                int count = ((PyramidsetRowModel) rowType).getNumberOfChildren();
                LinearLayout layout = ((PyramidsetRowModel) rowType).getLinearLayout();
                ArrayList<String> weightError = new ArrayList<>();
                ArrayList<String> repError = new ArrayList<>();

                for (int i = 0; i < count; i++) {

                    View view = layout.getChildAt(i);
                    //  if child at i = exercise row, check to see if any input is there for exercise, sets, and reps
                    //  if not, display error
                    if (view.getId() == exerciseRow.getId()) {
                        EditText weight = (EditText) view.findViewById(R.id.weight);
                        int countDecimal = weight.getText().toString().length() - weight.getText().toString().replace(".", "").length();
                        int countPercent = weight.getText().toString().length() - weight.getText().toString().replace("%", "").length();

                        //  if weight has a percentage and the percentage is not at the end or is by itself, display error
                        //  if weight has more than one percent sign, display error
                        //  if weight starts with or ends with a decimal display error
                        //  if weight has more than one decimal display error
                        if(weight.getText().toString().length() == 1 && weight.getText().toString().equalsIgnoreCase("%") ||
                                weight.getText().toString().length() >= 2 && weight.getText().toString().contains("%") && !weight.getText().toString().endsWith("%") ||
                                weight.getText().toString().length() >= 2 && weight.getText().toString().contains("%") && countPercent > 1||
                                weight.getText().toString().startsWith(".") || weight.getText().toString().endsWith(".") ||
                                countDecimal > 1){
                            weightError.add(i, "Yes");
                            insertDataAndSave = false;
                        }
                        else{
                            weightError.add(i, "No");
                        }
                        if(i == 0) {
                            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);
                            if (autoCompleteTextView.getText().toString().length() < 1) {
                                ((PyramidsetRowModel) rowType).setExerciseError(true);
                                insertDataAndSave = false;
                            }
                        }
                        EditText reps = (EditText) view.findViewById(R.id.reps);
                        if (reps.getText().toString().length() < 1) {
                            repError.add(i, "Yes");
                            insertDataAndSave = false;
                        }
                        else{
                            repError.add(i, "No");
                        }


                        ((PyramidsetRowModel) rowType).setWeightError(weightError);
                        ((PyramidsetRowModel) rowType).setRepError(repError);
                    }

                    //  if child at i = rest row, check to see if any input is there for minutes and seconds
                    //  if not, display error
                    else if (view.getId() == restRow.getId()) {
                        EditText minutes = (EditText) view.findViewById(R.id.minutesTV);
                        EditText seconds = (EditText) view.findViewById(R.id.secondsTV);


                        if (minutes.getText().toString().length() < 1) {
                            ((PyramidsetRowModel) rowType).setMinuteError(true);
                            insertDataAndSave = false;
                        }
                        if (seconds.getText().toString().length() < 1) {
                            ((PyramidsetRowModel) rowType).setSecondError(true);
                            insertDataAndSave = false;
                        }
                    }
                }
            }
        }


        if(insertDataAndSave) {

            SharedPreferences pref = home.activity.getSharedPreferences("time", Context.MODE_PRIVATE);
            pref.edit().putLong("timeElapsed", SystemClock.elapsedRealtime()).apply();

            displayWorkoutAdapter.startClicked = true;
            displayWorkoutAdapter.stopClicked = false;
            displayWorkoutAdapter.inputError = false;
            displayWorkoutAdapter.swipeListener = true;

            insertDataAndDisableEditText();
            home.recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    displayWorkoutAdapter.notifyDataSetChanged();
                }
            });
        }

        else{
            displayWorkoutAdapter.inputError = true;
            home.recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    displayWorkoutAdapter.notifyDataSetChanged();
                }
            });
        }

    }



}
