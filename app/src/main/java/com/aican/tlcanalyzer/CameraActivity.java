package com.aican.tlcanalyzer;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aican.tlcanalyzer.cropper.CropImage;
import com.aican.tlcanalyzer.utils.Source;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import com.jjoe64.graphview.GraphView;

public class CameraActivity extends AppCompatActivity {
    private ImageCapture imageCapture;
    private boolean flashMode = false;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private boolean isStartedForResult;
    private int sampleIndex = -1;
    ProgressDialog dialog;
    PreviewView preV;
    File outputDirectory;
    Camera camera;
    ExecutorService cameraExecutor;
    ImageView cameraCaptureBtn, flashBtnOn, flashBtnOff, pickImage;
    ActivityResultLauncher<Intent> activityResultLauncher;
    public static int PICK_IMAGE = 1;
    private ImageView focusIndicator;
    private SeekBar exposureSeekBar;
    SeekBar saturationSeekBar;

    File dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        pickImage = findViewById(R.id.pickImage);
        focusIndicator = findViewById(R.id.focus_indicator);
        exposureSeekBar = findViewById(R.id.exposureSeekBar);
        saturationSeekBar = findViewById(R.id.saturationSeekBar);
        isStartedForResult = getIntent().getBooleanExtra(getResources().getString(R.string.isStartedForResultKey), false);
        if (isStartedForResult) {
            sampleIndex = getIntent().getIntExtra(getResources().getString(R.string.sampleIndexKey), -1);
            activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Intent backwardIntent = new Intent();
                        Log.e("TAG", "cameraAct dual: put conc values " + result.getData().getFloatExtra(getResources().getString(R.string.concentrationKey), -4f));

                        backwardIntent.putExtra(getResources().getString(R.string.sampleIndexKey), result.getData().getIntExtra(getResources().getString(R.string.sampleIndexKey), -1));
                        backwardIntent.putExtra(getResources().getString(R.string.concentrationKey), result.getData().getFloatExtra(getResources().getString(R.string.concentrationKey), -1));
                        backwardIntent.putExtra(getResources().getString(R.string.rfArrayKey), result.getData().getFloatArrayExtra(getResources().getString(R.string.rfArrayKey)));

                        setResult(RESULT_OK, backwardIntent);
                    }
                }
                finish();
            });
        }

        getSupportActionBar().hide();

        dir = new File(new ContextWrapper(this).getExternalMediaDirs()[0], getResources().getString(R.string.app_name) + getIntent().getStringExtra("id"));


        dialog = new ProgressDialog(CameraActivity.this);
        // Check camera permissions if all permission granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[]
                    {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            startCamera();
//            try {
//                Thread.sleep(20000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            exposureSeekBar.setProgress(30);
            setExposure(30);
        }
        preV = findViewById(R.id.viewFinder);
        flashBtnOn = findViewById(R.id.flashOn);
        flashBtnOff = findViewById(R.id.flashOff);
        cameraCaptureBtn = findViewById(R.id.camera_capture_button);
        cameraCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setMessage("Processing...");
                dialog.setCancelable(false);
                dialog.show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    takePhoto();
                }
            }
        });
        flashBtnOff.setVisibility(View.VISIBLE);
        flashBtnOn.setVisibility(View.GONE);
        flashBtnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flashBtnOff.setVisibility(View.GONE);
                flashBtnOn.setVisibility(View.VISIBLE);
                if (camera.getCameraInfo().hasFlashUnit()) {
                    camera.getCameraControl().enableTorch(true);
                }
            }
        });
        flashBtnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flashBtnOn.setVisibility(View.GONE);
                flashBtnOff.setVisibility(View.VISIBLE);
                if (camera.getCameraInfo().hasFlashUnit()) {
                    camera.getCameraControl().enableTorch(false);
                }
            }
        });
        outputDirectory = getOutputDirectory();
        cameraExecutor = Executors.newSingleThreadExecutor();
        pickImage.setOnClickListener(v -> {

//            if (Build.VERSION.SDK_INT >= 33) {
//                String[] permissions = new String[]{
//                        Manifest.permission.READ_MEDIA_IMAGES,
//                        Manifest.permission.READ_MEDIA_VIDEO,
//                        Manifest.permission.READ_MEDIA_AUDIO
//                };
//
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
//                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ||
//                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                    // Permissions are not granted, request them
//                    ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION_REQUEST_CODE);
//                } else {
//                    // Permissions are already granted, proceed with using external storage
//                    // Your code for accessing external storage goes here
//                    launchGallery();
//                }
//            } else {
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
//                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                    // Permissions are not granted, request them
//                    ActivityCompat.requestPermissions(this, new String[]{
//                            Manifest.permission.READ_EXTERNAL_STORAGE,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    }, STORAGE_PERMISSION_REQUEST_CODE);
//                } else {
//                    // Permissions are already granted, proceed with using external storage
//                    // Your code for accessing external storage goes here
//                    launchGallery();
//                }
//            }
//
//            if (!checkPermission()) {
//                requestPermission();
//            } else {
//                // Launch the gallery
//                launchGallery();
//            }
            launchGallery();

        });

        preV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Get the touch coordinates
                    float x = event.getX();
                    float y = event.getY();

                    // Call a method to handle focusing
                    handleFocus(x, y);
                }
                return true;
            }
        });


    }


    private void setSaturation(float progress) {
        float saturationValue = (float) progress / 100f;
//        float saturationValue = (float) (progress + 50) / 50.0f; // Adjust the factor as needed

        // Create a new ColorMatrix
        ColorMatrix colorMatrix = new ColorMatrix();

        // Apply saturation to the ColorMatrix
        colorMatrix.setSaturation(saturationValue);

        // Create a ColorMatrixColorFilter with the adjusted ColorMatrix
        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);

        // Apply the ColorMatrixColorFilter to the CameraView (preV)
        preV.getBackground().setColorFilter(colorFilter);
    }


    private void handleFocus(float x, float y) {
        if (camera != null) {
            int viewWidth = preV.getWidth();
            int viewHeight = preV.getHeight();

            MeteringPointFactory factory = preV.getMeteringPointFactory();
            MeteringPoint point = factory.createPoint(x, y);

            FocusMeteringAction action = new FocusMeteringAction
                    .Builder(point, FocusMeteringAction.FLAG_AF)
                    .build();

            camera.getCameraControl().startFocusAndMetering(action);

            // Show the focusing indicator at the touch coordinates
            int indicatorSize = getResources().getDimensionPixelSize(R.dimen.focus_indicator_size);
            int xOffset = (int) x - indicatorSize / 2;
            int yOffset = (int) y - indicatorSize / 2;
            focusIndicator.layout(xOffset, yOffset, xOffset + indicatorSize, yOffset + indicatorSize);
            focusIndicator.setVisibility(View.VISIBLE);

            // Hide the indicator after a short delay (adjust duration as needed)
            new Handler().postDelayed(() -> focusIndicator.setVisibility(View.INVISIBLE), 1000);
        }
    }

    private void handleFocus2(float x, float y) {
        // Check if the camera is available
        if (camera != null) {
            // Get the dimensions of the PreviewView
            int viewWidth = preV.getWidth();
            int viewHeight = preV.getHeight();

            // Convert touch coordinates to a MeteringPoint
            MeteringPointFactory factory = preV.getMeteringPointFactory();
            MeteringPoint point = factory.createPoint(x, y);

            // Create a FocusMeteringAction
            FocusMeteringAction action = new FocusMeteringAction
                    .Builder(point, FocusMeteringAction.FLAG_AF)
                    .build();

            // Trigger focus
            camera.getCameraControl().startFocusAndMetering(action);
        }
    }

    int STORAGE_PERMISSION_REQUEST_CODE = 200;

    private static final int PERMISSION_REQUEST_CODE = 200;

    private boolean checkPermission() {
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

//
//    private void launchGallery() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
//    }

    private void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_IMAGE);
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


    public String saveImageViewToFile(Bitmap originalBitmapImage, String fileName) {
//        if (originalBitmapImage.getWidth() != originalBitmapImage.getHeight()) {
//            originalBitmapImage = convertToSquareWithTransparentBackground(originalBitmapImage);
//        }

        // Get the Drawable from the ImageView
        FileOutputStream outStream = null;

// Write to SD Card
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(new ContextWrapper(this).getExternalMediaDirs()[0], getResources().getString(R.string.app_name));
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

        }
        return fileName;
    }


    // Handle the result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
