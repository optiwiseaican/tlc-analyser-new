package com.aican.tlcanalyzer.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aican.tlcanalyzer.R;
import com.aican.tlcanalyzer.dataClasses.ContourData;
import com.aican.tlcanalyzer.dataClasses.ContourSet;
import com.aican.tlcanalyzer.dataClasses.HrVsAreaPer;
import com.aican.tlcanalyzer.dataClasses.ManualContour;
import com.aican.tlcanalyzer.dataClasses.MultiSplitIntensity;
import com.aican.tlcanalyzer.dataClasses.RFvsArea;
import com.aican.tlcanalyzer.dataClasses.SplitContourData;
import com.aican.tlcanalyzer.dataClasses.SplitData;
import com.aican.tlcanalyzer.dataClasses.userIDPASS.UserData;

import org.opencv.core.Mat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;

public class Source {

    public static boolean SHOW_VOLUME_DATA = false;
    public static boolean SHOW_LABEL_DATA = false;
    public static int SCALING_FACTOR_INT_GRAPH = 2;
    public static String manual_contour_prefix = "M";

    public static ArrayList<ManualContour> manualContourArrayList;

    public static String ANALYSIS_MODE = "SPOT";
    public static int PARTS_INTENSITY = 1000;
    //    public static int MOVING_AVG_WINDOW_SIZE = PARTS_INTENSITY / 25;
    public static float percentRFTop = 1.02f;
    public static float percentRFBottom = 0.9f;
    public static int WIDTH_OF_IMAGE = 0;
    public static int HEIGHT_OF_IMAGE = 0;
    public static boolean workingWithRectangleContour = false;
    public static ArrayList<SplitData> splitDataArrayList;
    public static ArrayList<HrVsAreaPer> hrVsAreaPerArrayListRM;
    public static ArrayList<HrVsAreaPer> hrVsAreaPerArrayListFinal;
    public static int splitPosition = 0;
    public static boolean cfrStatus = false;
    public static String CFR_KEY = "CFR" + UserRoles.UID;
    public static ArrayList<RFvsArea> intensityVsRFArray;
    public static boolean fileSaved = false;
    public static final int CAMERA_REQUEST = 1888;
    public static final int CAMERA_REQUEST_SPLIT = 1889;
    public static ArrayList<MultiSplitIntensity> intensityArrayList = new ArrayList<>();
    //    public static ArrayList<MultiSplitAllData> multiSplitAllDataList = new ArrayList<>();
    public static byte[] byteArr;
    public static ArrayList<Double> intensities;
    public static ArrayList<Double> multiIntensities;
    public static ArrayList<Double> volumeDATA;
    public static ArrayList<Double> volumeDATAROI;
    public static double largest;
    public static double smallest;
    public static Bitmap roiBitmap;
    public static Uri originalImageUri;
    public static ArrayList<RFvsArea> rFvsAreaArrayList;
    public static ArrayList<RFvsArea> rFvsAreaArrayListROI;
    public static ArrayList<ContourSet> contourSetArrayList;
    public static ArrayList<SplitContourData> splitContourDataList;
    public static String DB_NAME = "project_items";
    public static Bitmap contourBitmap;

    public static Mat imageMat;
    public static Uri contourUri;
    public static Bitmap contourBitmapROI;
    public static Bitmap originalBitmap;
    public static Bitmap originalBitmapROI;
    public static ArrayList<ContourData> contourDataArrayList;
    public static ArrayList<ContourData> contourDataArrayListROI;
    public static boolean manual = false;

    //crop code 1 for roi & 2 for manual contour drawing
    public static int CROP_CODE = 0;

    public static int shape = 1;
    public static boolean showContourImg = false;
    public static boolean changeRoi = false;

    public static String userId;
    public static String userPasscode;
    public static ArrayList<String> expiryDate_fetched, dateCreated_fetched;
    public static String logUserName;
    public static String loginUserRole;
    public static ArrayList<UserData> userDataArrayList;
    public static ArrayList<Bitmap> croppedArrayList;
    public static boolean retake = false;
    public static int oldVersion;
    public static int newVersion;
    public static boolean rectangle = false;
    public static boolean rectangleOneActivityToPixelActivity = false;
    public static boolean contourBaselineEdited = false;
    public static boolean hideAnalyserLayout = false;
    public static boolean rectangleContourEdited = false;

    public static int spotPositionFromAdapter = -1;

    public static Rect editRectangleContourRect = null;

    public static String removingContourID = "null";
    public static boolean removingContourFromAdapter = false;

