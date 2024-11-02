package com.aican.tlcanalyzer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aican.tlcanalyzer.utils.Source;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class AutoSplitImage extends AppCompatActivity implements View.OnTouchListener {
    ImageView iv_capture;
    AppCompatButton addBtn, minusBtn, cropBtn;
    ConstraintLayout parentLayout;
    //    ConstraintLayout parentLayout, viewLineUpLayout;
    View upView, bottomView;
    int lockClicked = 0, pos;
    float dy, valueMoveUp, height_in_cm, dx;
    Stack<View> viewStack;
    ViewGroup view;

    @SuppressLint({"ClickableViewAccessibility", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_auto_split_image);
        String path = getIntent().getStringExtra("img_path");
        height_in_cm = getIntent().getFloatExtra("height_in_cm", 0);
        valueMoveUp = getIntent().getFloatExtra("valueUpMove", 0);
        pos = getIntent().getIntExtra("pos", 0);
        Log.e("pos2", String.valueOf(pos));
        upView = findViewById(R.id.viewLineUp);
        bottomView = findViewById(R.id.viewLine);
        iv_capture = findViewById(R.id.iv_capture);
        parentLayout = findViewById(R.id.imgLayout);
//        viewLineUpLayout = findViewById(R.id.layout);
//        viewLineUpLayout.setY(valueMoveUp);

        cropBtn = findViewById(R.id.cropBtn);
        addBtn = findViewById(R.id.addBtn);
        minusBtn = findViewById(R.id.minusBtn);
        iv_capture.setImageURI(Uri.parse(path));
        view = findViewById(android.R.id.content);
        viewStack = new Stack<>();

//        Bitmap originalBitmap = null;


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addViewItem();
            }
        });
        minusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!viewStack.empty()) {
                    View v = viewStack.peek();
                    parentLayout.removeView(v);
                    viewStack.pop();
                }
            }
        });

        cropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap originalBitmap = ((BitmapDrawable) iv_capture.getDrawable()).getBitmap();

                // Perform cropping with the locked line coordinates
                ArrayList<Bitmap> croppedBitmaps = cropImageWithLines(originalBitmap, lockedLineXCoordinates);

                Source.croppedArrayList = croppedBitmaps;
                startActivity(new Intent(AutoSplitImage.this, CroppedImages.class));

            }
        });
    }


    public ArrayList<Bitmap> cropImageWithLines(Bitmap originalBitmap, List<Float> xCoordinates) {
        ArrayList<Bitmap> croppedBitmaps = new ArrayList<>();
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        // Sort the X-coordinates to get them in ascending order
        Collections.sort(xCoordinates);

        // Crop the first section from the left edge to the first line
        float firstX = xCoordinates.get(0);
        if (firstX > 0) {
            Bitmap firstSectionBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, (int) firstX, height);
            croppedBitmaps.add(firstSectionBitmap);
        }

        // Crop the sections between the locked lines
        for (int i = 0; i < xCoordinates.size() - 1; i++) {
            float startX = xCoordinates.get(i);
            float endX = xCoordinates.get(i + 1);
            if (startX >= 0 && endX >= 0) {
                int sectionWidth = (int) (endX - startX);
                Bitmap sectionBitmap = Bitmap.createBitmap(originalBitmap, (int) startX, 0, sectionWidth, height);
                croppedBitmaps.add(sectionBitmap);
            }
        }

        // Crop the last section from the last line to the right edge
        float lastX = xCoordinates.get(xCoordinates.size() - 1);
        int lastSectionWidth = width - (int) lastX;
        if (lastSectionWidth > 0) {
            Bitmap lastSectionBitmap = Bitmap.createBitmap(originalBitmap, (int) lastX, 0, lastSectionWidth, height);
            croppedBitmaps.add(lastSectionBitmap);
        }

        return croppedBitmaps;
    }