//                iv_capture.setImageURI(resultUri);

                if (!SplitImage.Companion.getAddingMainImage()) {

//                    SharedPrefData.saveData(this, SharedPrefData.PR_ACTUAL_LIMIT_KEY,
//                            String.valueOf(Subscription.NO_OF_PROJECTS_MADE + 1));


                    Intent intwnt = new Intent(CameraActivity.this, CapturedImagePreview.class);
                    intwnt.putExtra("img_path", String.valueOf(resultUri));
                    intwnt.putExtra("p", "pixel");
                    intwnt.putExtra("w", "new");
                    intwnt.putExtra("prevType", "main");
                    intwnt.putExtra("type", "new");
                    intwnt.putExtra("projectName", getIntent().getStringExtra("projectName"));
                    intwnt.putExtra("projectDescription", getIntent().getStringExtra("projectDescription"));
                    intwnt.putExtra("timeStamp", getIntent().getStringExtra("timeStamp"));
                    intwnt.putExtra("projectImage", getIntent().getStringExtra("projectImage"));
                    intwnt.putExtra("contourImage", getIntent().getStringExtra("contourImage"));
                    intwnt.putExtra("id", getIntent().getStringExtra("id"));
                    intwnt.putExtra("splitId", getIntent().getStringExtra("splitId"));
                    intwnt.putExtra("imageSplitAvailable", getIntent().getStringExtra("imageSplitAvailable"));
                    intwnt.putExtra("projectNumber", getIntent().getStringExtra("projectNumber"));
                    intwnt.putExtra("thresholdVal", getIntent().getStringExtra("thresholdVal"));
                    intwnt.putExtra("numberOfSpots", getIntent().getStringExtra("numberOfSpots"));
                    intwnt.putExtra("tableName", getIntent().getStringExtra("tableName"));
                    intwnt.putExtra("roiTableID", getIntent().getStringExtra("roiTableID"));
                    intwnt.putExtra(
                            "volumePlotTableID", getIntent().getStringExtra("volumePlotTableID")
                    );
                    intwnt.putExtra(
                            "intensityPlotTableID", getIntent().getStringExtra("intensityPlotTableID")
                    );
                    intwnt.putExtra(
                            "plotTableID", getIntent().getStringExtra("plotTableID")
                    );
                    startActivity(intwnt);

                } else {
                    Intent intwnt = new Intent(CameraActivity.this, CapturedImagePreview.class);
                    intwnt.putExtra("img_path", String.valueOf(resultUri));
                    intwnt.putExtra("prevType", "main");
                    intwnt.putExtra("p", "pixel");
                    intwnt.putExtra("w", "new");
                    intwnt.putExtra("type", "new");
                    intwnt.putExtra("projectName", getIntent().getStringExtra("projectName"));
                    intwnt.putExtra("projectDescription", getIntent().getStringExtra("projectDescription"));
                    intwnt.putExtra("timeStamp", getIntent().getStringExtra("timeStamp"));
                    intwnt.putExtra("projectImage", getIntent().getStringExtra("projectImage"));
                    intwnt.putExtra("contourImage", getIntent().getStringExtra("contourImage"));
                    intwnt.putExtra("id", getIntent().getStringExtra("id"));
                    intwnt.putExtra("splitId", getIntent().getStringExtra("splitId"));
                    intwnt.putExtra("imageSplitAvailable", getIntent().getStringExtra("imageSplitAvailable"));
                    intwnt.putExtra("projectNumber", getIntent().getStringExtra("projectNumber"));
                    intwnt.putExtra("thresholdVal", getIntent().getStringExtra("thresholdVal"));
                    intwnt.putExtra("numberOfSpots", getIntent().getStringExtra("numberOfSpots"));
                    intwnt.putExtra("tableName", getIntent().getStringExtra("tableName"));
                    intwnt.putExtra("roiTableID", getIntent().getStringExtra("roiTableID"));
                    intwnt.putExtra(
                            "volumePlotTableID", getIntent().getStringExtra("volumePlotTableID")
                    );
                    intwnt.putExtra(
                            "intensityPlotTableID", getIntent().getStringExtra("intensityPlotTableID")
                    );
                    intwnt.putExtra(
                            "plotTableID", getIntent().getStringExtra("plotTableID")
                    );
                    startActivity(intwnt);
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri picUri = data.getData();//<- get Uri here from data intent

                if (!SplitImage.Companion.getAddingMainImage()) {

                    if (picUri != null) {
//                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                        Bitmap bitmap = null;

                        try {
                            bitmap = android.provider.MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(),
                                    picUri);
//                            SharedPrefData.saveData(this, SharedPrefData.PR_ACTUAL_LIMIT_KEY,
//                                    String.valueOf(Subscription.NO_OF_PROJECTS_MADE + 1));
                            //                        cameraCaptureBtn.setImageURI(savedUri);
                            if (getIntent().getStringExtra("p").equals("pixel")) {
                                cameraCaptureBtn.setClickable(false);
//                                CropImage.activity(picUri)
//                                        .start(CameraActivity.this);


                                Intent intwnt = new Intent(CameraActivity.this, CapturedImagePreview.class);
                                intwnt.putExtra("img_path", String.valueOf(picUri));
                                intwnt.putExtra("prevType", "main");
                                intwnt.putExtra("p", "pixel");
                                intwnt.putExtra("w", "new");
                                intwnt.putExtra("type", "new");
                                intwnt.putExtra("projectName", getIntent().getStringExtra("projectName"));
                                intwnt.putExtra("projectDescription", getIntent().getStringExtra("projectDescription"));
                                intwnt.putExtra("timeStamp", getIntent().getStringExtra("timeStamp"));
                                intwnt.putExtra("projectImage", getIntent().getStringExtra("projectImage"));
                                intwnt.putExtra("contourImage", getIntent().getStringExtra("contourImage"));
                                intwnt.putExtra("id", getIntent().getStringExtra("id"));
                                intwnt.putExtra("splitId", getIntent().getStringExtra("splitId"));
                                intwnt.putExtra("imageSplitAvailable", getIntent().getStringExtra("imageSplitAvailable"));
                                intwnt.putExtra("projectNumber", getIntent().getStringExtra("projectNumber"));
                                intwnt.putExtra("thresholdVal", getIntent().getStringExtra("thresholdVal"));
                                intwnt.putExtra("numberOfSpots", getIntent().getStringExtra("numberOfSpots"));
                                intwnt.putExtra("tableName", getIntent().getStringExtra("tableName"));
                                intwnt.putExtra("roiTableID", getIntent().getStringExtra("roiTableID"));
                                intwnt.putExtra(
                                        "volumePlotTableID", getIntent().getStringExtra("volumePlotTableID")
                                );
                                intwnt.putExtra(
                                        "intensityPlotTableID", getIntent().getStringExtra("intensityPlotTableID")
                                );
                                intwnt.putExtra(
                                        "plotTableID", getIntent().getStringExtra("plotTableID")
                                );
                                startActivity(intwnt);


//                            Intent intent = new Intent(CameraActivity.this, ImageProcess.class);
//                            intent.putExtra("img_path", String.valueOf(savedUri));
//                            startActivity(intent);

                            } else {

//                            Intent intent = new Intent(CameraActivity.this, ShowImgActivity.class);
//                            intent.putExtra("img_path", String.valueOf(picUri));
//                            Log.e("TAG", "onImageSaved: " + picUri);
//                            Toast.makeText(CameraActivity.this, "Image selected", Toast.LENGTH_LONG).show();
//                            cameraCaptureBtn.setClickable(false);
//                            startActivity(intent);
                            }
//                        selectCompanyLogo.setText("Ok!");
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }


                }
                else {
                    if (picUri != null) {
//                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                        Bitmap bitmap = null;

                        try {
                            bitmap = android.provider.MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(),
                                    picUri);
                            //                        cameraCaptureBtn.setImageURI(savedUri);
                            cameraCaptureBtn.setClickable(false);
//                            CropImage.activity(picUri)
//                                    .start(CameraActivity.this);
//                            Intent intent = new Intent(CameraActivity.this, ImageProcess.class);
//                            intent.putExtra("img_path", String.valueOf(savedUri));
//                            startActivity(intent);

                            Intent intwnt = new Intent(CameraActivity.this, CapturedImagePreview.class);
                            intwnt.putExtra("img_path", String.valueOf(picUri));
                            intwnt.putExtra("prevType", "main");
                            intwnt.putExtra("p", "pixel");
                            intwnt.putExtra("w", "new");
                            intwnt.putExtra("type", "new");
                            intwnt.putExtra("projectName", getIntent().getStringExtra("projectName"));
                            intwnt.putExtra("projectDescription", getIntent().getStringExtra("projectDescription"));
                            intwnt.putExtra("timeStamp", getIntent().getStringExtra("timeStamp"));
                            intwnt.putExtra("projectImage", getIntent().getStringExtra("projectImage"));
                            intwnt.putExtra("contourImage", getIntent().getStringExtra("contourImage"));
                            intwnt.putExtra("id", getIntent().getStringExtra("id"));
                            intwnt.putExtra("splitId", getIntent().getStringExtra("splitId"));
                            intwnt.putExtra("imageSplitAvailable", getIntent().getStringExtra("imageSplitAvailable"));
                            intwnt.putExtra("projectNumber", getIntent().getStringExtra("projectNumber"));
                            intwnt.putExtra("thresholdVal", getIntent().getStringExtra("thresholdVal"));
                            intwnt.putExtra("numberOfSpots", getIntent().getStringExtra("numberOfSpots"));
                            intwnt.putExtra("tableName", getIntent().getStringExtra("tableName"));
                            intwnt.putExtra("roiTableID", getIntent().getStringExtra("roiTableID"));
                            intwnt.putExtra(
                                    "volumePlotTableID", getIntent().getStringExtra("volumePlotTableID")
                            );
                            intwnt.putExtra(
                                    "intensityPlotTableID", getIntent().getStringExtra("intensityPlotTableID")
                            );
                            intwnt.putExtra(
                                    "plotTableID", getIntent().getStringExtra("plotTableID")
                            );
                            startActivity(intwnt);

                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }
        }
    }

    public Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path1 = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path1);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
                exposureSeekBar.setProgress(30);
                setExposure(30);
            } else {
                Toast.makeText(this, "Camera permission required.", Toast.LENGTH_LONG).show();
                this.finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(getApplicationContext(), "Permission Granted..", Toast.LENGTH_SHORT).show();
                    launchGallery();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void startCamera2() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    Preview preview = new Preview.Builder().build();
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    preview.setSurfaceProvider(preV.getSurfaceProvider());
                    imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build();
                    cameraProvider.unbindAll();
                    camera = cameraProvider.bindToLifecycle(CameraActivity.this, cameraSelector, preview, imageCapture);

                    // Set exposure compensation
                    CameraControl cameraControl = camera.getCameraControl();
                    CameraInfo cameraInfo = camera.getCameraInfo();

                    if (cameraInfo.getExposureState().isExposureCompensationSupported()) {
                        Toast.makeText(CameraActivity.this, "Treu", Toast.LENGTH_SHORT).show();
                        int minExposure = cameraInfo.getExposureState().getExposureCompensationRange().getLower();
                        int maxExposure = cameraInfo.getExposureState().getExposureCompensationRange().getUpper();
                        int exposureCompensation = 30; // Set exposure to 30

                        exposureCompensation = Math.max(minExposure, Math.min(maxExposure, exposureCompensation));
                        cameraControl.setExposureCompensationIndex(exposureCompensation);
                    }

                } catch (ExecutionException | InterruptedException e) {
                    Toast.makeText(CameraActivity.this, "Error happen", Toast.LENGTH_SHORT).show();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    Preview preview = new Preview.Builder().build();
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    preview.setSurfaceProvider(preV.getSurfaceProvider());
                    imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build();
                    cameraProvider.unbindAll();
                    camera = cameraProvider.bindToLifecycle(CameraActivity.this, cameraSelector, preview, imageCapture);

//                    CameraInfo cameraInfo = camera.getCameraInfo();
//                    float zoomRatio = 1.0f;
                    camera.getCameraControl().setZoomRatio(0.0f);
//                    camera.getCameraControl().setLinearZoom(5.0f); // 0.0f means no zoom

                    setExposure(30);
                } catch (ExecutionException | InterruptedException e) {
                    Toast.makeText(CameraActivity.this, "Error happened", Toast.LENGTH_SHORT).show();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void setExposure(float progress) {
        if (camera != null) {
            CameraControl cameraControl = camera.getCameraControl();

            try {
                CameraInfo cameraInfo = camera.getCameraInfo();
                if (cameraInfo.getExposureState().isExposureCompensationSupported()) {
                    int minExposure = cameraInfo.getExposureState().getExposureCompensationRange().getLower();
                    int maxExposure = cameraInfo.getExposureState().getExposureCompensationRange().getUpper();
                    int exposureCompensation = Math.round(minExposure + (maxExposure - minExposure) * (progress / 100f));

                    cameraControl.setExposureCompensationIndex(exposureCompensation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void startCameraM() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    Preview preview = new Preview.Builder().build();
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    preview.setSurfaceProvider(preV.getSurfaceProvider());
                    imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build();
                    cameraProvider.unbindAll();
                    camera = cameraProvider.bindToLifecycle(CameraActivity.this, cameraSelector, preview, imageCapture);
//                    cameraProvider.unbindAll();


                } catch (ExecutionException | InterruptedException e) {
                    Toast.makeText(CameraActivity.this, "Error happen", Toast.LENGTH_SHORT).show();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private File getOutputDirectory() {
        File mediaDir = new File(new ContextWrapper(this).getExternalMediaDirs()[0], getResources().getString(R.string.app_name));
        if (!mediaDir.exists()) {
            mediaDir.mkdir();
        }
        if (mediaDir != null) {
            return mediaDir;
        } else {
            return getFilesDir();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void takePhoto() {
        if (imageCapture == null) {
            return;
        }
        final File photoFile = new File(outputDirectory,
                new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = Uri.fromFile(photoFile);
                        if (!SplitImage.Companion.getAddingMainImage()) {
                            if (getIntent().getStringExtra("p").equals("pixel")) {
                                cameraCaptureBtn.setClickable(false);
//                            CropImage.activity(savedUri)
//                                    .start(CameraActivity.this);

                                Intent intwnt = new Intent(CameraActivity.this, CapturedImagePreview.class);
                                intwnt.putExtra("img_path", String.valueOf(savedUri));
                                intwnt.putExtra("prevType", "main");
                                intwnt.putExtra("p", "pixel");
                                intwnt.putExtra("w", "new");
                                intwnt.putExtra("type", "new");
                                intwnt.putExtra("projectName", getIntent().getStringExtra("projectName"));
                                intwnt.putExtra("projectDescription", getIntent().getStringExtra("projectDescription"));
                                intwnt.putExtra("timeStamp", getIntent().getStringExtra("timeStamp"));
                                intwnt.putExtra("projectImage", getIntent().getStringExtra("projectImage"));
                                intwnt.putExtra("contourImage", getIntent().getStringExtra("contourImage"));
                                intwnt.putExtra("id", getIntent().getStringExtra("id"));
                                intwnt.putExtra("splitId", getIntent().getStringExtra("splitId"));
                                intwnt.putExtra("imageSplitAvailable", getIntent().getStringExtra("imageSplitAvailable"));
                                intwnt.putExtra("projectNumber", getIntent().getStringExtra("projectNumber"));
                                intwnt.putExtra("thresholdVal", getIntent().getStringExtra("thresholdVal"));
                                intwnt.putExtra("numberOfSpots", getIntent().getStringExtra("numberOfSpots"));
                                intwnt.putExtra("tableName", getIntent().getStringExtra("tableName"));
                                intwnt.putExtra("roiTableID", getIntent().getStringExtra("roiTableID"));
                                intwnt.putExtra(
                                        "volumePlotTableID", getIntent().getStringExtra("volumePlotTableID")
                                );
                                intwnt.putExtra(
                                        "intensityPlotTableID", getIntent().getStringExtra("intensityPlotTableID")
                                );
                                intwnt.putExtra(
                                        "plotTableID", getIntent().getStringExtra("plotTableID")
                                );
                                startActivity(intwnt);

                            } else {

                            }
                        } else {
                            Intent intwnt = new Intent(CameraActivity.this, CapturedImagePreview.class);
                            intwnt.putExtra("img_path", String.valueOf(savedUri));
                            intwnt.putExtra("prevType", "main");
                            intwnt.putExtra("p", "pixel");
                            intwnt.putExtra("w", "new");
                            intwnt.putExtra("type", "new");
                            intwnt.putExtra("projectName", getIntent().getStringExtra("projectName"));
                            intwnt.putExtra("projectDescription", getIntent().getStringExtra("projectDescription"));
                            intwnt.putExtra("timeStamp", getIntent().getStringExtra("timeStamp"));
                            intwnt.putExtra("projectImage", getIntent().getStringExtra("projectImage"));
                            intwnt.putExtra("contourImage", getIntent().getStringExtra("contourImage"));
                            intwnt.putExtra("id", getIntent().getStringExtra("id"));
                            intwnt.putExtra("splitId", getIntent().getStringExtra("splitId"));
                            intwnt.putExtra("imageSplitAvailable", getIntent().getStringExtra("imageSplitAvailable"));
                            intwnt.putExtra("projectNumber", getIntent().getStringExtra("projectNumber"));
                            intwnt.putExtra("thresholdVal", getIntent().getStringExtra("thresholdVal"));
                            intwnt.putExtra("numberOfSpots", getIntent().getStringExtra("numberOfSpots"));
                            intwnt.putExtra("tableName", getIntent().getStringExtra("tableName"));
                            intwnt.putExtra("roiTableID", getIntent().getStringExtra("roiTableID"));
                            intwnt.putExtra(
                                    "volumePlotTableID", getIntent().getStringExtra("volumePlotTableID")
                            );
                            intwnt.putExtra(
                                    "intensityPlotTableID", getIntent().getStringExtra("intensityPlotTableID")
                            );
                            intwnt.putExtra(
                                    "plotTableID", getIntent().getStringExtra("plotTableID")
                            );
                            startActivity(intwnt);
                        }
                        dialog.dismiss();

                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(CameraActivity.this, "Photo capture failed" + exception.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
//            startCamera();

        }
        exposureSeekBar.setProgress(30);

        exposureSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setExposure(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });


        saturationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Calculate the saturation value based on the progress (0-100)
                float saturationValue = progress / 100f;

                // Apply the saturation value to the camera
                setSaturation(saturationValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed for this example
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed for this example
            }
        });
        cameraCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setMessage("Processing...");
                dialog.setCancelable(false);
                dialog.show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    takePhoto();
                }
            }
        });

        if (!Source.retake) {
            finish();
        }

        if (SplitImage.Companion.getCompleted()) {
            finish();
        }


    }

    private int exposureCompensationValue = 0; // Default value

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        exposureSeekBar.setProgress(savedInstanceState.getInt("exposureCompensation", 0));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("exposureCompensation", exposureSeekBar.getProgress());
    }

    private void setExposureCompensation(int value) {
        if (camera != null) {
            CameraControl cameraControl = camera.getCameraControl();
            CameraInfo cameraInfo = camera.getCameraInfo();

            if (cameraInfo.getExposureState().isExposureCompensationSupported()) {
                int minExposure = cameraInfo.getExposureState().getExposureCompensationRange().getLower();
                int maxExposure = cameraInfo.getExposureState().getExposureCompensationRange().getUpper();

                value = Math.max(minExposure, Math.min(maxExposure, value));
                cameraControl.setExposureCompensationIndex(value);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dialog.cancel();
    }
}