package com.aican.tlcanalyzer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aican.tlcanalyzer.database.DatabaseHelper;
import com.aican.tlcanalyzer.database.UsersDatabase;
import com.aican.tlcanalyzer.dialog.AuthDialog;
import com.aican.tlcanalyzer.utils.SharedPrefData;
import com.aican.tlcanalyzer.utils.Source;
import com.aican.tlcanalyzer.utils.Subscription;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CroppingTemp extends AppCompatActivity {

    private ImageView imageView;
    private Mat originalImage;
    private ArrayList<Integer> verticalLinesXCoordinates = new ArrayList<>();
    File dir;
    Mat rgba;
    private Bitmap loadedBitmap;
    DatabaseHelper databaseHelper;
    UsersDatabase usersDatabase;
    String id, projectImage;
    String tableName;

    int sizeOfMainImageList = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropping_window_view);


        getSupportActionBar().hide();
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadOpenCV();

        usersDatabase = new UsersDatabase(this);

        databaseHelper = new DatabaseHelper(this);
        imageView = findViewById(R.id.imageView);
//        Uri imageUri = Uri.parse(getIntent().getStringExtra("img_path"));

        id = getIntent().getStringExtra("id").toString();
        projectImage = getIntent().getStringExtra("projectImage").toString();
        tableName = getIntent().getStringExtra("tableName").toString();

        dir = new File(new ContextWrapper(this).getExternalMediaDirs()[0], getResources().getString(R.string.app_name) + id);


        if (!SplitImage.Companion.getAddingMainImage()) {

            File outFile = new File(dir, projectImage);
            if (outFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

//                captured_image.setImageBitmap(myBitmap);
                loadedBitmap = myBitmap;
                imageView.setImageBitmap(loadedBitmap);
            } else {
//            Source.toast(this, "Image not available or deleted");

                Bitmap bitmap = CapturedImagePreview.Companion.getSplitBitmap();
                //                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                saveImageViewToFile(bitmap, getIntent().getStringExtra("projectImage"), this);

                String tempFileName = "TEMP" + getIntent().getStringExtra("projectImage");

                String originalFileName = "ORG_" + getIntent().getStringExtra("projectImage");

                Bitmap mainImageBitmap = bitmap;

                // here main image is saving
                saveImageToDownloads(mainImageBitmap, projectImage, this);
                saveImageViewToFile(mainImageBitmap, tempFileName, this);
                saveImageViewToFile(CapturedImagePreview.Companion.getOriginalBitmap(), originalFileName, this);

                loadedBitmap = bitmap;
                imageView.setImageBitmap(loadedBitmap);
            }
        } else {

            sizeOfMainImageList = SplitImage.Companion.getSizeOfMainImagesList();

            File outFile = new File(dir, projectImage + "_" + sizeOfMainImageList);
            if (outFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());

//                captured_image.setImageBitmap(myBitmap);
                loadedBitmap = myBitmap;
                imageView.setImageBitmap(loadedBitmap);
            } else {
//            Source.toast(this, "Image not available or deleted");

                Bitmap bitmap = CapturedImagePreview.Companion.getSplitBitmap();
                //                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                saveImageViewToFile(bitmap, getIntent().getStringExtra("projectImage") + "_" + sizeOfMainImageList, this);

                String tempFileName = "TEMP" + getIntent().getStringExtra("projectImage") + "_" + sizeOfMainImageList;
                String originalFileName = "ORG_" + getIntent().getStringExtra("projectImage") + "_" + sizeOfMainImageList;

                Bitmap mainImageBitmap = bitmap;


                // here main image is saving

                saveImageToDownloads(mainImageBitmap, projectImage, this);
                saveImageViewToFile(mainImageBitmap, tempFileName, this);
                saveImageViewToFile(CapturedImagePreview.Companion.getOriginalBitmap(), originalFileName, this);

                loadedBitmap = bitmap;
                imageView.setImageBitmap(loadedBitmap);
            }
        }
        if (loadedBitmap != null) {
            originalImage = new Mat();
            Utils.bitmapToMat(loadedBitmap, originalImage);

            imageView.setImageBitmap(loadedBitmap);
        } else {
            Toast.makeText(this, "Null img", Toast.LENGTH_SHORT).show();

        }

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int touchX = (int) event.getX();
                    int touchY = (int) event.getY();

                    // Calculate the touch coordinates relative to the displayed image bounds
                    int originalImageX = (int) ((float) touchX / v.getWidth() * originalImage.cols());
                    int originalImageY = (int) ((float) touchY / v.getHeight() * originalImage.rows());

                    verticalLinesXCoordinates.add(originalImageX);

                    drawVerticalLinesOnImage();
                    return true;
                }
                return false;
            }
        });


        Button btnFinalSlicing = findViewById(R.id.btnFinalSlicing);

        btnFinalSlicing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (verticalLinesXCoordinates != null) {
                    if (verticalLinesXCoordinates.size() > 0) {

                        if (!SplitImage.Companion.getAddingMainImage()) {
                            databaseHelper.createSplitTable(tableName);

                            SharedPrefData.saveData(CroppingTemp.this, SharedPrefData.PR_ACTUAL_LIMIT_KEY, String.valueOf(Subscription.NO_OF_PROJECTS_MADE + 1));

                            String mainImageTableID = "MAIN_IMG_" + id;
                            databaseHelper.createSplitMainImageTable(mainImageTableID);


                            boolean i = databaseHelper.insertData(getIntent().getStringExtra("id"), getIntent().getStringExtra("projectName"), getIntent().getStringExtra("projectDescription"), getIntent().getStringExtra("timeStamp"), getIntent().getStringExtra("projectImage"), "na", getIntent().getStringExtra("splitId"), "true", getIntent().getStringExtra("splitId"), "0", "0", getIntent().getStringExtra("tableName"), getIntent().getStringExtra("roiTableID"), "na", "na", "na", "-1000", "-1000");

                            if (i) {

                                usersDatabase.logUserAction(AuthDialog.activeUserName, AuthDialog.activeUserRole, "Created Split Project", getIntent().getStringExtra("projectName").toString(), getIntent().getStringExtra("id").toString(), "Split");

                                sliceImage();
                            }
                        } else {

                            String idm = "SPLIT_ID" + System.currentTimeMillis();
                            Date date = new Date();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            String timestamp = dateFormat.format(date);
                            String volumePlotTableID = "VOL_" + System.currentTimeMillis();
                            String intensityPlotTableID = "INT_" + System.currentTimeMillis();
                            String plotTableID = "TAB_" + System.currentTimeMillis();

                            boolean i = databaseHelper.insertSplitMainImage("MAIN_IMG_" + getIntent().getStringExtra("id"), idm, "Main Image " + (sizeOfMainImageList + 1), getIntent().getStringExtra("projectImage") + "_" + sizeOfMainImageList, timestamp, "100", "2", getIntent().getStringExtra("roiTableID"), volumePlotTableID, intensityPlotTableID, plotTableID, getIntent().getStringExtra("projectDescription"), "0", "-1000", "-1000");

                            if (i) {
                                databaseHelper.createVolumePlotTable(volumePlotTableID);
                                databaseHelper.createRfVsAreaIntensityPlotTable(intensityPlotTableID);
                                databaseHelper.createAllDataTable(plotTableID);
                                databaseHelper.createSpotLabelTable("LABEL_" + plotTableID);
                            }

                            usersDatabase.logUserAction(AuthDialog.activeUserName, AuthDialog.activeUserRole, "Main Image Added", getIntent().getStringExtra("projectName").toString(), getIntent().getStringExtra("id").toString(), "Split");

                            sliceImage();

                            SplitImage.Companion.setAddingMainImage(false);


                        }

                    } else {
                        Toast.makeText(CroppingTemp.this, "No split added", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        Button btnUndo = findViewById(R.id.btnUndo);
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the undo method to remove the last drawn line
                undoLastLine();
            }
        });

        Source.retake = false;


    }

    public String saveImageToDownloads(Bitmap originalBitmapImage, String projectName, Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = projectName + "_" + timeStamp + ".jpg";

        // Get the Downloads directory
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        // Create the TLC_IMAGES directory inside Downloads
        File tlcImagesDir = new File(downloadsDir, "TLC_IMAGES");
        if (!tlcImagesDir.exists()) {
            if (tlcImagesDir.mkdirs()) {
                Log.d("TAG", "TLC_IMAGES directory created successfully");
            } else {
                Log.e("TAG", "Failed to create TLC_IMAGES directory");
                return null;
            }
        }

        // Save the image inside TLC_IMAGES directory
        File outFile = new File(tlcImagesDir, fileName);

        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(outFile);
            originalBitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
            Log.d("TAG", "Image saved to: " + outFile.getAbsolutePath());

            // Show a Toast message with the file path
            Toast.makeText(context, "Saved to TLC_IMAGES: " + outFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return outFile.getAbsolutePath();
    }


    private void loadOpenCV() {
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private void undoLastLine() {
        if (!verticalLinesXCoordinates.isEmpty()) {

            // Remove the last drawn line coordinate
            verticalLinesXCoordinates.remove(verticalLinesXCoordinates.size() - 1);
            // Redraw the image without the last drawn line
            drawVerticalLinesOnImage();
        }
    }

    private void drawVerticalLinesOnImage() {
        Mat imageWithLines = originalImage.clone();

        Collections.sort(verticalLinesXCoordinates);

        Scalar lineColor = new Scalar(0, 0, 255); // Red color
        int lineThickness = 2;
        for (int x : verticalLinesXCoordinates) {
            drawVerticalLine(imageWithLines, x, lineColor, lineThickness);
        }

        Bitmap bitmapWithLines = Bitmap.createBitmap(imageWithLines.cols(), imageWithLines.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imageWithLines, bitmapWithLines);
        imageView.setImageBitmap(bitmapWithLines);
    }

    private void drawVerticalLine(Mat image, int x, Scalar color, int thickness) {
        Point start = new Point(x, 0);
        Point end = new Point(x, image.rows() - 1);
        Imgproc.line(image, start, end, color, thickness);
    }

    private void sliceImage() {
        List<Mat> slicedImages = new ArrayList<>();
        ArrayList<Bitmap> slicedImagesBitmap = new ArrayList<>();

        // Add the starting point (0) and the ending point (last column) of the image to the list of coordinates
        verticalLinesXCoordinates.add(0, 0);
        verticalLinesXCoordinates.add(originalImage.cols());

        // Loop through the X-coordinates and extract ROIs
        for (int i = 1; i < verticalLinesXCoordinates.size(); i++) {
            int startX = verticalLinesXCoordinates.get(i - 1);
            int endX = verticalLinesXCoordinates.get(i);

            // Define the ROI using the X-coordinates
            int roiWidth = endX - startX;
            Rect roi = new Rect(startX, 0, roiWidth, originalImage.rows());

            // Crop the original image to get the sliced part
            Mat slicedMat = new Mat(originalImage, roi);

            Bitmap slicedBitmap = Bitmap.createBitmap(slicedMat.cols(), slicedMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(slicedMat, slicedBitmap);


            slicedImagesBitmap.add(slicedBitmap);
            // Add the sliced part to the list
            slicedImages.add(slicedMat);

            String fileName = "Main Image " + (sizeOfMainImageList + 1) + " -> Image " + i;

            String roiTableIDF = "ROI_ID" + System.currentTimeMillis();
            String filePath = "SPLIT_NAME" + System.currentTimeMillis() + ".jpg";
            String fileId = "SPLIT_ID" + System.currentTimeMillis();
            String volumePlotTableID = "VOL_" + System.currentTimeMillis();
            String intensityPlotTableID = "INT_" + System.currentTimeMillis();
            String plotTableID = "TAB_" + System.currentTimeMillis();

            Date currentDate = new Date(System.currentTimeMillis());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            String formattedDate = sdf.format(currentDate);

            String timeStamp = formattedDate;

            saveImageViewToFile(slicedBitmap, filePath, this);

            String tempFileName = "TEMP" + filePath;

            Bitmap mainImageBitmap = slicedBitmap;

            saveImageViewToFile(mainImageBitmap, tempFileName, this);

            boolean s = databaseHelper.insertSplitImage(tableName, fileId, fileName,
                    filePath, timeStamp, "100", "2",
                    roiTableIDF, volumePlotTableID, intensityPlotTableID,
                    plotTableID, getIntent().getStringExtra("projectDescription").toString(),
                    "0", "-1000", "-1000");

            if (s) {
                databaseHelper.createVolumePlotTable(volumePlotTableID);
                databaseHelper.createRfVsAreaIntensityPlotTable(intensityPlotTableID);
                databaseHelper.createAllDataTable(plotTableID);
                databaseHelper.createSpotLabelTable("LABEL_" + plotTableID);

            }


//    val volumePlotTableID: String,
//    val intensityPlotTableID: String,
//    val plotTableID: String
        }

        Intent intent = new Intent(CroppingTemp.this, SplitImage.class);
        intent.putExtra("img_path", String.valueOf(getIntent().getStringExtra("img_path")));
        intent.putExtra("p", "pixel");
        intent.putExtra("w", "new");
        intent.putExtra("type", "new");
        intent.putExtra("projectName", getIntent().getStringExtra("projectName"));
        intent.putExtra("projectDescription", getIntent().getStringExtra("projectDescription"));
        intent.putExtra("timeStamp", getIntent().getStringExtra("timeStamp"));
        intent.putExtra("projectImage", getIntent().getStringExtra("projectImage"));
        intent.putExtra("contourImage", getIntent().getStringExtra("contourImage"));
        intent.putExtra("id", getIntent().getStringExtra("id"));
        intent.putExtra("splitId", getIntent().getStringExtra("splitId"));
        intent.putExtra("imageSplitAvailable", getIntent().getStringExtra("imageSplitAvailable"));
        intent.putExtra("projectNumber", getIntent().getStringExtra("projectNumber"));
        intent.putExtra("thresholdVal", getIntent().getStringExtra("thresholdVal"));
        intent.putExtra("numberOfSpots", getIntent().getStringExtra("numberOfSpots"));
        intent.putExtra("tableName", getIntent().getStringExtra("tableName"));
        intent.putExtra("roiTableID", getIntent().getStringExtra("roiTableID"));
        intent.putExtra("volumePlotTableID", "na");
        intent.putExtra("intensityPlotTableID", "na");
        intent.putExtra("plotTableID", "na");
//        intent.putExtra(
//                "volumePlotTableID", getIntent().getStringExtra("volumePlotTableID")
//        );
//        intent.putExtra(
//                "intensityPlotTableID", getIntent().getStringExtra("intensityPlotTableID")
//        );
//        intent.putExtra(
//                "plotTableID", getIntent().getStringExtra("plotTableID")
//        );
        Source.croppedArrayList = slicedImagesBitmap;
        startActivity(intent);
        Source.fileSaved = true;
        finish();
        SplitImage.Companion.setCompleted(true);

        // 'slicedImages' now contains the cropped parts of the image as Mat objects
        // You can further process or display these images as needed
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    rgba = new Mat();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public String saveImageViewToFile(Bitmap originalBitmapImage, String fileName, Context context) {

//        if (originalBitmapImage.getWidth() != originalBitmapImage.getHeight()) {
//            originalBitmapImage = convertToSquareWithTransparentBackground(originalBitmapImage);
//        }


        FileOutputStream outStream = null;
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(new ContextWrapper(context).getExternalMediaDirs()[0], context.getResources().getString(R.string.app_name) + id);
            dir.mkdirs();
            File outFile = new File(dir, fileName);
            outStream = new FileOutputStream(outFile);
            originalBitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
            Log.d("TAG", "onPictureTaken - wrote to " + outFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileName;
    }

    @Override
    protected void onResume() {
        super.onResume();

//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        if (verticalLinesXCoordinates != null) {
            verticalLinesXCoordinates.clear();
            originalImage = new Mat();
            Utils.bitmapToMat(loadedBitmap, originalImage);
            drawVerticalLinesOnImage();
        }

        if (SplitImage.Companion.getCompleted()) {
            finish();
        }


    }

    public Bitmap convertToSquareWithTransparentBackground(Bitmap originalBitmap) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        int newDimension = Math.max(width, height);
        Bitmap newBitmap = Bitmap.createBitmap(newDimension, newDimension, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);

        // Set a highly transparent background color
//        int transparentColor = Color.argb(255, 0, 0, 0);
        canvas.drawColor(Color.WHITE);

        int left = (newDimension - width) / 2;
        int top = (newDimension - height) / 2;

        // Draw the original bitmap onto the new canvas
        canvas.drawBitmap(originalBitmap, left, top, null);

        return newBitmap;
    }

    public static String firebaseURL2 = "analyser-0112553";

}