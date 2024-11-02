package com.aican.tlcanalyzer;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.aican.tlcanalyzer.customClasses.LegacyTableView;
import com.aican.tlcanalyzer.dataClasses.ContourData;
import com.aican.tlcanalyzer.database.UsersDatabase;
import com.aican.tlcanalyzer.databinding.ActivityVolumeGraphBinding;
import com.aican.tlcanalyzer.dialog.AuthDialog;
import com.aican.tlcanalyzer.utils.Source;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.util.ArrayList;

public class VolumeGraph extends AppCompatActivity {

    // variable for our bar chart
    BarChart barChart;

    // variable for our bar data.
    BarData barData;

    // variable for our bar data set.
    BarDataSet barDataSet;

    UsersDatabase usersDatabase;

    // array list for storing entries.
    ArrayList barEntriesArrayList;
    LegacyTableView legacyTableView;

    ImageView back;
    private float intensityGap = 100;
    String id;
    ActivityVolumeGraphBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVolumeGraphBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        // initializing variable for bar chart.
        barChart = findViewById(R.id.idBarChart);
        back = findViewById(R.id.back);
        id = getIntent().getStringExtra("id").toString();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        legacyTableView = findViewById(R.id.legacy_table_view);

        usersDatabase = new UsersDatabase(this);
        File exportDir = new File(getExternalFilesDir(null).toString() + "/" + "All PDF Files");

        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        usersDatabase.logUserAction(
                AuthDialog.activeUserName,
                AuthDialog.activeUserRole,
                "Volume Plot",
                getIntent().getStringExtra("projectName").toString(),
                getIntent().getStringExtra("id").toString(),
                AuthDialog.projectType
        );

        String contourImage = getIntent().getStringExtra("contourImage").toString();
        File dir = new File(
                this.getExternalMediaDirs()[0], getResources().getString(R.string.app_name) + id
        );
        File outFile = new File(dir, contourImage);
        if (outFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());
            Source.contourBitmap = myBitmap;
            binding.originalImage.setImageBitmap(Source.contourBitmap);

//            captured_image.setImageBitmap(myBitmap
        } else {
            Source.toast(this, "Contour image not available");
        }

        // calling method to get bar entries.
        getBarEntries();


        // creating a new bar data set.
        barDataSet = new BarDataSet(barEntriesArrayList, "Volume Graph");

        // creating a new bar data and
        // passing our bar data set.
        barData = new BarData(barDataSet);
        barChart.getAxisLeft().setAxisMinimum(0f);


        ////////////
//        barChart.getXAxis().setAxisMinimum(0f);

        double maxValue = getMaxValue(Source.volumeDATA); // Function to get the maximum value

        barChart.getAxisLeft().setAxisMaximum((float) maxValue);  // Adjust the maximum value as needed

        // below line is to set data
        // to our bar chart.
        barChart.setData(barData);

        // adding color to our bar data set.
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        // setting text color.
        barDataSet.setValueTextColor(Color.BLACK);

        // setting text size
        barDataSet.setValueTextSize(16f);

        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return barEntry.getData().toString();
            }
        });


        barChart.getDescription().setEnabled(false);

        plotTable();
    }

    public static double getMaxValue(ArrayList<Double> values) {
        double max = Double.MIN_VALUE;
        for (double value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max + (0.05 * max); // Add 5% of the max value
    }


    private void plotTable() {


        LegacyTableView.insertLegacyTitle("ID", "Rf", "Cv", "Area", "% area", "Volume");

        ArrayList<ContourData> contourDataArrayList = Source.contourDataArrayList;


        float totalArea = 0;

        for (int i = 0; i < contourDataArrayList.size(); i++) {
            totalArea += Float.parseFloat(contourDataArrayList.get(i).getArea());
        }


        for (int i = 0; i < contourDataArrayList.size(); i++) {
            LegacyTableView.insertLegacyContent(
                    contourDataArrayList.get(i).getId(),
                    contourDataArrayList.get(i).getRf(),
                    String.format("%.2f", (Float.parseFloat(String.valueOf(1.0 / Float.parseFloat(contourDataArrayList.get(i).getRf()))))),
                    contourDataArrayList.get(i).getArea(),
                    String.format("%.2f", ((Float.parseFloat(contourDataArrayList.get(i).getArea()) / totalArea) * intensityGap)) + " %",
                    contourDataArrayList.get(i).getVolume()
            );
        }

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

    private void getBarEntries() {
        // creating a new array list
        barEntriesArrayList = new ArrayList<>();


        for (int i = 0; i < Source.volumeDATA.size(); i++) {

            Double value = Source.volumeDATA.get(i);
            // Format the value as needed, e.g., with two decimal places
            String id = Source.contourDataArrayList.get(i).getId();

            String formattedValue = String.format("%.2f", value);
//            BarEntry barEntry = new BarEntry((float) (i + 0.5), Float.parseFloat(formattedValue));
            BarEntry barEntry = new BarEntry(i, Float.parseFloat(formattedValue));
            barEntry.setData(id);
            barEntriesArrayList.add(barEntry);


//            barEntriesArrayList.add(new BarEntry(i, Float.parseFloat(String.valueOf(Source.volumeDATA.get(i)))));
//            Toast.makeText(this, "" + Source.volumeDATA.get(i), Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        Source.showContourImg = true;

    }

}