package com.aican.tlcanalyzer.dataClasses;

import android.graphics.Color;

public class ContourGraphSelModel {

    String rfTop;
    String rfBottom;
    String rf;

    String id;
    int buttonColor;
    @Override
    public String toString() {
        return "RF Top: " + rfTop + ", RF Bottom: " + rfBottom + ", RF: " + rf + ", Button Color: " + buttonColor;
    }
    public ContourGraphSelModel(String rfTop, String rfBottom, String rf, int buttonColor) {
        this.rfTop = rfTop;
        this.rfBottom = rfBottom;
        this.rf = rf;
        this.buttonColor = buttonColor;
    }

    public ContourGraphSelModel(String rfTop, String rfBottom, String rf, String id, int buttonColor) {
        this.rfTop = rfTop;
        this.rfBottom = rfBottom;
        this.rf = rf;
        this.id = id;
        this.buttonColor = buttonColor;
    }

    public ContourGraphSelModel(String rfTop, String rfBottom, String rf) {
        this.rfTop = rfTop;
        this.rfBottom = rfBottom;
        this.rf = rf;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getButtonColor() {
        return buttonColor;
    }

    public void setButtonColor(int buttonColor) {
        this.buttonColor = buttonColor;
    }

    public String getRfTop() {
        return rfTop;
    }

    public void setRfTop(String rfTop) {
        this.rfTop = rfTop;
    }

    public String getRfBottom() {
        return rfBottom;
    }

    public void setRfBottom(String rfBottom) {
        this.rfBottom = rfBottom;
    }

    public String getRf() {
        return rf;
    }

    public void setRf(String rf) {
        this.rf = rf;
    }
}