    public static ArrayList<Rect> rectangleList;

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLoadingDialog(AppCompatActivity activity) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false); // Prevent dismissal
        progressDialog.show();

        new Handler().postDelayed(progressDialog::dismiss, 3000); // Auto-dismiss after 3 seconds
    }

    public static String formatToTwoDecimalPlaces(String value) {
        try {
            BigDecimal decimalValue = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
            return decimalValue.toString();
        } catch (NumberFormatException e) {
            return "0.00";  // Default to 0.00 if input is invalid
        }
    }

    public static void checkInternet(Context context) {

        if (!Source.isInternetAvailable(context)) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.no_internet, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setView(dialogView);

            final AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            Button tryAgain = dialogView.findViewById(R.id.tryAgain);
            tryAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Source.isInternetAvailable(context)) {
                        alertDialog.dismiss();
                    } else {
                        Source.toast(context, "No internet connection");
                    }
                }
            });

            alertDialog.setCancelable(false);
            alertDialog.show();
        }

    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            } else {
                @SuppressWarnings("deprecation")
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            }
        }

        return false;
    }


    //////////

    // peak detection

    public static int[] detectPeaks(double[] y, int old_lag, double threshold, double influence, int parts) {
        int lag = (old_lag * parts) / 1000;

        int[] signals = new int[y.length];
        double[] filteredY = new double[lag];
        double[] avgFilter = new double[y.length];
        double[] stdFilter = new double[y.length];
        avgFilter[lag] = mean(Arrays.copyOfRange(y, 0, lag));
        stdFilter[lag] = std(Arrays.copyOfRange(y, 0, lag));

        for (int i = lag + 1; i < y.length; i++) {
            if (Math.abs(y[i] - avgFilter[i - 1]) > threshold * stdFilter[i - 1]) {
                if (y[i] > avgFilter[i - 1]) {
                    signals[i] = 1;
                } else {
                    signals[i] = -1;
                }
                double[] slice = Arrays.copyOfRange(filteredY, Math.max(0, filteredY.length - lag), filteredY.length);
                filteredY = Arrays.copyOf(slice, slice.length + 1);
                filteredY[slice.length] = influence * y[i] + (1 - influence) * filteredY[slice.length - 1];
            } else {
                signals[i] = 0;
                double[] slice = Arrays.copyOfRange(filteredY, Math.max(0, filteredY.length - lag), filteredY.length);
                filteredY = Arrays.copyOf(slice, slice.length + 1);
                filteredY[slice.length] = y[i];
            }
            double[] subarray = Arrays.copyOfRange(filteredY, Math.max(0, filteredY.length - lag), filteredY.length);
            avgFilter[i] = mean(subarray);
            stdFilter[i] = std(subarray);
        }

        return signals;
    }

    // tlc issues:
    //auto crop doesnt work in white background
    //manually crop page, save button is very close to selection area, gets pressed by mistake a lot
    //hour selection dropdown on project home image is very small to click, increase dimensions
    //split image line addition page, image stretches out, need to fix
    //bug: final master report showed 2/3 images only
    //area column in analysis table has 5-10 decimals, limit all decimals to 2 places throughout app
    //add project button overlaps with project settings when page is filled with project tiles
    //main crop of project image should have re-edit option

    public static int[] detectPeaks2(double[] y, int lag, double threshold, double influence) {
        int[] signals = new int[y.length];
        double[] filteredY = new double[lag];
        double[] avgFilter = new double[y.length];
        double[] stdFilter = new double[y.length];

        avgFilter[lag] = mean(Arrays.copyOfRange(y, 0, lag));
        stdFilter[lag] = std(Arrays.copyOfRange(y, 0, lag));

        for (int i = lag + 1; i < y.length; i++) {
            if (Math.abs(y[i] - avgFilter[i - 1]) > threshold * stdFilter[i - 1]) {
                if (y[i] > avgFilter[i - 1]) {
                    signals[i] = 1;
                } else {
                    signals[i] = 0;
                }
                double[] temp = Arrays.copyOf(filteredY, filteredY.length + 1);
                temp[temp.length - 1] = influence * y[i] + (1 - influence) * filteredY[filteredY.length - 1];
                filteredY = temp;
            } else {
                signals[i] = 0;
                double[] temp = Arrays.copyOf(filteredY, filteredY.length + 1);
                temp[temp.length - 1] = y[i];
                filteredY = temp;
            }

            avgFilter[i] = mean(Arrays.copyOfRange(filteredY, Math.max(0, filteredY.length - lag), filteredY.length));
            stdFilter[i] = std(Arrays.copyOfRange(filteredY, Math.max(0, filteredY.length - lag), filteredY.length));
        }

        return signals;
    }

    ////////

    public static double mean(double[] arr) {
        double sum = 0;
        for (double value : arr) {
            sum += value;
        }
        return sum / arr.length;
    }

    public static double std(double[] arr) {
        double mean = mean(arr);
        double sum = 0;
        for (double value : arr) {
            sum += Math.pow(value - mean, 2);
        }
        return Math.sqrt(sum / arr.length);
    }


    /////////

}
