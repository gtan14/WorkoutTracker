package com.example.workouttracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Gerald on 11/7/2017.
 */

public class SavedWorkoutsAdapter extends RecyclerView.Adapter<SavedWorkoutsAdapter.MyViewHolder> implements ItemTouchHelperAdapter{

    private List<SavedWorkoutsModel> savedWorkoutsModelList;
    private MainActivity mainActivity;
    private SavedWorkouts sw;
    private final OnStartDragListener mDragStartListener;
    private List<RowType> rowType;
    private ShareWorkout shareWorkout;

    public class MyViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder{
        public TextView workoutName;
        public TextView completedTime;
        public ConstraintLayout constraintLayout;
        public ConstraintLayout nestedConstraintLayout;
        public ImageButton popupMenu;
        public ImageView dragHandle;
        //private View view;

        public MyViewHolder(View view){
            super(view);

            workoutName = (TextView) view.findViewById(R.id.workout);
            completedTime = (TextView) view.findViewById(R.id.timeCompleted);
            nestedConstraintLayout = (ConstraintLayout) view.findViewById(R.id.savedWorkoutNestedConstraint);
            constraintLayout = (ConstraintLayout) view.findViewById(R.id.savedWorkoutDisplayConstraintLayout);
            popupMenu = (ImageButton) view.findViewById(R.id.popupMenu);
            dragHandle = (ImageView) view.findViewById(R.id.savedWorkoutDragHandle);
        }

        @Override
        public void onItemSelected() {
            //nestedConstraintLayout.getBackground().setAlpha(127);
        }

        @Override
        public void onItemClear() {
            //nestedConstraintLayout.getBackground().setAlpha(255);
        }

    }



    @Override
    public void nextSet(){

    }

