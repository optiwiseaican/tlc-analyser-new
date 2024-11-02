package com.aican.tlcanalyzer;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.aican.tlcanalyzer.dataClasses.RFvsArea;
import com.aican.tlcanalyzer.dataClasses.SplitContourData;
import com.aican.tlcanalyzer.databinding.ActivityPlotMultiIntGraphBinding;
import com.aican.tlcanalyzer.utils.Source;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Comparator;

public class PlotMultiIntGraph extends AppCompatActivity {
    ActivityPlotMultiIntGraphBinding binding;
    BarChart barChartROI;
    LineChart intensityChartPlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlotMultiIntGraphBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        plotROIGraphs();
    }

    LineDataSet lineDataSet = new LineDataSet(null, null);

    private void showChart(ArrayList<ILineDataSet> iLineDataSets) {
        lineData = new LineData(iLineDataSets);

        intensityChartPlot.clear();
        intensityChartPlot.setData(lineData);
        intensityChartPlot.invalidate();
    }

    LineData lineData;
    ArrayList<LineDataSet> lineDataSetArrayList;

    private void plotROIGraphs() {


        // intensity plot start

        intensityChartPlot = findViewById(R.id.intensityChartPlot);

//        intensityChartPlot.setDragEnabled(true);
//        intensityChartPlot.setScaleEnabled(false);
//        intensityChartPlot.setPinchZoom(true);

        lineDataSetArrayList = new ArrayList<>();

        LineDataSet lineDataSet = new LineDataSet(null, null);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        for (int i = 0; i < Source.splitContourDataList.size(); i++) {
            SplitContourData multiSplitIntensity = Source.splitContourDataList.get(i);


            ArrayList<RFvsArea> rFvsAreaArrayListROI = multiSplitIntensity.getrFvsAreaArrayList();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                rFvsAreaArrayListROI.sort(Comparator.comparingDouble(RFvsArea::getRf));
            }

            ArrayList<Entry> informationROI = new ArrayList<>();
            for (int j = 0; j < rFvsAreaArrayListROI.size(); j++) {
                informationROI.add(new Entry(Float.parseFloat(String.valueOf(rFvsAreaArrayListROI.get(j).getRf())),
                        Float.parseFloat(String.valueOf(rFvsAreaArrayListROI.get(j).getArea()))));
            }
            lineDataSetArrayList.add(new LineDataSet(informationROI, multiSplitIntensity.getName()));

            lineDataSetArrayList.get(i).setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

//                if (i < lineDataSetArrayList.size()) {
            if (i == 0) {
                lineDataSetArrayList.get(i).setColor(Color.rgb(153, 0, 76));
            }

            if (i == 1) {
                lineDataSetArrayList.get(i).setColor(Color.RED);
            }

            if (i == 2) {
                lineDataSetArrayList.get(i).setColor(Color.GREEN);
            }

            if (i == 3) {
                lineDataSetArrayList.get(i).setColor(Color.BLACK);
            }

            if (i == 4) {
                lineDataSetArrayList.get(i).setColor(Color.BLUE);
            }

            if (i == 5) {
                lineDataSetArrayList.get(i).setColor(Color.RED);
            }

            if (i == 6) {
                lineDataSetArrayList.get(i).setColor(Color.rgb(4, 51, 179));
            }

            if (i == 7) {
                lineDataSetArrayList.get(i).setColor(Color.rgb(148, 74, 0));
            }

            if (i == 8) {
                lineDataSetArrayList.get(i).setColor(Color.rgb(5, 132, 137));
            }
            if (i == 9) {
                lineDataSetArrayList.get(i).setColor(Color.rgb(122, 96, 54));
            }

            if (i == 10) {
                lineDataSetArrayList.get(i).setColor(Color.rgb(102, 0, 0));
            }

            if (i == 11) {
                lineDataSetArrayList.get(i).setColor(Color.rgb(128, 148, 0));
            }

            if (i == 12) {
                lineDataSetArrayList.get(i).setColor(Color.rgb(0, 0, 204));
            }
            if (multiSplitIntensity.isSelected()) {

                dataSets.add(lineDataSetArrayList.get(i));

            }

        }

        showChart(dataSets);

    }
}