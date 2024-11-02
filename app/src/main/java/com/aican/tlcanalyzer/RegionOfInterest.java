package com.aican.tlcanalyzer;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aican.tlcanalyzer.adapterClasses.ContourDataAdapter;
import com.aican.tlcanalyzer.cropper.CropImage;
import com.aican.tlcanalyzer.customClasses.LegacyTableView;
import com.aican.tlcanalyzer.dataClasses.ContourData;
import com.aican.tlcanalyzer.dataClasses.RFvsArea;
import com.aican.tlcanalyzer.utils.Source;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RegionOfInterest extends AppCompatActivity {

    Uri image_path;
    ImageView cropped_image;
    ImageView captured_image;
    Button selectRegionOfInterest;
    Button spotContour, generateRoiReport;
    Bitmap bitImage = null;
    int x = 0, y = 0, w = 0, h = 0;
    TextView thresholdValue;
    TextView rfValues;
    int threshVal = 100;
    SeekBar setThreshold;
    EditText numberCount;
    LegacyTableView legacyTableView;
    ArrayList<ContourData> contourDataArrayList;
    ContourDataAdapter contourDataAdapter;
    ImageView back;
    ArrayList<Double> volumeArrayListROI;
    ArrayList<RFvsArea> rFvsAreaArrayList;
    String projectName;

    ImageButton decrement_thres, increment_thres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_of_interest);

        getSupportActionBar().hide();
        back = findViewById(R.id.back);
        legacyTableView = findViewById(R.id.legacy_table_view);
        volumeArrayListROI = new ArrayList<>();

        projectName = getIntent().getStringExtra("projectName");

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        increment_thres = findViewById(R.id.increment_thres);
        decrement_thres = findViewById(R.id.decrement_thres);
        generateRoiReport = findViewById(R.id.generateRoiReport);
        spotContour = findViewById(R.id.spotContours);
        captured_image = findViewById(R.id.captured_image);
        cropped_image = findViewById(R.id.cropped_image);
        selectRegionOfInterest = findViewById(R.id.selectRegionOfInterest);
        rfValues = findViewById(R.id.rfValues);
        thresholdValue = findViewById(R.id.thresholdValue);
        setThreshold = findViewById(R.id.setThreshold);
        numberCount = findViewById(R.id.numberCount);

        image_path = Source.originalImageUri;

        try {
            bitImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_path);

        } catch (IOException e) {
            e.printStackTrace();
        }

        cropped_image.setImageBitmap(bitImage);
        setThreshold.setProgress(100);
        setThreshold.setMax(255);

        setThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshVal = progress;
                thresholdValue.setText("Threshold : " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        increment_thres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threshVal = threshVal + 1;
                thresholdValue.setText("Threshold : " + threshVal);
                setThreshold.setProgress(threshVal);
            }
        });

        decrement_thres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                threshVal = threshVal - 1;
                thresholdValue.setText("Threshold : " + threshVal);
                setThreshold.setProgress(threshVal);
            }
        });

//        captured_image.setImageURI(image_path);

        selectRegionOfInterest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity(image_path)
