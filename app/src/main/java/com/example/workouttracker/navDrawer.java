package com.example.workouttracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import com.facebook.stetho.Stetho;

import static android.R.attr.fragment;

public class navDrawer extends AppCompatActivity
        {

    TabLayout tabLayout;
    tabListener tabListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_nav_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Stetho.initializeWithDefaults(this);

        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();*/

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabListener = new tabListener();
        tabLayout.addOnTabSelectedListener(tabListener);


        /*NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);
        View headerView = LayoutInflater.from(this).inflate(R.layout.dummy_header, null);
        navigationView.addHeaderView(headerView);
        navigationView.getHeaderView(0).setVisibility(View.GONE);
        int topPadding = (int) (getResources().getDimension(R.dimen.padding) / getResources().getDisplayMetrics().density);
        ((NavigationMenuView) navigationView.getChildAt(0)).setPadding(0, topPadding, 0, 0);*/
        Fragment home = new Home();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, home, "FragmentHome");
        ft.commit();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        tabLayout.removeOnTabSelectedListener(tabListener);
    }

    /*@Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }*/

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        SharedPreferences soundNotification = getSharedPreferences("sound", Context.MODE_PRIVATE);
        SharedPreferences vibrateNotification = getSharedPreferences("vibrate", Context.MODE_PRIVATE);
        //SharedPreferences round2 = getSharedPreferences("round2_5", Context.MODE_PRIVATE);
        //SharedPreferences round5 = getSharedPreferences("round5", Context.MODE_PRIVATE);
        //boolean roundPercentageToNearest2 = round2.getBoolean("round2_5RadioButton", false);
        //boolean roundPercentageToNearest5 = round5.getBoolean("round5RadioButton", true);
        boolean sound = soundNotification.getBoolean("soundCheckbox", false);
        boolean vibrate = vibrateNotification.getBoolean("vibrateCheckbox", false);
        //menu.findItem(R.id.round2_5).setChecked(roundPercentageToNearest2);
        //menu.findItem(R.id.round5).setChecked(roundPercentageToNearest5);
        menu.findItem(R.id.soundNotification).setChecked(sound);
        menu.findItem(R.id.vibrateNotification).setChecked(vibrate);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.soundNotification) {
                item.setChecked(!item.isChecked());
                SharedPreferences soundSetting = getSharedPreferences("sound", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = soundSetting.edit();
                editor.putBoolean("soundCheckbox", item.isChecked());
                editor.apply();
                return true;
        }
        if (id == R.id.vibrateNotification){
            item.setChecked(!item.isChecked());
            SharedPreferences soundSetting = getSharedPreferences("vibrate", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = soundSetting.edit();
            editor.putBoolean("vibrateCheckbox", item.isChecked());
            editor.apply();
            return true;
        }
        /*if (id == R.id.round2_5){
            item.setChecked(!item.isChecked());
            SharedPreferences round2_5 = getSharedPreferences("round2_5", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = round2_5.edit();
            editor.putBoolean("round2_5RadioButton", item.isChecked());
            editor.apply();
            return true;
        }
        if (id == R.id.round5){
            item.setChecked(!item.isChecked());
            SharedPreferences round5 = getSharedPreferences("round5", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = round5.edit();
            editor.putBoolean("round5RadioButton", item.isChecked());
            editor.apply();
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    /*@SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        displaySelectedScreen(item.getItemId());
        return true;
    }*/


    /*private void displaySelectedScreen(int itemId) {

        //creating fragment object
        Fragment home = null;
        Fragment savedWorkouts = null;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.home:
                home = new Home();
                break;
            case R.id.savedWorkouts:
                savedWorkouts = new SavedWorkouts();
                break;
        }

        //replacing the fragment
        if (home != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, home, "FragmentHome");
            ft.commit();
        }

        if (savedWorkouts != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, savedWorkouts, "FragmentSavedWorkouts");
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }*/

    private class tabListener implements TabLayout.OnTabSelectedListener{
        @Override
        public void onTabSelected(TabLayout.Tab tab) {

            if(tab.getPosition() == 0){
                Fragment home = new Home();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, home, "FragmentHome");
                ft.commit();
            }

            else if(tab.getPosition() == 1){
                Fragment workout = new SavedWorkouts();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, workout, "FragmentSavedWorkouts");
                ft.commit();
            }

            else if(tab.getPosition() == 2){
                Fragment max = new Max();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, max, "FragmentMax");
                ft.commit();
            }

        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }

}
