package com.example.workouttracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Gerald on 1/4/2018.
 */

public class Settings extends AppCompatActivity {

    private CheckBox vibrate, sound;
    private RadioGroup roundRadioGroup, conversionRadioGroup;
    private android.support.v7.widget.Toolbar toolbar;
    private RadioButton radioButton2_5, radioButton5, lbToKGRadioBtn, kGToLbRadioBtn, noConversionRadioBtn;
    private Button logoutBtn;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        vibrate = findViewById(R.id.vibrateCheckbox);
        sound = findViewById(R.id.soundCheckbox);
        roundRadioGroup = findViewById(R.id.roundRadioGroup);
        conversionRadioGroup = findViewById(R.id.conversionRadioGroup);
        logoutBtn = (Button) findViewById(R.id.logoutButton);
        radioButton2_5 = (RadioButton) findViewById(R.id.round2_5RadioButton);
        radioButton5 = (RadioButton) findViewById(R.id.round5RadioButton);
        lbToKGRadioBtn = (RadioButton) findViewById(R.id.lbToKGRadioBtn);
        kGToLbRadioBtn = (RadioButton) findViewById(R.id.KGToLbRadioBtn);
        noConversionRadioBtn = (RadioButton) findViewById(R.id.noConversionRadioBtn);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.settingsToolbar);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Toast.makeText(Settings.this, "Sign out successful", Toast.LENGTH_LONG).show();
                }
            }
        };

        //  sets title
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        setTitle("Settings");

        //  set the default settings for user
        radioButton2_5.setChecked(true);
        vibrate.setChecked(true);
        sound.setChecked(true);
        noConversionRadioBtn.setChecked(true);

        //  gets shared pref for rounding weight, and alerting user when rest is finished
        final SharedPreferences restAlertSharedPref = getSharedPreferences("restAlert", Context.MODE_PRIVATE);
        final SharedPreferences roundSharedPref = getSharedPreferences("round", Context.MODE_PRIVATE);
        final SharedPreferences conversionSharedPref = getSharedPreferences("weightConversion", Context.MODE_PRIVATE);

        boolean vibrateChecked = restAlertSharedPref.getBoolean("vibrate", false);
        boolean soundChecked = restAlertSharedPref.getBoolean("sound", false);
        String round = roundSharedPref.getString("roundWeight", "");
        String conversion = conversionSharedPref.getString("convert", "");


        //  if vibrate is true, set checkbox to checked
        //  if sound is true, set checkbox to checked
        //  if string round is not empty, set one of the radio buttons under round to checked depending on value
        //  if string conversion is not empty, set one of the radio buttons under conversion to checked depending on value
        if(vibrateChecked){
            vibrate.setChecked(true);
        }

        else{
            vibrate.setChecked(false);
        }

        if(soundChecked){
            sound.setChecked(true);
        }

        else{
            sound.setChecked(false);
        }

        if(!round.isEmpty()){
            if(round.equals("2.5")){
                radioButton2_5.setChecked(true);
            }

            else if(round.equals("5")){
                radioButton5.setChecked(true);
            }
        }

        if(!conversion.isEmpty()){
            if(conversion.equals("lbToKg")){
                lbToKGRadioBtn.setChecked(true);
            }
            else if(conversion.equals("kgToLb")){
                kGToLbRadioBtn.setChecked(true);
            }
            else if(conversion.equals("none")){
                noConversionRadioBtn.setChecked(true);
            }
        }

        //  listeners for when a user checks vibrate, sound, one of the rounding options, or one of the conversion options

        vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    restAlertSharedPref.edit().putBoolean("vibrate", true).apply();
                }
                else{
                    restAlertSharedPref.edit().putBoolean("vibrate", false).apply();
                }
            }
        });

        sound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    restAlertSharedPref.edit().putBoolean("sound", true).apply();
                }
                else{
                    restAlertSharedPref.edit().putBoolean("sound", false).apply();
                }
            }
        });

        roundRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                RadioButton round2_5 = (RadioButton) radioGroup.findViewById(R.id.round2_5RadioButton);
                RadioButton round5 = (RadioButton) radioGroup.findViewById(R.id.round5RadioButton);

                if(round2_5.isChecked()){
                    roundSharedPref.edit().putString("roundWeight", "2.5").apply();
                }

                else if(round5.isChecked()){
                    roundSharedPref.edit().putString("roundWeight", "5").apply();
                }
            }
        });

        conversionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                RadioButton lbToKG = (RadioButton) radioGroup.findViewById(R.id.lbToKGRadioBtn);
                RadioButton kgTolb = (RadioButton) radioGroup.findViewById(R.id.KGToLbRadioBtn);
                RadioButton none = (RadioButton) radioGroup.findViewById(R.id.noConversionRadioBtn);

                if(lbToKG.isChecked()){
                    conversionSharedPref.edit().putString("convert", "lbToKg").apply();
                }

                else if(kgTolb.isChecked()){
                    conversionSharedPref.edit().putString("convert", "kgToLb").apply();
                }

                else if(none.isChecked()){
                    conversionSharedPref.edit().putString("convert", "none").apply();
                }
            }
        });

        //  displays alert dialog when user presses logout btn
        //  clears autoSave and keepSignIn shared pref if user confirms
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                builder.setMessage("Are you sure you want to logout?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences sharedPreferences = getSharedPreferences("keepSignedIn", Context.MODE_PRIVATE);
                        SharedPreferences autoSave = getSharedPreferences("autoSave", Context.MODE_PRIVATE);
                        sharedPreferences.edit().clear().apply();
                        autoSave.edit().clear().apply();
                        Intent intent = new Intent(Settings.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

    }
}