//                        .setCropShape(CropImageView.CropShape.OVAL)
                        .start(RegionOfInterest.this);
            }
        });

        spotContour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (x == 0 && y == 0 && w == 0 && h == 0) {
                    Source.toast(RegionOfInterest.this, "First select the ROI then spot the contour");
                } else {
                    spotContour();

                }
            }
        });

        thresholdValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = getLayoutInflater().inflate(R.layout.edit_any_thing, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(RegionOfInterest.this)
                        .setView(dialogView);


                AlertDialog alertDialog = builder.create();

                TextView editName = dialogView.findViewById(R.id.editName);
                EditText getValue = dialogView.findViewById(R.id.getValue);
                Button submitBtnD = dialogView.findViewById(R.id.submitBtnD);

                editName.setText("Enter Threshold Value");

                submitBtnD.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (getValue.getText().toString() != null && !getValue.getText().toString().equals("") &&
                                Integer.parseInt(getValue.getText().toString()) <= 256) {
                            setThreshold.setProgress(Integer.parseInt(getValue.getText().toString()));
                            thresholdValue.setText("Threshold : " + getValue.getText().toString());
                            alertDialog.dismiss();
                        } else {
                            Toast.makeText(RegionOfInterest.this, "Enter the valid value", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                alertDialog.show();
            }

        });

        generateRoiReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegionOfInterest.this, ReportGenerate.class)
                        .putExtra("i", "withRoi").putExtra("projectName", projectName));
            }
        });

    }

    public static Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {


                Rect roi = result.getCropRect();

                Bitmap bit = drawRectWithROI(bitImage, roi.left, roi.top, roi.width(), roi.height());

                x = roi.left;
                y = roi.top;
                w = roi.width();
                h = roi.height();
                Source.originalBitmapROI = bit;

                cropped_image.setImageBitmap(bit);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }


    private void spotContour() {
        captured_image.setVisibility(View.VISIBLE);

        rFvsAreaArrayList = new ArrayList<>();
        contourDataArrayList = new ArrayList<>();
        Rect roi = new Rect(x, y, w, h);

        Bitmap bitImage = null;
        try {
            bitImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_path);

        } catch (IOException e) {
            e.printStackTrace();
        }

        Mat firstImage = new Mat();
        Utils.bitmapToMat(bitImage, firstImage);

        Mat roiMat = firstImage.submat(new Range(y, y + h), new Range(x, x + w));

        Mat grayScaleImage = new Mat();
        Imgproc.cvtColor(roiMat, grayScaleImage, Imgproc.COLOR_BGR2GRAY);

        // Apply threshold to convert the grayscale image to a binary image
        Mat binary = new Mat();
        Imgproc.threshold(grayScaleImage, binary, threshVal, 255, 0);

        // Find contours in the binary image
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(binary,
                contours,
                hierarchy,
                Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_NONE);

        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint c1, MatOfPoint c2) {
                double area1 = Imgproc.contourArea(c1);
                double area2 = Imgproc.contourArea(c2);
                return Double.compare(area2, area1);
            }
        });

        if (!numberCount.getText().toString().equals("")) {
            int range = Integer.parseInt(numberCount.getText().toString()) + 1;

            System.out.println("2. This : " + range + " &" + contours.size());


            if (range > contours.size()) {
                Toast.makeText(this, "Only " + contours.size()
                        + " number of contours available, You can check with different threshold value", Toast.LENGTH_LONG).show();
            } else {
                for (int i = 0; i < range; i++) {
                    Imgproc.drawContours(grayScaleImage, contours, i, new Scalar(0, 0, 255), 2);

                }
                for (int i = 1; i < range; i++) {
                    // Get the bounding box of the contour
                    org.opencv.core.Rect boundingRect = Imgproc.boundingRect(contours.get(i));

                    // Calculate the center point of the bounding box
                    Point centerPoint = new Point(boundingRect.x + (boundingRect.width / 2), boundingRect.y + (boundingRect.height / 2));

                    // Calculate the distance traveled by the center point
                    double contourDistance = Math.sqrt(Math.pow(centerPoint.x, 2) + Math.pow(centerPoint.y, 2));

                    // Calculate the distance traveled by the solvent front (assuming a linear gradient)
                    double solventFrontDistance = ((double) grayScaleImage.width() / 2) * (1.0 - (double) threshVal / 255.0);

                    // Calculate the RF value
                    double rfValue = contourDistance / solventFrontDistance;

                    // assuming you have already found the contour and its height
                    int topY = boundingRect.y;
                    int baseY = topY + boundingRect.height;
                    int centerY = topY + boundingRect.height / 2;

                    int imageHeight = bitImage.getHeight();

                    double area = Imgproc.contourArea(contours.get(i));
                    int height = boundingRect.height;

// assuming you have the image height stored in a variable called "imageHeight"
                    double normalizedTopY = 1 - ((double) topY / imageHeight);
                    double normalizedBaseY = 1 - ((double) baseY / imageHeight);
                    double normalizedCenterY = Double.parseDouble(String.format("%.2f", 1 - ((double) centerY / imageHeight)));
                    double cv = 1 / normalizedCenterY;

                    ArrayList<RFvsArea> ar = new ArrayList<>();
                    ar.add(new RFvsArea(normalizedBaseY, 0));
                    ar.add(new RFvsArea(normalizedCenterY, area));
                    ar.add(new RFvsArea(normalizedTopY, 0));

                    System.out.println("Height : " + height + ", Diameter : " + " - >>>>>>" + normalizedTopY + " , "
                            + normalizedBaseY + " , " + normalizedCenterY);

                    rFvsAreaArrayList.addAll(ar);


                    // Calculate the area of the contour

                    // Calculate the volume of the contour (assuming a cylindrical shape)
//            double volume = area * (solventFrontDistance - contourDistance);
                    DecimalFormat df = new DecimalFormat("0.00E0");
                    double number = area * Math.abs(solventFrontDistance - contourDistance);
                    System.out.println(df.format(number));

                    double volume = Double.parseDouble(df.format(number));

                    volumeArrayListROI.add(volume);

                    ContourData contourData = new ContourData(String.valueOf(i), String.valueOf(normalizedCenterY),
                            String.valueOf(normalizedTopY), String.valueOf(normalizedBaseY),
                            String.valueOf(cv), String.valueOf(area), String.valueOf(volume),"na");
                    contourDataArrayList.add(contourData);

                    // Print the RF value, area, and volume of the contour
                    Log.d("Contour " + i + " RF value", String.valueOf(rfValue));
                    Log.d("Contour " + i + " area", String.valueOf(area));
                    Log.d("Contour " + i + " volume", String.valueOf(volume));
                }

            }
        } else {
            Imgproc.drawContours(grayScaleImage, contours, -1, new Scalar(0, 0, 255), 2);
            for (int i = 1; i < contours.size(); i++) {
                // Get the bounding box of the contour
                org.opencv.core.Rect boundingRect = Imgproc.boundingRect(contours.get(i));

                // Calculate the center point of the bounding box
                Point centerPoint = new Point(boundingRect.x + (boundingRect.width / 2), boundingRect.y + (boundingRect.height / 2));

                // Calculate the distance traveled by the center point
                double contourDistance = Math.sqrt(Math.pow(centerPoint.x, 2) + Math.pow(centerPoint.y, 2));

                // Calculate the distance traveled by the solvent front (assuming a linear gradient)
                double solventFrontDistance = ((double) grayScaleImage.width() / 2) * (1.0 - (double) threshVal / 255.0);

                // Calculate the RF value
                double rfValue = contourDistance / solventFrontDistance;

                // assuming you have already found the contour and its height
                int topY = boundingRect.y;
                int baseY = topY + boundingRect.height;
                int centerY = topY + boundingRect.height / 2;

                int imageHeight = bitImage.getHeight();

                double area = Imgproc.contourArea(contours.get(i));

// assuming you have the image height stored in a variable called "imageHeight"
                double normalizedTopY = 1 - ((double) topY / imageHeight);
                double normalizedBaseY = 1 - ((double) baseY / imageHeight);
                double normalizedCenterY = Double.parseDouble(String.format("%.2f", 1 - ((double) centerY / imageHeight)));
                double cv = 1 / normalizedCenterY;

                double[] rfArray = {normalizedBaseY, normalizedCenterY, normalizedTopY};


                for (int j = 0; j < rfArray.length; j++) {
                    RFvsArea rFvsArea = null;
                    if (j == 0) {
                        rFvsArea = new RFvsArea(normalizedBaseY, 0);
                    }
                    if (j == 1) {
                        rFvsArea = new RFvsArea(normalizedCenterY, area);
                    }
                    if (j == 2) {
                        rFvsArea = new RFvsArea(normalizedTopY, 0);
                    }
                    rFvsAreaArrayList.add(rFvsArea);
                }

                // Calculate the area of the contour

                // Calculate the volume of the contour (assuming a cylindrical shape)
//            double volume = area * (solventFrontDistance - contourDistance);
                DecimalFormat df = new DecimalFormat("0.00E0");
                double number = area * Math.abs(solventFrontDistance - contourDistance);
                System.out.println(df.format(number));

                double volume = Double.parseDouble(df.format(number));

                volumeArrayListROI.add(volume);

                ContourData contourData = new ContourData(String.valueOf(i), String.valueOf(normalizedCenterY),
                        String.valueOf(normalizedTopY), String.valueOf(normalizedBaseY), String.valueOf(cv), String.valueOf(area),
                        String.valueOf(volume),"na");
                contourDataArrayList.add(contourData);

                // Print the RF value, area, and volume of the contour
                Log.d("Contour " + i + " RF value", String.valueOf(rfValue));
                Log.d("Contour " + i + " area", String.valueOf(area));
                Log.d("Contour " + i + " volume", String.valueOf(volume));
            }

        }

        Source.rFvsAreaArrayListROI = rFvsAreaArrayList;

        Source.contourDataArrayListROI = contourDataArrayList;

        Source.volumeDATAROI = volumeArrayListROI;
        // Convert the Mat to a Bitmap
        Bitmap bitmap = Bitmap.createBitmap(grayScaleImage.cols(), grayScaleImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(grayScaleImage, bitmap);
        Source.contourBitmapROI = bitmap;
        // Set the Bitmap in the ImageView
        captured_image.setImageBitmap(bitmap);

        plotTable();

    }

    private void plotTable() {
        LegacyTableView.insertLegacyTitle("ID", "Rf", "Cv", "Area", "% area", "Volume");
        float totalArea = 0.0f;

        for (ContourData co : contourDataArrayList) {
            totalArea = totalArea + Float.parseFloat(co.getArea());
        }


        for (ContourData co : contourDataArrayList) {
            LegacyTableView.insertLegacyContent(
                    co.getId(),
                    co.getRf(),
                    String.valueOf(1 / Float.parseFloat(co.getRf())),
                    co.getArea(),
                    String.format("%.2f",
                            ((Float.parseFloat(co.getArea()) / totalArea) * 100)
                    ) + " %",
                    co.getVolume()
            );
        }
        legacyTableView.setTheme(LegacyTableView.CUSTOM);
        //get titles and contents
        legacyTableView.setContent(LegacyTableView.readLegacyContent());
        legacyTableView.setTitle(LegacyTableView.readLegacyTitle());
        legacyTableView.setBottomShadowVisible(true);
        legacyTableView.setHighlight(LegacyTableView.ODD); //highlight rows oddly or evenly
        //tableView.setHighlight(EVEN);
        legacyTableView.setBottomShadowVisible(true);
        legacyTableView.setFooterTextAlignment(LegacyTableView.CENTER);
        legacyTableView.setTableFooterTextSize(5);
        legacyTableView.setTableFooterTextColor("#000000");
        legacyTableView.setTitleTextAlignment(LegacyTableView.CENTER);
        legacyTableView.setContentTextAlignment(LegacyTableView.CENTER);
        legacyTableView.setTablePadding(20);//increasing spacing will increase the table size
        //tableView.setBottomShadowColorTint("#ffffff");

        //tableView.setBackgroundEvenColor("#FFCCBC");
        //tableView.setBackgroundEvenColor("#303F9F");
        legacyTableView.setBackgroundOddColor("#dcdede");
        //you can also declare your color values as global strings to make your work easy :)
        legacyTableView.setHeaderBackgroundLinearGradientBOTTOM("#dcdede"); //header background bottom color
        legacyTableView.setHeaderBackgroundLinearGradientTOP("#dcdede"); //header background top color
        legacyTableView.setBorderSolidColor("#000000");
        legacyTableView.setTitleTextColor("#000000");
        legacyTableView.setTitleFont(LegacyTableView.BOLD);
        legacyTableView.setZoomEnabled(true);
        legacyTableView.setShowZoomControls(true);
        //by default the initial scale is 0, you
        // may change this depending on initiale scale preferences
        //tableView.setInitialScale(100);//default initialScale is zero (0)
        legacyTableView.setContentTextColor("#000000");
        legacyTableView.build();
    }

    public static Bitmap drawRectWithROI(Bitmap inputBitmap, int x, int y, int w, int h) {
        // Convert the bitmap to Mat format
        Mat inputMat = new Mat();
        Utils.bitmapToMat(inputBitmap, inputMat);

        // Create a copy of the input image
        Mat outputMat = inputMat.clone();

        // Define the coordinates of the rectangle
        int fullWidth = inputBitmap.getWidth();  // Get the full width of the image
        Point p1 = new Point(0, y);              // Set x coordinate to 0 for full width
        Point p2 = new Point(fullWidth, y + h);  // Set x coordinate to full width

        Scalar color = new Scalar(255, 0, 255); // Blue color
        int thickness = 2;

        // Draw the rectangle on the output image
        Imgproc.rectangle(outputMat, p1, p2, color, thickness);

        // Convert the output Mat to Bitmap format
        Bitmap outputBitmap = Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputMat, outputBitmap);

        return outputBitmap;
    }


    public static Bitmap drawRectWithROI2(Bitmap inputBitmap, int x, int y, int w, int h) {
        // Convert the bitmap to Mat format
        Mat inputMat = new Mat();
        Utils.bitmapToMat(inputBitmap, inputMat);

        // Create a copy of the input image
        Mat outputMat = inputMat.clone();

        // Define the coordinates of the rectangle
        Point p1 = new Point(x, y);
        Point p2 = new Point(x + w, y + h);
        Scalar color = new Scalar(255, 0, 255); // Blue color
        int thickness = 2;

        // Draw the rectangle on the output image
        Imgproc.rectangle(outputMat, p1, p2, color, thickness);

        // Convert the output Mat to Bitmap format
        Bitmap outputBitmap = Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputMat, outputBitmap);

        return outputBitmap;
    }

    public static Bitmap drawOvalWithROI(Bitmap inputBitmap, int x, int y, int w, int h) {
        // Convert the bitmap to Mat format
        Mat inputMat = new Mat();
        Utils.bitmapToMat(inputBitmap, inputMat);

        // Create a copy of the input image
        Mat outputMat = inputMat.clone();

        // Define the coordinates and axes of the oval
        Point center = new Point(x + w / 2, y + h / 2);
        Size axes = new Size(w / 2, h / 2);
        double angle = 0; // Rotation angle of the ellipse
        double startAngle = 0; // Starting angle of the elliptical arc in degrees
        double endAngle = 360; // Ending angle of the elliptical arc in degrees
        Scalar color = new Scalar(255, 0, 255); // Blue color
        int thickness = 2;

        // Draw the oval on the output image
        Imgproc.ellipse(outputMat, center, axes, angle, startAngle, endAngle, color, thickness);

        // Convert the output Mat to Bitmap format
        Bitmap outputBitmap = Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputMat, outputBitmap);

        return outputBitmap;
    }

    public static double calculateOvalArea(int w, int h) {
        // Calculate the semi-major and semi-minor axes
        double semiMajorAxis = w / 2.0;
        double semiMinorAxis = h / 2.0;

        // Calculate the area of the oval
        double area = Math.PI * semiMajorAxis * semiMinorAxis;

        return area;
    }
    public static int calculateRectangleArea(int w, int h) {
        // Calculate the area of the rectangle
        int area = w * h;
        return area;
    }

    public static double calculateOvalVolume(double a, double b, double c) {
        // Calculate the volume of the ellipsoid

        return (4.0 / 3.0) * Math.PI * a * b * c;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        Source.showContourImg = true;

    }
}