package com.aican.tlcanalyzer.demoActivities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aican.tlcanalyzer.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointD;

import java.util.ArrayList;

public class DragBaselineActivity extends AppCompatActivity implements OnChartGestureListener, OnChartValueSelectedListener {

    private LineChart chart;
    private LineDataSet lineDataSet;
    private LineData lineData;
    private ArrayList<Entry> dataVal;
    private float baselineY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_baseline);

        chart = findViewById(R.id.chart);
        chart.setOnChartGestureListener(this);
        chart.setOnChartValueSelectedListener(this);

        // Example data
        dataVal = new ArrayList<>();
        dataVal.add(new Entry(1, 10));
        dataVal.add(new Entry(2, 20));
//        dataVal.add(new Entry(3, 35));
//        dataVal.add(new Entry(4, 45));
//        dataVal.add(new Entry(5, 58));

        lineDataSet = new LineDataSet(dataVal, "Data Set");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setCircleColor(Color.BLUE);

        lineData = new LineData(lineDataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        // Handle touch gesture start
        baselineY = getBaselineY(me);
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        // Handle touch gesture end
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        // Handle long press
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        // Handle double tap
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        // Handle single tap
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        // Handle fling
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        // Handle scale
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        // Handle translate
        float newBaselineY = getBaselineY(me);

        // Update baseline data
        updateBaselineData(baselineY, newBaselineY);

        baselineY = newBaselineY;

        // Notify the chart that the data has changed
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        // Handle value selection
        Toast.makeText(this, "Value selected: " + e.getY(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {
        // Handle nothing selectedg
    }

    private float getBaselineY(MotionEvent me) {
        MPPointD point = chart.getTransformer(YAxis.AxisDependency.LEFT).getValuesByTouchPoint(me.getX(), 0);
        return (float) point.y;
    }

    private void updateBaselineData(float oldBaselineY, float newBaselineY) {
        for (Entry entry : dataVal) {
            float x = entry.getX();
            float y = entry.getY();
            float deltaY = newBaselineY - oldBaselineY;
            entry.setY(y + deltaY);
        }
    }
}