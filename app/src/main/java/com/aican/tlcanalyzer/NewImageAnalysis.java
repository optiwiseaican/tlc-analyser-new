package com.aican.tlcanalyzer;


import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.aican.tlcanalyzer.adapterClasses.ContourIntGraphAdapter;
import com.aican.tlcanalyzer.adapterClasses.ContourListAdapter;
import com.aican.tlcanalyzer.adapterClasses.ManualContourListAdapter;
import com.aican.tlcanalyzer.cropper.CropImage;
import com.aican.tlcanalyzer.cropper.CropImageView;
import com.aican.tlcanalyzer.customClasses.LegacyTableView;
import com.aican.tlcanalyzer.dataClasses.ContourData;
import com.aican.tlcanalyzer.dataClasses.ContourGraphSelModel;
import com.aican.tlcanalyzer.dataClasses.ContourSet;
import com.aican.tlcanalyzer.dataClasses.ContourWithID;
import com.aican.tlcanalyzer.dataClasses.LabelData;
import com.aican.tlcanalyzer.dataClasses.ManualContour;
import com.aican.tlcanalyzer.dataClasses.MultiSplitIntensity;
import com.aican.tlcanalyzer.dataClasses.ProjectOfflineData;
import com.aican.tlcanalyzer.dataClasses.RFvsArea;
import com.aican.tlcanalyzer.dataClasses.SplitContourData;
import com.aican.tlcanalyzer.dataClasses.SplitData;
import com.aican.tlcanalyzer.dataClasses.XY;
import com.aican.tlcanalyzer.database.DatabaseHelper;
import com.aican.tlcanalyzer.database.UsersDatabase;
import com.aican.tlcanalyzer.databinding.ActivityNewImageAnalysisBinding;
import com.aican.tlcanalyzer.dialog.AuthDialog;
import com.aican.tlcanalyzer.dialog.LoadingDialog;
import com.aican.tlcanalyzer.interfaces.OnClicksListeners;
import com.aican.tlcanalyzer.interfaces.RemoveContourInterface;
import com.aican.tlcanalyzer.settingActivities.SplitSettings;
import com.aican.tlcanalyzer.specialHelperClasses.ImageAnalysisClass;
import com.aican.tlcanalyzer.utils.SharedPrefData;
import com.aican.tlcanalyzer.utils.Source;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jsibbold.zoomage.ZoomageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class NewImageAnalysis extends AppCompatActivity implements RemoveContourInterface, AuthDialog.AuthCallback, OnClicksListeners {

    public ArrayList<RFvsArea> intensityVsRFArray;
    public Uri grayImgUri;
    String detectedContourImageName;
    Uri imageUri;
    File outputDirectory;
    ZoomageView captured_image;
    CardView getPixels;
    String path = "";
    Mat rgba;
    Mat image;
    Mat grayImage;
    Bitmap resizedBitmap;
    Mat invertedImage;
    Button spotContours;
    CardView getIntensityGraph;
    Button regionOfInterest;
    SeekBar setThreshold;
    TextView thresholdValue;
    TextView rfValues;
    int threshVal = 100;
    ArrayList<ContourData> contourDataArrayList;
    //    ContourDataAdapter contourDataAdapter;
    ArrayList<Double> volumeArrayList;
    TextView numberCountText;
    int numberCount = 4;
    List<MatOfPoint> refinedContour;
    ArrayList<RFvsArea> rFvsAreaArrayList;
    ArrayList<LabelData> labelDataArrayList;
    ArrayList<RFvsArea> mrFvsAreaArrayList;
    ArrayList<ContourSet> contourSetArrayList;
    Button saveData;
    String projectName;
    String projectDescription;
    String projectImage;
    String id;
    String splitId;
    String projectNumber;
    String thresholdVal;
    String numberOfSpots;
    String tableName;
    String imageSplitAvailable;
    String contourImage;
    String timeStamp;
    String work;
    String roiTableID;
    String[] works = {"new", "existing", "split"};
    File dir;
    DatabaseHelper databaseHelper;
    int newThreshold;
    String newThresholdString;
    ActivityNewImageAnalysisBinding binding;
    SeekBar noOfCounts;
    LegacyTableView legacyTableView;
    String volumePlotTableID, intensityPlotTableID, plotTableID;
    Bitmap autoContBitmap = null;
    Bitmap manuContBitmap = null;
    int k = 0;
    Bitmap bit = null;
    ArrayList<ManualContour> manualContourArrayList = new ArrayList<>();
    Stack<Drawable> drawnShapesStack = new Stack<>();
    View dialogView;
    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    String fileName = String.format("%d.jpg", System.currentTimeMillis());
    String contourImageFileName;
    List<MatOfPoint> contours;

    public static Bitmap GRAYSCALE_BITMAP;

    Bitmap originalBitmapForPixel;
    Mat grayScaleImage;
    Bitmap bitImage = null;
    ArrayList<Integer> contourList;
    List<MatOfPoint> contourWithDataList;
    Mat adjustedImage;
    boolean processed = true;
    final Bitmap[] bitImageArray = {null};

    float influence = 0.3f;
    float threshold = 0.5f;
    int lag = 100;

    ArrayList<Pair<Integer, Integer>> highlightedRegions;
    private ArrayList<Entry> information;

    private LineDataSet lineDataSet = new LineDataSet(null, null);
    private ArrayList<ILineDataSet> iLineDataSets = new ArrayList<>();
    private ArrayList<ContourData> contourDataArrayListNew = new ArrayList<>();
    private String mode;
    private LineData lineData;

    int[] thresArray = {50, 100, 150, 200, 250};

    UsersDatabase usersDatabase;

    Bitmap mainImageBitmap = null;
    Bitmap roiImageBitmap = null;

    String hour = "0";
    String rmSpot = "-1000";
    String finalSpot = "-1000";

    String hr = "0";

    String contourJsonFileName = "null";


    ImageAnalysisClass imageAnalysisClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewImageAnalysisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        imageAnalysisClass = new ImageAnalysisClass(this);


        usersDatabase = new UsersDatabase(this);

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().getStringExtra("w").toString().equals("split")) {
                    refreshMainSplitImage = 1;
                }
                Source.retake = false;

                finish();
            }
        });


//        imageUri = Uri.parse(getIntent().getStringExtra("imageUri"));
        getIntensityGraph = binding.plotL.intensityPlot;
        thresholdValue = binding.anaL.spotDetection.thresholdValue;
        setThreshold = binding.anaL.spotDetection.setThreshold;
        captured_image = binding.capturedImage;
        getPixels = binding.plotL.volumePlot;
        regionOfInterest = findViewById(R.id.regionOfInterest);
        spotContours = binding.anaL.spotDetection.spotContours;
        legacyTableView = (LegacyTableView) findViewById(R.id.legacy_table_view);
        numberCountText = binding.anaL.spotDetection.noOfSpots;
        noOfCounts = binding.anaL.spotDetection.noOfCounts;

        saveData = findViewById(R.id.saveData);
        refinedContour = new ArrayList<>();
        rFvsAreaArrayList = new ArrayList<>();
        labelDataArrayList = new ArrayList<>();
        contourSetArrayList = new ArrayList<>();
        path = getIntent().getStringExtra("img_path");
        imageUri = Uri.parse(path);
        roiTableID = getIntent().getStringExtra("roiTableID");
        hour = getIntent().getStringExtra("hour");
        rmSpot = getIntent().getStringExtra("rmSpot");
        finalSpot = getIntent().getStringExtra("finalSpot");


        volumePlotTableID = getIntent().getStringExtra("volumePlotTableID");
        intensityPlotTableID = getIntent().getStringExtra("intensityPlotTableID");
        plotTableID = getIntent().getStringExtra("plotTableID");
        hr = getIntent().getStringExtra("hour");


        if (getIntent().getStringExtra("w").toString().equals("existing")) {
            AuthDialog.projectType = "Normal";
            Source.originalImageUri = Uri.fromFile(new File(path));
        }

        if (getIntent().getStringExtra("w").toString().equals("new")) {
            AuthDialog.projectType = "Normal";

            Source.originalImageUri = Uri.parse(path);
        }

        if (getIntent().getStringExtra("w").toString().equals("split")) {
            AuthDialog.projectType = "Split";
            Source.originalImageUri = Uri.fromFile(new File(path));
        }
        binding.anaL.addDone.setVisibility(View.GONE);

        String type = "";
        type = getIntent().getStringExtra("type").toString();
//        Source.checkInternet(this);


        if (type.equals("multi") || type.equals("mainImg")) {
            binding.anaL.addDone.setVisibility(View.VISIBLE);

            String finalType = type;
            binding.anaL.addDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (contourDataArrayList == null || contourDataArrayList.size() < 0) {
//                        Source.toast(NewImageAnalysis.this, "First Spot then add this");
                    } else {
                        if (finalType.equals("multi")) {
                            MultiSplitIntensity multiSplitIntensity = new MultiSplitIntensity(getIntent().getStringExtra("imageName"), true, Source.rFvsAreaArrayList);
                            Source.intensityArrayList.add(multiSplitIntensity);
                        }
                        if (finalType.equals("mainImg")) {
                            MultiSplitIntensity multiSplitIntensity = new MultiSplitIntensity("Main Image", true, Source.rFvsAreaArrayList);
                            Source.intensityArrayList.add(multiSplitIntensity);
                        }
                        finish();
                    }
                }
            });

            binding.plotL.intensityPlot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }

        databaseHelper = new DatabaseHelper(this);

        outputDirectory = imageAnalysisClass.getOutputDirectory();

        projectName = getIntent().getStringExtra("projectName");
        projectDescription = getIntent().getStringExtra("projectDescription");
        timeStamp = getIntent().getStringExtra("timeStamp");
        projectImage = getIntent().getStringExtra("projectImage");
        id = getIntent().getStringExtra("id");
        splitId = getIntent().getStringExtra("splitId");
        projectNumber = getIntent().getStringExtra("projectNumber");
        newThresholdString = getIntent().getStringExtra("thresholdVal");
        numberOfSpots = getIntent().getStringExtra("numberOfSpots");
        imageSplitAvailable = getIntent().getStringExtra("imageSplitAvailable");
        contourImage = getIntent().getStringExtra("contourImage");
        tableName = getIntent().getStringExtra("tableName");


        binding.projectName.setText(projectName);
        binding.projectName.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.project_details, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(NewImageAnalysis.this).setView(dialogView);


            AlertDialog alertDialog = builder.create();

            TextView projectNameD = dialogView.findViewById(R.id.projectNameD);
            TextView projectDescriptionD = dialogView.findViewById(R.id.projectDescriptionD);

            projectNameD.setText(projectName);
            projectDescriptionD.setText(projectDescription);


            alertDialog.show();
        });

        work = getIntent().getStringExtra("w");
        binding.analyzerLine.setVisibility(View.VISIBLE);
        binding.roiLine.setVisibility(View.GONE);
        binding.plotterLine.setVisibility(View.GONE);
        binding.anaL.analyzerLayout.setVisibility(View.VISIBLE);
        binding.roiL.roiLayout.setVisibility(View.GONE);
        binding.plotL.plotterLayout.setVisibility(View.GONE);
        binding.analyzerLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.analyzerLine.setVisibility(View.VISIBLE);
                binding.roiLine.setVisibility(View.GONE);
                binding.plotterLine.setVisibility(View.GONE);
                binding.anaL.analyzerLayout.setVisibility(View.VISIBLE);
                binding.roiL.roiLayout.setVisibility(View.GONE);
                binding.plotL.plotterLayout.setVisibility(View.GONE);

            }
        });

        binding.roiLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Source.changeRoi = true;
                Source.CROP_CODE = 1;
                CropImage.activity(Source.originalImageUri).setGuidelines(CropImageView.Guidelines.ON).setCropShape(CropImageView.CropShape.RECTANGLE).start(NewImageAnalysis.this);

            }
        });

        binding.analysisButtonNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.detectButtonNew.setCardBackgroundColor(Color.WHITE);
                binding.analysisButtonNew.setCardBackgroundColor(Color.LTGRAY);
                binding.anaL.analyzerLayout.setVisibility(View.GONE);
                binding.roiL.roiLayout.setVisibility(View.GONE);
                binding.plotL.plotterLayout.setVisibility(View.VISIBLE);
            }
        });


        binding.detectButtonNew.setCardBackgroundColor(Color.LTGRAY);
        binding.analysisButtonNew.setCardBackgroundColor(Color.WHITE);

        binding.detectButtonNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.detectButtonNew.setCardBackgroundColor(Color.LTGRAY);
                binding.analysisButtonNew.setCardBackgroundColor(Color.WHITE);
                binding.anaL.analyzerLayout.setVisibility(View.VISIBLE);
                binding.roiL.roiLayout.setVisibility(View.GONE);
                binding.plotL.plotterLayout.setVisibility(View.GONE);
            }
        });

        binding.plotterLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.analyzerLine.setVisibility(View.GONE);
                binding.roiLine.setVisibility(View.GONE);
                binding.plotterLine.setVisibility(View.VISIBLE);

                binding.anaL.analyzerLayout.setVisibility(View.GONE);
                binding.roiL.roiLayout.setVisibility(View.GONE);
                binding.plotL.plotterLayout.setVisibility(View.VISIBLE);

            }
        });


        if (work.equals(works[2])) {
            contourImageFileName = "CONT" + getIntent().getStringExtra("pid") + ".png";

        } else {
            contourImageFileName = "CONT" + id + ".png";

        }


        spotContours.setEnabled(false);
        getPixels.setEnabled(false);

        dir = new File(new ContextWrapper(this).getExternalMediaDirs()[0], getResources().getString(R.string.app_name) + id);
        setThreshold.setMax(255);

        if (work.equals(works[0])) {
            newThreshold = 100;
            setThreshold.setProgress(newThreshold);
            threshVal = 100;

            noOfCounts.setProgress(numberCount);
            numberCountText.setText("No. of Spots : " + numberCount);

        }

        UsersDatabase userDatabase = new UsersDatabase(this);


        if (work.equals(works[1])) {

            if (Source.cfrStatus) {
                AuthDialog.authDialog(this, false, false, userDatabase, this);
            }

            newThreshold = Integer.parseInt(newThresholdString);
            threshVal = Integer.parseInt(newThresholdString);

            setThreshold.setProgress(newThreshold);
            thresholdValue.setText("Threshold : " + newThresholdString);
            numberCount = Integer.parseInt(getIntent().getStringExtra("numberOfSpots"));
            numberCountText.setText("No. of Spots : " + getIntent().getStringExtra("numberOfSpots"));
            noOfCounts.setProgress(numberCount);
        }
        if (work.equals(works[2])) {
            newThreshold = Integer.parseInt(newThresholdString);
            setThreshold.setProgress(newThreshold);
            threshVal = Integer.parseInt(newThresholdString);

            thresholdValue.setText("Threshold : " + newThresholdString);

            numberCount = Integer.parseInt(getIntent().getStringExtra("numberOfSpots"));
            numberCountText.setText("No. of Spots : " + getIntent().getStringExtra("numberOfSpots"));
            noOfCounts.setProgress(numberCount);

        }
        noOfCounts.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                numberCount = progress;
                numberCountText.setText("No. of Spots : " + numberCount);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.anaL.spotDetection.decrementSpots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberCount = numberCount - 1;
                numberCountText.setText("No. of Spots : " + numberCount);
                noOfCounts.setProgress(numberCount);
            }
        });

        binding.anaL.spotDetection.incrementSpots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberCount = numberCount + 1;
                numberCountText.setText("No. of Spots : " + numberCount);
                noOfCounts.setProgress(numberCount);
            }
        });


        setThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                newThreshold = progress;
                threshVal = progress;

                newThresholdString = String.valueOf(newThreshold);
                thresholdValue.setText("Threshold : " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.anaL.spotDetection.decrementThres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newThreshold = newThreshold - 1;

                thresholdValue.setText("Threshold : " + newThreshold);

                setThreshold.setProgress(newThreshold);

            }
        });

        binding.anaL.spotDetection.incrementThres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newThreshold = newThreshold + 1;

                thresholdValue.setText(String.format("Threshold : %d", newThreshold));

                setThreshold.setProgress(newThreshold);

            }
        });

        final int[] ik = {0};
        int thresArraySize = thresArray.length;
        binding.anaL.spotDetection.thresArrayBtn.setText(newThreshold + "");

        binding.anaL.spotDetection.thresArrayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ik[0] <= thresArraySize) {

                    if (ik[0] == thresArraySize) {
                        ik[0] = 0;
                        newThreshold = thresArray[ik[0]];
                        thresholdValue.setText("Threshold : " + newThreshold + "");
                        binding.anaL.spotDetection.thresArrayBtn.setText(newThreshold + "");

                        setThreshold.setProgress(newThreshold);
                    } else {
                        newThreshold = thresArray[ik[0]];
                        thresholdValue.setText("Threshold : " + newThreshold);
                        binding.anaL.spotDetection.thresArrayBtn.setText(newThreshold + "");
                        setThreshold.setProgress(newThreshold);
                        ik[0]++;
                    }
                }

            }
        });

        thresholdValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = getLayoutInflater().inflate(R.layout.edit_any_thing, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(NewImageAnalysis.this).setView(dialogView);


                AlertDialog alertDialog = builder.create();

                TextView editName = dialogView.findViewById(R.id.editName);
                EditText getValue = dialogView.findViewById(R.id.getValue);
                Button submitBtnD = dialogView.findViewById(R.id.submitBtnD);

                editName.setText("Enter Threshold Value");

                submitBtnD.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getValue.getText().toString() != null && !getValue.getText().toString().equals("") && Integer.parseInt(getValue.getText().toString()) <= 256) {
                            setThreshold.setProgress(Integer.parseInt(getValue.getText().toString()));
                            thresholdValue.setText("Threshold : " + getValue.getText().toString());
                            alertDialog.dismiss();
                        } else {
//                            Toast.makeText(NewImageAnalysis.this, "Enter the valid value", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                alertDialog.show();
            }
        });


        binding.anaL.spotDetection.noOfSpots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = getLayoutInflater().inflate(R.layout.edit_any_thing, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(NewImageAnalysis.this).setView(dialogView);


                AlertDialog alertDialog = builder.create();

                TextView editName = dialogView.findViewById(R.id.editName);
                EditText getValue = dialogView.findViewById(R.id.getValue);
                Button submitBtnD = dialogView.findViewById(R.id.submitBtnD);

                editName.setText("Enter No of Spots Value");

                submitBtnD.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getValue.getText().toString() != null && !getValue.getText().toString().equals("") && Integer.parseInt(getValue.getText().toString()) <= 100) {
                            noOfCounts.setProgress(Integer.parseInt(getValue.getText().toString()));
                            numberCountText.setText("No. of Spots : " + getValue.getText().toString());
                            alertDialog.dismiss();
                        } else {
//                            Toast.makeText(NewImageAnalysis.this, "Enter the valid value", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                alertDialog.show();
            }

        });

        volumeArrayList = new ArrayList<>();

        Source.volumeDATA = new ArrayList<>();

        Source.rFvsAreaArrayList = new ArrayList<>();

        rFvsAreaArrayList = new ArrayList<>();

        getPixels.setOnClickListener(v -> {

            if (Source.volumeDATA.size() == 0) {
//                Toast.makeText(this, "No data available to plot data", Toast.LENGTH_SHORT).show();
            } else {
                Intent i = new Intent(NewImageAnalysis.this, VolumeGraph.class);
                i.putExtra("id", id);
                i.putExtra("i", "withoutRoi");
                i.putExtra("projectName", projectName);
                i.putExtra("contourImage", contourImageFileName);
                startActivity(i);
            }


        });


        spotContours.setOnClickListener(v -> {

            if (numberCount > 0) {
                if (Source.volumeDATA != null) {
                    Source.volumeDATA.clear();
                }


                contourDataArrayList.clear();
                labelDataArrayList.clear();


                spotContour();

                settingLabelData();

            } else {
                Toast.makeText(this, "No. of spots can't be zero", Toast.LENGTH_SHORT).show();
            }


        });


        getIntensityGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                plotIntensityGraph(grayImage, refinedContour);5645

//                imageAnalysisClass.plotIntensityGraph(projectName, id);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    performAnalysis(1);
                    AnalysisTask task = new AnalysisTask();
                    task.execute(1);


                }
            }
        });

        binding.anaL.graphDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAnalysisClass.plotIntensityGraphAnalysisAndSpot(projectName, id, 0);

            }
        });

        binding.anaL.autoGraphDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAnalysisClass.plotIntensityGraphAnalysisAndSpot(projectName, id, 1);

            }
        });

        binding.splitSettings.setVisibility(View.GONE);
        if (work.equals(works[0]) || work.equals(works[1])) {
            binding.splitSettings.setVisibility(View.VISIBLE);

            String INTENSITY_PART_KEY = "INTENSITY_PART_KEY_" + id;

            if (SharedPrefData.getSavedData(
                    NewImageAnalysis.this,
                    INTENSITY_PART_KEY
            ) != null && SharedPrefData.getSavedData(
                    NewImageAnalysis.this,
                    INTENSITY_PART_KEY
            ) != ""
            ) {
                String data =
                        SharedPrefData.getSavedData(NewImageAnalysis.this, INTENSITY_PART_KEY);

                Source.PARTS_INTENSITY = Integer.parseInt(data);

            } else {
                SharedPrefData.saveData(NewImageAnalysis.this, INTENSITY_PART_KEY, "1000");
                Source.PARTS_INTENSITY = 1000;
            }


            binding.splitSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SplitImage.Companion.setINTENSITY_PART_KEY(INTENSITY_PART_KEY);
                    Intent i = new Intent(NewImageAnalysis.this, SplitSettings.class);
                    i.putExtra("id", getIntent().getStringExtra("id"));
                    i.putExtra("projectName", getIntent().getStringExtra("projectName"));
                    startActivity(i);
                }
            });

        }

        //


        if (work.equals(works[0])) {
            try {
                bitImageArray[0] = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(path));

            } catch (IOException e) {
                e.printStackTrace();
            }

            String fileName2 = "";
            if (bitImageArray[0] != null) {
                fileName2 = imageAnalysisClass.saveImageViewToFile(bitImageArray[0], fileName, id);
            }

            String tempFileName = "TEMP" + fileName2;

            mainImageBitmap = bitImageArray[0];

            imageAnalysisClass.saveImageViewToFile(mainImageBitmap, tempFileName, id, works, work);

//
//            thresholdVal = String.valueOf(threshVal);
            numberOfSpots = String.valueOf(numberCount);

            ProjectOfflineData projectOfflineData = new ProjectOfflineData(id, projectName, projectDescription, timeStamp,
                    projectNumber, fileName2, imageSplitAvailable, splitId,
                    newThresholdString, numberOfSpots, tableName, roiTableID,
                    volumePlotTableID, intensityPlotTableID, plotTableID, rmSpot, finalSpot);

            float i = databaseHelper.updateData(projectOfflineData);
