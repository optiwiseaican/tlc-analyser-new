package com.aican.tlcanalyzer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.aican.tlcanalyzer.dataClasses.ProjectOfflineData;
import com.aican.tlcanalyzer.dataClasses.SplitData;
import com.aican.tlcanalyzer.utils.Source;

import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static String DATABASE_TABLE = "ProjectDetails";

    private static final int DATABASE_VERSION = 5;
//    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(@Nullable Context context) {
        super(context, "projects.db", null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table ProjectDetails(id TEXT, projectName TEXT, projectDescription TEXT,timeStamp TEXT, projectNumber TEXT, projectImage TEXT," +
                "imageSplitAvailable TEXT, splitId TEXT, thresholdVal TEXT, noOfSpots TEXT, tableName TEXT, roiTableID TEXT, volumePlotTableID TEXT" +
                ", intensityPlotTableID TEXT, plotTableID TEXT," +
                "rmSpot TEXT, finalSpot TEXT)");
//        db.execSQL("create Table ProjectDetails(id INTEGER PRIMARY KEY AUTOINCREMENT, projectName TEXT, projectDescription TEXT, projectImage TEXT, projectNumber TEXT, imageSplitAvaialable TEXT, splitId TEXT, thresholdVal TEXT, noOfSpots TEXT)");
        db.execSQL("CREATE TABLE SplitImageData(id TEXT, name TEXT, path TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        Source.oldVersion = oldVersion;
        Source.newVersion = newVersion;
        if (oldVersion >= newVersion) {

            return;
        }


//
//        db.execSQL("DROP TABLE IF EXISTS ProjectDetails");
//        db.execSQL("DROP TABLE IF EXISTS SplitImageData");

        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + "ProjectDetails" + " ADD COLUMN " + "volumePlotTableID" + " TEXT DEFAULT 'na';");
            db.execSQL("ALTER TABLE " + "ProjectDetails" + " ADD COLUMN " + "intensityPlotTableID" + " TEXT DEFAULT 'na';");
            db.execSQL("ALTER TABLE " + "ProjectDetails" + " ADD COLUMN " + "plotTableID" + " TEXT DEFAULT 'na';");
        }

