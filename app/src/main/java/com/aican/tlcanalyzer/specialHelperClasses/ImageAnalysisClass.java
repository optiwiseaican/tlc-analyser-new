package com.aican.tlcanalyzer.specialHelperClasses;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.aican.tlcanalyzer.PeakDetectionAutomatic;
import com.aican.tlcanalyzer.PeakDetectionManually;
import com.aican.tlcanalyzer.PixelGraph;
import com.aican.tlcanalyzer.R;
import com.aican.tlcanalyzer.cropper.CropImage;
import com.aican.tlcanalyzer.cropper.CropImageView;
import com.aican.tlcanalyzer.dataClasses.ManualContour;
import com.aican.tlcanalyzer.utils.Source;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class ImageAnalysisClass {

    Context context;

    public ImageAnalysisClass(Context context) {
        this.context = context;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void filterOutDarkPixelsInRegion(Bitmap bitmap, int regionLeft, int regionTop, int regionWidth, int regionHeight) {
        // Create a mutable copy of the original Bitmap to perform modifications
        Bitmap modifiedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        // Iterate through the specified region and log the pixel values
        for (int y = regionTop; y < regionTop + regionHeight; y++) {
            for (int x = regionLeft; x < regionLeft + regionWidth; x++) {
                int pixel = modifiedBitmap.getPixel(x, y);

                // Extract the RGB color components from the pixel
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                // Log the RGB values of the current pixel
                String logMessage = String.format("Pixel at (%d, %d): R=%d, G=%d, B=%d", x, y, red, green, blue);

//                Log.d("PixelValues", logMessage);

                Log.d("DarknessLevelPixel", calculateLuminance(red, green, blue) + "");

                // Define a threshold for darkness (adjust as needed)
                int darknessThreshold = 50;

                // Check if the pixel is dark or black based on its color components
                if (red < darknessThreshold && green < darknessThreshold && blue < darknessThreshold) {
                    // Replace the dark/black pixel with a white color (for example)
                    modifiedBitmap.setPixel(x, y, Color.WHITE);
                }
            }
        }

        // Use the modifiedBitmap with filtered dark/black pixels as needed
        // For instance, display it in an ImageView or perform further processing
        // (Here, you might return the modifiedBitmap or use it as needed)
        // For example:
        // imageView.setImageBitmap(modifiedBitmap);
    }

    // Calculate luminance based on RGB values
    public static double calculateLuminance(int red, int green, int blue) {
        // Convert RGB to perceived luminance using a formula (e.g., Y = 0.299*R + 0.587*G + 0.114*B)
        return 0.299 * red + 0.587 * green + 0.114 * blue;
    }

    public Bitmap changeColorOfBrightPixelsInRegion(Bitmap originalBitmap, int regionLeft, int regionTop, int regionWidth, int regionHeight, int luminanceThreshold, int newColor) {
        if (originalBitmap == null) {
            return null;
        }

        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        Bitmap modifiedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        for (int y = regionTop; y < Math.min(regionTop + regionHeight, height); y++) {
            for (int x = regionLeft; x < Math.min(regionLeft + regionWidth, width); x++) {
                int pixel = modifiedBitmap.getPixel(x, y);

                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                // Calculate luminance of the pixel
                double luminance = 0.299 * red + 0.587 * green + 0.114 * blue;

                Log.d("DarknessLevelPixel", luminance + "");


                // Check if luminance is greater than the threshold
                if (luminance < luminanceThreshold) {
                    // Change the color of the pixel to the specified new color
                    modifiedBitmap.setPixel(x, y, newColor);
                }
            }
        }

        return modifiedBitmap;
    }

    public int calculateDarkSpotsArea(Bitmap grayscaleBitmap, int regionLeft, int regionTop, int regionWidth, int regionHeight, int luminanceThreshold) {
        if (grayscaleBitmap == null) {
            return 0;
        }

        int darkSpotsArea = 0;

        // Iterate through the specified region
        for (int y = regionTop; y < Math.min(regionTop + regionHeight, grayscaleBitmap.getHeight()); y++) {
            for (int x = regionLeft; x < Math.min(regionLeft + regionWidth, grayscaleBitmap.getWidth()); x++) {
                int pixel = grayscaleBitmap.getPixel(x, y);
                int intensity = Color.red(pixel); // Assuming it's a grayscale image

                // Check if the pixel intensity is below the specified threshold
                if (intensity < luminanceThreshold) {
                    // Increment the area count for dark spots
                    darkSpotsArea++;
                }
            }
        }

        return darkSpotsArea;
    }

    public Bitmap changeColorOfBrightPixelsInRegionArray(Bitmap originalBitmap, int regionLeft, int regionTop, int regionWidth, int regionHeight, int[] luminanceThreshold, int newColor) {
        if (originalBitmap == null) {
            return null;
        }

        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        Bitmap modifiedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        for (int y = regionTop; y < Math.min(regionTop + regionHeight, height); y++) {
            for (int x = regionLeft; x < Math.min(regionLeft + regionWidth, width); x++) {
                int pixel = modifiedBitmap.getPixel(x, y);

                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                // Calculate luminance of the pixel
                double luminance = 0.299 * red + 0.587 * green + 0.114 * blue;

                Log.d("DarknessLevelPixel", luminance + "");


                // Check if luminance is greater than the threshold
                int first = luminanceThreshold[0];
                int second = luminanceThreshold[1];
//                for (int i = 0; i < luminanceThreshold.length; i++) {
//                    if
//                }
//                if (luminance < luminanceThreshold) {
//                    // Change the color of the pixel to the specified new color
//                    modifiedBitmap.setPixel(x, y, newColor);
//                }

                if (first < luminance && luminance <= second) {
                    modifiedBitmap.setPixel(x, y, newColor);

                }

            }
        }

        return modifiedBitmap;
    }

    public int calculateDarkSpotsAreaArray(Bitmap grayscaleBitmap, int regionLeft, int regionTop, int regionWidth, int regionHeight, int[] luminanceThreshold) {
        if (grayscaleBitmap == null) {
            return 0;
        }

        int darkSpotsArea = 0;

        // Iterate through the specified region
        for (int y = regionTop; y < Math.min(regionTop + regionHeight, grayscaleBitmap.getHeight()); y++) {
            for (int x = regionLeft; x < Math.min(regionLeft + regionWidth, grayscaleBitmap.getWidth()); x++) {
                int pixel = grayscaleBitmap.getPixel(x, y);
                int intensity = Color.red(pixel); // Assuming it's a grayscale image

                // Check if the pixel intensity is below the specified threshold
//                if (intensity < luminanceThreshold) {
//                    // Increment the area count for dark spots
//                    darkSpotsArea++;
//                }

                int first = luminanceThreshold[0];
                int second = luminanceThreshold[1];
                if (first < intensity && intensity <= second) {
                    darkSpotsArea++;

                }

            }
        }

        return darkSpotsArea;
    }


    public int autoThresholdFromGrayscaleInRegion(Bitmap grayscaleBitmap, int regionLeft, int regionTop, int regionWidth, int regionHeight) {
        if (grayscaleBitmap == null) {
            return 0;
        }

        int[] histogram = new int[256]; // Array to store histogram values for each intensity level
        int totalPixels = 0;

        // Calculate histogram within the specified region
        for (int y = regionTop; y < Math.min(regionTop + regionHeight, grayscaleBitmap.getHeight()); y++) {
            for (int x = regionLeft; x < Math.min(regionLeft + regionWidth, grayscaleBitmap.getWidth()); x++) {
                int pixel = grayscaleBitmap.getPixel(x, y);
                int intensity = Color.red(pixel); // Assuming it's a grayscale image
                histogram[intensity]++;
                totalPixels++;
            }
        }

        // Calculate threshold using Otsu's method (or other desired method) within the region

        int sum = 0;
        for (int t = 0; t < 256; t++) {
            sum += t * histogram[t];
        }
        int sumB = 0;
        int wB = 0;
        int wF = 0;
        int mB;
        int mF;
        int max = 0;
        int threshold = 0;
        for (int t = 0; t < 256; t++) {
            wB += histogram[t];
            if (wB == 0) continue;
            wF = totalPixels - wB;
            if (wF == 0) break;
            sumB += t * histogram[t];
            mB = sumB / wB;
            mF = (sum - sumB) / wF;
            int between = wB * wF * (mB - mF) * (mB - mF);
            if (between > max) {
                max = between;
                threshold = t;
            }
        }

        return threshold;
    }


    public Bitmap convertToGrayAndReturnBitmap(Mat inputColorMat) {
        // Check if input color Mat is valid
        if (inputColorMat.empty()) {
            return null; // Return null if input is invalid
        }

        // Create an output Mat to store the grayscale image
        Mat grayMat = new Mat();

        // Convert the input color Mat to grayscale
        Imgproc.cvtColor(inputColorMat, grayMat, Imgproc.COLOR_BGR2GRAY);

        // Create a Bitmap from the grayscale Mat
        Bitmap bitmap = Bitmap.createBitmap(grayMat.cols(), grayMat.rows(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(grayMat, bitmap);

        return bitmap; // Return the Bitmap representing the grayscale image
    }

    public String generateUniqueIndexName(ArrayList<ManualContour> manualContourArrayList) {
        int index = 1;
        String indexName = Source.manual_contour_prefix + index;

        while (this.isIndexNameInUse(indexName, manualContourArrayList)) {
            index++;
            indexName = Source.manual_contour_prefix + index;
        }

        return indexName;
    }

    public String saveImageViewToFile(Bitmap originalBitmapImage, String fileName, String id, String[] works, String work) {

//        if (originalBitmapImage.getWidth() != originalBitmapImage.getHeight()) {
//            originalBitmapImage = convertToSquareWithTransparentBackground(originalBitmapImage);
//        }


        FileOutputStream outStream = null;

        // Write to SD Card
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir;
            if (work.equals(works[2])) {
                dir = new File(new ContextWrapper(context).getExternalMediaDirs()[0], context.getResources().getString(R.string.app_name)
//                                + getIntent().getStringExtra("pid")
                        + id);
            } else {
                dir = new File(new ContextWrapper(context).getExternalMediaDirs()[0], context.getResources().getString(R.string.app_name) + id);
            }
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
        }

        return fileName;
    }


    public String saveImageViewToFile(Bitmap originalBitmapImage, String fileName, String id) {
//        if (originalBitmapImage.getWidth() != originalBitmapImage.getHeight()) {
//            originalBitmapImage = convertToSquareWithTransparentBackground(originalBitmapImage);
//        }

        // Get the Drawable from the ImageView
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

        }
        return fileName;
    }

    public void changeROIFunction() {

        Source.changeRoi = true;
        Source.CROP_CODE = 1;
        CropImage.activity(Source.originalImageUri).setGuidelines(CropImageView.Guidelines.ON).setCropShape(CropImageView.CropShape.RECTANGLE).start((Activity) context);


    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public boolean isIndexNameInUse(String indexName, ArrayList<ManualContour> manualContourArrayList) {
        for (ManualContour contour : manualContourArrayList) {
            if (contour.getIndexName().equals(indexName)) {
                return true;
            }
        }
        return false;
    }

    public double calculateIntensity(Mat grayImage, MatOfPoint contour) {

        int pixelCount = 0;
        double totalIntensity = 0.0;

        // Iterate through contour points
        for (Point point : contour.toArray()) {
            // Get pixel value at the contour point
            double intensity = grayImage.get((int) point.y, (int) point.x)[0]; // Use (int) point.y as row and (int) point.x as column

            // Accumulate total intensity and pixel count
            totalIntensity += intensity;
            pixelCount++;
        }

        // Calculate average intensity
        double averageIntensity = totalIntensity / pixelCount;

        return averageIntensity;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public ArrayList<Double> applyMovingAverage(ArrayList<Double> values, int windowSize) {
        ArrayList<Double> smoothedValues = new ArrayList<>();
        LinkedList<Double> window = new LinkedList<>();
        double sum = 0;

        for (int i = 0; i < values.size(); i++) {
            double value = values.get(i);

            sum += value;
            window.add(value);

            if (window.size() > windowSize) {
                sum -= window.remove();
            }

            if (window.size() >= windowSize) {
                smoothedValues.add(sum / windowSize);
            } else {
                smoothedValues.add(sum / window.size());
            }
        }

        return smoothedValues;
    }

    public double[] calculateRFValues(int numParts) {
        double[] rfValues = new double[numParts];

        double increment = 1.0 / numParts; // Calculate the increment for each RF value

        for (int i = 0; i < numParts; i++) {
            rfValues[i] = i * increment; // Calculate the RF value for the current part
        }

        return rfValues;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public ArrayList<Double> calculateIntensities(Mat grayMat, double[] rfValues) {
        ArrayList<Double> intensityValues = new ArrayList<>();

        int numSections = rfValues.length; // Number of sections based on RF values
        int imageWidth = grayMat.cols(); // Width of the grayscale image


        for (int i = 0; i < numSections; i++) {
            double rfValue = rfValues[i];

            Log.e("rfValueFFFF", rfValue + "");


            // Calculate the Y-coordinate for the horizontal line based on RF value
            int horizontalLineY = (int) (rfValue * grayMat.rows());
            System.out.println(grayMat.rows());
            System.out.println(horizontalLineY);
//            int horizontalLineY = (int) (rfValue );

            if (horizontalLineY >= 0 && horizontalLineY < grayMat.rows()) {
                // Extract the intensity values along the horizontal line for the current RF section
                ArrayList<Double> lineIntensityValues = new ArrayList<>();
                for (int x = 0; x < imageWidth; x++) {
                    double intensityValue = grayMat.get(horizontalLineY, x)[0]; // Assuming single-channel intensity
                    lineIntensityValues.add(intensityValue);
                }


                // Calculate the average intensity for the horizontal line
                double averageIntensity = lineIntensityValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

                Log.e("AVGIntensity", averageIntensity + "");
                intensityValues.add(averageIntensity);
            } else {
                // Handle out-of-bounds values
                intensityValues.add(0.0); // Or set it to a suitable default value
            }
        }
        Log.e("AVGIntensity", intensityValues + "");

        return intensityValues;
    }

    public static double[] calculateRFValues(float rfTop, float rfBottom, int numParts) {
        double[] rfValues = new double[numParts];

//        Log.e("NotError", rfTop + ", " + rfBottom);
        double increment = 1.0 / numParts; // Calculate the increment for each RF value

        for (int i = (int) rfBottom; i < rfTop; i++) {
//            Log.e("ValueRangee", "" + i);
            rfValues[i] = i * increment; // Calculate the RF value for the current part
        }

        return rfValues;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean checkIntensities(Mat grayMat, float rfTop, float rfBottom) {

        double[] rfValues = calculateRFValues(rfTop * Source.PARTS_INTENSITY, rfBottom * Source.PARTS_INTENSITY, Source.PARTS_INTENSITY);

        for (int i = 0; i < rfValues.length; i++) {
            Log.e("RFLENGTH", rfValues[i] + "");
        }

        ArrayList<Double> intensityValues = new ArrayList<>();

        int numSections = rfValues.length; // Number of sections based on RF values
        int imageWidth = grayMat.cols(); // Width of the grayscale image

        int noOfPixels = 0;

        Log.e("ImageWidth", imageWidth + "");

        for (int i = 0; i < numSections; i++) {
            double rfValue = rfValues[i];
            if (rfValue != 0) {

                // Calculate the Y-coordinate for the horizontal line based on RF value
                int horizontalLineY = (int) (rfValue * grayMat.rows());
                System.out.println(grayMat.rows());
                System.out.println(horizontalLineY);
//            int horizontalLineY = (int) (rfValue );

                if (horizontalLineY >= 0 && horizontalLineY < grayMat.rows()) {
                    // Extract the intensity values along the horizontal line for the current RF section
                    ArrayList<Double> lineIntensityValues = new ArrayList<>();
                    for (int x = 0; x < imageWidth; x++) {
                        double intensityValue = grayMat.get(horizontalLineY, x)[0]; // Assuming single-channel intensity
                        lineIntensityValues.add(intensityValue);
                    }

                    // Calculate the average intensity for the horizontal line
                    double averageIntensity = lineIntensityValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

                    intensityValues.add(averageIntensity);
                    Log.e("NotError", "Value of " + averageIntensity);

                    if (averageIntensity > 14.0) {
                        Log.e("NotError", +i + " Greater than " + averageIntensity);

                        noOfPixels++;
                    }
                    if (noOfPixels == 2) {
                        return true;
                    }
                } else {
                    // Handle out-of-bounds values
                    intensityValues.add(0.0); // Or set it to a suitable default value
                }
            }
        }

        return false;
    }


    public void plotIntensityGraphAnalysisAndSpot(String projectName, String id, int so) {

        if (Source.rFvsAreaArrayList == null || Source.rFvsAreaArrayList.size() == 0) {
//            Toast.makeText(this, "No data available to plot data", Toast.LENGTH_SHORT).show();

        } else {

            if (so == 0) {
//            context.startActivity(new Intent(context, GraphAnalysisAndSpot.class).putExtra("projectName", projectName).putExtra("id", id));
                context.startActivity(new Intent(context, PeakDetectionManually.class).putExtra("projectName", projectName).putExtra("id", id));

            }
            if (so == 1) {
//            context.startActivity(new Intent(context, GraphAnalysisAndSpot.class).putExtra("projectName", projectName).putExtra("id", id));
                context.startActivity(new Intent(context, PeakDetectionAutomatic.class).putExtra("projectName", projectName).putExtra("id", id));

            }
        }
//        // Plot the intensity graph
//        double[] intensityArray = intensityList.stream().mapToDouble(Double::doubleValue).toArray();
//        Plot2DPanel plot = new Plot2DPanel();
//        plot.addLinePlot("Intensity Graph", intensityArray);
//        JFrame frame = new JFrame("Intensity Graph");
//        frame.setContentPane(plot);
//        frame.setSize(800, 600);
//        frame.setVisible(true);
    }


    public void plotIntensityGraph(String projectName, String id, String plotTableID) {

        if (Source.rFvsAreaArrayList == null || Source.rFvsAreaArrayList.size() == 0) {
//            Toast.makeText(this, "No data available to plot data", Toast.LENGTH_SHORT).show();

        } else {
            context.startActivity(new Intent(context, PixelGraph.class).
                    putExtra("projectName", projectName).
                    putExtra("id", id)
                    .putExtra("plotTableID", plotTableID));
        }
//        // Plot the intensity graph
//        double[] intensityArray = intensityList.stream().mapToDouble(Double::doubleValue).toArray();
//        Plot2DPanel plot = new Plot2DPanel();
//        plot.addLinePlot("Intensity Graph", intensityArray);
//        JFrame frame = new JFrame("Intensity Graph");
//        frame.setContentPane(plot);
//        frame.setSize(800, 600);
//        frame.setVisible(true);
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public File getOutputDirectory() {
        File mediaDir = new File(new ContextWrapper(context).getExternalMediaDirs()[0], context.getResources().getString(R.string.app_name));
        if (!mediaDir.exists()) {
            mediaDir.mkdir();
        }
        if (mediaDir != null) {
            return mediaDir;
        } else {
            return context.getFilesDir();
        }
    }

    public int configToBitDepth(Bitmap.Config config) {
        switch (config) {
            case ALPHA_8:
                return 8;
            case RGB_565:
                return 16;
            case ARGB_8888:
            default:
                return 32;
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

}