//            Toast.makeText(this, "" + getIntent().getStringExtra("id"), Toast.LENGTH_SHORT).show();
        } else {
            try {
                mainImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(dir, "TEMP" + projectImage)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }


        // save data end

        Log.e("Vol", volumePlotTableID);
        Log.e("int", intensityPlotTableID);
        Log.e("table", plotTableID);

        saveData.setOnClickListener(v -> {
//            Bitmap bitImage = null;

            if (work.equals(works[0])) {
                try {
                    bitImageArray[0] = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(path));

                } catch (IOException e) {
                    e.printStackTrace();
                }

                String fileName2 = "";
                if (bitImageArray[0] != null) {
                    fileName2 = imageAnalysisClass.saveImageViewToFile(bitImageArray[0], fileName, id);
                }

//
//            thresholdVal = String.valueOf(threshVal);
                numberOfSpots = String.valueOf(numberCount);

                ProjectOfflineData projectOfflineData =
                        new ProjectOfflineData(id, projectName, projectDescription, timeStamp, projectNumber,
                                fileName2, imageSplitAvailable, splitId, newThresholdString,
                                numberOfSpots, tableName, roiTableID, volumePlotTableID,
                                intensityPlotTableID, plotTableID, rmSpot, finalSpot);

                float i = databaseHelper.updateData(projectOfflineData);
//                Toast.makeText(this, "" + getIntent().getStringExtra("id"), Toast.LENGTH_SHORT).show();
            }

            if (work.equals(works[1])) {
                File outFile = new File(dir, projectImage);
                if (outFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

//                captured_image.setImageBitmap(myBitmap);
                    bitImageArray[0] = myBitmap;

                } else {
//                    Source.toast(this, "Image not available or deleted");
                }
                String fileName2 = "";
                if (bitImageArray[0] != null) {
                    fileName2 = imageAnalysisClass.saveImageViewToFile(bitImageArray[0], fileName, id);
                }

//            thresholdVal = String.valueOf(threshVal);
                numberOfSpots = String.valueOf(numberCount);

                if (outFile.exists()) {
                    ProjectOfflineData projectOfflineData =
                            new ProjectOfflineData(id, projectName, projectDescription, timeStamp,
                                    projectNumber, projectImage, imageSplitAvailable, splitId,
                                    newThresholdString, numberOfSpots, tableName, roiTableID,
                                    volumePlotTableID, intensityPlotTableID, plotTableID, rmSpot, finalSpot);

                    float i = databaseHelper.updateData(projectOfflineData);
//                    Toast.makeText(this, "" + getIntent().getStringExtra("id"), Toast.LENGTH_SHORT).show();
                } else {
//                    Source.toast(this, "Image not available or deleted");
                }


            }

            if (work.equals(works[2])) {
                numberOfSpots = String.valueOf(numberCount);

                File outFile = new File(dir, projectImage);
                if (outFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

//                captured_image.setImageBitmap(myBitmap);
                    bitImageArray[0] = myBitmap;

                    SplitData splitData = new SplitData(getIntent().getStringExtra("pid"),
                            getIntent().getStringExtra("imageName"), projectImage, getIntent().getStringExtra("timeStamp"), newThresholdString, numberOfSpots, roiTableID,
                            volumePlotTableID, intensityPlotTableID, plotTableID, projectDescription, hour, rmSpot, finalSpot);

                    long i = databaseHelper.updateSplitData(splitData, tableName);

//                    Toast.makeText(this, "" + getIntent().getStringExtra("id") + " " + i, Toast.LENGTH_SHORT).show();


                } else {
//                    Source.toast(this, "Image not available or deleted");
                }


            }


        });

        binding.plotL.plotTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Source.contourDataArrayList == null || Source.contourDataArrayList.size() == 0) {
//                    Source.toast(NewImageAnalysis.this, "First Spot then plot the table");
                } else {

                    startActivity(new Intent(NewImageAnalysis.this, PlotTable.class).putExtra("projectName", projectName).putExtra("id", id));

//                    plotTable();
                }
            }
        });


        String finalType1 = type;
        binding.plotL.generateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (finalType1.equals("mainImg")) {
                    if (Source.splitContourDataList.isEmpty()) {
//                        Toast.makeText(NewImageAnalysis.this, "No data available", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent i = new Intent(NewImageAnalysis.this, PlotMultipleIntensity.class);
                        i.putExtra("w", "split");
                        i.putExtra("img_path", getIntent().getStringExtra("img_path"));
                        i.putExtra("projectName", getIntent().getStringExtra("projectName"));
                        i.putExtra("projectDescription", getIntent().getStringExtra("projectDescription"));
                        i.putExtra("projectImage", getIntent().getStringExtra("projectImage"));
                        i.putExtra("imageName", getIntent().getStringExtra("imageName"));
                        i.putExtra("tableName", tableName);
                        i.putExtra("contourImage", contourImageFileName);
                        i.putExtra("roiTableID", getIntent().getStringExtra("roiTableID"));
                        i.putExtra("thresholdVal", getIntent().getStringExtra("thresholdVal"));
                        i.putExtra("numberOfSpots", getIntent().getStringExtra("numberOfSpots"));
                        i.putExtra("id", getIntent().getStringExtra("id"));
                        i.putExtra("pid", getIntent().getStringExtra("pid"));
                        startActivity(i);
                    }
                } else {

                    if (Source.contourDataArrayList == null || Source.contourDataArrayList.size() == 0 || Source.volumeDATA == null || Source.volumeDATA.size() == 0 || Source.rFvsAreaArrayList == null || Source.rFvsAreaArrayList.size() == 0 || Source.contourBitmap == null || Source.contourDataArrayList == null || Source.contourDataArrayList.size() == 0) {
//                        Source.toast(NewImageAnalysis.this, "First Spot then generate the report");
                    } else {

                        Intent i = new Intent(NewImageAnalysis.this, ReportGenerate.class);
                        i.putExtra("id", id);
                        i.putExtra("i", "withoutRoi");
                        i.putExtra("projectName", projectName);
                        i.putExtra("plotTableID", plotTableID);
                        i.putExtra("contourImage", contourImageFileName);

                        File f = new File(dir, projectImage);
                        if (f.exists()) {
                            if (work.equals(works[0])) {

//                            Source.originalBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());

                            } else {
                                Source.originalBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                            }
                        } else {
                            try {
                                Source.originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(path));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (!work.equals(works[0])) {
//                                Toast.makeText(NewImageAnalysis.this, "Project image not exist", Toast.LENGTH_SHORT).show();
                            }
                        }
                        startActivity(i);

                    }
                }
            }
        });
        binding.splitProjectName.setVisibility(View.GONE);

        if (work.equals(works[2])) {
            binding.splitProjectName.setVisibility(View.VISIBLE);
            binding.splitProjectName.setText(getIntent().getStringExtra("splitProjectName").toString());

        }
        binding.roiL.regionOfInterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contourDataArrayList == null || contourDataArrayList.size() == 0) {
//                    Source.toast(NewImageAnalysis.this, "First Spot then plot the table");
                } else {
                    Intent i = new Intent(NewImageAnalysis.this, RegionOfInterest.class);
//                i.putExtra("path", grayImgUri.toString());
                    i.putExtra("projectName", projectName);
                    File f = new File(dir, projectImage);
                    if (f.exists()) {
                        if (work.equals(works[0])) {

//                            Source.originalBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());

                        } else {
                            Source.originalBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                        }
                    } else {
                        try {
                            Source.originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(path));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (!work.equals(works[0])) {
//                            Toast.makeText(NewImageAnalysis.this, "Project image not exist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    startActivity(i);

                }
            }
        });

        binding.removeContour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((contourList != null && !contourList.isEmpty()) || (manualContourArrayList != null && !manualContourArrayList.isEmpty())) {
                    dialogView = getLayoutInflater().inflate(R.layout.remove_contour, null);
                    builder = new AlertDialog.Builder(NewImageAnalysis.this).setView(dialogView);


                    alertDialog = builder.create();

                    RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
                    RecyclerView recyclerView2 = dialogView.findViewById(R.id.recyclerView2);

                    if (contourList != null) {
                        ContourListAdapter contourListAdapter1 = new ContourListAdapter(NewImageAnalysis.this, contourList, NewImageAnalysis.this);
                        recyclerView.setAdapter(contourListAdapter1);
                    }
                    if (manualContourArrayList != null) {
                        ManualContourListAdapter contourListAdapter2 = new ManualContourListAdapter(NewImageAnalysis.this, manualContourArrayList, NewImageAnalysis.this);
                        recyclerView2.setAdapter(contourListAdapter2);
                    }
                    alertDialog.show();
                } else {
//                    Toast.makeText(NewImageAnalysis.this, "First spot", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.anaL.spotDetection.drawSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Source.showContourImg = true;
                if (Source.roiBitmap != null && contourList != null && !contourList.isEmpty()) {
//                    Intent i = new Intent(NewImageAnalysis.this, DrawContourManually.class);
//                    i.putExtra("id", id);
//                    i.putExtra("imageFileName", contourImageFileName);
//                    startActivity(i);
                    setupDrawSpotListener(2);
                } else {
                    if (Source.contourDataArrayList == null) {
                        Source.contourDataArrayList = new ArrayList<>();
                    }
                    if (Source.rFvsAreaArrayList == null) {
                        Source.rFvsAreaArrayList = new ArrayList<>();
                    }
                    if (Source.volumeDATA == null) {
                        Source.volumeDATA = new ArrayList<>();
                    }

                    if (Source.roiBitmap != null) {

                        if (Source.volumeDATA.isEmpty()) {
                            imageAnalysisClass.saveImageViewToFile(Source.roiBitmap, contourImageFileName, id, works, work);

                            File outFile = new File(dir, contourImageFileName);
                            if (outFile.exists()) {
                                Source.contourUri = Uri.fromFile(new File(outFile.getAbsolutePath()));
                            }

//
                            setupDrawSpotListener(2);
                        } else {

                            File outFile = new File(dir, contourImageFileName);
                            if (outFile.exists()) {
                                Source.contourUri = Uri.fromFile(new File(outFile.getAbsolutePath()));
                            }

                            setupDrawSpotListener(2);

                        }
                    } else {
//                        Toast.makeText(NewImageAnalysis.this, "Image is not available to draw", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });


        bit = Source.contourBitmap;


//        settingDataFromDatabase();

        String finalType2 = type;
        String finalType122 = getIntent().getStringExtra("mtype") != null
                ? getIntent().getStringExtra("mtype")
                : "null";

        binding.plotL.splitCOmapre.setVisibility(View.GONE);

        if (finalType2.equals("multi") || finalType2.equals("split") || finalType2.equals("mainImg")) {

            if (!finalType122.equals("mainImg")) {
                binding.plotL.splitCOmapre.setVisibility(View.VISIBLE);
            }
            binding.plotL.addToCompare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String finalType = finalType2;

                    if (contourDataArrayList == null || contourDataArrayList.size() < 0) {
//                        Source.toast(NewImageAnalysis.this, "First Spot then add this");
                    } else {
                        if (finalType.equals("multi")) {
                            MultiSplitIntensity multiSplitIntensity = new MultiSplitIntensity(getIntent().getStringExtra("imageName"), true, Source.rFvsAreaArrayList);
                            Source.intensityArrayList.add(multiSplitIntensity);
                        }
                        if (finalType.equals("mainImg")) {
                            MultiSplitIntensity multiSplitIntensity = new MultiSplitIntensity("Main Image", true, Source.rFvsAreaArrayList);
                            Source.intensityArrayList.add(multiSplitIntensity);
                        }
//                        Source.toast(NewImageAnalysis.this, "Added");
                        usersDatabase.logUserAction(AuthDialog.activeUserName, AuthDialog.activeUserRole, "Split added to analysis", getIntent().getStringExtra("projectName").toString(), getIntent().getStringExtra("id").toString(), AuthDialog.projectType);
                    }

                }
            });
            if (work.equals(works[2])) {
                if (getIntent().getStringExtra("positionOf") != null) {

                    ArrayList<SplitData> splitDataArrayList = new ArrayList<>(Source.splitDataArrayList);

//                    Toast.makeText(this, "Size " + splitDataArrayList.size(), Toast.LENGTH_SHORT).show();

                    Source.splitPosition = Integer.parseInt(getIntent().getStringExtra("positionOf").toString()) + 1;
//                    Toast.makeText(this, "" + "Position " + Source.splitPosition, Toast.LENGTH_SHORT).show();

                    if (Source.splitPosition < splitDataArrayList.size()) {


                        SplitData data = splitDataArrayList.get(Source.splitPosition);

                        File outFile = new File(dir, data.getImagePath());

                        binding.plotL.nextImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                                Source.splitPosition++;
                                Intent intent = new Intent(NewImageAnalysis.this, NewImageAnalysis.class);
                                intent.putExtra("positionOf", String.valueOf(Source.splitPosition));
                                intent.putExtra("w", "split");
                                intent.putExtra("img_path", outFile.getPath());
                                intent.putExtra("projectName", projectName);
                                intent.putExtra("projectDescription", data.getDescription());
                                intent.putExtra("projectImage", data.getImagePath());
                                intent.putExtra("hour", data.getHour());
                                intent.putExtra("projectNumber", projectNumber);
                                intent.putExtra("splitId", splitId);
                                intent.putExtra("type", "multi");
                                intent.putExtra("rmSpot", data.getRmSpot());
                                intent.putExtra("finalSpot", data.getFinalSpot());

                                intent.putExtra("imageName", data.getImageName());
                                intent.putExtra("timeStamp", getIntent().getStringExtra("timeStamp"));
                                intent.putExtra("splitProjectName", data.getImageName());
                                intent.putExtra("tableName", tableName);
                                intent.putExtra("roiTableID", data.getRoiTableID());
                                intent.putExtra("thresholdVal", data.getThresholdVal());
                                intent.putExtra("numberOfSpots", data.getNoOfSpots());
                                intent.putExtra("id", id);
                                intent.putExtra("pid", data.getId());
                                intent.putExtra("volumePlotTableID", data.getVolumePlotTableID());
                                intent.putExtra("intensityPlotTableID", data.getIntensityPlotTableID());
                                intent.putExtra("plotTableID", data.getPlotTableID());


                                startActivity(intent);
                                finish();

                            }
                        });
                    } else {
//                        binding.plotL.nextImage.setVisibility(View.GONE);

                        binding.plotL.nextImage.setText("Done");
                        binding.plotL.nextImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });


//                        Toast.makeText(this, "position null", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            if (finalType2.equals("mainImg")) {
                ArrayList<SplitData> splitDataArrayList = new ArrayList<>(Source.splitDataArrayList);

//                Toast.makeText(this, "Size " + splitDataArrayList.size(), Toast.LENGTH_SHORT).show();

                Source.splitPosition = 0;
                // Toast.makeText(this, "" + "Position " + Source.splitPosition, Toast.LENGTH_SHORT).show();

                if (Source.splitPosition < splitDataArrayList.size()) {


                    SplitData data = splitDataArrayList.get(Source.splitPosition);

                    File outFile = new File(dir, data.getImagePath());

                    binding.plotL.nextImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                                Source.splitPosition++;
                            Intent intent = new Intent(NewImageAnalysis.this, NewImageAnalysis.class);
                            intent.putExtra("positionOf", String.valueOf(Source.splitPosition));
                            intent.putExtra("w", "split");
                            intent.putExtra("img_path", outFile.getPath());
                            intent.putExtra("projectName", projectName);
                            intent.putExtra("projectDescription", data.getDescription());
                            intent.putExtra("projectImage", data.getImagePath());
                            intent.putExtra("projectNumber", projectNumber);
                            intent.putExtra("splitId", splitId);
                            intent.putExtra("type", "multi");

                            intent.putExtra("imageName", data.getImageName());
                            intent.putExtra("timeStamp", getIntent().getStringExtra("timeStamp"));
                            intent.putExtra("tableName", tableName);
                            intent.putExtra("roiTableID", data.getRoiTableID());
                            intent.putExtra("thresholdVal", data.getThresholdVal());
                            intent.putExtra("numberOfSpots", data.getNoOfSpots());
                            intent.putExtra("id", id);
                            intent.putExtra("pid", data.getId());
                            intent.putExtra("volumePlotTableID", data.getVolumePlotTableID());
                            intent.putExtra("intensityPlotTableID", data.getIntensityPlotTableID());
                            intent.putExtra("plotTableID", data.getPlotTableID());


                            startActivity(intent);
                            finish();

                        }
                    });
                } else {
//                        binding.plotL.nextImage.setVisibility(View.GONE);

                    binding.plotL.nextImage.setText("Done");
                    binding.plotL.nextImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });


//                        Toast.makeText(this, "position null", Toast.LENGTH_SHORT).show();
                }
            }
        }


        binding.anaL.changeROI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAnalysisClass.changeROIFunction();
            }
        });

//        binding.clearAllData.setOn
        binding.clearAllData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewImageAnalysis.this);
                alertDialogBuilder.setMessage("Are you sure you want to clear all data?");
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        File outFile = new File(dir, contourImageFileName);

                        if (outFile.exists()) {
                            outFile.delete();
                        }

                        Bitmap b = processImage();

                        binding.anaL.spotDetection.spotContours.setText("Generate Spots");

                        String fileName34 = "";
                        if (work.equals(works[0]) || work.equals(works[1])) {
                            fileName34 = "CD_" + id + ".json";
                        }

                        if (work.equals(works[2])) {
                            fileName34 = "CD_" + getIntent().getStringExtra("pid").toString() + ".json";
                        }

                        File myDir = new File(dir, fileName34);

                        if (myDir.exists()) {
                            Gson gson = new Gson();

                            BufferedReader bufferedReader = null;
                            try {
                                bufferedReader = new BufferedReader(new FileReader(new File(dir, fileName34)));


                                Type mapType = new TypeToken<Map<String, List<Map<String, Object>>>>() {
                                }.getType();
                                Map<String, List<Map<String, Object>>> dataMap = gson.fromJson(bufferedReader, mapType);


                                dataMap.put("contoursData", new ArrayList<>());
                                dataMap.put("manualContour", new ArrayList<>());

                                // Write the updated dataMap back to the JSON file
                                try {
                                    Writer output = new BufferedWriter(new FileWriter(new File(dir, fileName34)));
                                    output.write(gson.toJson(dataMap));
                                    output.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        // Clear data structures in your code
                        if (contourList != null) {
                            contourList.clear();
                        } else {
                            contourList = new ArrayList<>();
                        }
                        rFvsAreaArrayList.clear();
                        labelDataArrayList.clear();
                        contourSetArrayList.clear();
                        volumeArrayList.clear();
                        contourDataArrayList.clear();
                        labelDataArrayList.clear();

                        manualContourArrayList.clear();
                        contourList.clear();

//                saveImageViewToFile(b, contourImageFileName);

                        contourList = new ArrayList<>();
//                        rFvsAreaArrayList = new ArrayList<>();
                        contourSetArrayList = new ArrayList<>();
                        volumeArrayList = new ArrayList<>();
                        contourDataArrayList = new ArrayList<>();
//        contourWithDataList = new ArrayList<>();

                        Source.volumeDATA = new ArrayList<>();
                        Source.contourDataArrayList = new ArrayList<>();
//                        Source.rFvsAreaArrayList = new ArrayList<>();
                        Source.splitContourDataList = new ArrayList<>();


                        databaseHelper.deleteDataFromTable(volumePlotTableID);
//                        databaseHelper.deleteDataFromTable(intensityPlotTableID);
                        databaseHelper.deleteDataFromTable(plotTableID);
                        databaseHelper.deleteDataFromTable("LABEL_" + plotTableID);

                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();


            }
        });


        binding.anaL.intensityPlotting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    performAnalysis(1);
                    AnalysisTask task = new AnalysisTask();
                    task.execute(1);
                }
            }
        });
        String keyForData = "KEY_NOTES";
        if (work.equals(works[2])) {
            keyForData = keyForData + getIntent().getStringExtra("pid");
        } else {
            keyForData = keyForData + getIntent().getStringExtra("id");

        }

        final String[] prNotes = {""};

        if (SharedPrefData.getSavedData(this, keyForData) == null) {
            SharedPrefData.saveData(this, keyForData, "");
        } else {
            prNotes[0] = SharedPrefData.getSavedData(this, keyForData);
        }

        binding.anaL.projectDescriptionD.setText(prNotes[0]);

        String finalKeyForData = keyForData;
        binding.anaL.projectDescriptionD.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = String.valueOf(s);
                Log.e("Textyyyy", text);
                Log.e("Textyyyy2", s + "");


            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = String.valueOf(s);
                Log.e("TextyyyyAfter", text);
                Log.e("Textyyyy2After", s + "");

                prNotes[0] = text;
                SharedPrefData.saveData(NewImageAnalysis.this
                        , finalKeyForData, text);

            }
        });

        binding.revertToMainImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (work.equals(works[0])) {
                    imageAnalysisClass.saveImageViewToFile(mainImageBitmap, fileName, id, works, work);

                } else {
                    imageAnalysisClass.saveImageViewToFile(mainImageBitmap, projectImage, id, works, work);
                }

                captured_image.setImageBitmap(mainImageBitmap);

                usersDatabase.logUserAction(AuthDialog.activeUserName, AuthDialog.activeUserRole, "Revert to main image", getIntent().getStringExtra("projectName").toString(), getIntent().getStringExtra("id").toString(), AuthDialog.projectType);

            }
        });

        binding.manageSpots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((contourList != null && !contourList.isEmpty()) || (manualContourArrayList != null && !manualContourArrayList.isEmpty())) {

                }

                if ((contourDataArrayList != null && !contourDataArrayList.isEmpty()) || (manualContourArrayList != null && !manualContourArrayList.isEmpty())
                ) {

                    ArrayList<String> totalSpots = new ArrayList<>();
                    for (int i = 0; i < contourDataArrayList.size(); i++) {
                        totalSpots.add("Spot " + contourDataArrayList.get(i).getId());
                    }


                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.manage_spots, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(NewImageAnalysis.this).setView(dialogView);

                    final AlertDialog alertDialog = builder.create();
                    if (alertDialog.getWindow() != null) {
                        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    }

                    TextView rmSpinner = dialogView.findViewById(R.id.rmSpinner);
                    TextView finalSpinner = dialogView.findViewById(R.id.finalSpinner);
                    Button saveDataRFinal = dialogView.findViewById(R.id.saveDataRFinal);


                    ArrayAdapter<String> adapter1 = new ArrayAdapter<>(NewImageAnalysis.this, android.R.layout.simple_spinner_item, totalSpots);
                    adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    ArrayAdapter<String> adapter2 = new ArrayAdapter<>(NewImageAnalysis.this, android.R.layout.simple_spinner_item, totalSpots);
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    LayoutInflater inflater2 = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    // Spinner 1 Popup
                    View popupView1 = inflater2.inflate(R.layout.spinner_popup, null);
                    final PopupWindow popupWindow1 = new PopupWindow(popupView1, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    ListView listView1 = popupView1.findViewById(R.id.spinner_list_view);
                    listView1.setAdapter(adapter1);
                    rmSpinner.setText("Spot " + rmSpot + "");
                    // Handle item selection for Spinner 1
                    listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String selectedItem = totalSpots.get(position);
                            rmSpinner.setText(selectedItem);
                            rmSpot = selectedItem.replace("Spot ", "");

                            popupWindow1.dismiss();
                        }
                    });

                    // Show the PopupWindow for Spinner 1 on TextView click
                    rmSpinner.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow1.showAsDropDown(rmSpinner, 0, 0);
                        }
                    });

                    // Dismiss PopupWindow for Spinner 1 when touched outside
                    popupWindow1.setOutsideTouchable(true);
                    popupWindow1.setFocusable(true);
                    popupWindow1.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

                    popupView1.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, android.view.MotionEvent event) {
                            popupWindow1.dismiss();
                            return true;
                        }
                    });

                    // Spinner 2 Popup
                    View popupView2 = inflater2.inflate(R.layout.spinner_popup, null);
                    final PopupWindow popupWindow2 = new PopupWindow(popupView2, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    ListView listView2 = popupView2.findViewById(R.id.spinner_list_view);
                    listView2.setAdapter(adapter2);

                    finalSpinner.setText("Spot " + finalSpot);

                    // Handle item selection for Spinner 2
                    listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String selectedItem = totalSpots.get(position);
                            finalSpinner.setText(selectedItem);
                            finalSpot = selectedItem.replace("Spot ", "");
                            popupWindow2.dismiss();
                        }
                    });

                    // Show the PopupWindow for Spinner 2 on TextView click
                    finalSpinner.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popupWindow2.showAsDropDown(finalSpinner, 0, 0);
                        }
                    });

                    // Dismiss PopupWindow for Spinner 2 when touched outside
                    popupWindow2.setOutsideTouchable(true);
                    popupWindow2.setFocusable(true);
                    popupWindow2.setBackgroundDrawable(new ColorDrawable(Color.LTGRAY));

                    popupView2.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, android.view.MotionEvent event) {
                            popupWindow2.dismiss();
                            return true;
                        }
                    });

                    saveDataRFinal.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (work.equals(works[0])) {

//
//            thresholdVal = String.valueOf(threshVal);
                                numberOfSpots = String.valueOf(numberCount);

                                ProjectOfflineData projectOfflineData =
                                        new ProjectOfflineData(id, projectName, projectDescription, timeStamp, projectNumber,
                                                fileName, imageSplitAvailable, splitId, newThresholdString,
                                                numberOfSpots, tableName, roiTableID, volumePlotTableID,
                                                intensityPlotTableID, plotTableID, rmSpot, finalSpot);

                                float i = databaseHelper.updateData(projectOfflineData);
//                                Toast.makeText(NewImageAnalysis.this, "" + getIntent().getStringExtra("id"), Toast.LENGTH_SHORT).show();

                                alertDialog.dismiss();


                            }

                            if (work.equals(works[1])) {
                                File outFile = new File(dir, projectImage);


//            thresholdVal = String.valueOf(threshVal);
                                numberOfSpots = String.valueOf(numberCount);

                                if (outFile.exists()) {
                                    ProjectOfflineData projectOfflineData =
                                            new ProjectOfflineData(id, projectName, projectDescription, timeStamp,
                                                    projectNumber, projectImage, imageSplitAvailable, splitId,
                                                    newThresholdString, numberOfSpots, tableName, roiTableID,
                                                    volumePlotTableID, intensityPlotTableID, plotTableID, rmSpot, finalSpot);

                                    float i = databaseHelper.updateData(projectOfflineData);
//                                    Toast.makeText(NewImageAnalysis.this, "" + getIntent().getStringExtra("id"), Toast.LENGTH_SHORT).show();
                                    alertDialog.dismiss();


                                } else {
//                                    Source.toast(NewImageAnalysis.this, "Image not available or deleted");
                                }


                            }

                            if (work.equals(works[2])) {
                                numberOfSpots = String.valueOf(numberCount);

                                File outFile = new File(dir, projectImage);
                                if (outFile.exists()) {
                                    Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

//                captured_image.setImageBitmap(myBitmap);
                                    bitImageArray[0] = myBitmap;

                                    SplitData splitData = new SplitData(getIntent().getStringExtra("pid"),
                                            getIntent().getStringExtra("imageName"), projectImage,
                                            getIntent().getStringExtra("timeStamp"), newThresholdString, numberOfSpots, roiTableID,
                                            volumePlotTableID, intensityPlotTableID, plotTableID, projectDescription, hour, rmSpot, finalSpot);

                                    long i = databaseHelper.updateSplitData(splitData, tableName);

//                                    Toast.makeText(NewImageAnalysis.this, "" + getIntent().getStringExtra("id") + " " + i, Toast.LENGTH_SHORT).show();
//
                                    alertDialog.dismiss();

                                } else {
//                                    Source.toast(NewImageAnalysis.this, "Image not available or deleted");
                                }


                            }

                            settingDataFromDatabase();

                        }
                    });

                    alertDialog.show();
                } else {
                    //    Toast.makeText(NewImageAnalysis.this, "First spot", Toast.LENGTH_SHORT).show();
                }
            }
        });
        View rootView = getWindow().getDecorView().getRootView();

        if (work.equals(works[2])) {


            if (rmSpot == null || rmSpot.equals("-1000")) {
                Snackbar snackbar = Snackbar.make(rootView, "RM Spot is not selected", Snackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();

                snackbar.getView().setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                snackbar.getView().setOnTouchListener(new View.OnTouchListener() {
                    private float initialX;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                initialX = event.getX();
                                break;
                            case MotionEvent.ACTION_UP:
                                float finalX = event.getX();
                                if (finalX - initialX < -100) { // Adjust the threshold for a swipe here
                                    snackbar.dismiss();
                                }
                                break;
                        }
                        return true;
                    }
                });
                snackbar.show();
            }

            if (finalSpot == null || finalSpot.equals("-1000")) {
                Snackbar snackbar = Snackbar.make(rootView, "Final Spot is not selected", Snackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();

                snackbar.getView().setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                snackbar.getView().setOnTouchListener(new View.OnTouchListener() {
                    private float initialX;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                initialX = event.getX();
                                break;
                            case MotionEvent.ACTION_UP:
                                float finalX = event.getX();
                                if (finalX - initialX < -100) { // Adjust the threshold for a swipe here
                                    snackbar.dismiss();
                                }
                                break;
                        }
                        return true;
                    }
                });
                snackbar.show();
            }

            if ((rmSpot == null || rmSpot.equals("-1000")) && (finalSpot == null || finalSpot.equals("-1000"))) {
                Snackbar snackbar = Snackbar.make(rootView, "RM & Final Spot is not selected", Snackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();

                snackbar.getView().setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                snackbar.getView().setOnTouchListener(new View.OnTouchListener() {
                    private float initialX;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                initialX = event.getX();
                                break;
                            case MotionEvent.ACTION_UP:
                                float finalX = event.getX();
                                if (finalX - initialX < -100) { // Adjust the threshold for a swipe here
                                    snackbar.dismiss();
                                }
                                break;
                        }
                        return true;
                    }
                });
                snackbar.show();
            }
        }

        binding.anaL.bandDetection.bandDetectionLay.setVisibility(View.GONE);
        binding.anaL.spotDetection.spotDetectionLay.setVisibility(View.VISIBLE);