//        onCreate(db);
    }

    public boolean logUserAction(String name, String role, String activity, String projectName,
                                 String projectID, String projectType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", getPresentDate());
        contentValues.put("time", getPresentTime());
        contentValues.put("name", name);
        contentValues.put("role", role);
        contentValues.put("activity", activity);
        contentValues.put("projectName", projectName);
        contentValues.put("projectID", projectID);
        contentValues.put("projectType", projectType);
        long result = db.insert("UserLogDetails", null, contentValues);
        return result != -1;
    }


    public void createSplitTable(String tableName) {

        final SQLiteDatabase db = getWritableDatabase();

        String CREATE_TABLE_NEW_USER = "CREATE TABLE " + tableName + "(id TEXT, name TEXT, path TEXT, " +
                "timeStamp TEXT, " +
                "thresholdVal TEXT, noOfSpots TEXT, tableName TEXT, roiTableID TEXT," +
                "volumePlotTableID TEXT, intensityPlotTableID TEXT, plotTableID TEXT, " +
                "description TEXT, hour TEXT, rmSpot TEXT, finalSpot TEXT)";
        db.execSQL(CREATE_TABLE_NEW_USER);
        db.close();
    }

    public void createSplitMainImageTable(String tableName) {

        final SQLiteDatabase db = getWritableDatabase();

        String CREATE_TABLE_NEW_USER = "CREATE TABLE " + tableName + "(id TEXT, name TEXT, path TEXT, " +
                "timeStamp TEXT, " +
                "thresholdVal TEXT, noOfSpots TEXT, tableName TEXT, roiTableID TEXT," +
                "volumePlotTableID TEXT, intensityPlotTableID TEXT, plotTableID TEXT, " +
                "description TEXT, hour TEXT, rmSpot TEXT, finalSpot TEXT)";
        db.execSQL(CREATE_TABLE_NEW_USER);
        db.close();
    }


    public void createAllDataTable(String tableName) {
        final SQLiteDatabase db = getWritableDatabase();
        String CREATE_TABLE_NEW_USER = "CREATE TABLE " + tableName + "(id TEXT, rf TEXT, rfTop TEXT, rfBottom TEXT, cv TEXT, " +
                "area TEXT, " +
                "areaPercent TEXT, volume TEXT)";
//                "areaPercent TEXT, volume TEXT, chemicalName TEXT)";
        db.execSQL(CREATE_TABLE_NEW_USER);
        db.close();
    }

    public void updateLabelById(String tableName, String id, String newLabel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("label", newLabel); // Replace with the actual column name

        db.update(tableName, values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void createRfVsAreaIntensityPlotTable(String tableName) {
        final SQLiteDatabase db = getWritableDatabase();
        String CREATE_TABLE_NEW_USER = "CREATE TABLE " + tableName + "(id TEXT, rf TEXT, area TEXT)";
        db.execSQL(CREATE_TABLE_NEW_USER);
        db.close();
    }

    public void deleteDataFromTable(String tableName) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + tableName);
        db.close();
    }

    public void createVolumePlotTable(String tableName) {
        final SQLiteDatabase db = getWritableDatabase();
        String CREATE_TABLE_NEW_USER = "CREATE TABLE " + tableName + "(id TEXT, ind TEXT, volume TEXT)";
        db.execSQL(CREATE_TABLE_NEW_USER);
        db.close();
    }

    public void createSpotLabelTable(String tableName) {
        final SQLiteDatabase db = getWritableDatabase();
        String CREATE_TABLE_NEW_USER = "CREATE TABLE " + tableName + "(id TEXT, label TEXT, text1 TEXT, text2 TEXT)";
        db.execSQL(CREATE_TABLE_NEW_USER);
        db.close();
    }

    ///////////////

    public boolean insertRfVsAreaIntensityPlotTableData(String tableName, String id, String rf, String area
    ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("rf", rf);
        contentValues.put("area", area);


        long result = db.insert(tableName, null, contentValues);
        return result != -1;

    }

    public boolean insertAllDataTableData(String tableName, String id, String rf, String rfTop, String rfBottom, String cv
            , String area, String areaPercent, String volume, String chemicalName
    ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("rf", rf);
        contentValues.put("rfTop", rfTop);
        contentValues.put("rfBottom", rfBottom);
        contentValues.put("cv", cv);
        contentValues.put("area", area);
        contentValues.put("areaPercent", areaPercent);
        contentValues.put("volume", volume);
//        contentValues.put("chemicalName", chemicalName);

        long result = db.insert(tableName, null, contentValues);
        return result != -1;

    }

    public boolean updateDataTableDataById(String tableName, String id, String newRf, String newRfTop, String newRfBottom) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("rf", newRf);
        contentValues.put("rfTop", newRfTop);
        contentValues.put("rfBottom", newRfBottom);

        long result = db.update(tableName, contentValues, "id=?", new String[]{id});
        return result != -1;
    }


    public boolean insertSpotLabelData(String tableName, String id, String label) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("label", label);

        long result = db.insert(tableName, null, contentValues);
        return result != -1;

    }

    public void deleteRowById(String tableName, String id) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Define the WHERE clause to specify the condition for deletion
        String whereClause = "id = ?";

        // Specify the value for the WHERE clause
        String[] whereArgs = {String.valueOf(id)};

        // Perform the deletion
        db.delete(tableName, whereClause, whereArgs);

        // Close the database
        db.close();
    }


    public boolean insertVolumePlotTableData(String tableName, String id, String ind, String volume
    ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("ind", ind);
        contentValues.put("volume", volume);

        long result = db.insert(tableName, null, contentValues);
        return result != -1;

    }


    public void deleteTableData(String tableName, String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteLastRow(String table) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Define the table name and a column that can be used to identify the last row
        String primaryKeyColumn = "id"; // Change this to your actual primary key column

        // Get the ID of the last row in the table
        String query = "SELECT MAX(" + primaryKeyColumn + ") FROM " + table;
        Cursor cursor = db.rawQuery(query, null);
        int lastRowId = -1;
        if (cursor.moveToFirst()) {
            lastRowId = cursor.getInt(0);
        }
        cursor.close();

        // Delete the last row if a valid ID was obtained
        if (lastRowId >= 0) {
            String whereClause = primaryKeyColumn + " = ?";
            String[] whereArgs = {String.valueOf(lastRowId)};
            db.delete(table, whereClause, whereArgs);
        }

        db.close();
    }

    public Cursor getDataFromTable(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from " + tableName, null);
        return cursor;
    }


    public boolean insertSplitImage(String tableName, String id, String name, String path, String timeStamp,
                                    String thresholdVal, String noOfSpots, String roiTableID,
                                    String volumePlotTableID, String intensityPlotTableID, String plotTableID, String description,
                                    String hour, String rmSpot, String finalSpot
    ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("name", name);
        contentValues.put("path", path);
        contentValues.put("timeStamp", timeStamp);
        contentValues.put("thresholdVal", thresholdVal);
        contentValues.put("noOfSpots", noOfSpots);
        contentValues.put("tableName", tableName);
        contentValues.put("roiTableID", roiTableID);
        contentValues.put("volumePlotTableID", volumePlotTableID);
        contentValues.put("intensityPlotTableID", intensityPlotTableID);
        contentValues.put("plotTableID", plotTableID);
        contentValues.put("description", description);
        contentValues.put("hour", hour);
        contentValues.put("rmSpot", rmSpot);
        contentValues.put("finalSpot", finalSpot);

        long result = db.insert(tableName, null, contentValues);
        return result != -1;

    }


    public boolean insertSplitMainImage(String tableName, String id, String name, String path, String timeStamp,
                                        String thresholdVal, String noOfSpots, String roiTableID,
                                        String volumePlotTableID, String intensityPlotTableID, String plotTableID, String description,
                                        String hour, String rmSpot, String finalSpot
    ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("name", name);
        contentValues.put("path", path);
        contentValues.put("timeStamp", timeStamp);
        contentValues.put("thresholdVal", thresholdVal);
        contentValues.put("noOfSpots", noOfSpots);
        contentValues.put("tableName", tableName);
        contentValues.put("roiTableID", roiTableID);
        contentValues.put("volumePlotTableID", volumePlotTableID);
        contentValues.put("intensityPlotTableID", intensityPlotTableID);
        contentValues.put("plotTableID", plotTableID);
        contentValues.put("description", description);
        contentValues.put("hour", hour);
        contentValues.put("rmSpot", rmSpot);
        contentValues.put("finalSpot", finalSpot);

        long result = db.insert(tableName, null, contentValues);
        return result != -1;

    }


    public long updateSplitData(SplitData data, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
//        String CREATE_TABLE_NEW_USER = "CREATE TABLE " + tableName + "(id TEXT, name TEXT, path TEXT, " +
//                "timeStamp TEXT, " +
//                "thresholdVal TEXT, noOfSpots TEXT, tableName TEXT, roiTableID TEXT," +
//                "volumePlotTableID TEXT, intensityPlotTableID TEXT, plotTableID TEXT)";
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", data.getId());
        contentValues.put("name", data.getImageName());
        contentValues.put("path", data.getImagePath());
        contentValues.put("timeStamp", data.getTimeStamp());
        contentValues.put("thresholdVal", data.getThresholdVal());
        contentValues.put("noOfSpots", data.getNoOfSpots());
        contentValues.put("tableName", tableName);
        contentValues.put("roiTableID", data.getRoiTableID());
        contentValues.put("volumePlotTableID", data.getVolumePlotTableID());
        contentValues.put("intensityPlotTableID", data.getIntensityPlotTableID());
        contentValues.put("plotTableID", data.getPlotTableID());
        contentValues.put("description", data.getDescription());
        contentValues.put("hour", data.getHour());
        contentValues.put("rmSpot", data.getRmSpot());
        contentValues.put("finalSpot", data.getFinalSpot());


        //Lets update now
        return db.update(tableName, contentValues, "id" + "=?",
                new String[]{String.valueOf(data.getId())});


    }

    public boolean insertData(String id, String projectName, String projectDescription, String timeStamp,
                              String projectImage, String contourImage, String projectNumber, String imageSplitAvailable
            , String splitId, String thresholdVal, String noOfSpots, String tableName, String roiTableID,
                              String volumePlotTableID, String intensityPlotTableID, String plotTableID,
                              String rmSpot, String finalSpot) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("projectName", projectName);
        contentValues.put("projectDescription", projectDescription);
        contentValues.put("timeStamp", timeStamp);
        contentValues.put("projectNumber", projectNumber);
        contentValues.put("projectImage", projectImage);
        contentValues.put("imageSplitAvailable", imageSplitAvailable);
        contentValues.put("splitId", splitId);
        contentValues.put("thresholdVal", thresholdVal);
        contentValues.put("noOfSpots", noOfSpots);
        contentValues.put("tableName", tableName);
        contentValues.put("roiTableID", roiTableID);
        contentValues.put("volumePlotTableID", volumePlotTableID);
        contentValues.put("intensityPlotTableID", intensityPlotTableID);
        contentValues.put("plotTableID", plotTableID);
        contentValues.put("rmSpot", rmSpot);
        contentValues.put("finalSpot", finalSpot);

        long result = db.insert("ProjectDetails", null, contentValues);
        return result != -1;

    }

    public void deleteLink(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_TABLE, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }


    public void deleteSplitImg(String tableName, String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }


    public Cursor getDatas() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from ProjectDetails", null);
        return cursor;
    }


    public Cursor getSplitTableData(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (Source.oldVersion < 5) {
            if (!checkColumnsExist(tableName)) {

                db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + "volumePlotTableID" + " TEXT DEFAULT 'na';");
                db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + "intensityPlotTableID" + " TEXT DEFAULT 'na';");
                db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + "plotTableID" + " TEXT DEFAULT 'na';");
            }
        }

        String query = "SELECT * FROM " + tableName;
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }

    public boolean checkColumnsExist(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);

        ArrayList<String> arrayOfNewColumns = new ArrayList();
        arrayOfNewColumns.add("volumePlotTableID");
        arrayOfNewColumns.add("intensityPlotTableID");
        arrayOfNewColumns.add("plotTableID");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String columnName = cursor.getString(cursor.getColumnIndex("name"));
                for (String targetColumn : arrayOfNewColumns) {
                    if (columnName.equals(targetColumn)) {
                        cursor.close();
                        return true; // Column exists
                    }
                }
            }
            cursor.close();
        }

        return false; // Column does not exist
    }


    public Cursor getSplitDatas() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from SplitImageData", null);
        return cursor;
    }


    public long updateData(ProjectOfflineData data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("projectName", data.getProjectName());
        contentValues.put("projectDescription", data.getProjectDescription());
        contentValues.put("timeStamp", data.getTimeStamp());
        contentValues.put("projectNumber", data.getProjectNumber());
        contentValues.put("projectImage", data.getProjectImage());
        contentValues.put("imageSplitAvailable", data.getImageSplitAvailable());
        contentValues.put("splitId", data.getSplitId());
        contentValues.put("thresholdVal", data.getThresholdVal());
        contentValues.put("noOfSpots", data.getNoOfSpots());
        contentValues.put("tableName", data.getTableName());
        contentValues.put("roiTableID", data.getRoiTableID());
        contentValues.put("volumePlotTableID", data.getVolumePlotTableID());
        contentValues.put("intensityPlotTableID", data.getIntensityPlotTableID());
        contentValues.put("plotTableID", data.getPlotTableID());
        contentValues.put("rmSpot", data.getRmSpot());
        contentValues.put("finalSpot", data.getFinalSpot());

        //Lets update now
        return db.update(DATABASE_TABLE, contentValues, "id" + "=?",
                new String[]{String.valueOf(data.getId())});


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
}
