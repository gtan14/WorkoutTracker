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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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


import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;



public class Home extends Fragment {

    private LayoutInflater layoutInflater;
    public List<RowType> rowTypeList = new ArrayList<>();
    public RecyclerView recyclerView;
    private DisplayWorkoutAdapter displayWorkoutAdapter;
    private OnStartDragListener onStartDragListener;
    MainActivity activity;
    private boolean loadWorkout;
    public Boolean titleSet;
    View activityNav;
    TabLayout tabLayout;
    Drawable weightDrawable;
    Drawable exDrawable;
    Drawable setDrawable;
    Drawable repDrawable;
    Drawable minDrawable;
    Drawable secDrawable;
    Intent serviceIntent;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private boolean FABClicked;
    public FrameLayout frameLayout;
    private final String TAG = "FIREBASE";
    public LinearLayoutManager layoutManager;
    private String loadedTableName;
    public FloatingActionButton newWorkoutFAB;
    private Home home;
    ItemTouchHelper touchHelper;


    //  Assigns activity to MainActivity context once Home is attached
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        home = this;
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
    public void onStop(){
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

        SharedPreferences sharedPreferences1 = activity.getSharedPreferences("startClicked", Context.MODE_PRIVATE);
        SharedPreferences sharedPreferences = activity.getSharedPreferences("autoSave", Context.MODE_PRIVATE);
        boolean resume = sharedPreferences1.getBoolean("click", false);
        displayWorkoutAdapter.startTimer = false;


        //  called when stop button is clicked
        //  prevents the workout from being auto saved, which caused it to crash
        if(loadWorkout){
            SharedPreferences autoSave = activity.getSharedPreferences("autoSave", Context.MODE_PRIVATE);
            autoSave.edit().clear().apply();
        }
        final String autoPauseTableName = sharedPreferences.getString("autoPause", "");

        //  if workout is being resumed, initialize content of recyclerview to data of node called workoutPause
        if(resume) {
            rowTypeList.clear();
            String pausedWorkoutName = "workoutPause";
            displayWorkoutAdapter.setDataForLoadedWorkout(pausedWorkoutName, displayWorkoutAdapter, true);
            sharedPreferences1.edit().clear().apply();
        }

        //  workout is not being resumed (user is navigating here to edit or view workout), clear all shared prefs that are related to a started workout
        //  includes all the shared prefs created from rest countdown service and the swipe index counter
        else{
            SharedPreferences countdownMin = activity.getSharedPreferences("countdownMin", Context.MODE_PRIVATE);
            SharedPreferences minPref = activity.getSharedPreferences("min", Context.MODE_PRIVATE);
            SharedPreferences secPref = activity.getSharedPreferences("sec", Context.MODE_PRIVATE);
            SharedPreferences swipe = home.activity.getSharedPreferences("swipeIndex", Context.MODE_PRIVATE);
            SharedPreferences inputErrorSharedPref = activity.getSharedPreferences("inputError", Context.MODE_PRIVATE);
            if(inputErrorSharedPref.getBoolean("error", false)){
                displayWorkoutAdapter.inputError = true;
            }
            inputErrorSharedPref.edit().clear().apply();
            swipe.edit().clear().apply();
            countdownMin.edit().clear().apply();
            minPref.edit().clear().apply();
            secPref.edit().clear().apply();
        }

        //  if workout is being auto saved, check to see if a node of that workout exists
        //  if it exists, setup display adapter
        if(!autoPauseTableName.equals("")){
            myRef.child("workouts").child(autoPauseTableName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        activity.setTitle(autoPauseTableName);
                        int sizeOfList = rowTypeList.size();
                        rowTypeList.clear();
                        displayWorkoutAdapter.notifyItemRangeRemoved(0, sizeOfList);
                        titleSet = true;
                        displayWorkoutAdapter.setDataForLoadedWorkout(autoPauseTableName, displayWorkoutAdapter, false);
                    }

                    //  This will be called if there is a workout being auto saved, and that workout is deleted
                    //  Reset fragment back to normal
                    else{
                        SharedPreferences autoSave = activity.getSharedPreferences("autoSave", Context.MODE_PRIVATE);
                        autoSave.edit().clear().apply();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

    }


    @Override
    public void onPause(){
        super.onPause();


        final SaveChanges saveChanges = new SaveChanges(this, displayWorkoutAdapter);

        //  if a rest countdown is in progress, unregister the receiver for that countdown
        try {
            activity.unregisterReceiver(displayWorkoutAdapter.restCountDown.secondReceiver);
        }
        catch (IllegalArgumentException e){

        }

        //  if workout is in progress, save workout to a node called workoutPause
        if(displayWorkoutAdapter.startClicked) {

            String workoutNameOnPause = activity.getTitle().toString();
            String pauseTableName = "workoutPause";
            SharedPreferences sharedPreferences = activity.getSharedPreferences("workoutNameOnPause", Context.MODE_PRIVATE);
            SharedPreferences sharedPreferences1 = activity.getSharedPreferences("startClicked", Context.MODE_PRIVATE);
            sharedPreferences.edit().putString("name", workoutNameOnPause).apply();
            sharedPreferences1.edit().putBoolean("click", true).apply();
            saveChanges.saveToDB(pauseTableName, displayWorkoutAdapter.rowTypeList, true, null, null, null);
        }

        //  auto save workout if a workout is present
        else{
            final String homeTitle = activity.getTitle().toString();

            if(!displayWorkoutAdapter.stopClicked) {
                if (!homeTitle.equals("Home")) {
                    SharedPreferences autoSave = activity.getSharedPreferences("autoSave", Context.MODE_PRIVATE);
                    if (!FABClicked) {
                        autoSave.edit().putString("autoPause", homeTitle).apply();
                    }
                    //  if the FAB is clicked, clear the shared pref in charge of auto saving workout
                    else {
                        autoSave.edit().clear().apply();
                    }
                    saveChanges.saveToDB(homeTitle, displayWorkoutAdapter.rowTypeList, false, null, null, null);
                }
            }
        }

        if(displayWorkoutAdapter.inputError){
            SharedPreferences sharedPreferences = activity.getSharedPreferences("inputError", Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean("error", true).apply();
        }

    }


    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        layoutInflater = activity.getLayoutInflater();

        //  view initialization
        activityNav = layoutInflater.inflate(R.layout.app_bar_nav_drawer, null);
        tabLayout = (TabLayout) activityNav.findViewById(R.id.tabs);
        recyclerView = v.findViewById(R.id.rowContainerRecyclerView);
        newWorkoutFAB = (FloatingActionButton) v.findViewById(R.id.newWorkoutFAB);
        frameLayout = (FrameLayout) v.findViewById(R.id.homeFrameLayout);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);

        SharedPreferences addWorkoutFAB = activity.getSharedPreferences("newWorkoutFAB", Context.MODE_PRIVATE);
        boolean newWorkoutFABClicked = addWorkoutFAB.getBoolean("addWorkout", false);

        //  initialize firebase and set up display adapter
        firebaseInit();
        setRecyclerView();

        //  hides the FAB when a keyboard appears
        //  shows the FAB when it is hidden, and a workout has not been started
        frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(activity != null) {
                    int heightDiff = frameLayout.getRootView().getHeight() - frameLayout.getHeight();
                    if (heightDiff > dpToPx(activity, 200)) {
                        newWorkoutFAB.setVisibility(View.INVISIBLE);
                    }
                    else if(!displayWorkoutAdapter.startClicked){
                        newWorkoutFAB.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        serviceIntent = new Intent(activity, CountdownService.class);

        //  if workout is being loaded, set up display adapter with data from the node of the value of string loadedTableName
        if(loadWorkout){
            SharedPreferences autoSave = activity.getSharedPreferences("autoSave", Context.MODE_PRIVATE);
            autoSave.edit().clear().apply();
            activity.setTitle(loadedTableName);
            titleSet = true;
            displayWorkoutAdapter.setDataForLoadedWorkout(loadedTableName, displayWorkoutAdapter, false);
        }

        //  if workout is not being loaded, set up display adapter with just the buttons
        else{
            titleSet = false;
            ButtonsHolder buttonsHolder = new ButtonsHolder();
            rowTypeList.add(buttonsHolder);
            displayWorkoutAdapter.notifyItemInserted(0);
        }


        //  if workout is not being loaded or resumed, set title to Home
        //  if workout is being resumed, set title to the workout in progress
        if(!titleSet) {

            SharedPreferences pause = activity.getSharedPreferences("workoutNameOnPause", Context.MODE_PRIVATE);
            SharedPreferences sharedPreferences1 = activity.getSharedPreferences("startClicked", Context.MODE_PRIVATE);
            String workoutName = pause.getString("name", "");
            boolean start = sharedPreferences1.getBoolean("click", false);

            if(!workoutName.isEmpty() && start) {
                activity.setTitle(workoutName);
                pause.edit().clear().apply();
                titleSet = true;
            }

            else {
                activity.setTitle("Home");
            }
        }

        //  show popup dialog once FAB is clicked and the Home fragment is loaded
        if(newWorkoutFABClicked){
            addWorkoutFAB.edit().clear().apply();
            SaveChanges saveChanges = new SaveChanges(this, displayWorkoutAdapter);
            saveChanges.setActivityTitle();
        }



        //  on click FAB, replace fragment with new Home fragment
        newWorkoutFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = activity.getSharedPreferences("newWorkoutFAB", Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean("addWorkout", true).apply();
                FABClicked = true;

                Fragment home = new Home();
                FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, home, "FragmentHome");
                ft.commit();
            }
        });
    }


    //  Method is called when workout is loaded
    public void booleanLoadWorkout(String loadedTableName){
        loadWorkout = true;
        this.loadedTableName = loadedTableName;
    }

    //  method to initialize firebase
    private void firebaseInit(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            if(user.getDisplayName() != null) {
                myRef = database.getReference(user.getDisplayName() + "WorkoutTracker");
            }
        }

    }

    //  sets up the recyclerview
    //  recycler view is in reverse order (top is size - 1 and bottom is 0)
    private void setRecyclerView(){
        layoutManager = new LinearLayoutManager(activity);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        onStartDragListener = new OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                touchHelper.startDrag(viewHolder);
            }
        };
        displayWorkoutAdapter = new DisplayWorkoutAdapter(rowTypeList, Home.this, onStartDragListener);


        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(displayWorkoutAdapter);
        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(displayWorkoutAdapter, displayWorkoutAdapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    //  method to convert dp to px
    //  this is used to determine whether a keyboard is shown or not
    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

}