//        binding.anaL.spotDetection.spotDetectionLay.setVisibility(View.VISIBLE);


        highlightedRegions = new ArrayList<>();
        information = new ArrayList<>();


        binding.anaL.bandDetection.influenceSeek.setMax(100);
        binding.anaL.bandDetection.lagSeek.setMax(500);
        binding.anaL.bandDetection.setThreshold.setMax(500);
        binding.anaL.bandDetection.thresholdSeek.setMax(100);

        String mess = "Lag : " + lag + ", Influence : " + influence + ", Threshold : "
                + threshold;

        binding.anaL.bandDetection.allValuesTxt.setText(mess);

        binding.anaL.bandDetection.influenceSeek.setProgress(20);
        binding.anaL.bandDetection.lagSeek.setProgress(100);
        binding.anaL.bandDetection.thresholdSeek.setProgress(20);
        binding.anaL.bandDetection.setThreshold.setProgress(100);

        binding.anaL.bandDetection.influenceSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                influence = (float) progress / 100 * 2;

                String mess = "Lag : " + lag + ", Influence : " + influence + ", Threshold : " + threshold;

                binding.anaL.bandDetection.allValuesTxt.setText(mess);

                binding.anaL.bandDetection.chart.clear();
                if (information.size() > 0) {
                    Log.d("SizeInfor", information.size() + "");

                    highlightAndDisplayAllRegions();
                } else {

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.anaL.bandDetection.setThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lag = progress;

                String mess = "Lag : " + lag + ", Influence : " + influence + ", Threshold : " + threshold;

                binding.anaL.bandDetection.allValuesTxt.setText(mess);
                binding.anaL.bandDetection.thresholdValue.setText("Threshold : " + lag);

                binding.anaL.bandDetection.chart.clear();
                if (information.size() > 0) {
                    Log.d("SizeInfor", information.size() + "");

                    highlightAndDisplayAllRegions();
                } else {

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        binding.anaL.bandDetection.thresholdSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold = (float) progress / 100 * 2;

                String mess = "Lag : " + lag + ", Influence : " + influence + ", Threshold : " + threshold;

                binding.anaL.bandDetection.allValuesTxt.setText(mess);

                binding.anaL.bandDetection.chart.clear();
                if (information.size() > 0) {
                    Log.d("SizeInfor", information.size() + "");

                    highlightAndDisplayAllRegions();
                } else {

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.anaL.bandDetection.lagSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lag = progress;

                String mess = "Lag : " + lag + ", Influence : " + influence + ", Threshold : " + threshold;

                binding.anaL.bandDetection.allValuesTxt.setText(mess);

                binding.anaL.bandDetection.chart.clear();
                if (information.size() > 0) {
                    Log.d("SizeInfor", information.size() + "");

                    highlightAndDisplayAllRegions();
                } else {

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.anaL.bandDetection.saveTheseBands.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rectangleList != null && rectangleList.size() > 0) {
                    Source.rectangleList = new ArrayList();

                    Source.rectangleList.addAll(rectangleList);
                    Source.rectangle = true;
                    Source.shape = 0;

                    onResume();


                    binding.anaL.bandDetection.startEndBandAnal.setText("Start Band Analysis");
                    binding.anaL.bandDetection.bandAnalysisPanel.setVisibility(View.GONE);

//                    finish();
                } else {
                    Toast.makeText(NewImageAnalysis.this, "No spots are there", Toast.LENGTH_SHORT).show();
                }
            }
        });


        final boolean[] startAnal = {true};
        binding.anaL.bandDetection.bandAnalysisPanel.setVisibility(View.GONE);

        binding.anaL.bandDetection.startEndBandAnal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (startAnal[0]) {
                    AnalysisTask task = new AnalysisTask();
                    task.execute(15);

                    binding.anaL.bandDetection.startEndBandAnal.setText("End Band Analysis");
                    binding.anaL.bandDetection.bandAnalysisPanel.setVisibility(View.VISIBLE);
                    startAnal[0] = false;
                } else {

                    binding.capturedImage.setImageBitmap(Source.contourBitmap);
                    binding.anaL.bandDetection.startEndBandAnal.setText("Start Band Analysis");
                    binding.anaL.bandDetection.bandAnalysisPanel.setVisibility(View.GONE);
                    startAnal[0] = true;
                }

            }
        });

        binding.anaL.bandDetection.decrementThres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lag <= 0) {
                    Toast.makeText(NewImageAnalysis.this, "Min value reached", Toast.LENGTH_SHORT).show();
                } else {
                    lag -= 2;
                    binding.anaL.bandDetection.lagSeek.setProgress(lag);
                    binding.anaL.bandDetection.setThreshold.setProgress(lag);

                    String mess = "Lag : " + lag + ", Influence : " + influence + ", Threshold : " + threshold;
                    binding.anaL.bandDetection.allValuesTxt.setText(mess);
                    binding.anaL.bandDetection.thresholdValue.setText("Threshold : " + lag);

                    binding.anaL.bandDetection.chart.clear();
                    if (information.size() > 0) {
                        Log.d("SizeInfor", information.size() + "");

                        highlightAndDisplayAllRegions();
                    } else {

                    }
                }
            }
        });

        binding.anaL.bandDetection.incrementThres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lag >= 500) {
                    Toast.makeText(NewImageAnalysis.this, "Max value reached", Toast.LENGTH_SHORT).show();
                } else {
                    lag += 2;
                    binding.anaL.bandDetection.lagSeek.setProgress(lag);
                    binding.anaL.bandDetection.setThreshold.setProgress(lag);

                    String mess = "Lag : " + lag + ", Influence : " + influence + ", Threshold : " + threshold;
                    binding.anaL.bandDetection.allValuesTxt.setText(mess);
                    binding.anaL.bandDetection.thresholdValue.setText("Threshold : " + lag);


                    binding.anaL.bandDetection.chart.clear();
                    if (information.size() > 0) {
                        Log.d("SizeInfor", information.size() + "");

                        highlightAndDisplayAllRegions();
                    } else {

                    }
                }
            }
        });


        binding.anaL.bandDetection.lagDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lag <= 0) {
                    Toast.makeText(NewImageAnalysis.this, "Min value reached", Toast.LENGTH_SHORT).show();
                } else {
                    lag -= 2;
                    binding.anaL.bandDetection.lagSeek.setProgress(lag);

                    String mess = "Lag : " + lag + ", Influence : " + influence + ", Threshold : " + threshold;
                    binding.anaL.bandDetection.allValuesTxt.setText(mess);

                    binding.anaL.bandDetection.chart.clear();
                    if (information.size() > 0) {
                        Log.d("SizeInfor", information.size() + "");

                        highlightAndDisplayAllRegions();
                    } else {

                    }
                }
            }
        });

        binding.anaL.bandDetection.lagUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lag >= 500) {
                    Toast.makeText(NewImageAnalysis.this, "Max value reached", Toast.LENGTH_SHORT).show();
                } else {
                    lag += 2;
                    binding.anaL.bandDetection.lagSeek.setProgress(lag);

                    String mess = "Lag : " + lag + ", Influence : " + influence + ", Threshold : " + threshold;
                    binding.anaL.bandDetection.allValuesTxt.setText(mess);

                    binding.anaL.bandDetection.chart.clear();
                    if (information.size() > 0) {
                        Log.d("SizeInfor", information.size() + "");

                        highlightAndDisplayAllRegions();
                    } else {

                    }
                }
            }
        });

        binding.anaL.bandDetection.thesDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (threshold <= 0) {
                    Toast.makeText(NewImageAnalysis.this, "Min value reached", Toast.LENGTH_SHORT).show();
                } else {
                    threshold -= 0.1;
                    binding.anaL.bandDetection.thresholdSeek.setProgress((int) ((threshold / 2) * 100));

                    String mess = "Lag : " + lag + ", Influence : " + influence + ", Threshold : " + threshold;
                    binding.anaL.bandDetection.allValuesTxt.setText(mess);

                    binding.anaL.bandDetection.chart.clear();
                    if (information.size() > 0) {
                        Log.d("SizeInfor", information.size() + "");

                        highlightAndDisplayAllRegions();
                    } else {

                    }
                }
            }
        });

        binding.anaL.bandDetection.threUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (threshold >= 2) {
                    Toast.makeText(NewImageAnalysis.this, "Max value reached", Toast.LENGTH_SHORT).show();
                } else {
                    threshold += 0.1;
                    binding.anaL.bandDetection.thresholdSeek.setProgress((int) ((threshold / 2) * 100));

                    String mess = "Lag : " + lag + ", Influence : " + influence + ", Threshold : " + threshold;
                    binding.anaL.bandDetection.allValuesTxt.setText(mess);

                    binding.anaL.bandDetection.chart.clear();
                    if (information.size() > 0) {
                        Log.d("SizeInfor", information.size() + "");

                        highlightAndDisplayAllRegions();
                    } else {

                    }
                }
            }
        });

        binding.anaL.bandDetection.influenceDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (influence <= 0) {
                    Toast.makeText(NewImageAnalysis.this, "Min value reached", Toast.LENGTH_SHORT).show();
                } else {
                    influence -= 0.1;
                    binding.anaL.bandDetection.influenceSeek.setProgress((int) ((influence / 2) * 100));

                    String mess = "Lag : " + lag + ", Influence : " + influence + ", Threshold : " + threshold;
                    binding.anaL.bandDetection.allValuesTxt.setText(mess);

                    binding.anaL.bandDetection.chart.clear();
                    if (information.size() > 0) {
                        Log.d("SizeInfor", information.size() + "");

                        highlightAndDisplayAllRegions();
                    } else {

                    }
                }
            }
        });

        binding.anaL.bandDetection.influenceUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (influence >= 2) {
                    Toast.makeText(NewImageAnalysis.this, "Max value reached", Toast.LENGTH_SHORT).show();
                } else {
                    influence += 0.1;
                    binding.anaL.bandDetection.influenceSeek.setProgress((int) ((influence / 2) * 100));

                    String mess = "Lag : " + lag + ", Influence : " + influence + ", Threshold : " + threshold;
                    binding.anaL.bandDetection.allValuesTxt.setText(mess);

                    binding.anaL.bandDetection.chart.clear();
                    if (information.size() > 0) {
                        Log.d("SizeInfor", information.size() + "");

                        highlightAndDisplayAllRegions();
                    } else {

                    }
                }
            }
        });

        final boolean[] startAdvance = {true};
        binding.anaL.bandDetection.advanceOptPanel.setVisibility(View.GONE);

        binding.anaL.bandDetection.bandAdvanceOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (startAdvance[0]) {
                    binding.anaL.bandDetection.bandAdvanceOpt.setText("Hide Advance Options");

                    binding.anaL.bandDetection.advanceOptPanel.setVisibility(View.VISIBLE);
                    startAdvance[0] = false;
                } else {
                    binding.anaL.bandDetection.bandAdvanceOpt.setText("Advance Options");

                    binding.anaL.bandDetection.advanceOptPanel.setVisibility(View.GONE);
                    startAdvance[0] = true;
                }

            }
        });

        binding.anaL.bandDetection.addBand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupDrawSpotListener(0);

            }
        });

    }

    // oncreate end

    private void showData() {
        information.clear();
        lineDataSet.clear();

        ArrayList<ContourGraphSelModel> contourGraphSelModelArrayList = new ArrayList<>();
        String mySTR = "Pixel Graph";


//        ArrayList<RFvsArea> rFvsAreaArrayList = new ArrayList<>(Source.rFvsAreaArrayList);

//        Toast.makeText(this, "" + mrFvsAreaArrayList.size(), Toast.LENGTH_SHORT).show();


        Collections.reverse(rFvsAreaArrayList);
        ArrayList<Double> tempA = new ArrayList<>();

        for (int i = 0; i < rFvsAreaArrayList.size(); i++) {
            tempA.add(Source.PARTS_INTENSITY - Double.parseDouble(String.valueOf(rFvsAreaArrayList.get(i).getRf())));
            information.add(new Entry(
                    Source.PARTS_INTENSITY - Float.parseFloat(String.valueOf(rFvsAreaArrayList.get(i).getRf())),
                    Float.parseFloat(String.valueOf(rFvsAreaArrayList.get(i).getArea()))
            ));
            mySTR = mySTR + "[ rf: " + (Source.PARTS_INTENSITY - Float.parseFloat(String.valueOf(rFvsAreaArrayList.get(i).getRf()))) +
                    ", inten: " + Float.parseFloat(String.valueOf(rFvsAreaArrayList.get(i).getArea())) + " ]";
        }
        System.out.println(mySTR);

        for (int i = 0; i < contourDataArrayListNew.size(); i++) {
            contourGraphSelModelArrayList.add(new ContourGraphSelModel(
                    contourDataArrayListNew.get(i).getRfTop(),
                    contourDataArrayListNew.get(i).getRfBottom(),
                    contourDataArrayListNew.get(i).getRf(),
                    contourDataArrayListNew.get(i).getId(),
                    contourDataArrayListNew.get(i).getButtonColor()
            ));
        }
    }

    private ArrayList<android.graphics.Rect> rectangleList = new ArrayList<>();
    private ArrayList<ContourData> arrayListCont;
    private ContourIntGraphAdapter contourIntGraphAdapter;

    private void highlightAndDisplayAllRegions() {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        Log.d("SizeInfoo", information.size() + "");


        Log.d("SizeInfss", information.size() + "");

        lineDataSet.setValues(information);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setColor(getColor(R.color.purple_200));

        lineDataSet.setDrawFilled(false);
        lineDataSets.add(lineDataSet);

        highlightedRegions.clear();
        rectangleList.clear();

        arrayListCont = new ArrayList<>();
        rectangleList = new ArrayList<>();

        Log.d("SizeInf", information.size() + "");

        double[] points = new double[information.size()];

        for (int i = 0; i < information.size(); i++) {
            Entry inf = information.get(i);
            System.out.println("InforMotion " + inf.getY());
            points[i] = inf.getY();
        }

        System.out.println("InforMotionPointSize " + points.length);


        int[] signals = Source.detectPeaks(points,
                lag,
                threshold,
                influence, Source.PARTS_INTENSITY);

        List<Integer> peakStart = new ArrayList<>();
        List<Integer> peakEnd = new ArrayList<>();

        boolean inPeak = false;
        int prevSignal = -1;
        for (int i = 0; i < signals.length; i++) {
            if ((signals[i] == 0 || signals[i] == 1) && prevSignal == -1) {
                System.out.println("Signal Starting Point" + signals[i] + " i " + i);
                peakStart.add(i);
            }
            if ((prevSignal == 0 || prevSignal == 1) && signals[i] == -1) {
                System.out.println("Signal Ending Point" + signals[i] + " i " + i);
                peakEnd.add(i);
            }
            prevSignal = signals[i];
        }

        System.out.println("Peak starting points (x-axis): " + peakStart);
        System.out.println("Peak ending points (x-axis): " + peakEnd);

        List<Pair<Integer, Integer>> highlightedRegionPeak = new ArrayList<>();

        int minLength = Math.min(peakStart.size(), peakEnd.size());

        for (int i = 0; i < minLength; i++) {
            highlightedRegionPeak.add(new Pair<>(peakStart.get(i), peakEnd.get(i)));
        }

        for (int i = 0; i < highlightedRegionPeak.size(); i++) {
            highlightedRegions.add(new Pair<>(highlightedRegionPeak.get(i).first, highlightedRegionPeak.get(i).second));
            System.out.println("Pairss " + highlightedRegionPeak.get(i).first + ", " + highlightedRegionPeak.get(i).second);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(NewImageAnalysis.this, "MS" + highlightedRegions.size(), Toast.LENGTH_SHORT).show();
            }
        });

        ArrayList<LineDataSet> shadedDataSets = new ArrayList<>();
        Bitmap bit = Source.contourBitmap;

        for (int im = 0; im < highlightedRegions.size(); im++) {
            Pair<Integer, Integer> region = highlightedRegions.get(im);

            System.out.println("Pairssmmm " + highlightedRegions.get(im).first + ", " + highlightedRegions.get(im).second);

            String rfTop = String.valueOf(region.second / (float) Source.PARTS_INTENSITY);
            String rf = String.valueOf(((region.first + region.second) / 2) / (float) Source.PARTS_INTENSITY);
            String rfBottom = String.valueOf(region.first / (float) Source.PARTS_INTENSITY);
            String id = "g" + (im + 1);

            float newRfTop = Float.parseFloat(rfTop) * (float) Source.PARTS_INTENSITY;
            float newRf = Float.parseFloat(rf) * (float) Source.PARTS_INTENSITY;
            float newRfBottom = Float.parseFloat(rfBottom) * (float) Source.PARTS_INTENSITY;

            System.out.println("RF Topss: " + rfTop + ", RF Bottom: " + rfBottom + ", RF: " + rf + ", Button Color: ");
            System.out.println("NRF Topss: " + newRfTop + ", RF Bottom: " + newRfBottom + ", RF: " + newRf + ", Button Color: ");


            arrayListCont.add(new ContourData(id, rf, rfTop, rfBottom, "0", "0", "0", true));

            android.graphics.Rect re = convertRFToRect(String.valueOf(rfTop), String.valueOf(rfBottom), String.valueOf(rf));

            bit = RegionOfInterest.drawRectWithROI(bit, re.left, re.top, re.width(), re.height());

            int x = re.left;
            int y = re.top;
            int w = re.width();
            int h = re.height();

            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setTextSize(30f);
            paint.setStyle(Paint.Style.FILL);

            Canvas canvas = new Canvas(bit);
            canvas.drawText(id, x, y, paint);

            Log.e("Love", "ID : " + id + " top " + re.top + ", bottom " + re.bottom + ", left " + re.left + ", right " + re.right);

            ArrayList<Entry> shadedRegion = new ArrayList<>();
            for (Entry entry : information) {
                float xEntry = entry.getX();
                float yEntry = entry.getY();
                if (xEntry >= newRfBottom && xEntry <= newRfTop) {
                    shadedRegion.add(new Entry(xEntry, yEntry));
                    Log.e("ThisIsNotAnErrorBBB", "Top : " + xEntry + " Bottom : " + yEntry + " RF : " + rf);
                }
            }

            LineDataSet shadedDataSet = new LineDataSet(shadedRegion, "");
            shadedDataSet.setDrawCircles(false);
            shadedDataSet.setColor(Color.MAGENTA);
            shadedDataSet.setDrawFilled(true);
            shadedDataSet.setFillColor(Color.MAGENTA);
            shadedDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            shadedDataSet.setLineWidth(0f);

            shadedDataSets.add(shadedDataSet);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showContoursList();
        }

        ArrayList<ContourGraphSelModel> contourGraphSelModelArrayList = new ArrayList<>();
        for (int i = 0; i < contourDataArrayListNew.size(); i++) {
            ContourData a = contourDataArrayListNew.get(i);
            contourGraphSelModelArrayList.add(new ContourGraphSelModel(a.getRfTop(), a.getRfBottom(), a.getRf(), a.getId(), a.getButtonColor()));
//            a.toString();

            System.out.println("RF Topss: " + a.getRfTop() + ", RF Bottom: " + a.getRfBottom() + ", RF: " + a.getRf() + ", Button Color: " + a.getButtonColor());

        }

        highLightMyRegion(contourGraphSelModelArrayList, lineDataSets);
    }

    private android.graphics.Rect convertRFToRect(String rfTop, String rfBottom, String rf) {
        int imageHeight = Source.contourBitmap.getHeight();
        double rfTopValue = Double.parseDouble(rfTop);
        double rfBottomValue = Double.parseDouble(rfBottom);
        double rfValue = Double.parseDouble(rf);

        int top = (int) ((1 - rfTopValue) * Source.contourBitmap.getHeight());
        int bottom = (int) ((1 - rfBottomValue) * Source.contourBitmap.getHeight());
        int width = Source.contourBitmap.getWidth();
        int height = imageHeight;
        int left = 0;
        int right = width;

        return new android.graphics.Rect(left, top, right, bottom);
    }


    private void getIntensityData() {
        rFvsAreaArrayList = new ArrayList<>();
        mrFvsAreaArrayList = new ArrayList<>();

        Cursor cursor = databaseHelper.getDataFromTable(intensityPlotTableID);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {

                    mrFvsAreaArrayList.add(new RFvsArea(Double.parseDouble(String.valueOf(cursor.getString(1))), Double.parseDouble(String.valueOf(cursor.getString(2)))));
                    rFvsAreaArrayList.add(new RFvsArea(Double.parseDouble(
                            String.valueOf(cursor.getString(1))),
                            Double.parseDouble(String.valueOf(cursor.getString(2)))));

                } while (cursor.moveToNext());
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(NewImageAnalysis.this, "D " + rFvsAreaArrayList.size(), Toast.LENGTH_SHORT).show();

            }
        });


    }


    private void highLightMyRegion(ArrayList<ContourGraphSelModel> contourGraphSelModelArrayList, ArrayList<ILineDataSet> lineDataSets) {
        ArrayList<LineDataSet> shadedDataSets = new ArrayList<>();
        binding.anaL.bandDetection.chart.clear();
        rectangleList.clear();
        Bitmap bit = Source.contourBitmap;

        Log.e("Logebel", "1");

        for (ContourGraphSelModel cont : contourGraphSelModelArrayList) {
            String id = cont.getId();
            float newRfTop = Float.parseFloat(cont.getRfTop()) * Source.PARTS_INTENSITY;
            float newRfBottom = Float.parseFloat(cont.getRfBottom()) * Source.PARTS_INTENSITY;
            float newRf = Float.parseFloat(cont.getRf()) * Source.PARTS_INTENSITY;

//            Toast.makeText(this, "Source.PARTS_INTENSITY" + Source.PARTS_INTENSITY, Toast.LENGTH_SHORT).show();

            Log.e("RfsB", "B " + newRfBottom + ", T " + newRfTop + " R " + newRf);

            android.graphics.Rect re = convertRFToRect(cont.getRfTop(), cont.getRfBottom(), cont.getRf());
            rectangleList.add(re);

            bit = RegionOfInterest.drawRectWithROI(bit, re.left, re.top, re.width(), re.height());

            int x = re.left;
            int y = re.top;
            int w = re.width();
            int h = re.height();
            Log.e("TisNewI", "left - " + re.left + " top - " + re.top + " width - " + re.width());

            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setTextSize(30f);
            paint.setStyle(Paint.Style.FILL);

            Canvas canvas = new Canvas(bit);
            canvas.drawText(id, x, y, paint);


            binding.capturedImage.setImageBitmap(bit);


            ArrayList<Entry> shadedRegion = new ArrayList<>();
            for (Entry entry : information) {
                float xq = entry.getX();
                float yq = entry.getY();
                if (xq >= newRfBottom && xq <= newRfTop) {
                    shadedRegion.add(new Entry(xq, yq));
                }
            }

            LineDataSet shadedDataSet = new LineDataSet(shadedRegion, "");
            shadedDataSet.setDrawCircles(false);
            shadedDataSet.setColor(Color.MAGENTA);
            shadedDataSet.setDrawFilled(true);
            shadedDataSet.setFillColor(Color.MAGENTA);
            shadedDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            shadedDataSet.setLineWidth(0f);

            shadedDataSets.add(shadedDataSet);
            lineDataSets.add(shadedDataSet);
        }

        LineData lineData = new LineData(lineDataSets);
        binding.anaL.bandDetection.chart.setData(lineData);
        binding.anaL.bandDetection.chart.getDescription().setEnabled(false);
        binding.anaL.bandDetection.chart.getLegend().setEnabled(false);
        binding.anaL.bandDetection.chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.anaL.bandDetection.chart.invalidate();
//
//        binding.anaL.bandDetection.chart.clear();
//        binding.anaL.bandDetection.chart.setData(lineData);
//        binding.anaL.bandDetection.chart.invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showContoursList() {
        contourDataArrayListNew = new ArrayList<>();
        for (int i = 0; i < arrayListCont.size(); i++) {
            int color;
            switch (i) {
                case 0:
                    color = getColor(R.color.grey);
                    break;
                case 1:
                    color = getColor(R.color.yellow);
                    break;
                // ... other cases
                default:
                    color = getColor(R.color.blue2);
                    break;
            }

            contourDataArrayListNew.add(new ContourData(
                    arrayListCont.get(i).getId(),
                    arrayListCont.get(i).getRf(),
                    arrayListCont.get(i).getRfTop(),
                    arrayListCont.get(i).getRfBottom(),
                    arrayListCont.get(i).getCv(),
                    arrayListCont.get(i).getArea(),
                    arrayListCont.get(i).getVolume(),
                    arrayListCont.get(i).isSelected(),
                    color
            ));
        }

        contourIntGraphAdapter = new ContourIntGraphAdapter(this, contourDataArrayListNew, 0,
                this, true, false, false);
        binding.anaL.bandDetection.contourListRecView.setAdapter(contourIntGraphAdapter);
        contourIntGraphAdapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void performAnalysis(int so) {


        if (work.equals(works[1])) {
            File outFile = new File(dir, projectImage);
            if (outFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

                bitImage = myBitmap;

            } else {
//                Source.toast(this, "Image not available or deleted");
            }
        }
        if (work.equals(works[0])) {
            File outFile = new File(dir, projectImage);
            if (outFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

                bitImage = myBitmap;

            } else {
//                Source.toast(this, "Image not available or deleted");
            }
        }

        if (work.equals(works[2])) {
            File outFile = new File(dir, projectImage);
            if (outFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

                bitImage = myBitmap;
            } else {
//                Source.toast(this, "Image not available or deleted");
            }
        }

        Bitmap.Config config = bitImage.getConfig();

        Log.e("BitImage", imageAnalysisClass.configToBitDepth(config) + "");


        Mat firstImage = new Mat();
        Utils.bitmapToMat(bitImage, firstImage);

        // grayscale
        grayScaleImage = new Mat();
        Imgproc.cvtColor(firstImage, grayScaleImage, Imgproc.COLOR_BGR2GRAY);


//        Mat binary = new Mat();
//        Imgproc.threshold(grayScaleImage, binary, threshVal, 255, 0);


        // Preprocess the image and calculate intensities
        double[] rfValues = imageAnalysisClass.calculateRFValues(Source.PARTS_INTENSITY); // Calculated RF values


        for (int i = 0; i < rfValues.length; i++) {
            Log.e("RFValuesEEEE", rfValues[i] + "");
        }

        ArrayList<Double> intensityValues = imageAnalysisClass.calculateIntensities(grayScaleImage, rfValues);

        int windowSize = Source.PARTS_INTENSITY / 25;


//        ArrayList<Double> movingAvgList = imageAnalysisClass.applyMovingAverage(intensityValues, windowSize);
        ArrayList<Double> movingAvgList = intensityValues;

        ArrayList<RFvsArea> rFvsAreas = new ArrayList<>();

        for (int i = 0; i < movingAvgList.size(); i++) {
            rFvsAreas.add(new RFvsArea(i, (255 - movingAvgList.get(i))));
//            rFvsAreas.add(new RFvsArea(rfValues[i], (255 - movingAvgList.get(i))));
//            rFvsAreas.add(new RFvsArea(i, (movingAvgList.get(i))));
            Log.d("IntValuesMy", " i = " + i + " & intensity = " + (255 - movingAvgList.get(i)));
        }
        Source.rFvsAreaArrayList = rFvsAreas;

        databaseHelper.deleteDataFromTable(intensityPlotTableID);


        for (int im = 0; im < Source.rFvsAreaArrayList.size(); im++) {
            databaseHelper.insertRfVsAreaIntensityPlotTableData(
                    intensityPlotTableID,
                    String.valueOf(im),
                    String.valueOf(Source.rFvsAreaArrayList.get(im).getRf()),
                    String.valueOf(Source.rFvsAreaArrayList.get(im).getArea())
            );
        }


        if (so == 1) {

            this.startActivity(new Intent(NewImageAnalysis.this, PixelGraph.class)
                    .putExtra("projectName", projectName).putExtra("id", id).
                    putExtra("contourJsonFileName", contourJsonFileName)
                    .putExtra("plotTableID", plotTableID));
        }
    }

    private void settingLabelData() {
        labelDataArrayList.clear();
        Cursor cursor = databaseHelper.getDataFromTable("LABEL_" + plotTableID);
        if (cursor != null) {


            if (cursor.moveToFirst()) {
                do {

                    labelDataArrayList.add(new LabelData(cursor.getString(0), cursor.getString(1)));

                } while (cursor.moveToNext());
            }
        }
//        Toast.makeText(this, "L_" + labelDataArrayList.size(), Toast.LENGTH_SHORT).show();
    }

    private void settingDataFromDatabase() {


        if (volumeArrayList == null || volumeArrayList.size() == 0) {
            volumeArrayList = new ArrayList<>();

            Cursor cursor = databaseHelper.getDataFromTable(volumePlotTableID);
            if (cursor != null) {


                if (cursor.moveToFirst()) {
                    do {

                        volumeArrayList.add(Double.parseDouble(String.valueOf(cursor.getString(2))));

                    } while (cursor.moveToNext());
                }
            }

        }
        ArrayList rFvsAreaArrayList = new ArrayList<>();

        if (rFvsAreaArrayList == null || rFvsAreaArrayList.size() == 0) {

            Cursor cursor = databaseHelper.getDataFromTable(intensityPlotTableID);

//            Toast.makeText(this, "In " + cursor.getCount(), Toast.LENGTH_SHORT).show();

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {

                        rFvsAreaArrayList.add(new RFvsArea(Double.parseDouble(String.valueOf(cursor.getString(1))), Double.parseDouble(String.valueOf(cursor.getString(2)))));

                    } while (cursor.moveToNext());
                }
            }

        }

        if (contourDataArrayList == null || contourDataArrayList.size() == 0) {
            contourDataArrayList = new ArrayList<>();

            Cursor cursor = databaseHelper.getDataFromTable(plotTableID);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {

                        contourDataArrayList.add(new
                                ContourData(
                                cursor.getString(0),
                                cursor.getString(1),
                                cursor.getString(2),
                                cursor.getString(3),
                                cursor.getString(4),
                                cursor.getString(5),
                                cursor.getString(7),
                                "na"
//                                cursor.getString(8)

                        ));

                    } while (cursor.moveToNext());
                }
            }
        }
