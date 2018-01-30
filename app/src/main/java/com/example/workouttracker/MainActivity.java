package com.example.workouttracker;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.method.KeyListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.jar.Attributes;

import static android.R.attr.id;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;


public class MainActivity extends navDrawer implements SavedWorkouts.onLoadWorkout{
    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private String username;
    private boolean receiverRegisered;


    //  Method that displays the home fragment when a workout is loaded
    //  current fragment is replaced with home fragment
    public void loadWorkout(String workoutName){
        Home home = new Home();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, home, "home");
        ft.commit();
        home.booleanLoadWorkout(workoutName);
    }

    @Override
    protected void onResume(){
        super.onResume();

        //  if receiver is not registered yet, register it
        //  this prevents 2 alert dialogs from appearing if a workout is sent
        if(!receiverRegisered) {
            LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver), new IntentFilter("workoutReceived"));
        }

        //  gets the shared pref that is being used to store the sender id and workout name when a user sends a workout
        SharedPreferences sharedPreferences = getSharedPreferences("workoutReceived", Context.MODE_PRIVATE);
        String senderId = sharedPreferences.getString("senderId", "");
        String workoutName = sharedPreferences.getString("workoutName", "");
        if(!senderId.isEmpty() && !workoutName.isEmpty()){
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
            Intent intent = new Intent("workoutReceived");
            intent.putExtra("sender", senderId);
            intent.putExtra("workoutName", workoutName);

            //  Sends broadcast to MainActivity with the sender's username
            broadcastManager.sendBroadcast(intent);
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onStop(){
        super.onStop();

        //  flag that shows if receiver is registered or not
        receiverRegisered = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    //  Receiver that is used in FirebaseMessagingService when user sends another user a workout
    //  Displays alert dialog with sender username, asking if user wants to accept or decline sent workout
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null){
                final String sender = intent.getStringExtra("sender");
                final String workoutName = intent.getStringExtra("workoutName");

                //  clears the shared pref that is used to store the sender id and workout name
                //  necessary so that alert dialog does not keep showing up saying that a workout has been sent
                SharedPreferences sharedPreferences = getSharedPreferences("workoutReceived", Context.MODE_PRIVATE);
                sharedPreferences.edit().clear().apply();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                String message = "Workout sent from " + sender;
                builder.setMessage(message);
                builder.setCancelable(false);
                builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteTempSentWorkoutNode(sender, username, workoutName, false);
                        dialogInterface.dismiss();
                    }
                });
                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteTempSentWorkoutNode(sender, username, workoutName, true);
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver), new IntentFilter("workoutReceived"));
        receiverRegisered = true;

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            if(user.getDisplayName() != null) {
                myRef = database.getReference(user.getDisplayName() + "WorkoutTracker");
                username = user.getDisplayName();
            }
        }

        sendRegistrationToServer();
        //  removes pause workout if there is one
        myRef.child("workoutPause").removeValue();
    }


    //  if activity is destroyed, remove pause workout and any shared prefs needed to be deleted to reset app to normal
    @Override
    public void onDestroy(){
        super.onDestroy();

        myRef.child("workoutPause").removeValue();
        SharedPreferences countdownMin = getSharedPreferences("countdownMin", Context.MODE_PRIVATE);
        SharedPreferences minPref = getSharedPreferences("min", Context.MODE_PRIVATE);
        SharedPreferences secPref = getSharedPreferences("sec", Context.MODE_PRIVATE);
        countdownMin.edit().clear().apply();
        minPref.edit().clear().apply();
        secPref.edit().clear().apply();
        SharedPreferences sharedPreferences = getSharedPreferences("startClicked", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
        SharedPreferences pref = getSharedPreferences("swipeIndex", Context.MODE_PRIVATE);
        pref.edit().clear().apply();
        SharedPreferences sharedPreferences1 = getSharedPreferences("workoutNameOnPause", Context.MODE_PRIVATE);
        sharedPreferences1.edit().clear().apply();
    }

    //  method that will set the token of a device to a specific user
    //  required in order for users to send workouts to one another
    private void sendRegistrationToServer() {

        SharedPreferences sharedPreferences = getSharedPreferences("token", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("tokenID", "");
        FirebaseUser user = mAuth.getCurrentUser();
        String username = "";
        if (user != null) {
            username = user.getDisplayName();
        }

        if(username != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("token");
            reference.child(username).setValue(token);
        }
    }

    //  method that is called when a user either accepts or declines a sent workout
    //  if the user accepts, the receiver's workouts are checked to see if there is the same workout name that exists
    //  if it does exist, user is asked to create new name
    //  the workout that is saved in a temp node by the sender, is transferred to the receivers profile to the new name,
    //  then the workout in the temp node is deleted
    //  if it doesn't exist, workout is transferred and placed into the node with original workout name
    //  if the user declines, the workout in the temp node is deleted
    private void deleteTempSentWorkoutNode(String sender, String receiver, String workoutName, boolean accept){

        //  checks to see if the sender is null or not
        //  having a null sender crashed the app, so a check is necessary to prevent crashes from this
        if(sender != null) {

            String childName = sender + "To" + receiver;

            final DatabaseReference fromPath = database.getReference("shareWorkout").child(childName).child(workoutName);
            final DatabaseReference toPath = myRef.child("workouts").child(workoutName).child("list");
            final DatabaseReference possibleExistingWorkout = myRef.child("workouts").child(workoutName);

            if (accept) {

                possibleExistingWorkout.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        possibleExistingWorkout.removeEventListener(this);

                        //  if workout name already exists, ask user for new name
                        if (dataSnapshot.exists()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            final EditText editText = new EditText(MainActivity.this);
                            builder.setMessage("Workout name already exists. Create new name");
                            builder.setView(editText);
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });

                            //  once user presses ok on new name, it is checked to see if it exists already
                            //  if not, the workout from the temp node is transferred to a new node with the new name
                            //  setOnShowListener is used to prevent the dialogInterface from dismissing if workout name shows as exists after user clicks ok
                            builder.setPositiveButton("OK", null);
                            final AlertDialog alertDialog = builder.create();
                            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(final DialogInterface dialogInterface) {
                                    Button positive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    positive.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            myRef.child("workouts").child(editText.getText().toString()).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    myRef.child("workouts").child(editText.getText().toString()).removeEventListener(this);
                                                    if (dataSnapshot.exists()) {
                                                        Toast.makeText(MainActivity.this, "Name exists", Toast.LENGTH_LONG).show();
                                                    } else {
                                                        dialogInterface.dismiss();
                                                        DatabaseReference newPath = myRef.child("workouts").child(editText.getText().toString()).child("list");
                                                        transferWorkoutFromTempNode(fromPath, newPath);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    });
                                }
                            });
                            alertDialog.show();

                        } else {
                            transferWorkoutFromTempNode(fromPath, toPath);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else {
                fromPath.removeValue();
            }
        }

        else{
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
        }

    }

    //  method that transfers the workout from the temp node to a node in the receiver's workout
    //  the temp node is deleted after the workout is transferred
    private void transferWorkoutFromTempNode(final DatabaseReference fromPath, final DatabaseReference toPath){
        fromPath.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(snapshot.getKey().equals("workout")) {
                        toPath.setValue(snapshot.getValue(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                                if (firebaseError != null) {
                                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG).show();
                                    fromPath.removeValue();
                                }
                            }
                        });
                    }
                }
                fromPath.removeEventListener(this);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
