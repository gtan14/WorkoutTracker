package com.example.workouttracker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.os.Vibrator;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.stetho.inspector.protocol.module.Database;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


import static android.R.attr.order;
import static android.R.attr.width;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;


import static android.widget.Toast.makeText;
import static com.example.workouttracker.CountdownService.COUNTDOWN_SECONDS;
import static com.example.workouttracker.R.id.auto;
import static com.example.workouttracker.R.id.autoCompleteTextView;
import static com.example.workouttracker.R.id.benchMax;
import static com.example.workouttracker.R.id.exercise;
import static com.example.workouttracker.R.id.minutesTV;
import static com.example.workouttracker.R.id.snatchMax;
import static com.example.workouttracker.R.id.soundNotification;
import static com.example.workouttracker.R.id.time;
import static com.example.workouttracker.R.id.view;
import static com.example.workouttracker.R.layout.row;
import static com.example.workouttracker.R.layout.superset;
import static com.example.workouttracker.SQLiteHelper.TIME;


public class Home extends Fragment {

    LinearLayout mContainerView;
    private int index;
    private Button mAddButton;
    private Button addRest;
    private Button saveChanges;
    private Button start;
    private Button stop;
    private Button next;
    private Button editWorkout;
    private Button addPyramid;
    private Button resume;
    private Button addSuperset;
    private FrameLayout layout_MainMenu;
    private long startTime;
    private boolean closePw;
    private EditText secondsTV;
    private View secondsTVParent;
    private EditText minuteTV;
    private boolean startClick;
    private View pwView;
    private View pwOverrideView;
    private LayoutInflater layoutInflater;
    private int inputtedMin;
    private int inputtedSec;
    private String timeElapsed;
    MainActivity activity;
    EditText fileName;
    private boolean loadWorkout;
    ScrollView homeScroll;
    private Boolean startTimer;
    private Boolean titleSet;
    int mScrollDistance;
    int absoluteTop;
    int absoluteBottom;
    int eventTouchYCoord;
    View activityNav;
    TabLayout tabLayout;
    ExerciseModel onPauseExerciseModel;
    Drawable weightDrawable;
    Drawable exDrawable;
    Drawable setDrawable;
    Drawable repDrawable;
    Drawable minDrawable;
    Drawable secDrawable;
    long endTime;
    Intent serviceIntent;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("workoutTracker");
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private final String TAG = "FIREBASE";


    //Assigns activity to MainActivity context once Home is attached
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.d("attach", "attach");

        if(context instanceof MainActivity){
            activity = (MainActivity) context;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
        loadWorkout = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returns layout file
        View v= inflater.inflate(R.layout.row_container, container, false);
        return v;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }

    @Override
    public void onResume(){
        super.onResume();

        Log.d("resume", "resume");

        final String TABLE_NAME = "onpauseeeeeeeeeetable";
        SQLiteHelper sqLiteHelper = new SQLiteHelper(activity);
        boolean tableExists = sqLiteHelper.tableExists(TABLE_NAME);
        ArrayList<ExerciseModel> exerciseModelArrayList = new ArrayList<>();


        if(tableExists) {

            startClick = true;

            //activity.setTitle(activityTitleOnPause);
            addRest.setVisibility(View.GONE);
            mAddButton.setVisibility(View.GONE);
            saveChanges.setVisibility(View.GONE);
            addPyramid.setVisibility(View.GONE);
            addSuperset.setVisibility(View.GONE);
            next.setVisibility(View.VISIBLE);
            stop.setVisibility(View.VISIBLE);


            ArrayList<String> tableNames = sqLiteHelper.getTableNames();
            for(String a: tableNames)
                //If the table name matches the workout name
                //Change the title to the workout name and display home fragment
                if(a.equals(TABLE_NAME)) {
                    exerciseModelArrayList = sqLiteHelper.getAllRecords(a);
                }

            if (exerciseModelArrayList.size() > 0) {
                ExerciseModel exerciseModel;

                //iterates through each row and determines the view
                for (int i = 0; i < exerciseModelArrayList.size(); i++) {
                    exerciseModel = exerciseModelArrayList.get(i);
                    String view = exerciseModel.getView();

                    if(i == 0){
                        activity.setTitle(view);
                        titleSet = true;
                    }

                    //if view = 1, add exercise row without focus with corresponding exercise, weight, sets, and reps to mContainerView
                    if (view.equals("1")) {
                        String ex = exerciseModel.getExercise();
                        String weight = exerciseModel.getWeight();
                        String sets = exerciseModel.getSets();
                        String reps = exerciseModel.getReps();
                        inflateEditRowWOutFocus(ex, weight, sets, reps);
                    }

                    //if view = 2, add rest row without focus with corresponding minutes and seconds
                    else if (view.equals("2")) {
                        String minutes = exerciseModel.getMinutes();
                        String seconds = exerciseModel.getSeconds();
                        inflateRestRowWOutFocus(minutes, seconds);
                    }

                    //if view = 3, add superset view
                    else if (view.equals("3")) {
                        ArrayList<String> exercise = new ArrayList<>();
                        ArrayList<String> w = new ArrayList<>();
                        ArrayList<String> s = new ArrayList<>();
                        ArrayList<String> r = new ArrayList<>();
                        ArrayList<String> min = new ArrayList<>();
                        ArrayList<String> sec = new ArrayList<>();
                        String childCount = exerciseModel.getSets();
                        int numEx = Integer.parseInt(childCount);
                        ExerciseModel exModel;
                        //Checks the next row after the superset view and iterates the child count of the superset view
                        i++;
                        for (int a = i; a < i + numEx; a++) {
                            exModel = exerciseModelArrayList.get(a);
                            String v = exModel.getView();

                            //if view = 4, add exercise, weight, sets, and reps to their corresponding ArrayList
                            if (v.equals("4")) {
                                String ex = exModel.getExercise();
                                exercise.add(ex);
                                String weight = exModel.getWeight();
                                w.add(weight);
                                String sets = exModel.getSets();
                                s.add(sets);
                                String reps = exModel.getReps();
                                r.add(reps);
                            }

                            //if view = 5, add minutes and seconds to corresponding ArrayList
                            else if (v.equals("5")) {
                                String minutes = exModel.getMinutes();
                                min.add(minutes);
                                String seconds = exModel.getSeconds();
                                sec.add(seconds);
                            }
                        }
                        //Add the superset row with the corresponding ArrayLists
                        //numEx - 1 represents the number of exercises in the superset row
                        if (min.isEmpty() && sec.isEmpty()) {
                            inflateEditRowSupersetNoRest(exercise, w, s, r, numEx);
                        } else {
                            inflateEditRowSuperset(exercise, w, s, r, min, sec, numEx - 1);
                        }
                    }

                    //if view = 6, add pyramid set view
                    else if (view.equals("6")) {
                        ArrayList<String> exercisePyramid = new ArrayList<>();
                        ArrayList<String> wPyramid = new ArrayList<>();
                        ArrayList<String> sPyramid = new ArrayList<>();
                        ArrayList<String> rPyramid = new ArrayList<>();
                        ArrayList<String> minPyramid = new ArrayList<>();
                        ArrayList<String> secPyramid = new ArrayList<>();
                        String childCount = exerciseModel.getSets();
                        int numEx = Integer.parseInt(childCount);
                        ExerciseModel exModel;

                        //Checks the next row after the superset view and iterates the child count of the pyramid set view
                        i++;
                        for (int a = i; a < i + numEx; a++) {
                            exModel = exerciseModelArrayList.get(a);
                            String v = exModel.getView();

                            //if view = 4, add exercise, weight, sets, and reps to their corresponding ArrayList
                            if (v.equals("7")) {
                                String ex = exModel.getExercise();
                                exercisePyramid.add(ex);
                                String weight = exModel.getWeight();
                                wPyramid.add(weight);
                                String reps = exModel.getReps();
                                rPyramid.add(reps);
                            }

                            //if view = 8, add minutes and seconds to corresponding ArrayList
                            else if (v.equals("8")) {
                                String minutes = exModel.getMinutes();
                                minPyramid.add(minutes);
                                String seconds = exModel.getSeconds();
                                secPyramid.add(seconds);
                            }
                        }
                        //Add the superset row with the corresponding ArrayLists
                        //numEx - 1 represents the number of exercises in the superset row
                        if (minPyramid.isEmpty() && secPyramid.isEmpty()) {
                            inflatePyramidRowNoRest(exercisePyramid, wPyramid, rPyramid, numEx);
                        } else {
                            inflatePyramidRow(exercisePyramid, wPyramid, rPyramid, minPyramid, secPyramid, numEx - 1);
                        }
                    }

                }
                disableText();
            }

                    /*
        Initializes secondsTV and minuteTV so that the receivers know which textview to update
         */

            //if rest is for exercise row
            if(mContainerView.getChildAt(1).getId() == R.id.rest_row && mContainerView.getChildAt(0).getId() == R.id.exercise_row) {
                secondsTVParent = mContainerView.getChildAt(1);
                secondsTV = (EditText) secondsTVParent.findViewById(R.id.secondsTV);
                minuteTV = (EditText) secondsTVParent.findViewById(R.id.minutesTV);
            }

            //if rest is for itself
            else if(mContainerView.getChildAt(0).getId() == R.id.rest_row) {
                secondsTVParent = mContainerView.getChildAt(0);
                secondsTV = (EditText) secondsTVParent.findViewById(R.id.secondsTV);
                minuteTV = (EditText) secondsTVParent.findViewById(R.id.minutesTV);
            }

            //if rest is for a superset
            else if(mContainerView.getChildAt(0).getId() == R.id.supersetLayout){
                LinearLayout linearLayout = (LinearLayout) mContainerView.getChildAt(0);
                View possibleRestRow = linearLayout.getChildAt(linearLayout.getChildCount() - 1);
                if(possibleRestRow.getId() == R.id.rest_row){
                    secondsTVParent = possibleRestRow;
                    secondsTV = (EditText) secondsTVParent.findViewById(R.id.secondsTV);
                    minuteTV = (EditText) secondsTVParent.findViewById(R.id.minutesTV);
                }
            }

            //if rest is for a pyramid set
            else if(mContainerView.getChildAt(0).getId() == R.id.pyramidContainer){
                LinearLayout linearLayout = (LinearLayout) mContainerView.getChildAt(0);
                View possibleRestRow = linearLayout.getChildAt(linearLayout.getChildCount() - 1);
                if(possibleRestRow.getId() == R.id.rest_row){
                    secondsTVParent = possibleRestRow;
                    secondsTV = (EditText) secondsTVParent.findViewById(R.id.secondsTV);
                    minuteTV = (EditText) secondsTVParent.findViewById(R.id.minutesTV);
                }
            }

            if(isMyServiceRunning(CountdownService.class)){
                Intent countSec = new Intent(COUNTDOWN_SECONDS);
                activity.registerReceiver(secondReceiver, new IntentFilter(COUNTDOWN_SECONDS));
                activity.sendBroadcast(countSec);
            }
        }
    }