//        Toast.makeText(this, "RFS" + rFvsAreaArrayList.size(), Toast.LENGTH_SHORT).show();

        Source.volumeDATA = volumeArrayList;
        Source.contourDataArrayList = contourDataArrayList;
        Source.rFvsAreaArrayList.addAll(rFvsAreaArrayList);

//        Toast.makeText(this, "" + Source.rFvsAreaArrayList.size(), Toast.LENGTH_SHORT).show();

        boolean isThisAvailable = false;
        int avaIndex = 0;
        for (int i = 0; i < Source.splitContourDataList.size(); i++) {
            if (Source.splitContourDataList.get(i).getMainImageName().equals(projectImage)) {
                isThisAvailable = true;
                avaIndex = i;
            }
        }
        if (isThisAvailable) {
            Source.splitContourDataList.remove(avaIndex);
        }
        String finalType = getIntent().getStringExtra("type");
        String imgName = null;
        if (finalType.equals("multi")) {
            imgName = getIntent().getStringExtra("imageName");
        }
        if (finalType.equals("mainImg")) {
//            imgName = "Main Image";
            imgName = getIntent().getStringExtra("imageName");

        }

//        getIntensityData();


        Source.splitContourDataList.add(new SplitContourData(imgName, true,
                contourImageFileName,
                projectImage, hr, rmSpot, finalSpot, volumeArrayList, rFvsAreaArrayList,
                contourSetArrayList, contourDataArrayList, labelDataArrayList));
//        Toast.makeText(this, "RFS" + rFvsAreaArrayList.size(), Toast.LENGTH_SHORT).show();


    }

    public void populateTheContourDataFromJSON() {

        String contourType = "contoursData";

        String fileName34 = "";

        if (work.equals(works[0]) || work.equals(works[1])) {
            fileName34 = "CD_" + id + ".json";
        }

        if (work.equals(works[2])) {
            fileName34 = "CD_" + getIntent().getStringExtra("pid").toString() + ".json";

        }

        contourJsonFileName = fileName34;

        File myDir = new File(dir, fileName34);
        if (myDir.exists()) {

            String keyContours = "contoursData";
            String keyManualContours = "manualContour";
            List<List<Point>> contoursData = new ArrayList<>();
            ArrayList<ContourWithID> matOfPointContourData = new ArrayList<>();
            ArrayList<MatOfPoint> contData = new ArrayList<>();


            ArrayList<android.graphics.Rect> manualRectangles = new ArrayList<>();
            ArrayList<String> shapeOfManualCont = new ArrayList<>();
            ArrayList<String> idOfManualCont = new ArrayList<>();
            ArrayList<String> idOfNormalCont = new ArrayList<>();

            Gson gson = new Gson();

            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(dir, fileName34)));
                Type mapType = new TypeToken<Map<String, List<Map<String, Object>>>>() {
                }.getType();
                Map<String, List<Map<String, Object>>> dataMap = gson.fromJson(bufferedReader, mapType);

                if (dataMap.containsKey(keyContours)) {
                    List<Map<String, Object>> rawContours = dataMap.get(keyContours);
                    for (Map<String, Object> contourData : rawContours) {
                        String id = (String) contourData.get("id");

                        if (id != null) {
                            List<Map<String, Double>> pointDataList = (List<Map<String, Double>>) contourData.get("data");

                            List<Point> points = new ArrayList<>();
                            for (Map<String, Double> pointData : pointDataList) {
                                double x = pointData.get("x");
                                double y = pointData.get("y");
                                points.add(new Point(x, y));
                            }
                            contoursData.add(points);
                            MatOfPoint matOfPoint = new MatOfPoint();
                            matOfPoint.fromList(points);
                            contData.add(matOfPoint);
                            idOfNormalCont.add(id);
                            matOfPointContourData.add(new ContourWithID(id, matOfPoint));
                        }
                    }

                    if (dataMap.containsKey(keyManualContours)) {
                        List<Map<String, Object>> manualContours = dataMap.get(keyManualContours);
                        Log.d("ManualContoursSize", String.valueOf(manualContours.size())); // Add this line for debugging

                        for (Map<String, Object> manualContourData : manualContours) {
                            String id = (String) manualContourData.get("id");
                            String shape = (String) manualContourData.get("shape");
                            Map<String, Double> roiData = (Map<String, Double>) manualContourData.get("roi");

                            int left = (int) roiData.get("left").intValue();
                            int top = (int) roiData.get("top").intValue();
                            int right = (int) roiData.get("right").intValue();
                            int bottom = (int) roiData.get("bottom").intValue();

                            android.graphics.Rect rect = new android.graphics.Rect(left, top, right, bottom);


                            Log.e("left", left + "");
                            Log.e("top", top + "");
                            Log.e("right", right + "");
                            Log.e("bottom", bottom + "");
//
//                            android.graphics.Rect rect = new android.graphics.Rect(left, top, right, bottom);


                            manualRectangles.add(rect);
                            shapeOfManualCont.add(shape);
                            idOfManualCont.add(id);
                        }
                    }
                }


                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!matOfPointContourData.isEmpty() || !shapeOfManualCont.isEmpty()) {
                binding.anaL.spotDetection.spotContours.setText("Regenerate Spots");

            }

            // Now contoursData contains the remaining contours after removal

            // ... (rest of your code)
            manualContourArrayList.clear();

            contourList = new ArrayList<>();
            rFvsAreaArrayList = new ArrayList<>();
            contourSetArrayList = new ArrayList<>();
            volumeArrayList = new ArrayList<>();
            contourDataArrayList = new ArrayList<>();
//        contourWithDataList = new ArrayList<>();

            databaseHelper.deleteDataFromTable(volumePlotTableID);
//            databaseHelper.deleteDataFromTable(intensityPlotTableID);
            databaseHelper.deleteDataFromTable(plotTableID);

            AnalysisTask task = new AnalysisTask();
            task.execute(0);


            if (work.equals(works[1])) {
                File outFile = new File(dir, projectImage);
                if (outFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

                    Source.originalBitmap = myBitmap;

//                captured_image.setImageBitmap(myBitmap);
                    bitImage = myBitmap;

                } else {
//                Source.toast(this, "Image not available or deleted");
                }
            }
            if (work.equals(works[0])) {
                try {
                    bitImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(path));
                    Source.originalBitmap = bitImage;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (work.equals(works[2])) {
                File outFile = new File(dir, projectImage);
                if (outFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());
                    Source.originalBitmap = myBitmap;

//                captured_image.setImageBitmap(myBitmap);
                    bitImage = myBitmap;
                } else {
//                Source.toast(this, "Image not available or deleted");
                }
            }

            Mat firstImage = new Mat();
            Utils.bitmapToMat(bitImage, firstImage);

            grayScaleImage = new Mat();
//        Imgproc.cvtColor(firstImage, grayScaleImage, Imgproc.COLOR_BGR2GRAY);
            Imgproc.cvtColor(firstImage, grayScaleImage, Imgproc.COLOR_BGR2GRAY);

            // Apply threshold to convert the grayscale image to a binary image
            Mat binary = new Mat();
            Imgproc.threshold(grayScaleImage, binary, threshVal, 255, 0);


//        // Find contours in the binary image
            contours = new ArrayList<>();
//        Mat hierarchy = new Mat();


            Log.d("ContourSize", contData.size() + "");


            Scalar contourColor = new Scalar(255, 244, 143);
            int contourThickness = 2;
            int font = Imgproc.FONT_HERSHEY_SIMPLEX;
            double fontScale = 1;
            int fontThickness = 2;


