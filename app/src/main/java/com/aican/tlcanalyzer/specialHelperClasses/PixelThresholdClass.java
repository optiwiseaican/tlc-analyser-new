package com.aican.tlcanalyzer.specialHelperClasses;

import android.graphics.Bitmap;
import android.graphics.Color;

public class PixelThresholdClass {

    public static int[] calculateThresholdRangeByPercentage(Bitmap grayscaleBitmap, int regionLeft, int regionTop, int regionWidth, int regionHeight, double startPercentage, double endPercentage, int steps) {
        if (grayscaleBitmap == null || steps <= 0) {
            return null;
        }

        int[] thresholds = new int[steps];
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

        int startThreshold = calculateThresholdByPercentage(histogram, totalPixels, startPercentage);
        int endThreshold = calculateThresholdByPercentage(histogram, totalPixels, endPercentage);

        for (int i = 0; i < steps; i++) {
            thresholds[i] = startThreshold + (endThreshold - startThreshold) * i / (steps - 1);
        }

        return thresholds;
    }

    private static int calculateThresholdByPercentage(int[] histogram, int totalPixels, double percentile) {
        int sum = 0;
        int threshold = 0;

        int percentileThreshold = (int) (totalPixels * percentile);

        for (int i = 0; i < histogram.length; i++) {
            sum += histogram[i];
            if (sum >= percentileThreshold) {
                threshold = i;
                break;
            }
        }

        return threshold;
    }


    public static int autoThresholdByPercentileFromGrayscaleInRegion(
            Bitmap grayscaleBitmap,
            int regionLeft, int regionTop, int regionWidth,
            int regionHeight, double percentile) {

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

        int threshold = calculateThresholdByPercentile(histogram, totalPixels, percentile);

        return threshold;
    }

    private static int calculateThresholdByPercentile(int[] histogram, int totalPixels, double percentile) {
        int sum = 0;
        int threshold = 0;

        int percentileThreshold = (int) (totalPixels * percentile);

        for (int i = 0; i < histogram.length; i++) {
            sum += histogram[i];
            if (sum >= percentileThreshold) {
                threshold = i;
                break;
            }
        }

        return threshold;
    }


    public static int autoThresholdStabilizedFromGrayscaleInRegion(Bitmap grayscaleBitmap, int regionLeft, int regionTop, int regionWidth, int regionHeight) {
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

        // Smooth the histogram to reduce noise
        histogram = smoothHistogram(histogram);

        // Normalize histogram values
        histogram = normalizeHistogram(histogram, totalPixels);

        // Calculate threshold using a modified Otsu's method within the region
        int threshold = calculateThreshold(histogram);

        return threshold;
    }

    // Smoothing the histogram to reduce noise
    private static int[] smoothHistogram(int[] histogram) {
        // Implement histogram smoothing technique here (e.g., apply averaging or Gaussian smoothing)
        // Return the smoothed histogram
        // Example: (for simplicity, using a simple 3-bin moving average smoothing)
        int[] smoothedHistogram = new int[256];
        for (int i = 1; i < 255; i++) {
            smoothedHistogram[i] = (histogram[i - 1] + histogram[i] + histogram[i + 1]) / 3;
        }
        return smoothedHistogram;
    }

    // Normalize histogram values to compensate for varying pixel densities
    private static int[] normalizeHistogram(int[] histogram, int totalPixels) {
        int[] normalizedHistogram = new int[256];
        for (int i = 0; i < 256; i++) {
            normalizedHistogram[i] = (int) Math.round((double) histogram[i] / totalPixels * 1000); // Scale to avoid precision loss
        }
        return normalizedHistogram;
    }

    // Calculate threshold using a modified Otsu's method or other technique


    private static int calculateThreshold(int[] histogram) {
        int threshold = 0;

        // Calculate threshold based on histogram values
        for (int i = 1; i < histogram.length - 1; i++) {
            if (histogram[i] > histogram[i - 1] && histogram[i] > histogram[i + 1]) {
                threshold = i;
                break;
            }
        }

        return threshold;
    }

}
