package com.aican.tlcanalyzer.dialog;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aican.tlcanalyzer.ProjectView;
import com.aican.tlcanalyzer.R;
import com.aican.tlcanalyzer.database.UsersDatabase;
import com.aican.tlcanalyzer.utils.Source;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AuthDialog {

    public static String activeUserName = "no-user";
    public static String activeUserRole = "no-role";

    public static String projectType = "na";
    public static String projectName = "na";
    public static String projectID = "na";

    public static Dialog loadingDialog;


    public static void authDialog(Activity context, boolean cancelable,
                                  boolean cancelOnTouchOutside, UsersDatabase databaseHelper, AuthCallback authCallback) {
        if (!context.isFinishing()) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                cancelDialog();
            }
            loadingDialog = new Dialog(context);
            LayoutInflater layoutInflater = context.getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.auth_dialog, null);
            loadingDialog.setContentView(view);
            try {
//                loadingDialog.getWindow().setDimAmount(0);
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


//                AlertDialog.Builder builder = new AlertDialog.Builder(context);


                EditText userID, passcode;
                Button authenticate;
                TextView homeBtn;


                userID = view.findViewById(R.id.userID);
                passcode = view.findViewById(R.id.password);
                authenticate = view.findViewById(R.id.authenticateRole);
                homeBtn = view.findViewById(R.id.homeBtn);


                homeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        context.startActivity(new Intent(context, ProjectView.class));
                        context.finish();
                    }
                });

                authenticate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        String userId = userID.getText().toString();
//                        String userId = "v";
                        String userPasscode = passcode.getText().toString();
//                        String userPasscode = "v";

                        Date date = Calendar.getInstance().getTime();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        String presentDate = dateFormat.format(date);

                        Date present = getParsedDate(presentDate);

//                        Source.toast(context, Source.userDataArrayList.size() + "");

                        if (Source.userDataArrayList != null) {

                            for (int i = 0; i < Source.userDataArrayList.size(); i++) {
                                if (userId.equals(Source.userDataArrayList.get(i).getId()) &&
                                        userPasscode.equals(Source.userDataArrayList.get(i).getPasscode())
                                ) {

                                    if (!Source.userDataArrayList.get(i).getExpiryDate().equals("na")) {

                                        if (present.compareTo(getParsedDate(Source.userDataArrayList.get(i).getExpiryDate())) < 0) {


                                            Source.logUserName = Source.userDataArrayList.get(i).getName();
                                            Source.loginUserRole = Source.userDataArrayList.get(i).getRole();

                                            AuthDialog.activeUserName = Source.userDataArrayList.get(i).getName();
                                            AuthDialog.activeUserRole = Source.userDataArrayList.get(i).getRole();

                                            SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor myShared = sharedPreferences.edit();

                                            myShared.putString("userid", Source.userId);
                                            myShared.commit();

                                            Source.userId = userID.getText().toString();
                                            Source.userPasscode = passcode.getText().toString();

                                            authCallback.onAuthenticationSuccess();

                                            cancelDialog();

                                            return;
                                        } else {
                                            Toast.makeText(context, "Password expired, please change it", Toast.LENGTH_SHORT).show();
                                            Log.d("expiryDate", "Present date :" + presentDate + " Expiry Date: " + Source.expiryDate_fetched.get(i));
                                        }
                                    } else {

                                        Source.logUserName = Source.userDataArrayList.get(i).getName();
                                        Source.loginUserRole = Source.userDataArrayList.get(i).getRole();

                                        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor myShared = sharedPreferences.edit();

                                        myShared.putString("userid", Source.userId);
                                        myShared.commit();

                                        Source.userId = userID.getText().toString();
                                        Source.userPasscode = passcode.getText().toString();

                                        authCallback.onAuthenticationSuccess();

                                        cancelDialog();

                                        return;
                                    }
                                }
                            }
                            Toast.makeText(context, "Access Not Granted", Toast.LENGTH_SHORT).show();
                        } else {
                            Source.toast(context, "No user added");
                        }
                    }
                });

            } catch (Exception e) {
                String TAG = "ShowLoading";
                Log.d(TAG, "showLoading: " + e.getMessage());
            }
            loadingDialog.setCanceledOnTouchOutside(cancelOnTouchOutside);
            loadingDialog.setCancelable(cancelable);
            loadingDialog.show();
        }
    }

    public static void cancelDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            try {
                loadingDialog.cancel();
                loadingDialog = null;
            } catch (Exception e) {
                String TAG = "ShowLoading";

                Log.d(TAG, "cancelLoading: " + e.getMessage());
            }
        }
    }

    public interface AuthCallback {
        void onAuthenticationSuccess();
    }

    public static String getPresentDate() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String presentDate = dateFormat.format(date);
        return presentDate;
    }

    private static Date getParsedDate(String date) {
        Date presentDt = null;
        try {
            presentDt = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    .parse(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return presentDt;
    }
}
