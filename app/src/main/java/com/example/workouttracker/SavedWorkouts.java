package com.example.workouttracker;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static com.example.workouttracker.R.id.timeCompleted;
import static com.example.workouttracker.R.id.view;
import static com.example.workouttracker.R.id.workout;
import static com.example.workouttracker.R.id.workoutLoaderRelativeLayout;


public class SavedWorkouts extends Fragment {
    LinearLayout savedWorkoutContainer;
    onLoadWorkout onLoadWorkout;
    private TextView tableName;
    private TextView timeElapsed;



    //Calls loadWorkout from MainActivity whenever a relative layout is clicked
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments

        return inflater.inflate(R.layout.saved_workouts, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Saved Workouts");
        savedWorkoutContainer = (LinearLayout) getActivity().findViewById(R.id.savedWorkoutsContainer);
    }

    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);

        addViewWithText();

    }

    //Method to add a relative layout with corresponding workout name and time, if there is one
    public void addViewWithText(){
        final LayoutInflater layoutInflater = (LayoutInflater) getActivity().getLayoutInflater();
        FirebaseDatabase.getInstance().getReference().child("workoutTracker").child("workouts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ExerciseModel exerciseModel = snapshot.getValue(ExerciseModel.class);
                            String workoutName = snapshot.getKey();

                            if(tableName != null && timeElapsed != null) {
                                if (workoutName.equals(tableName.getText().toString() + "TimeTable")) {
                                    timeElapsed.setText(exerciseModel.getTime());
                                    tableName = null;
                                    timeElapsed = null;
                                }
                            }

                            else {
                                View v = layoutInflater.inflate(R.layout.saved_workout_display, null);
                                final RelativeLayout workoutLoaderRelativeLayout = (RelativeLayout) v.findViewById(R.id.workoutLoaderRelativeLayout);
                                ImageButton popup = (ImageButton) v.findViewById(R.id.popupMenu);
                                final TextView table = (TextView) v.findViewById(R.id.workout);
                                tableName = table;
                                timeElapsed = (TextView) v.findViewById(R.id.timeCompleted);

                                //Sets the workout name to the table name
                                table.setText(workoutName);

                                //Whenever a relative layout is clicked, call loadWorkout
                                workoutLoaderRelativeLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (getActivity() != null) {
                                            onLoadWorkout.loadWorkout(table.getText().toString());
                                        }
                                    }
                                });

                                popup.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        showPopup(view);
                                    }
                                });

                                //Relative layout is added to the SavedWorkouts fragment
                                savedWorkoutContainer.addView(v);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

    }


    //Called when the three dot menu is pressed
    //Displays the option to delete the workout
    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        View view = (View) v.getParent();
        final TextView workout = (TextView) view.findViewById(R.id.workout);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        deleteWorkout(item, workout.getText().toString());
                        return true;
                    default:
                        return false;
                }
            }
        });
        inflater.inflate(R.menu.context_menu, popup.getMenu());
        popup.show();
    }


    //Called when delete from the three dot menu is pressed
    public void deleteWorkout(MenuItem item, String workoutName) {
        Fragment savedWorkout = new SavedWorkouts();
        //Deletes the workout and the time table related to it
        if (item.getItemId() == R.id.delete) {
            FirebaseDatabase.getInstance().getReference().child("workoutTracker").child("workouts").child(workoutName).removeValue();
            FirebaseDatabase.getInstance().getReference().child("workoutTracker").child("workouts").child(workoutName + "TimeTable").removeValue();
        }
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        //Updates savedWorkout fragment
        ft.replace(R.id.content_frame, savedWorkout, "FragmentHome");
        ft.commit();
    }

}
