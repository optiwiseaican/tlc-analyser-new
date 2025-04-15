package com.aican.tlcanalyzer;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class NewAutoSplitImage extends AppCompatActivity implements View.OnTouchListener {
    ImageView iv_capture;
    AppCompatButton addBtn, minusBtn, cropBtn;
    ConstraintLayout parentLayout;
    //    ConstraintLayout parentLayout, viewLineUpLayout;
    int lockClicked = 0, pos;

    float dy, valueMoveUp, height_in_cm, dx;
    Stack<View> viewStack;
    ViewGroup view;

    @SuppressLint({"ClickableViewAccessibility", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_new_auto_split_image);
        String path = getIntent().getStringExtra("img_path");
        height_in_cm = getIntent().getFloatExtra("height_in_cm", 0);
        valueMoveUp = getIntent().getFloatExtra("valueUpMove", 0);
        pos = getIntent().getIntExtra("pos", 0);
        Log.e("pos2", String.valueOf(pos));




        parentLayout = findViewById(R.id.parentLayout);
        iv_capture = findViewById(R.id.iv_capture);


        cropBtn = findViewById(R.id.cropBtn);
        addBtn = findViewById(R.id.addBtn);
        minusBtn = findViewById(R.id.minusBtn);


        iv_capture.setImageURI(Uri.parse(path));
        view = findViewById(android.R.id.content);
        viewStack = new Stack<>();

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
                    Matrix imageMatrix = iv_capture.getImageMatrix();
                    float[] matrixValues = new float[9];
                    imageMatrix.getValues(matrixValues);

                    float imageViewX = iv_capture.getX();
                    float imageViewY = iv_capture.getY();

                    float bitmapLeft = matrixValues[Matrix.MTRANS_X];
                    float lineXOnImageView = motionEvent.getRawX() + dx - imageViewX;
                    float lineXOnBitmap = (lineXOnImageView - bitmapLeft) / matrixValues[Matrix.MSCALE_X];

                    // Save this x-coordinate to a list or use it directly as needed
                    Log.d("X Coordinate", "Line X on Bitmap: " + lineXOnBitmap);

                    // You can update the line position information or display it on the UI as needed.
                    // For example, you can set the text to a TextView on the line_layout:
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
                    layout.setOnTouchListener(NewAutoSplitImage.this);
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

    public static String firebaseUrl3 = "-default-rtdb.firebaseio.com/";

}