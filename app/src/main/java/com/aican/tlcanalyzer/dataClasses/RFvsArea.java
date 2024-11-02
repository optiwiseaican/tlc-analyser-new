package com.aican.tlcanalyzer.dataClasses;

import androidx.annotation.NonNull;

public class RFvsArea {
    double rf;
    double area;

    public RFvsArea(double rf, double area) {
        this.rf = rf;
        this.area = area;
    }

    public double getRf() {
        return rf;
    }

    public void setRf(double rf) {
        this.rf = rf;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    @NonNull
    @Override
    public String toString() {
        return "RF : " + rf + " & Area : " + area;
    }
}
