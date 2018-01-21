package com.example.workouttracker;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Gerald on 1/2/2018.
 */

public class Profile extends Fragment {
    MainActivity activity;
    RecyclerView maxContainer;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private MaxAdapter maxAdapter;
    public ArrayList<maxModel> maxModelArrayList;
    private LinearLayoutManager layoutManager;
    private TextView username;
    private FrameLayout frameLayout;
    //private FloatingActionsMenu floatingActionsMenu;
    private FloatingActionButton addMax;
    //private FloatingActionButton addFriend;
    LayoutInflater layoutInflater;


    //  Assigns activity to MainActivity context once Home is attached
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof MainActivity){
            activity = (MainActivity) context;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returns layout file
        View v= inflater.inflate(R.layout.profile, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {

        //  View initialization
        maxContainer = (RecyclerView) v.findViewById(R.id.addMaxContainer);
        //floatingActionsMenu = (FloatingActionsMenu) v.findViewById(R.id.floatingActionButtonMenu);
        addMax = (FloatingActionButton) v.findViewById(R.id.addMaxFAB);
        //addFriend = (FloatingActionButton) v.findViewById(R.id.addFriendFAB);
        frameLayout = (FrameLayout) v.findViewById(R.id.profileFrameLayout);
        username = (TextView) v.findViewById(R.id.username);
        mAuth = FirebaseAuth.getInstance();
        layoutInflater = getLayoutInflater();

    }

    @Override
    public void onPause(){
        super.onPause();

        //  auto saves max data to max node in Firebase
        myRef.child("max").setValue(maxModelArrayList);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        //  initialize Firebase and set the username for the user to the text view
        initializeFirebase();
        activity.setTitle("Profile");

        maxModelArrayList = new ArrayList<>();

        //addMax.setSize(FloatingActionButton.SIZE_MINI);
        addMax.setTitle("+Add max");
        //addMax.setIcon(R.drawable.fab_bg_mini);

        //addFriend.setSize(FloatingActionButton.SIZE_MINI);

        //  if keyboard is shown, hide FAB
        //  if keyboard is hidden, show FAB
        frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(activity != null) {
                    int heightDiff = frameLayout.getRootView().getHeight() - frameLayout.getHeight();
                    if (heightDiff > dpToPx(activity, 200)) {
                        addMax.setVisibility(View.INVISIBLE);
                    } else {
                        addMax.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        //  initialize recycler view for max, and display all the maxes user has saved
        setRecyclerView();
        loadMax();


        //  creates a new max by adding a new viewholder to max recyclerview
        addMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                maxModel maxModel = new maxModel();
                maxModelArrayList.add(maxModel);
                maxAdapter.notifyItemInserted(maxAdapter.getItemCount() - 1);
            }
        });
    }

    //  checks Firebase for any maxes that are saved
    //  if found, max is added to maxModelArrayList which will be used to display data in adapter
    private void loadMax(){
        if(myRef != null) {
            myRef.child("max")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            maxModelArrayList.clear();
                            if (dataSnapshot.exists()) {
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    maxModel maxModel = snapshot.getValue(maxModel.class);
                                    maxModelArrayList.add(maxModel);
                                }
                                maxAdapter.notifyDataSetChanged();
                            }
                            //myRef.child("max").removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
        }
    }

    //  sets recycler view by setting adapter and touch helper
    private void setRecyclerView(){
        layoutManager = new LinearLayoutManager(activity);
        maxContainer.setLayoutManager(layoutManager);
        maxAdapter = new MaxAdapter(maxModelArrayList, this);
        maxContainer.setItemAnimator(new DefaultItemAnimator());
        maxContainer.setAdapter(maxAdapter);
        ItemTouchHelper.Callback callback =
                new MaxItemTouchHelperCallback(maxAdapter, maxAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(maxContainer);
    }

    //  initialize Firebase by getting a reference to users data path
    private void initializeFirebase(){

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if(user.getDisplayName() != null) {
                myRef = database.getReference(user.getDisplayName() + "WorkoutTracker");
                String name = "Username: " + user.getDisplayName();
                username.setText(name);
            }
        }
    }


    //  method to check if a keyboard is shown or not
    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }


}

