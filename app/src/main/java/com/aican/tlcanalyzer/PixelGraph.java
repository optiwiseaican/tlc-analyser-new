package com.aican.tlcanalyzer;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aican.tlcanalyzer.adapterClasses.ContourIntGraphAdapter;
import com.aican.tlcanalyzer.adapterClasses.MyCustomTableAdapter;
import com.aican.tlcanalyzer.customClasses.LegacyTableView;
import com.aican.tlcanalyzer.dataClasses.AreaWithContourID;
import com.aican.tlcanalyzer.dataClasses.ContourData;
import com.aican.tlcanalyzer.dataClasses.ContourGraphSelModel;
import com.aican.tlcanalyzer.dataClasses.ContourSet;
import com.aican.tlcanalyzer.dataClasses.ContourTableData;
import com.aican.tlcanalyzer.dataClasses.LabelData;
import com.aican.tlcanalyzer.dataClasses.RFvsArea;
import com.aican.tlcanalyzer.database.DatabaseHelper;
import com.aican.tlcanalyzer.database.UsersDatabase;
import com.aican.tlcanalyzer.databinding.ActivityPixelGraphBinding;
import com.aican.tlcanalyzer.demoActivities.DragBaselineActivity;
import com.aican.tlcanalyzer.dialog.AuthDialog;
import com.aican.tlcanalyzer.interfaces.EditCallBack;
import com.aican.tlcanalyzer.interfaces.OnClicksListeners;
import com.aican.tlcanalyzer.interfaces.RemoveContourInterface;
import com.aican.tlcanalyzer.utils.Source;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class PixelGraph extends AppCompatActivity implements OnClicksListeners, EditCallBack {


    DatabaseHelper databaseHelper;

    LineChart chart;
    String mode;
    LineDataSet lineDataSet = new LineDataSet(null, null);
    ArrayList<ILineDataSet> iLineDataSets = new ArrayList<>();
    LineData lineData;
    ArrayList<Double> intensities;
    ArrayList<RFvsArea> rFvsAreaArrayList;
    ArrayList<ContourSet> contourSetArrayList;
    ImageView back;
    UsersDatabase usersDatabase;
    ActivityPixelGraphBinding binding;
    ContourIntGraphAdapter contourIntGraphAdapter;
    ArrayList<ContourData> contourDataArrayListNew;
    LegacyTableView legacyTableView;
    MyCustomTableAdapter myCustomTableAdapter;

    ArrayList<AreaWithContourID> contoursAreaArrayList;
    private TableLayout tableLayout;

    private TableRow tableRowHeader;
    String plotTableID;

    ArrayList<LabelData> labelDataArrayList;

    RemoveContourInterface removeContourInterface;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPixelGraphBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        getSupportActionBar().hide();
        databaseHelper = new DatabaseHelper(this);

        plotTableID = getIntent().getStringExtra("plotTableID");

//        LoadingDialog.cancelLoading();

        back = findViewById(R.id.back);
        tableLayout = findViewById(R.id.table);
        tableLayout.setStretchAllColumns(true);
        legacyTableView = findViewById(R.id.legacy_table_view);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        contoursAreaArrayList = new ArrayList<>();

        usersDatabase = new UsersDatabase(this);

        usersDatabase.logUserAction(AuthDialog.activeUserName, AuthDialog.activeUserRole, "Intensity Plot", getIntent().getStringExtra("projectName").toString(), getIntent().getStringExtra("id").toString(), AuthDialog.projectType);

        binding.showImagebtn.setText(getString(R.string.show_image));
        binding.myImageCard.setVisibility(View.GONE);

        binding.capturedImage.setImageBitmap(Source.contourBitmap);
        insertLabelData();

        final int[] kj = {0};
        binding.showImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (kj[0] == 0) {
                    binding.showImagebtn.setText(getString(R.string.hide_image));
                    binding.myImageCard.setVisibility(View.VISIBLE);
                    kj[0]++;

                } else {
                    binding.showImagebtn.setText(getString(R.string.show_image));
                    binding.myImageCard.setVisibility(View.GONE);

                    kj[0]--;
                }

            }
        });

        binding.zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chart.zoomIn();
            }
        });

        binding.chart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);

                return false;
            }
        });

        binding.zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chart.zoomOut();
            }
        });

        chart = findViewById(R.id.chart);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getDataFromIntent();
        }
//        setupChart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            showContoursList();
        }

        showData();

//        plotTableRecView();
        plotTable();

        binding.graphDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PixelGraph.this, PeakDetectionManually.class)
                        .putExtra("projectName", getIntent().getStringExtra("projectName"))
                        .putExtra("id", getIntent().getStringExtra("id")));

            }
        });

        binding.autoGraphDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PixelGraph.this, PeakDetectionAutomatic.class)
