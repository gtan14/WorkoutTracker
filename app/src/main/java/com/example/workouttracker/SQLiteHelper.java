package com.example.workouttracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Array;
import java.util.ArrayList;

import static android.R.attr.data;
import static android.R.attr.max;
import static android.R.attr.name;
import static com.example.workouttracker.R.id.exercise;
import static java.sql.Types.VARCHAR;


public class SQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SQLiteDatabase.db";
    public SQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public String TABLE_NAME;
    public String ON_PAUSE_TABLE;
    public String LOAD = "LOAD_WORK";
    public static final String TYPE = "TYPE";
    public static final String EXERCISE_NAME = "EXERCISE";
    public static final String TIME = "TIME";
    public static final String S = "S";
    public static final String REPS = "REPS";
    public static final String MINUTES = "MINUTES";
    public static final String SECONDS = "SECONDS";
    public static final String WEIGHT = "WEIGHT";
    public static final String SQUAT = "SQUAT";
    public static final String BENCH = "BENCH";
    public static final String DEADLIFT = "DEADLIFT";
    public static final String CJ = "CJ";
    public static final String SNATCH = "SNATCH";
    public static final String POWER_CLEAN = "POWER_CLEAN";

    SQLiteDatabase database;

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    //Creates the table corresponding to the workout with the table name as the workout name
    public void createUserTable(String tableName) {
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        String CREATE_TABLE_NEW_USER = "CREATE TABLE " + tableName + " ( " + TYPE + " INTEGER," + EXERCISE_NAME + " VARCHAR, " + WEIGHT + " INTEGER, " + S + " INTEGER, " + REPS + " INTEGER, " + MINUTES + " INTEGER, " + SECONDS + " INTEGER)";
        database.execSQL(CREATE_TABLE_NEW_USER);
        TABLE_NAME = tableName;
        database.close();
    }

    //Creates the table corresponding to the workout when onPause() is called
    public void createOnPauseTable(String tableName) {
        database = this.getReadableDatabase();
        String CREATE_TABLE_NEW_USER = "CREATE TABLE " + tableName + " ( " + TYPE + " INTEGER," + EXERCISE_NAME + " VARCHAR, " + WEIGHT + " INTEGER, " + S + " INTEGER, " + REPS + " INTEGER, " + MINUTES + " INTEGER, " + SECONDS + " INTEGER)";
        database.execSQL(CREATE_TABLE_NEW_USER);
        ON_PAUSE_TABLE = tableName;
        database.close();
    }

    //Creates table for 1rm for squat, bench, deadlift, cj, and snatch
    public void createMaxTable(String tableName){
        database = this.getReadableDatabase();
        String CREATE_TABLE_NEW_TIME = "CREATE TABLE " + tableName + " ( " + SQUAT + " INTEGER," + BENCH + " INTEGER," + DEADLIFT + " INTEGER," + CJ + " INTEGER," + SNATCH + " INTEGER," + POWER_CLEAN + " INTEGER)";
        database.execSQL(CREATE_TABLE_NEW_TIME);
        database.close();
    }

    //Creates a time table that stores the time it took to complete the workout. Corresponds to the userTable
    public void createTimeTable(String tableName){
        database = this.getReadableDatabase();
        String table = "[" + tableName  + "TIIIIIME]";
        String CREATE_TABLE_NEW_TIME = "CREATE TABLE " + table + " ( " + TIME + " VARCHAR)";
        database.execSQL(CREATE_TABLE_NEW_TIME);
        database.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


    //Inserts View, Exercise, Weight, Sets, Reps into the userTable
    //For exercise view
    public void insertRecordExercise(ExerciseModel contact, String tableName) {
        tableName = "[" + tableName + "]";
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TYPE, contact.getView());
        contentValues.put(EXERCISE_NAME, contact.getExercise());
        contentValues.put(WEIGHT, contact.getWeight());
        contentValues.put(S, contact.getSets());
        contentValues.put(REPS, contact.getReps());
        contentValues.putNull(MINUTES);
        contentValues.putNull(SECONDS);
        database.insert(tableName, null, contentValues);
        database.close();
    }

    //Inserts View, Minutes, Seconds into UserTable
    //For rest view
    public void insertRecordRest(ExerciseModel contact, String tableName) {
        tableName = "[" + tableName + "]";
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TYPE, contact.getView());
        contentValues.putNull(EXERCISE_NAME);
        contentValues.putNull(WEIGHT);
        contentValues.putNull(S);
        contentValues.putNull(REPS);
        contentValues.put(MINUTES, contact.getMinutes());
        contentValues.put(SECONDS, contact.getSeconds());
        database.insert(tableName, null, contentValues);
        database.close();
    }

    //Inserts View and set into UserTable
    //For superset view and pyramid set view
    public void insertRecordSuperset(ExerciseModel contact, String tableName) {
        tableName = "[" + tableName + "]";
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TYPE, contact.getView());
        contentValues.putNull(EXERCISE_NAME);
        contentValues.putNull(WEIGHT);
        contentValues.put(S, contact.getSets());
        contentValues.putNull(REPS);
        contentValues.putNull(MINUTES);
        contentValues.putNull(SECONDS);
        database.insert(tableName, null, contentValues);
        database.close();
    }

    //Inserts the time into the time table
    public void insertTime(ExerciseModel contact, String tableName){
        tableName = "[" + tableName + "]";
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TIME, contact.getTime());
        database.insert(tableName, null, contentValues);
        database.close();
    }

    //Insert 1rm into max table
    public void insertMax(maxModel maxModel, String tableName){
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SQUAT, maxModel.getMaxSquat());
        contentValues.put(BENCH, maxModel.getMaxBench());
        contentValues.put(DEADLIFT, maxModel.getMaxDeadlift());
        contentValues.put(CJ, maxModel.getMaxCJ());
        contentValues.put(SNATCH, maxModel.getMaxSnatch());
        contentValues.put(POWER_CLEAN, maxModel.getMaxPowerClean());
        database.insert(tableName, null, contentValues);
        database.close();
    }


    //Updates the userTable exercise
    public void updateRecordExercise(ExerciseModel contact, String tableName) {
        tableName = "[" + tableName + "]";
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TYPE, contact.getView());
        contentValues.put(EXERCISE_NAME, contact.getExercise());
        contentValues.put(WEIGHT, contact.getWeight());
        contentValues.put(S, contact.getSets());
        contentValues.put(REPS, contact.getReps());
        contentValues.putNull(MINUTES);
        contentValues.putNull(SECONDS);
        database.insert(tableName, null, contentValues);
        database.close();
    }

    //updates View, Minutes, Seconds into UserTable
    //For rest view
    public void updateRecordRest(ExerciseModel contact, String tableName) {
        tableName = "[" + tableName + "]";
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TYPE, contact.getView());
        contentValues.putNull(EXERCISE_NAME);
        contentValues.putNull(WEIGHT);
        contentValues.putNull(S);
        contentValues.putNull(REPS);
        contentValues.put(MINUTES, contact.getMinutes());
        contentValues.put(SECONDS, contact.getSeconds());
        database.insert(tableName, null, contentValues);
        database.close();
    }

    //updates View and set into UserTable
    //For superset view and pyramid set view
    public void updateRecordSuperset(ExerciseModel contact, String tableName) {
        tableName = "[" + tableName + "]";
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TYPE, contact.getView());
        contentValues.putNull(EXERCISE_NAME);
        contentValues.putNull(WEIGHT);
        contentValues.put(S, contact.getSets());
        contentValues.putNull(REPS);
        contentValues.putNull(MINUTES);
        contentValues.putNull(SECONDS);
        database.insert(tableName, null, contentValues);
        database.close();
    }

    //Deletes all the rows in the table
    public void deleteRecord(String tableName) {
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        database.delete(tableName, null, null);
        database.close();
    }

    //Deletes tableName from database
    public void deleteTable(String tableName){
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        database.execSQL("DROP TABLE IF EXISTS " + tableName);
        database.close();
    }

    //Gets all the table names and assigns it as an ArrayList<String>
    public ArrayList<String> getTableNames(){
        database = this.getWritableDatabase();
        ArrayList<String> tableNames = new ArrayList<String>();
        Cursor c = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        //While cursor is not in the last position and while it has a position after
        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                tableNames.add( c.getString( c.getColumnIndex("name")) );
                c.moveToNext();
            }
        }
        c.close();
        database.close();
        return tableNames;
    }

    /*
    public void clearDatabase(String TABLE_NAME) {
        String clearDBQuery = "DELETE FROM "+TABLE_NAME;
        database.execSQL(clearDBQuery);
        database.close();
    }*/

    //Gets each column value for tableName
    public ArrayList<ExerciseModel> getAllRecords(String tableName) {
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        Cursor cursor = database.query(tableName, null, null, null, null, null, null);
        ArrayList<ExerciseModel> exercise = new ArrayList<ExerciseModel>();
        ExerciseModel exerciseModel;
        if (cursor.getCount() > 0) {
            //Gets data from each row and stores it into ArrayList<ExerciseModel>
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                exerciseModel = new ExerciseModel();
                exerciseModel.setView(cursor.getString(0));
                exerciseModel.setExercise(cursor.getString(1));
                exerciseModel.setWeight(cursor.getString(2));
                exerciseModel.setSets(cursor.getString(3));
                exerciseModel.setReps(cursor.getString(4));
                exerciseModel.setMinutes(cursor.getString(5));
                exerciseModel.setSeconds(cursor.getString(6));
                exercise.add(exerciseModel);
            }
        }
        cursor.close();
        database.close();
        return exercise;
    }

    //Gets the time for tableName and stores it in ArrayList<ExerciseModel>
    public ArrayList<ExerciseModel> getTimeRecords(String tableName) {
        database = this.getReadableDatabase();
        tableName = "[" + tableName + "]";
        Cursor cursor = database.query(tableName, null, null, null, null, null, null);
        ArrayList<ExerciseModel> exercise = new ArrayList<ExerciseModel>();
        ExerciseModel exerciseModel;
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                exerciseModel = new ExerciseModel();
                exerciseModel.setTime(cursor.getString(0));
                exercise.add(exerciseModel);
            }
        }
        cursor.close();
        database.close();
        return exercise;
    }

    //Gets the maxes from max table
    public ArrayList<maxModel> getMaxes(String tableName) {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(tableName, null, null, null, null, null, null);
        ArrayList<maxModel> maxModels = new ArrayList<maxModel>();
        maxModel maxModel;
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                maxModel = new maxModel();
                maxModel.setMaxSquat(cursor.getString(0));
                maxModel.setMaxBench(cursor.getString(1));
                maxModel.setMaxDeadlift(cursor.getString(2));
                maxModel.setMaxCJ(cursor.getString(3));
                maxModel.setMaxSnatch(cursor.getString(4));
                maxModel.setMaxPowerClean(cursor.getString(5));
                maxModels.add(maxModel);
            }
        }
        cursor.close();
        database.close();
        return maxModels;
    }

    boolean tableExists(String tableName)
    {
        database =this.getReadableDatabase();
        Cursor cursor = database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

}
