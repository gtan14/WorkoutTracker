package com.example.workouttracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Gerald on 10/29/2017.
 */

public class SignupActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private EditText reenterPass;
    private EditText firstName;
    private EditText lastName;
    private Button cancel;
    private Button signup;
    private TextInputLayout emailTextInputLayout;
    private TextInputLayout passwordTextInputLayout;
    private TextInputLayout reenterPassTextInputLayout;
    private TextInputLayout firstNameTextInputLayout;
    private TextInputLayout lastNameTextInputLayout;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private final String TAG = "FIREBASE";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        //  view initialization
        email = findViewById(R.id.email_sign_up);
        password = findViewById(R.id.password_sign_up);
        reenterPass = findViewById(R.id.reenter_pass_sign_up);
        firstName = findViewById(R.id.first_name_sign_up);
        lastName = findViewById(R.id.last_name_sign_up);

        cancel = findViewById(R.id.cancel_signup_button);
        signup = findViewById(R.id.sign_up_button);

        emailTextInputLayout = findViewById(R.id.email_text_input_layout);
        passwordTextInputLayout = findViewById(R.id.password_text_input_layout);
        reenterPassTextInputLayout = findViewById(R.id.reenter_pass_text_input_layout);
        firstNameTextInputLayout = findViewById(R.id.first_name_text_input_layout);
        lastNameTextInputLayout = findViewById(R.id.last_name_text_input_layout);


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
        String firstNameString = firstName.getText().toString();
        String lastNameString = lastName.getText().toString();
        boolean proceedToSignup = true;

        /*
            Error will show if any of the text fields are empty
        */

        if(emailString.isEmpty()){
            emailTextInputLayout.setError("Enter email");
            proceedToSignup = false;
        }

        if(passwordString.isEmpty()){
            passwordTextInputLayout.setError("Enter password");
            proceedToSignup = false;
        }

        if(reenterPassString.isEmpty()){
            reenterPassTextInputLayout.setError("Re-enter password");
            proceedToSignup = false;
        }

        if(firstNameString.isEmpty()){
            firstNameTextInputLayout.setError("Enter first name");
            proceedToSignup = false;
        }

        if(lastNameString.isEmpty()){
            lastNameTextInputLayout.setError("Enter last name");
            proceedToSignup = false;
        }

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
                                createNewUser(email.getText().toString(), firstName.getText().toString(), lastName.getText().toString());
                                Toast.makeText(SignupActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }

                            // ...
                        }
                    });
        }
    }

    //  creates a new user in firebase with attr: email, first name, and last name
    private void createNewUser(String email, String firstName, String lastName) {

        User userFromRegistration = new User();

        userFromRegistration.setEmail(email);
        userFromRegistration.setFirstName(firstName);
        userFromRegistration.setLastName(lastName);

        String emailString = userFromRegistration.getEmail().replace(".", ",");

        myRef.child(emailString).setValue(userFromRegistration);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
