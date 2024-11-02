package com.aican.tlcanalyzer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.aican.tlcanalyzer.CroppingTemp;
import com.aican.tlcanalyzer.NewAutoSplitImage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

public class SharedPrefData {

    /*
         Don't change the key's, unless want to face the conflicts
     */

    public static String PR_LIMIT_KEY = "prLimit" + UserRoles.UID;
    public static String PR_ACTUAL_LIMIT_KEY = "prActLimit" + UserRoles.UID;

    SharedPrefData() {

    }

    public static void saveData(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString(key, value);
        myEdit.commit();
    }

    public static String getSavedData(Context context, String key) {
        SharedPreferences sh = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        return sh.getString(key, "");
    }

    public static String firebaseURL = "https://tlc-" + CroppingTemp.firebaseURL2 + NewAutoSplitImage.firebaseUrl3;

    private static final String api = "8b2d3c9806a66a270be8863182d91275a26767c6";
    private static final String app = "1:217705094036:android:065773e0db5ced6e27cd81";
    private static final String project = "tlc-analyser-0112553";

    public static FirebaseApp getInstance(Context context) {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setProjectId(project)
                .setApplicationId(app)
                .setApiKey(api)
                .setDatabaseUrl(firebaseURL)
                .build();

        try {
            FirebaseApp app = FirebaseApp.initializeApp(context, options, "primary");
            FirebaseDatabase.getInstance(app).setPersistenceEnabled(true);

        } catch (IllegalStateException e) {
            //Ignore
        }

        return FirebaseApp.getInstance("primary");
    }

}