//    @SuppressLint("ClickableViewAccessibility")
//    @Override
//    public boolean onTouch(View view, MotionEvent motionEvent) {
//        if (view.getId() == R.id.layout) {
//            switch (motionEvent.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    dx = view.getX() - motionEvent.getRawX();
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    view.setX(motionEvent.getRawX() + dx);
//                    break;
//                default:
//                    return false;
//            }
//            return true;
//        } else {
//            return false;
//        }
//    }
//

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getId() == R.id.layout) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dx = view.getX() - motionEvent.getRawX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    view.setX(motionEvent.getRawX() + dx);
                    break;
                case MotionEvent.ACTION_UP:
                    float bitmapToImageViewRatio = 0.0f;

                    BitmapDrawable drawable = (BitmapDrawable) iv_capture.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    float imageViewWidth = iv_capture.getWidth();
                    float bitmapWidth = bitmap.getWidth();
                    bitmapToImageViewRatio = (float) bitmapWidth / imageViewWidth;

                    float lineXOnBitmap;
                    lineXOnBitmap = (view.getX() - iv_capture.getX()) * bitmapToImageViewRatio;

                    // Save this x-coordinate to a list or use it directly as needed
                    Log.d("X Coordinate", "Line X on Bitmap: " + lineXOnBitmap);
                    TextView textHeight = view.findViewById(R.id.height);
                    textHeight.setText("X: " + lineXOnBitmap);

                    break;
                default:
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }


//    @SuppressLint("ClickableViewAccessibility")
//    @Override
//    public boolean onTouch(View view, MotionEvent motionEvent) {
//        if (view.getId() == R.id.layout) {
//            switch (motionEvent.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    dy = view.getY() - motionEvent.getRawY();
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    view.setY(motionEvent.getRawY() + dy);
//                    break;
//                default:
//                    return false;
//            }
//            return true;
//        } else {
//            return false;
//        }
//    }

    ArrayList<Float> lockedLineXCoordinates = new ArrayList<>();

    public void addViewItem() {
        View layout = getLayoutInflater().inflate(R.layout.line_layout, view, false);
        viewStack.push(layout);
        ImageView lockBtn = layout.findViewById(R.id.lockBtn);
        TextView textHeight = layout.findViewById(R.id.height);
        lockBtn.setBackground(getResources().getDrawable(R.drawable.unlock));

        layout.setOnTouchListener(this);
        lockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lockClicked == 0) {
                    lockClicked = 1;
                    int[] l1 = new int[2];
                    int[] l2 = new int[2];
                    bottomView.getLocationOnScreen(l1);
                    float btl = (float) l1[1] - (((float) bottomView.getHeight()) / (float) 2) - getResources().getDimensionPixelSize(R.dimen.pad);
                    layout.getLocationOnScreen(l2);
                    float k = (float) (((float) (height_in_cm * (l2[1] - pos + getResources().getDimensionPixelSize(R.dimen.pad2)))) /
                            ((float) (btl - pos + getResources().getDimensionPixelSize(R.dimen.pad2))));
                    float actualDistance = (height_in_cm - k) / height_in_cm;
//                    textHeight.setText("Rf " + actualDistance);

                    // my experiment

//                    float x_xis = layout.getX();
//
//                    textHeight.setText("Locked " + x_xis);

                    float bitmapToImageViewRatio = 0.0f;

                    BitmapDrawable drawable = (BitmapDrawable) iv_capture.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    float imageViewWidth = iv_capture.getWidth();
                    float bitmapWidth = bitmap.getWidth();
                    bitmapToImageViewRatio = (float) bitmapWidth / imageViewWidth;

                    float lineXOnBitmap;
                    lineXOnBitmap = (layout.getX() - iv_capture.getX()) * bitmapToImageViewRatio;

                    textHeight.setText("" + lineXOnBitmap);


//                    lockedLineXCoordinates.add(lineXOnBitmap);


                    //

                    textHeight.setVisibility(View.VISIBLE);
                    lockBtn.setBackground(getResources().getDrawable(R.drawable.lock2));
                    layout.setEnabled(false);
                } else {
                    lockClicked = 0;
                    textHeight.setVisibility(View.INVISIBLE);
                    lockBtn.setBackground(getResources().getDrawable(R.drawable.unlock));
                    layout.setEnabled(true);
                    layout.setOnTouchListener(AutoSplitImage.this);
                }

            }
        });
        parentLayout.addView(layout);
    }

    private void openDialog(Bitmap croppedBitmap) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.dialog_image_cropped, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();

        ImageView iv_cropped_image = dialogView.findViewById(R.id.iv_cropped_image);

        iv_cropped_image.setImageBitmap(croppedBitmap);

        alertDialog.show();
    }
}