    @Override
    public void onItemDismiss(RecyclerView.ViewHolder viewHolder, final int position) {

        final SavedWorkoutsModel savedWorkoutsModel = savedWorkoutsModelList.get(position);
        final String workoutToDelete = savedWorkoutsModel.getWorkoutName();
        final String workoutTable = mainActivity.myRef.child("savedWorkoutTempDelete").push().getKey();
        final String timeTable = mainActivity.myRef.child("savedWorkoutTempDelete").push().getKey();

        //  moves data from workout that is deleted to a temp node for both the workout and time table
        moveData(mainActivity.myRef.child("workouts").child(workoutToDelete), mainActivity.myRef.child("savedWorkoutTempDelete").child(workoutTable));
        moveData(mainActivity.myRef.child("workouts").child(workoutToDelete + "TimeTable"), mainActivity.myRef.child("savedWorkoutTempDelete").child(timeTable));

        savedWorkoutsModelList.remove(position);
        notifyItemRemoved(position);

        //  display an undo option
        //  if undo is clicked, move the data from temp node to original workout node
        //  if undo option is dismissed, remove the temp node containing the workout data
        Snackbar snackbar = Snackbar
                .make(sw.recyclerView, "Workout deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        savedWorkoutsModelList.add(position, savedWorkoutsModel);
                        notifyItemInserted(position);
                        moveData(mainActivity.myRef.child("savedWorkoutTempDelete").child(workoutTable), mainActivity.myRef.child("workouts").child(workoutToDelete));
                        moveData(mainActivity.myRef.child("savedWorkoutTempDelete").child(timeTable), mainActivity.myRef.child("workouts").child(workoutToDelete + "TimeTable"));
                    }
                });
        snackbar.addCallback(new Snackbar.Callback() {

            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                mainActivity.myRef.child("savedWorkoutTempDelete").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            mainActivity.myRef.child("savedWorkoutTempDelete").child(workoutTable).removeValue();
                            mainActivity.myRef.child("savedWorkoutTempDelete").child(timeTable).removeValue();
                        }
                        mainActivity.myRef.child("savedWorkoutTempDelete").removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
            }

            @Override
            public void onShown(Snackbar snackbar) {

            }
        });
        snackbar.show();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        SavedWorkoutsModel savedWorkoutsModelFromPosition = savedWorkoutsModelList.get(fromPosition);
        SavedWorkoutsModel savedWorkoutsModelToPosition = savedWorkoutsModelList.get(toPosition);

        String fromPositionTableName = savedWorkoutsModelFromPosition.getWorkoutName();
        String toPositionTableName = savedWorkoutsModelToPosition.getWorkoutName();

        //  swaps order number for 2 workouts
        //  workouts are displayed based on order number
        DatabaseReference fromPositionRef = mainActivity.myRef.child("workouts").child(fromPositionTableName).child("orderNumber");
        DatabaseReference toPositionRef = mainActivity.myRef.child("workouts").child(toPositionTableName).child("orderNumber");

        fromPositionRef.setValue(toPosition);
        toPositionRef.setValue(fromPosition);

        Collections.swap(savedWorkoutsModelList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);


        return true;
    }

    public SavedWorkoutsAdapter (List<SavedWorkoutsModel> savedWorkoutsModelList, MainActivity mainActivity, SavedWorkouts savedWorkouts, OnStartDragListener onStartDragListener){
        this.savedWorkoutsModelList = savedWorkoutsModelList;
        this.mainActivity = mainActivity;
        this.sw = savedWorkouts;
        mDragStartListener = onStartDragListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_workout_display, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position){
        final int pos = holder.getAdapterPosition();
        SavedWorkoutsModel savedWorkoutsModel = savedWorkoutsModelList.get(pos);
        holder.workoutName.setText(savedWorkoutsModel.getWorkoutName());
        holder.completedTime.setText(savedWorkoutsModel.getCompletedTime());

        sw.myRef.child("workouts").child(holder.workoutName.getText().toString()).child("orderNumber").setValue(pos);

        //  if workout is clicked, and a workout is not in progress, load that corresponding workout
        //  if workout is in progress, display toast
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw.getActivity() != null) {
                    sw.myRef.child("workoutPause")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.exists()){
                                        sw.onLoadWorkout.loadWorkout(holder.workoutName.getText().toString());
                                    }

                                    else{
                                        Toast.makeText(mainActivity, "Stop workout first", Toast.LENGTH_LONG).show();
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                }
            }
        });

        holder.popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view, pos);
            }
        });

        //  Start a drag whenever the handle view it touched
        holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }

                return false;
            }
        });
    }

    @Override
    public int getItemCount(){
        return savedWorkoutsModelList.size();
    }

    //  method to clear out recycler view items
    public void clear(){
        int size = this.savedWorkoutsModelList.size();
        if(size > 0){
            for(int i = 0; i < size; i++){
                this.savedWorkoutsModelList.remove(0);
            }
            this.notifyItemRangeRemoved(0, size);
        }
    }

    //  Called when overflow menu is clicked
    //  gives user option to delete all workouts, or share corresponding workout
    public void showPopup(View v, final int pos) {
        PopupMenu popup = new PopupMenu(mainActivity, v);
        MenuInflater inflater = popup.getMenuInflater();
        View view = (View) v.getParent();
        final TextView workout = (TextView) view.findViewById(R.id.workout);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.deleteAll:
                        deleteAllWorkouts(item);
                        return true;
                    case R.id.shareWorkout:
                        shareWorkout(workout.getText().toString());
                        return true;
                    case R.id.resetTime:
                        resetTime(workout.getText().toString(), pos);
                        return true;
                    default:
                        return false;
                }
            }
        });
        inflater.inflate(R.menu.context_menu, popup.getMenu());
        popup.show();
    }

    //  called when share is clicked from the overflow menu
    //  creates an alert dialog, asking for the receiver username
    //  if receiver username is same as current user username, display toast error
    //  if receiver username does not exist, display toast error
    //  if receiver username does exist, and no error, call retrieveWorkout
    public void shareWorkout(final String workout){
        if(sw.getContext() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(sw.getContext());
            builder.setTitle("Share");

            final EditText input = new EditText(sw.getContext());
            builder.setView(input);
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    final String receiver = input.getText().toString();
                    final DatabaseReference reference = sw.database.getReference("token");

                    //  checks to see if user exists
                    reference.child(receiver).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(sw.username.equals(input.getText().toString())){
                                Toast.makeText(sw.getContext(), "Can't send workout to self", Toast.LENGTH_LONG).show();
                            }

                            else if(dataSnapshot.exists() && !sw.username.equals(input.getText().toString())){
                                String receiverToken = (String) dataSnapshot.getValue();
                                retrieveWorkout(receiverToken, workout, receiver);
                            }

                            else{
                                Toast.makeText(sw.getContext(), "User doesn't exist", Toast.LENGTH_LONG).show();
                            }

                            reference.child(receiver).removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });
            builder.show();
        }
    }

    private void resetTime(final String workoutName, final int pos){
        if(sw.getContext() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(sw.getContext());
            builder.setMessage("Time for this workout will be deleted");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    final String tableName = workoutName + "TimeTable";
                    savedWorkoutsModelList.get(pos).setCompletedTime(null);
                    sw.myRef.child("workouts").child(tableName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                notifyItemChanged(pos);
                            }
                            sw.myRef.child("workouts").child(tableName).removeValue();
                        }
                    });
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        }
    }

    //  called once a user sends a workout to another user
    //  creates a temp node with the receiver's token id, the workout name being sent, the sender, and the actual workout
    //  name of the node is under sharedWorkout/{senderToReceiver}
    private void retrieveWorkout(String receiverToken, final String workout, final String receiverName){
        rowType = new ArrayList<>();
        shareWorkout = new ShareWorkout();
        shareWorkout.setToken(receiverToken);
        shareWorkout.setWorkoutName(workout);
        shareWorkout.setId(sw.username);
        sw.myRef.child("workouts").child(workout).child("list").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    String viewType = (String) s.child("view").getValue();
                    if (viewType != null) {
                        if (viewType.equals("Exercise row")) {
                            ExerciseModel exerciseModel = s.getValue(ExerciseModel.class);
                            rowType.add(exerciseModel);
                        } else if (viewType.equals("Rest row")) {
                            RestRowModel restRowModel = s.getValue(RestRowModel.class);
                            rowType.add(restRowModel);
                        } else if (viewType.equals("Superset container")) {
                            SupersetRowModel supersetRowModel = s.getValue(SupersetRowModel.class);
                            rowType.add(supersetRowModel);
                        } else if (viewType.equals("Pyramidset container")) {
                            PyramidsetRowModel pyramidsetRowModel = s.getValue(PyramidsetRowModel.class);
                            rowType.add(pyramidsetRowModel);
                        }
                    }
                }
                sw.myRef.child("workouts").child(workout).child("list").removeEventListener(this);
                shareWorkout.setWorkout(rowType);

                //  creates a node that specifies the sender and receiver
                //  under that node, another node is created specifying the workout name
                //  this is where the workout is temporarily stored for the receiver, until the receiver declines or accepts
                //  username.replace is used just in case the username contains '.'. This will crash the app since Firebase does not accept '.' in a node name
                //  removeValue() is used to delete any existing node that is there
                //  this could be caused by a previous sent workout, and the retriever was not able to decline or accept, thus the node is still existing
                String childName = sw.username.replace(".", ",") + "To" + receiverName;
                sw.database.getReference("shareWorkout").child(childName).child(workout).removeValue();
                sw.database.getReference("shareWorkout").child(childName).child(workout).setValue(shareWorkout);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //  Called when delete all from the overflow menu is pressed
    public void deleteAllWorkouts(final MenuItem item) {

        //  displays alert dialog, asking if user wants to delete all workouts
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setMessage("All workouts will be deleted")
                .setTitle("Delete")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //  Deletes the workout and the time table related to it
                        if (item.getItemId() == R.id.deleteAll) {
                            mainActivity.myRef.child("workouts").removeValue();
                            clear();
                        }
                    }
                })
                .show();

    }

    //  method to move data from one path to another
    private void moveData(final DatabaseReference fromPath, final DatabaseReference toPath) {
        fromPath.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                toPath.setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                        if (firebaseError != null) {
                            System.out.println("Copy failed");
                        } else {
                            System.out.println("Success");
                            fromPath.removeValue();
                        }
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
