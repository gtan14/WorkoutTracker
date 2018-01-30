package com.example.workouttracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Gerald on 10/29/2017.
 */

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private CheckBox keepSignedIn;
    private Button signIn;
    private Button signUp;
    private TextInputLayout emailTextInputLayout;
    private TextInputLayout passwordTextInputLayout;
    private FirebaseAuth mAuth;
    private final String TAG = "FIREBASE";
    private Bundle workoutReceivedBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("keepSignedIn", Context.MODE_PRIVATE);
        boolean keepLoggedIn = sharedPreferences.getBoolean("signIn", false);

        mAuth = FirebaseAuth.getInstance();

        /*Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            workoutReceivedBundle = bundle;
        }*/

        //  if keep signed in checkbox is checked, auto sign in
        if(keepLoggedIn){
            autoSignIn();
        }

        else {
            setContentView(R.layout.activity_login);


            //  view initialization
            email = (EditText) findViewById(R.id.email_login);
            password = (EditText) findViewById(R.id.password);
            keepSignedIn = (CheckBox) findViewById(R.id.keep_signed_in_checkbox);
            signIn = (Button) findViewById(R.id.signin_button);
            signUp = (Button) findViewById(R.id.sign_up_button);
            emailTextInputLayout = findViewById(R.id.email_signin_text_input_layout);
            passwordTextInputLayout = findViewById(R.id.password_signin_text_input_layout);

            textListener(emailTextInputLayout, passwordTextInputLayout);


            keepSignedIn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    SharedPreferences sharedPreferences = getSharedPreferences("keepSignedIn", Context.MODE_PRIVATE);
                    sharedPreferences.edit().putBoolean("signIn", true).apply();
                }
            });
        }

    }

    //  sign in on click btn
    public void signIn(View view){

        boolean proceedWithSignIn = true;
        if(email.getText().toString().length() == 0){
            emailTextInputLayout.setError("Enter email");
            proceedWithSignIn = false;
        }

        if(password.getText().toString().length() == 0){
            passwordTextInputLayout.setError("Enter password");
            proceedWithSignIn = false;
        }

        if(proceedWithSignIn) {
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                            //  If sign in fails, display a message to the user. If sign in succeeds
                            //  the auth state listener will be notified and logic to handle the
                            //  signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Invalid email/password", Toast.LENGTH_SHORT).show();
                            } else {

                                //  creates a shared pref with username and pass if keep signed in checkbox is checked
                                //  this is so that users can automatically sign in
                                if (keepSignedIn.isChecked()) {
                                    SharedPreferences sharedPreferences = getSharedPreferences("credentialsForAutoSignIn", Context.MODE_PRIVATE);
                                    sharedPreferences.edit().putString("usernameAutoSignIn", email.getText().toString()).apply();
                                    sharedPreferences.edit().putString("passAutoSignIn", password.getText().toString()).apply();
                                }
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }

                            // ...
                        }
                    });
        }
    }

    //  method that auto signs in users
    //  retrieves username and password last used for last sign in through shared prefs
    //  signs in with those credentials
    //  if successful, takes user to home screen
    private void autoSignIn(){
        SharedPreferences sharedPreferences = getSharedPreferences("credentialsForAutoSignIn", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("usernameAutoSignIn", "");
        String pass = sharedPreferences.getString("passAutoSignIn", "");

        if(!email.isEmpty() && !pass.isEmpty()) {
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                            //  If sign in fails, display a message to the user. If sign in succeeds
                            //  the auth state listener will be notified and logic to handle the
                            //  signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Invalid email/password", Toast.LENGTH_SHORT).show();
                            } else {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                if(workoutReceivedBundle != null) {
                                    intent.putExtra("workoutReceivedBundle", workoutReceivedBundle);
                                }
                                startActivity(intent);
                            }

                            // ...
                        }
                    });
        }
    }

    //  sign up btn on click
    public void signup(View view){
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(intent);
    }

    private void textListener(TextInputLayout...textInputLayouts){
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