//            Toast.makeText(this, "" + matOfPointContourData.size(), Toast.LENGTH_SHORT).show();
//            Toast.makeText(this, "" + manualRectangles.size(), Toast.LENGTH_SHORT).show();

            if (!matOfPointContourData.isEmpty() && !manualRectangles.isEmpty()) {
                for (int i = 0; i < matOfPointContourData.size(); i++) {


                    String id = idOfNormalCont.get(i);

//                    int t = i + 1;
//                Imgproc.drawContours(grayScaleImage, contourWithDataList, i, contourColor, contourThickness);
                    Imgproc.drawContours(firstImage, contData, i, contourColor, contourThickness);
                    // Get the bounding rectangle of the contour
                    Rect boundingRect = Imgproc.boundingRect(contData.get(i));
                    // Draw the contour number on the image
                    Imgproc.putText(firstImage, "" + id, new Point(boundingRect.x, boundingRect.y - 5), font, fontScale, new Scalar(0, 0, 255), fontThickness);

                    // Calculate the center point of the bounding box
                    Point centerPoint = new Point(boundingRect.x + (boundingRect.width / 2), boundingRect.y + (boundingRect.height / 2));

                    // Calculate the distance traveled by the center point
                    double contourDistance = Math.sqrt(Math.pow(centerPoint.x, 2) + Math.pow(centerPoint.y, 2));

                    // Calculate the distance traveled by the solvent front (assuming a linear gradient)
                    double solventFrontDistance = ((double) grayScaleImage.width() / 2) * (1.0 - (double) threshVal / 255.0);

                    // Calculate the RF value
                    double rfValue = (10 - (contourDistance / solventFrontDistance)) / 10;
                    contourList.add(Integer.parseInt(id));


                    int height = boundingRect.height;

                    double area = Imgproc.contourArea(contData.get(i));

                    // assuming you have already found the contour and its height
                    int topY = boundingRect.y;
                    int baseY = topY + boundingRect.height;
                    int centerY = topY + boundingRect.height / 2;

                    // assuming you have a Bitmap object called "imageBitmap"
                    int imageHeight = bitImage.getHeight();


// assuming you have the image height stored in a variable called "imageHeight"
                    double normalizedTopY = 1 - ((double) topY / imageHeight);
                    double normalizedBaseY = 1 - ((double) baseY / imageHeight);

                    double normalizedCenterY = Double.parseDouble(String.format("%.2f", 1 - ((double) centerY / imageHeight)));

                    double cv = 1 / normalizedCenterY;


                    ArrayList<RFvsArea> ar = new ArrayList<>();
                    ar.add(new RFvsArea(normalizedBaseY, 0));
                    ar.add(new RFvsArea(normalizedCenterY, area));
                    ar.add(new RFvsArea(normalizedTopY, 0));

                    System.out.println("Height : " + height + ", Diameter : " + " - >>>>>>" + normalizedTopY + " , " + normalizedBaseY + " , " + normalizedCenterY);

//                rFvsAreaArrayList.addAll(ar);

                    ArrayList<XY> xyArrayList = new ArrayList<>();
                    xyArrayList.add(new XY(normalizedBaseY, 0));
                    xyArrayList.add(new XY(normalizedCenterY, area));
                    xyArrayList.add(new XY(normalizedTopY, 0));


                    ContourSet contourSet = new ContourSet(xyArrayList);
                    contourSetArrayList.add(contourSet);

                    DecimalFormat df = new DecimalFormat("0.00E0");
                    double number = area * Math.abs(solventFrontDistance - contourDistance);
                    System.out.println(df.format(number));

                    double volume = Double.parseDouble(df.format(number));

                    volumeArrayList.add(volume);

                    ContourData contourData = new ContourData(String.valueOf(id),
                            String.valueOf(normalizedCenterY),
                            String.valueOf(normalizedTopY), String.valueOf(normalizedBaseY), String.valueOf(cv),
                            String.valueOf(area), String.valueOf(volume), "na");
                    contourDataArrayList.add(contourData);

                    databaseHelper.insertAllDataTableData(plotTableID, String.valueOf(id), String.valueOf(normalizedCenterY),
                            String.valueOf(normalizedTopY), String.valueOf(normalizedBaseY), String.valueOf(cv),
                            String.valueOf(area), "na", String.valueOf(volume), "na");

                    databaseHelper.insertVolumePlotTableData(volumePlotTableID, String.valueOf(id), String.valueOf(id), String.valueOf(volume));

                    // Print the RF value, area, and volume of the contour
                    Log.d("Contour " + i + " RF value", String.valueOf(rfValue));
                    Log.d("Contour " + i + " area", String.valueOf(area));
                    Log.d("Contour " + i + " volume", String.valueOf(volume));
//                }
                }

                String rf = "";

                Source.volumeDATA = volumeArrayList;

//            Source.rFvsAreaArrayList = rFvsAreaArrayList;
                Source.contourSetArrayList = contourSetArrayList;
                Source.contourDataArrayList = contourDataArrayList;

                Bitmap bitmap = Bitmap.createBitmap(firstImage.cols(), firstImage.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(firstImage, bitmap);

                for (int i = 0; i < manualRectangles.size(); i++) {
                    android.graphics.Rect roi = manualRectangles.get(i);
                    String shape = shapeOfManualCont.get(i);
                    String id = idOfManualCont.get(i);

                    Log.e("ThisID", id);

                    if (shape.equals("1")) {
                        bitmap = RegionOfInterest.drawOvalWithROI(bitmap, roi.left, roi.top, roi.width(), roi.height());
                        manualContourArrayList.add(new ManualContour(1, roi, id, id, Source.rFvsAreaArrayList.size()));

//                        addManualContourToJSON("" + Source.contourDataArrayList.size(), roi, 1);

                    } else {
                        bitmap = RegionOfInterest.drawRectWithROI(bitmap, roi.left, roi.top, roi.width(), roi.height());
                        manualContourArrayList.add(new ManualContour(0, roi, id, id, Source.rFvsAreaArrayList.size()));
//                        addManualContourToJSON("" + Source.contourDataArrayList.size(), roi, 0);

                    }


                    int x = roi.left;
                    int y = roi.top;
                    int w = roi.width();
                    int h = roi.height();

                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setTextSize(30f);
                    paint.setStyle(Paint.Style.FILL);


                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawText(id, (float) x, (float) y, paint);


                    DecimalFormat df = new DecimalFormat("0.00E0");

                    Bitmap originalBitmap = GRAYSCALE_BITMAP/* your original Bitmap */;
                    int regionLeft = x; // Specify the left coordinate of the region
                    int regionTop = y; // Specify the top coordinate of the region
                    int regionWidth = w; // Specify the width of the region
                    int regionHeight = h; // Specify the height of the region


//                imageAnalysisClass.filterOutDarkPixelsInRegion(originalBitmap, regionLeft, regionTop, regionWidth, regionHeight);

//                    int autoThreshold =
//                            PixelThresholdClass.autoThresholdByPercentileFromGrayscaleInRegion(GRAYSCALE_BITMAP,
//                                    regionLeft, regionTop, regionWidth, regionHeight, 0.3);
//
////                int autoThreshold =
////                        imageAnalysisClass.autoThresholdFromGrayscaleInRegion(GRAYSCALE_BITMAP,
////                                regionLeft, regionTop, regionWidth, regionHeight);
////                int[] autoThreshold =
////                        PixelThresholdClass.calculateThresholdRangeByPercentage(GRAYSCALE_BITMAP,
////                                regionLeft, regionTop, regionWidth, regionHeight, 0.2, 0.4, 2);
//
//
////                Log.e("AutoThresHold", autoThreshold[0] + "" + autoThreshold[1]);
//                    Log.e("AutoThresHold", autoThreshold + "");
//
//                    originalBitmap = imageAnalysisClass.changeColorOfBrightPixelsInRegion(GRAYSCALE_BITMAP,
//                            regionLeft, regionTop, regionWidth, regionHeight, autoThreshold, Color.RED);
//
//                    binding.sampleImg.setImageBitmap(originalBitmap);
//
//                    int pixelArea = imageAnalysisClass.calculateDarkSpotsArea(
//                            GRAYSCALE_BITMAP,
//                            regionLeft, regionTop, regionWidth, regionHeight,
//                            autoThreshold
//                    );
//
//                    Log.e("AreaOfSpecificContour", pixelArea + "");


                    double area = RegionOfInterest.calculateOvalArea(w, h);
//                    double area = pixelArea;

                    double solventFrontDistance = w * 0.5 * (1.0 - 100.0 / 255.0);

                    double contourDistance = Math.sqrt(x * x + y * y);

                    double number = area * Math.abs(solventFrontDistance - contourDistance);
                    System.out.println(df.format(number));

                    double volume = Double.parseDouble(df.format(number));

                    int imageHeight = Source.contourBitmap.getHeight();
                    double distanceFromTop = (y + roi.bottom) * 0.5;

                    double maxDistance = imageHeight;
                    double rfValue4 = 1.0 - (distanceFromTop / maxDistance);

                    double cv = 1 / rfValue4;

                    double rfValueTop = rfValue4 + (roi.height() / 2) / (double) imageHeight;
                    double rfValueBottom = rfValue4 - (roi.height() / 2) / (double) imageHeight;

                    contourDataArrayList.add(new ContourData(id, String.format("%.2f", rfValue4),
                            String.format("%.2f", rfValueTop), String.format("%.2f", rfValueBottom), String.format("%.2f", cv),
                            String.format("%.2f", area), String.valueOf(volume), "na"));

                    Log.d("RFvsArea", rfValueTop + " : " + 0.0);
                    Log.d("RFvsArea", rfValue4 + " : " + area);
                    Log.d("RFvsArea", rfValueBottom + " : " + 0.0);


                    databaseHelper.insertAllDataTableData(plotTableID, id, String.format("%.2f", rfValue4),
                            String.format("%.2f", rfValueTop), String.format("%.2f", rfValueBottom), String.format("%.2f", cv),
                            String.format("%.2f", area), "na", String.valueOf(volume), "na");
                    databaseHelper.insertVolumePlotTableData(volumePlotTableID, id, id, String.valueOf(volume));

                    // 1,2,3
                    // 0,1,2
                    // 1,1,1, 2,2,2, 3,3,3
                    // 0,1,2, 3,4,5, 6,7,8

                    Source.volumeDATA.add(volume);


                }

//                Toast.makeText(this, "this is pop", Toast.LENGTH_SHORT).show();

//            Toast.makeText(this, "Spotted", Toast.LENGTH_SHORT).show();
                Source.contourBitmap = bitmap;
                manuContBitmap = bitmap;
                imageAnalysisClass.saveImageViewToFile(bitmap, contourImageFileName, id, works, work);

                File outFile = new File(dir, contourImageFileName);
                if (outFile.exists()) {
                    Source.contourUri = Uri.fromFile(new File(outFile.getAbsolutePath()));
                }

                // Set the Bitmap in the ImageView
                captured_image.setImageBitmap(bitmap);
//        removeContour();
            } else if (!matOfPointContourData.isEmpty() && manualRectangles.isEmpty()) {
                for (int i = 0; i < matOfPointContourData.size(); i++) {

                    String id = idOfNormalCont.get(i);
//                    String id = matOfPointContourData.get(i).getId();

//                    int t = i + 1;
//                Imgproc.drawContours(grayScaleImage, contourWithDataList, i, contourColor, contourThickness);
                    Imgproc.drawContours(firstImage, contData, i, contourColor, contourThickness);
                    // Get the bounding rectangle of the contour
                    Rect boundingRect = Imgproc.boundingRect(contData.get(i));
                    // Draw the contour number on the image
                    Imgproc.putText(firstImage, "" + id, new Point(boundingRect.x, boundingRect.y - 5), font, fontScale, new Scalar(0, 0, 255), fontThickness);

                    // Calculate the center point of the bounding box
                    Point centerPoint = new Point(boundingRect.x + (boundingRect.width / 2), boundingRect.y + (boundingRect.height / 2));

                    // Calculate the distance traveled by the center point
                    double contourDistance = Math.sqrt(Math.pow(centerPoint.x, 2) + Math.pow(centerPoint.y, 2));

                    // Calculate the distance traveled by the solvent front (assuming a linear gradient)
                    double solventFrontDistance = ((double) grayScaleImage.width() / 2) * (1.0 - (double) threshVal / 255.0);

                    // Calculate the RF value
                    double rfValue = (10 - (contourDistance / solventFrontDistance)) / 10;
                    contourList.add(Integer.parseInt(id));


                    int height = boundingRect.height;

                    double area = Imgproc.contourArea(contData.get(i));

                    // assuming you have already found the contour and its height
                    int topY = boundingRect.y;
                    int baseY = topY + boundingRect.height;
                    int centerY = topY + boundingRect.height / 2;

                    // assuming you have a Bitmap object called "imageBitmap"
                    int imageHeight = bitImage.getHeight();


// assuming you have the image height stored in a variable called "imageHeight"
                    double normalizedTopY = 1 - ((double) topY / imageHeight);
                    double normalizedBaseY = 1 - ((double) baseY / imageHeight);

                    double normalizedCenterY = Double.parseDouble(String.format("%.2f", 1 - ((double) centerY / imageHeight)));

                    double cv = 1 / normalizedCenterY;


                    ArrayList<RFvsArea> ar = new ArrayList<>();
                    ar.add(new RFvsArea(normalizedBaseY, 0));
                    ar.add(new RFvsArea(normalizedCenterY, area));
                    ar.add(new RFvsArea(normalizedTopY, 0));

                    System.out.println("Height : " + height + ", Diameter : " + " - >>>>>>" + normalizedTopY + " , " + normalizedBaseY + " , " + normalizedCenterY);

//                rFvsAreaArrayList.addAll(ar);

                    ArrayList<XY> xyArrayList = new ArrayList<>();
                    xyArrayList.add(new XY(normalizedBaseY, 0));
                    xyArrayList.add(new XY(normalizedCenterY, area));
                    xyArrayList.add(new XY(normalizedTopY, 0));


                    ContourSet contourSet = new ContourSet(xyArrayList);
                    contourSetArrayList.add(contourSet);

//                     Calculate the area of the contour


                    // Calculate the volume of the contour (assuming a cylindrical shape)
//            double volume = area * (solventFrontDistance - contourDistance);
                    DecimalFormat df = new DecimalFormat("0.00E0");
                    double number = area * Math.abs(solventFrontDistance - contourDistance);
                    System.out.println(df.format(number));

                    double volume = Double.parseDouble(df.format(number));

                    volumeArrayList.add(volume);

                    ContourData contourData = new ContourData(String.valueOf(id), String.valueOf(normalizedCenterY),
                            String.valueOf(normalizedTopY), String.valueOf(normalizedBaseY), String.valueOf(cv),
                            String.valueOf(area), String.valueOf(volume), "na");
                    contourDataArrayList.add(contourData);

                    databaseHelper.insertAllDataTableData(plotTableID, String.valueOf(id), String.valueOf(normalizedCenterY),
                            String.valueOf(normalizedTopY), String.valueOf(normalizedBaseY), String.valueOf(cv), String.valueOf(area),
                            "na", String.valueOf(volume), "na");
//
                    databaseHelper.insertVolumePlotTableData(volumePlotTableID, String.valueOf(id), String.valueOf(id), String.valueOf(volume));

                    // Print the RF value, area, and volume of the contour
                    Log.d("Contour " + i + " RF value", String.valueOf(rfValue));
                    Log.d("Contour " + i + " area", String.valueOf(area));
                    Log.d("Contour " + i + " volume", String.valueOf(volume));
//                }
                }

                String rf = "";
                // Calculate the RF value of all contours

                // Calculate the RF value, area, and volume of all contours

                Source.volumeDATA = volumeArrayList;

//            Source.rFvsAreaArrayList = rFvsAreaArrayList;
                Source.contourSetArrayList = contourSetArrayList;
                Source.contourDataArrayList = contourDataArrayList;

                Bitmap bitmap = Bitmap.createBitmap(firstImage.cols(), firstImage.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(firstImage, bitmap);

//            Toast.makeText(this, "Spotted", Toast.LENGTH_SHORT).show();
                Source.contourBitmap = bitmap;
                manuContBitmap = bitmap;
                imageAnalysisClass.saveImageViewToFile(bitmap, contourImageFileName, id, works, work);

                File outFile = new File(dir, contourImageFileName);
                if (outFile.exists()) {
                    Source.contourUri = Uri.fromFile(new File(outFile.getAbsolutePath()));
                }

                // Set the Bitmap in the ImageView
                captured_image.setImageBitmap(bitmap);

            } else if (matOfPointContourData.isEmpty() && !manualRectangles.isEmpty()) {
                Source.contourDataArrayList = new ArrayList<>();
                manualContourArrayList = new ArrayList<>();
                Source.volumeDATA = new ArrayList<>();

                if (Source.contourDataArrayList != null) {
                    Source.contourDataArrayList.clear();
                }
                if (manualContourArrayList != null) {

                    manualContourArrayList.clear();
                }
                if (Source.volumeDATA != null) {

                    Source.volumeDATA.clear();
                }

                Bitmap bitmap = bitImage;

                for (int i = 0; i < manualRectangles.size(); i++) {
                    android.graphics.Rect roi = manualRectangles.get(i);
                    String shape = shapeOfManualCont.get(i);
                    String id = idOfManualCont.get(i);

                    Log.e("ThisID", id);
//                    Toast.makeText(this, "" + id, Toast.LENGTH_SHORT).show();

                    if (shape.equals("1")) {
                        bitmap = RegionOfInterest.drawOvalWithROI(bitmap, roi.left, roi.top, roi.width(), roi.height());
                        manualContourArrayList.add(new ManualContour(1, roi, id, id, Source.rFvsAreaArrayList.size()));

//                        addManualContourToJSON("" + Source.contourDataArrayList.size(), roi, 1);

                    } else {
                        bitmap = RegionOfInterest.drawRectWithROI(bitmap, roi.left, roi.top, roi.width(), roi.height());
                        manualContourArrayList.add(new ManualContour(0, roi, id, id, Source.rFvsAreaArrayList.size()));
//                        addManualContourToJSON("" + Source.contourDataArrayList.size(), roi, 0);

                    }


                    int x = roi.left;
                    int y = roi.top;
                    int w = roi.width();
                    int h = roi.height();

                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setTextSize(30f);
                    paint.setStyle(Paint.Style.FILL);


                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawText(id, (float) x, (float) y, paint);


                    DecimalFormat df = new DecimalFormat("0.00E0");


                    Bitmap originalBitmap = GRAYSCALE_BITMAP/* your original Bitmap */;
                    int regionLeft = x; // Specify the left coordinate of the region
                    int regionTop = y; // Specify the top coordinate of the region
                    int regionWidth = w; // Specify the width of the region
                    int regionHeight = h; // Specify the height of the region


//                imageAnalysisClass.filterOutDarkPixelsInRegion(originalBitmap, regionLeft, regionTop, regionWidth, regionHeight);

//                    int autoThreshold =
//                            PixelThresholdClass.autoThresholdByPercentileFromGrayscaleInRegion(GRAYSCALE_BITMAP,
//                                    regionLeft, regionTop, regionWidth, regionHeight, 0.3);
//
////                int autoThreshold =
////                        imageAnalysisClass.autoThresholdFromGrayscaleInRegion(GRAYSCALE_BITMAP,
////                                regionLeft, regionTop, regionWidth, regionHeight);
////                int[] autoThreshold =
////                        PixelThresholdClass.calculateThresholdRangeByPercentage(GRAYSCALE_BITMAP,
////                                regionLeft, regionTop, regionWidth, regionHeight, 0.2, 0.4, 2);
//
//
////                Log.e("AutoThresHold", autoThreshold[0] + "" + autoThreshold[1]);
//                    Log.e("AutoThresHold", autoThreshold + "");
//
//                    originalBitmap = imageAnalysisClass.changeColorOfBrightPixelsInRegion(GRAYSCALE_BITMAP,
//                            regionLeft, regionTop, regionWidth, regionHeight, autoThreshold, Color.RED);
//
//                    binding.sampleImg.setImageBitmap(originalBitmap);
//
//                    int pixelArea = imageAnalysisClass.calculateDarkSpotsArea(
//                            GRAYSCALE_BITMAP,
//                            regionLeft, regionTop, regionWidth, regionHeight,
//                            autoThreshold
//                    );
//
//                    Log.e("AreaOfSpecificContour", pixelArea + "");


                    double area = RegionOfInterest.calculateOvalArea(w, h);
//                    double area = pixelArea;

                    double solventFrontDistance = w * 0.5 * (1.0 - 100.0 / 255.0);

                    double contourDistance = Math.sqrt(x * x + y * y);

                    double number = area * Math.abs(solventFrontDistance - contourDistance);
                    System.out.println(df.format(number));

                    double volume = Double.parseDouble(df.format(number));

                    int imageHeight = Source.contourBitmap.getHeight();
                    double distanceFromTop = (y + roi.bottom) * 0.5;

                    double rfValue4 = 1.0 - (distanceFromTop / (double) imageHeight);

                    double cv = 1 / rfValue4;

                    double rfValueTop = rfValue4 + (roi.height() / 2) / (double) imageHeight;
                    double rfValueBottom = rfValue4 - (roi.height() / 2) / (double) imageHeight;


                    contourDataArrayList.add(new ContourData(id, String.format("%.2f", rfValue4),
                            String.format("%.2f", rfValueTop), String.format("%.2f", rfValueBottom), String.format("%.2f", cv),
                            String.format("%.2f", area), String.valueOf(volume), "na"));


                    Source.contourDataArrayList.add(new ContourData(id, String.format("%.2f", rfValue4),
                            String.format("%.2f", rfValueTop), String.format("%.2f", rfValueBottom), String.format("%.2f", cv), String.format("%.2f", area),
                            String.valueOf(volume), "na"));

                    Log.d("RFvsArea", rfValueTop + " : " + 0.0);
                    Log.d("RFvsArea", rfValue4 + " : " + area);
                    Log.d("RFvsArea", rfValueBottom + " : " + 0.0);


                    databaseHelper.insertAllDataTableData(plotTableID, id, String.format("%.2f", rfValue4), String.format("%.2f", rfValueTop),
                            String.format("%.2f", rfValueBottom), String.format("%.2f", cv), String.format("%.2f", area),
                            "na", String.valueOf(volume), "na");
                    databaseHelper.insertVolumePlotTableData(volumePlotTableID, id, id, String.valueOf(volume));

                    // 1,2,3
                    // 0,1,2
                    // 1,1,1, 2,2,2, 3,3,3
                    // 0,1,2, 3,4,5, 6,7,8

                    Source.volumeDATA.add(volume);


                }

//                Toast.makeText(this, "this is pop" + Source.volumeDATA.size(), Toast.LENGTH_SHORT).show();

//            Toast.makeText(this, "Spotted", Toast.LENGTH_SHORT).show();
                Source.contourBitmap = bitmap;
                manuContBitmap = bitmap;
                imageAnalysisClass.saveImageViewToFile(bitmap, contourImageFileName, id, works, work);

                File outFile = new File(dir, contourImageFileName);
                if (outFile.exists()) {
                    Source.contourUri = Uri.fromFile(new File(outFile.getAbsolutePath()));
                }

                // Set the Bitmap in the ImageView
                captured_image.setImageBitmap(bitmap);


            } else {

                File outFile = new File(dir, contourImageFileName);

                if (outFile.exists()) {
                    outFile.delete();
                }

                Bitmap b = processImage();

//                saveImageViewToFile(b, contourImageFileName);
//            Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show();
            }
        } else {
            binding.anaL.spotDetection.spotContours.setText("Generate Spots");

        }
    }

    void removeManualContourFromJSON(String removeIndex, String fileName34) {
        String keyManualContours = "manualContour";
        Gson gson = new Gson();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(dir, fileName34)));

            Type mapType = new TypeToken<Map<String, List<Map<String, Object>>>>() {
            }.getType();
            Map<String, List<Map<String, Object>>> dataMap = gson.fromJson(bufferedReader, mapType);

            if (dataMap.containsKey(keyManualContours)) {
                List<Map<String, Object>> manualContours = dataMap.get(keyManualContours);
                List<Map<String, Object>> updatedManualContours = new ArrayList<>();

                for (Map<String, Object> manualContourData : manualContours) {
                    String id = (String) manualContourData.get("id");

                    if (id != null && !id.equals(removeIndex)) {
                        updatedManualContours.add(manualContourData);
                    }
                }

                // Update the data map with the new manual contours list
                dataMap.put(keyManualContours, updatedManualContours);

                // Write the updated data back to the JSON file
                try (FileWriter writer = new FileWriter(new File(dir, fileName34))) {
                    gson.toJson(dataMap, writer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                bufferedReader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void removeContourFromJSON(String removeIndex, String fileName34) {
//        String fileName34 = ""; // Provide the correct file name here
        String keyContours = "contoursData";

        Gson gson = new Gson();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(dir, fileName34)));

            Type mapType = new TypeToken<Map<String, List<Map<String, Object>>>>() {
            }.getType();
            Map<String, List<Map<String, Object>>> dataMap = gson.fromJson(bufferedReader, mapType);

            if (dataMap.containsKey(keyContours)) {
                List<Map<String, Object>> rawContours = dataMap.get(keyContours);
                List<Map<String, Object>> updatedContours = new ArrayList<>();

                for (Map<String, Object> contourData : rawContours) {
                    String id = (String) contourData.get("id");

                    if (id != null && !id.equals(String.valueOf(removeIndex))) {
                        updatedContours.add(contourData);
                    }
                }

                // Update the data map with the new contours list
                dataMap.put(keyContours, updatedContours);

                // Write the updated data back to the JSON file
                try (FileWriter writer = new FileWriter(new File(dir, fileName34))) {
                    gson.toJson(dataMap, writer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                bufferedReader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void plotContourRemove(String removeIndex, String contourType) {


        String fileName34 = "";

        if (work.equals(works[0]) || work.equals(works[1])) {
            fileName34 = "CD_" + id + ".json";
        }

        if (work.equals(works[2])) {
            fileName34 = "CD_" + getIntent().getStringExtra("pid").toString() + ".json";
        }

        String keyContours = "contoursData";
        String keyManualContours = "manualContour";

        List<List<Point>> contoursData = new ArrayList<>();
        ArrayList<ContourWithID> matOfPointContourData = new ArrayList<>();
        ArrayList<MatOfPoint> contData = new ArrayList<>();
        Gson gson = new Gson();
        ArrayList<android.graphics.Rect> manualRectangles = new ArrayList<>();
        ArrayList<String> shapeOfManualCont = new ArrayList<>();
        ArrayList<String> idOfManualCont = new ArrayList<>();
        ArrayList<String> idOfNormalCont = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(dir, fileName34)));

            Type mapType = new TypeToken<Map<String, List<Map<String, Object>>>>() {
            }.getType();
            Map<String, List<Map<String, Object>>> dataMap = gson.fromJson(bufferedReader, mapType);


            if (dataMap.containsKey(keyContours)) {
                List<Map<String, Object>> rawContours = dataMap.get(keyContours);
                for (Map<String, Object> contourData : rawContours) {
                    String id = (String) contourData.get("id");
                    System.out.println("id: " + id + ", removeIndex: " + String.valueOf(removeIndex)); // Debug statement

                    if (contourType.equals(keyContours)) {

                        if (id != null && !id.equals(String.valueOf(removeIndex))) {
                            List<Map<String, Double>> pointDataList = (List<Map<String, Double>>) contourData.get("data");
                            idOfNormalCont.add(id);

                            System.out.println("Adding contour with ID: " + id); // Debug statement

                            List<Point> points = new ArrayList<>();
                            for (Map<String, Double> pointData : pointDataList) {
                                double x = pointData.get("x");
                                double y = pointData.get("y");
                                points.add(new Point(x, y));
                            }
                            contoursData.add(points);
                            MatOfPoint matOfPoint = new MatOfPoint();
                            matOfPoint.fromList(points);
                            contData.add(matOfPoint);
                            matOfPointContourData.add(new ContourWithID(id, matOfPoint));
                        } else {
                            System.out.println("Skipping contour with ID: " + id); // Debug statement
                        }
                    } else {
                        List<Map<String, Double>> pointDataList = (List<Map<String, Double>>) contourData.get("data");

                        System.out.println("Adding contour with ID: " + id); // Debug statement

                        List<Point> points = new ArrayList<>();
                        for (Map<String, Double> pointData : pointDataList) {
                            double x = pointData.get("x");
                            double y = pointData.get("y");
                            points.add(new Point(x, y));
                        }
                        contoursData.add(points);
                        MatOfPoint matOfPoint = new MatOfPoint();
                        matOfPoint.fromList(points);
                        idOfNormalCont.add(id);
                        contData.add(matOfPoint);

                        matOfPointContourData.add(new ContourWithID(id, matOfPoint));
                    }
                }
            }
            if (dataMap.containsKey(keyManualContours)) {
                List<Map<String, Object>> manualContours = dataMap.get(keyManualContours);
                Log.d("ManualContoursSize", String.valueOf(manualContours.size())); // Add this line for debugging

                if (contourType.equals(keyManualContours)) {
                    for (Map<String, Object> manualContourData : manualContours) {
                        String id = (String) manualContourData.get("id");
                        String shape = (String) manualContourData.get("shape");
                        Map<String, Double> roiData = (Map<String, Double>) manualContourData.get("roi");

                        int left = (int) roiData.get("left").intValue();
                        int top = (int) roiData.get("top").intValue();
                        int right = (int) roiData.get("right").intValue();
                        int bottom = (int) roiData.get("bottom").intValue();

                        android.graphics.Rect rect = new android.graphics.Rect(left, top, right, bottom);


                        Log.e("left", left + "");
                        Log.e("top", top + "");
                        Log.e("right", right + "");
                        Log.e("bottom", bottom + "");
//
//                            android.graphics.Rect rect = new android.graphics.Rect(left, top, right, bottom);

                        if (id != null && !id.equals(String.valueOf(removeIndex))) {
                            manualRectangles.add(rect);
                            shapeOfManualCont.add(shape);
                            idOfManualCont.add(id);
                        } else {
                            System.out.println("Skipping contour with ID: " + id); // Debug statement
                        }

                        // Now you have the manual contour data in 'roiPoints'
                        // You can add it to your existing data structures as needed
                        // For example, add 'roiPoints' to contoursData, create MatOfPoint, and add it to contData and matOfPointContourData
                    }


                } else {
                    for (Map<String, Object> manualContourData : manualContours) {
                        String id = (String) manualContourData.get("id");
                        String shape = (String) manualContourData.get("shape");
                        Map<String, Double> roiData = (Map<String, Double>) manualContourData.get("roi");

                        int left = (int) roiData.get("left").intValue();
                        int top = (int) roiData.get("top").intValue();
                        int right = (int) roiData.get("right").intValue();
                        int bottom = (int) roiData.get("bottom").intValue();

                        android.graphics.Rect rect = new android.graphics.Rect(left, top, right, bottom);


                        Log.e("left", left + "");
                        Log.e("top", top + "");
                        Log.e("right", right + "");
                        Log.e("bottom", bottom + "");
//
//                            android.graphics.Rect rect = new android.graphics.Rect(left, top, right, bottom);


                        manualRectangles.add(rect);
                        shapeOfManualCont.add(shape);
                        idOfManualCont.add(id);


                        // Now you have the manual contour data in 'roiPoints'
                        // You can add it to your existing data structures as needed
                        // For example, add 'roiPoints' to contoursData, create MatOfPoint, and add it to contData and matOfPointContourData
                    }
                }
            }

            bufferedReader.close();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        if (!matOfPointContourData.isEmpty() || !manualRectangles.isEmpty()) {
            binding.anaL.spotDetection.spotContours.setText("Regenerate Spots");

        } else {
            binding.anaL.spotDetection.spotContours.setText("Generate Spots");
        }

        // saving data to json file - remove the contour
        // Call this function with the index you want to remove
        String indexToRemove = removeIndex; // Change this to the correct index

        if (contourType.equals(keyContours)) {
            removeContourFromJSON(indexToRemove, fileName34);
        }
        if (contourType.equals(keyManualContours)) {
            removeManualContourFromJSON(indexToRemove, fileName34);
        }
        // Now contoursData contains the remaining contours after removal

        // ... (rest of your code)

        contourList = new ArrayList<>();
        rFvsAreaArrayList = new ArrayList<>();
        contourSetArrayList = new ArrayList<>();
        volumeArrayList = new ArrayList<>();
        contourDataArrayList = new ArrayList<>();
//        contourWithDataList = new ArrayList<>();

        databaseHelper.deleteDataFromTable(volumePlotTableID);
//        databaseHelper.deleteDataFromTable(intensityPlotTableID);
        databaseHelper.deleteDataFromTable(plotTableID);


        if (work.equals(works[1])) {
            File outFile = new File(dir, projectImage);
            if (outFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

//                captured_image.setImageBitmap(myBitmap);
                bitImage = myBitmap;

            } else {
//                Source.toast(this, "Image not available or deleted");
            }
        }
        if (work.equals(works[0])) {
            try {
                bitImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(path));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (work.equals(works[2])) {
            File outFile = new File(dir, projectImage);
            if (outFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

//                captured_image.setImageBitmap(myBitmap);
                bitImage = myBitmap;
            } else {
//                Source.toast(this, "Image not available or deleted");
            }
        }

        Mat firstImage = new Mat();
        Utils.bitmapToMat(bitImage, firstImage);

        grayScaleImage = new

                Mat();
//        Imgproc.cvtColor(firstImage, grayScaleImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(firstImage, grayScaleImage, Imgproc.COLOR_BGR2GRAY);

        // Apply threshold to convert the grayscale image to a binary image
        Mat binary = new Mat();
        Imgproc.threshold(grayScaleImage, binary, threshVal, 255, 0);


//        // Find contours in the binary image
        contours = new ArrayList<>();


        Log.d("ContourSize", contData.size() + "");


        Scalar contourColor = new Scalar(255, 244, 143);
        int contourThickness = 2;
        int font = Imgproc.FONT_HERSHEY_SIMPLEX;
        double fontScale = 1;
        int fontThickness = 2;


//        Toast.makeText(this, "" + matOfPointContourData.size(), Toast.LENGTH_SHORT).show();

        if (!matOfPointContourData.isEmpty() && !manualRectangles.isEmpty()) {
            for (int i = 0; i < matOfPointContourData.size(); i++) {

                String nID = idOfNormalCont.get(i);

//                String id = matOfPointContourData.get(i).getId();
                String id = nID;

//                int t = i + 1;
//                Imgproc.drawContours(grayScaleImage, contourWithDataList, i, contourColor, contourThickness);
                Imgproc.drawContours(firstImage, contData, i, contourColor, contourThickness);
                // Get the bounding rectangle of the contour
                Rect boundingRect = Imgproc.boundingRect(contData.get(i));
                // Draw the contour number on the image
                Imgproc.putText(firstImage, "" + id, new Point(boundingRect.x, boundingRect.y - 5), font, fontScale, new Scalar(0, 0, 255), fontThickness);

                // Calculate the center point of the bounding box
                Point centerPoint = new Point(boundingRect.x + (boundingRect.width / 2), boundingRect.y + (boundingRect.height / 2));

                // Calculate the distance traveled by the center point
                double contourDistance = Math.sqrt(Math.pow(centerPoint.x, 2) + Math.pow(centerPoint.y, 2));

                // Calculate the distance traveled by the solvent front (assuming a linear gradient)
                double solventFrontDistance = ((double) grayScaleImage.width() / 2) * (1.0 - (double) threshVal / 255.0);

                // Calculate the RF value
                double rfValue = (10 - (contourDistance / solventFrontDistance)) / 10;
                contourList.add(Integer.parseInt(id));


                int height = boundingRect.height;

                double area = Imgproc.contourArea(contData.get(i));

                // assuming you have already found the contour and its height
                int topY = boundingRect.y;
                int baseY = topY + boundingRect.height;
                int centerY = topY + boundingRect.height / 2;

                // assuming you have a Bitmap object called "imageBitmap"
                int imageHeight = bitImage.getHeight();


// assuming you have the image height stored in a variable called "imageHeight"
                double normalizedTopY = 1 - ((double) topY / imageHeight);
                double normalizedBaseY = 1 - ((double) baseY / imageHeight);

                double normalizedCenterY = Double.parseDouble(String.format("%.2f", 1 - ((double) centerY / imageHeight)));

                double cv = 1 / normalizedCenterY;


                ArrayList<RFvsArea> ar = new ArrayList<>();
                ar.add(new RFvsArea(normalizedBaseY, 0));
                ar.add(new RFvsArea(normalizedCenterY, area));
                ar.add(new RFvsArea(normalizedTopY, 0));

                System.out.println("Height : " + height + ", Diameter : " + " - >>>>>>" + normalizedTopY + " , " + normalizedBaseY + " , " + normalizedCenterY);

//                rFvsAreaArrayList.addAll(ar);

                ArrayList<XY> xyArrayList = new ArrayList<>();
                xyArrayList.add(new XY(normalizedBaseY, 0));
                xyArrayList.add(new XY(normalizedCenterY, area));
                xyArrayList.add(new XY(normalizedTopY, 0));


                ContourSet contourSet = new ContourSet(xyArrayList);
                contourSetArrayList.add(contourSet);

//                     Calculate the area of the contour


                // Calculate the volume of the contour (assuming a cylindrical shape)
//            double volume = area * (solventFrontDistance - contourDistance);
                DecimalFormat df = new DecimalFormat("0.00E0");
                double number = area * Math.abs(solventFrontDistance - contourDistance);
                System.out.println(df.format(number));

                double volume = Double.parseDouble(df.format(number));

                volumeArrayList.add(volume);

                ContourData contourData = new ContourData(String.valueOf(nID), String.valueOf(normalizedCenterY),
                        String.valueOf(normalizedTopY), String.valueOf(normalizedBaseY), String.valueOf(cv),
                        String.valueOf(area), String.valueOf(volume), "na");
                contourDataArrayList.add(contourData);

                databaseHelper.insertAllDataTableData(plotTableID, String.valueOf(nID), String.valueOf(normalizedCenterY),
                        String.valueOf(normalizedTopY), String.valueOf(normalizedBaseY), String.valueOf(cv), String.valueOf(area),
                        "na", String.valueOf(volume), "na");

                databaseHelper.insertVolumePlotTableData(volumePlotTableID, String.valueOf(nID), nID, String.valueOf(volume));

                // Print the RF value, area, and volume of the contour
                Log.d("Contour " + i + " RF value", String.valueOf(rfValue));
                Log.d("Contour " + i + " area", String.valueOf(area));
                Log.d("Contour " + i + " volume", String.valueOf(volume));
//                }
            }

            String rf = "";
            // Calculate the RF value of all contours

            // Calculate the RF value, area, and volume of all contours

            Source.volumeDATA = volumeArrayList;

//            Source.rFvsAreaArrayList = rFvsAreaArrayList;
            Source.contourSetArrayList = contourSetArrayList;
            Source.contourDataArrayList = contourDataArrayList;

            Bitmap bitmap = Bitmap.createBitmap(firstImage.cols(), firstImage.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(firstImage, bitmap);

////            Toast.makeText(this, "Spotted", Toast.LENGTH_SHORT).show();
            Source.contourBitmap = bitmap;
            manuContBitmap = bitmap;

//            Toast.makeText(this, "Man" + manualRectangles.size(), Toast.LENGTH_SHORT).show();
            manualContourArrayList.clear();


            for (int i = 0; i < manualRectangles.size(); i++) {
                android.graphics.Rect roi = manualRectangles.get(i);
                String shape = shapeOfManualCont.get(i);
                String id = idOfManualCont.get(i);

                Log.e("ThisID", id);

                if (shape.equals("1")) {
                    bitmap = RegionOfInterest.drawOvalWithROI(bitmap, roi.left, roi.top, roi.width(), roi.height());
                    manualContourArrayList.add(new ManualContour(1, roi, id, id, Source.rFvsAreaArrayList.size()));

//                        addManualContourToJSON("" + Source.contourDataArrayList.size(), roi, 1);

                } else {
                    bitmap = RegionOfInterest.drawRectWithROI(bitmap, roi.left, roi.top, roi.width(), roi.height());
                    manualContourArrayList.add(new ManualContour(0, roi, id, id, Source.rFvsAreaArrayList.size()));
//                        addManualContourToJSON("" + Source.contourDataArrayList.size(), roi, 0);

                }


                int x = roi.left;
                int y = roi.top;
                int w = roi.width();
                int h = roi.height();

                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setTextSize(30f);
                paint.setStyle(Paint.Style.FILL);


                Canvas canvas = new Canvas(bitmap);
                canvas.drawText(id, (float) x, (float) y, paint);


                DecimalFormat df = new DecimalFormat("0.00E0");

                Bitmap originalBitmap = GRAYSCALE_BITMAP/* your original Bitmap */;
                int regionLeft = x; // Specify the left coordinate of the region
                int regionTop = y; // Specify the top coordinate of the region
                int regionWidth = w; // Specify the width of the region
                int regionHeight = h; // Specify the height of the region


//                imageAnalysisClass.filterOutDarkPixelsInRegion(originalBitmap, regionLeft, regionTop, regionWidth, regionHeight);

//                int autoThreshold =
//                        PixelThresholdClass.autoThresholdByPercentileFromGrayscaleInRegion(GRAYSCALE_BITMAP,
//                                regionLeft, regionTop, regionWidth, regionHeight, 0.3);
//
////                int autoThreshold =
////                        imageAnalysisClass.autoThresholdFromGrayscaleInRegion(GRAYSCALE_BITMAP,
////                                regionLeft, regionTop, regionWidth, regionHeight);
////                int[] autoThreshold =
////                        PixelThresholdClass.calculateThresholdRangeByPercentage(GRAYSCALE_BITMAP,
////                                regionLeft, regionTop, regionWidth, regionHeight, 0.2, 0.4, 2);
//
//
////                Log.e("AutoThresHold", autoThreshold[0] + "" + autoThreshold[1]);
//                Log.e("AutoThresHold", autoThreshold + "");
//
//                originalBitmap = imageAnalysisClass.changeColorOfBrightPixelsInRegion(GRAYSCALE_BITMAP,
//                        regionLeft, regionTop, regionWidth, regionHeight, autoThreshold, Color.RED);
//
//                binding.sampleImg.setImageBitmap(originalBitmap);
//
//                int pixelArea = imageAnalysisClass.calculateDarkSpotsArea(
//                        GRAYSCALE_BITMAP,
//                        regionLeft, regionTop, regionWidth, regionHeight,
//                        autoThreshold
//                );
//
//                Log.e("AreaOfSpecificContour", pixelArea + "");


                double area = RegionOfInterest.calculateOvalArea(w, h);
//                double area = pixelArea;


                double solventFrontDistance = w * 0.5 * (1.0 - 100.0 / 255.0);

                double contourDistance = Math.sqrt(x * x + y * y);

                double number = area * Math.abs(solventFrontDistance - contourDistance);
                System.out.println(df.format(number));

                double volume = Double.parseDouble(df.format(number));

                int imageHeight = Source.contourBitmap.getHeight();
                double distanceFromTop = (y + roi.bottom) * 0.5;

                double maxDistance = imageHeight;
                double rfValue4 = 1.0 - (distanceFromTop / maxDistance);

                double cv = 1 / rfValue4;

                double rfValueTop = rfValue4 + (roi.height() / 2) / (double) imageHeight;
                double rfValueBottom = rfValue4 - (roi.height() / 2) / (double) imageHeight;

                contourDataArrayList.add(new ContourData(id, String.format("%.2f", rfValue4),
                        String.format("%.2f", rfValueTop), String.format("%.2f", rfValueBottom),
                        String.format("%.2f", cv), String.format("%.2f", area), String.valueOf(volume), "na"));
                Log.d("RFvsArea", rfValueTop + " : " + 0.0);
                Log.d("RFvsArea", rfValue4 + " : " + area);
                Log.d("RFvsArea", rfValueBottom + " : " + 0.0);


                databaseHelper.insertAllDataTableData(plotTableID, id, String.format("%.2f", rfValue4), String.format("%.2f", rfValueTop),
                        String.format("%.2f", rfValueBottom), String.format("%.2f", cv), String.format("%.2f", area),
                        "na", String.valueOf(volume), "na");
                databaseHelper.insertVolumePlotTableData(volumePlotTableID, id, id, String.valueOf(volume));

                // 1,2,3
                // 0,1,2
                // 1,1,1, 2,2,2, 3,3,3
                // 0,1,2, 3,4,5, 6,7,8

                Source.volumeDATA.add(volume);


            }

//                Toast.makeText(this, "this is pop", Toast.LENGTH_SHORT).show();

//            Toast.makeText(this, "Spotted", Toast.LENGTH_SHORT).show();
            Source.contourBitmap = bitmap;
            manuContBitmap = bitmap;
            imageAnalysisClass.saveImageViewToFile(bitmap, contourImageFileName, id, works, work);

            File outFile = new File(dir, contourImageFileName);
            if (outFile.exists()) {
                Source.contourUri = Uri.fromFile(new File(outFile.getAbsolutePath()));
            }

            // Set the Bitmap in the ImageView
            captured_image.setImageBitmap(bitmap);
        } else if (!matOfPointContourData.isEmpty() && manualRectangles.isEmpty()) {

            for (int i = 0; i < matOfPointContourData.size(); i++) {

//                String id = matOfPointContourData.get(i).getId();
                String nID = idOfNormalCont.get(i);

//                String id = matOfPointContourData.get(i).getId();
                String id = nID;

//                int t = i + 1;
//                Imgproc.drawContours(grayScaleImage, contourWithDataList, i, contourColor, contourThickness);
                Imgproc.drawContours(firstImage, contData, i, contourColor, contourThickness);
                // Get the bounding rectangle of the contour
                Rect boundingRect = Imgproc.boundingRect(contData.get(i));
                // Draw the contour number on the image
                Imgproc.putText(firstImage, "" + id, new Point(boundingRect.x, boundingRect.y - 5), font, fontScale, new Scalar(0, 0, 255), fontThickness);

                // Calculate the center point of the bounding box
                Point centerPoint = new Point(boundingRect.x + (boundingRect.width / 2), boundingRect.y + (boundingRect.height / 2));

                // Calculate the distance traveled by the center point
                double contourDistance = Math.sqrt(Math.pow(centerPoint.x, 2) + Math.pow(centerPoint.y, 2));

                // Calculate the distance traveled by the solvent front (assuming a linear gradient)
                double solventFrontDistance = ((double) grayScaleImage.width() / 2) * (1.0 - (double) threshVal / 255.0);

                // Calculate the RF value
                double rfValue = (10 - (contourDistance / solventFrontDistance)) / 10;
                contourList.add(Integer.parseInt(id));


                int height = boundingRect.height;

                double area = Imgproc.contourArea(contData.get(i));

                // assuming you have already found the contour and its height
                int topY = boundingRect.y;
                int baseY = topY + boundingRect.height;
                int centerY = topY + boundingRect.height / 2;

                // assuming you have a Bitmap object called "imageBitmap"
                int imageHeight = bitImage.getHeight();


// assuming you have the image height stored in a variable called "imageHeight"
                double normalizedTopY = 1 - ((double) topY / imageHeight);
                double normalizedBaseY = 1 - ((double) baseY / imageHeight);

                double normalizedCenterY = Double.parseDouble(String.format("%.2f", 1 - ((double) centerY / imageHeight)));

                double cv = 1 / normalizedCenterY;


                ArrayList<RFvsArea> ar = new ArrayList<>();
                ar.add(new RFvsArea(normalizedBaseY, 0));
                ar.add(new RFvsArea(normalizedCenterY, area));
                ar.add(new RFvsArea(normalizedTopY, 0));

                System.out.println("Height : " + height + ", Diameter : " + " - >>>>>>" + normalizedTopY + " , " + normalizedBaseY + " , " + normalizedCenterY);

//                rFvsAreaArrayList.addAll(ar);

                ArrayList<XY> xyArrayList = new ArrayList<>();
                xyArrayList.add(new XY(normalizedBaseY, 0));
                xyArrayList.add(new XY(normalizedCenterY, area));
                xyArrayList.add(new XY(normalizedTopY, 0));


                ContourSet contourSet = new ContourSet(xyArrayList);
                contourSetArrayList.add(contourSet);

//                     Calculate the area of the contour


                // Calculate the volume of the contour (assuming a cylindrical shape)
//            double volume = area * (solventFrontDistance - contourDistance);
                DecimalFormat df = new DecimalFormat("0.00E0");
                double number = area * Math.abs(solventFrontDistance - contourDistance);
                System.out.println(df.format(number));

                double volume = Double.parseDouble(df.format(number));

                volumeArrayList.add(volume);

                ContourData contourData = new ContourData(nID, String.valueOf(normalizedCenterY),
                        String.valueOf(normalizedTopY), String.valueOf(normalizedBaseY), String.valueOf(cv),
                        String.valueOf(area), String.valueOf(volume), "na");
                contourDataArrayList.add(contourData);

                databaseHelper.insertAllDataTableData(plotTableID, nID, String.valueOf(normalizedCenterY), String.valueOf(normalizedTopY),
                        String.valueOf(normalizedBaseY), String.valueOf(cv), String.valueOf(area), "na",
                        String.valueOf(volume), "na");

                databaseHelper.insertVolumePlotTableData(volumePlotTableID, nID, nID, String.valueOf(volume));

                // Print the RF value, area, and volume of the contour
                Log.d("Contour " + i + " RF value", String.valueOf(rfValue));
                Log.d("Contour " + i + " area", String.valueOf(area));
                Log.d("Contour " + i + " volume", String.valueOf(volume));
//                }
            }

            String rf = "";
            // Calculate the RF value of all contours

            // Calculate the RF value, area, and volume of all contours

            Source.volumeDATA = volumeArrayList;

//            Source.rFvsAreaArrayList = rFvsAreaArrayList;
            Source.contourSetArrayList = contourSetArrayList;
            Source.contourDataArrayList = contourDataArrayList;

            Bitmap bitmap = Bitmap.createBitmap(firstImage.cols(), firstImage.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(firstImage, bitmap);

////            Toast.makeText(this, "Spotted", Toast.LENGTH_SHORT).show();
            Source.contourBitmap = bitmap;
            manuContBitmap = bitmap;
            imageAnalysisClass.saveImageViewToFile(bitmap, contourImageFileName, id, works, work);

            File outFile = new File(dir, contourImageFileName);
            if (outFile.exists()) {
                Source.contourUri = Uri.fromFile(new File(outFile.getAbsolutePath()));
            }

            // Set the Bitmap in the ImageView
            captured_image.setImageBitmap(bitmap);

            manualContourArrayList.clear();


        } else if (matOfPointContourData.isEmpty() && !manualRectangles.isEmpty()) {


            manualContourArrayList.clear();

            Bitmap bitmap = bitImage;

            for (int i = 0; i < manualRectangles.size(); i++) {
                android.graphics.Rect roi = manualRectangles.get(i);
                String shape = shapeOfManualCont.get(i);
                String id = idOfManualCont.get(i);

                Log.e("ThisID", id);

                if (shape.equals("1")) {
                    bitmap = RegionOfInterest.drawOvalWithROI(bitmap, roi.left, roi.top, roi.width(), roi.height());
                    manualContourArrayList.add(new ManualContour(1, roi, id, id, Source.rFvsAreaArrayList.size()));

//                        addManualContourToJSON("" + Source.contourDataArrayList.size(), roi, 1);

                } else {
                    bitmap = RegionOfInterest.drawRectWithROI(bitmap, roi.left, roi.top, roi.width(), roi.height());
                    manualContourArrayList.add(new ManualContour(0, roi, id, id, Source.rFvsAreaArrayList.size()));
//                        addManualContourToJSON("" + Source.contourDataArrayList.size(), roi, 0);

                }


                int x = roi.left;
                int y = roi.top;
                int w = roi.width();
                int h = roi.height();

                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setTextSize(30f);
                paint.setStyle(Paint.Style.FILL);


                Canvas canvas = new Canvas(bitmap);
                canvas.drawText(id, (float) x, (float) y, paint);


                DecimalFormat df = new DecimalFormat("0.00E0");


                Bitmap originalBitmap = GRAYSCALE_BITMAP/* your original Bitmap */;
                int regionLeft = x; // Specify the left coordinate of the region
                int regionTop = y; // Specify the top coordinate of the region
                int regionWidth = w; // Specify the width of the region
                int regionHeight = h; // Specify the height of the region


//                imageAnalysisClass.filterOutDarkPixelsInRegion(originalBitmap, regionLeft, regionTop, regionWidth, regionHeight);
//
//                int autoThreshold =
//                        PixelThresholdClass.autoThresholdByPercentileFromGrayscaleInRegion(GRAYSCALE_BITMAP,
//                                regionLeft, regionTop, regionWidth, regionHeight, 0.3);
//
////                int autoThreshold =
////                        imageAnalysisClass.autoThresholdFromGrayscaleInRegion(GRAYSCALE_BITMAP,
////                                regionLeft, regionTop, regionWidth, regionHeight);
////                int[] autoThreshold =
////                        PixelThresholdClass.calculateThresholdRangeByPercentage(GRAYSCALE_BITMAP,
////                                regionLeft, regionTop, regionWidth, regionHeight, 0.2, 0.4, 2);
//
//
////                Log.e("AutoThresHold", autoThreshold[0] + "" + autoThreshold[1]);
//                Log.e("AutoThresHold", autoThreshold + "");
//
//                originalBitmap = imageAnalysisClass.changeColorOfBrightPixelsInRegion(GRAYSCALE_BITMAP,
//                        regionLeft, regionTop, regionWidth, regionHeight, autoThreshold, Color.RED);
//
//                binding.sampleImg.setImageBitmap(originalBitmap);
//
//                int pixelArea = imageAnalysisClass.calculateDarkSpotsArea(
//                        GRAYSCALE_BITMAP,
//                        regionLeft, regionTop, regionWidth, regionHeight,
//                        autoThreshold
//                );
//
//                Log.e("AreaOfSpecificContour", pixelArea + "");


                double area = RegionOfInterest.calculateOvalArea(w, h);
//                double area = pixelArea;


                double solventFrontDistance = w * 0.5 * (1.0 - 100.0 / 255.0);

                double contourDistance = Math.sqrt(x * x + y * y);

                double number = area * Math.abs(solventFrontDistance - contourDistance);
                System.out.println(df.format(number));

                double volume = Double.parseDouble(df.format(number));

                int imageHeight = Source.contourBitmap.getHeight();
                double distanceFromTop = (y + roi.bottom) * 0.5;

                double maxDistance = imageHeight;
                double rfValue4 = 1.0 - (distanceFromTop / maxDistance);

                double cv = 1 / rfValue4;

                double rfValueTop = rfValue4 + (roi.height() / 2) / (double) imageHeight;
                double rfValueBottom = rfValue4 - (roi.height() / 2) / (double) imageHeight;

                contourDataArrayList.add(new ContourData(id, String.format("%.2f", rfValue4),
                        String.format("%.2f", rfValueTop), String.format("%.2f", rfValueBottom),
                        String.format("%.2f", cv), String.format("%.2f", area), String.valueOf(volume), "na"));


                Log.d("RFvsArea", rfValueTop + " : " + 0.0);
                Log.d("RFvsArea", rfValue4 + " : " + area);
                Log.d("RFvsArea", rfValueBottom + " : " + 0.0);

                databaseHelper.insertAllDataTableData(plotTableID, id, String.format("%.2f", rfValue4), String.format("%.2f", rfValueTop),
                        String.format("%.2f", rfValueBottom), String.format("%.2f", cv), String.format("%.2f", area),
                        "na", String.valueOf(volume), "na");
                databaseHelper.insertVolumePlotTableData(volumePlotTableID, id, id, String.valueOf(volume));

                // 1,2,3
                // 0,1,2
                // 1,1,1, 2,2,2, 3,3,3
                // 0,1,2, 3,4,5, 6,7,8

                Source.volumeDATA.add(volume);


            }

//                Toast.makeText(this, "this is pop", Toast.LENGTH_SHORT).show();

//            Toast.makeText(this, "Spotted", Toast.LENGTH_SHORT).show();
            Source.contourBitmap = bitmap;
            manuContBitmap = bitmap;
            imageAnalysisClass.saveImageViewToFile(bitmap, contourImageFileName, id, works, work);

            File outFile = new File(dir, contourImageFileName);
            if (outFile.exists()) {
                Source.contourUri = Uri.fromFile(new File(outFile.getAbsolutePath()));
            }

            // Set the Bitmap in the ImageView
            captured_image.setImageBitmap(bitmap);


        } else {

            File outFile = new File(dir, contourImageFileName);

            if (outFile.exists()) {
                outFile.delete();
            }

            Bitmap b = processImage();

        }


        finish();
        this.overridePendingTransition(0, 0);
        startActivity(getIntent());

    }

    @Override
    public void removeManualContour(int id, String mId, int rfId) {

        for (int i = 0; i < manualContourArrayList.size(); i++) {


            Source.volumeDATA.remove(Source.volumeDATA.size() - 1);
            Source.contourDataArrayList.remove(Source.contourDataArrayList.size() - 1);

            databaseHelper.deleteLastRow(volumePlotTableID);
            databaseHelper.deleteLastRow(intensityPlotTableID);
            databaseHelper.deleteLastRow(plotTableID);
        }

//        manualContourArrayList.remove(id);
        databaseHelper.deleteRowById("LABEL_" + plotTableID, mId);

        plotContourRemove(String.valueOf(mId), "manualContour");


//        plotNewManualContour(id);
//        plotContour(id);
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    public void addManualContourToJSON(String manualContourID, android.graphics.Rect roi,
                                       int shape) {

        JSONObject jsonObject = new JSONObject();

        String fileName34 = "";

        if (work.equals(works[0]) || work.equals(works[1])) {
            fileName34 = "CD_" + id + ".json";
        }

        if (work.equals(works[2])) {
            fileName34 = "CD_" + getIntent().getStringExtra("pid") + ".json";
        }

        if (!new File(dir, fileName34).exists()) {
            Gson gson = new Gson();


            // Adding contoursData
            try {

                jsonObject.put("contoursData", new JSONArray());
                jsonObject.put("manualContour", new JSONArray());

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            // Adding an empty manualContour array at the beginning

            if (work.equals(works[0]) || work.equals(works[1])) {
                fileName34 = "CD_" + id + ".json";
            }
            if (work.equals(works[2])) {
                fileName34 = "CD_" + getIntent().getStringExtra("pid") + ".json";
            }


            FileOutputStream fos = null;


            try {
                Writer output = null;
                File file = new File(dir, fileName34);
                output = new BufferedWriter(new FileWriter(file));
                output.write(jsonObject.toString());
                output.close();
                Toast.makeText(getApplicationContext(), "Composition saved", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }

        String keyManualContours = "manualContour";

        Gson gson = new Gson();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(dir, fileName34)));

            Type mapType = new TypeToken<Map<String, List<Map<String, Object>>>>() {
            }.getType();
            Map<String, List<Map<String, Object>>> dataMap = gson.fromJson(bufferedReader, mapType);

            // Create a new manual contour object
            Map<String, Object> manualContour = new HashMap<>();
            manualContour.put("id", manualContourID);  // Use "m" + manualContourID as ID
            manualContour.put("shape", String.valueOf(shape));  // Assuming 1 for manual contour shape
            manualContour.put("roi", roi);

            // Check if manualContours key exists, if not, create it
            if (!dataMap.containsKey(keyManualContours)) {
                dataMap.put(keyManualContours, new ArrayList<>());
            }

            // Add the manual contour object to the list
            dataMap.get(keyManualContours).add(manualContour);

            // Write the updated dataMap back to the JSON file
            Writer output = new BufferedWriter(new FileWriter(new File(dir, fileName34)));
            output.write(gson.toJson(dataMap));
            output.close();

            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private final ArrayList<String> manualContourIds = new ArrayList<String>();


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                if (Source.CROP_CODE == 2) {

                    android.graphics.Rect roi = result != null ? result.getCropRect() : null;
                    Bitmap bit;

//                    String s = "m" + Source.contourDataArrayList.size();

                    String indexName = imageAnalysisClass.generateUniqueIndexName(manualContourArrayList);


//                    s = Source.contourDataArrayList.size() + manualContourArrayList.size();

                    if (Source.shape == 1) {
                        bit = RegionOfInterest.drawOvalWithROI(Source.contourBitmap,
                                roi.left, roi.top, roi.width(), roi.height());

                        manualContourArrayList.add(new ManualContour(1, roi,
                                indexName,
                                indexName, Source.rFvsAreaArrayList.size()));

                        addManualContourToJSON("" + indexName, roi, 1);

                    } else {
                        bit = RegionOfInterest.drawRectWithROI(Source.contourBitmap, roi.left, roi.top, roi.width(), roi.height());
                        manualContourArrayList.add(new ManualContour(0, roi,
                                indexName,
                                indexName, Source.rFvsAreaArrayList.size()));
                        addManualContourToJSON("" + indexName, roi, 0);

                    }

                    int x = roi.left;
                    int y = roi.top;
                    int w = roi.width();
                    int h = roi.height();

                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setTextSize(30f);
                    paint.setStyle(Paint.Style.FILL);


                    Canvas canvas = new Canvas(bit);
                    canvas.drawText((indexName), (float) x, (float) y, paint);

                    DecimalFormat df = new DecimalFormat("0.00E0");

                    int idOf = Source.contourDataArrayList.size() + 1;

                    Bitmap originalBitmap = GRAYSCALE_BITMAP/* your original Bitmap */;
                    int regionLeft = x; // Specify the left coordinate of the region
                    int regionTop = y; // Specify the top coordinate of the region
                    int regionWidth = w; // Specify the width of the region
                    int regionHeight = h; // Specify the height of the region


//                imageAnalysisClass.filterOutDarkPixelsInRegion(originalBitmap, regionLeft, regionTop, regionWidth, regionHeight);

//                    int autoThreshold =
//                            PixelThresholdClass.autoThresholdByPercentileFromGrayscaleInRegion(GRAYSCALE_BITMAP,
//                                    regionLeft, regionTop, regionWidth, regionHeight, 0.3);
//
////                int autoThreshold =
////                        imageAnalysisClass.autoThresholdFromGrayscaleInRegion(GRAYSCALE_BITMAP,
////                                regionLeft, regionTop, regionWidth, regionHeight);
////                int[] autoThreshold =
////                        PixelThresholdClass.calculateThresholdRangeByPercentage(GRAYSCALE_BITMAP,
////                                regionLeft, regionTop, regionWidth, regionHeight, 0.2, 0.4, 2);
//
//
////                Log.e("AutoThresHold", autoThreshold[0] + "" + autoThreshold[1]);
//                    Log.e("AutoThresHold", autoThreshold + "");
//
//                    originalBitmap = imageAnalysisClass.changeColorOfBrightPixelsInRegion(GRAYSCALE_BITMAP,
//                            regionLeft, regionTop, regionWidth, regionHeight, autoThreshold, Color.RED);
//
//                    binding.sampleImg.setImageBitmap(originalBitmap);
//
//                    int pixelArea = imageAnalysisClass.calculateDarkSpotsArea(
//                            GRAYSCALE_BITMAP,
//                            regionLeft, regionTop, regionWidth, regionHeight,
//                            autoThreshold
//                    );
//
//                    Log.e("AreaOfSpecificContour", pixelArea + "");


                    double area = RegionOfInterest.calculateOvalArea(w, h);
//                    double area = pixelArea;


                    double solventFrontDistance = w * 0.5 * (1.0 - 100.0 / 255.0);

                    double contourDistance = Math.sqrt(x * x + y * y);

                    double number = area * Math.abs(solventFrontDistance - contourDistance);
                    System.out.println(df.format(number));

                    double volume = Double.parseDouble(df.format(number));

                    int imageHeight = Source.contourBitmap.getHeight();
                    double distanceFromTop = (roi.top + roi.bottom) * 0.5;

                    double maxDistance = imageHeight;
                    double rfValue4 = 1.0 - (distanceFromTop / maxDistance);

                    double cv = 1 / rfValue4;

                    double rfValueTop = rfValue4 + (roi.height() / 2) / (double) imageHeight;
                    double rfValueBottom = rfValue4 - (roi.height() / 2) / (double) imageHeight;

//                    Toast.makeText(this, "" + Source.contourDataArrayList.size(), Toast.LENGTH_SHORT).show();

                    contourDataArrayList.add(new ContourData(indexName, String.format("%.2f", rfValue4),
                            String.format("%.2f", rfValueTop), String.format("%.2f", rfValueBottom),
                            String.format("%.2f", cv), String.format("%.2f", area), String.valueOf(volume), "na"));

                    Toast.makeText(this, "" + Source.contourDataArrayList.size(), Toast.LENGTH_SHORT).show();

//                    Source.contourDataArrayList.add(new ContourData(indexName, String.format("%.2f", rfValue4),
//                            String.format("%.2f", rfValueTop), String.format("%.2f", rfValueBottom), String.format("%.2f", cv), String.format("%.2f", area), String.valueOf(volume)));
//                    Toast.makeText(this, "" + Source.contourDataArrayList.size(), Toast.LENGTH_SHORT).show();

                    Source.contourDataArrayList = contourDataArrayList;

                    Log.d("RFvsArea", rfValueTop + " : " + 0.0);
                    Log.d("RFvsArea", rfValue4 + " : " + area);
                    Log.d("RFvsArea", rfValueBottom + " : " + 0.0);

                    AnalysisTask task = new AnalysisTask();
                    task.execute(0);


                    databaseHelper.insertAllDataTableData(plotTableID, indexName, String.format("%.2f", rfValue4),
                            String.format("%.2f", rfValueTop), String.format("%.2f", rfValueBottom), String.format("%.2f", cv),
                            String.format("%.2f", area), "na", String.valueOf(volume), "na");

                    databaseHelper.insertSpotLabelData("LABEL_" + plotTableID, indexName, "na");


                    databaseHelper.insertVolumePlotTableData(
                            volumePlotTableID,
                            indexName,
                            indexName,
                            String.valueOf(volume));

                    // 1,2,3
                    // 0,1,2
                    // 1,1,1, 2,2,2, 3,3,3
                    // 0,1,2, 3,4,5, 6,7,8

                    Source.volumeDATA.add(volume);

                    boolean isThisAvailable = false;
                    int avaIndex = 0;
                    for (int i = 0; i < Source.splitContourDataList.size(); i++) {
                        if (Source.splitContourDataList.get(i).getMainImageName().equals(projectImage)) {
                            isThisAvailable = true;
                            avaIndex = i;
                        }
                    }
                    if (isThisAvailable) {
                        Source.splitContourDataList.remove(avaIndex);
                    }
                    String finalType = getIntent().getStringExtra("type");
                    String imgName = null;
                    if (finalType.equals("multi")) {
                        imgName = getIntent().getStringExtra("imageName");
                    }
                    if (finalType.equals("mainImg")) {
//            imgName = "Main Image";
                        imgName = getIntent().getStringExtra("imageName");

                    }

                    Source.splitContourDataList.add(new SplitContourData(imgName, true,
                            contourImageFileName,
                            projectImage, hr, rmSpot, finalSpot, volumeArrayList, rFvsAreaArrayList,
                            contourSetArrayList, contourDataArrayList, labelDataArrayList));
//        Toast.makeText(this, "" + volumeArrayList.size(), Toast.LENGTH_SHORT).show();


                    String imageFileName = contourImageFileName;

                    imageAnalysisClass.saveImageViewToFile(bit, imageFileName, id, works, work);

                    File outFile = new File(dir, imageFileName);
                    if (outFile.exists()) {
                        Source.contourUri = Uri.fromFile(new File(outFile.getAbsolutePath()));
                    }

                    Source.contourBitmap = bit;

                    BitmapDrawable drawable = new BitmapDrawable(getResources(), bit);
                    drawnShapesStack.push(drawable);


                    usersDatabase.logUserAction(AuthDialog.activeUserName, AuthDialog.activeUserRole, "Manual Contours Added", getIntent().getStringExtra("projectName"), getIntent().getStringExtra("id"), AuthDialog.projectType);

                    k++;

                    captured_image.setImageBitmap(bit);

                    Source.CROP_CODE = 0;
                }
                if (Source.CROP_CODE == 1) {

                    try {
                        if (result != null) {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), result.getUri());

                            imageAnalysisClass.saveImageViewToFile(bitmap, projectImage, id, works, work);

                            captured_image.setImageURI(result != null ? result.getUri() : null);
                            Source.changeRoi = false;

                            usersDatabase.logUserAction(AuthDialog.activeUserName, AuthDialog.activeUserRole, "Changed ROI", getIntent().getStringExtra("projectName"), getIntent().getStringExtra("id"), AuthDialog.projectType);

                            Source.CROP_CODE = 0;

                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("CropImageError", "Error cropping image: " + error.getMessage());
            }
        }
    }

    private void setupDrawSpotListener(int visibility) {

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.crop_options, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(NewImageAnalysis.this).setView(dialogView);

        final AlertDialog alertDialog = builder.create();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button rectangleCont = dialogView.findViewById(R.id.rectangleCont);
        Button circleCont = dialogView.findViewById(R.id.circleCont);
        Button saveIt = dialogView.findViewById(R.id.saveIt);
        CheckBox circleCheckBox = dialogView.findViewById(R.id.circleCheckBox);
        CheckBox rectangleCheckBox = dialogView.findViewById(R.id.rectangleCheckBox);

        if (visibility == 0) {
            circleCheckBox.setVisibility(View.GONE);
        }
        if (visibility == 1) {
            rectangleCheckBox.setVisibility(View.GONE);

        }
        if (visibility == 2) {
            circleCheckBox.setVisibility(View.VISIBLE);
            rectangleCheckBox.setVisibility(View.VISIBLE);

        }


        rectangleCheckBox.setChecked(true);
        circleCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (circleCheckBox.isChecked()) {
                    rectangleCheckBox.setChecked(false);
                    circleCheckBox.setChecked(true);

                } else {
                    circleCheckBox.setChecked(false);
                    rectangleCheckBox.setChecked(true);

                }

            }
        });

        rectangleCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rectangleCheckBox.isChecked()) {
                    rectangleCheckBox.setChecked(true);
                    circleCheckBox.setChecked(false);

                } else {
                    circleCheckBox.setChecked(true);
                    rectangleCheckBox.setChecked(false);

                }

            }
        });


        saveIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rectangleCheckBox.isChecked()) {
                    Source.shape = 0;
                    alertDialog.dismiss();
                    Source.CROP_CODE = 2;

                    Source.WIDTH_OF_IMAGE = Source.contourBitmap.getWidth();
                    Source.HEIGHT_OF_IMAGE = Source.contourBitmap.getHeight();
                    Source.workingWithRectangleContour = true;

                    if (manualContourArrayList != null) {
                        Source.manualContourArrayList = manualContourArrayList;
                    } else {
                        Source.manualContourArrayList = new ArrayList<>();
                    }

                    Intent i = new Intent(NewImageAnalysis.this, DrawRectangleCont.class);
                    startActivity(i);


                }
                if (circleCheckBox.isChecked()) {
                    Source.shape = 1;
                    alertDialog.dismiss();
                    Source.CROP_CODE = 2;
                    CropImage.activity(Source.contourUri)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setCropShape(CropImageView.CropShape.OVAL)
                            .setInitialRotation(90)
//                            .setRotationDegrees(90)
                            .start(NewImageAnalysis.this);
                }
            }
        });


        rectangleCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Source.shape = 0;
                alertDialog.dismiss();
                Source.CROP_CODE = 2;
                CropImage.activity(Source.contourUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
//                        .setMinCropWindowSize(10,10)

//                        .setSnapRadius(0)
                        .start(NewImageAnalysis.this);
            }
        });

        circleCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Source.shape = 1;
                alertDialog.dismiss();
                Source.CROP_CODE = 2;
                CropImage.activity(Source.contourUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropShape(CropImageView.CropShape.OVAL)

                        .start(NewImageAnalysis.this);
            }
        });

        alertDialog.show();

    }

    @Override
    public void removeContour(int id) {
        databaseHelper.deleteRowById("LABEL_" + plotTableID, String.valueOf(id));

        plotContourRemove(String.valueOf(id), "contoursData");

//        plotContour(id);
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }


    Mat originalImageCopy;

    public void spotContour() {

        usersDatabase.logUserAction(AuthDialog.activeUserName, AuthDialog.activeUserRole, "Spotted", getIntent().getStringExtra("projectName"), getIntent().getStringExtra("id"), AuthDialog.projectType);

        contourList = new ArrayList<>();
//        rFvsAreaArrayList = new ArrayList<>();
        contourSetArrayList = new ArrayList<>();

        intensityVsRFArray = new ArrayList<>();

        databaseHelper.deleteDataFromTable(volumePlotTableID);
//        databaseHelper.deleteDataFromTable(intensityPlotTableID);
        databaseHelper.deleteDataFromTable(plotTableID);
        databaseHelper.deleteDataFromTable("LABEL_" + plotTableID);

        if (work.equals(works[1])) {
            File outFile = new File(dir, projectImage);
            if (outFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

//                captured_image.setImageBitmap(myBitmap);
                bitImage = myBitmap;

            } else {
//                Source.toast(this, "Image not available or deleted");
            }
        }
        if (work.equals(works[0])) {


            bitImage = bitImageArray[0];

        }

        if (work.equals(works[2])) {
            File outFile = new File(dir, projectImage);
            if (outFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

//                captured_image.setImageBitmap(myBitmap);
                bitImage = myBitmap;
            } else {
//                Source.toast(this, "Image not available or deleted");
            }
        }

        Mat firstImage = new Mat();
        Utils.bitmapToMat(bitImage, firstImage);

        originalImageCopy = new Mat();
        firstImage.copyTo(originalImageCopy);


        // grayscale
        grayScaleImage = new Mat();
        Imgproc.cvtColor(firstImage, grayScaleImage, Imgproc.COLOR_BGR2GRAY);

        double alpha = 1.5; // Contrast control
        double beta = 10; // Brightness control

        adjustedImage = grayScaleImage;

        // Apply threshold to convert the grayscale image to a binary image
        Mat binary = new Mat();
        Imgproc.threshold(adjustedImage, binary, threshVal, 255, 0);

        // Find contours in the binary image
        contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);


        Log.d("ContourSize", contours.size() + "");
