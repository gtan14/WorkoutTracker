package com.example.workouttracker;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.List;

/**
 * Created by Gerald on 1/2/2018.
 */

public class MaxAdapter extends RecyclerView.Adapter<MaxAdapter.MaxViewHolder> implements ItemTouchHelperAdapter{

    public List<maxModel> maxModelList;
    private Profile profile;
    private boolean updateTextWatcher;

    public class MaxViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder{
        public EditText max;
        public AutoCompleteTextView exercise;
        public MaxRowTextWatcher maxRowTextWatcher;

        public MaxViewHolder(View view, MaxAdapter maxAdapter){
            super(view);

            exercise = view.findViewById(R.id.exerciseMaxEditText);
            max = view.findViewById(R.id.maxForExEditText);

            maxRowTextWatcher = new MaxRowTextWatcher(maxAdapter, exercise, max);
        }

        @Override
        public void onItemSelected() {
            //itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            //itemView.setBackgroundColor(0);
        }
    }

    @Override
    public void nextSet(){

    }

    @Override
    public void onItemDismiss(RecyclerView.ViewHolder viewHolder, final int position) {

        final maxModel maxModel = maxModelList.get(position);
        final String tempKey = profile.myRef.child("maxTempDelete").push().getKey();

        //  moves data from max node to a temp node
        moveData(profile.myRef.child("max").child(String.format("%s", position)), profile.myRef.child("maxTempDelete").child(tempKey));


        //  removes corresponding viewholder from adapter, and updates existing viewholders with new position for text watcher
        maxModelList.remove(position);
        notifyItemRemoved(position);
        //notifyItemRangeChanged(0, getItemCount());
        updatePosition();

        //  create snackbar, that acts as an undo action
        //  if undo is clicked, move the data from the temp node to the original node
        //  if snackbar is dismissed, delete the data in the temp node
        Snackbar snackbar = Snackbar
                .make(profile.maxContainer, "Max deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateTextWatcher = true;
                        maxModelList.add(position, maxModel);
                        notifyItemInserted(position);
                        moveData(profile.myRef.child("maxTempDelete").child(tempKey), profile.myRef.child("max").child(String.format("%s", position)));
                    }
                });

        snackbar.addCallback(new Snackbar.Callback() {

            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                profile.myRef.child("maxTempDelete").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            profile.myRef.child("maxTempDelete").removeValue();
                        }
                        profile.myRef.child("maxTempDelete").removeEventListener(this);
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

        return false;
    }

    public MaxAdapter (List<maxModel> maxModelList, Profile profile){
        this.maxModelList = maxModelList;
        this.profile = profile;
    }

    @Override
    public MaxAdapter.MaxViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.max, parent, false);
        return new MaxAdapter.MaxViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(final MaxAdapter.MaxViewHolder holder, int position){

        int pos = holder.getAdapterPosition();


        //MaxRowTextWatcher maxRowTextWatcher = new MaxRowTextWatcher(this, holder.exercise, holder.max);
        holder.maxRowTextWatcher.updatePosition(pos);


        maxModel maxModel = maxModelList.get(pos);
        holder.exercise.setText(maxModel.getExercise());
        holder.max.setText(maxModel.getMax());

        (maxModelList.get(pos)).setMaxRowTextWatcher(holder.maxRowTextWatcher);


        if(updateTextWatcher){
            updatePosition();
            updateTextWatcher = false;
        }

        //  displays auto complete for exercises
        if(profile.getContext() != null) {
            holder.exercise.setAdapter(getExerciseAdapter(profile.getContext()));
        }
    }

    @Override
    public int getItemCount(){
        return maxModelList.size();
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

    //  exercise adapter for the exercise autoComplete
    //  displays suggested options of exercises
    private ArrayAdapter<String> getExerciseAdapter(Context context){
        String[] exercise = new String[]{};
        if(context != null) {
            exercise = context.getResources().getStringArray(R.array.exercises);
        }
        return new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, exercise);
    }

    //  updates position of textwatcher
    //  this is necessary, in case a viewholder is deleted or added, and the position changes
    public void updatePosition(){
        for(int i = 0; i <= getItemCount() - 1; i++){
            maxModelList.get(i).getMaxRowTextWatcher().updatePosition(i);
        }
    }

    public void updatePosition(int position, MaxRowTextWatcher maxRowTextWatcher){
        maxRowTextWatcher.updatePosition(position);
    }
}