    @Override
    public void onPause(){
        super.onPause();

        if(startClick) {
            final String TABLE_NAME = "onpauseeeeeeeeeetable";

            final String exerciseView = "1";
            final String restView = "2";

            //Views for superset
            final String supersetView = "3";
            final String exerciseSuperset = "4";
            final String restSuperset = "5";

            //Views for pyramid set
            final String pyramidSetView = "6";
            final String exercisePyramidSet = "7";
            final String restPyramidSet = "8";

            SQLiteHelper sqLiteHelper = new SQLiteHelper(getActivity());
            boolean tableExists = sqLiteHelper.tableExists(TABLE_NAME);
            if(tableExists){
                sqLiteHelper.deleteTable(TABLE_NAME);
                sqLiteHelper.createOnPauseTable(TABLE_NAME);
            }
            else {
                sqLiteHelper.createOnPauseTable(TABLE_NAME);
            }

            onPauseExerciseModel = new ExerciseModel();
            onPauseExerciseModel.setView(activity.getTitle().toString());
            sqLiteHelper.insertRecordExercise(onPauseExerciseModel, TABLE_NAME);

            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
            View exerciseRow = layoutInflater.inflate(R.layout.row, null);
            View restRow = layoutInflater.inflate(R.layout.rest_row, null);
            View superset = layoutInflater.inflate(R.layout.superset, null);
            View pyramidRow = layoutInflater.inflate(R.layout.pyramidset, null);
            int numberOfViews = mContainerView.getChildCount() - 10;

                for (int a = 0; a <= numberOfViews; a++) {
                    if (mContainerView.getChildAt(a).getId() == exerciseRow.getId()) {
                        View v = mContainerView.getChildAt(a);
                        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);
                        EditText sets = (EditText) v.findViewById(R.id.sets);
                        EditText reps = (EditText) v.findViewById(R.id.reps);
                        EditText weight = (EditText) v.findViewById(R.id.weight);

                        onPauseExerciseModel.setView(exerciseView);
                        onPauseExerciseModel.setExercise(autoCompleteTextView.getText().toString());
                        onPauseExerciseModel.setWeight(weight.getText().toString());
                        onPauseExerciseModel.setSets(sets.getText().toString());
                        onPauseExerciseModel.setReps(reps.getText().toString());
                        sqLiteHelper.insertRecordExercise(onPauseExerciseModel, TABLE_NAME);


                    } else if (mContainerView.getChildAt(a).getId() == restRow.getId()) {
                        View v = mContainerView.getChildAt(a);
                        EditText minutes = (EditText) v.findViewById(minutesTV);
                        EditText seconds = (EditText) v.findViewById(R.id.secondsTV);

                        onPauseExerciseModel.setView(restView);
                        onPauseExerciseModel.setMinutes(minutes.getText().toString());
                        onPauseExerciseModel.setSeconds(seconds.getText().toString());
                        sqLiteHelper.insertRecordRest(onPauseExerciseModel, TABLE_NAME);

                    } else if (mContainerView.getChildAt(a).getId() == superset.getId()) {
                        View supersetContainer = mContainerView.getChildAt(a);
                        LinearLayout layout = (LinearLayout) supersetContainer.findViewById(R.id.supersetLayout);

                        onPauseExerciseModel.setView(supersetView);
                        onPauseExerciseModel.setSets(String.format("%s", layout.getChildCount()));
                        sqLiteHelper.insertRecordSuperset(onPauseExerciseModel, TABLE_NAME);
                        for (int i = 0; i < layout.getChildCount(); i++) {
                            if (layout.getChildAt(i).getId() == exerciseRow.getId()) {
                                View v = layout.getChildAt(i);
                                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);
                                EditText sets = (EditText) v.findViewById(R.id.sets);
                                EditText reps = (EditText) v.findViewById(R.id.reps);
                                EditText weight = (EditText) v.findViewById(R.id.weight);

                                onPauseExerciseModel.setView(exerciseSuperset);
                                onPauseExerciseModel.setExercise(autoCompleteTextView.getText().toString());
                                onPauseExerciseModel.setWeight(weight.getText().toString());
                                onPauseExerciseModel.setSets(sets.getText().toString());
                                onPauseExerciseModel.setReps(reps.getText().toString());
                                sqLiteHelper.insertRecordExercise(onPauseExerciseModel, TABLE_NAME);
                            } else if (layout.getChildAt(i).getId() == restRow.getId()) {
                                View v = layout.getChildAt(i);
                                EditText minutes = (EditText) v.findViewById(minutesTV);
                                EditText seconds = (EditText) v.findViewById(R.id.secondsTV);

                                onPauseExerciseModel.setView(restSuperset);
                                onPauseExerciseModel.setMinutes(minutes.getText().toString());
                                onPauseExerciseModel.setSeconds(seconds.getText().toString());
                                sqLiteHelper.insertRecordRest(onPauseExerciseModel, TABLE_NAME);
                            }
                        }

                    }

                    //if child at a = pyramid set row, iterate through children
                    else if (mContainerView.getChildAt(a).getId() == pyramidRow.getId()) {
                        View pyramidContainer = mContainerView.getChildAt(a);
                        LinearLayout layout = (LinearLayout) pyramidContainer.findViewById(R.id.pyramidContainer);

                        //sets the view = pyramidSetView and assigns the child count of pyramid row to sets
                        onPauseExerciseModel.setView(pyramidSetView);
                        onPauseExerciseModel.setSets(String.format("%s", layout.getChildCount()));
                        sqLiteHelper.insertRecordSuperset(onPauseExerciseModel, TABLE_NAME);
                        for (int i = 0; i < layout.getChildCount(); i++) {
                            if (layout.getChildAt(i).getId() == exerciseRow.getId()) {
                                View v = layout.getChildAt(i);
                                EditText sets = (EditText) v.findViewById(R.id.sets);
                                EditText reps = (EditText) v.findViewById(R.id.reps);
                                EditText weight = (EditText) v.findViewById(R.id.weight);

                                onPauseExerciseModel.setView(exercisePyramidSet);
                                if (i == 0) {
                                    AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);
                                    onPauseExerciseModel.setExercise(autoCompleteTextView.getText().toString());
                                }

                                onPauseExerciseModel.setWeight(weight.getText().toString());
                                onPauseExerciseModel.setSets(sets.getText().toString());
                                onPauseExerciseModel.setReps(reps.getText().toString());
                                sqLiteHelper.insertRecordExercise(onPauseExerciseModel, TABLE_NAME);
                            } else if (layout.getChildAt(i).getId() == restRow.getId()) {
                                View v = layout.getChildAt(i);
                                EditText minutes = (EditText) v.findViewById(minutesTV);
                                EditText seconds = (EditText) v.findViewById(R.id.secondsTV);

                                onPauseExerciseModel.setView(restPyramidSet);
                                onPauseExerciseModel.setMinutes(minutes.getText().toString());
                                onPauseExerciseModel.setSeconds(seconds.getText().toString());
                                sqLiteHelper.insertRecordRest(onPauseExerciseModel, TABLE_NAME);
                            }
                        }
                    }
                }
        }

    }

    //Initializes views
    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        layout_MainMenu = (FrameLayout) v.findViewById(R.id.mainmenu);
        layoutInflater = activity.getLayoutInflater();
        pwView = layoutInflater.inflate(R.layout.save_popup_window, null);
        mContainerView = (LinearLayout) v.findViewById(R.id.parentView);
        View rest = layoutInflater.inflate(R.layout.rest_row, null);
        secondsTV = (EditText) rest.findViewById(R.id.secondsTV);
        minuteTV = (EditText) rest.findViewById(R.id.minutesTV);

        pwOverrideView = layoutInflater.inflate(R.layout.save_override, null);
        activityNav = layoutInflater.inflate(R.layout.app_bar_nav_drawer, null);
        tabLayout = (TabLayout) activityNav.findViewById(R.id.tabs);



        mAddButton = (Button) v.findViewById(R.id.addNew);
        addSuperset = (Button) v.findViewById(R.id.supersetButton);
        homeScroll = (ScrollView) v.findViewById(R.id.homeScroll);
        addRest = (Button) v.findViewById(R.id.addRest);
        saveChanges = (Button) v.findViewById(R.id.saveChanges);
        start = (Button) v.findViewById(R.id.start_workout);
        stop = (Button) v.findViewById(R.id.stop_workout);
        next = (Button) v.findViewById(R.id.next_exercise);
        editWorkout = (Button) v.findViewById(R.id.edit_workout);
        addPyramid = (Button) v.findViewById(R.id.addPyramid);
        resume = (Button) v.findViewById(R.id.resume);

    }

    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        serviceIntent = new Intent(activity, CountdownService.class);

        //sets swipe listener for scrollview
        swipeLeft(homeScroll);


        //Initializes new ArrayList<String> for each column of userTable
        //((MainActivity) getActivity()).sqLiteHelper = new SQLiteHelper(getContext());

        homeScroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                homeScroll.getViewTreeObserver().removeOnScrollChangedListener(this);
                mScrollDistance = homeScroll.getScrollY();
                int[] coords = new int[2];
                homeScroll.getLocationOnScreen(coords);
                absoluteTop = coords[1];
                absoluteBottom = coords[1] + homeScroll.getHeight();
            }
        });


        //if workout from SavedWorkouts fragment is clicked
        if(loadWorkout){

            //sets the title, hides buttons, and disables text
            activity.setTitle(activity.titleName);
            titleSet = true;
            mAddButton.setVisibility(View.GONE);
            addSuperset.setVisibility(View.GONE);
            addPyramid.setVisibility(View.GONE);
            addRest.setVisibility(View.GONE);
            saveChanges.setVisibility(View.GONE);
            start.setVisibility(View.VISIBLE);
            editWorkout.setVisibility(View.VISIBLE);

            FirebaseDatabase.getInstance().getReference().child("workoutTracker").child("workouts").child(activity.getTitle().toString())
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        ArrayList<String> exercise;
                        ArrayList<String> w;
                        ArrayList<String> s;
                        ArrayList<String> r;
                        ArrayList<String> min;
                        ArrayList<String> sec;

                        ArrayList<String> exercisePyramid;
                        ArrayList<String> wPyramid;
                        ArrayList<String> sPyramid;
                        ArrayList<String> rPyramid;
                        ArrayList<String> minPyramid;
                        ArrayList<String> secPyramid;

                        int childCountInt = 0;
                        int index = 0;

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
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                ExerciseModel exerciseModel = snapshot.getValue(ExerciseModel.class);
                                if (exerciseModel.getView().equals(exerciseView)) {
                                    String ex = exerciseModel.getExercise();
                                    String weight = exerciseModel.getWeight();
                                    String sets = exerciseModel.getSets();
                                    String reps = exerciseModel.getReps();
                                    inflateEditRowWOutFocus(ex, weight, sets, reps);
                                }

                                //if view = 2, add rest row without focus with corresponding minutes and seconds
                                else if (exerciseModel.getView().equals(restView)){
                                    String minutes = exerciseModel.getMinutes();
                                    String seconds = exerciseModel.getSeconds();
                                    inflateRestRowWOutFocus(minutes, seconds);
                                }

                                //if view = 3, add superset view
                                else if(exerciseModel.getView().equals(supersetView)) {
                                    exercise = new ArrayList<>();
                                    w = new ArrayList<>();
                                    s = new ArrayList<>();
                                    r = new ArrayList<>();
                                    min = new ArrayList<>();
                                    sec = new ArrayList<>();
                                    String childCountString = exerciseModel.getChildCount();
                                    childCountInt = Integer.parseInt(childCountString);
                                }


                                else if(exerciseModel.getView().equals(exerciseSuperset)){
                                    String ex = exerciseModel.getExercise();
                                    exercise.add(ex);
                                    String weight = exerciseModel.getWeight();
                                    w.add(weight);
                                    String sets = exerciseModel.getSets();
                                    s.add(sets);
                                    String reps = exerciseModel.getReps();
                                    r.add(reps);
                                    index++;
                                    if(index == childCountInt) {
                                        inflateEditRowSupersetNoRest(exercise, w, s, r, childCountInt);
                                        childCountInt = 0;
                                        index = 0;
                                        exercise.clear();
                                        w.clear();
                                        s.clear();
                                        r.clear();
                                    }

                                }

                                //if view = 5, add minutes and seconds to corresponding ArrayList
                                else if(exerciseModel.getView().equals(restSuperset)) {
                                    String minutes = exerciseModel.getMinutes();
                                    min.add(minutes);
                                    String seconds = exerciseModel.getSeconds();
                                    sec.add(seconds);
                                    index++;
                                    if(index == childCountInt) {
                                        inflateEditRowSuperset(exercise, w, s, r, min, sec, childCountInt - 1);
                                        childCountInt = 0;
                                        index = 0;
                                        exercise.clear();
                                        w.clear();
                                        s.clear();
                                        r.clear();
                                    }

                                }

                                //if view = 6, add pyramid set view
                                else if(exerciseModel.getView().equals(pyramidSetView)) {
                                    exercisePyramid = new ArrayList<>();
                                    wPyramid = new ArrayList<>();
                                    sPyramid = new ArrayList<>();
                                    rPyramid = new ArrayList<>();
                                    minPyramid = new ArrayList<>();
                                    secPyramid = new ArrayList<>();
                                    String childCount = exerciseModel.getChildCount();
                                    childCountInt = Integer.parseInt(childCount);
                                }

                                        //if view = 4, add exercise, weight, sets, and reps to their corresponding ArrayList
                                else if(exerciseModel.getView().equals(exercisePyramidSet)){
                                    String ex = exerciseModel.getExercise();
                                    exercisePyramid.add(ex);
                                    String weight = exerciseModel.getWeight();
                                    wPyramid.add(weight);
                                    String reps = exerciseModel.getReps();
                                    rPyramid.add(reps);
                                    index++;
                                    if(index == childCountInt){
                                        inflatePyramidRowNoRest(exercisePyramid, wPyramid, rPyramid, childCountInt);
                                        childCountInt = 0;
                                        index = 0;
                                        exercisePyramid.clear();
                                        wPyramid.clear();
                                        rPyramid.clear();
                                    }
                                }

                                        //if view = 8, add minutes and seconds to corresponding ArrayList
                                else if(exerciseModel.getView().equals(restPyramidSet)) {
                                    String minutes = exerciseModel.getMinutes();
                                    minPyramid.add(minutes);
                                    String seconds = exerciseModel.getSeconds();
                                    secPyramid.add(seconds);
                                    index++;
                                    if(index == childCountInt){
                                        inflatePyramidRow(exercisePyramid, wPyramid, rPyramid, minPyramid, secPyramid, childCountInt - 1);
                                        childCountInt = 0;
                                        index = 0;
                                        exercisePyramid.clear();
                                        wPyramid.clear();
                                        rPyramid.clear();
                                        minPyramid.clear();
                                        secPyramid.clear();
                                    }
                                }
                            }
                            disableText();
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

            /*if (((MainActivity) getActivity()).exercise.size() > 0) {
                ExerciseModel exerciseModel;

                //iterates through each row and determines the view
                for (int i = 0; i < ((MainActivity) getActivity()).exercise.size(); i++) {
                    exerciseModel = ((MainActivity) getActivity()).exercise.get(i);
                    String view = exerciseModel.getView();

                    //if view = 1, add exercise row without focus with corresponding exercise, weight, sets, and reps to mContainerView
                    if (view.equals("1")) {
                        String ex = exerciseModel.getExercise();
                        String weight = exerciseModel.getWeight();
                        String sets = exerciseModel.getSets();
                        String reps = exerciseModel.getReps();
                        inflateEditRowWOutFocus(ex, weight, sets, reps);
                    }

                    //if view = 2, add rest row without focus with corresponding minutes and seconds
                    else if (view.equals("2")){
                        String minutes = exerciseModel.getMinutes();
                        String seconds = exerciseModel.getSeconds();
                        inflateRestRowWOutFocus(minutes, seconds);
                    }

                    //if view = 3, add superset view
                    else if(view.equals("3")){
                        ArrayList<String> exercise = new ArrayList<>();
                        ArrayList<String> w = new ArrayList<>();
                        ArrayList<String> s = new ArrayList<>();
                        ArrayList<String> r = new ArrayList<>();
                        ArrayList<String> min = new ArrayList<>();
                        ArrayList<String> sec = new ArrayList<>();
                        String childCount = exerciseModel.getSets();
                        int numEx = Integer.parseInt(childCount);
                        ExerciseModel exModel;
                        //Checks the next row after the superset view and iterates the child count of the superset view
                        i++;
                        for(int a = i; a < i + numEx; a++){
                            exModel = ((MainActivity) getActivity()).exercise.get(a);
                            String v = exModel.getView();

                            //if view = 4, add exercise, weight, sets, and reps to their corresponding ArrayList
                            if(v.equals("4")){
                                String ex = exModel.getExercise();
                                exercise.add(ex);
                                String weight = exModel.getWeight();
                                w.add(weight);
                                String sets = exModel.getSets();
                                s.add(sets);
                                String reps = exModel.getReps();
                                r.add(reps);
                            }

                            //if view = 5, add minutes and seconds to corresponding ArrayList
                            else if(v.equals("5")) {
                                String minutes = exModel.getMinutes();
                                min.add(minutes);
                                String seconds = exModel.getSeconds();
                                sec.add(seconds);
                            }
                        }
                        //Add the superset row with the corresponding ArrayLists
                        //numEx - 1 represents the number of exercises in the superset row
                        if(min.isEmpty() && sec.isEmpty()){
                            inflateEditRowSupersetNoRest(exercise, w, s, r, numEx);
                        }
                        else {
                            inflateEditRowSuperset(exercise, w, s, r, min, sec, numEx - 1);
                        }
                    }

                    //if view = 6, add pyramid set view
                    else if(view.equals("6")){
                        ArrayList<String> exercisePyramid = new ArrayList<>();
                        ArrayList<String> wPyramid = new ArrayList<>();
                        ArrayList<String> sPyramid = new ArrayList<>();
                        ArrayList<String> rPyramid = new ArrayList<>();
                        ArrayList<String> minPyramid = new ArrayList<>();
                        ArrayList<String> secPyramid = new ArrayList<>();
                        String childCount = exerciseModel.getSets();
                        int numEx = Integer.parseInt(childCount);
                        ExerciseModel exModel;

                        //Checks the next row after the superset view and iterates the child count of the pyramid set view
                        i++;
                        for(int a = i; a < i + numEx; a++){
                            exModel = ((MainActivity) getActivity()).exercise.get(a);
                            String v = exModel.getView();

                            //if view = 4, add exercise, weight, sets, and reps to their corresponding ArrayList
                            if(v.equals("7")){
                                String ex = exModel.getExercise();
                                exercisePyramid.add(ex);
                                String weight = exModel.getWeight();
                                wPyramid.add(weight);
                                String reps = exModel.getReps();
                                rPyramid.add(reps);
                            }

                            //if view = 8, add minutes and seconds to corresponding ArrayList
                            else if(v.equals("8")) {
                                String minutes = exModel.getMinutes();
                                minPyramid.add(minutes);
                                String seconds = exModel.getSeconds();
                                secPyramid.add(seconds);
                            }
                        }
                        //Add the superset row with the corresponding ArrayLists
                        //numEx - 1 represents the number of exercises in the superset row
                        if(minPyramid.isEmpty() && secPyramid.isEmpty()){
                            inflatePyramidRowNoRest(exercisePyramid, wPyramid, rPyramid, numEx);
                        }
                        else {
                            inflatePyramidRow(exercisePyramid, wPyramid, rPyramid, minPyramid, secPyramid, numEx - 1);
                        }
                    }
                }
            }

            disableText();*/
        }
        else{
            titleSet = false;
        }

        if(!titleSet)
            activity.setTitle("Home");

        startTimer = false;
        closePw = false;
        startClick = false;

        //used to keep track of the current position in the superset row
        index = 0;

        //layout is 100% opacity
        layout_MainMenu.getForeground().setAlpha(0);


        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inflateEditRow(null, null, null, null);
            }
        });

        addSuperset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exerciseNumSuperset();
            }
        });

        addPyramid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNumPyramidSet();
            }
        });


        addRest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inflateRestRow(null, null);
            }
        });


        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                boolean showPw = true;
                LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
                View exerciseRow = layoutInflater.inflate(R.layout.row, null);
                View restRow = layoutInflater.inflate(R.layout.rest_row, null);
                View superset = layoutInflater.inflate(R.layout.superset, null);
                View pyramidRow = layoutInflater.inflate(R.layout.pyramidset, null);
                int numberOfViews = mContainerView.getChildCount() - 10;

                //checks for all the children of mContainerView excluding the buttons
                for (int a = 0; a <= numberOfViews; a++) {

                    //if the first row = exercise row, check to see if there is any input in the exercise, sets, and reps field
                    //if not, display error
                    if (mContainerView.getChildAt(a).getId() == exerciseRow.getId()) {
                        View v = mContainerView.getChildAt(a);
                        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);
                        EditText sets = (EditText) v.findViewById(R.id.sets);
                        EditText reps = (EditText) v.findViewById(R.id.reps);
                        EditText weight = (EditText) v.findViewById(R.id.weight);
                        int countDecimal = weight.getText().toString().length() - weight.getText().toString().replace(".", "").length();
                        int countPercent = weight.getText().toString().length() - weight.getText().toString().replace("%", "").length();
                        if (autoCompleteTextView.getText().toString().length() < 1) {
                            showPw = false;
                            autoCompleteTextView.setError("Enter an exercise");
                            autoCompleteTextView.setHint("");
                        }
                        if (sets.getText().toString().length() < 1) {
                            showPw = false;
                            sets.setError("Enter # sets");
                            sets.setHint("");
                        }
                        if (sets.getText().toString().equals("0")){
                            showPw = false;
                            sets.setError("0 is not valid");
                            sets.setText("");
                        }
                        if (reps.getText().toString().length() < 1) {
                            showPw = false;
                            reps.setError("Enter # reps");
                            reps.setHint("");
                        }

                        //if weight has a percentage and the percentage is not at the end or is by itself, display error
                        //if weight has more than one percent sign, display error
                        //if weight starts with or ends with a decimal display error
                        //if weight has more than one decimal display error
                        if(weight.getText().toString().length() == 1 && weight.getText().toString().equalsIgnoreCase("%") ||
                                weight.getText().toString().length() >= 2 && weight.getText().toString().contains("%") && !weight.getText().toString().endsWith("%") ||
                                weight.getText().toString().length() >= 2 && weight.getText().toString().contains("%") && countPercent > 1||
                                weight.getText().toString().startsWith(".") || weight.getText().toString().endsWith(".") ||
                                countDecimal > 1){
                            showPw = false;
                            weight.setError("Change format");
                            weight.setHint("");
                        }
                    }

                    //if the first row = rest row, check to see if there is any input for minutes and seconds
                    //if not, display error
                    else if (mContainerView.getChildAt(a).getId() == restRow.getId()) {
                        View v = mContainerView.getChildAt(a);
                        EditText minutes = (EditText) v.findViewById(minutesTV);
                        EditText seconds = (EditText) v.findViewById(R.id.secondsTV);
                        if (minutes.getText().toString().length() < 1) {
                            showPw = false;
                            minutes.setError("Enter minutes");
                            minutes.setHint("");
                        }
                        if (seconds.getText().toString().length() < 1) {
                            showPw = false;
                            seconds.setError("Enter seconds");
                            seconds.setHint("");
                        }
                    }

                    //if first row = superset row, iterate through children of superset row
                    else if (mContainerView.getChildAt(a).getId() == superset.getId()) {
                        View v = mContainerView.getChildAt(a);
                        LinearLayout layout = (LinearLayout) v.findViewById(R.id.supersetLayout);
                        for (int i = 0; i < layout.getChildCount(); i++) {

                            //if child at i = exercise row, check to see if any input is there for exercise, sets, and reps
                            //if not, display error
                            if (layout.getChildAt(i).getId() == exerciseRow.getId()) {
                                View s = layout.getChildAt(i);
                                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) s.findViewById(R.id.autoCompleteTextView);
                                EditText sets = (EditText) s.findViewById(R.id.sets);
                                EditText reps = (EditText) s.findViewById(R.id.reps);
                                EditText weight = (EditText) s.findViewById(R.id.weight);
                                int countDecimal = weight.getText().toString().length() - weight.getText().toString().replace(".", "").length();
                                int countPercent = weight.getText().toString().length() - weight.getText().toString().replace("%", "").length();
                                if (autoCompleteTextView.getText().toString().length() < 1) {
                                    showPw = false;
                                    autoCompleteTextView.setError("Enter an exercise");
                                    autoCompleteTextView.setHint("");
                                }
                                if (sets.getText().toString().length() < 1) {
                                    showPw = false;
                                    sets.setError("Enter # sets");
                                    sets.setHint("");
                                }
                                if (sets.getText().toString().equals("0")){
                                    showPw = false;
                                    sets.setError("0 is not valid");
                                    sets.setText("");
                                }
                                if (reps.getText().toString().length() < 1) {
                                    showPw = false;
                                    reps.setError("Enter # reps");
                                    reps.setHint("");
                                }

                                //if weight has a percentage and the percentage is not at the end or is by itself, display error
                                //if weight has more than one percent sign, display error
                                //if weight starts with or ends with a decimal display error
                                //if weight has more than one decimal display error
                                if(weight.getText().toString().length() == 1 && weight.getText().toString().equalsIgnoreCase("%") ||
                                        weight.getText().toString().length() >= 2 && weight.getText().toString().contains("%") && !weight.getText().toString().endsWith("%") ||
                                        weight.getText().toString().length() >= 2 && weight.getText().toString().contains("%") && countPercent > 1||
                                        weight.getText().toString().startsWith(".") || weight.getText().toString().endsWith(".") ||
                                        countDecimal > 1){
                                    showPw = false;
                                    weight.setError("Change format");
                                    weight.setHint("");
                                }
                            }

                            //if child at i = rest row, check to see if any input is there for minutes and seconds
                            //if not, display error
                            else if (layout.getChildAt(i).getId() == restRow.getId()) {
                                View z = layout.getChildAt(i);
                                EditText minutes = (EditText) z.findViewById(R.id.minutesTV);
                                EditText seconds = (EditText) z.findViewById(R.id.secondsTV);
                                if (minutes.getText().toString().length() < 1) {
                                    showPw = false;
                                    minutes.setError("Enter minutes");
                                    minutes.setHint("");
                                }
                                if (seconds.getText().toString().length() < 1) {
                                    showPw = false;
                                    seconds.setError("Enter seconds");
                                    seconds.setHint("");
                                }
                            }
                        }
                    }

                    //if first row = pyramid set row, iterate through children of pyramid set row
                    else if (mContainerView.getChildAt(a).getId() == pyramidRow.getId()) {
                        View v = mContainerView.getChildAt(a);
                        LinearLayout layout = (LinearLayout) v.findViewById(R.id.pyramidContainer);
                        for (int i = 0; i < layout.getChildCount(); i++) {

                            //if child at i = exercise row, check to see if any input is there for exercise, sets, and reps
                            //if not, display error
                            if (layout.getChildAt(i).getId() == exerciseRow.getId()) {
                                View s = layout.getChildAt(i);
                                EditText weight = (EditText) s.findViewById(R.id.weight);
                                int countDecimal = weight.getText().toString().length() - weight.getText().toString().replace(".", "").length();
                                int countPercent = weight.getText().toString().length() - weight.getText().toString().replace("%", "").length();
                                //if weight has a percentage and the percentage is not at the end or is by itself, display error
                                //if weight has more than one percent sign, display error
                                //if weight starts with or ends with a decimal display error
                                //if weight has more than one decimal display error
                                if(weight.getText().toString().length() == 1 && weight.getText().toString().equalsIgnoreCase("%") ||
                                        weight.getText().toString().length() >= 2 && weight.getText().toString().contains("%") && !weight.getText().toString().endsWith("%") ||
                                        weight.getText().toString().length() >= 2 && weight.getText().toString().contains("%") && countPercent > 1||
                                        weight.getText().toString().startsWith(".") || weight.getText().toString().endsWith(".") ||
                                        countDecimal > 1){
                                    showPw = false;
                                    weight.setError("Change format");
                                    weight.setHint("");
                                }
                                if(i == 0) {
                                    AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) s.findViewById(R.id.autoCompleteTextView);
                                    if (autoCompleteTextView.getText().toString().length() < 1) {
                                        showPw = false;
                                        autoCompleteTextView.setError("Enter an exercise");
                                        autoCompleteTextView.setHint("");
                                    }
                                }
                                EditText reps = (EditText) s.findViewById(R.id.reps);
                                if (reps.getText().toString().length() < 1) {
                                    showPw = false;
                                    reps.setError("Enter # reps");
                                    reps.setHint("");
                                }
                            }

                            //if child at i = rest row, check to see if any input is there for minutes and seconds
                            //if not, display error
                            else if (layout.getChildAt(i).getId() == restRow.getId()) {
                                View z = layout.getChildAt(i);
                                EditText minutes = (EditText) z.findViewById(R.id.minutesTV);
                                EditText seconds = (EditText) z.findViewById(R.id.secondsTV);
                                if (minutes.getText().toString().length() < 1) {
                                    showPw = false;
                                    minutes.setError("Enter minutes");
                                    minutes.setHint("");
                                }
                                if (seconds.getText().toString().length() < 1) {
                                    showPw = false;
                                    seconds.setError("Enter seconds");
                                    seconds.setHint("");
                                }
                            }
                        }
                    }
                }

                next.setVisibility(View.GONE);


                //if current view is a loaded workout
                if(titleSet && showPw) {

                    String tableName = activity.getTitle().toString();
                    myRef.child("workouts").child(tableName).removeValue();
                    //dont display a popup window for saving
                    showPw = false;

                    addRest.setVisibility(View.GONE);
                    mAddButton.setVisibility(View.GONE);
                    saveChanges.setVisibility(View.GONE);
                    addPyramid.setVisibility(View.GONE);
                    addSuperset.setVisibility(View.GONE);
                    start.setVisibility(View.VISIBLE);
                    editWorkout.setVisibility(View.VISIBLE);

                    disableText();


                    for (int a = 0; a <= numberOfViews; a++) {
                        if (mContainerView.getChildAt(a).getId() == exerciseRow.getId()) {
                            View v = mContainerView.getChildAt(a);
                            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);
                            EditText sets = (EditText) v.findViewById(R.id.sets);
                            EditText reps = (EditText) v.findViewById(R.id.reps);
                            EditText weight = (EditText) v.findViewById(R.id.weight);

                            String exerciseString = autoCompleteTextView.getText().toString();
                            String weightString = weight.getText().toString();
                            String setString = sets.getText().toString();
                            String repsString = reps.getText().toString();
                            insertDataToTable(tableName, exerciseView, null, exerciseString, weightString, setString, repsString, null, null, "0");


                        } else if (mContainerView.getChildAt(a).getId() == restRow.getId()) {
                            View v = mContainerView.getChildAt(a);
                            EditText minutes = (EditText) v.findViewById(minutesTV);
                            EditText seconds = (EditText) v.findViewById(R.id.secondsTV);
                            String minutesString = minutes.getText().toString();
                            String secondsString = seconds.getText().toString();

                            insertDataToTable(tableName, restView, null, null, null, null, null, minutesString, secondsString, "0");

                        }
                        else if (mContainerView.getChildAt(a).getId() == superset.getId()){
                            View supersetContainer = mContainerView.getChildAt(a);
                            LinearLayout layout = (LinearLayout) supersetContainer.findViewById(R.id.supersetLayout);

                            String childCountString = String.format("%s", layout.getChildCount());
                            insertDataToTable(tableName, supersetView, null, null, null, null, null, null, null, childCountString);
                            for(int i = 0; i < layout.getChildCount(); i++){
                                if(layout.getChildAt(i).getId() == exerciseRow.getId()){
                                    View v = layout.getChildAt(i);
                                    AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);
                                    EditText sets = (EditText) v.findViewById(R.id.sets);
                                    EditText reps = (EditText) v.findViewById(R.id.reps);
                                    EditText weight = (EditText) v.findViewById(R.id.weight);
                                    String exerciseString = autoCompleteTextView.getText().toString();
                                    String weightString = weight.getText().toString();
                                    String setString = sets.getText().toString();
                                    String repsString = reps.getText().toString();

                                    insertDataToTable(tableName, exerciseSuperset, null, exerciseString, weightString, setString, repsString, null, null, childCountString);

                                }

                                else if(layout.getChildAt(i).getId() == restRow.getId()){
                                    View v = layout.getChildAt(i);
                                    EditText minutes = (EditText) v.findViewById(minutesTV);
                                    EditText seconds = (EditText) v.findViewById(R.id.secondsTV);
                                    String minutesString = minutes.getText().toString();
                                    String secondsString = seconds.getText().toString();

                                    insertDataToTable(tableName, restSuperset, null, null, null, null, null, minutesString, secondsString, childCountString);

                                }
                            }

                        }

                        //if child at a = pyramid set row, iterate through children
                        else if (mContainerView.getChildAt(a).getId() == pyramidRow.getId()){
                            View pyramidContainer = mContainerView.getChildAt(a);
                            LinearLayout layout = (LinearLayout) pyramidContainer.findViewById(R.id.pyramidContainer);

                            //sets the view = pyramidSetView and assigns the child count of pyramid row to sets
                            String childCountString = String.format("%s", layout.getChildCount());
                            insertDataToTable(tableName, pyramidSetView, null, null, null, null, null, null, null, childCountString);
                            for(int i = 0; i < layout.getChildCount(); i++){
                                if(layout.getChildAt(i).getId() == exerciseRow.getId()){
                                    View v = layout.getChildAt(i);
                                    EditText sets = (EditText) v.findViewById(R.id.sets);
                                    EditText reps = (EditText) v.findViewById(R.id.reps);
                                    EditText weight = (EditText) v.findViewById(R.id.weight);
                                    AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);
                                    String exerciseString = autoCompleteTextView.getText().toString();
                                    String weightString = weight.getText().toString();
                                    String setString = sets.getText().toString();
                                    String repsString = reps.getText().toString();

                                    insertDataToTable(tableName, exercisePyramidSet, null, exerciseString, weightString, setString, repsString, null, null, childCountString);
                                }

                                else if(layout.getChildAt(i).getId() == restRow.getId()){
                                    View v = layout.getChildAt(i);
                                    EditText minutes = (EditText) v.findViewById(minutesTV);
                                    EditText seconds = (EditText) v.findViewById(R.id.secondsTV);
                                    String minutesString = minutes.getText().toString();
                                    String secondsString = seconds.getText().toString();

                                    insertDataToTable(tableName, restPyramidSet, null, null, null, null, null, minutesString, secondsString, childCountString);
                                }
                            }

                        }
                    }
                }

                if (showPw) {

                    //darken the background once the popup window shows
                    layout_MainMenu.getForeground().setAlpha(220);

                    Button cancel = (Button) pwView.findViewById(R.id.cancel);
                    Button save = (Button) pwView.findViewById(R.id.save);
                    final TextInputLayout fileNameWrapper = (TextInputLayout) pwView.findViewById(R.id.saveTextInputLayout);

                    final Button cancelOverride = (Button) pwOverrideView.findViewById(R.id.cancelOverride);
                    final Button saveOverride = (Button) pwOverrideView.findViewById(R.id.saveOverride);

                    final SQLiteHelper sqLiteHelper = new SQLiteHelper(activity);
                    final ArrayList<String> tableNames = sqLiteHelper.getTableNames();

                    if (activity != null && isAdded() && !activity.isFinishing()) {

                        //display popup window with pwView as the layout
                        final PopupWindow pw = new PopupWindow (pwView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
                        pw.setTouchable(true);
                        pw.setFocusable(true);
                        pw.setOutsideTouchable(false);
                        pw.update();


                        pw.showAtLocation(mContainerView, Gravity.CENTER, 0, 0);
                        fileNameWrapper.setHint("Name");


                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                start.setVisibility(View.GONE);
                                editWorkout.setVisibility(View.GONE);
                                mAddButton.setVisibility(View.VISIBLE);
                                addPyramid.setVisibility(View.VISIBLE);
                                addSuperset.setVisibility(View.VISIBLE);
                                addRest.setVisibility(View.VISIBLE);
                                saveChanges.setVisibility(View.VISIBLE);
                                pw.dismiss();
                            }
                        });

                        save.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                fileName = (EditText) pwView.findViewById(R.id.enterFileName);
                                closePw = true;

                                //if fileName does not have any input make a toast to show an error
                                if (fileName.getText().toString().length() < 1) {
                                    fileName.requestFocus();
                                    closePw = false;
                                    String enterFileName = "Enter File Name";
                                    Toast toast = makeText(activity, enterFileName, Toast.LENGTH_SHORT);
                                    toast.show();
                                }

                                //if fileName is larger than 28 characters, make a toast to show an error
                                else if(fileName.getText().toString().length() > 28){
                                    fileName.requestFocus();
                                    closePw = false;
                                    String tooLong = "File Name Too Long";
                                    Toast toast = makeText(activity, tooLong, Toast.LENGTH_SHORT);
                                    toast.show();
                                }

                                //if fileName equals 1,2,3,4,5,6,7, or 8
                                else if(fileName.getText().toString().equals("1") || fileName.getText().toString().equals("2") ||
                                        fileName.getText().toString().equals("3") || fileName.getText().toString().equals("4") ||
                                        fileName.getText().toString().equals("5") || fileName.getText().toString().equals("6") ||
                                        fileName.getText().toString().equals("7") || fileName.getText().toString().equals("8")){

                                    fileName.requestFocus();
                                    closePw = false;
                                    String invalidName = "Invalid name";
                                    Toast toast = makeText(activity, invalidName, Toast.LENGTH_SHORT);
                                    toast.show();
                                }

                                //iterates through tableNames to find one that matches user input
                                //if user already has a workout named the same, display new popup window asking if they are sure they want to override
                                for(final String a: tableNames)
                                    if(a.equals(fileName.getText().toString())) {
                                        pw.dismiss();
                                        final PopupWindow pwOverride = new PopupWindow(pwOverrideView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
                                        pwOverride.setTouchable(true);
                                        pwOverride.setFocusable(true);
                                        pwOverride.setOutsideTouchable(false);
                                        pwOverride.update();
                                        pwOverride.showAtLocation(mContainerView, Gravity.CENTER, 0, 0);
                                        layout_MainMenu.getForeground().setAlpha(220);

                                        closePw = false;

                                        pwOverride.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                            @Override
                                            public void onDismiss() {
                                                layout_MainMenu.getForeground().setAlpha(0);
                                            }
                                        });

                                        cancelOverride.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                pwOverride.dismiss();
                                                pw.setTouchable(true);
                                                pw.setFocusable(true);
                                                pw.setOutsideTouchable(false);
                                                pw.update();
                                                pw.showAtLocation(mContainerView, Gravity.CENTER, 0, 0);
                                                layout_MainMenu.getForeground().setAlpha(220);
                                            }
                                        });

                                        saveOverride.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                //deletes the old table and old time table corresponding to the file name
                                                sqLiteHelper.deleteTable(a + "TIIIIIME");
                                                pwOverride.dismiss();
                                                activity.setTitle(fileName.getText().toString());
                                                String tableName = activity.getTitle().toString();
                                                myRef.child("workouts").child(tableName).removeValue();
                                                titleSet = true;


                                                addRest.setVisibility(View.GONE);
                                                mAddButton.setVisibility(View.GONE);
                                                addSuperset.setVisibility(View.GONE);
                                                addPyramid.setVisibility(View.GONE);
                                                saveChanges.setVisibility(View.GONE);
                                                start.setVisibility(View.VISIBLE);
                                                editWorkout.setVisibility(View.VISIBLE);


                                                //saves a new table and time table to the fileName
                                                //sqLiteHelper.createUserTable(fileName.getText().toString());
                                                sqLiteHelper.createTimeTable(fileName.getText().toString());


                                                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                                                View exerciseRow = layoutInflater.inflate(R.layout.row, null);
                                                View restRow = layoutInflater.inflate(R.layout.rest_row, null);
                                                View superset = layoutInflater.inflate(R.layout.superset, null);
                                                View pyramidRow = layoutInflater.inflate(R.layout.pyramidset, null);
                                                int numberOfViews = mContainerView.getChildCount() - 10;
                                                for (int a = 0; a <= numberOfViews; a++) {
                                                    if (mContainerView.getChildAt(a).getId() == exerciseRow.getId()) {
                                                        View v = mContainerView.getChildAt(a);
                                                        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);
                                                        EditText sets = (EditText) v.findViewById(R.id.sets);
                                                        EditText reps = (EditText) v.findViewById(R.id.reps);
                                                        EditText weight = (EditText) v.findViewById(R.id.weight);

                                                        String exerciseString = autoCompleteTextView.getText().toString();
                                                        String weightString = weight.getText().toString();
                                                        String setString = sets.getText().toString();
                                                        String repsString = reps.getText().toString();
                                                        insertDataToTable(tableName, exerciseView, null, exerciseString, weightString, setString, repsString, null, null, "0");


                                                    } else if (mContainerView.getChildAt(a).getId() == restRow.getId()) {
                                                        View v = mContainerView.getChildAt(a);
                                                        EditText minutes = (EditText) v.findViewById(minutesTV);
                                                        EditText seconds = (EditText) v.findViewById(R.id.secondsTV);
                                                        String minutesString = minutes.getText().toString();
                                                        String secondsString = seconds.getText().toString();

                                                        insertDataToTable(tableName, restView, null, null, null, null, null, minutesString, secondsString, "0");

                                                    }
                                                    else if (mContainerView.getChildAt(a).getId() == superset.getId()){
                                                        View supersetContainer = mContainerView.getChildAt(a);
                                                        LinearLayout layout = (LinearLayout) supersetContainer.findViewById(R.id.supersetLayout);

                                                        String childCountString = String.format("%s", layout.getChildCount());
                                                        insertDataToTable(tableName, supersetView, null, null, null, null, null, null, null, childCountString);
                                                        for(int i = 0; i < layout.getChildCount(); i++){
                                                            if(layout.getChildAt(i).getId() == exerciseRow.getId()){
                                                                View v = layout.getChildAt(i);
                                                                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);
                                                                EditText sets = (EditText) v.findViewById(R.id.sets);
                                                                EditText reps = (EditText) v.findViewById(R.id.reps);
                                                                EditText weight = (EditText) v.findViewById(R.id.weight);
                                                                String exerciseString = autoCompleteTextView.getText().toString();
                                                                String weightString = weight.getText().toString();
                                                                String setString = sets.getText().toString();
                                                                String repsString = reps.getText().toString();

                                                                insertDataToTable(tableName, exerciseSuperset, null, exerciseString, weightString, setString, repsString, null, null, childCountString);

                                                            }

                                                            else if(layout.getChildAt(i).getId() == restRow.getId()){
                                                                View v = layout.getChildAt(i);
                                                                EditText minutes = (EditText) v.findViewById(minutesTV);
                                                                EditText seconds = (EditText) v.findViewById(R.id.secondsTV);
                                                                String minutesString = minutes.getText().toString();
                                                                String secondsString = seconds.getText().toString();

                                                                insertDataToTable(tableName, restSuperset, null, null, null, null, null, minutesString, secondsString, childCountString);

                                                            }
                                                        }

                                                    }

                                                    //if child at a = pyramid set row, iterate through children
                                                    else if (mContainerView.getChildAt(a).getId() == pyramidRow.getId()){
                                                        View pyramidContainer = mContainerView.getChildAt(a);
                                                        LinearLayout layout = (LinearLayout) pyramidContainer.findViewById(R.id.pyramidContainer);

                                                        //sets the view = pyramidSetView and assigns the child count of pyramid row to sets
                                                        String childCountString = String.format("%s", layout.getChildCount());
                                                        insertDataToTable(tableName, pyramidSetView, null, null, null, null, null, null, null, childCountString);
                                                        for(int i = 0; i < layout.getChildCount(); i++){
                                                            if(layout.getChildAt(i).getId() == exerciseRow.getId()){
                                                                View v = layout.getChildAt(i);
                                                                EditText sets = (EditText) v.findViewById(R.id.sets);
                                                                EditText reps = (EditText) v.findViewById(R.id.reps);
                                                                EditText weight = (EditText) v.findViewById(R.id.weight);
                                                                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);
                                                                String exerciseString = autoCompleteTextView.getText().toString();
                                                                String weightString = weight.getText().toString();
                                                                String setString = sets.getText().toString();
                                                                String repsString = reps.getText().toString();

                                                                insertDataToTable(tableName, exercisePyramidSet, null, exerciseString, weightString, setString, repsString, null, null, childCountString);
                                                            }

                                                            else if(layout.getChildAt(i).getId() == restRow.getId()){
                                                                View v = layout.getChildAt(i);
                                                                EditText minutes = (EditText) v.findViewById(minutesTV);
                                                                EditText seconds = (EditText) v.findViewById(R.id.secondsTV);
                                                                String minutesString = minutes.getText().toString();
                                                                String secondsString = seconds.getText().toString();

                                                                insertDataToTable(tableName, restPyramidSet, null, null, null, null, null, minutesString, secondsString, childCountString);
                                                            }
                                                        }

                                                    }
                                                }
                                                pw.dismiss();
                                                disableText();

                                            }
                                        });
                                    }

                                //if there are no errors and no file already has the same name
                                if (closePw) {
                                    activity.setTitle(fileName.getText().toString());
                                    String tableName = activity.getTitle().toString();
                                    titleSet = true;


                                    addRest.setVisibility(View.GONE);
                                    mAddButton.setVisibility(View.GONE);
                                    addSuperset.setVisibility(View.GONE);
                                    addPyramid.setVisibility(View.GONE);
                                    saveChanges.setVisibility(View.GONE);
                                    start.setVisibility(View.VISIBLE);
                                    editWorkout.setVisibility(View.VISIBLE);


                                    LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                                    View exerciseRow = layoutInflater.inflate(R.layout.row, null);
                                    View restRow = layoutInflater.inflate(R.layout.rest_row, null);
                                    View superset = layoutInflater.inflate(R.layout.superset, null);
                                    View pyramidRow = layoutInflater.inflate(R.layout.pyramidset, null);
                                    int numberOfViews = mContainerView.getChildCount() - 10;
                                    for (int a = 0; a <= numberOfViews; a++) {
                                        if (mContainerView.getChildAt(a).getId() == exerciseRow.getId()) {
                                            View v = mContainerView.getChildAt(a);
                                            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);
                                            EditText sets = (EditText) v.findViewById(R.id.sets);
                                            EditText reps = (EditText) v.findViewById(R.id.reps);
                                            EditText weight = (EditText) v.findViewById(R.id.weight);

                                            String exerciseString = autoCompleteTextView.getText().toString();
                                            String weightString = weight.getText().toString();
                                            String setString = sets.getText().toString();
                                            String repsString = reps.getText().toString();
                                            insertDataToTable(tableName, exerciseView, null, exerciseString, weightString, setString, repsString, null, null, "0");


                                        } else if (mContainerView.getChildAt(a).getId() == restRow.getId()) {
                                            View v = mContainerView.getChildAt(a);
                                            EditText minutes = (EditText) v.findViewById(minutesTV);
                                            EditText seconds = (EditText) v.findViewById(R.id.secondsTV);
                                            String minutesString = minutes.getText().toString();
                                            String secondsString = seconds.getText().toString();

                                            insertDataToTable(tableName, restView, null, null, null, null, null, minutesString, secondsString, "0");

                                        }
                                        else if (mContainerView.getChildAt(a).getId() == superset.getId()){
                                            View supersetContainer = mContainerView.getChildAt(a);
                                            LinearLayout layout = (LinearLayout) supersetContainer.findViewById(R.id.supersetLayout);

                                            String childCountString = String.format("%s", layout.getChildCount());
                                            insertDataToTable(tableName, supersetView, null, null, null, null, null, null, null, childCountString);
                                            for(int i = 0; i < layout.getChildCount(); i++){
                                                if(layout.getChildAt(i).getId() == exerciseRow.getId()){
                                                    View v = layout.getChildAt(i);
                                                    AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);
                                                    EditText sets = (EditText) v.findViewById(R.id.sets);
                                                    EditText reps = (EditText) v.findViewById(R.id.reps);
                                                    EditText weight = (EditText) v.findViewById(R.id.weight);
                                                    String exerciseString = autoCompleteTextView.getText().toString();
                                                    String weightString = weight.getText().toString();
                                                    String setString = sets.getText().toString();
                                                    String repsString = reps.getText().toString();

                                                    insertDataToTable(tableName, exerciseSuperset, null, exerciseString, weightString, setString, repsString, null, null, childCountString);

                                                }

                                                else if(layout.getChildAt(i).getId() == restRow.getId()){
                                                    View v = layout.getChildAt(i);
                                                    EditText minutes = (EditText) v.findViewById(minutesTV);
                                                    EditText seconds = (EditText) v.findViewById(R.id.secondsTV);
                                                    String minutesString = minutes.getText().toString();
                                                    String secondsString = seconds.getText().toString();

                                                    insertDataToTable(tableName, restSuperset, null, null, null, null, null, minutesString, secondsString, childCountString);

                                                }
                                            }

                                        }

                                        //if child at a = pyramid set row, iterate through children
                                        else if (mContainerView.getChildAt(a).getId() == pyramidRow.getId()){
                                            View pyramidContainer = mContainerView.getChildAt(a);
                                            LinearLayout layout = (LinearLayout) pyramidContainer.findViewById(R.id.pyramidContainer);

                                            //sets the view = pyramidSetView and assigns the child count of pyramid row to sets
                                            String childCountString = String.format("%s", layout.getChildCount());
                                            insertDataToTable(tableName, pyramidSetView, null, null, null, null, null, null, null, childCountString);
                                            for(int i = 0; i < layout.getChildCount(); i++){
                                                if(layout.getChildAt(i).getId() == exerciseRow.getId()){
                                                    View v = layout.getChildAt(i);
                                                    EditText sets = (EditText) v.findViewById(R.id.sets);
                                                    EditText reps = (EditText) v.findViewById(R.id.reps);
                                                    EditText weight = (EditText) v.findViewById(R.id.weight);
                                                    AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);
                                                    String exerciseString = autoCompleteTextView.getText().toString();
                                                    String weightString = weight.getText().toString();
                                                    String setString = sets.getText().toString();
                                                    String repsString = reps.getText().toString();


                                                    insertDataToTable(tableName, exercisePyramidSet, null, exerciseString, weightString, setString, repsString, null, null, childCountString);

                                                }

                                                else if(layout.getChildAt(i).getId() == restRow.getId()){
                                                    View v = layout.getChildAt(i);
                                                    EditText minutes = (EditText) v.findViewById(minutesTV);
                                                    EditText seconds = (EditText) v.findViewById(R.id.secondsTV);
                                                    String minutesString = minutes.getText().toString();
                                                    String secondsString = seconds.getText().toString();

                                                    insertDataToTable(tableName, restPyramidSet, null, null, null, null, null, minutesString, secondsString, childCountString);

                                                }
                                            }

                                        }
                                    }
                                    pw.dismiss();
                                    disableText();
                                }
                            }
                        });

                        pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                layout_MainMenu.getForeground().setAlpha(0);
                            }
                        });


                    }
                }
            }
        });





        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences pref = activity.getSharedPreferences("time", Context.MODE_PRIVATE);
                pref.edit().putLong("timeElapsed", SystemClock.elapsedRealtime()).apply();

                mAddButton.setVisibility(View.GONE);
                addSuperset.setVisibility(View.GONE);
                addPyramid.setVisibility(View.GONE);
                start.setVisibility(View.GONE);
                addRest.setVisibility(View.GONE);
                editWorkout.setVisibility(View.GONE);
                next.setVisibility(View.VISIBLE);
                stop.setVisibility(View.VISIBLE);

                LayoutInflater layoutInflater = activity.getLayoutInflater();
                View exerciseView = layoutInflater.inflate(R.layout.row, null);
                View supersetView = layoutInflater.inflate(R.layout.superset, null);
                View pyramidView = layoutInflater.inflate(R.layout.pyramidset, null);
                ArrayList<maxModel> maxModelArrayList = new ArrayList<maxModel>();
                SQLiteHelper sqLiteHelper = new SQLiteHelper(activity);
                String tableName = "maxxxxxxxxxx";

                //if 1rm for lifts are entered
                if(sqLiteHelper.tableExists(tableName)) {

                    //gets the 1rm and assigns it to an arraylist
                    maxModelArrayList = sqLiteHelper.getMaxes(tableName);
                    maxModel maxModel = new maxModel();

                    //converts the 1rm arraylist to maxModel so that maxes can be retrieved
                    maxModel = maxModelArrayList.get(0);
                    for (int i = 0; i < mContainerView.getChildCount() - 10; i++) {
                        View v = mContainerView.getChildAt(i);

                        //if a child of mContainerView is an exercise view
                        if (v.getId() == exerciseView.getId()) {
                            //converts the percent to a number based off of 1rm
                            convert1RMPercent(v, maxModel);
                        }

                        //if child of mContainerView is a superset view, iterate through children to see if exercise view exists
                        //if exists, convert percentage if there is one to number based off of 1rm
                        else if(v.getId() == supersetView.getId()){
                            LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.supersetLayout);
                            for(int j = 0; j < linearLayout.getChildCount() - 1; j++){
                                View currentIndex = linearLayout.getChildAt(j);
                                if(currentIndex.getId() == exerciseView.getId()){
                                    convert1RMPercent(currentIndex, maxModel);
                                }
                            }
                        }

                        //if child of mContainerView is a pyramid view, iterate through children to see if exercise view exists
                        //if exists, convert percentage if there is one to number based off of 1rm
                        else if(v.getId() == pyramidView.getId()){
                            LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.pyramidContainer);
                            AutoCompleteTextView exercise = null;
                            for(int j = 0; j < linearLayout.getChildCount() - 1; j++){
                                View currentIndex = linearLayout.getChildAt(j);
                                if(currentIndex.getId() == exerciseView.getId()){

                                    //uses the exercise that is used for the pyramid set
                                    if(j == 0){
                                        exercise = (AutoCompleteTextView) currentIndex.findViewById(R.id.autoCompleteTextView);
                                    }

                                    //converts all the percents based off of the exercise found
                                    convert1RMPercentPyramidView(currentIndex, maxModel, exercise);
                                }
                            }
                        }


                    }
                }
                startClick = true;
            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(isMyServiceRunning(CountdownService.class)){
                    activity.unregisterReceiver(secondReceiver);
                    //activity.unregisterReceiver(minuteReceiver);
                    activity.stopService(serviceIntent);
                    startTimer = false;
                }

                //gets the elapsed time from when start button is clicked
                SharedPreferences pref = activity.getSharedPreferences("time", Context.MODE_PRIVATE);
                long startTime = pref.getLong("timeElapsed", -1);
                long endTime = SystemClock.elapsedRealtime();
                long elapsedMilliSeconds = endTime - startTime;
                long seconds = elapsedMilliSeconds / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                startClick = false;

                String pauseTable = "onpauseeeeeeeeeetable";
                SQLiteHelper sqLiteHelper = new SQLiteHelper(activity);
                boolean tableExists = sqLiteHelper.tableExists(pauseTable);
                if(tableExists){
                    sqLiteHelper.deleteTable(pauseTable);
                }


                next.setVisibility(View.GONE);
                stop.setVisibility(View.GONE);
                resume.setVisibility(View.GONE);
                editWorkout.setVisibility(View.VISIBLE);
                start.setVisibility(View.VISIBLE);


                final String TIME = String.format("%s", hours % 24 + ":" + minutes % 60 + ":" + seconds % 60);
                //if current layout is a loaded workout
                if(titleSet) {
                    final String workoutName = activity.getTitle().toString();

                    myRef.child("workouts").child(workoutName + "TimeTable")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()) {

                                        ExerciseModel exerciseModel = dataSnapshot.getValue(ExerciseModel.class);
                                        timeElapsed = exerciseModel.getTime();

                                        //gets the time completed for the workout and compares it to the most recent time
                                        //if the most recent is faster, replace the old time with the new
                                        //if not, use the old time
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeElapsed);
                                        SimpleDateFormat sdf = new SimpleDateFormat(TIME);
                                        int storedTime = Integer.parseInt(simpleDateFormat.format(new Date()).replace(":", ""));
                                        int recentTime = Integer.parseInt(sdf.format(new Date()).replace(":", ""));

                                        if(recentTime < storedTime){
                                            insertDataToTable(workoutName, null, TIME, null, null, null, null, null, null, null);
                                        }
                                    }
                                    else{
                                        Log.d("asdjkl", "Asdjkl");
                                        insertDataToTable(workoutName, null, TIME, null, null, null, null, null, null, null);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });


                    //reloads the workout
                    activity.loadWorkout(activity.getTitle().toString());
                }
                notification(TIME);


            }
        });



        //same as swipe
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextSet();
            }
        });


        editWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start.setVisibility(View.GONE);
                next.setVisibility(View.GONE);
                editWorkout.setVisibility(View.GONE);
                mAddButton.setVisibility(View.VISIBLE);
                addSuperset.setVisibility(View.VISIBLE);
                addPyramid.setVisibility(View.VISIBLE);
                addRest.setVisibility(View.VISIBLE);
                saveChanges.setVisibility(View.VISIBLE);

                LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
                View exerciseRow = layoutInflater.inflate(R.layout.row, null);
                View restRow = layoutInflater.inflate(R.layout.rest_row, null);
                View supersetRow = layoutInflater.inflate(R.layout.superset, null);
                View pyramidRow = layoutInflater.inflate(R.layout.pyramidset, null);
                int numberOfViews = mContainerView.getChildCount()-10;

                //re enables the text views
                for(int a = 0; a < numberOfViews; a++){
                    if(mContainerView.getChildAt(a).getId() == exerciseRow.getId()){
                        View v = mContainerView.getChildAt(a);
                        ImageButton deleteEx = (ImageButton) v.findViewById(R.id.deleteEx);
                        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);
                        EditText sets = (EditText) v.findViewById(R.id.sets);
                        EditText reps = (EditText) v.findViewById(R.id.reps);
                        EditText weight = (EditText) v.findViewById(R.id.weight);

                        TextInputLayout exTextInputLayout = (TextInputLayout) v.findViewById(R.id.exerciseTextInputLayout);
                        TextInputLayout weightTextInputLayout = (TextInputLayout) v.findViewById(R.id.weightTextInputLayout);
                        TextInputLayout repTextInputLayout = (TextInputLayout) v.findViewById(R.id.repsTextInputLayout);
                        TextInputLayout setTextInputLayout = (TextInputLayout) v.findViewById(R.id.setsTextInputLayout);

                        ImageView reorder = (ImageView) v.findViewById(R.id.reorder);
                        reorder.setVisibility(View.VISIBLE);
                        deleteEx.setVisibility(View.VISIBLE);

                        weight.setBackground(weightDrawable);
                        autoCompleteTextView.setBackground(exDrawable);
                        reps.setBackground(repDrawable);
                        sets.setBackground(setDrawable);

                        ((MainActivity) getActivity()).enableEditText(exTextInputLayout);
                        ((MainActivity) getActivity()).enableEditText(weightTextInputLayout);
                        ((MainActivity) getActivity()).enableEditText(repTextInputLayout);
                        ((MainActivity) getActivity()).enableEditText(setTextInputLayout);
                        //weight.setHint(R.string.weight);
                    }
                    else if(mContainerView.getChildAt(a).getId() == restRow.getId()){
                        View v = mContainerView.getChildAt(a);
                        ImageButton deleteRest = (ImageButton) v.findViewById(R.id.deleteRest);
                        deleteRest.setVisibility(View.VISIBLE);
                        ImageView reorder = (ImageView) v.findViewById(R.id.reorder);
                        reorder.setVisibility(View.VISIBLE);
                        EditText minutes = (EditText) v.findViewById(minutesTV);
                        EditText seconds = (EditText) v.findViewById(R.id.secondsTV);

                        TextInputLayout minTextInputLayout = (TextInputLayout) v.findViewById(R.id.minTextInputLayout);
                        TextInputLayout secTextInputLayout = (TextInputLayout) v.findViewById(R.id.secTextInputLayout);

                        minutes.setBackground(minDrawable);
                        seconds.setBackground(secDrawable);

                        ((MainActivity) getActivity()).enableEditText(minTextInputLayout);
                        ((MainActivity) getActivity()).enableEditText(secTextInputLayout);
                    }
                    else if (mContainerView.getChildAt(a).getId() == supersetRow.getId()){
                        View v = mContainerView.getChildAt(a);
                        LinearLayout layout = (LinearLayout) v.findViewById(R.id.supersetLayout);
                        for(int i = 0; i < layout.getChildCount(); i++){
                            if(layout.getChildAt(i).getId() == exerciseRow.getId()){
                                View z = layout.getChildAt(i);
                                if(i == 0){
                                    ImageView reorder = (ImageView) z.findViewById(R.id.reorder);
                                    reorder.setVisibility(View.VISIBLE);
                                }
                                ImageButton deleteEx = (ImageButton) z.findViewById(R.id.deleteEx);
                                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) z.findViewById(R.id.autoCompleteTextView);
                                EditText sets = (EditText) z.findViewById(R.id.sets);
                                EditText reps = (EditText) z.findViewById(R.id.reps);
                                EditText weight = (EditText) z.findViewById(R.id.weight);

                                TextInputLayout exTextInputLayout = (TextInputLayout) z.findViewById(R.id.exerciseTextInputLayout);
                                TextInputLayout weightTextInputLayout = (TextInputLayout) z.findViewById(R.id.weightTextInputLayout);
                                TextInputLayout repTextInputLayout = (TextInputLayout) z.findViewById(R.id.repsTextInputLayout);
                                TextInputLayout setTextInputLayout = (TextInputLayout) z.findViewById(R.id.setsTextInputLayout);

                                autoCompleteTextView.setBackground(exDrawable);
                                sets.setBackground(setDrawable);
                                reps.setBackground(repDrawable);
                                weight.setBackground(weightDrawable);

                                deleteEx.setVisibility(View.VISIBLE);
                                ((MainActivity) getActivity()).enableEditText(exTextInputLayout);
                                ((MainActivity) getActivity()).enableEditText(weightTextInputLayout);
                                ((MainActivity) getActivity()).enableEditText(repTextInputLayout);
                                ((MainActivity) getActivity()).enableEditText(setTextInputLayout);
                                //weight.setHint(R.string.weight);
                            }
                            else if(layout.getChildAt(i).getId() == restRow.getId()){
                                View z = layout.getChildAt(i);
                                ImageButton deleteRest = (ImageButton) z.findViewById(R.id.deleteRest);
                                deleteRest.setVisibility(View.VISIBLE);
                                EditText minutes = (EditText) z.findViewById(minutesTV);
                                EditText seconds = (EditText) z.findViewById(R.id.secondsTV);

                                TextInputLayout minTextInputLayout = (TextInputLayout) z.findViewById(R.id.minTextInputLayout);
                                TextInputLayout secTextInputLayout = (TextInputLayout) z.findViewById(R.id.secTextInputLayout);

                                minutes.setBackground(minDrawable);
                                seconds.setBackground(secDrawable);

                                ((MainActivity) getActivity()).enableEditText(minTextInputLayout);
                                ((MainActivity) getActivity()).enableEditText(secTextInputLayout);
                            }
                        }
                    }
                    else if (mContainerView.getChildAt(a).getId() == pyramidRow.getId()){
                        View v = mContainerView.getChildAt(a);
                        LinearLayout layout = (LinearLayout) v.findViewById(R.id.pyramidContainer);
                        for(int i = 0; i < layout.getChildCount(); i++){
                            if(layout.getChildAt(i).getId() == exerciseRow.getId()){
                                View z = layout.getChildAt(i);
                                if(i == 0){
                                    ImageView reorder = (ImageView) z.findViewById(R.id.reorder);
                                    reorder.setVisibility(View.VISIBLE);
                                    AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) z.findViewById(R.id.autoCompleteTextView);
                                    TextInputLayout exTextInputLayout = (TextInputLayout) z.findViewById(R.id.exerciseTextInputLayout);
                                    autoCompleteTextView.setBackground(exDrawable);
                                    ((MainActivity) getActivity()).enableEditText(exTextInputLayout);
                                }
                                ImageButton deleteEx = (ImageButton) z.findViewById(R.id.deleteEx);
                                EditText reps = (EditText) z.findViewById(R.id.reps);
                                EditText weight = (EditText) z.findViewById(R.id.weight);
                                TextInputLayout weightTextInputLayout = (TextInputLayout) z.findViewById(R.id.weightTextInputLayout);
                                TextInputLayout repTextInputLayout = (TextInputLayout) z.findViewById(R.id.repsTextInputLayout);
                                deleteEx.setVisibility(View.VISIBLE);

                                reps.setBackground(repDrawable);
                                weight.setBackground(weightDrawable);

                                ((MainActivity) getActivity()).enableEditText(repTextInputLayout);
                                ((MainActivity) getActivity()).enableEditText(weightTextInputLayout);
                                //weight.setHint(R.string.weight);
                            }
                            else if(layout.getChildAt(i).getId() == restRow.getId()){
                                View z = layout.getChildAt(i);
                                ImageButton deleteRest = (ImageButton) z.findViewById(R.id.deleteRest);
                                deleteRest.setVisibility(View.VISIBLE);
                                EditText minutes = (EditText) z.findViewById(minutesTV);
                                EditText seconds = (EditText) z.findViewById(R.id.secondsTV);

                                TextInputLayout minTextInputLayout = (TextInputLayout) z.findViewById(R.id.minTextInputLayout);
                                TextInputLayout secTextInputLayout = (TextInputLayout) z.findViewById(R.id.secTextInputLayout);

                                minutes.setBackground(minDrawable);
                                seconds.setBackground(secDrawable);

                                ((MainActivity) getActivity()).enableEditText(minTextInputLayout);
                                ((MainActivity) getActivity()).enableEditText(secTextInputLayout);
                            }
                        }
                    }
                }
            }
        });


        if(!loadWorkout) {
            start.setVisibility(View.GONE);
            editWorkout.setVisibility(View.GONE);
        }
        stop.setVisibility(View.GONE);
        next.setVisibility(View.GONE);
        resume.setVisibility(View.GONE);

    }


    //Method is called when workout is loaded
    public void booleanLoadWorkout(){
        loadWorkout = true;
    }

    //Method for disabling text of EditText
    public void disableText(){
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View exerciseRow = layoutInflater.inflate(R.layout.row, null);
        View restRow = layoutInflater.inflate(R.layout.rest_row, null);
        View supersetRow = layoutInflater.inflate(R.layout.superset, null);
        View pyramidRow = layoutInflater.inflate(R.layout.pyramidset, null);
        int numberOfViews = mContainerView.getChildCount()-10;
        for(int a = 0; a <= numberOfViews; a++){
            if(mContainerView.getChildAt(a).getId() == exerciseRow.getId()){
                View v = mContainerView.getChildAt(a);
                ImageButton deleteEx = (ImageButton) v.findViewById(R.id.deleteEx);
                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);
                EditText sets = (EditText) v.findViewById(R.id.sets);
                EditText reps = (EditText) v.findViewById(R.id.reps);
                EditText weight = (EditText) v.findViewById(R.id.weight);


                TextInputLayout weightTextInputLayout = (TextInputLayout) v.findViewById(R.id.weightTextInputLayout);

                ImageView reorder = (ImageView) v.findViewById(R.id.reorder);
                reorder.setVisibility(View.INVISIBLE);
                deleteEx.setVisibility(View.INVISIBLE);

                ((MainActivity) getActivity()).disableEditText(autoCompleteTextView);
                ((MainActivity) getActivity()).disableEditText(sets);
                ((MainActivity) getActivity()).disableEditText(reps);
                ((MainActivity) getActivity()).disableEditText(weight);

                if(weight.getText().length() > 0){
                    weightTextInputLayout.setHint("Weight");
                }
                else{
                    weightTextInputLayout.setHint("");
                }
            }
            else if(mContainerView.getChildAt(a).getId() == restRow.getId()){
                View v = mContainerView.getChildAt(a);
                ImageButton deleteRest = (ImageButton) v.findViewById(R.id.deleteRest);
                ImageView reorder = (ImageView) v.findViewById(R.id.reorder);
                reorder.setVisibility(View.INVISIBLE);
                deleteRest.setVisibility(View.INVISIBLE);
                EditText minutes = (EditText) v.findViewById(minutesTV);
                EditText seconds = (EditText) v.findViewById(R.id.secondsTV);

                TextInputLayout minTextInputLayout = (TextInputLayout) v.findViewById(R.id.minTextInputLayout);
                TextInputLayout secTextInputLayout = (TextInputLayout) v.findViewById(R.id.secTextInputLayout);

                ((MainActivity) getActivity()).disableEditText(minutes);
                ((MainActivity) getActivity()).disableEditText(seconds);
            }
            else if(mContainerView.getChildAt(a).getId() == supersetRow.getId()){
                View v = mContainerView.getChildAt(a);
                LinearLayout layout = (LinearLayout) v.findViewById(R.id.supersetLayout);
                for(int i = 0; i < layout.getChildCount(); i++){
                    if(layout.getChildAt(i).getId() == exerciseRow.getId()){
                        View z = layout.getChildAt(i);
                        if(i == 0){
                            ImageView reorder = (ImageView) z.findViewById(R.id.reorder);
                            reorder.setVisibility(View.INVISIBLE);
                        }
                        ImageButton deleteEx = (ImageButton) z.findViewById(R.id.deleteEx);
                        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) z.findViewById(R.id.autoCompleteTextView);
                        EditText sets = (EditText) z.findViewById(R.id.sets);
                        EditText reps = (EditText) z.findViewById(R.id.reps);
                        EditText weight = (EditText) z.findViewById(R.id.weight);

                        TextInputLayout exTextInputLayout = (TextInputLayout) z.findViewById(R.id.exerciseTextInputLayout);
                        TextInputLayout weightTextInputLayout = (TextInputLayout) z.findViewById(R.id.weightTextInputLayout);
                        TextInputLayout repTextInputLayout = (TextInputLayout) z.findViewById(R.id.repsTextInputLayout);
                        TextInputLayout setTextInputLayout = (TextInputLayout) z.findViewById(R.id.setsTextInputLayout);
                        deleteEx.setVisibility(View.INVISIBLE);

                        ((MainActivity) getActivity()).disableEditText(autoCompleteTextView);
                        ((MainActivity) getActivity()).disableEditText(sets);
                        ((MainActivity) getActivity()).disableEditText(reps);
                        ((MainActivity) getActivity()).disableEditText(weight);
                        if(weight.getText().length() > 0){
                            weightTextInputLayout.setHint("Weight");
                        }
                        else{
                            weightTextInputLayout.setHint("");
                        }
                    }
                    else if(layout.getChildAt(i).getId() == restRow.getId()){
                        View f = layout.getChildAt(i);
                        ImageButton deleteRest = (ImageButton) f.findViewById(R.id.deleteRest);
                        deleteRest.setVisibility(View.INVISIBLE);
                        EditText minutes = (EditText) f.findViewById(R.id.minutesTV);
                        EditText seconds = (EditText) f.findViewById(R.id.secondsTV);


                        ((MainActivity) getActivity()).disableEditText(minutes);
                        ((MainActivity) getActivity()).disableEditText(seconds);
                    }
                }
            }

            else if(mContainerView.getChildAt(a).getId() == pyramidRow.getId()){
                View v = mContainerView.getChildAt(a);
                LinearLayout layout = (LinearLayout) v.findViewById(R.id.pyramidContainer);
                for(int i = 0; i < layout.getChildCount(); i++){
                    if(layout.getChildAt(i).getId() == exerciseRow.getId()){
                        View z = layout.getChildAt(i);
                        if(i == 0){
                            ImageView reorder = (ImageView) z.findViewById(R.id.reorder);
                            reorder.setVisibility(View.INVISIBLE);
                            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) z.findViewById(R.id.autoCompleteTextView);
                            TextInputLayout exTextInputLayout = (TextInputLayout) z.findViewById(R.id.exerciseTextInputLayout);
                            ((MainActivity) getActivity()).disableEditText(autoCompleteTextView);
                        }
                        ImageButton deleteEx = (ImageButton) z.findViewById(R.id.deleteEx);
                        EditText reps = (EditText) z.findViewById(R.id.reps);
                        EditText weight = (EditText) z.findViewById(R.id.weight);
                        TextInputLayout weightTextInputLayout = (TextInputLayout) z.findViewById(R.id.weightTextInputLayout);
                        deleteEx.setVisibility(View.INVISIBLE);
                        ((MainActivity) getActivity()).disableEditText(reps);
                        ((MainActivity) getActivity()).disableEditText(weight);
                        if(weight.getText().length() > 0){
                            weightTextInputLayout.setHint("Weight");
                        }
                        else{
                            weightTextInputLayout.setHint("");
                        }
                    }
                    else if(layout.getChildAt(i).getId() == restRow.getId()){
                        View f = layout.getChildAt(i);
                        ImageButton deleteRest = (ImageButton) f.findViewById(R.id.deleteRest);
                        deleteRest.setVisibility(View.INVISIBLE);
                        EditText minutes = (EditText) f.findViewById(R.id.minutesTV);
                        EditText seconds = (EditText) f.findViewById(R.id.secondsTV);


                        ((MainActivity) getActivity()).disableEditText(minutes);
                        ((MainActivity) getActivity()).disableEditText(seconds);
                    }
                }
            }
        }
    }

    //Method for adding exercise row to mContainerView
    public void inflateEditRow(String exercise, String weight, String sets, String reps){

            final String REORDER = "";
            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
            final View row = layoutInflater.inflate(R.layout.row, null);
            final ConstraintLayout rowView = (ConstraintLayout) row.findViewById(R.id.exercise_row);
            final AutoCompleteTextView autoCompleteTextView1 = (AutoCompleteTextView) rowView.findViewById(autoCompleteTextView);
            final EditText set = (EditText) rowView.findViewById(R.id.sets);
            final EditText rep = (EditText) rowView.findViewById(R.id.reps);
            final ImageView reorder = (ImageView) rowView.findViewById(R.id.reorder);
            final EditText weigh = (EditText) rowView.findViewById(R.id.weight);
            final ImageButton deleteEx = (ImageButton) rowView.findViewById(R.id.deleteEx);
            deleteEx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContainerView.removeView((View) view.getParent());
                }
            });


        weightDrawable = weigh.getBackground();
        exDrawable = autoCompleteTextView1.getBackground();
        repDrawable = rep.getBackground();
        setDrawable = set.getBackground();

        swipeLeft(autoCompleteTextView1);
        swipeLeft(weigh);
        swipeLeft(set);
        swipeLeft(rep);
        swipeLeft(row);


            autoCompleteTextView1.setAdapter(getExerciseAdapter(getActivity()));


            if (exercise != null && !exercise.isEmpty() && sets != null && !sets.isEmpty() && reps != null && !reps.isEmpty()) {
                autoCompleteTextView1.setText(exercise);
                set.setText(sets);
                rep.setText(reps);
                weigh.setText(weight);
            }

            reorder.setTag(REORDER);

        //drag listener for all the views on the screen
        myDragEventListener myDragEventListener = new myDragEventListener();
        reorder.setOnDragListener(myDragEventListener);
        rowView.setOnDragListener(myDragEventListener);
        mContainerView.setOnDragListener(myDragEventListener);

        reorder.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int eventTouchXCoord;
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    ConstraintLayout ex = (ConstraintLayout) view.getParent();
                    ClipData clipData = ClipData.newPlainText(reorder.getTag().toString(), reorder.getTag().toString());
                    eventTouchXCoord = (int) motionEvent.getX();
                    eventTouchYCoord = (int) motionEvent.getY();
                    EventDragShadowBuilder shadowBuilder = new EventDragShadowBuilder(ex, eventTouchXCoord, eventTouchYCoord);
                    view.startDrag(clipData, shadowBuilder, view, 0);
                    return true;
                }
                else{
                    return false;
                }
            }
        });
            //adds the view to the top of the buttons/bottom of the workout
        mContainerView.addView(rowView, mContainerView.getChildCount() - 10);
    }


    //method for creating a pyramid set
    //similar to superset but the exercise edittext is invisible for every exercise row after the first. The set is invisible too
    public void inflatePyramidRow(ArrayList<String> exercise, ArrayList<String> weight, ArrayList<String> reps, ArrayList<String> min, ArrayList<String> sec, int numberOfSets){

        final String REORDER = "";
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View pyramidSetContainer = layoutInflater.inflate(R.layout.pyramidset, null);
        final LinearLayout pyramidLayout = (LinearLayout) pyramidSetContainer.findViewById(R.id.pyramidContainer);

        mContainerView.addView(pyramidSetContainer, mContainerView.getChildCount() - 10);
        for(int i = 0; i < numberOfSets; i++) {
            final View rowView = layoutInflater.inflate(R.layout.row, null);
            final View restRow = layoutInflater.inflate(R.layout.rest_row, null);
            final AutoCompleteTextView autoCompleteTextView1 = (AutoCompleteTextView) rowView.findViewById(autoCompleteTextView);
            final EditText set = (EditText) rowView.findViewById(R.id.sets);
            final EditText rep = (EditText) rowView.findViewById(R.id.reps);
            final TextView x = (TextView) rowView.findViewById(R.id.X);
            final ImageView reorder = (ImageView) rowView.findViewById(R.id.reorder);
            final EditText weigh = (EditText) rowView.findViewById(R.id.weight);
            final ImageButton deleteEx = (ImageButton) rowView.findViewById(R.id.deleteEx);
            pyramidLayout.addView(rowView);
            if(i > 0 && i < numberOfSets){
                autoCompleteTextView1.setVisibility(View.GONE);
                reorder.setVisibility(View.INVISIBLE);
            }

            weightDrawable = weigh.getBackground();
            exDrawable = autoCompleteTextView1.getBackground();
            repDrawable = rep.getBackground();
            setDrawable = set.getBackground();

            swipeLeft(autoCompleteTextView1);
            swipeLeft(weigh);
            swipeLeft(set);
            swipeLeft(rep);
            swipeLeft(rowView);

            deleteEx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if((View) view.getParent() == pyramidLayout.getChildAt(0)){
                            pyramidLayout.removeView((View) view.getParent());
                            if (pyramidLayout.getChildCount() == 0) {
                                mContainerView.removeView(pyramidLayout);
                            }
                            else if(pyramidLayout.getChildCount() == 1 && pyramidLayout.getChildAt(0).getId() == R.id.rest_row){
                                mContainerView.removeView(pyramidLayout);
                            }
                            else if(pyramidLayout.getChildCount() == 2 && pyramidLayout.getChildAt(1).getId() == R.id.rest_row){
                                mContainerView.removeView(pyramidLayout);
                            }
                            else {
                                View first = pyramidLayout.getChildAt(0);
                                ImageView order = (ImageView) first.findViewById(R.id.reorder);
                                if (first.getId() == R.id.exercise_row) {
                                    AutoCompleteTextView ex = (AutoCompleteTextView) first.findViewById(autoCompleteTextView);
                                    ex.setVisibility(View.VISIBLE);
                                }
                                order.setVisibility(View.VISIBLE);
                            }
                    }
                    else {
                        pyramidLayout.removeView((View) view.getParent());
                    }
                }
            });
            if(i == numberOfSets - 1){
                pyramidLayout.addView(restRow);
                final EditText minutes = (EditText) restRow.findViewById(minutesTV);
                TextView rest = (TextView) restRow.findViewById(R.id.rest);
                final EditText seconds = (EditText) restRow.findViewById(R.id.secondsTV);

                minDrawable = minutes.getBackground();
                secDrawable = seconds.getBackground();

                swipeLeft(minutes);
                swipeLeft(rest);
                swipeLeft(restRow);
                swipeLeft(seconds);

                final ImageView order = (ImageView) restRow.findViewById(R.id.reorder);
                order.setVisibility(View.INVISIBLE);
                if (min != null && !min.isEmpty() && sec != null && !sec.isEmpty()) {
                    minutes.setText(min.get(0));
                    seconds.setText(sec.get(0));
                }
                final ImageButton deleteRest = (ImageButton) restRow.findViewById(R.id.deleteRest);
                deleteRest.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View view) {
                                                      pyramidLayout.removeView((View) view.getParent());
                                                      if (pyramidLayout.getChildCount() == 0) {
                                                          mContainerView.removeView(pyramidLayout);
                                                      }
                                                  }
                                              });
            }

            set.setVisibility(View.GONE);
            x.setVisibility(View.GONE);
            autoCompleteTextView1.setAdapter(getExerciseAdapter(getActivity()));

            if (exercise != null && !exercise.isEmpty() && reps != null && !reps.isEmpty()) {
                autoCompleteTextView1.setText(exercise.get(i));
                rep.setText(reps.get(i));
                weigh.setText(weight.get(i));
            }

            reorder.setTag(REORDER);

            //drag listener for all the views on the screen
            myDragEventListener myDragEventListener = new myDragEventListener();
            reorder.setOnDragListener(myDragEventListener);
            mContainerView.setOnDragListener(myDragEventListener);
            rowView.setOnDragListener(myDragEventListener);
            restRow.setOnDragListener(myDragEventListener);

            reorder.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int eventTouchXCoord;
                    int eventTouchYCoord;
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        ConstraintLayout ex = (ConstraintLayout) view.getParent();
                        ClipData clipData = ClipData.newPlainText(reorder.getTag().toString(), reorder.getTag().toString());
                        eventTouchXCoord = (int) motionEvent.getX();
                        eventTouchYCoord = (int) motionEvent.getY();
                        EventDragShadowBuilder shadowBuilder = new EventDragShadowBuilder(ex, eventTouchXCoord, eventTouchYCoord);
                        view.startDrag(clipData, shadowBuilder, view, 0);
                        return true;
                    }
                        return false;
                    }

            });

        }
    }

    //method for creating a pyramid set
    //similar to superset but the exercise edittext is invisible for every exercise row after the first. The set is invisible too
    public void inflatePyramidRowNoRest(ArrayList<String> exercise, ArrayList<String> weight, ArrayList<String> reps, int numberOfSets){

        final String REORDER = "";
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View pyramidSetContainer = layoutInflater.inflate(R.layout.pyramidset, null);
        final LinearLayout pyramidLayout = (LinearLayout) pyramidSetContainer.findViewById(R.id.pyramidContainer);

        mContainerView.addView(pyramidSetContainer, mContainerView.getChildCount() - 10);
        for(int i = 0; i < numberOfSets; i++) {
            final View rowView = layoutInflater.inflate(R.layout.row, null);
            final View restRow = layoutInflater.inflate(R.layout.rest_row, null);
            final AutoCompleteTextView autoCompleteTextView1 = (AutoCompleteTextView) rowView.findViewById(autoCompleteTextView);
            final EditText set = (EditText) rowView.findViewById(R.id.sets);
            final EditText rep = (EditText) rowView.findViewById(R.id.reps);
            final TextView x = (TextView) rowView.findViewById(R.id.X);
            final ImageView reorder = (ImageView) rowView.findViewById(R.id.reorder);
            final EditText weigh = (EditText) rowView.findViewById(R.id.weight);
            final ImageButton deleteEx = (ImageButton) rowView.findViewById(R.id.deleteEx);
            pyramidLayout.addView(rowView);
            if(i > 0 && i < numberOfSets){
                autoCompleteTextView1.setVisibility(View.GONE);
                reorder.setVisibility(View.INVISIBLE);
            }

            weightDrawable = weigh.getBackground();
            exDrawable = autoCompleteTextView1.getBackground();
            repDrawable = rep.getBackground();
            setDrawable = set.getBackground();

            swipeLeft(autoCompleteTextView1);
            swipeLeft(weigh);
            swipeLeft(set);
            swipeLeft(rep);
            swipeLeft(rowView);

            deleteEx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if((View) view.getParent() == pyramidLayout.getChildAt(0)){
                        pyramidLayout.removeView((View) view.getParent());
                        if(pyramidLayout.getChildCount() == 0){
                            mContainerView.removeView(pyramidLayout);
                        }
                        else if(pyramidLayout.getChildCount() == 1){
                            mContainerView.removeView(pyramidLayout);
                        }
                        else {
                            View first = pyramidLayout.getChildAt(0);
                            ImageView order = (ImageView) first.findViewById(R.id.reorder);
                            if (first.getId() == R.id.exercise_row) {
                                AutoCompleteTextView ex = (AutoCompleteTextView) first.findViewById(autoCompleteTextView);
                                ex.setVisibility(View.VISIBLE);
                            }
                            order.setVisibility(View.VISIBLE);
                        }
                    }
                    else {
                        pyramidLayout.removeView((View) view.getParent());
                    }
                }
            });

            set.setVisibility(View.GONE);
            x.setVisibility(View.GONE);
            autoCompleteTextView1.setAdapter(getExerciseAdapter(getActivity()));

            if (exercise != null && !exercise.isEmpty() && reps != null && !reps.isEmpty()) {
                autoCompleteTextView1.setText(exercise.get(i));
                rep.setText(reps.get(i));
                weigh.setText(weight.get(i));
            }

            reorder.setTag(REORDER);

            //drag listener for all the views on the screen
            myDragEventListener myDragEventListener = new myDragEventListener();
            reorder.setOnDragListener(myDragEventListener);
            mContainerView.setOnDragListener(myDragEventListener);
            rowView.setOnDragListener(myDragEventListener);
            restRow.setOnDragListener(myDragEventListener);

            reorder.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int eventTouchXCoord;
                    int eventTouchYCoord;
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        ConstraintLayout ex = (ConstraintLayout) view.getParent();
                        ClipData clipData = ClipData.newPlainText(reorder.getTag().toString(), reorder.getTag().toString());
                        eventTouchXCoord = (int) motionEvent.getX();
                        eventTouchYCoord = (int) motionEvent.getY();
                        EventDragShadowBuilder shadowBuilder = new EventDragShadowBuilder(ex, eventTouchXCoord, eventTouchYCoord);
                        view.startDrag(clipData, shadowBuilder, view, 0);
                        return true;
                    }
                    return false;
                }

            });

        }
    }


    //Method for adding rest row
    public void inflateRestRow(String min, String sec){
        if(activity != null) {
            final String REORDER = "";
            LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
            final View restView = layoutInflater.inflate(R.layout.rest_row, null);
            final EditText minutes = (EditText) restView.findViewById(minutesTV);
            final ImageView reorder = (ImageView) restView.findViewById(R.id.reorder);
            final EditText seconds = (EditText) restView.findViewById(R.id.secondsTV);
            TextView rest = (TextView) restView.findViewById(R.id.rest);

            final ImageButton deleteRest = (ImageButton) restView.findViewById(R.id.deleteRest);
            deleteRest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContainerView.removeView((View) view.getParent());
                }
            });

            minDrawable = minutes.getBackground();
            secDrawable = seconds.getBackground();

            swipeLeft(rest);
            swipeLeft(minutes);
            swipeLeft(seconds);
            swipeLeft(restView);


            if (min != null && !min.isEmpty() && sec != null && !sec.isEmpty()) {
                minutes.setText(min);
                seconds.setText(sec);
            }

            reorder.setTag(REORDER);

            //drag listener for the views on the screen
            myDragEventListener myDragEventListener = new myDragEventListener();
            reorder.setOnDragListener(myDragEventListener);
            mContainerView.setOnDragListener(myDragEventListener);
            restView.setOnDragListener(myDragEventListener);

            reorder.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int eventTouchXCoord;
                    int eventTouchYCoord;
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        ConstraintLayout ex = (ConstraintLayout) view.getParent();
                        ClipData clipData = ClipData.newPlainText(reorder.getTag().toString(), reorder.getTag().toString());
                        eventTouchXCoord = (int) motionEvent.getX();
                        eventTouchYCoord = (int) motionEvent.getY();
                        EventDragShadowBuilder shadowBuilder = new EventDragShadowBuilder(ex, eventTouchXCoord, eventTouchYCoord);
                        view.startDrag(clipData, shadowBuilder, view, 0);
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            //adds view to the top of the buttons/bottom of the workout
            mContainerView.addView(restView, mContainerView.getChildCount() - 10);
        }
    }

    //same as inflateRestRow except that minutes does not have focus
    public void inflateRestRowWOutFocus(String min, String sec){
        if(activity != null) {
                final String REORDER = "";
                LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
                final View restView = layoutInflater.inflate(R.layout.rest_row, null);
                final EditText minutes = (EditText) restView.findViewById(minutesTV);
                final ImageView reorder = (ImageView) restView.findViewById(R.id.reorder);
                final EditText seconds = (EditText) restView.findViewById(R.id.secondsTV);
                TextView rest =(TextView) restView.findViewById(R.id.rest);

                final ImageButton deleteRest = (ImageButton) restView.findViewById(R.id.deleteRest);
                deleteRest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mContainerView.removeView((View) view.getParent());
                    }
                });

            minDrawable = minutes.getBackground();
            secDrawable = seconds.getBackground();

            swipeLeft(rest);
            swipeLeft(minutes);
            swipeLeft(seconds);
            swipeLeft(restView);


                if (min != null && !min.isEmpty() && sec != null && !sec.isEmpty()) {
                    minutes.setText(min);
                    seconds.setText(sec);
                }

                reorder.setTag(REORDER);

                //drag listener for the views on the screen
                myDragEventListener myDragEventListener = new myDragEventListener();
                reorder.setOnDragListener(myDragEventListener);
                mContainerView.setOnDragListener(myDragEventListener);
                restView.setOnDragListener(myDragEventListener);

                reorder.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        int eventTouchXCoord;
                        int eventTouchYCoord;
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            ConstraintLayout ex = (ConstraintLayout) view.getParent();
                            ClipData clipData = ClipData.newPlainText(reorder.getTag().toString(), reorder.getTag().toString());
                            eventTouchXCoord = (int) motionEvent.getX();
                            eventTouchYCoord = (int) motionEvent.getY();
                            EventDragShadowBuilder shadowBuilder = new EventDragShadowBuilder(ex, eventTouchXCoord, eventTouchYCoord);
                            view.startDrag(clipData, shadowBuilder, view, 0);
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

                //adds view to the top of the buttons/bottom of the workout
                mContainerView.addView(restView, mContainerView.getChildCount() - 10);
                minutes.clearFocus();
        }
    }

    //same as inflateEditRow except exercise does not have focus
    public void inflateEditRowWOutFocus(String exercise, String weight, String sets, String reps){


        final String REORDER = "";
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View rowView = layoutInflater.inflate(R.layout.row, null);
        final AutoCompleteTextView autoCompleteTextView1 = (AutoCompleteTextView) rowView.findViewById(autoCompleteTextView);
        final EditText set = (EditText) rowView.findViewById(R.id.sets);
        final EditText rep = (EditText) rowView.findViewById(R.id.reps);
        final ImageView reorder = (ImageView) rowView.findViewById(R.id.reorder);
        final EditText weigh = (EditText) rowView.findViewById(R.id.weight);
        final ImageButton deleteEx = (ImageButton) rowView.findViewById(R.id.deleteEx);
        deleteEx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContainerView.removeView((View) view.getParent());
            }
        });

        weightDrawable = weigh.getBackground();
        exDrawable = autoCompleteTextView1.getBackground();
        repDrawable = rep.getBackground();
        setDrawable = set.getBackground();

        swipeLeft(autoCompleteTextView1);
        swipeLeft(weigh);
        swipeLeft(set);
        swipeLeft(rep);
        swipeLeft(rowView);


        autoCompleteTextView1.setAdapter(getExerciseAdapter(getActivity()));

        if (exercise != null && !exercise.isEmpty() && sets != null && !sets.isEmpty() && reps != null && !reps.isEmpty()) {
            autoCompleteTextView1.setText(exercise);
            set.setText(sets);
            rep.setText(reps);
            weigh.setText(weight);
        }

        reorder.setTag(REORDER);

        //drag listener for all the views on the screen
        myDragEventListener myDragEventListener = new myDragEventListener();
        reorder.setOnDragListener(myDragEventListener);
        mContainerView.setOnDragListener(myDragEventListener);
        rowView.setOnDragListener(myDragEventListener);

        reorder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int eventTouchXCoord;
                int eventTouchYCoord;
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    ConstraintLayout ex = (ConstraintLayout) view.getParent();
                    ClipData clipData = ClipData.newPlainText(reorder.getTag().toString(), reorder.getTag().toString());
                    eventTouchXCoord = (int) motionEvent.getX();
                    eventTouchYCoord = (int) motionEvent.getY();
                    EventDragShadowBuilder shadowBuilder = new EventDragShadowBuilder(ex, eventTouchXCoord, eventTouchYCoord);
                    view.startDrag(clipData, shadowBuilder, view, 0);
                    return true;
                } else {
                    return false;
                }
            }
        });

        //adds the view to the top of the buttons/bottom of the workout
        mContainerView.addView(rowView, mContainerView.getChildCount() - 10);
        autoCompleteTextView1.clearFocus();
    }

    //exercise adapter for the exercise EditText
    //displays the options of exercises
    private ArrayAdapter<String> getExerciseAdapter(Context context){
        Activity activity = getActivity();
        String[] exercise = new String[]{};
        if(activity != null && isAdded()) {
            exercise = getResources().getStringArray(R.array.exercises);
        }
        return new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, exercise);
    }

    //Method for when the rest row is about to start
    //Passes the minutes and seconds EditText as parameters to startTimerSeconds
    public void restCountdownSeconds(){
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View restRow = layoutInflater.inflate(R.layout.rest_row, null);
        View exerciseRow = layoutInflater.inflate(R.layout.row, null);
        //if the first child is an exercise row and the second is a rest row
        if(mContainerView.getChildAt(1).getId() == restRow.getId() && mContainerView.getChildAt(0).getId() == exerciseRow.getId()) {
            secondsTVParent = mContainerView.getChildAt(1);
            secondsTV = (EditText) secondsTVParent.findViewById(R.id.secondsTV);
            minuteTV = (EditText) secondsTVParent.findViewById(R.id.minutesTV);
            int min = Integer.valueOf(minuteTV.getText().toString());
            int sec = Integer.valueOf(secondsTV.getText().toString());
            if(!startTimer) {
                serviceIntent.putExtra("seconds", sec);
                serviceIntent.putExtra("minutes", min);
                activity.registerReceiver(secondReceiver, new IntentFilter(COUNTDOWN_SECONDS));
                activity.startService(serviceIntent);
                startTimer = true;
            }
        }
        //if the first child is a rest row
        else if(mContainerView.getChildAt(0).getId() == restRow.getId()){
            secondsTVParent = mContainerView.getChildAt(0);
            secondsTV = (EditText) secondsTVParent.findViewById(R.id.secondsTV);
            minuteTV = (EditText) secondsTVParent.findViewById(R.id.minutesTV);
            int min = Integer.valueOf(minuteTV.getText().toString());
            int sec = Integer.valueOf(secondsTV.getText().toString());

            if(!startTimer) {
                serviceIntent.putExtra("seconds", sec);
                serviceIntent.putExtra("minutes", min);
                activity.registerReceiver(secondReceiver, new IntentFilter(COUNTDOWN_SECONDS));
                activity.startService(serviceIntent);
                startTimer = true;
            }
        }
    }

    //checks to see if a service is running
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //broadcast receiver for the seconds
    private BroadcastReceiver secondReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(activity != null) {

                startTimer = true;

                SharedPreferences minPref = activity.getSharedPreferences("min", Context.MODE_PRIVATE);
                SharedPreferences secPref = activity.getSharedPreferences("sec", Context.MODE_PRIVATE);
                SharedPreferences countdownMin = activity.getSharedPreferences("countdownMin", Context.MODE_PRIVATE);
                SharedPreferences timerPref = activity.getSharedPreferences("timer", Context.MODE_PRIVATE);
                boolean timerFinished = timerPref.getBoolean("timerFinished", false);
                int decrementedMin = countdownMin.getInt("minCountdown", -1);
                inputtedMin = minPref.getInt("inputtedMin", -1);
                inputtedSec = secPref.getInt("inputtedSec", -1);
                Log.d("inputtedMin", String.format("%s", inputtedMin));
                Log.d("inputtedSec", String.format("%s", inputtedSec));


                final View restRow = layoutInflater.inflate(R.layout.rest_row, null);
                final View exerciseRow = layoutInflater.inflate(R.layout.row, null);
                final View pyramidContainer = layoutInflater.inflate(R.layout.pyramidset, null);
                final View supersetContainer = layoutInflater.inflate(R.layout.superset, null);


                //gets each countdown sec
                long millis = intent.getLongExtra("countdownSeconds", -1);

                //updates the second text every second
                secondsTV.setText(String.format("%s", millis / 1000));

                if (decrementedMin != -1) {
                    minuteTV.setText(String.format("%s", decrementedMin));
                    countdownMin.edit().clear().apply();
                }

                //if timer onFinish() is called
                if (inputtedMin != -1 && inputtedSec != -1) {

                    //if timer is started for just rest row
                    if (mContainerView.getChildAt(0).getId() == restRow.getId()) {

                        //if timer is finished remove timer
                        //set startTimer to false
                        //vibrate or reduce sound once rest is finished
                        //if (timerFinished) {

                            mContainerView.removeView(secondsTVParent);
                            startTimer = false;
                            minPref.edit().clear().apply();
                            secPref.edit().clear().apply();
                            //timerPref.edit().clear().apply();

                        //}
                    }

                    //if timer is started for a exerciseRow
                    else if (mContainerView.getChildAt(0).getId() == exerciseRow.getId() && mContainerView.getChildAt(1).getId() == restRow.getId()) {
                        View exerciseView = mContainerView.getChildAt(0);
                        EditText sets = (EditText) exerciseView.findViewById(R.id.sets);
                        int setsInt = Integer.parseInt(sets.getText().toString());

                        /*
                        If timer is finished and value of set is above 1: reset the rest, but don't start timer, stop service, and unregister receivers
                        If timer is finished and value of set is 1: remove rest row, stop service, and unregister receiver
                         */
                        //if (secondsTV.getText().toString().equals("1") && minuteTV.getText().toString().equals("0")) {
                        if (setsInt > 1) {

                            minuteTV.setText(String.format("%s", inputtedMin));
                            secondsTV.setText(String.format("%s", inputtedSec));
                            sets.setText(String.format("%s", setsInt));

                            startTimer = false;

                            activity.stopService(serviceIntent);
                            activity.unregisterReceiver(secondReceiver);

                            minPref.edit().clear().apply();
                            secPref.edit().clear().apply();

                        } else {

                            mContainerView.removeView(secondsTVParent);
                            startTimer = false;

                            activity.stopService(serviceIntent);
                            activity.unregisterReceiver(secondReceiver);

                            minPref.edit().clear().apply();
                            secPref.edit().clear().apply();
                        }
                        //}
                    }

                    //if timer is started for a pyramid set
                    else if (mContainerView.getChildAt(0).getId() == pyramidContainer.getId()) {
                        LinearLayout layout = (LinearLayout) mContainerView.getChildAt(0);
                        boolean removeView = true;
                        startTimer = false;

                        if (layout.getChildCount() > 2) {
                            removeView = false;
                            minuteTV.setText(String.format("%s", inputtedMin));
                            secondsTV.setText(String.format("%s", inputtedSec));
                        }

                        if (removeView) {

                            activity.stopService(serviceIntent);
                            activity.unregisterReceiver(secondReceiver);

                            minPref.edit().clear().apply();
                            secPref.edit().clear().apply();

                            layout.removeViewAt(layout.getChildCount() - 1);

                        } else if (!removeView) {

                            activity.stopService(serviceIntent);
                            activity.unregisterReceiver(secondReceiver);

                            minPref.edit().clear().apply();
                            secPref.edit().clear().apply();
                        }
                    }

                    //if timer is started for superset
                    else if (mContainerView.getChildAt(0).getId() == supersetContainer.getId()) {
                        boolean removeView = true;
                        LinearLayout layout = (LinearLayout) mContainerView.getChildAt(0);
                        startTimer = false;

                        //iterates through all the exercises in the superset row
                        //if 1 of them has set > 1, don't remove timer, and reset timer
                        //else remove timer
                        for (int i = 0; i < layout.getChildCount() - 1; i++) {
                            View exerciseView = layout.getChildAt(i);
                            EditText sets = (EditText) exerciseView.findViewById(R.id.sets);
                            int setInt = Integer.valueOf(sets.getText().toString());
                            if (setInt > 1) {
                                removeView = false;
                            }
                        }
                        if (removeView) {
                            activity.stopService(serviceIntent);
                            activity.unregisterReceiver(secondReceiver);

                            minPref.edit().clear().apply();
                            secPref.edit().clear().apply();

                            layout.removeViewAt(layout.getChildCount() - 1);
                        }
                        else{

                            minuteTV.setText(String.format("%s", inputtedMin));
                            secondsTV.setText(String.format("%s", inputtedSec));
                            activity.stopService(serviceIntent);
                            activity.unregisterReceiver(secondReceiver);

                            minPref.edit().clear().apply();
                            secPref.edit().clear().apply();
                        }

                    }
                }


            }
        }
    };



    //Method for decreasing set in exercise row
    public void decreaseSet() {
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.row, null);
            if (mContainerView.getChildCount() > 10 && mContainerView.getChildAt(0).getId() == v.getId()) {
                View a = mContainerView.getChildAt(0);
                EditText set = (EditText) a.findViewById(R.id.sets);
                String setString = set.getText().toString();
                int intString = Integer.parseInt(setString);

                //if set > 1, decrease by 1 and assign set new int
                if (intString > 1) {
                    --intString;
                    set.setText(String.valueOf(intString));
                }

                //if set = 1, remove the view
                else if (intString == 1) {
                    mContainerView.removeViewAt(0);
                }
            }
    }

    //method for decreasing pyramid set
    public void decreasePyramidSet(LinearLayout layout){
        LayoutInflater layoutInflater = (LayoutInflater) activity.getLayoutInflater();
        View exerciseRow = layoutInflater.inflate(R.layout.row, null);
        //if first child is an exercise row
        if(layout.getChildAt(0).getId() == exerciseRow.getId()){
            //if this exercise row is the only child, remove the row and remove the view from container
            if(layout.getChildCount() == 1){
                layout.removeViewAt(0);
                mContainerView.removeView(layout);
            }
            //else if the next child is another exercise row, get the weight and reps of the second row and replace the first row with it. Delete second row
            else if(layout.getChildAt(1).getId() == exerciseRow.getId()){
                View first = layout.getChildAt(0);
                View second = layout.getChildAt(1);
                EditText weightFirst = (EditText) first.findViewById(R.id.weight);
                EditText repsFirst = (EditText) first.findViewById(R.id.reps);
                EditText weightSecond = (EditText) second.findViewById(R.id.weight);
                EditText repsSecond = (EditText) second.findViewById(R.id.reps);
                weightFirst.setText(weightSecond.getText().toString());
                repsFirst.setText(repsSecond.getText().toString());
                layout.removeViewAt(1);
            }
        }
    }

    public void restCountdownSecondsPyramidSet(LinearLayout layout){
        secondsTV = (EditText) layout.findViewById(R.id.secondsTV);
        minuteTV = (EditText) layout.findViewById(R.id.minutesTV);
        int min = Integer.valueOf(minuteTV.getText().toString());
        int sec = Integer.valueOf(secondsTV.getText().toString());

            if(!startTimer) {
                serviceIntent.putExtra("seconds", sec);
                serviceIntent.putExtra("minutes", min);
                activity.registerReceiver(secondReceiver, new IntentFilter(COUNTDOWN_SECONDS));
                activity.startService(serviceIntent);
                startTimer = true;
            }

        }


    /*Below are methods only for the superset row*/

    //Method for decreasing set of exercise row in a superset row
    public void decreaseSuperset(int position, LinearLayout layout) {
        LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View exerciseRow = layoutInflater.inflate(R.layout.row, null);
        if(layout.getChildAt(position).getId() == exerciseRow.getId()) {
            View v = layout.getChildAt(position);
            EditText set = (EditText) v.findViewById(R.id.sets);
            String setString = set.getText().toString();
            int intString = Integer.parseInt(setString);
            if (intString > 1) {
                --intString;
                set.setText(String.valueOf(intString));
            }
            else if (intString == 1){
                layout.removeViewAt(position);
                index--;
            }
        }
        if(layout.getChildCount() == 0){
            mContainerView.removeView(layout);
        }
    }

    public void restCountdownSecondsSuperset(int index, LinearLayout layout){
        secondsTV = (EditText) layout.findViewById(R.id.secondsTV);
        minuteTV = (EditText) layout.findViewById(R.id.minutesTV);
        int min = Integer.valueOf(minuteTV.getText().toString());
        int sec = Integer.valueOf(secondsTV.getText().toString());
        if(!startTimer) {
            serviceIntent.putExtra("seconds", sec);
            serviceIntent.putExtra("minutes", min);
            activity.registerReceiver(secondReceiver, new IntentFilter(COUNTDOWN_SECONDS));
            activity.startService(serviceIntent);
            startTimer = true;
        }
    }

    //Method for adding superset row
    public void inflateEditRowSuperset(ArrayList<String> exercise, ArrayList<String> weight, ArrayList<String> sets, ArrayList<String> reps, ArrayList<String> min, ArrayList<String> sec, int numExercise){
        final String REORDER = "";
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View supersetView = layoutInflater.inflate(superset, null);
        final LinearLayout layout = (LinearLayout) supersetView.findViewById(R.id.supersetLayout);

        mContainerView.addView(supersetView, mContainerView.getChildCount() - 10);
        //displays numExercise amount of exercise rows and after the last exercise row is added, a rest row is added as well
        for(int i = 0; i < numExercise; i++) {
            View restRow = layoutInflater.inflate(R.layout.rest_row, null);
            View exerciseView = layoutInflater.inflate(R.layout.row, null);
            layout.addView(exerciseView);
            final AutoCompleteTextView autoCompleteTextView1 = (AutoCompleteTextView) exerciseView.findViewById(autoCompleteTextView);
            final EditText set = (EditText) exerciseView.findViewById(R.id.sets);
            final ImageView reorder = (ImageView) exerciseView.findViewById(R.id.reorder);
            final EditText rep = (EditText) exerciseView.findViewById(R.id.reps);
            final EditText weigh = (EditText) exerciseView.findViewById(R.id.weight);
            final ImageButton deleteEx = (ImageButton) exerciseView.findViewById(R.id.deleteEx);
            if(i > 0 && i < numExercise){
                reorder.setVisibility(View.INVISIBLE);
            }

            weightDrawable = weigh.getBackground();
            exDrawable = autoCompleteTextView1.getBackground();
            repDrawable = rep.getBackground();
            setDrawable = set.getBackground();

            swipeLeft(autoCompleteTextView1);
            swipeLeft(set);
            swipeLeft(rep);
            swipeLeft(weigh);
            swipeLeft(exerciseView);

            deleteEx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if((View) view.getParent() == layout.getChildAt(0)){
                        layout.removeView((View) view.getParent());
                        if(layout.getChildCount() > 0) {
                            View first = layout.getChildAt(0);
                            ImageView order = (ImageView) first.findViewById(R.id.reorder);
                            order.setVisibility(View.VISIBLE);
                        }
                    }
                    else {
                        layout.removeView((View) view.getParent());
                    }
                    //if there are no children after pressing deleteEx, remove the superset row from mContainerView
                    if(layout.getChildCount() == 0){
                        mContainerView.removeView(layout);
                    }
                }
            });
            reorder.setTag(REORDER);

            //drag listener for all the views on the screen
            myDragEventListener myDragEventListener = new myDragEventListener();
            reorder.setOnDragListener(myDragEventListener);
            mContainerView.setOnDragListener(myDragEventListener);
            exerciseView.setOnDragListener(myDragEventListener);
            restRow.setOnDragListener(myDragEventListener);

            reorder.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int eventTouchXCoord;
                    int eventTouchYCoord;
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        ConstraintLayout ex = (ConstraintLayout) view.getParent();
                        ClipData clipData = ClipData.newPlainText(reorder.getTag().toString(), reorder.getTag().toString());
                        eventTouchXCoord = (int) motionEvent.getX();
                        eventTouchYCoord = (int) motionEvent.getY();
                        EventDragShadowBuilder shadowBuilder = new EventDragShadowBuilder(ex, eventTouchXCoord, eventTouchYCoord);
                        view.startDrag(clipData, shadowBuilder, view, 0);
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            autoCompleteTextView1.setAdapter(getExerciseAdapter(getActivity()));
            if (exercise != null && !exercise.isEmpty() && sets != null && !sets.isEmpty() && reps != null && !reps.isEmpty()) {
                autoCompleteTextView1.setText(exercise.get(i));
                set.setText(sets.get(i));
                rep.setText(reps.get(i));
                weigh.setText(weight.get(i));
            }
            //adds rest row for the superset
            if(i == numExercise - 1){
                layout.addView(restRow);
                final EditText minutes = (EditText) restRow.findViewById(minutesTV);
                final EditText seconds = (EditText) restRow.findViewById(R.id.secondsTV);
                TextView rest = (TextView) restRow.findViewById(R.id.rest);

                minDrawable = minutes.getBackground();
                secDrawable = seconds.getBackground();

                swipeLeft(minutes);
                swipeLeft(seconds);
                swipeLeft(rest);
                swipeLeft(restRow);

                final ImageView order = (ImageView) restRow.findViewById(R.id.reorder);
                order.setVisibility(View.INVISIBLE);
                if (min != null && !min.isEmpty() && sec != null && !sec.isEmpty()) {
                    minutes.setText(min.get(0));
                    seconds.setText(sec.get(0));
                }
                final ImageButton deleteRest = (ImageButton) restRow.findViewById(R.id.deleteRest);
                deleteRest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        layout.removeView((View) view.getParent());
                        if(layout.getChildCount() == 0){
                            mContainerView.removeView(layout);
                        }
                    }
                });
            }
        }
    }

    //Method for adding superset row with no rest row
    public void inflateEditRowSupersetNoRest(ArrayList<String> exercise, ArrayList<String> weight, ArrayList<String> sets, ArrayList<String> reps, int numExercise){
        final String REORDER = "";
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View supersetView = layoutInflater.inflate(superset, null);
        final LinearLayout layout = (LinearLayout) supersetView.findViewById(R.id.supersetLayout);

        mContainerView.addView(supersetView, mContainerView.getChildCount() - 10);
        //displays numExercise amount of exercise rows and after the last exercise row is added, a rest row is added as well
        for(int i = 0; i < numExercise; i++) {
            View restRow = layoutInflater.inflate(R.layout.rest_row, null);
            View exerciseView = layoutInflater.inflate(R.layout.row, null);
            layout.addView(exerciseView);
            final AutoCompleteTextView autoCompleteTextView1 = (AutoCompleteTextView) exerciseView.findViewById(autoCompleteTextView);
            autoCompleteTextView1.clearFocus();
            final EditText set = (EditText) exerciseView.findViewById(R.id.sets);
            final ImageView reorder = (ImageView) exerciseView.findViewById(R.id.reorder);
            final EditText rep = (EditText) exerciseView.findViewById(R.id.reps);
            final EditText weigh = (EditText) exerciseView.findViewById(R.id.weight);
            final ImageButton deleteEx = (ImageButton) exerciseView.findViewById(R.id.deleteEx);
            if(i > 0 && i < numExercise){
                reorder.setVisibility(View.INVISIBLE);
            }

            weightDrawable = weigh.getBackground();
            exDrawable = autoCompleteTextView1.getBackground();
            repDrawable = rep.getBackground();
            setDrawable = set.getBackground();

            swipeLeft(autoCompleteTextView1);
            swipeLeft(set);
            swipeLeft(rep);
            swipeLeft(weigh);
            swipeLeft(exerciseView);

            deleteEx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if((View) view.getParent() == layout.getChildAt(0)){
                        layout.removeView((View) view.getParent());
                        if(layout.getChildCount() > 0) {
                            View first = layout.getChildAt(0);
                            ImageView order = (ImageView) first.findViewById(R.id.reorder);
                            order.setVisibility(View.VISIBLE);
                        }
                    }
                    else {
                        layout.removeView((View) view.getParent());
                    }
                    //if there are no children after pressing deleteEx, remove the superset row from mContainerView
                    if(layout.getChildCount() == 0){
                        mContainerView.removeView(layout);
                    }
                }
            });
            reorder.setTag(REORDER);

            //drag listener for all the views on the screen
            myDragEventListener myDragEventListener = new myDragEventListener();
            reorder.setOnDragListener(myDragEventListener);
            mContainerView.setOnDragListener(myDragEventListener);
            exerciseView.setOnDragListener(myDragEventListener);
            restRow.setOnDragListener(myDragEventListener);

            reorder.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int eventTouchXCoord;
                    int eventTouchYCoord;
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        ConstraintLayout ex = (ConstraintLayout) view.getParent();
                        ClipData clipData = ClipData.newPlainText(reorder.getTag().toString(), reorder.getTag().toString());
                        eventTouchXCoord = (int) motionEvent.getX();
                        eventTouchYCoord = (int) motionEvent.getY();
                        EventDragShadowBuilder shadowBuilder = new EventDragShadowBuilder(ex, eventTouchXCoord, eventTouchYCoord);
                        view.startDrag(clipData, shadowBuilder, view, 0);
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            autoCompleteTextView1.setAdapter(getExerciseAdapter(getActivity()));
            if (exercise != null && !exercise.isEmpty() && sets != null && !sets.isEmpty() && reps != null && !reps.isEmpty()) {
                autoCompleteTextView1.setText(exercise.get(i));
                set.setText(sets.get(i));
                rep.setText(reps.get(i));
                weigh.setText(weight.get(i));
            }
        }
    }


    //Method for the dialog that shows when +add superset is clicked
    public void exerciseNumSuperset(){
        CharSequence colors[] = new CharSequence[] {"2", "3", "4", "5", "6", "7", "8", "9", "10"};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Pick # of exercises");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]
                inflateEditRowSuperset(null, null, null, null, null, null, which + 2);
            }
        });
        builder.show();
    }

    //Method for the dialog that shows when +add superset is clicked
    public void setNumPyramidSet(){
        CharSequence colors[] = new CharSequence[] {"2", "3", "4", "5", "6", "7", "8", "9", "10"};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Pick # of sets");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]
                inflatePyramidRow(null, null, null, null, null, which + 2);
            }
        });
        builder.show();
    }



    public void notification(String time){

        RemoteViews contentView = new RemoteViews(activity.getPackageName(), R.layout.notification_drawer);
        contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(activity)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Time Elapsed")
                        .setContentText(time)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(Notification.PRIORITY_HIGH);
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(activity, MainActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(activity);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);


// mNotificationId is a unique integer your app uses to identify the
// notification. For example, to cancel the notification, you can pass its ID
// number to NotificationManager.cancel().
        final int mNotificationId = 1;
        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }


    protected class myDragEventListener implements View.OnDragListener {

        // This is the method that the system calls when it dispatches a drag event to the
        // listener.

        public boolean onDrag(View v, DragEvent event) {

            // Defines a variable to store the action type for the incoming event
            final int action = event.getAction();


            int absLocation[] = new int[2];
            v.getLocationInWindow(absLocation);


            Point touchPosition;


            // Handles each of the expected events
            switch(action) {

                case DragEvent.ACTION_DRAG_STARTED:

                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:

                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:

                    touchPosition = getTouchPositionFromDragEvent(v, event);

                    if (touchPosition.y > (layout_MainMenu.getBottom() + mScrollDistance - 200)){
                        homeScroll.smoothScrollBy(0, 30);
                    }
                    else if (touchPosition.y < (absoluteTop + 300)){
                        homeScroll.smoothScrollBy(0, -30);
                    }

                    return true;

                case DragEvent.ACTION_DRAG_EXITED:

                    return true;

                case DragEvent.ACTION_DROP:

                        //if imageview is dropped on onto the same imageview
                        //else do nothing
                        if(v.getId() == R.id.reorder) {

                            //dragged view
                            View dragReorder = (View) event.getLocalState();

                            //superset view for dropped view
                            View vP = ((View) v.getParent());
                            //superset view for dragged view
                            View dragP = ((View) dragReorder.getParent());


                            //if vP is a superset layout and dragP is a superset layout
                            if (vP instanceof ConstraintLayout && dragP instanceof ConstraintLayout && ((View) vP.getParent()).getId() == R.id.supersetLayout && ((View) dragP.getParent()).getId() == R.id.supersetLayout) {
                                LinearLayout exerciseLayout = (LinearLayout) dragP.getParent();
                                LinearLayout vParent = (LinearLayout) vP.getParent();
                                switchViews(mContainerView.indexOfChild(vParent), mContainerView.indexOfChild(exerciseLayout), exerciseLayout, vParent);
                            }

                            //if vP is a superset row and dragP is an exercise row
                            else if (vP instanceof ConstraintLayout && dragP instanceof ConstraintLayout && ((View) vP.getParent()).getId() == R.id.supersetLayout && ((View) dragP.getParent()).getId() != R.id.supersetLayout && ((View) dragP.getParent()).getId() != R.id.pyramidContainer && dragP.getId() == R.id.exercise_row) {
                                ConstraintLayout exerciseLayout = (ConstraintLayout) dragReorder.getParent();
                                LinearLayout vParent = (LinearLayout) vP.getParent();
                                switchViews(mContainerView.indexOfChild(vParent), mContainerView.indexOfChild(exerciseLayout), exerciseLayout, vParent);
                            }

                            //if vP is a superset row and dragP is a rest row
                            else if (vP instanceof ConstraintLayout && dragP instanceof ConstraintLayout && ((View) vP.getParent()).getId() == R.id.supersetLayout && dragP.getId() == R.id.rest_row) {
                                ConstraintLayout exerciseLayout = (ConstraintLayout) dragReorder.getParent();
                                LinearLayout vParent = (LinearLayout) vP.getParent();
                                switchViews(mContainerView.indexOfChild(vParent), mContainerView.indexOfChild(exerciseLayout), exerciseLayout, vParent);
                            }

                            //if vP is a rest row and dragP is a superset row
                            else if (vP instanceof ConstraintLayout && dragP instanceof ConstraintLayout && ((View) dragP.getParent()).getId() == R.id.supersetLayout && vP.getId() == R.id.rest_row) {
                                LinearLayout exerciseLayout = (LinearLayout) dragP.getParent();
                                ConstraintLayout vParent = (ConstraintLayout) v.getParent();
                                switchViews(mContainerView.indexOfChild(vParent), mContainerView.indexOfChild(exerciseLayout), exerciseLayout, vParent);
                            }

                            //if vP is an exercise row and dragP is a superset row
                            else if (vP instanceof ConstraintLayout && dragP instanceof ConstraintLayout && ((View) dragP.getParent()).getId() == R.id.supersetLayout && ((View) vP.getParent()).getId() != R.id.supersetLayout && ((View) vP.getParent()).getId() != R.id.pyramidContainer && vP.getId() == R.id.exercise_row) {
                                LinearLayout exerciseLayout = (LinearLayout) dragP.getParent();
                                ConstraintLayout vParent = (ConstraintLayout) v.getParent();
                                switchViews(mContainerView.indexOfChild(vParent), mContainerView.indexOfChild(exerciseLayout), exerciseLayout, vParent);
                            }

                            //if vP is rest row and dragP is exercise row
                            else if (vP instanceof ConstraintLayout && dragP instanceof ConstraintLayout && ((View) dragP.getParent()).getId() != R.id.supersetLayout && ((View) dragP.getParent()).getId() != R.id.pyramidContainer &&
                                    ((View) vP.getParent()).getId() != R.id.supersetLayout && ((View) vP.getParent()).getId() != R.id.pyramidContainer) {
                                ConstraintLayout exerciseLayout = (ConstraintLayout) dragReorder.getParent();
                                ConstraintLayout vParent = (ConstraintLayout) v.getParent();
                                switchViews(mContainerView.indexOfChild(vParent), mContainerView.indexOfChild(exerciseLayout), exerciseLayout, vParent);
                            }

                            //if vP is an exercise row and dragP is a rest row
                            else if (vP instanceof ConstraintLayout && dragP instanceof ConstraintLayout && ((View) vP.getParent()).getId() != R.id.supersetLayout && ((View) vP.getParent()).getId() != R.id.pyramidContainer && vP.getId() == R.id.exercise_row && dragP.getId() == R.id.rest_row
                                    && ((View) dragP.getParent()).getId() != R.id.supersetLayout && ((View) dragP.getParent()).getId() != R.id.pyramidContainer) {
                                ConstraintLayout exerciseLayout = (ConstraintLayout) dragReorder.getParent();
                                ConstraintLayout vParent = (ConstraintLayout) v.getParent();
                                switchViews(mContainerView.indexOfChild(vParent), mContainerView.indexOfChild(exerciseLayout), exerciseLayout, vParent);
                            }

                            //if vP is a rest row and dragP is a rest row
                            else if (vP instanceof ConstraintLayout && dragP instanceof ConstraintLayout && vP.getId() == R.id.rest_row && dragP.getId() == R.id.rest_row) {
                                ConstraintLayout exerciseLayout = (ConstraintLayout) dragReorder.getParent();
                                ConstraintLayout vParent = (ConstraintLayout) v.getParent();
                                switchViews(mContainerView.indexOfChild(vParent), mContainerView.indexOfChild(exerciseLayout), exerciseLayout, vParent);
                            }

                            //if vP is a pyramid layout and dragP is a pyramid set layout
                            else if (vP instanceof ConstraintLayout && dragP instanceof ConstraintLayout && ((View) vP.getParent()).getId() == R.id.pyramidContainer && ((View) dragP.getParent()).getId() == R.id.pyramidContainer) {
                                LinearLayout exerciseLayout = (LinearLayout) dragP.getParent();
                                LinearLayout vParent = (LinearLayout) vP.getParent();
                                switchViews(mContainerView.indexOfChild(vParent), mContainerView.indexOfChild(exerciseLayout), exerciseLayout, vParent);
                            }

                            //if vP is a pyramid set row and dragP is an exercise row
                            else if (vP instanceof ConstraintLayout && dragP instanceof ConstraintLayout && ((View) vP.getParent()).getId() == R.id.pyramidContainer && ((View) dragP.getParent()).getId() != R.id.supersetLayout && ((View) dragP.getParent()).getId() != R.id.pyramidContainer && dragP.getId() == R.id.exercise_row) {
                                ConstraintLayout exerciseLayout = (ConstraintLayout) dragReorder.getParent();
                                LinearLayout vParent = (LinearLayout) vP.getParent();
                                switchViews(mContainerView.indexOfChild(vParent), mContainerView.indexOfChild(exerciseLayout), exerciseLayout, vParent);
                            }

                            //if vP is an exercise row and dragP is a pyramid set row
                            else if (vP instanceof ConstraintLayout && dragP instanceof ConstraintLayout && ((View) dragP.getParent()).getId() == R.id.pyramidContainer && ((View) vP.getParent()).getId() != R.id.supersetLayout && ((View) vP.getParent()).getId() != R.id.pyramidContainer && vP.getId() == R.id.exercise_row) {
                                LinearLayout exerciseLayout = (LinearLayout) dragP.getParent();
                                ConstraintLayout vParent = (ConstraintLayout) v.getParent();
                                switchViews(mContainerView.indexOfChild(vParent), mContainerView.indexOfChild(exerciseLayout), exerciseLayout, vParent);
                            }

                            //if vP is a pyramid set row and dragP is a rest row
                            else if (vP instanceof ConstraintLayout && dragP instanceof ConstraintLayout && ((View) vP.getParent()).getId() == R.id.pyramidContainer && dragP.getId() == R.id.rest_row) {
                                ConstraintLayout exerciseLayout = (ConstraintLayout) dragReorder.getParent();
                                LinearLayout vParent = (LinearLayout) vP.getParent();
                                switchViews(mContainerView.indexOfChild(vParent), mContainerView.indexOfChild(exerciseLayout), exerciseLayout, vParent);
                            }

                            //if vP is a rest row and dragP is a pyramid set row
                            else if (vP instanceof ConstraintLayout && dragP instanceof ConstraintLayout && ((View) dragP.getParent()).getId() == R.id.pyramidContainer && vP.getId() == R.id.rest_row) {
                                LinearLayout exerciseLayout = (LinearLayout) dragP.getParent();
                                ConstraintLayout vParent = (ConstraintLayout) v.getParent();
                                switchViews(mContainerView.indexOfChild(vParent), mContainerView.indexOfChild(exerciseLayout), exerciseLayout, vParent);
                            }

                            //if vP is a pyramid layout and dragP is a super set layout
                            else if (vP instanceof ConstraintLayout && dragP instanceof ConstraintLayout && (((View) vP.getParent()).getId() == R.id.pyramidContainer && ((View) dragP.getParent()).getId() == R.id.supersetLayout) ||
                                    (((View) vP.getParent()).getId() == R.id.supersetLayout && ((View) dragP.getParent()).getId() == R.id.pyramidContainer)) {
                                LinearLayout exerciseLayout = (LinearLayout) dragP.getParent();
                                LinearLayout vParent = (LinearLayout) vP.getParent();
                                switchViews(mContainerView.indexOfChild(vParent), mContainerView.indexOfChild(exerciseLayout), exerciseLayout, vParent);
                            }

                        }
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    // returns true; the value is ignored.
                    return true;

                // An unknown action type was received.
                default:
                    break;
            }

            return false;
        }
    };

    //returns the abs position of a dragged view
    public static Point getTouchPositionFromDragEvent(View item, DragEvent event) {
        Rect rItem = new Rect();
        item.getGlobalVisibleRect(rItem);
        return new Point(rItem.left + Math.round(event.getX()), rItem.top + Math.round(event.getY()));
    }

    //method to switch view positions when dragging and dropping
    public void switchViews(int droppedIndex, int dragIndex, View exerciseLayout, View vParent){
        int childcount = mContainerView.getChildCount() - 10;
        View[] children = new View[childcount];
        //iterate through mContainerView and assign each view to View[] children
        //the positions of exerciselayout and vParent are swapped
        for (int i = 0; i < childcount; i++) {
            if (i == droppedIndex) {
                children[i] = exerciseLayout;
            } else if (i == dragIndex) {
                children[i] = vParent;
            } else {
                children[i] = mContainerView.getChildAt(i);
            }
        }

        //remove all views from mContainerView and add the new View[]
        for (int i = 0; i < childcount; i++) {
            mContainerView.removeViewAt(0);
        }
        for (int i = 0; i < childcount; i++) {
            mContainerView.addView(children[i], mContainerView.getChildCount() - 10);
        }
    }


    //method that sets the swipe listener that mimics the next button
    //swipe right to left
    public void swipeLeft(View view){
        view.setOnTouchListener(new OnSwipeTouchListener(((MainActivity) getActivity())){
            public void onSwipeLeft(){
                nextSet();
            }
        });
    }

    /*
    It will either
    1) Decrease the set you are on, and move onto the next exercise or rest
    2) Start a rest timer
    Index is used as the current position you are at in terms in supersets and pyramid sets
     */
    public void nextSet(){

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View supersetView = layoutInflater.inflate(superset, null);
        View restRow = layoutInflater.inflate(R.layout.rest_row, null);
        View pyramidView = layoutInflater.inflate(R.layout.pyramidset, null);

        /*
        Gets a sharedPref that is used as the current position you are at in terms in supersets and pyramid sets
        If there is no existing sharedPref, index is 0
        If there is existing sharedPref, use that value as index
         */
        SharedPreferences pref = activity.getSharedPreferences("swipeIndex", Context.MODE_PRIVATE);
        if(pref.getInt("index", -1) == -1){
            index = 0;
        }
        else{
            index = pref.getInt("index", -1);
        }

        /*
        If exercise you are currently on is a superset, and if a rest timer has not been started and start has been clicked
         */
        if (mContainerView.getChildAt(0).getId() == supersetView.getId()) {
            if(!startTimer && startClick) {

                View supersetContainer = mContainerView.getChildAt(0);
                LinearLayout layout = (LinearLayout) supersetContainer.findViewById(R.id.supersetLayout);
                int position = layout.getChildCount();

                //if rest row exists and current position is not the last exercise
                if (index != position - 2 && layout.getChildAt(position - 1).getId() == restRow.getId()) {

                    decreaseSuperset(index, layout);
                    index++;
                    pref.edit().putInt("index", index).apply();
                }

                //if rest row exists and current position is the last exercise
                else if (index == position - 2 && layout.getChildAt(index + 1).getId() == restRow.getId()) {

                    //decrease the set of that exercise
                    decreaseSuperset(index, layout);

                    //start rest countdown
                    restCountdownSecondsSuperset(position, layout);

                    //resets index to the first exercise
                    index = 0;
                    pref.edit().clear().apply();
                }

                //if rest row does not exist
                else if (layout.getChildAt(position - 1).getId() != restRow.getId()) {

                    //decrease set of that exercise
                    decreaseSuperset(index, layout);

                    /*
                    If current position is the last exercise, restart index back to 0
                    If current position is not the last exercise, increase index
                     */
                    if(index == position - 1){
                        index = 0;
                        pref.edit().clear().apply();
                    }
                    else {
                        index++;
                        pref.edit().putInt("index", index).apply();
                    }
                }
            }
        }

        /*
        If exercise you are currently on is a pyramidset, and if a rest timer has not been started and start has been clicked
         */
        else if(mContainerView.getChildAt(0).getId() == pyramidView.getId()){
            if(!startTimer && startClick) {

                View pyramidSetContainer = mContainerView.getChildAt(0);
                LinearLayout layout = (LinearLayout) pyramidSetContainer.findViewById(R.id.pyramidContainer);

                //removes set you are on
                decreasePyramidSet(layout);

                //if pyramid set has 2+ children and the last child is a rest row, start rest timer
                if(layout.getChildCount() >= 2) {
                    if (layout.getChildAt(layout.getChildCount() - 1).getId() == restRow.getId()) {

                        restCountdownSecondsPyramidSet(layout);
                    }
                }
            }
        }

        /*
        If current position is either exercise row or rest row and rest timer has not been started, and start has been clicked
         */
        else {
            if (!startTimer && startClick) {
                //index is reset
                index = 0;
                pref.edit().clear().apply();
                decreaseSet();
                restCountdownSeconds();
            }
        }
    }

    //method to round to nearest multiple of 5
    int round(int num) {
        int temp = num%5;
        if (temp<3)
            return num-temp;
        else
            return num+5-temp;
    }

    //method to convert percentage inputted to an int based on max inputted for that exercise
    public void convert1RMPercent(View v, maxModel maxModel){

        EditText weight = (EditText) v.findViewById(R.id.weight);
        AutoCompleteTextView exercise = (AutoCompleteTextView) v.findViewById(R.id.autoCompleteTextView);

        //if weight has a percentage
        if (weight.getText().toString().contains("%")) {

            String exerciseString = exercise.getText().toString();

            //gets the percent as a decimal
            String multiplier = weight.getText().toString().replace("%", "");
            double decimalMultiplier = Double.parseDouble(multiplier) / 100;

            //if the exercise is squat, multiply decimal by 1rm squat
            if(exerciseString.equalsIgnoreCase("Squat") || exerciseString.equalsIgnoreCase("Squats")){

                String squatMax = maxModel.getMaxSquat();

                //if a squat max is entered
                if(!squatMax.isEmpty()){

                    double squatMaxDouble = Double.parseDouble(squatMax);
                    double result = squatMaxDouble * decimalMultiplier;

                    //rounds the converted percent to nearest multiple of 5
                    //sets the weight as result
                    int roundedResult = round((int) result);
                    weight.setText(String.format("%s", roundedResult));
                }
            }

            //if the exercise is bench, multiply decimal by 1rm bench
            else if(exerciseString.equalsIgnoreCase("Bench")){

                String benchMax = maxModel.getMaxBench();

                //if a bench max is entered
                if(!benchMax.isEmpty()) {

                    double benchMaxDouble = Double.parseDouble(benchMax);
                    double result = benchMaxDouble * decimalMultiplier;

                    //rounds the converted percent to nearest multiple of 5
                    //sets the weight as result
                    int roundedResult = round((int) result);
                    weight.setText(String.format("%s", roundedResult));
                }
            }

            //if the exercise is deadlift, multiply decimal by 1rm deadlift
            else if(exerciseString.equalsIgnoreCase("deadlift") || exerciseString.equalsIgnoreCase("deadlifts")){

                String deadliftMax = maxModel.getMaxDeadlift();

                //if a deadlift max is entered
                if(!deadliftMax.isEmpty()) {

                    double deadliftMaxDouble = Double.parseDouble(deadliftMax);
                    double result = deadliftMaxDouble * decimalMultiplier;

                    //rounds the converted percent to nearest multiple of 5
                    //sets the weight as result
                    int roundedResult = round((int) result);
                    weight.setText(String.format("%s", roundedResult));
                }
            }

            //if the exercise is c&j, multiply decimal by 1rm c&j
            else if(exerciseString.equalsIgnoreCase("cj") || exerciseString.equalsIgnoreCase("c&j") || exerciseString.equalsIgnoreCase("clean and jerk") || exerciseString.equalsIgnoreCase("clean and jerks")){

                String cjMax = maxModel.getMaxCJ();

                //if a c&j max is entered
                if(!cjMax.isEmpty()) {

                    double cjMaxDouble = Double.parseDouble(cjMax);
                    double result = cjMaxDouble * decimalMultiplier;

                    //rounds the converted percent to nearest multiple of 5
                    //sets the weight as result
                    int roundedResult = round((int) result);
                    weight.setText(String.format("%s", roundedResult));
                }
            }

            //if the exercise is snatch, multiply decimal by 1rm snatch
            else if(exerciseString.equalsIgnoreCase("snatch") || exerciseString.equalsIgnoreCase("snatches")){
                String snatchMax = maxModel.getMaxSnatch();

                //if a snatch max is entered
                if(!snatchMax.isEmpty()) {
                    double snatchMaxDouble = Double.parseDouble(snatchMax);
                    double result = snatchMaxDouble * decimalMultiplier;

                    //rounds the converted percent to nearest multiple of 5
                    //sets the weight as result
                    int roundedResult = round((int) result);
                    weight.setText(String.format("%s", roundedResult));
                }
            }

            //if the exercise is powerclean, multiply decimal by 1rm powerclean
            else if(exerciseString.equalsIgnoreCase("power clean") || exerciseString.equalsIgnoreCase("power cleans")){

                String powerCleanMax = maxModel.getMaxPowerClean();

                //if a snatch max is entered
                if(!powerCleanMax.isEmpty()) {

                    double powerCleanMaxDouble = Double.parseDouble(powerCleanMax);
                    double result = powerCleanMaxDouble * decimalMultiplier;

                    //rounds the converted percent to nearest multiple of 5
                    //sets the weight as result
                    int roundedResult = round((int) result);
                    weight.setText(String.format("%s", roundedResult));
                }
            }
        }
    }

    //method to convert percent to number based off of 1rm for the pyramidset view
    public void convert1RMPercentPyramidView(View v, maxModel maxModel, AutoCompleteTextView exercise){
        EditText weight = (EditText) v.findViewById(R.id.weight);

        //if weight has a percentage
        if (weight.getText().toString().contains("%")) {
            String exerciseString = exercise.getText().toString();

            //gets the percent as a decimal
            String multiplier = weight.getText().toString().replace("%", "");
            double decimalMultiplier = Double.parseDouble(multiplier) / 100;

            //if the exercise is squat, multiply decimal by 1rm squat
            if(exerciseString.equalsIgnoreCase("Squat") || exerciseString.equalsIgnoreCase("Squats")){
                String squatMax = maxModel.getMaxSquat();

                //if a squat max is entered
                if(!squatMax.isEmpty()){
                    double squatMaxDouble = Double.parseDouble(squatMax);
                    double result = squatMaxDouble * decimalMultiplier;

                    //rounds the converted percent to nearest multiple of 5
                    //sets the weight as result
                    int roundedResult = round((int) result);
                    weight.setText(String.format("%s", roundedResult));
                }
            }

            //if the exercise is bench, multiply decimal by 1rm bench
            else if(exerciseString.equalsIgnoreCase("Bench")){
                String benchMax = maxModel.getMaxBench();

                //if a bench max is entered
                if(!benchMax.isEmpty()) {
                    double benchMaxDouble = Double.parseDouble(benchMax);
                    double result = benchMaxDouble * decimalMultiplier;

                    //rounds the converted percent to nearest multiple of 5
                    //sets the weight as result
                    int roundedResult = round((int) result);
                    weight.setText(String.format("%s", roundedResult));
                }
            }

            //if the exercise is deadlift, multiply decimal by 1rm deadlift
            else if(exerciseString.equalsIgnoreCase("deadlift") || exerciseString.equalsIgnoreCase("deadlifts")){
                String deadliftMax = maxModel.getMaxDeadlift();

                //if a deadlift max is entered
                if(!deadliftMax.isEmpty()) {
                    double deadliftMaxDouble = Double.parseDouble(deadliftMax);
                    double result = deadliftMaxDouble * decimalMultiplier;

                    //rounds the converted percent to nearest multiple of 5
                    //sets the weight as result
                    int roundedResult = round((int) result);
                    weight.setText(String.format("%s", roundedResult));
                }
            }

            //if the exercise is c&j, multiply decimal by 1rm c&j
            else if(exerciseString.equalsIgnoreCase("cj") || exerciseString.equalsIgnoreCase("c&j") || exerciseString.equalsIgnoreCase("clean and jerk") || exerciseString.equalsIgnoreCase("clean and jerks")){
                String cjMax = maxModel.getMaxCJ();

                //if a c&j max is entered
                if(!cjMax.isEmpty()) {
                    double cjMaxDouble = Double.parseDouble(cjMax);
                    double result = cjMaxDouble * decimalMultiplier;

                    //rounds the converted percent to nearest multiple of 5
                    //sets the weight as result
                    int roundedResult = round((int) result);
                    weight.setText(String.format("%s", roundedResult));
                }
            }

            //if the exercise is snatch, multiply decimal by 1rm snatch
            else if(exerciseString.equalsIgnoreCase("snatch") || exerciseString.equalsIgnoreCase("snatches")){
                String snatchMax = maxModel.getMaxSnatch();

                //if a snatch max is entered
                if(!snatchMax.isEmpty()) {
                    double snatchMaxDouble = Double.parseDouble(snatchMax);
                    double result = snatchMaxDouble * decimalMultiplier;

                    //rounds the converted percent to nearest multiple of 5
                    //sets the weight as result
                    int roundedResult = round((int) result);
                    weight.setText(String.format("%s", roundedResult));
                }
            }

            //if the exercise is powerclean, multiply decimal by 1rm powerclean
            else if(exerciseString.equalsIgnoreCase("power clean") || exerciseString.equalsIgnoreCase("power cleans")){
                String powerCleanMax = maxModel.getMaxPowerClean();

                //if a snatch max is entered
                if(!powerCleanMax.isEmpty()) {
                    double powerCleanMaxDouble = Double.parseDouble(powerCleanMax);
                    double result = powerCleanMaxDouble * decimalMultiplier;

                    //rounds the converted percent to nearest multiple of 5
                    //sets the weight as result
                    int roundedResult = round((int) result);
                    weight.setText(String.format("%s", roundedResult));
                }
            }
        }
    }


    //method to insert data into firebase
    //will either insert an exercise row
    //      if exercise, sets, and reps are not null
    //or will insert a rest row
    //      if minutes and seconds are not null
    public void insertDataToTable(String tableName, String viewType, String time, String exercise, String weight, String sets, String reps, String minutes, String seconds, String childCount) {
        ExerciseModel exerciseModel = new ExerciseModel();

        if(time != null){
            exerciseModel.setTime(time);
            String timeTable = tableName + "TimeTable";
            myRef.child("workouts").child(timeTable).setValue(exerciseModel);
        }

        else {
            exerciseModel.setView(viewType);
            exerciseModel.setChildCount(childCount);

            if (exercise != null && sets != null && reps != null) {
                exerciseModel.setExercise(exercise);
                exerciseModel.setWeight(weight);
                exerciseModel.setSets(sets);
                exerciseModel.setReps(reps);
            }

            if (minutes != null && seconds != null) {
                exerciseModel.setMinutes(minutes);
                exerciseModel.setSeconds(seconds);
            }

            DatabaseReference newRef = myRef.child("workouts").child(tableName).push();
            newRef.setValue(exerciseModel);
        }

    }



}