// Sort contours by volume in descending order
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint c1, MatOfPoint c2) {
                double area1 = Imgproc.contourArea(c1);
                double area2 = Imgproc.contourArea(c2);
                return Double.compare(area2, area1);
            }
        });

        Log.d("ContourSize", contours.size() + "");


        plotContour();

    }


    public void plotContour() {


        manualContourArrayList.clear();

        contourWithDataList = new ArrayList<>();

        Scalar contourColor = new Scalar(255, 244, 143);
        int contourThickness = 2;
        int font = Imgproc.FONT_HERSHEY_SIMPLEX;
        double fontScale = 1;
        int fontThickness = 2;

        adjustedImage = originalImageCopy;


        if (numberCount != 0) {
            int range = numberCount + 1;
            System.out.println("1. This : " + range + " &" + contours.size());

            if (range > contours.size()) {
//                Toast.makeText(this, "Only " + contours.size() + " number of spots available, You can check with different threshold value", Toast.LENGTH_LONG).show();
            } else {

                /////////////////////////////////////////////
                List<JSONObject> contoursData = new ArrayList<>();  // New list for contours data
                JSONArray contoursDataArray = new JSONArray();
                JSONObject jsonObject = new JSONObject();


                /////////////////////////////////////////////////

                for (int i = 1; i < range; i++) {
                    contourWithDataList.add(contours.get(i));

                    ///////////////////////////////////////////
                    JSONObject contourDatam = new JSONObject();
                    try {
                        contourDatam.put("id", String.valueOf(i));
                        JSONArray dataArray = new JSONArray();
                        List<Point> points = contours.get(i).toList();
                        for (Point point : points) {
                            JSONObject pointObject = new JSONObject();
                            pointObject.put("x", point.x);
                            pointObject.put("y", point.y);
                            dataArray.put(pointObject);
                        }
                        contourDatam.put("data", dataArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    contoursDataArray.put(contourDatam);
                    ////////////////////////////////////////////

                    Imgproc.drawContours(adjustedImage, contours, i, contourColor, contourThickness);
                    // Get the bounding rectangle of the contour
                    Rect boundingRect = Imgproc.boundingRect(contours.get(i));
                    // Draw the contour number on the image
                    contourList.add(i);
                    Imgproc.putText(adjustedImage, "" + i, new Point(boundingRect.x, boundingRect.y - 5), font, fontScale, new Scalar(0, 0, 255), fontThickness);
//
                    Point centerPoint = new Point(boundingRect.x + (boundingRect.width / 2), boundingRect.y + (boundingRect.height / 2));

                    double f = imageAnalysisClass.calculateIntensity(adjustedImage, contours.get(i));

                    Log.d("ContourIntensity", String.valueOf(f));

                    // Calculate the distance traveled by the center point
                    double contourDistance = Math.sqrt(Math.pow(centerPoint.x, 2) + Math.pow(centerPoint.y, 2));

                    // Calculate the distance traveled by the solvent front (assuming a linear gradient)
                    double solventFrontDistance = ((double) adjustedImage.width() / 2) * (1.0 - (double) threshVal / 255.0);

                    // Calculate the RF value
                    double rfValue = (10 - (contourDistance / solventFrontDistance)) / 10;


                    int height = boundingRect.height;
                    // we assume there is only one contour in the image
                    MatOfPoint contour = contours.get(0);
                    //


                    // calculate the height of the contour
                    double contourHeight = Imgproc.boundingRect(contour).height;
                    double area = Imgproc.contourArea(contours.get(i));

                    // assuming you have already found the contour and its height
                    int topY = boundingRect.y;
                    int baseY = topY + boundingRect.height;
                    int centerY = topY + boundingRect.height / 2;

                    // assuming you have a Bitmap object called "imageBitmap"
                    int imageHeight = bitImage.getHeight();


// assuming you have the image height stored in a variable called "imageHeight"
                    double normalizedTopY = 1 - ((double) topY / imageHeight);
                    double normalizedBaseY = 1 - ((double) baseY / imageHeight);

                    double normalizedCenterY = Double.parseDouble(String.format("%.2f", 1 - ((double) centerY / imageHeight)));

                    double cv = 1 / normalizedCenterY;


                    ArrayList<RFvsArea> ar = new ArrayList<>();
                    ar.add(new RFvsArea(normalizedBaseY, 0));
                    ar.add(new RFvsArea(normalizedCenterY, area));
                    ar.add(new RFvsArea(normalizedTopY, 0));

                    System.out.println("Height : " + height + ", Diameter : " + " - >>>>>>" + normalizedTopY + " , " + normalizedBaseY + " , " + normalizedCenterY);

//                    rFvsAreaArrayList.addAll(ar);

                    intensityVsRFArray.add(new RFvsArea(normalizedCenterY, f));


                    ArrayList<XY> xyArrayList = new ArrayList<>();
                    xyArrayList.add(new XY(normalizedBaseY, 0));
                    xyArrayList.add(new XY(normalizedCenterY, area));
                    xyArrayList.add(new XY(normalizedTopY, 0));


                    ContourSet contourSet = new ContourSet(xyArrayList);
                    contourSetArrayList.add(contourSet);


//                     Calculate the area of the contour


                    // Calculate the volume of the contour (assuming a cylindrical shape)
//            double volume = area * (solventFrontDistance - contourDistance);
                    DecimalFormat df = new DecimalFormat("0.00E0");
                    double number = area * Math.abs(solventFrontDistance - contourDistance);
                    System.out.println(df.format(number));

                    double volume = Double.parseDouble(df.format(number));
//
//
                    databaseHelper.insertVolumePlotTableData(volumePlotTableID, String.valueOf(i), String.valueOf(i), String.valueOf(volume));

                    volumeArrayList.add(volume);


                    ContourData contourData = new ContourData(String.valueOf(i), String.valueOf(normalizedCenterY),
                            String.valueOf(normalizedTopY), String.valueOf(normalizedBaseY), String.valueOf(cv), String.valueOf(area),
                            String.valueOf(volume), "na");
                    contourDataArrayList.add(contourData);

                    databaseHelper.insertAllDataTableData(plotTableID, String.valueOf(i), String.valueOf(normalizedCenterY),
                            String.valueOf(normalizedTopY), String.valueOf(normalizedBaseY), String.valueOf(cv),
                            String.valueOf(area), "na", String.valueOf(volume), "na");

                    databaseHelper.insertSpotLabelData("LABEL_" + plotTableID, String.valueOf(i), "na");


                    // Print the RF value, area, and volume of the contour
                    Log.d("Contour " + i + " RF value", String.valueOf(rfValue));
                    Log.d("Contour " + i + " area", String.valueOf(area));
                    Log.d("Contour " + i + " volume", String.valueOf(volume));
                }

                ////////////////////////////////////////////////////

                Gson gson = new Gson();


                // Adding contoursData
                try {
                    jsonObject.put("contoursData", contoursDataArray);
                    jsonObject.put("manualContour", new JSONArray());

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                // Adding an empty manualContour array at the beginning

                String fileName34 = "";
                if (work.equals(works[0]) || work.equals(works[1])) {
                    fileName34 = "CD_" + id + ".json";
                }
                if (work.equals(works[2])) {
                    fileName34 = "CD_" + getIntent().getStringExtra("pid") + ".json";
                }


                FileOutputStream fos = null;


                try {
                    Writer output = null;
                    File file = new File(dir, fileName34);
                    output = new BufferedWriter(new FileWriter(file));
                    output.write(jsonObject.toString());
                    output.close();
                    Toast.makeText(getApplicationContext(), "Composition saved", Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }


                // Loading Contours
                List<List<Point>> loadedContoursData = new ArrayList<>();
                try {
                    JSONArray contoursDataArrays = jsonObject.getJSONArray("contoursData");

                    for (int i = 0; i < contoursDataArrays.length(); i++) {
                        JSONObject contourObject = contoursDataArrays.getJSONObject(i);
                        JSONArray data = contourObject.getJSONArray("data");

                        List<Point> contourPoints = new ArrayList<>();
                        for (int j = 0; j < data.length(); j++) {
                            JSONObject pointObject = data.getJSONObject(j);
                            double x = pointObject.getDouble("x");
                            double y = pointObject.getDouble("y");
                            contourPoints.add(new Point(x, y));
                        }

                        loadedContoursData.add(contourPoints);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

// Now, loadedContoursData contains the loaded contours as List<List<Point>>

                ArrayList<MatOfPoint> loadedContours = new ArrayList<>();
                for (List<Point> contourPoints : loadedContoursData) {
                    MatOfPoint contour = new MatOfPoint();
                    contour.fromList(contourPoints);
                    loadedContours.add(contour);
                }


                ///////////////////////////////////////////

            }
            refinedContour = contourWithDataList;
        }


        String rf = "";
        // Calculate the RF value of all contours

        // Calculate the RF value, area, and volume of all contours

        Source.volumeDATA = volumeArrayList;

//        Source.rFvsAreaArrayList = new ArrayList<>();
        Source.contourSetArrayList = contourSetArrayList;
        Source.contourDataArrayList = contourDataArrayList;


        Source.intensityVsRFArray = intensityVsRFArray;

        boolean isThisAvailable = false;
        int avaIndex = 0;
        for (int i = 0; i < Source.splitContourDataList.size(); i++) {
            if (Source.splitContourDataList.get(i).getMainImageName().equals(projectImage)) {
                isThisAvailable = true;
                avaIndex = i;
            }
        }
        if (isThisAvailable) {
            Source.splitContourDataList.remove(avaIndex);
        }
        String finalType = getIntent().getStringExtra("type");
        String imgName = null;
        if (finalType.equals("multi")) {
            imgName = getIntent().getStringExtra("imageName");
        }
        if (finalType.equals("mainImg")) {
            imgName = "Main Image";
        }
//        ggrgr
//        Source.splitContourDataList.add(new SplitContourData(imgName, true, contourImageFileName, projectImage, volumeArrayList, rFvsAreaArrayList, contourSetArrayList, contourDataArrayList));
        Source.splitContourDataList.add(new SplitContourData(imgName, true, contourImageFileName, projectImage, hr, rmSpot, finalSpot,
                volumeArrayList, Source.rFvsAreaArrayList, contourSetArrayList, contourDataArrayList
                , labelDataArrayList
        ));
//

        binding.anaL.spotDetection.spotContours.setText("Regenerate Spots");

        Bitmap bitmap = Bitmap.createBitmap(adjustedImage.cols(), adjustedImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(adjustedImage, bitmap);

//        Toast.makeText(this, "Spotted", Toast.LENGTH_SHORT).show();
        Source.contourBitmap = bitmap;
        manuContBitmap = bitmap;
        imageAnalysisClass.saveImageViewToFile(bitmap, contourImageFileName, id, works, work);

        File outFile = new File(dir, contourImageFileName);
        if (outFile.exists()) {
            Source.contourUri = Uri.fromFile(new File(outFile.getAbsolutePath()));
        }

        // Set the Bitmap in the ImageView
        captured_image.setImageBitmap(bitmap);
//        removeContour();
    }

    public Mat imageToMat() {
        Bitmap bitImage = null;
        if (work.equals(works[1])) {
            File outFile = new File(dir, projectImage);
            if (outFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

//                captured_image.setImageBitmap(myBitmap);
                bitImage = myBitmap;
            } else {
//                Source.toast(this, "Image not avaialable or deleted");
            }
        }
        if (work.equals(works[0])) {
            try {
                bitImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(path));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (work.equals(works[2])) {
            File outFile = new File(dir, projectImage);
            if (outFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

//                captured_image.setImageBitmap(myBitmap);
                bitImage = myBitmap;
            } else {
//                Source.toast(this, "Image not available or deleted");
            }
        }

        imageMat = new Mat();
        Utils.bitmapToMat(bitImage, imageMat);
        return imageMat;
    }

    Mat imageMat;

    private Bitmap processImage() {
        Bitmap bitImage = null;
        if (work.equals(works[1])) {
            File outFile = new File(dir, projectImage);
            if (outFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

//                captured_image.setImageBitmap(myBitmap);
                bitImage = myBitmap;
            } else {
//                Source.toast(this, "Image not avaialable or deleted");
            }
        }
        if (work.equals(works[0])) {
            try {
                bitImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(path));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (work.equals(works[2])) {
            File outFile = new File(dir, projectImage);
            if (outFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

//                captured_image.setImageBitmap(myBitmap);
                bitImage = myBitmap;
            } else {
//                Source.toast(this, "Image not available or deleted");
            }
        }

//        Bitmap originalBitmap = bitImage/* your original Bitmap */;
//        int regionLeft = 100; // Specify the left coordinate of the region
//        int regionTop = 100; // Specify the top coordinate of the region
//        int regionWidth = 200; // Specify the width of the region
//        int regionHeight = 200; // Specify the height of the region
//
//        imageAnalysisClass.filterOutDarkPixelsInRegion(originalBitmap, regionLeft, regionTop, regionWidth, regionHeight);


        image = new Mat();
        Utils.bitmapToMat(bitImage, image);


        grayImage = new Mat();
        grayImage = image;
        invertedImage = grayImage;

        double alpha = 1.5; // Contrast control
        double beta = 10; // Brightness control


        Mat adjustedImage1 = invertedImage;
//        Mat adjustedImage1 = new Mat();
//        invertedImage.convertTo(adjustedImage1, CvType.CV_8U, alpha, beta);


        Bitmap bitmapGray = Bitmap.createBitmap(adjustedImage1.cols(), adjustedImage1.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(adjustedImage1, bitmapGray);


        GRAYSCALE_BITMAP = imageAnalysisClass.convertToGrayAndReturnBitmap(adjustedImage1);

        if (!Source.rectangle) {
            originalBitmapForPixel = GRAYSCALE_BITMAP;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(NewImageAnalysis.this, "Running", Toast.LENGTH_SHORT).show();
            }
        });
//        Source.originalImageUri = Uri.parse(getIntent().getStringExtra("img_path"));
        Source.roiBitmap = bitmapGray;
//        grayImgUri = getImageUri(ImageProcess.this, bitmapGray);

        Source.contourBitmap = bitmapGray;

        captured_image.setImageBitmap(bitImage);

        spotContours.setEnabled(true);
        getPixels.setEnabled(true);

        File outFile = new File(dir, contourImageFileName);

        if (outFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

            Source.contourBitmap = myBitmap;

            Toast.makeText(this, "True", Toast.LENGTH_SHORT).show();

            captured_image.setImageBitmap(myBitmap);
        } else {
            Toast.makeText(this, "False", Toast.LENGTH_SHORT).show();
            Source.contourBitmap = bitImage;

//            Source.toast(this, "Spot image not available");
        }

//        return bitmapGray;
        return bitImage;

    }

    public void onResume() {
        super.onResume();

        imageToMat();
        Source.imageMat = imageMat;

        String INTENSITY_PART_KEY = "INTENSITY_PART_KEY_" + id;

        if (SharedPrefData.getSavedData(
                NewImageAnalysis.this,
                INTENSITY_PART_KEY
        ) != null && SharedPrefData.getSavedData(
                NewImageAnalysis.this,
                INTENSITY_PART_KEY
        ) != ""
        ) {
            String data =
                    SharedPrefData.getSavedData(NewImageAnalysis.this, INTENSITY_PART_KEY);

            Source.PARTS_INTENSITY = Integer.parseInt(data);
//            Toast.makeText(this, "" + data, Toast.LENGTH_SHORT).show();

        } else {
//            Toast.makeText(this, "100", Toast.LENGTH_SHORT).show();
            SharedPrefData.saveData(NewImageAnalysis.this, INTENSITY_PART_KEY, "1000");
            Source.PARTS_INTENSITY = 1000;
        }

        String ANALYSIS_MODE_KEY = "ANALYSIS_MODE_KEY" + id;

        if (work.equals(works[2])) {
            ANALYSIS_MODE_KEY = "ANALYSIS_MODE_KEY" + getIntent().getStringExtra("pid");
        } else {
            ANALYSIS_MODE_KEY = "ANALYSIS_MODE_KEY" + id;
        }

        if (SharedPrefData.getSavedData(
                NewImageAnalysis.this,
                ANALYSIS_MODE_KEY
        ) != null && SharedPrefData.getSavedData(
                NewImageAnalysis.this,
                ANALYSIS_MODE_KEY
        ) != ""
        ) {
            String data =
                    SharedPrefData.getSavedData(NewImageAnalysis.this, ANALYSIS_MODE_KEY);
            Source.ANALYSIS_MODE = data;

        } else {
            SharedPrefData.saveData(NewImageAnalysis.this, ANALYSIS_MODE_KEY, "SPOT");
            Source.ANALYSIS_MODE = "SPOT";
        }

        if (Source.ANALYSIS_MODE.equals("SPOT")) {
            binding.anaL.spotDetectTab.setBackgroundDrawable(getDrawable(R.drawable.blue_tab_bac3_red));
            binding.anaL.bandDetectTab.setBackgroundDrawable(getDrawable(R.drawable.tab_bc));
            binding.anaL.spotDetectTab.setTextColor(getColor(R.color.white));
            binding.anaL.bandDetectTab.setTextColor(getColor(R.color.aican_blue));


            binding.anaL.bandDetection.bandDetectionLay.setVisibility(View.GONE);
            binding.anaL.spotDetection.spotDetectionLay.setVisibility(View.VISIBLE);
        }
        if (Source.ANALYSIS_MODE.equals("BAND")) {
            binding.anaL.bandDetectTab.setBackgroundDrawable(getDrawable(R.drawable.blue_tab_bac3_red));
            binding.anaL.spotDetectTab.setBackgroundDrawable(getDrawable(R.drawable.tab_bc));

            binding.anaL.spotDetectTab.setTextColor(getColor(R.color.aican_blue));
            binding.anaL.bandDetectTab.setTextColor(getColor(R.color.white));


            binding.anaL.bandDetection.bandDetectionLay.setVisibility(View.VISIBLE);
            binding.anaL.spotDetection.spotDetectionLay.setVisibility(View.GONE);
        }
        String finalANALYSIS_MODE_KEY = ANALYSIS_MODE_KEY;

        binding.anaL.spotDetectTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Source.ANALYSIS_MODE.equals("BAND")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewImageAnalysis.this);
                    alertDialogBuilder.setMessage("Are you sure you want to change the project type to Spot Analysis Mode? It will delete all the Band Analysis Data");
                    alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            binding.anaL.spotDetectTab.setBackgroundDrawable(getDrawable(R.drawable.blue_tab_bac3_red));
                            binding.anaL.bandDetectTab.setBackgroundDrawable(getDrawable(R.drawable.tab_bc));

                            SharedPrefData.saveData(NewImageAnalysis.this, finalANALYSIS_MODE_KEY, "SPOT");
                            Source.ANALYSIS_MODE = "SPOT";

                            binding.anaL.spotDetectTab.setTextColor(getColor(R.color.white));
                            binding.anaL.bandDetectTab.setTextColor(getColor(R.color.aican_blue));


                            binding.anaL.bandDetection.bandDetectionLay.setVisibility(View.GONE);
                            binding.anaL.spotDetection.spotDetectionLay.setVisibility(View.VISIBLE);


                            File outFile = new File(dir, contourImageFileName);

                            if (outFile.exists()) {
                                outFile.delete();
                            }

                            Bitmap b = processImage();

                            binding.anaL.spotDetection.spotContours.setText("Generate Spots");

                            String fileName34 = "";
                            if (work.equals(works[0]) || work.equals(works[1])) {
                                fileName34 = "CD_" + id + ".json";
                            }

                            if (work.equals(works[2])) {
                                fileName34 = "CD_" + getIntent().getStringExtra("pid").toString() + ".json";
                            }

                            File myDir = new File(dir, fileName34);

                            if (myDir.exists()) {
                                Gson gson = new Gson();

                                BufferedReader bufferedReader = null;
                                try {
                                    bufferedReader = new BufferedReader(new FileReader(new File(dir, fileName34)));


                                    Type mapType = new TypeToken<Map<String, List<Map<String, Object>>>>() {
                                    }.getType();
                                    Map<String, List<Map<String, Object>>> dataMap = gson.fromJson(bufferedReader, mapType);


                                    dataMap.put("contoursData", new ArrayList<>());
                                    dataMap.put("manualContour", new ArrayList<>());

                                    // Write the updated dataMap back to the JSON file
                                    try {
                                        Writer output = new BufferedWriter(new FileWriter(new File(dir, fileName34)));
                                        output.write(gson.toJson(dataMap));
                                        output.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } catch (FileNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            // Clear data structures in your code
                            if (contourList != null) {
                                contourList.clear();
                            } else {
                                contourList = new ArrayList<>();
                            }
//                            rFvsAreaArrayList.clear();
                            contourSetArrayList.clear();
                            volumeArrayList.clear();
                            contourDataArrayList.clear();

                            manualContourArrayList.clear();
                            contourList.clear();

//                saveImageViewToFile(b, contourImageFileName);

                            contourList = new ArrayList<>();
                            rFvsAreaArrayList = new ArrayList<>();
                            contourSetArrayList = new ArrayList<>();
                            volumeArrayList = new ArrayList<>();
                            contourDataArrayList = new ArrayList<>();
//        contourWithDataList = new ArrayList<>();

                            Source.volumeDATA = new ArrayList<>();
                            Source.contourDataArrayList = new ArrayList<>();
//                            Source.rFvsAreaArrayList = new ArrayList<>();
                            Source.splitContourDataList = new ArrayList<>();


                            databaseHelper.deleteDataFromTable(volumePlotTableID);
//                        databaseHelper.deleteDataFromTable(intensityPlotTableID);
                            databaseHelper.deleteDataFromTable(plotTableID);


                        }
                    });
                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                }

            }
        });

        binding.anaL.bandDetectTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Source.ANALYSIS_MODE.equals("SPOT")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewImageAnalysis.this);
                    alertDialogBuilder.setMessage("Are you sure you want to change the project type to Band Analysis Mode? It will delete all the Spot Analysis Data");
                    alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            binding.anaL.bandDetectTab.setBackgroundDrawable(getDrawable(R.drawable.blue_tab_bac3_red));
                            binding.anaL.spotDetectTab.setBackgroundDrawable(getDrawable(R.drawable.tab_bc));

                            SharedPrefData.saveData(NewImageAnalysis.this, finalANALYSIS_MODE_KEY, "BAND");
                            Source.ANALYSIS_MODE = "BAND";

                            binding.anaL.spotDetectTab.setTextColor(getColor(R.color.aican_blue));
                            binding.anaL.bandDetectTab.setTextColor(getColor(R.color.white));

                            binding.anaL.bandDetection.bandDetectionLay.setVisibility(View.VISIBLE);
                            binding.anaL.spotDetection.spotDetectionLay.setVisibility(View.GONE);


                            File outFile = new File(dir, contourImageFileName);

                            if (outFile.exists()) {
                                outFile.delete();
                            }

                            Bitmap b = processImage();

                            binding.anaL.spotDetection.spotContours.setText("Generate Spots");

                            String fileName34 = "";
                            if (work.equals(works[0]) || work.equals(works[1])) {
                                fileName34 = "CD_" + id + ".json";
                            }

                            if (work.equals(works[2])) {
                                fileName34 = "CD_" + getIntent().getStringExtra("pid").toString() + ".json";
                            }

                            File myDir = new File(dir, fileName34);

                            if (myDir.exists()) {
                                Gson gson = new Gson();

                                BufferedReader bufferedReader = null;
                                try {
                                    bufferedReader = new BufferedReader(new FileReader(new File(dir, fileName34)));


                                    Type mapType = new TypeToken<Map<String, List<Map<String, Object>>>>() {
                                    }.getType();
                                    Map<String, List<Map<String, Object>>> dataMap = gson.fromJson(bufferedReader, mapType);


                                    dataMap.put("contoursData", new ArrayList<>());
                                    dataMap.put("manualContour", new ArrayList<>());

                                    // Write the updated dataMap back to the JSON file
                                    try {
                                        Writer output = new BufferedWriter(new FileWriter(new File(dir, fileName34)));
                                        output.write(gson.toJson(dataMap));
                                        output.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } catch (FileNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            // Clear data structures in your code
                            if (contourList != null) {
                                contourList.clear();
                            } else {
                                contourList = new ArrayList<>();
                            }
//                            rFvsAreaArrayList.clear();
                            contourSetArrayList.clear();
                            volumeArrayList.clear();
                            contourDataArrayList.clear();

                            manualContourArrayList.clear();
                            contourList.clear();

//                saveImageViewToFile(b, contourImageFileName);

                            contourList = new ArrayList<>();
                            rFvsAreaArrayList = new ArrayList<>();
                            contourSetArrayList = new ArrayList<>();
                            volumeArrayList = new ArrayList<>();
                            contourDataArrayList = new ArrayList<>();
//        contourWithDataList = new ArrayList<>();

                            Source.volumeDATA = new ArrayList<>();
                            Source.contourDataArrayList = new ArrayList<>();
//                            Source.rFvsAreaArrayList = new ArrayList<>();
                            Source.splitContourDataList = new ArrayList<>();


                            databaseHelper.deleteDataFromTable(volumePlotTableID);
//                        databaseHelper.deleteDataFromTable(intensityPlotTableID);
                            databaseHelper.deleteDataFromTable(plotTableID);

                        }
                    });
                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                }


            }
        });


        if (Source.manual) {
            captured_image.setImageBitmap(Source.contourBitmap);
//            Toast.makeText(this, "Source.manual", Toast.LENGTH_SHORT).show();
            binding.analyzerLine.setVisibility(View.GONE);
            binding.roiLine.setVisibility(View.GONE);
            binding.plotterLine.setVisibility(View.VISIBLE);

            binding.anaL.analyzerLayout.setVisibility(View.GONE);
            binding.roiL.roiLayout.setVisibility(View.GONE);
            binding.plotL.plotterLayout.setVisibility(View.VISIBLE);
            Source.manual = false;

        }

        if (Source.rectangle) {

            for (int ikk = 0; ikk < Source.rectangleList.size(); ikk++) {


                android.graphics.Rect roi = Source.rectangleList.get(ikk);
                Bitmap bit;

//                    String s = "m" + Source.contourDataArrayList.size();

                String indexName = imageAnalysisClass.generateUniqueIndexName(manualContourArrayList);


//                    s = Source.contourDataArrayList.size() + manualContourArrayList.size();

                if (Source.shape == 1) {
                    bit = RegionOfInterest.drawOvalWithROI(Source.contourBitmap, roi.left, roi.top, roi.width(), roi.height());
//
                    manualContourArrayList.add(new ManualContour(1, roi,
                            indexName,
                            indexName, Source.rFvsAreaArrayList.size()));

                    addManualContourToJSON("" + indexName, roi, 1);

                } else {
                    bit = RegionOfInterest.drawRectWithROI(Source.contourBitmap, roi.left, roi.top, roi.width(), roi.height());
                    manualContourArrayList.add(new ManualContour(0, roi,
                            indexName,
                            indexName, Source.rFvsAreaArrayList.size()));
                    addManualContourToJSON("" + indexName, roi, 0);

                }

                int x = roi.left;
                int y = roi.top;
                int w = roi.width();
                int h = roi.height();

                binding.sampleImg.setImageBitmap(GRAYSCALE_BITMAP);

                /* your original Bitmap */
                ;
                int regionLeft = x; // Specify the left coordinate of the region
                int regionTop = y; // Specify the top coordinate of the region
                int regionWidth = w; // Specify the width of the region
                int regionHeight = h; // Specify the height of the region


//                imageAnalysisClass.filterOutDarkPixelsInRegion(originalBitmap, regionLeft, regionTop, regionWidth, regionHeight);

//                int autoThreshold =
//                        PixelThresholdClass.autoThresholdByPercentileFromGrayscaleInRegion(originalBitmapForPixel,
//                                regionLeft, regionTop, regionWidth, regionHeight, 0.3);

//                int autoThreshold =
//                        imageAnalysisClass.autoThresholdFromGrayscaleInRegion(GRAYSCALE_BITMAP,
//                                regionLeft, regionTop, regionWidth, regionHeight);
//                int[] autoThreshold =
//                        PixelThresholdClass.calculateThresholdRangeByPercentage(GRAYSCALE_BITMAP,
//                                regionLeft, regionTop, regionWidth, regionHeight, 0.2, 0.4, 2);


//                Log.e("AutoThresHold", autoThreshold[0] + "" + autoThreshold[1]);
//                Log.e("AutoThresHold", autoThreshold + "");
//
//                originalBitmapForPixel = imageAnalysisClass.changeColorOfBrightPixelsInRegion(originalBitmapForPixel,
//                        regionLeft, regionTop, regionWidth, regionHeight, autoThreshold, Color.RED);
//
//                binding.sampleImg.setImageBitmap(originalBitmapForPixel);
//
//                int pixelArea = imageAnalysisClass.calculateDarkSpotsArea(
//                        originalBitmapForPixel,
//                        regionLeft, regionTop, regionWidth, regionHeight,
//                        autoThreshold
//                );

//                Log.e("AreaOfSpecificContour", pixelArea + "");

                //1526

                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setTextSize(30f);
                paint.setStyle(Paint.Style.FILL);


                Canvas canvas = new Canvas(bit);
                canvas.drawText((indexName), (float) x, (float) y, paint);

                DecimalFormat df = new DecimalFormat("0.00E0");

                int idOf = Source.contourDataArrayList.size() + 1;


                double area = RegionOfInterest.calculateRectangleArea(w, h);
//                double area = pixelArea;

                double solventFrontDistance = w * 0.5 * (1.0 - 100.0 / 255.0);

                double contourDistance = Math.sqrt(x * x + y * y);

                double number = area * Math.abs(solventFrontDistance - contourDistance);
                System.out.println(df.format(number));

                double volume = Double.parseDouble(df.format(number));

                int imageHeight = Source.contourBitmap.getHeight();
                double distanceFromTop = (y + roi.bottom) * 0.5;

                double maxDistance = imageHeight;
                double rfValue4 = 1.0 - (distanceFromTop / maxDistance);

                double cv = 1 / rfValue4;

                double rfValueTop = rfValue4 + (roi.height() / 2) / (double) imageHeight;
                double rfValueBottom = rfValue4 - (roi.height() / 2) / (double) imageHeight;

//                PixelGraph.checkForSpotsInRegion(imageMat,
//                        String.valueOf(rfValueTop), String.valueOf(rfValueBottom));


                contourDataArrayList.add(new ContourData(indexName, String.format("%.2f", rfValue4),
                        String.format("%.2f", rfValueTop), String.format("%.2f", rfValueBottom),
                        String.format("%.2f", cv), String.format("%.2f", area), String.valueOf(volume), "na"));

                Log.d("RFvsArea", rfValueTop + " : " + 0.0);
                Log.d("RFvsArea", rfValue4 + " : " + area);
                Log.d("RFvsArea", rfValueBottom + " : " + 0.0);


                databaseHelper.insertAllDataTableData(plotTableID, indexName, String.format("%.2f", rfValue4), String.format("%.2f", rfValueTop),
                        String.format("%.2f", rfValueBottom), String.format("%.2f", cv), String.format("%.2f", area), "na",
                        String.valueOf(volume), "na");

                databaseHelper.insertSpotLabelData("LABEL_" + plotTableID, indexName, "na");

                databaseHelper.insertVolumePlotTableData(volumePlotTableID, indexName, indexName, String.valueOf(volume));

                // 1,2,3
                // 0,1,2
                // 1,1,1, 2,2,2, 3,3,3
                // 0,1,2, 3,4,5, 6,7,8

                Source.volumeDATA.add(volume);

                boolean isThisAvailable = false;
                int avaIndex = 0;
                for (int i = 0; i < Source.splitContourDataList.size(); i++) {
                    if (Source.splitContourDataList.get(i).getMainImageName().equals(projectImage)) {
                        isThisAvailable = true;
                        avaIndex = i;
                    }
                }
                if (isThisAvailable) {
                    Source.splitContourDataList.remove(avaIndex);
                }
                String finalType = getIntent().getStringExtra("type");
                String imgName = null;
                if (finalType.equals("multi")) {
                    imgName = getIntent().getStringExtra("imageName");
                }

                if (finalType.equals("mainImg")) {
//            imgName = "Main Image";
                    imgName = getIntent().getStringExtra("imageName");

                }

                Source.splitContourDataList.add(new SplitContourData(imgName, true,
                        contourImageFileName,
                        projectImage, hr, rmSpot, finalSpot, volumeArrayList, rFvsAreaArrayList,
                        contourSetArrayList, contourDataArrayList, labelDataArrayList));
//        Toast.makeText(this, "" + volumeArrayList.size(), Toast.LENGTH_SHORT).show();


                String imageFileName = contourImageFileName;

                imageAnalysisClass.saveImageViewToFile(bit, imageFileName, id, works, work);

                File outFile = new File(dir, imageFileName);
                if (outFile.exists()) {
                    Source.contourUri = Uri.fromFile(new File(outFile.getAbsolutePath()));
                }

                Source.contourBitmap = bit;

                BitmapDrawable drawable = new BitmapDrawable(getResources(), bit);
                drawnShapesStack.push(drawable);


                usersDatabase.logUserAction(AuthDialog.activeUserName, AuthDialog.activeUserRole, "Manual Contours Added", getIntent().getStringExtra("projectName"), getIntent().getStringExtra("id"), AuthDialog.projectType);

                k++;

                captured_image.setImageBitmap(bit);


            }


            Source.CROP_CODE = 0;
            Source.rectangle = false;
            Source.contourDataArrayList = contourDataArrayList;
            if (Source.rectangleOneActivityToPixelActivity) {
                Source.rectangleOneActivityToPixelActivity = false;
                AnalysisTask task = new AnalysisTask();
                task.execute(1);
            }


        }
        AnalysisTask task = new AnalysisTask();
        task.execute(0);

        settingLabelData();

        settingDataFromDatabase();

        if (Source.contourBaselineEdited) {
            Source.contourBaselineEdited = false;
            AnalysisTask task1 = new AnalysisTask();
            task1.execute(1);
        }

        if (Source.removingContourFromAdapter && !Source.removingContourID.equals("null")
                && Source.spotPositionFromAdapter != -1) {

//            LoadingDialog.showLoading(this, false, false, "Deleting");

            if (Source.removingContourID.contains("m")) {
                removeManualContour(Source.spotPositionFromAdapter, Source.removingContourID
                        , Source.spotPositionFromAdapter);
            } else {
                removeContour(Integer.parseInt(Source.removingContourID));
            }
            Source.spotPositionFromAdapter = -1;
            Source.removingContourID = "null";
            Source.removingContourFromAdapter = false;
            AnalysisTask task1 = new AnalysisTask();
            task1.execute(1);
        }

    }

    public static boolean hasDarkerPixels(Mat grayMat, double rfTop, double rfBottom) {
        int imageWidth = grayMat.cols(); // Width of the grayscale image

        // Calculate the Y-coordinates for the start and end of the region based on rfTop and rfBottom
        int startY = (int) (rfTop * Source.PARTS_INTENSITY * grayMat.rows());
        int endY = (int) (rfBottom * Source.PARTS_INTENSITY * grayMat.rows());

        // Ensure startY and endY are within the valid range
        startY = Math.max(0, Math.min(startY, grayMat.rows() - 1));
        endY = Math.max(0, Math.min(endY, grayMat.rows() - 1));

        // Check if startY and endY are valid (region is within the image bounds and has valid height)
        if (startY >= grayMat.rows() || endY < 0 || endY - startY <= 0) {
            // Handle invalid region (out of bounds or empty region)
            return false;
        }

        // Iterate through the pixels within the specified region
        for (int y = startY; y <= endY; y++) {
            for (int x = 0; x < imageWidth; x++) {
                double intensityValue = grayMat.get(y, x)[0]; // Get the intensity value of the pixel

                // Check if the intensity value is below a threshold (considered as "darker" pixel)
                double intensityThreshold = 150; // Adjust this threshold as needed
                if (intensityValue < intensityThreshold) {
                    return true; // Found a darker pixel within the region
                }
            }
        }


        return false; // No darker pixels found within the region
    }


    private void processImage1() {
        Bitmap bitImage = null;
        try {
            bitImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(path));

        } catch (IOException e) {
            e.printStackTrace();
        }


        image = new Mat();
        Utils.bitmapToMat(bitImage, image);

        Mat blurredImage = new Mat();
        Imgproc.GaussianBlur(image, blurredImage, new Size(3, 3), 0);


        grayImage = new Mat();
        Imgproc.cvtColor(blurredImage, grayImage, Imgproc.COLOR_BGR2GRAY);

