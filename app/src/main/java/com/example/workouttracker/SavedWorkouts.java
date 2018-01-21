package com.example.workouttracker;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;



public class SavedWorkouts extends Fragment{
    onLoadWorkout onLoadWorkout;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private MainActivity activity;
    private List<SavedWorkoutsModel> savedWorkoutsModelList = new ArrayList<>();
    public RecyclerView recyclerView;
    public String username;
    private SavedWorkoutsAdapter savedWorkoutsAdapter;
    private OnStartDragListener onStartDragListener;
    public ItemTouchHelper itemTouchHelper;


    //  Calls loadWorkout from MainActivity whenever a workout is clicked
    public interface onLoadWorkout{
        public void loadWorkout(String workoutName);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onLoadWorkout = (onLoadWorkout) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onLoadWorkout ");
        }
        if(context instanceof MainActivity){
            activity = (MainActivity) context;
        }
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.saved_workouts, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.savedWorkoutsRecyclerView);

    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        activity.setTitle("Saved Workouts");

        //  initialize firebase and recyclerview
        //  adds all the workouts to recyclerview
        initFirebase();
        initRecyclerView();
        addWorkoutToAdapter();

    }


    //  Method to add workout with corresponding time table to recycler view
    //  orders the workouts by orderNumber and loops through all the workouts saved
    public void addWorkoutToAdapter(){
        Query query =  myRef.child("workouts").orderByChild("orderNumber");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            savedWorkoutsModelList.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String workoutName = snapshot.getKey();
                                SavedWorkoutsModel savedWorkoutsModel = new SavedWorkoutsModel();

                                if (!workoutName.contains("TimeTable")){

                                    savedWorkoutsModel.setWorkoutName(workoutName);


                                    for(DataSnapshot snapshot1 : dataSnapshot.getChildren()){
                                        String possibleTimeTable = snapshot1.getKey();
                                        if (possibleTimeTable.equals(savedWorkoutsModel.getWorkoutName() + "TimeTable")) {
                                            WorkoutTime workoutTime = snapshot1.getValue(WorkoutTime.class);
                                            savedWorkoutsModel.setCompletedTime(workoutTime.getTime());
                                        }
                                    }

                                    savedWorkoutsModelList.add(savedWorkoutsModel);
                                    savedWorkoutsAdapter.notifyDataSetChanged();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

    }

    //  method to initialize firebase
    private void initFirebase(){
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if(user.getDisplayName() != null) {
                myRef = database.getReference(user.getDisplayName() + "WorkoutTracker");
                username = user.getDisplayName();
            }
        }
    }

    //  method to initialize recyclerview
    private void initRecyclerView(){
        onStartDragListener = new OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                itemTouchHelper.startDrag(viewHolder);
            }
        };
        savedWorkoutsAdapter = new SavedWorkoutsAdapter(savedWorkoutsModelList, activity, this, onStartDragListener);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);


        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(savedWorkoutsAdapter);
        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(savedWorkoutsAdapter, null);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }



}