//                startActivity(new Intent(PixelGraph.this, PeakDetection.class)
                        .putExtra("projectName", getIntent().getStringExtra("projectName"))
                        .putExtra("id", getIntent().getStringExtra("id")));

            }
        });

        binding.dragActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PixelGraph.this, DragBaselineActivity.class));
            }
        });

    }

    private void insertLabelData() {
        labelDataArrayList = new ArrayList<>();
        Cursor cursor = databaseHelper.getDataFromTable("LABEL_" + plotTableID);
        if (cursor != null) {


            if (cursor.moveToFirst()) {
                do {

                    labelDataArrayList.add(new LabelData(cursor.getString(0), cursor.getString(1)));

                } while (cursor.moveToNext());
            }
        }
    }


    //on create end

    private void openEditDialog(final TextView textView, String ids) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Label");

        // Set up the input
        final EditText input = new EditText(this);
        input.setText(textView.getText().toString()); // Set the current text to the input
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newText = input.getText().toString();
                textView.setText(newText);
                databaseHelper.updateLabelById("LABEL_" + plotTableID, ids, newText);
                // You can add logic here to update your data model if needed
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    //"ID", "Rf", "Cv", "Area", "% area", "Volume"
    private void createTableRow(String ID, String Rf, String Cv, String Area, String pArea,
                                String volume, String label, int index) {
        TableRow tableRow = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        tableRow.setLayoutParams(lp);

        TextView textViewID = new TextView(this);
        TextView textViewRf = new TextView(this);
        TextView textViewCv = new TextView(this);
        TextView textViewArea = new TextView(this);
        TextView textViewPArea = new TextView(this);
        TextView textViewVolume = new TextView(this);
        TextView textViewLabel = new TextView(this);

        textViewID.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 0));
        textViewRf.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 0.3f));
        textViewCv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1.5f));
        textViewArea.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 0));
        textViewPArea.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 0.3f));
        if (Source.SHOW_VOLUME_DATA)
            textViewVolume.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 0.3f));
        if (Source.SHOW_LABEL_DATA)
            textViewLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 0.3f));

        textViewID.setGravity(Gravity.CENTER);
        textViewRf.setGravity(Gravity.CENTER);
        textViewCv.setGravity(Gravity.CENTER);
        textViewArea.setGravity(Gravity.CENTER);
        textViewPArea.setGravity(Gravity.CENTER);
        textViewVolume.setGravity(Gravity.CENTER);
        textViewLabel.setGravity(Gravity.CENTER);

        textViewCv.setMaxLines(3);
        textViewRf.setMaxLines(2);
        textViewPArea.setMaxLines(2);
        textViewArea.setMaxLines(2);
        textViewVolume.setMaxLines(2);
        textViewLabel.setMaxLines(2);

        textViewID.setPadding(5, 15, 5, 15);
        textViewRf.setPadding(5, 15, 5, 15);
        textViewCv.setPadding(5, 15, 5, 15);
        textViewArea.setPadding(5, 15, 5, 15);
        textViewPArea.setPadding(5, 15, 5, 15);
        textViewVolume.setPadding(5, 15, 5, 15);
        textViewLabel.setPadding(5, 15, 5, 15);

        textViewID.setText(ID);
        textViewRf.setText(Rf);
        textViewCv.setText(Cv);
        textViewArea.setText(Area);
        textViewPArea.setText(pArea);
        textViewVolume.setText(volume);
        textViewLabel.setText(label);

        textViewID.setTextColor(getColor(R.color.black));
        textViewRf.setTextColor(getColor(R.color.black));
        textViewCv.setTextColor(getColor(R.color.black));
        textViewArea.setTextColor(getColor(R.color.black));
        textViewPArea.setTextColor(getColor(R.color.black));
        textViewVolume.setTextColor(getColor(R.color.black));
        textViewLabel.setTextColor(getColor(R.color.black));

        textViewID.setBackgroundResource(R.drawable.cell_shape_white);
        textViewRf.setBackgroundResource(R.drawable.cell_shape_grey);
        textViewCv.setBackgroundResource(R.drawable.cell_shape_white);
        textViewArea.setBackgroundResource(R.drawable.cell_shape_grey);
        textViewPArea.setBackgroundResource(R.drawable.cell_shape_white);
        textViewVolume.setBackgroundResource(R.drawable.cell_shape_grey);
        textViewLabel.setBackgroundResource(R.drawable.cell_shape_grey);


        if (index == -1) {
            tableRowHeader = tableRow;
            textViewID.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) getResources().getDimension(R.dimen.font_size_medium));
            textViewRf.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) getResources().getDimension(R.dimen.font_size_medium));
            textViewCv.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) getResources().getDimension(R.dimen.font_size_medium));
            textViewArea.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) getResources().getDimension(R.dimen.font_size_medium));
            textViewPArea.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) getResources().getDimension(R.dimen.font_size_medium));
            textViewVolume.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) getResources().getDimension(R.dimen.font_size_medium));
            textViewLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) getResources().getDimension(R.dimen.font_size_medium));

