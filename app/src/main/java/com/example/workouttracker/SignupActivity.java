package com.example.workouttracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Gerald on 10/29/2017.
 */

public class SignupActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private EditText reenterPass;
    private EditText username;
    private Button cancel;
    private Button signup;
    private TextInputLayout emailTextInputLayout;
    private TextInputLayout passwordTextInputLayout;
    private TextInputLayout reenterPassTextInputLayout;
    private TextInputLayout usernameTextInputLayout;
    private FirebaseAuth mAuth;
    private final String TAG = "FIREBASE";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("users");
    private boolean proceedToSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        //  view initialization
        email = findViewById(R.id.email_sign_up);
        password = findViewById(R.id.password_sign_up);
        reenterPass = findViewById(R.id.reenter_pass_sign_up);
        username = findViewById(R.id.username_sign_up);

        cancel = findViewById(R.id.cancel_signup_button);
        signup = findViewById(R.id.sign_up_button);

        emailTextInputLayout = findViewById(R.id.email_text_input_layout);
        passwordTextInputLayout = findViewById(R.id.password_text_input_layout);
        reenterPassTextInputLayout = findViewById(R.id.reenter_pass_text_input_layout);
        usernameTextInputLayout = findViewById(R.id.username_text_input_layout);

        textListeners(emailTextInputLayout, passwordTextInputLayout, reenterPassTextInputLayout, usernameTextInputLayout);


        mAuth = FirebaseAuth.getInstance();

    }


    //  method that cancels signup
    public void cancelSignup(View view){

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    //  signup btn on click
    //  checks for any input errors
    //  if no errors, take user back to login screen
    //  if errors, display toast
    public void signup(View view){

        String emailString = email.getText().toString();
        String passwordString = password.getText().toString();
        String reenterPassString = reenterPass.getText().toString();
        final String usernameString = username.getText().toString();
        proceedToSignup = true;

        /*
            Error will show if any of the text fields are empty
            Error will show if password is less than 6 chars

        */

        if(emailString.isEmpty()){
            emailTextInputLayout.setError("Enter email");
            proceedToSignup = false;
        }

        if(passwordString.isEmpty()){
            passwordTextInputLayout.setError("Enter password");
            proceedToSignup = false;
        }

        else if(passwordString.length() < 6){
            passwordTextInputLayout.setError("Must be at least 6 characters");
        }

        if(reenterPassString.isEmpty()){
            reenterPassTextInputLayout.setError("Re-enter password");
            proceedToSignup = false;
        }

        if(usernameString.isEmpty()){
            usernameTextInputLayout.setError("Enter username");
            proceedToSignup = false;
        }

        else{

            //  checks to see if there are any users with the same username that this user is signing up for
            //  if there is, prevent from successfully signing up, and show error
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        if(snapshot.getKey().equals(usernameString)){
                            usernameTextInputLayout.setError("Username taken");
                            proceedToSignup = false;
                        }
                    }
                    myRef.removeEventListener(this);
                    proceedSignup(usernameString);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    //  method that successfully signs up a user
    private void proceedSignup(final String usernameString){

        if(proceedToSignup){
            mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                            //  If sign in fails, display a message to the user. If sign in succeeds
                            //  the auth state listener will be notified and logic to handle the
                            //  signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(SignupActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(SignupActivity.this, "Signup failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{

                                //  create a user if sign up was successful
                                //  user will have a username linked to that account
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(usernameString)
                                        .build();

                                if(user != null) {
                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        createNewUser(usernameString);
                                                        Toast.makeText(SignupActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });
                                }
                            }

                        }
                    });
        }
    }

    //  creates a new user in firebase with attr: email, first name, and last name
    private void createNewUser(String username) {

        User userFromRegistration = new User();

        //userFromRegistration.setEmail(email);
        userFromRegistration.setUsername(username);

        myRef.child(username).setValue(userFromRegistration);
    }

    //  text watcher that removes the errors from the edit text once text is changed
    private void textListeners(TextInputLayout...textInputLayouts){
        for(final TextInputLayout layout : textInputLayouts){
            if(layout.getEditText() != null) {
                layout.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        layout.setError("");
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
