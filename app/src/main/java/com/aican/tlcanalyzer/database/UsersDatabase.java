package com.aican.tlcanalyzer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UsersDatabase extends SQLiteOpenHelper {

    public UsersDatabase(Context context) {
        super(context, "userdatabase.db", null, 3);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table Userdetails(name TEXT,role TEXT,id TEXT," +
                "passcode TEXT,expiryDate TEXT,dateCreated TEXT)");
        db.execSQL("create Table UserLogDetails(date TEXT, time TEXT, name TEXT, " +
                "role TEXT, activities TEXT, projectName TEXT, projectID TEXT, projectType TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS UserLogDetails");


        if (oldVersion >= newVersion) {
            return;
        }
    }

    public boolean logUserAction(String name, String role, String activities, String projectName,
                                 String projectID, String projectType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", getPresentDate());
        contentValues.put("time", getPresentTime());
        contentValues.put("name", name);
        contentValues.put("role", role);
        contentValues.put("activities", activities);
        contentValues.put("projectName", projectName);
        contentValues.put("projectID", projectID);
        contentValues.put("projectType", projectType);
        long result = db.insert("UserLogDetails", null, contentValues);
        return result != -1;
    }

    public Cursor get_userActivity_data() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from UserLogDetails", null);
        return cursor;
    }



    public boolean updateUserDetails(String name, String uid, String newName, String role, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor = db.rawQuery("Select * from UserDataDetails", null);

//        if (cursor.getCount() > 0) {
        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put("name", newName);
        dataToInsert.put("role", role);
        dataToInsert.put("passcode", password);
        dataToInsert.put("expiryDate", getExpiryDate());
        dataToInsert.put("dateCreated", getPresentDate());

        ContentValues dataToInsertUserData = new ContentValues();
        dataToInsertUserData.put("Username", newName);
        dataToInsertUserData.put("Role", role);
        dataToInsertUserData.put("expiryDate", getExpiryDate());
        dataToInsertUserData.put("dateCreated", getPresentDate());


        long result = db.update("Userdetails", dataToInsert, "id=?", new String[]{uid});
        Log.e("ResultCode021", result + " , ");
        if (result == -1) {
            return false;
        } else {
            return true;
        }
//        } else {
//            return false;
//        }


    }


    public Boolean insert_data(String name, String role, String id, String passcode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("role", role);
        contentValues.put("id", id);
        contentValues.put("passcode", passcode);
        contentValues.put("expiryDate", getExpiryDate());
        contentValues.put("dateCreated", getPresentDate());
        long result = db.insert("Userdetails", null, contentValues);
        return result != -1;
    }

    public Cursor get_data() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from Userdetails", null);
        return cursor;
    }

    private String getExpiryDate() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String presentDate = dateFormat.format(date);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            cal.setTime(sdf.parse(presentDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // use add() method to add the days to the given date
        cal.add(Calendar.DAY_OF_MONTH, 90);
        String expiryDate = sdf.format(cal.getTime());

        return expiryDate;
    }

    private String getPresentTime() {
        long currentTimeMillis = System.currentTimeMillis();
        Date currentDate = new Date(currentTimeMillis);

        // Create a SimpleDateFormat object to format the time
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Format the time and display it
        String formattedTime = sdf.format(currentDate);
        return formattedTime;
    }

    private String getPresentDate() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String presentDate = dateFormat.format(date);
        return presentDate;
    }

    public Boolean delete_data(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from Userdetails", null);
        if (cursor.getCount() > 0) {
            long result = db.delete("Userdetails", "name=?", new String[]{name});
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

}