//            textViewRf.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_sort_by_alpha_black, 0);
//            textViewCv.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_sort_by_alpha_black, 0);
//            textViewArea.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_sort_by_alpha_black, 0);
//            textViewPArea.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_sort_by_alpha_black, 0);
//            textViewVolume.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_sort_by_alpha_black, 0);

            textViewID.setBackgroundResource(R.drawable.cell_shape_blue);
            textViewRf.setBackgroundResource(R.drawable.cell_shape_blue);
            textViewCv.setBackgroundResource(R.drawable.cell_shape_blue);
            textViewArea.setBackgroundResource(R.drawable.cell_shape_blue);
            textViewPArea.setBackgroundResource(R.drawable.cell_shape_blue);
            textViewVolume.setBackgroundResource(R.drawable.cell_shape_blue);
            textViewLabel.setBackgroundResource(R.drawable.cell_shape_blue);
        } else {
            textViewID.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) getResources().getDimension(R.dimen.font_size_small));
            textViewRf.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) getResources().getDimension(R.dimen.font_size_small));
            textViewCv.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) getResources().getDimension(R.dimen.font_size_small));
            textViewArea.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) getResources().getDimension(R.dimen.font_size_small));
            textViewPArea.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) getResources().getDimension(R.dimen.font_size_small));
            textViewVolume.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) getResources().getDimension(R.dimen.font_size_small));
            textViewLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int) getResources().getDimension(R.dimen.font_size_small));

        }

        textViewLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open a dialog for editing the text
                openEditDialog(textViewLabel, textViewID.getText().toString());
            }
        });

        tableRow.addView(textViewID);
        tableRow.addView(textViewRf);
        tableRow.addView(textViewCv);
        tableRow.addView(textViewArea);
        tableRow.addView(textViewPArea);
        if (Source.SHOW_VOLUME_DATA)
            tableRow.addView(textViewVolume);
        if (Source.SHOW_LABEL_DATA)
            tableRow.addView(textViewLabel);


        tableLayout.addView(tableRow, index + 1);
    }

    private void createTableHeader() {
        tableLayout.removeAllViews();
        createTableRow("ID", "Rf", "Cv", "Area", "% area", "Volume", "Label", -1);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showContoursList() {
        contourDataArrayListNew = new ArrayList<>();

        for (int i = 0; i < Source.contourDataArrayList.size(); i++) {

            int color = getResources().getColor(R.color.grey);

            if (i == 0) {
                color = getResources().getColor(R.color.grey);

            }
            if (i == 1) {
                color = getResources().getColor(R.color.yellow);

            }
            if (i == 2) {
                color = getResources().getColor(R.color.orange);


            }
            if (i == 3) {
                color = getResources().getColor(R.color.blue);


            }
            if (i == 4) {
                color = getColor(R.color.yellow2);


            }
            if (i == 5) {
                color = getColor(R.color.teal);


            }
            if (i == 6) {
                color = getColor(R.color.purple);

            }
            if (i == 7) {
                color = getColor(R.color.green);

            }
            if (i == 8) {
                color = getColor(R.color.pink);

            }
            if (i == 9) {
                color = getColor(R.color.colorPrimary);

            }
            if (i == 10) {
                color = getResources().getColor(R.color.blue2);
            }

            contourDataArrayListNew.add(new ContourData(Source.contourDataArrayList.get(i).getId(),
                    Source.contourDataArrayList.get(i).getRf(),
                    Source.contourDataArrayList.get(i).getRfTop(),
                    Source.contourDataArrayList.get(i).getRfBottom(),
                    Source.contourDataArrayList.get(i).getCv(),
                    Source.formatToTwoDecimalPlaces(Source.contourDataArrayList.get(i).getArea()),
                    Source.contourDataArrayList.get(i).getVolume(),
                    Source.contourDataArrayList.get(i).isSelected(),
                    color));
        }

//        contourDataArrayListNew = Source.contourDataArrayList;

        contourIntGraphAdapter = new ContourIntGraphAdapter(true, this, contourDataArrayListNew,
                0, this, true, true,
                true, this);

        binding.contourListRecView.setAdapter(contourIntGraphAdapter);
        contourIntGraphAdapter.notifyDataSetChanged();


    }

    private void plotTable() {

        createTableHeader();

        LegacyTableView.insertLegacyTitle("ID", "Rf", "Cv", "Area", "% area", "Volume");

        ArrayList<ContourData> contourDataArrayList = Source.contourDataArrayList;


        float totalArea = 0;

        if (contourDataArrayList.size() == contoursAreaArrayList.size()) {

            for (int i = 0; i < contourDataArrayList.size(); i++) {

                totalArea += contoursAreaArrayList.get(i).getArea();

            }

            for (int i = 0; i < contourDataArrayList.size(); i++) {
                LegacyTableView.insertLegacyContent(contourDataArrayList.get(i).getId(),
                        contourDataArrayList.get(i).getRf(),
                        String.format("%.2f", (Float.parseFloat(String.valueOf(1.0 / Float.parseFloat(contourDataArrayList.get(i).getRf()))))),
                        Source.formatToTwoDecimalPlaces(contoursAreaArrayList.get(i).getArea() + ""),
                        String.format("%.2f", ((Float.parseFloat(String.valueOf(contoursAreaArrayList.get(i).getArea())) / totalArea) * 100)) +
                                " %", contourDataArrayList.get(i).getVolume());
            }

            tableLayout.removeAllViews();
            tableLayout.addView(tableRowHeader);
            // data rows
            for (int i = 0; i < contourDataArrayList.size(); i++) {
                createTableRow(
                        contourDataArrayList.get(i).getId(),
                        contourDataArrayList.get(i).getRf(),
                        String.format("%.2f", (Float.parseFloat(String.valueOf(1.0 / Float.parseFloat(contourDataArrayList.get(i).getRf()))))),
                        Source.formatToTwoDecimalPlaces(contoursAreaArrayList.get(i).getArea() + ""),
                        String.format("%.2f", ((Float.parseFloat(String.valueOf(contoursAreaArrayList.get(i).getArea())) / totalArea) * 100)) +
                                " %", contourDataArrayList.get(i).getVolume(),
                        labelDataArrayList.get(i).getLabel(), i

                );
            }

        } else {

            for (int i = 0; i < contourDataArrayList.size(); i++) {

                totalArea += Float.parseFloat(contourDataArrayList.get(i).getArea());

            }

            for (int i = 0; i < contourDataArrayList.size(); i++) {
                LegacyTableView.insertLegacyContent(contourDataArrayList.get(i).getId(),
                        contourDataArrayList.get(i).getRf(),
                        String.format("%.2f", (Float.parseFloat(String.valueOf(1.0 / Float.parseFloat(contourDataArrayList.get(i).getRf()))))),
//                        contourDataArrayList.get(i).getArea(),
                        "null",
//                        String.format("%.2f", ((Float.parseFloat(contourDataArrayList.get(i).getArea()) / totalArea) * 100)) + +
//                                " %"
                        "null", contourDataArrayList.get(i).getVolume());
            }

            tableLayout.removeAllViews();
            tableLayout.addView(tableRowHeader);
            // data rows
            for (int i = 0; i < contourDataArrayList.size(); i++) {
                createTableRow(
                        contourDataArrayList.get(i).getId(),
                        contourDataArrayList.get(i).getRf(),
                        String.format("%.2f", (Float.parseFloat(String.valueOf(1.0 / Float.parseFloat(contourDataArrayList.get(i).getRf()))))),
//                        contourDataArrayList.get(i).getArea(),
                        "null",
//                        String.format("%.2f", ((Float.parseFloat(contourDataArrayList.get(i).getArea()) / totalArea) * 100)) + +
//                                " %"
                        "null", contourDataArrayList.get(i).getVolume(),
                        labelDataArrayList.get(i).getLabel(),
                        i
                );
            }

        }

//        for (int i = 0; i < contourDataArrayList.size(); i++) {
//
//            totalArea += Float.parseFloat(contourDataArrayList.get(i).getArea());
//
//        }
//
//        for (int i = 0; i < contourDataArrayList.size(); i++) {
//            LegacyTableView.insertLegacyContent(contourDataArrayList.get(i).getId(),
//                    contourDataArrayList.get(i).getRf(),
//                    String.format("%.2f", (Float.parseFloat(String.valueOf(1.0 / Float.parseFloat(contourDataArrayList.get(i).getRf()))))),
//                    contourDataArrayList.get(i).getArea(),
//                    String.format("%.2f", ((Float.parseFloat(contourDataArrayList.get(i).getArea()) / totalArea) * 100)) +
//                            " %", contourDataArrayList.get(i).getVolume());
//        }


        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Adjust padding and text size for landscape mode
            legacyTableView.setTablePadding(10); // Set a smaller padding value
            legacyTableView.setContentTextSize(12); // Set a smaller content text size
        }

        legacyTableView.setTheme(LegacyTableView.CUSTOM);
        legacyTableView.setContent(LegacyTableView.readLegacyContent());
        legacyTableView.setTitle(LegacyTableView.readLegacyTitle());
//        legacyTableView.setBottomShadowVisible(true);
        legacyTableView.setHighlight(LegacyTableView.ODD);
        legacyTableView.setBottomShadowVisible(false);
        legacyTableView.setFooterTextAlignment(LegacyTableView.CENTER);
        legacyTableView.setTableFooterTextSize(5);
        legacyTableView.setTableFooterTextColor("#f0f0ff");
        legacyTableView.setTitleTextAlignment(LegacyTableView.CENTER);
        legacyTableView.setContentTextAlignment(LegacyTableView.CENTER);
        legacyTableView.setTablePadding(20);
        legacyTableView.setBackgroundOddColor("#F0F0FF");
        legacyTableView.setHeaderBackgroundLinearGradientBOTTOM("#F0F0FF");
        legacyTableView.setHeaderBackgroundLinearGradientTOP("#F0F0FF");
        legacyTableView.setBorderSolidColor("#f0f0ff");
        legacyTableView.setTitleTextColor("#212121");
        legacyTableView.setTitleFont(LegacyTableView.BOLD);
        legacyTableView.setZoomEnabled(false);
        legacyTableView.setShowZoomControls(false);

        legacyTableView.setContentTextColor("#000000");
        legacyTableView.build();
    }


    public static float adjustRfBottom(float diff) {
        // diff - difference between rfTop and rfBottom
        float minAdjustment = 0.6f;
        float maxAdjustment = 0.88f;
        if (diff == (1 * (Source.PARTS_INTENSITY / 100)) || diff == (2 * (Source.PARTS_INTENSITY / 100))) {
            return maxAdjustment;

        }
        if (diff == (3 * (Source.PARTS_INTENSITY / 100)) || diff == (4 * (Source.PARTS_INTENSITY / 100))) {
            return 0.80f;
//            return minAdjustment + (0.1f * (diff - 1));

        }
        if (diff == (5 * (Source.PARTS_INTENSITY / 100)) || diff == (6 * (Source.PARTS_INTENSITY / 100))) {
            return 0.85f;
//            return minAdjustment + (0.1f * (diff - 1));

        } else {
            return maxAdjustment;
        }
    }

    ArrayList<Entry> information;

    private void showData() {
        //normal
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        Collections.reverse(rFvsAreaArrayList);


        String mySTR = "Pixel Graph";

        information = new ArrayList<>();
        for (int i = 0; i < rFvsAreaArrayList.size(); i++) {

            float scaledYValue = Float.parseFloat(String.valueOf(rFvsAreaArrayList.get(i).getArea())) / Source.SCALING_FACTOR_INT_GRAPH; // Adjust the scaling factor as needed
//            information.add(new Entry(Source.PARTS_INTENSITY - Float.parseFloat(String.valueOf(rFvsAreaArrayList.get(i).getRf())), scaledYValue));
            information.add(new Entry(Source.PARTS_INTENSITY - Float.parseFloat(String.valueOf(rFvsAreaArrayList.get(i).getRf())), Float.parseFloat(String.valueOf(rFvsAreaArrayList.get(i).getArea()))));

        }
//        System.out.println(mySTR);

        ArrayList<ContourGraphSelModel> contourGraphSelModelArrayList = new ArrayList<>();

//        //normal 2593    2129
        for (int i = 0; i < contourDataArrayListNew.size(); i++) {
            contourGraphSelModelArrayList.add(new ContourGraphSelModel(
                    contourDataArrayListNew.get(i).getRfTop(),
                    contourDataArrayListNew.get(i).getRfBottom(),
                    contourDataArrayListNew.get(i).getRf(),
                    contourDataArrayListNew.get(i).getId(),
                    contourDataArrayListNew.get(i).getButtonColor()));
        }
////
        minYValue = Float.MAX_VALUE; // Initialize with a very large value

        for (RFvsArea entry : rFvsAreaArrayList) {
            if (entry.getArea() < minYValue) {
                minYValue = (float) entry.getArea();
            }
        }

//        Log.e("LowestValue", "" + minYValue);


        showChart5Reverse(information, contourGraphSelModelArrayList);

    }


    float minYValue = 0.0f;

    public void showChart5Reverse
            (ArrayList<Entry> dataVal, ArrayList<ContourGraphSelModel> contourDataArray) {

        contoursAreaArrayList.clear();

        iLineDataSets.clear();

        lineDataSet.setValues(dataVal);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setColor(getColor(R.color.purple_200));

        // Disable filling for the entire curve
        lineDataSet.setDrawFilled(false);
        lineDataSet.setLineWidth(1);

        iLineDataSets.add(lineDataSet);

        // Create an ArrayList to hold all shaded regions
        ArrayList<LineDataSet> shadedDataSets = new ArrayList<>();

        for (int i = 0; i < contourDataArray.size(); i++) {
//        for (int i = contourDataArray.size() - 1; i >= 0; i--) {

            float mRFTop = Integer.parseInt(String.format("%.0f", Float.parseFloat(contourDataArray.get(i).getRfTop()) * Source.PARTS_INTENSITY));
            float mRFBottom = Integer.parseInt(String.format("%.0f", Float.parseFloat(contourDataArray.get(i).getRfBottom()) * Source.PARTS_INTENSITY));

            float mRF = Integer.parseInt(String.format("%.0f", Float.parseFloat(contourDataArray.get(i).getRf()) * Source.PARTS_INTENSITY));

            float newRfBottom = mRFBottom * Source.percentRFBottom;
            float newRfTop = mRFTop * Source.percentRFTop;

            String id = contourDataArray.get(i).getId();
            if (id.contains(Source.manual_contour_prefix)) {
                newRfTop = mRFTop;
                newRfBottom = mRFBottom;

            } else {
                newRfTop = mRFTop * Source.percentRFTop;
                newRfBottom = mRFBottom * adjustRfBottom(mRFTop - mRFBottom);
            }
            newRfTop = mRFTop;
            newRfBottom = mRFBottom;


//            Log.e("CON_ID", id);

//            Toast.makeText(this, "ID" + id, Toast.LENGTH_SHORT).show();


            Log.e("ThisIsNotAnErrorVishal", "Top : " + newRfTop + " Bottom : " + newRfBottom + " RF : " + mRF);


            // Top : 426.0 Bottom : 338.0 RF : 380.0
            // Top : 972.0 Bottom : 944.0 RF : 960.0

            //Top : 434.52 Bottom : 297.44 RF : 380.0
            // Top : 991.44 Bottom : 830.72 RF : 960.0


            ArrayList<Entry> shadedRegion = new ArrayList<>();

            float int1 = dataVal.get(0).getY();
            float int2 = 0.0f;

            for (Entry entry : dataVal) {
                float x = entry.getX();
                float y = entry.getY();
                if (x >= newRfBottom && x <= newRfTop) {
                    Entry n = new Entry(x, y);
                    shadedRegion.add(n);
//                    Log.e("Shaded Region", n.toString());
                }
                if (id.contains(Source.manual_contour_prefix)) {
                    if (x == newRfBottom) {
                        int1 = y;
                    }
                    if (x == newRfTop) {
                        int2 = y;
                    }
                } else {
                    if (x == mRFBottom) {
                        int1 = y;
                    }
                    if (x == mRFTop) {
                        int2 = y;
                    }
                }
//                if (x >= newRfTop) {
//                    break;
//                }
            }

            Log.e("Int12VishalYAxisPoints", int1 + ", " + int2);

//            LineData lineData = new LineData();
//            chart.setData(lineData);
//
//            drawSlopeLine(chart, newRfBottom, newRfTop, int1, int2);

            ArrayList<Entry> slopePoints = calculateSlopePoints(newRfBottom, newRfTop, int1, int2, dataVal);

            for (int j = 0; j < slopePoints.size(); j++) {
//                Log.e("SlopePoints", slopePoints.get(j).getX() + ", " + slopePoints.get(j).getY());
            }

//            drawBaseline(chart, slopePoints);

//            drawSlopeLine(chart, newRfBottom, newRfTop, int1, int2);

            // in production use
            double peakArea = calculateAreaUnderCurveNew(shadedRegion) * 100 / Source.PARTS_INTENSITY;


//            double lowerRectangleArea = calculateRectangleArea(minYValue, shadedRegion) * 100 / Source.PARTS_INTENSITY;
            double lowerRectangleArea = calculateAreaRectangleNew((int1 + int2) / 2.0f, slopePoints, int1, int2) * 100 / Source.PARTS_INTENSITY;
            double lowerRectangleArea2 = calculateAreaRectangleNew((int1 + int2) / 2.0f, slopePoints) * 100 / Source.PARTS_INTENSITY;

            // Create a new shaded dataset for the region according to slopePoints
            LineDataSet slopePointsDataSet = new LineDataSet(slopePoints, "Slope Points");
            slopePointsDataSet.setDrawCircles(false);
            slopePointsDataSet.setColor(Color.RED); // Set the color for the region
            slopePointsDataSet.setDrawFilled(true); // Fill the region
            slopePointsDataSet.setFillColor(Color.RED); // Set the fill color for the region
            slopePointsDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            slopePointsDataSet.setLineWidth(0f); // Set the line width to zero for the region

//            shadedDataSets.add(slopePointsDataSet); // Add the new shaded dataset


//            Log.e("LowestPointOnYaxis", minYValue + "");
            // in production use
//            double lowerRectangleArea = calculateAreaRectangleNew(minYValue, shadedRegion) * 100 / Source.PARTS_INTENSITY;

//            Log.e("AreaUnderCurve124", peakArea + "");
//            Log.e("AreaUnderCurveRectangle1", lowerRectangleArea2 + "");
//            Log.e("AreaUnderCurveRectangleNew", lowerRectangleArea + "");

            double finalArea = peakArea - lowerRectangleArea;

            contoursAreaArrayList.add(new AreaWithContourID(id, finalArea));

//            Log.e("FinalArea", finalArea + ", " + id);


            // Create a new LineDataSet for each shaded region 1762  ---  2186
            LineDataSet shadedDataSet = new LineDataSet(shadedRegion, "Shaded Area " + (i + 1));
            shadedDataSet.setDrawCircles(false);

//            shadedDataSet.setColor(getColor(R.color.grey));
            shadedDataSet.setColor(contourDataArray.get(i).getButtonColor());
            shadedDataSet.setDrawFilled(true);
//            shadedDataSet.setFillColor(getColor(R.color.grey));
            shadedDataSet.setFillColor(contourDataArray.get(i).getButtonColor());

//            shadedDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            shadedDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            shadedDataSet.setLineWidth(0f); // Set the line width to zero for the shaded area

            ArrayList<Entry> slopePoints2 = new ArrayList<>();
            slopePoints2.add(new Entry(mRFBottom, int1));
            slopePoints2.add(new Entry(mRFTop, int2));

            LineDataSet baselineDataSet = new LineDataSet(slopePoints2, "Baseline");
            baselineDataSet.setDrawCircles(false);
            baselineDataSet.setColor(Color.BLACK); // Set the color for the baseline
            baselineDataSet.setLineWidth(1f); // Set the line width for the baseline

//            if (id.contains("m")) {
            shadedDataSets.add(baselineDataSet);
//            }
            // Add the shaded dataset to the list of shaded datasets


            // Create a new ArrayList to hold the final shaded region
            ArrayList<Entry> finalShadedRegion = new ArrayList<>();
            List<Entry> lineEntries = new ArrayList<>(); // List to store the entries for the lines

// Iterate over the points of shadedDataSet (whole shaded region)
            for (Entry shadedEntry : shadedDataSet.getValues()) {
                float x = shadedEntry.getX();
                float y = shadedEntry.getY();

                // Find the corresponding y-value in slopePointsDataSet (lower part shaded region)
                float lowerPartY = 0.0f;
                for (Entry slopeEntry : slopePointsDataSet.getValues()) {
                    if (slopeEntry.getX() == x) {
                        lowerPartY = slopeEntry.getY();
                        break;
                    }
                }

                // Subtract the lower part shaded region from the whole shaded region
                float finalY = y - lowerPartY;

//                Log.e("VishalShaded", "FinalY : " + finalY + " , Y : " + y + " ,lowerPartY: " + lowerPartY);
                lineEntries.add(new Entry(x, y));
                lineEntries.add(new Entry(x, lowerPartY));
//                finalShadedRegion.add(new Entry(x, finalY));
            }
            LineDataSet lineDataSet = new LineDataSet(lineEntries, "Lines");

            lineDataSet.setDrawCircles(false);
// Customize the appearance of the lines
            lineDataSet.setColor(contourDataArray.get(i).getButtonColor()); // Set line color
            lineDataSet.setLineWidth(1f);

//            shadedDataSets.add(lineDataSet);


//            shadedDataSets.add(finalShadedDataSet); // Add the new shaded dataset


            shadedDataSets.add(shadedDataSet);
        }

        // Add all shaded datasets to iLineDataSets
        iLineDataSets.addAll(shadedDataSets);

        lineData = new LineData(iLineDataSets);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//        chart.setVisibleXRangeMaximum(20);


        // Find the minimum Y value in your data


        chart.clear();
        chart.setData(lineData);
        chart.invalidate();
    }
    // Function to draw a vertical line segment from (x, startY) to (x, endY)

    private ArrayList<Entry> calculateSlopePoints(float x1, float x2, float int1, float int2, ArrayList<Entry> dataVal) {
        ArrayList<Entry> slopePoints = new ArrayList<>();

        // Calculate slope
        float slope = (int2 - int1) / (x2 - x1);

        // Iterate over the x-axis values and calculate corresponding y-axis values
        for (float x = x1; x <= x2; x++) {
            // Use the slope formula to calculate the corresponding y-axis value
            float y = int1 + slope * (x - x1);
            slopePoints.add(new Entry(x, y));
        }

        return slopePoints;
    }


    private float interpolateY(float int1, float int2, float x, float y1, float y2, ArrayList<Entry> dataVal) {
        // Linear interpolation to find the y value between y1 and y2 at the given x
        return y1 + (y2 - y1) * ((x - int1) / (int2 - int1));
    }

    public static double calculateAreaUnderCurveNew(ArrayList<Entry> shadedRegion) {
        double area = 0.0;

        // Sort the shaded region by x-values to ensure it's in ascending order
        Collections.sort(shadedRegion, new EntryComparator());

        // Calculate area using the rectangle method emphasizing height
        for (Entry entry : shadedRegion) {
            // Consider peak height for the area calculation
            area += entry.getY() * 1; // Height * Width
        }

        return area; // Apply scaling factor if needed
    }

    public static double calculateAreaRectangleNew(float minYValue, ArrayList<Entry> slopePoints, float int1, float int2) {
        double area = 0.0;


        for (Entry entry : slopePoints) {
            area = area + entry.getY() * 1; // Height * Width
        }

        return area;
    }

    public static double calculateAreaRectangleNew(float minYValue, ArrayList<Entry> shadedRegion) {
        double area = 0.0;

        // Sort the shaded region by x-values to ensure it's in ascending order

        // Calculate area using the rectangle method emphasizing height
        for (Entry entry : shadedRegion) {
            // Consider peak height for the area calculation
            area = area + minYValue * 1; // Height * Width
        }

        return area; // Apply scaling factor if needed
    }

    @Override
    public void editOnClick(@NonNull String id, @NonNull String rf, @NonNull String rfTop, @NonNull String rfBottom) {
        Intent inte = new Intent(PixelGraph.this, EditPixelBaseline.class);
        inte.putExtra("spotId", id);
        inte.putExtra("pId", getIntent().getStringExtra("id"));
        inte.putExtra("rf", rf);
        inte.putExtra("rfTop", rfTop);
        inte.putExtra("rfBottom", rfBottom);
        inte.putExtra("plotTableName", plotTableID);
        inte.putExtra("contourJsonFileName", getIntent().getStringExtra("contourJsonFileName"));

        startActivity(inte);
//      overridePendingTransition(R.anim.down_to_up, R.anim.up_to_down)
        overridePendingTransition(0, 0);
    }

    // Custom comparator for sorting Entry objects by x-values
    static class EntryComparator implements Comparator<Entry> {
        @Override
        public int compare(Entry entry1, Entry entry2) {
            return Float.compare(entry1.getX(), entry2.getX());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getDataFromIntent() {
        rFvsAreaArrayList = new ArrayList<>();
        Intent intent = getIntent();
        mode = intent.getStringExtra(getResources().getString(R.string.modeKey));
//        b = intent.getByteArrayExtra(getResources().getString(R.string.pixelsArrayKey));
        intensities = Source.intensities;
//        rFvsAreaArrayList = Source.intensityVsRFArray;
        rFvsAreaArrayList = Source.rFvsAreaArrayList;
        contourSetArrayList = Source.contourSetArrayList;

//        for (int i = 0; i < rFvsAreaArrayList.size(); i++) {
////            System.out.println("Before sort");
////            System.out.println(rFvsAreaArrayList.get(i).getRf() + " , " + rFvsAreaArrayList.get(i).getArea());
//        }

        // Sort the list by the rf value in ascending order
        rFvsAreaArrayList.sort(Comparator.comparingDouble(RFvsArea::getRf));

        String myArray = "[";

        for (int i = 0; i < rFvsAreaArrayList.size(); i++) {

            myArray = myArray + ", " + rFvsAreaArrayList.get(i).getArea();
//            System.out.println("After sort");
//            System.out.println(rFvsAreaArrayList.get(i).getRf() + " , " + rFvsAreaArrayList.get(i).getArea());

        }

        myArray = myArray + " ]";

        System.out.println();
//        System.out.println("p = " + myArray);
        System.out.println();

        exportDataToTextFile(myArray);

//        Log.e( yIntensityPoints",myArray);

    }

    private void exportDataToTextFile(String data) {
        File file = new File(
                getExternalFilesDir(null).toString() + File.separator + "All PDF Files/REPORT_" + plotTableID + ".txt"
        );
//        File file = new File(Environment.getExternalStorageDirectory(), "data" + plotTableID +".txt");
        try {
            FileWriter writer = new FileWriter(file);
            writer.append(data);
            writer.flush();
            writer.close();
//            Toast.makeText(this, "Data exported to data.txt", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
//            Toast.makeText(this, "Failed to export data", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupChart() {
        chart.getDescription().setEnabled(false);
        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
//        chart.getXAxis().setLabelCount(5, true);
        Legend l = chart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(true);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(200f);
        rightAxis.setLabelCount(12);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(200f);
        leftAxis.setLabelCount(12);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(0.0001f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        Source.showContourImg = true;

    }

    @Override
    public void onClick(int position, int parentPosition, String id, String rfTop, String
            rfBottom, String rf, boolean isSelected) {

        float mRFTop = Float.parseFloat(rfTop) * Source.PARTS_INTENSITY;
        float mRFBottom = Float.parseFloat(rfBottom) * Source.PARTS_INTENSITY;
        float mRF = Float.parseFloat(rf) * Source.PARTS_INTENSITY;

//        Log.e("ThisIsNotAnError", "Top : " + mRFTop + " Bottom : " + mRFBottom + " RF : " + mRF);

        ArrayList<ContourGraphSelModel> contourGraphSelModelArrayList = new ArrayList<>();

        for (int i = 0; i < contourDataArrayListNew.size(); i++) {
            if (contourDataArrayListNew.get(i).isSelected()) {

                contourGraphSelModelArrayList.add(new ContourGraphSelModel(contourDataArrayListNew.get(i).getRfTop(),
                        contourDataArrayListNew.get(i).getRfBottom(), contourDataArrayListNew.get(i).getRf(),
                        contourDataArrayListNew.get(i).getId(),
                        contourDataArrayListNew.get(i).getButtonColor()));

            }
        }

        showChart5Reverse(information, contourGraphSelModelArrayList);


    }


    @Override
    public void newOnClick(int position) {

    }

    private void plotTableRecView() {

        ArrayList<ContourTableData> tableDataArrayList = new ArrayList<>();


        ArrayList<ContourData> contourDataArrayList = Source.contourDataArrayList;


        float totalArea = 0;

        for (int i = 0; i < contourDataArrayList.size(); i++) {
            totalArea += Float.parseFloat(contourDataArrayList.get(i).getArea());
        }


        for (int i = 0; i < contourDataArrayList.size(); i++) {
            tableDataArrayList.add(new ContourTableData(contourDataArrayList.get(i).getId(), String.format("%.2f", (Float.parseFloat(contourDataArrayList.get(i).getRf()))), contourDataArrayList.get(i).getRfTop(), contourDataArrayList.get(i).getRfBottom(), String.format("%.2f", (Float.parseFloat(contourDataArrayList.get(i).getCv()))), contourDataArrayList.get(i).getArea(), contourDataArrayList.get(i).getVolume(), String.format("%.2f", ((Float.parseFloat(contourDataArrayList.get(i).getArea()) / totalArea) * Source.PARTS_INTENSITY)) + " %"));
        }
        myCustomTableAdapter = new MyCustomTableAdapter(this, tableDataArrayList);
        binding.contourTableRecView.setAdapter(myCustomTableAdapter);
        myCustomTableAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Source.hideAnalyserLayout = false;

        if (Source.rectangleOneActivityToPixelActivity) {
//            Source.hideAnalyserLayout = true;
            finish();
        }

        if (Source.contourBaselineEdited) {
            Source.hideAnalyserLayout = true;

            finish();
        }

        String tempHourSelected = getIntent().getStringExtra("reason");

        if ("tempIntensityPlot".equals(tempHourSelected)) {
            finish();
        }


        String processing = getIntent().getStringExtra("processing");

        if ("intensity".equals(processing)) {
            finish();
        }


    }


    public static boolean checkForSpotsInRegion(Mat image, String rfTopS, String rfBottomS) {

        float rfTop = Float.parseFloat(rfTopS);
        float rfBottom = Float.parseFloat(rfBottomS);
        int startY = (int) (rfTop * image.rows());
        int endY = (int) (rfBottom * image.rows());

        boolean spotsAvailable = false;


        return spotsAvailable;
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
//
//                processed = false;
//            }

        }

        //
    }

    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i("OpenCV", "OpenCV loaded successfully");
            } else {
                super.onManagerConnected(status);
            }
        }
    };
}