//
//        Mat equalized = new Mat();
//        Imgproc.equalizeHist(grayImage, equalized);

        invertedImage = grayImage;


        Bitmap bitmapGray = Bitmap.createBitmap(invertedImage.cols(), invertedImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(invertedImage, bitmapGray);


        captured_image.setImageBitmap(bitmapGray);

        spotContours.setEnabled(true);
        getPixels.setEnabled(true);

    }

    @Override
    public void onAuthenticationSuccess() {
        usersDatabase.logUserAction(AuthDialog.activeUserName, AuthDialog.activeUserRole, "Open Analysis Page", getIntent().getStringExtra("projectName"), getIntent().getStringExtra("id"), AuthDialog.projectType);
    }

    @Override
    protected void onStart() {
        super.onStart();

//        Toast.makeText(this, "On Start", Toast.LENGTH_SHORT).show();
        //

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {

            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//            if (!Source.showContourImg && !Source.manual && !Source.changeRoi) {
//                processImage();
//            }

//            if (processed) {
            processImage();


            populateTheContourDataFromJSON();

//                processed = false;
//            }

        }

        //
    }

    public static int refreshMainSplitImage = 0;

    @Override
    public void onBackPressed() {
        if (work.equals(works[2])) {
            refreshMainSplitImage = 1;
        }
        Source.retake = false;
        super.onBackPressed();
    }

    @Override
    public void onClick(int position, int parentPosition, @NonNull String id, @NonNull String rfTop, @NonNull String rfBottom, @NonNull String rf, boolean isSelected) {

        float mRFTop = Float.parseFloat(rfTop) * Source.PARTS_INTENSITY;
        float mRFBottom = Float.parseFloat(rfBottom) * Source.PARTS_INTENSITY;
        float mRF = Float.parseFloat(rf) * Source.PARTS_INTENSITY;

        Log.e("ThisIsNotAnError", "Top : " + mRFTop + " Bottom : " + mRFBottom + " RF : " + mRF);

        ArrayList<ContourGraphSelModel> contourGraphSelModelArrayList = new ArrayList<>();

        for (int i = 0; i < arrayListCont.size(); i++) {
            if (arrayListCont.get(i).isSelected()) {
                if (contourDataArrayListNew.get(i).isSelected()) {
                    contourGraphSelModelArrayList.add(new ContourGraphSelModel(
                            contourDataArrayListNew.get(i).getRfTop(),
                            contourDataArrayListNew.get(i).getRfBottom(),
                            contourDataArrayListNew.get(i).getRf(),
                            contourDataArrayListNew.get(i).getId(),
                            contourDataArrayListNew.get(i).getButtonColor()
                    ));
                }
            }
        }

        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        lineDataSets.add(lineDataSet);
        highLightMyRegion(contourGraphSelModelArrayList, lineDataSets);

    }

    @Override
    public void newOnClick(int position) {

    }

    @Override
    public void editManualContour(int id, @NonNull String mId, int rfId, android.graphics.Rect rect) {

        if (alertDialog != null) {
            alertDialog.dismiss();
        }

        Intent intent = new Intent(NewImageAnalysis.this, EditRectangleContour.class);
        Source.editRectangleContourRect = rect;
        intent.putExtra("plotTableName", plotTableID);
        intent.putExtra("contourJsonFileName", contourJsonFileName);
        intent.putExtra("pId", this.id);
        intent.putExtra("spotId", mId);
        startActivity(intent);


    }

    private class AnalysisTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LoadingDialog.showLoading(NewImageAnalysis.this, false, false, "Processing...");
        }

        int typee = 0;

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Integer... integers) {
            typee = integers[0];
            if (integers[0] == 1) {
                performAnalysis(integers[0]);
            } else if (integers[0] == 0 || integers[0] == 15) {
                getIntensityData();
            }
//            performAnalysis(integers[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            LoadingDialog.cancelLoading();
            if (typee == 15) {
                showData();

                if (information.size() > 0) {

                    highlightAndDisplayAllRegions();
                } else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(NewImageAnalysis.this, "First plot the intensity graph", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }


            // This method is called on the main thread after the background task is finished.
            // You can update UI elements here if needed.
        }
    }


    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i("OpenCV", "OpenCV loaded successfully");
                rgba = new Mat();
            } else {
                super.onManagerConnected(status);
            }
        }
    };
}
